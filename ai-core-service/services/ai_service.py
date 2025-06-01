import json
import re
from typing import List, Dict, Any, Callable, Optional
from langchain.schema import HumanMessage, SystemMessage
from config.service_config import fast_llm  # 모델 설정 import
from utils.json_filter import filter_invalid_amounts
from openai import AsyncOpenAI
import yaml
from difflib import SequenceMatcher

def preprocess_member_names(message_content: str, members: List[Dict[str, str]]) -> str:
    """
    메시지에서 부분 이름을 전체 이름으로 매칭하여 전처리
    """
    # 멤버 이름 매핑 생성 (ID -> 이름)
    member_names = {}
    for member_dict in members:
        for member_id, name in member_dict.items():
            member_names[member_id] = name
    
    # 메시지에서 이름 패턴 찾기 (이름 + 숫자 패턴)
    name_amount_pattern = r'(\w+)\s+(\d+)'
    matches = re.findall(name_amount_pattern, message_content)
    
    processed_content = message_content
    
    for mentioned_name, amount in matches:
        # 가장 유사한 멤버 이름 찾기
        best_match = None
        best_score = 0
        
        for member_id, full_name in member_names.items():
            # 부분 매칭 점수 계산
            if mentioned_name in full_name:
                score = len(mentioned_name) / len(full_name)
                if score > best_score:
                    best_score = score
                    best_match = (member_id, full_name)
            
            # 유사도 매칭
            similarity = SequenceMatcher(None, mentioned_name, full_name).ratio()
            if similarity > 0.6 and similarity > best_score:
                best_score = similarity
                best_match = (member_id, full_name)
        
        # 매칭된 경우 메시지 내용 업데이트
        if best_match and best_score > 0.5:
            member_id, full_name = best_match
            # 원본 메시지에 멤버 정보 추가 (AI가 이해할 수 있도록)
            processed_content = processed_content.replace(
                f"{mentioned_name} {amount}",
                f"{mentioned_name}({full_name}, ID:{member_id}) {amount}"
            )
    
    return processed_content

def sanitize_json_string(json_str: str) -> str:
    """
    JSON 문자열을 정리하여 유효한 형식으로 변환합니다.
    
    Args:
        json_str (str): 정리할 JSON 문자열
        
    Returns:
        str: 정리된 JSON 문자열
    """
    # 시작과 끝의 공백 제거
    json_str = json_str.strip()
    
    # 이미 배열 형식인지 확인
    if json_str.startswith('[') and json_str.endswith(']'):
        # 배열 내부 처리
        json_str = _fix_json_properties(json_str)
    else:
        # 배열이 아닌 경우 처리
        
        # 여러 개의 독립 JSON 객체가 있는지 확인
        # 예: {...} {...} 또는 {...}\n{...}
        if json_str.count('{') > 1 and json_str.count('}') > 1:
            # 정규식으로 모든 JSON 객체 추출
            import re
            objects = re.findall(r'\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}', json_str)
            
            if objects:
                # 각 객체 정리 후 배열로 묶기
                fixed_objects = [_fix_json_properties(obj) for obj in objects]
                json_str = '[' + ','.join(fixed_objects) + ']'
            else:
                # JSON 객체를 찾지 못한 경우, 원래 문자열에서 JSON 배열 부분 추출 시도
                start_idx = json_str.find('[')
                end_idx = json_str.rfind(']') + 1
                if start_idx != -1 and end_idx != -1:
                    json_str = json_str[start_idx:end_idx]
                    json_str = _fix_json_properties(json_str)
                else:
                    # 배열도 찾지 못한 경우, 단일 객체를 배열로 변환 시도
                    start_idx = json_str.find('{')
                    end_idx = json_str.rfind('}') + 1
                    if start_idx != -1 and end_idx != -1:
                        single_object = json_str[start_idx:end_idx]
                        single_object = _fix_json_properties(single_object)
                        json_str = '[' + single_object + ']'
        else:
            # 단일 JSON 객체만 있는 경우
            start_idx = json_str.find('{')
            end_idx = json_str.rfind('}') + 1
            if start_idx != -1 and end_idx != -1:
                single_object = json_str[start_idx:end_idx]
                single_object = _fix_json_properties(single_object)
                json_str = '[' + single_object + ']'
    
    return json_str

def _fix_json_properties(json_str: str) -> str:
    """
    JSON 문자열 내부의 속성명과 문자열을 올바르게 수정합니다.
    
    Args:
        json_str (str): 수정할 JSON 문자열
        
    Returns:
        str: 수정된 JSON 문자열
    """
    # 따옴표가 없는 속성명에 따옴표 추가 (JavaScript 스타일 -> JSON 스타일)
    # 예: {name: "value"} -> {"name": "value"}
    json_str = re.sub(r'([{,])\s*([a-zA-Z0-9_]+)\s*:', r'\1"\2":', json_str)
    
    # 작은따옴표를 큰따옴표로 변환 (JSON은 큰따옴표만 허용)
    # 문자열 내부의 작은따옴표는 건드리지 않기 위해 복잡한 패턴 사용
    in_string = False
    result = []
    for i, char in enumerate(json_str):
        if char == '"' and (i == 0 or json_str[i-1] != '\\'):
            in_string = not in_string
        
        if char == "'" and not in_string:
            result.append('"')
        else:
            result.append(char)
    
    return ''.join(result)

def parse_json_response(response_text: str) -> List[Dict[str, Any]]:
    """
    AI 응답에서 JSON을 파싱하는 함수
    
    Args:
        response_text (str): AI 응답 텍스트
        
    Returns:
        List[Dict[str, Any]]: 파싱된 JSON 배열
    """
    try:
        # 응답이 비어있거나 공백만 있는 경우 처리
        if not response_text or response_text.strip() == "":
            print("빈 응답 - 정산 항목 없음")
            return []
        
        # JSON 문자열 정리
        cleaned_json = sanitize_json_string(response_text)
        
        # 정리된 결과가 비어있는 경우 처리
        if not cleaned_json or cleaned_json.strip() == "":
            print("정리 후 빈 응답 - 정산 항목 없음")
            return []
        
        # JSON 파싱
        result = json.loads(cleaned_json)
        
        # 리스트가 아닌 경우 리스트로 변환
        if not isinstance(result, list):
            result = [result] if result else []
        
        return result
        
    except json.JSONDecodeError as e:
        print(f"경고: 응답이 유효한 JSON 형식이 아닙니다 - {str(e)}")
        print(f"원본 응답: {response_text[:100]}...")  # 디버깅용으로 앞부분만 출력
        return []
    except Exception as e:
        print(f"JSON 파싱 중 오류 발생: {str(e)}")
        return []

async def process_conversation(
    conversation: List[Dict[str, str]], 
    input_prompt: Dict[str, Any],
    callback: Optional[Callable[[List[Dict[str, Any]]], None]] = None,
    members: Optional[List[Dict[str, str]]] = None
) -> List[Dict[str, Any]]:
    """대화 내용을 처리하는 함수 - GPT-3.5 모델 사용 (1차 프롬프트)"""
    try:
        # 프롬프트 구성
        system_prompt = input_prompt.get('system', '')
        input_text = input_prompt.get('input', '')
        
        # 멤버 이름 전처리 적용 (members가 제공된 경우)
        processed_conversation = conversation.copy()
        if members:
            for msg in processed_conversation:
                msg['message_content'] = preprocess_member_names(msg['message_content'], members)
        
        # 특별 규칙 추가 (외화 금액과 멤버 수에 관한 규칙)
        special_rules = """
            【중요 규칙】
            1. 외화 금액은 절대 원화로 변환하지 마세요. 원래 숫자 그대로 사용하세요:
            "23유로", "23EUR", "택시비 23유로" → amount: 23 (절대 23000으로 변환하지 마세요)
            "27유로", "27EUR", "택시비 27유로" → amount: 27 (절대 27000으로 변환하지 마세요)

            2. 대화 시작 부분에 멤버 수(member_count)가 제공됩니다. 이 수와 동일한 인원으로 나눠야 한다는 표현이 있으면 반드시 "n분의1"로 처리하세요.
            예: member_count가 5일 때 "5명이서 나눠야 함" → hint_type: "n분의1"
            
            3. 정산 항목이 있는 경우에만 JSON 배열로 응답하세요:
            - 정산 항목이 있는 경우: JSON 배열로 출력
            - 정산 항목이 없는 경우: 아무것도 출력하지 말 것
            - 일반 대화, 계획/예약 단계: 아무것도 출력하지 말 것
            - 금액이 없는 대화: 아무것도 출력하지 말 것
            
            4. JSON을 출력하는 경우에만 다음 규칙을 따르세요:
            - 모든 결과는 반드시 대괄호([])로 묶어 배열 형태로 반환해야 합니다.
            - 모든 속성명은 큰따옴표(")로 감싸야 합니다.
            - 여러 항목이 있는 경우 쉼표(,)로 구분하고 하나의 배열에 넣어야 합니다.
            
            옳은 예 (정산 항목이 있는 경우): 
            [
              {"speaker": "1", "item": "식당", "amount": 15},
              {"speaker": "2", "item": "호텔", "amount": 50}
            ]
            
            틀린 예 (정산 항목이 없는데 빈 배열 출력):
            []
            
            올바른 예 (정산 항목이 없는 경우):
            (아무것도 출력하지 않음)
            """
        
        # 시스템 프롬프트에 특별 규칙 추가
        enhanced_system_prompt = system_prompt + special_rules
        
        # 대화 내용을 문자열로 변환 (전처리된 내용 사용)
        conversation_text = "\n".join([
            f"{msg['speaker']}: {msg['message_content']}"
            for msg in processed_conversation
        ])
        
        # 메시지 구성
        messages = [
            SystemMessage(content=enhanced_system_prompt),
            HumanMessage(content=input_text + "\n\n" + conversation_text)
        ]
        
        # GPT-3.5 모델 호출
        response = await fast_llm.ainvoke(messages)
        full_response = response.content
        
        # JSON 파싱
        result = parse_json_response(full_response)
        
        # 1차 프롬프트에서만 amount 필터링 적용
        filtered_result = filter_invalid_amounts(result)
        
        if len(filtered_result) != len(result):
            print(f"필터링 완료: {len(result)} → {len(filtered_result)}개 항목")
        
        # 결과가 있는 경우 콜백 호출
        if filtered_result and callback:
            await callback(filtered_result)
        
        return filtered_result
            
    except Exception as e:
        print(f"대화 처리 중 오류 발생: {str(e)}")
        return []

async def process_summary(
    conversation: List[Dict[str, str]], 
    input_prompt: Dict[str, Any],
    callback: Optional[Callable[[List[Dict[str, Any]]], None]] = None
) -> List[Dict[str, Any]]:
    """2차 검증을 위한 함수 - 단순 JSON 검증용"""
    try:
        # 프롬프트 구성
        system_prompt = input_prompt.get('system', '')
        input_text = input_prompt.get('input', '')
        
        # JSON 형식 유효성 규칙 추가
        json_rules = """
        【JSON 응답 규칙】
        항상 유효한 JSON 형식으로 응답해야 합니다. 다음 규칙을 반드시 지켜주세요:
        - 모든 결과는 반드시 대괄호([])로 묶어 배열 형태로 반환해야 합니다.
        - 모든 속성명은 큰따옴표(")로 감싸야 합니다.
        - 여러 항목이 있는 경우 쉼표(,)로 구분하고 하나의 배열에 넣어야 합니다.
        
        옳은 예 (배열로 묶음): 
        [
          {"item": "식당", "place": "로마"},
          {"item": "호텔", "place": "파리"}
        ]
        
        틀린 예 (개별 객체): 
        {"item": "식당", "place": "로마"}
        {"item": "호텔", "place": "파리"}
        
        틀린 예 (속성명에 따옴표 없음):
        [{item: "식당", place: "로마"}]
        """
        
        # 시스템 프롬프트에 JSON 규칙 추가
        enhanced_system_prompt = system_prompt + json_rules
        
        # 대화 내용을 문자열로 변환
        conversation_text = "\n".join([
            f"{msg['speaker']}: {msg['message_content']}"
            for msg in conversation
        ])
        
        # 메시지 구성
        messages = [
            SystemMessage(content=enhanced_system_prompt),
            HumanMessage(content=input_text + "\n\n" + conversation_text)
        ]
        
        # 모델 호출
        response = await fast_llm.ainvoke(messages)
        full_response = response.content
        
        # 개선된 JSON 파싱 사용
        result = parse_json_response(full_response)
        
        # 결과가 있는 경우 콜백 호출
        if result and callback:
            await callback(result)
        
        return result
            
    except Exception as e:
        print(f"2차 검증 중 오류 발생: {str(e)}")
        return []

async def process_final(
    conversation: List[Dict[str, str]], 
    input_prompt: Dict[str, Any],
    callback: Optional[Callable[[List[Dict[str, Any]]], None]] = None
) -> List[Dict[str, Any]]:
    """3차 검증을 위한 함수 - JSON 파싱 및 유효성 검증"""
    try:
        # 프롬프트 구성
        system_prompt = input_prompt.get('system', '')
        input_text = input_prompt.get('input', '')
        
        # JSON 파싱을 위한 특별 지시사항 추가
        json_validation_rules = """
        【JSON 응답 규칙】
        1. 반드시 유효한 JSON 배열 형식으로 응답하세요.
        2. 각 객체는 다음 필드를 포함해야 합니다:
           - payer: 문자열 (발화자)
           - participants: 문자열 배열 (참여자들)
           - constants: 객체 (각 참여자의 고정 금액)
           - ratios: 객체 (각 참여자의 비율)
        3. 모든 속성명은 반드시 큰따옴표(")로 감싸야 합니다.
        4. 여러 항목이 있는 경우 쉼표(,)로 구분하고 하나의 배열에 넣어야 합니다.
        5. JSON 형식이 아닌 다른 텍스트는 포함하지 마세요.
        6. 응답은 반드시 '['로 시작하고 ']'로 끝나야 합니다.
        
        옳은 예 (배열로 묶음): 
        [
          {"payer": "1", "participants": ["0", "1"], "constants": {"0": 0, "1": 0}, "ratios": {"0": 1, "1": 1}},
          {"payer": "2", "participants": ["0", "1", "2"], "constants": {"0": 0, "1": 0, "2": 0}, "ratios": {"0": 1, "1": 1, "2": 1}}
        ]
        
        틀린 예 (개별 객체): 
        {"payer": "1", "participants": ["0", "1"], "constants": {"0": 0, "1": 0}, "ratios": {"0": 1, "1": 1}}
        {"payer": "2", "participants": ["0", "1", "2"], "constants": {"0": 0, "1": 0, "2": 0}, "ratios": {"0": 1, "1": 1, "2": 1}}
        
        틀린 예 (속성명에 따옴표 없음):
        [{payer: "1", participants: ["0", "1"], constants: {"0": 0, "1": 0}, ratios: {"0": 1, "1": 1}}]
        """
        
        # 시스템 프롬프트에 JSON 검증 규칙 추가
        enhanced_system_prompt = system_prompt + json_validation_rules
        
        # 대화 내용을 문자열로 변환
        conversation_text = "\n".join([
            f"{msg['speaker']}: {msg['message_content']}"
            for msg in conversation
        ])
        
        # 메시지 구성
        messages = [
            SystemMessage(content=enhanced_system_prompt),
            HumanMessage(content=input_text + "\n\n" + conversation_text)
        ]
        
        # 모델 호출
        response = await fast_llm.ainvoke(messages)
        full_response = response.content
        
        # 개선된 JSON 파싱 사용
        result = parse_json_response(full_response)
        
        # 각 항목의 필수 필드 검증
        validated_result = []
        for item in result:
            if not isinstance(item, dict):
                continue
                
            if not all(key in item for key in ['payer', 'participants', 'constants', 'ratios']):
                continue
                
            if not isinstance(item['payer'], str) or \
               not isinstance(item['participants'], list) or \
               not isinstance(item['constants'], dict) or \
               not isinstance(item['ratios'], dict):
                continue
            
            validated_result.append(item)
        
        # 결과가 있는 경우 콜백 호출
        if validated_result and callback:
            await callback(validated_result)
        
        return validated_result
            
    except Exception as e:
        print(f"3차 검증 중 오류 발생: {str(e)}")
        return [] 
import json
from typing import List, Dict, Any, Callable, Optional
from langchain.schema import HumanMessage, SystemMessage
from config.service_config import fast_llm  # 모델 설정 import

async def process_conversation(
    conversation: List[Dict[str, str]], 
    input_prompt: Dict[str, Any],
    callback: Optional[Callable[[List[Dict[str, Any]]], None]] = None
) -> List[Dict[str, Any]]:
    """대화 내용을 처리하는 함수 - GPT-3.5 모델 사용"""
    try:
        # 프롬프트 구성
        system_prompt = input_prompt.get('system', '')
        input_text = input_prompt.get('input', '')
        
        # 특별 규칙 추가 (외화 금액과 멤버 수에 관한 규칙)
        special_rules = """
            【매우 중요한 규칙】
            1. 모든 외화 금액(EUR, USD 등)은 절대 곱하지 않고 원래 숫자 그대로 사용하세요.
            "19유로", "19EUR", "택시비 19유로" → amount: 19 (절대 19000으로 변환하지 마세요)
            "23유로", "23EUR", "택시비 23유로" → amount: 23 (절대 23000으로 변환하지 마세요)
            "27유로", "27EUR", "택시비 27유로" → amount: 27 (절대 27000으로 변환하지 마세요)

            2. 대화 시작 부분에 멤버 수(member_count)가 제공됩니다. 이 수와 동일한 인원으로 나눠야 한다는 표현이 있으면 반드시 "n분의1"로 처리하세요.
            예: member_count가 5일 때 "5명이서 나눠야 함" → hint_type: "n분의1"
            """
        
        # 시스템 프롬프트에 특별 규칙 추가
        enhanced_system_prompt = system_prompt + special_rules
        
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
        
        # GPT-3.5 모델 호출
        response = await fast_llm.ainvoke(messages)
        full_response = response.content
        
        # JSON 파싱
        try:
            result = None
            if full_response.strip().startswith('[') and full_response.strip().endswith(']'):
                result = json.loads(full_response)
            else:
                start_idx = full_response.find('[')
                end_idx = full_response.rfind(']') + 1
                if start_idx != -1 and end_idx != -1:
                    json_str = full_response[start_idx:end_idx]
                    result = json.loads(json_str)
                else:
                    return []
            
            # 결과가 있는 경우 콜백 호출
            if result and callback:
                await callback(result)
            
            return result if result else []
                
        except json.JSONDecodeError as e:
            print(f"경고: 응답이 유효한 JSON 형식이 아닙니다 - {str(e)}")
            return []
            
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
        
        # 대화 내용을 문자열로 변환
        conversation_text = "\n".join([
            f"{msg['speaker']}: {msg['message_content']}"
            for msg in conversation
        ])
        
        # 메시지 구성 (특별 규칙 없이 순수 프롬프트만 사용)
        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=input_text + "\n\n" + conversation_text)
        ]
        
        # 모델 호출
        response = await fast_llm.ainvoke(messages)
        full_response = response.content
        
        # JSON 파싱
        try:
            result = None
            if full_response.strip().startswith('[') and full_response.strip().endswith(']'):
                result = json.loads(full_response)
            else:
                start_idx = full_response.find('[')
                end_idx = full_response.rfind(']') + 1
                if start_idx != -1 and end_idx != -1:
                    json_str = full_response[start_idx:end_idx]
                    result = json.loads(json_str)
                else:
                    return []
            
            # 결과가 있는 경우 콜백 호출
            if result and callback:
                await callback(result)
            
            return result if result else []
                
        except json.JSONDecodeError as e:
            print(f"경고: 응답이 유효한 JSON 형식이 아닙니다 - {str(e)}")
            return []
            
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
        3. JSON 형식이 아닌 다른 텍스트는 포함하지 마세요.
        4. 응답은 반드시 '['로 시작하고 ']'로 끝나야 합니다.
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
        
        # JSON 파싱 및 검증
        try:
            # 응답에서 JSON 부분만 추출
            json_str = full_response.strip()
            if not (json_str.startswith('[') and json_str.endswith(']')):
                start_idx = json_str.find('[')
                end_idx = json_str.rfind(']') + 1
                if start_idx != -1 and end_idx != -1:
                    json_str = json_str[start_idx:end_idx]
                else:
                    print("경고: JSON 배열을 찾을 수 없습니다.")
                    return []
            
            # JSON 파싱
            result = json.loads(json_str)
            
            # 결과 유효성 검증
            if not isinstance(result, list):
                print("경고: 결과가 배열이 아닙니다.")
                return []
            
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
                
        except json.JSONDecodeError as e:
            print(f"경고: JSON 파싱 실패 - {str(e)}")
            return []
            
    except Exception as e:
        print(f"3차 검증 중 오류 발생: {str(e)}")
        return [] 
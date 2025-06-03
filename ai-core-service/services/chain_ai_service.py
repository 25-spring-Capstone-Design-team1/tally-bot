import json
import asyncio
import re
from typing import List, Dict, Any, Optional, Callable
from langchain.prompts import PromptTemplate
from langchain.schema import HumanMessage, SystemMessage
from config.service_config import fast_llm
from services.ai_service import parse_json_response
from utils.json_filter import filter_invalid_amounts

class ChainAIService:
    """효율적인 체인 기반 AI 서비스"""
    
    def __init__(self):
        # 인스턴스 변수로 LLM을 저장하지 않음 (상태 격리를 위해)
        pass
        
    def _get_fresh_llm(self):
        """완전히 새로운 LLM 인스턴스 생성 (상태 격리)"""
        import importlib
        import sys
        import time
        
        # 모듈 캐시에서 제거하여 완전히 새로 로드
        if 'config.service_config' in sys.modules:
            del sys.modules['config.service_config']
        
        # 시간 기반 고유 식별자로 완전한 격리 보장
        unique_id = int(time.time() * 1000000)  # 마이크로초 단위
        
        # 새로운 모듈 인스턴스 로드
        config_module = importlib.import_module('config.service_config')
        
        # 고유 식별자를 통한 추가 격리
        print(f"🔧 새 LLM 인스턴스 생성: {unique_id}")
        
        return config_module.fast_llm
        
    def _escape_braces_in_prompt(self, prompt_text: str) -> str:
        """프롬프트 텍스트에서 JSON 예시의 중괄호를 이스케이프 처리"""
        # 모든 중괄호를 이스케이프하되, LangChain 변수는 보존
        # 1. 먼저 LangChain 변수를 임시로 보호
        langchain_vars = []
        temp_text = prompt_text
        
        # LangChain 변수 패턴 찾기 (단일 중괄호로 둘러싸인 변수명)
        var_pattern = r'\{([a-zA-Z_][a-zA-Z0-9_]*)\}'
        
        def replace_var(match):
            var_name = match.group(1)
            placeholder = f"__LANGCHAIN_VAR_{len(langchain_vars)}__"
            langchain_vars.append(var_name)
            return placeholder
        
        # LangChain 변수를 플레이스홀더로 교체
        temp_text = re.sub(var_pattern, replace_var, temp_text)
        
        # 2. 모든 남은 중괄호를 이스케이프
        temp_text = temp_text.replace('{', '{{').replace('}', '}}')
        
        # 3. LangChain 변수를 다시 복원
        for i, var_name in enumerate(langchain_vars):
            placeholder = f"__LANGCHAIN_VAR_{i}__"
            temp_text = temp_text.replace(placeholder, f'{{{var_name}}}')
        
        return temp_text
        
    def _create_input_chain(self, input_prompt: Dict[str, Any]):
        """1차 프롬프트 체인 생성 (완전한 상태 격리)"""
        system_prompt = input_prompt.get('system', '')
        input_text = input_prompt.get('input', '')
        
        # 강화된 상태 격리 규칙
        isolation_rules = """
            【상태 격리 규칙 - 매우 중요】
            1. 이 요청은 완전히 새로운 독립적인 처리입니다.
            2. 이전에 처리한 어떤 대화나 데이터와도 무관합니다.
            3. 오직 현재 제공된 대화 내용만을 분석하세요.
            4. 다른 파일이나 이전 요청의 정보를 절대 참조하지 마세요.
            5. 현재 대화에 없는 정보는 절대 추가하지 마세요.
            
            【대화 내용 검증 규칙】
            1. 반드시 제공된 대화 텍스트에 명시적으로 언급된 항목만 추출하세요.
            2. 대화에 없는 장소, 금액, 항목은 절대 포함하지 마세요.
            3. 추론이나 가정을 통해 항목을 추가하지 마세요.
            4. 각 항목은 대화에서 직접 확인할 수 있어야 합니다.
            
            【처리 규칙】
            1. 모든 외화 금액(EUR, USD 등)은 절대 곱하지 않고 원래 숫자 그대로 사용하세요.
            2. 대화 시작 부분에 멤버 수(member_count)가 제공됩니다. 이 수와 동일한 인원으로 나눠야 한다는 표현이 있으면 반드시 "n분의1"로 처리하세요.
            3. 정산 항목이 있는 경우에만 JSON 배열로 응답하세요.
            4. 모든 결과는 반드시 대괄호([])로 묶어 배열 형태로 반환해야 합니다.
        """
        
        enhanced_system_prompt = system_prompt + isolation_rules
        
        # 중괄호 이스케이프 처리
        escaped_system_prompt = self._escape_braces_in_prompt(enhanced_system_prompt)
        escaped_input_text = self._escape_braces_in_prompt(input_text)
        
        # 매번 완전히 새로운 PromptTemplate과 LLM 인스턴스 생성
        prompt_template = PromptTemplate(
            input_variables=["conversation"],
            template=f"{escaped_system_prompt}\n\n{escaped_input_text}\n\n{{conversation}}"
        )
        
        # 완전히 새로운 LLM 인스턴스 생성
        fresh_llm = self._get_fresh_llm()
        
        return prompt_template | fresh_llm
    
    def _create_secondary_chain(self, secondary_prompt: Dict[str, Any]):
        """2차 프롬프트 체인 생성 (완전한 상태 격리)"""
        system_prompt = secondary_prompt.get('system', '')
        input_text = secondary_prompt.get('input', '')
        
        # 강화된 상태 격리 및 JSON 형식 규칙
        isolation_and_json_rules = """
        【상태 격리 규칙 - 매우 중요】
        1. 이 요청은 완전히 새로운 독립적인 처리입니다.
        2. 이전에 처리한 어떤 데이터와도 무관합니다.
        3. 오직 현재 제공된 items_data만을 분석하세요.
        4. 다른 파일이나 이전 요청의 정보를 절대 참조하지 마세요.
        
        【JSON 응답 규칙】
        - 모든 결과는 반드시 대괄호([])로 묶어 배열 형태로 반환해야 합니다.
        - 모든 속성명은 큰따옴표(")로 감싸야 합니다.
        - 여러 항목이 있는 경우 쉼표(,)로 구분하고 하나의 배열에 넣어야 합니다.
        """
        
        enhanced_system_prompt = system_prompt + isolation_and_json_rules
        
        # 중괄호 이스케이프 처리
        escaped_system_prompt = self._escape_braces_in_prompt(enhanced_system_prompt)
        escaped_input_text = self._escape_braces_in_prompt(input_text)
        
        # 매번 완전히 새로운 PromptTemplate과 LLM 인스턴스 생성
        prompt_template = PromptTemplate(
            input_variables=["items_data"],
            template=f"{escaped_system_prompt}\n\n{escaped_input_text}\n\n{{items_data}}"
        )
        
        # 완전히 새로운 LLM 인스턴스 생성
        fresh_llm = self._get_fresh_llm()
        
        return prompt_template | fresh_llm
    
    def _create_final_chain(self, final_prompt: Dict[str, Any]):
        """3차 프롬프트 체인 생성 (완전한 상태 격리)"""
        system_prompt = final_prompt.get('system', '')
        input_text = final_prompt.get('input', '')
        
        # 강화된 상태 격리 및 JSON 검증 규칙
        isolation_and_validation_rules = """
        【상태 격리 규칙 - 매우 중요】
        1. 이 요청은 완전히 새로운 독립적인 처리입니다.
        2. 이전에 처리한 어떤 데이터와도 무관합니다.
        3. 오직 현재 제공된 final_data와 member_info만을 분석하세요.
        4. 다른 파일이나 이전 요청의 정보를 절대 참조하지 마세요.
        
        【JSON 응답 규칙】
        1. 반드시 유효한 JSON 배열 형식으로 응답하세요.
        2. 각 객체는 다음 필드를 포함해야 합니다:
           - payer: 문자열 (발화자)
           - participants: 문자열 배열 (참여자들)
           - constants: 객체 (각 참여자의 고정 금액)
           - ratios: 객체 (각 참여자의 비율)
        3. 모든 속성명은 반드시 큰따옴표(")로 감싸야 합니다.
        4. 응답은 반드시 '['로 시작하고 ']'로 끝나야 합니다.
        """
        
        enhanced_system_prompt = system_prompt + isolation_and_validation_rules
        
        # 중괄호 이스케이프 처리
        escaped_system_prompt = self._escape_braces_in_prompt(enhanced_system_prompt)
        escaped_input_text = self._escape_braces_in_prompt(input_text)
        
        # 매번 완전히 새로운 PromptTemplate과 LLM 인스턴스 생성
        prompt_template = PromptTemplate(
            input_variables=["final_data", "member_info"],
            template=f"{escaped_system_prompt}\n\n{escaped_input_text}\n\nMember Info: {{member_info}}\n\nFinal Data: {{final_data}}"
        )
        
        # 완전히 새로운 LLM 인스턴스 생성
        fresh_llm = self._get_fresh_llm()
        
        return prompt_template | fresh_llm
    
    async def process_with_sequential_chain(
        self,
        conversation: List[Dict[str, str]],
        input_prompt: Dict[str, Any],
        secondary_prompt: Dict[str, Any],
        final_prompt: Dict[str, Any],
        member_names: List[str],
        id_to_name: Dict[str, str],
        name_to_id: Dict[str, str]
    ) -> Dict[str, Any]:
        """체인을 순차적으로 실행하여 전체 프로세스를 효율적으로 처리"""
        
        try:
            # 1. 입력 데이터 준비 및 로깅
            conversation_text = "\n".join([
                f"{msg['speaker']}: {msg['message_content']}"
                for msg in conversation
            ])
            
            # 입력 데이터 상세 로깅
            print(f"🔍 입력 데이터 분석:")
            print(f"   📝 대화 메시지 수: {len(conversation)}개")
            print(f"   👥 멤버 정보: {id_to_name}")
            print(f"   📄 첫 번째 메시지: {conversation[0] if conversation else 'None'}")
            if len(conversation) > 1:
                print(f"   📄 두 번째 메시지: {conversation[1]['speaker']}: {conversation[1]['message_content'][:50]}...")
            
            member_info = f"member_count: {len(id_to_name)}\nmember_mapping: {json.dumps(id_to_name, ensure_ascii=False)}"
            
            # 2. 1차 체인 실행
            print(f"🔄 1차 체인 실행 시작")
            input_chain = self._create_input_chain(input_prompt)
            input_result_raw = await input_chain.ainvoke({"conversation": conversation_text})
            
            # AIMessage에서 content 추출
            if hasattr(input_result_raw, 'content'):
                input_result_content = input_result_raw.content
            else:
                input_result_content = str(input_result_raw)
            
            print(f"   📊 1차 체인 원시 응답 길이: {len(input_result_content)}자")
            print(f"   📊 1차 체인 원시 응답 시작: {input_result_content[:100]}...")
            
            input_result = parse_json_response(input_result_content)
            print(f"   ✅ 1차 체인 파싱 결과: {len(input_result)}개 항목")
            
            if not input_result:
                print("   ⚠️ 1차 체인 결과가 비어있음")
                return {
                    "final_result": []
                }
            
            # 3. 통화 변환 및 필터링
            from services.result_processor import preprocess_conversation_results, extract_items_only, extract_complex_items
            converted_result = await preprocess_conversation_results(input_result)
            converted_result = filter_invalid_amounts(converted_result)
            
            print(f"   📊 통화 변환 후: {len(converted_result)}개 항목")
            
            # 4. 2차 체인 실행 (place 추출)
            items_only = extract_items_only(converted_result)
            if items_only:
                print(f"🔄 2차 체인 실행 시작 ({len(items_only)}개 항목)")
                secondary_chain = self._create_secondary_chain(secondary_prompt)
                secondary_result_raw = await secondary_chain.ainvoke({
                    "items_data": json.dumps(items_only, ensure_ascii=False)
                })
                
                # AIMessage에서 content 추출
                if hasattr(secondary_result_raw, 'content'):
                    secondary_result_content = secondary_result_raw.content
                else:
                    secondary_result_content = str(secondary_result_raw)
                
                secondary_result = parse_json_response(secondary_result_content)
                print(f"   ✅ 2차 체인 결과: {len(secondary_result)}개 항목")
            else:
                secondary_result = []
                print("   ⚠️ 2차 체인 입력 데이터 없음")
            
            # 5. 3차 체인 실행 (복잡한 항목 처리)
            complex_items = extract_complex_items(converted_result)
            final_result = []
            
            print(f"🔄 복잡한 항목 분석: {len(complex_items)}개")
            
            if complex_items:
                from services.result_processor import map_place_to_complex_items, process_complex_results, process_all_results
                
                # 복잡한 항목에 place 정보 매핑
                mapped_complex_items = map_place_to_complex_items(complex_items, secondary_result, converted_result)
                
                # 3차 프롬프팅을 위한 입력 데이터 준비
                final_input_data = []
                for item in mapped_complex_items:
                    final_input = {
                        "speaker": item["speaker"],
                        "amount": item["amount"],
                        "hint_type": item["hint_type"],
                        "hint_phrases": item.get("hint_phrases", [])
                    }
                    final_input_data.append(final_input)
                
                if final_input_data:
                    print(f"🔄 3차 체인 실행 시작 ({len(final_input_data)}개 항목)")
                    final_chain = self._create_final_chain(final_prompt)
                    final_result_raw = await final_chain.ainvoke({
                        "final_data": json.dumps(final_input_data, ensure_ascii=False),
                        "member_info": member_info
                    })
                    
                    # AIMessage에서 content 추출
                    if hasattr(final_result_raw, 'content'):
                        final_result_content = final_result_raw.content
                    else:
                        final_result_content = str(final_result_raw)
                    
                    complex_results = parse_json_response(final_result_content)
                    
                    if complex_results:
                        # 복잡한 결과 후처리
                        processed_complex_results = process_complex_results(complex_results, mapped_complex_items, name_to_id)
                        # 모든 결과 처리 및 합치기
                        final_result = process_all_results(converted_result, secondary_result, processed_complex_results, member_names, id_to_name, name_to_id)
                    else:
                        # 복잡한 결과가 없는 경우 표준 결과만 처리
                        final_result = process_all_results(converted_result, secondary_result, None, member_names, id_to_name, name_to_id)
                else:
                    final_result = process_all_results(converted_result, secondary_result, None, member_names, id_to_name, name_to_id)
            else:
                # 복잡한 항목이 없는 경우 표준 결과만 처리
                from services.result_processor import process_all_results
                final_result = process_all_results(converted_result, secondary_result, None, member_names, id_to_name, name_to_id)
            
            # 디버깅: 최종 결과 확인
            print(f"=== 최종 결과 ===")
            print(f"1차 결과 (converted_result): {len(converted_result)}개 항목")
            print(f"2차 결과 (secondary_result): {len(secondary_result)}개 항목")
            print(f"최종 결과 (final_result): {len(final_result if final_result else [])}개 항목")
            print("최종 정산 결과:")
            for i, item in enumerate(final_result if final_result else []):
                print(f"  [{i+1}] {json.dumps(item, ensure_ascii=False, indent=2)}")
            print("=" * 50)
            
            return {
                "final_result": final_result if final_result else []
            }
                
        except Exception as e:
            print(f"체인 처리 중 오류 발생: {str(e)}")
            import traceback
            traceback.print_exc()
            return {
                "final_result": []
            }
    
    async def process_chunked_with_sequential_chain(
        self,
        conversation: List[Dict[str, str]],
        input_prompt: Dict[str, Any],
        secondary_prompt: Dict[str, Any],
        final_prompt: Dict[str, Any],
        member_names: List[str],
        id_to_name: Dict[str, str],
        name_to_id: Dict[str, str],
        chunk_size: int = 10
    ) -> Dict[str, Any]:
        """청크 단위로 체인 처리 (완전 격리 버전)"""
        
        # conversation이 딕셔너리인지 리스트인지 확인
        if isinstance(conversation, dict):
            messages = conversation.get("messages", [])
            print(f"🔍 딕셔너리 형태 입력 데이터:")
            print(f"   📝 chatroom_name: {conversation.get('chatroom_name', 'N/A')}")
            print(f"   👥 members: {conversation.get('members', [])}")
        else:
            messages = conversation
            print(f"🔍 리스트 형태 입력 데이터:")
        
        print(f"   📝 전체 메시지 수: {len(messages)}개")
        
        # 시스템 메시지와 사용자 메시지 분리
        system_messages = []
        user_messages = []
        
        for msg in messages:
            if isinstance(msg, dict) and msg.get('speaker') == 'system':
                system_messages.append(msg)
                print(f"   🔧 시스템 메시지: {msg['message_content'][:100]}...")
            else:
                user_messages.append(msg)
        
        print(f"   📊 시스템 메시지: {len(system_messages)}개, 사용자 메시지: {len(user_messages)}개")
        
        # 첫 번째 시스템 메시지 보존
        preserved_system_message = system_messages[0] if system_messages else None
        
        # 사용자 메시지를 청크로 분할
        chunks = [user_messages[i:i + chunk_size] for i in range(0, len(user_messages), chunk_size)]
        
        print(f"🔄 청크 처리 시작: {len(chunks)}개 청크, 각 청크당 최대 {chunk_size}개 메시지")
        
        # 전체 대화 내용 해시 생성 (파일 식별용)
        import hashlib
        conversation_text = "\n".join([msg.get('message_content', '') for msg in user_messages])
        conversation_hash = hashlib.md5(conversation_text.encode()).hexdigest()[:8]
        print(f"🔐 대화 고유 식별자: {conversation_hash}")
        
        # 각 청크를 순차적으로 처리
        combined_input_results = []
        combined_secondary_results = []
        combined_final_results = []
        
        for i, chunk in enumerate(chunks):
            print(f"📦 청크 {i+1}/{len(chunks)} 처리 중... ({len(chunk)}개 메시지)")
            
            # 청크 내용 미리보기
            if chunk:
                print(f"   📄 청크 첫 메시지: {chunk[0]['speaker']}: {chunk[0]['message_content'][:50]}...")
                if len(chunk) > 1:
                    print(f"   📄 청크 마지막 메시지: {chunk[-1]['speaker']}: {chunk[-1]['message_content'][:50]}...")
            
            # 청크별 고유 식별자 생성
            chunk_text = "\n".join([msg.get('message_content', '') for msg in chunk])
            chunk_hash = hashlib.md5(chunk_text.encode()).hexdigest()[:8]
            
            # 각 청크마다 완전히 새로운 대화 구성 (모든 청크에 시스템 메시지 포함)
            chunk_conversation = []
            if preserved_system_message:
                # 모든 청크에 시스템 메시지 포함하여 완전한 컨텍스트 제공
                chunk_conversation.append(preserved_system_message)
                print(f"   🔧 시스템 메시지 포함 (청크 {i+1})")
            
            chunk_conversation.extend(chunk)
            print(f"   📝 청크 대화 구성: {len(chunk_conversation)}개 메시지")
            print(f"   🔐 청크 고유 식별자: {chunk_hash}")
            
            # 청크별 격리 강화를 위한 추가 지시사항
            enhanced_input_prompt = input_prompt.copy()
            enhanced_input_prompt['system'] = input_prompt.get('system', '') + f"""
            
            【청크 처리 격리 규칙 - 청크 {i+1}/{len(chunks)}】
            1. 이 청크는 전체 대화의 일부입니다 (청크 {i+1}/{len(chunks)}).
            2. 오직 현재 청크에 포함된 메시지만을 분석하세요.
            3. 다른 청크나 이전 처리 결과를 절대 참조하지 마세요.
            4. 현재 청크에 명시적으로 언급되지 않은 정산 항목은 포함하지 마세요.
            5. 청크 번호: {i+1}/{len(chunks)}
            6. 대화 식별자: {conversation_hash}
            7. 청크 식별자: {chunk_hash}
            
            【엄격한 내용 검증】
            - 현재 청크의 메시지에서 직접 언급된 항목만 추출하세요.
            - 이전 청크에서 언급된 항목을 반복하지 마세요.
            - 추론이나 가정으로 항목을 추가하지 마세요.
            """
            
            try:
                # 각 청크를 완전히 독립적으로 처리
                chunk_result = await self.process_with_sequential_chain(
                    chunk_conversation,
                    enhanced_input_prompt,  # 강화된 프롬프트 사용
                    secondary_prompt,
                    final_prompt,
                    member_names,
                    id_to_name,
                    name_to_id
                )
                
                # 결과 누적 (개선된 중복 제거)
                if isinstance(chunk_result, dict):
                    new_input_results = chunk_result.get("result", [])
                    new_secondary_results = chunk_result.get("secondary_result", [])
                    new_final_results = chunk_result.get("final_result", [])
                    
                    # 중복 제거 전 항목 수 로깅
                    print(f"   📊 청크 {i+1} 원시 결과: 1차={len(new_input_results)}개, 2차={len(new_secondary_results)}개, 최종={len(new_final_results)}개")
                    
                    # 개선된 중복 제거 로직 적용
                    deduplicated_input = self._deduplicate_results_strict(new_input_results, combined_input_results)
                    deduplicated_secondary = self._deduplicate_results_strict(new_secondary_results, combined_secondary_results)
                    deduplicated_final = self._deduplicate_results_strict(new_final_results, combined_final_results)
                    
                    combined_input_results.extend(deduplicated_input)
                    combined_secondary_results.extend(deduplicated_secondary)
                    combined_final_results.extend(deduplicated_final)
                    
                    # 중복 제거 후 항목 수 로깅
                    print(f"   ✅ 청크 {i+1} 중복제거 후: 1차={len(deduplicated_input)}개, 2차={len(deduplicated_secondary)}개, 최종={len(deduplicated_final)}개 추가")
                    print(f"   📈 누적 결과: 1차={len(combined_input_results)}개, 2차={len(combined_secondary_results)}개, 최종={len(combined_final_results)}개")
                
            except Exception as e:
                print(f"❌ 청크 {i+1} 처리 중 오류: {str(e)}")
                continue
        
        print(f"🎉 모든 청크 처리 완료: 총 {len(combined_final_results)}개 최종 결과")
        
        return {
            "final_result": combined_final_results
        }
    
    def _deduplicate_results_strict(self, new_results: List[Dict], existing_results: List[Dict]) -> List[Dict]:
        """엄격한 중복 제거 (빈 값 처리 개선)"""
        if not new_results:
            return []
        
        # 기존 결과의 키 조합으로 중복 체크
        existing_keys = set()
        for result in existing_results:
            if isinstance(result, dict):
                # item과 amount를 기본 키로 사용 (더 엄격한 중복 체크)
                item = result.get('item', '').strip()
                amount = str(result.get('amount', '')).strip()
                
                if item and amount:  # item과 amount가 모두 있는 경우만 중복 체크
                    key = (item, amount)
                    existing_keys.add(key)
        
        # 새 결과에서 중복되지 않는 항목만 반환
        deduplicated = []
        for result in new_results:
            if isinstance(result, dict):
                item = result.get('item', '').strip()
                amount = str(result.get('amount', '')).strip()
                
                # item이나 amount가 비어있는 경우 스킵 (잘못된 데이터)
                if not item or not amount:
                    print(f"🔄 빈 값으로 인한 스킵: item='{item}', amount='{amount}'")
                    continue
                
                key = (item, amount)
                if key not in existing_keys:
                    deduplicated.append(result)
                    existing_keys.add(key)
                    print(f"✅ 새 항목 추가: {item} ({amount})")
                else:
                    # 실제 중복 제거된 항목 로깅
                    print(f"🔄 중복 제거: {item} ({amount})")
        
        return deduplicated 
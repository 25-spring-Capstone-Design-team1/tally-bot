import json
from typing import List, Dict, Any, Callable, Optional
from langchain.schema import HumanMessage, SystemMessage
from services.config import fast_llm  # 모델 설정 import

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
        
        # 대화 내용을 문자열로 변환
        conversation_text = "\n".join([
            f"{msg['speaker']}: {msg['message_content']}"
            for msg in conversation
        ])
        
        print(f"전체 대화 처리 시작...")
        
        # 메시지 구성
        messages = [
            SystemMessage(content=system_prompt),
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
                    print(f"경고: 응답에서 JSON 배열을 찾을 수 없습니다")
                    return []
            
            # 결과가 있는 경우 출력 및 콜백 호출
            if result:
                print(f"총 {len(result)}개의 항목이 추출되었습니다.")
                
                # 콜백 함수가 제공된 경우 결과 전달
                if callback:
                    await callback(result)
                else:
                    # 콜백이 없으면 내부에서 결과 출력
                    print(f"=== 처리 결과 ===")
                    print(json.dumps(result, ensure_ascii=False, indent=2))
                    print("=" * 50)
                
                return result
            else:
                print("추출된 항목이 없습니다.")
                return []
                
        except json.JSONDecodeError as e:
            print(f"경고: 응답이 유효한 JSON 형식이 아닙니다 - {str(e)}")
            print("원본 응답:", full_response)
            return []
            
    except Exception as e:
        print(f"대화 처리 중 오류 발생: {str(e)}")
        return [] 
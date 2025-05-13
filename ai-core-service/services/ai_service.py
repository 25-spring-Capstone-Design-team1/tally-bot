import os
from langchain_openai import ChatOpenAI
from langchain.schema import HumanMessage, SystemMessage
import json
from typing import List, Dict, Any
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

# 필수 환경 변수 검증
required_env_vars = {
    "OPENAI_API_KEY": os.getenv("OPENAI_API_KEY"),
    "LANGSMITH_API_KEY": os.getenv("LANGSMITH_API_KEY")
}

missing_vars = [var for var, value in required_env_vars.items() if not value]
if missing_vars:
    raise EnvironmentError(
        f"필수 환경 변수가 누락되었습니다: {', '.join(missing_vars)}\n"
        ".env 파일을 생성하거나 환경 변수를 설정해주세요."
    )

# 환경 변수 설정
os.environ["LANGSMITH_TRACING"] = "true"
os.environ["LANGSMITH_ENDPOINT"] = "https://api.smith.langchain.com"
os.environ["LANGSMITH_API_KEY"] = required_env_vars["LANGSMITH_API_KEY"]
os.environ["LANGSMITH_PROJECT"] = "tally-temporary"
os.environ["OPENAI_API_KEY"] = required_env_vars["OPENAI_API_KEY"]

# LangChain 모델 설정 - GPT-4 사용
llm = ChatOpenAI(
    model_name="gpt-4",
    temperature=0.0,
    metadata={
        "ls_provider": "openai",
        "ls_model_name": "gpt-4"
    }
)

async def process_conversation(conversation: List[Dict[str, str]], input_prompt: Dict[str, Any]) -> Dict[str, Any]:
    """대화 내용을 처리하는 함수"""
    try:
        # 대화 내용을 문자열로 변환
        conversation_text = "\n".join([
            f"{msg['speaker']}: {msg['message_content']}"
            for msg in conversation
        ])
        
        # 프롬프트 구성 - 절대 환율 추론을 하지 않도록 강력하게 지시
        system_prompt = input_prompt.get('system', '')
        system_prompt += """
        ⚠️⚠️ 가장 중요한 추가 지시사항 ⚠️⚠️
        1. 절대, 어떤 상황에서도 환율 계산을 하지 마세요.
        2. "300유로"는 반드시 "amount": 300, "currency": "EUR"로 추출해야 합니다.
        3. "300유로(43만원)" 같은 경우에도 반드시 "amount": 300, "currency": "EUR"로만 추출하세요.
        4. 금액이 원화가 아닌 외화로 명시된 경우 원래 금액과 통화 그대로 유지하세요.
        5. 원화만 "amount": 430000 처럼 표시하고 currency 필드는 생략하세요.
        6. 환율에 관한 모든 정보는 무시하고, 언급된 그대로의 금액만 사용하세요.
        """
        
        input_text = input_prompt.get('input', '')
        input_text += """
        ⚠️ 다시 강조합니다: 절대 환율 변환 계산을 하지 마세요. 오직 대화에서 언급된 그대로의 금액을 사용하세요. ⚠️
        """
        
        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=input_text + "\n\n" + conversation_text)
        ]
        
        # GPT 호출
        response = await llm.ainvoke(messages)
        full_response = response.content
        
        # JSON 파싱
        try:
            # 순수 JSON 배열만 추출 시도
            if full_response.strip().startswith('[') and full_response.strip().endswith(']'):
                result = json.loads(full_response)
                return result
            else:
                # JSON 부분만 추출 시도
                start_idx = full_response.find('[')
                end_idx = full_response.rfind(']') + 1
                if start_idx != -1 and end_idx != -1:
                    json_str = full_response[start_idx:end_idx]
                    result = json.loads(json_str)
                    return result
                else:
                    print("오류: 응답에서 JSON 배열을 찾을 수 없습니다")
                    print("원본 응답:", full_response)
                    return None
                    
        except json.JSONDecodeError as e:
            print(f"오류: 응답이 유효한 JSON 형식이 아닙니다 - {str(e)}")
            print("원본 응답:", full_response)
            return None
    except Exception as e:
        print(f"대화 처리 중 오류 발생: {str(e)}")
        return None 
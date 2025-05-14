import os
from langchain_openai import ChatOpenAI
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

def initialize_environment():
    """환경 변수 검증 및 설정"""
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
    
    return required_env_vars

# 환경 변수 초기화
env_vars = initialize_environment()

# 빠른 모델(GPT-3.5) - 단순 추출 작업용
fast_llm = ChatOpenAI(
    model_name="gpt-3.5-turbo",
    temperature=0.0,
    metadata={
        "ls_provider": "openai",
        "ls_model_name": "gpt-3.5-turbo"
    }
)

# 정확한 모델(GPT-4) - 복잡한 추론 작업용
accurate_llm = ChatOpenAI(
    model_name="gpt-4",
    temperature=0.0,
    metadata={
        "ls_provider": "openai",
        "ls_model_name": "gpt-4"
    }
)

# 기본 모델은 빠른 처리를 위해 GPT-3.5 사용
llm = fast_llm 
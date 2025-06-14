import os
import boto3
import json
from langchain_openai import ChatOpenAI

def get_secret(secret_name, region_name="ap-northeast-2"):
    """AWS Secrets Manager에서 시크릿 값을 가져옵니다."""
    secret_name = "prod/AppBeta/apikey"
    region_name = "ap-northeast-2"

    session = boto3.session.Session()
    client = session.client(
        service_name='secretsmanager',
        region_name=region_name
    )
    
    try:
        get_secret_value_response = client.get_secret_value(
            SecretId=secret_name
        )
    except Exception as e:
        raise e
    
    secret = get_secret_value_response['SecretString']
    return json.loads(secret)

def initialize_environment():
    """환경 변수 검증 및 설정"""
    try:
        # AWS Secrets Manager에서 시크릿 가져오기
        secrets = get_secret("prod/AppBeta/apikey")
        
        # 필수 환경 변수 검증
        required_env_vars = {
            "OPENAI_API_KEY": secrets.get("OpenAI"),
            "LANGSMITH_API_KEY": secrets.get("Langsmith"),
            "DEEP_EVAL_API_KEY": secrets.get("DeepEval")
        }

        missing_vars = [var for var, value in required_env_vars.items() if not value]
        if missing_vars:
            raise EnvironmentError(
                f"필수 환경 변수가 누락되었습니다: {', '.join(missing_vars)}\n"
                "AWS Secrets Manager에 필요한 시크릿이 설정되어 있는지 확인해주세요."
            )

        # 환경 변수 설정
        os.environ["LANGSMITH_TRACING"] = "true"
        os.environ["LANGSMITH_ENDPOINT"] = "https://api.smith.langchain.com"
        os.environ["LANGSMITH_API_KEY"] = required_env_vars["LANGSMITH_API_KEY"]
        os.environ["LANGSMITH_PROJECT"] = "tally-temporary"
        os.environ["OPENAI_API_KEY"] = required_env_vars["OPENAI_API_KEY"]
        
        # DeepEval 환경 변수 설정 (Confident AI)
        if required_env_vars["DEEP_EVAL_API_KEY"]:
            os.environ["CONFIDENT_API_KEY"] = required_env_vars["DEEP_EVAL_API_KEY"]
        
        return required_env_vars
        
    except Exception as e:
        raise EnvironmentError(f"AWS Secrets Manager에서 시크릿을 가져오는 중 오류가 발생했습니다: {str(e)}")

def get_api_keys():
    """설정된 API 키들을 반환합니다"""
    return {
        "openai": os.getenv("OPENAI_API_KEY"),
        "langsmith": os.getenv("LANGSMITH_API_KEY"), 
        "deepeval": os.getenv("CONFIDENT_API_KEY"),
        "deep_eval": os.getenv("DEEP_EVAL_API_KEY")  # 호환성을 위해 유지
    }

def ensure_api_key(service_name: str) -> str:
    """특정 서비스의 API 키가 설정되어 있는지 확인하고 반환합니다"""
    api_keys = get_api_keys()
    
    if service_name.lower() == "openai":
        key = api_keys["openai"]
        if not key:
            raise ValueError("OpenAI API 키가 설정되지 않았습니다.")
        return key
    elif service_name.lower() in ["deepeval", "confident"]:
        key = api_keys["deepeval"]
        if not key:
            raise ValueError("DeepEval (Confident AI) API 키가 설정되지 않았습니다.")
        return key
    elif service_name.lower() == "langsmith":
        key = api_keys["langsmith"]
        if not key:
            raise ValueError("LangSmith API 키가 설정되지 않았습니다.")
        return key
    else:
        raise ValueError(f"알 수 없는 서비스: {service_name}")

# 환경 변수 초기화
try:
    env_vars = initialize_environment()
except Exception as e:
    env_vars = {}

# 빠른 모델(GPT-3.5) - 단순 추출 작업용
fast_llm = ChatOpenAI(
    model="gpt-3.5-turbo",
    temperature=0.0
)

# 실험용 모델(GPT-4o-mini) - 새로운 실험용
experimental_llm = ChatOpenAI(
    model="gpt-4o-mini",
    temperature=0.0
)

# 정확한 모델(GPT-4) - 복잡한 추론 작업용
accurate_llm = ChatOpenAI(
    model="gpt-4",
    temperature=0.0
)

# 기본 모델은 실험을 위해 GPT-4o-mini 사용
llm = experimental_llm 
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
            "LANGSMITH_API_KEY": secrets.get("Langsmith")
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
        
        return required_env_vars
    except Exception as e:
        raise EnvironmentError(f"AWS Secrets Manager에서 시크릿을 가져오는 중 오류가 발생했습니다: {str(e)}")

# 환경 변수 초기화
env_vars = initialize_environment()

# 빠른 모델(GPT-3.5) - 단순 추출 작업용
fast_llm = ChatOpenAI(
    model="gpt-3.5-turbo",
    temperature=0.0
)

# 정확한 모델(GPT-4) - 복잡한 추론 작업용
accurate_llm = ChatOpenAI(
    model="gpt-4",
    temperature=0.0
)

# 기본 모델은 빠른 처리를 위해 GPT-3.5 사용
llm = fast_llm 
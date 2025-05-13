import os
from langchain_openai import ChatOpenAI
from langchain.schema import HumanMessage, SystemMessage
from langchain.chains import LLMChain
from langchain.prompts import ChatPromptTemplate
import yaml
import json
import hashlib

# 환경 변수 설정 - os.environ에 추가해야 함
os.environ["LANGSMITH_TRACING"] = "true"
os.environ["LANGSMITH_ENDPOINT"] = "https://api.smith.langchain.com"
os.environ["LANGSMITH_API_KEY"] = "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"
os.environ["LANGSMITH_PROJECT"] = "tally-temporary"
os.environ["OPENAI_API_KEY"] = "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"

# LangChain 모델 설정과 호출
llm = ChatOpenAI(
    model_name="gpt-4o",
    temperature=0.1,
    # LangSmith 추적을 위한 메타데이터 추가
    metadata={
        "ls_provider": "openai",
        "ls_model_name": "gpt-4o"
    }
)

# 캐시 딕셔너리
prompt_cache = {}

def load_prompt(file_path):
    # 파일 내용의 해시 계산
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()
        content_hash = hashlib.md5(content.encode()).hexdigest()
    
    # 프롬프트가 이미 캐시되어 있는지 확인
    if content_hash in prompt_cache:
        return prompt_cache[content_hash]
    
    # 프롬프트 로드 및 캐싱
    prompt = yaml.safe_load(content)
    prompt_cache[content_hash] = prompt
    return prompt

# 1. yaml 설정파일 read
input_prompt = load_prompt('input_prompt.yaml')
system_prompt = load_prompt('system_prompt.yaml')

# 2. json 대화내역 read
with open('input_prompt.yaml', 'r', encoding='utf-8') as file:
    input_prompt = yaml.safe_load(file)
with open('system_prompt.yaml', 'r', encoding='utf-8') as file:
    system_prompt = yaml.safe_load(file)
with open('sample_conversation.json', 'r', encoding='utf-8') as file:
    json_content = json.load(file)

# Extract only 'speaker' and 'message_content'
extracted_content = [{'speaker': msg['speaker'], 'message_content': msg['message_content']} for msg in json_content['messages']]

# 모델 호출
response = llm.invoke(str(input_prompt) + str(extracted_content))
print(response.content)

response = llm.invoke(str(system_prompt) + str(response.content))
print(response.content)
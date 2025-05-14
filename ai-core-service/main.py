import asyncio
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import json

from load import load_prompt, load_conversation
from services.ai_service import process_conversation
from utils.currency_converter import convert_currency_in_json

# FastAPI 앱 생성
app = FastAPI(
    title="Tally Bot AI Core Service",
    description="대화 분석 및 통화 변환 API",
    version="1.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 요청 모델 정의
class ChatMessage(BaseModel):
    unique_chat_id: Optional[str] = None
    speaker: str
    message_content: str
    timestamp: Optional[str] = None

class ConversationRequest(BaseModel):
    chatroom_name: str
    members: List[str]
    messages: List[ChatMessage]
    prompt_file: str = "resources/input_prompt.yaml"

# 응답 모델 정의
class ConversationResponse(BaseModel):
    result: List[Dict[str, Any]]

# 기본 프롬프트 및 결과 처리 함수
async def result_handler(result):
    try:
        converted_result = await convert_currency_in_json(result)
        print("\n=== 통화 변환 결과 ===")
        print(json.dumps(converted_result, ensure_ascii=False, indent=2))
        print("=" * 60)
        return converted_result
    except Exception as e:
        print(f"통화 변환 오류: {str(e)}")
        print("=" * 60)
        return result

@app.get("/")
async def root():
    return {"message": "Tally Bot AI Core Service API"}

@app.post("/api/process", response_model=ConversationResponse)
async def process_api(request: ConversationRequest, background_tasks: BackgroundTasks):
    try:
        # 프롬프트 로드
        input_prompt = await load_prompt(request.prompt_file)
        
        # sample_conversation.json 형식에서 필요한 대화 형식으로 변환
        conversation = [{
            'speaker': 'system', 
            'message_content': f"members: {request.members}\nmember_count: {len(request.members)}"
        }]
        
        # 실제 대화 내용 추가
        conversation.extend([
            {
                'speaker': msg.speaker,
                'message_content': msg.message_content
            }
            for msg in request.messages
        ])
        
        # 대화 처리 및 결과 변환
        result = await process_conversation(
            conversation,
            input_prompt,
            callback=None
        )
        
        if not result:
            raise HTTPException(status_code=400, detail="처리 결과가 없습니다.")
        
        # 통화 변환 처리
        converted_result = await convert_currency_in_json(result)
        
        return {"result": converted_result}
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

@app.post("/api/process-file")
async def process_conversation_from_file(
    background_tasks: BackgroundTasks,
    conversation_file: str = "resources/sample_conversation.json",
    prompt_file: str = "resources/input_prompt.yaml"
):
    try:
        # 1. yaml 설정파일과 대화내역을 병렬로 로드
        input_prompt_task = asyncio.create_task(load_prompt(prompt_file))
        conversation_task = asyncio.create_task(load_conversation(conversation_file))
        
        # 모든 로드 작업이 완료될 때까지 대기
        input_prompt, conversation = await asyncio.gather(
            input_prompt_task, 
            conversation_task
        )

        # 2. 대화 처리
        result = await process_conversation(
            conversation, 
            input_prompt,
            callback=None
        )
        
        if not result:
            raise HTTPException(status_code=400, detail="처리 결과가 없습니다.")
            
        # 통화 변환 처리
        converted_result = await convert_currency_in_json(result)
        
        return {"result": converted_result}
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

# FastAPI가 uvicorn을 통해 실행될 때 사용
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True) 

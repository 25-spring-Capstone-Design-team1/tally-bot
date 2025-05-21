import json
from fastapi import HTTPException, BackgroundTasks

from config.app_config import create_app
from models.conversation import ConversationRequest, ConversationResponse
from handlers.process_handler import process_conversation_logic, load_resources

# FastAPI 앱 생성
app = create_app()

def format_json_for_console(data_list):
    """각 항목마다 줄바꿈이 있는 형태로 JSON 배열을 출력합니다."""
    result = "[\n"
    for i, item in enumerate(data_list):
        item_str = json.dumps(item, ensure_ascii=False, separators=(',', ':'))
        result += "  " + item_str
        if i < len(data_list) - 1:
            result += ","
        result += "\n"
    result += "]"
    return result

@app.get("/")
async def root():
    return {"message": "Tally Bot AI Core Service API"}

@app.post("/api/process", response_model=ConversationResponse)
async def process_api(request: ConversationRequest, background_tasks: BackgroundTasks):
    try:
        # 프롬프트 로드
        input_prompt, secondary_prompt, final_prompt, _ = await load_resources(
            request.prompt_file,
            request.secondary_prompt_file,
            request.final_prompt_file
        )
        
        # sample_conversation.json 형식에서 필요한 대화 형식으로 변환
        conversation = [{
            'speaker': 'system', 
            'message_content': f"members: {json.dumps(request.members, ensure_ascii=False)}\nmember_count: {len(request.members)}"
        }]
        
        # 실제 대화 내용 추가
        conversation.extend([
            {
                'speaker': msg.speaker,
                'message_content': msg.message_content
            }
            for msg in request.messages
        ])
        
        # 공통 대화 처리 로직 호출
        return await process_conversation_logic(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=final_prompt,
            members=request.members
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

@app.post("/api/process-file")
async def process_conversation_from_file(
    background_tasks: BackgroundTasks,
    conversation_file: str = "resources/sample_conversation.json", # 대화 JSON 파일
    prompt_file: str = "resources/input_prompt.yaml", # 1차 프롬프트 파일
    secondary_prompt_file: str = "resources/secondary_prompt.yaml",  # 2차 프롬프트 파일
    final_prompt_file: str = "resources/final_prompt.yaml"  # 3차 프롬프트 파일
):
    try:
        # 프롬프트와 대화 로드
        input_prompt, secondary_prompt, final_prompt, conversation = await load_resources(
            prompt_file,
            secondary_prompt_file,
            final_prompt_file,
            conversation_file
        )
        
        # member 정보 추출
        members_text = conversation[0]['message_content']
        members_line = [line for line in members_text.split('\n') if line.startswith('members:')][0]
        members_str = members_line.replace('members:', '').strip()
        members = json.loads(members_str)
        
        # 공통 대화 처리 로직 호출
        return await process_conversation_logic(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=final_prompt,
            members=members
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

# FastAPI가 uvicorn을 통해 실행될 때 사용
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True) 

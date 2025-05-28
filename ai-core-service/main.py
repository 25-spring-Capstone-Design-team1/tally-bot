import json
from fastapi import HTTPException, BackgroundTasks

from config.app_config import create_app
from models.conversation import ConversationRequest, ConversationResponse
from handlers.process_handler import process_conversation_logic, load_resources

# FastAPI 앱 생성
app = create_app()

def create_member_mapping(members_data):
    """
    멤버 데이터에서 ID-이름 매핑과 이름-ID 매핑을 생성합니다.
    
    Args:
        members_data: [{"0":"지훈", "1":"준호", "2":"소연", "3":"유진", "4":"민우"}]
        
    Returns:
        tuple: (id_to_name, name_to_id) 매핑 딕셔너리
    """
    id_to_name = {}
    name_to_id = {}
    
    if members_data and isinstance(members_data[0], dict):
        members_dict = members_data[0]
        for member_id, member_name in members_dict.items():
            id_to_name[member_id] = member_name
            name_to_id[member_name] = member_id
    
    return id_to_name, name_to_id

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
        
        # ID-이름 매핑 생성
        id_to_name, name_to_id = create_member_mapping(request.members)
        
        # sample_conversation.json 형식에서 필요한 대화 형식으로 변환
        conversation = [{
            'speaker': 'system', 
            'message_content': f"member_count: {len(id_to_name)}\nmember_mapping: {json.dumps(id_to_name, ensure_ascii=False)}"
        }]
        
        # 실제 대화 내용 추가
        conversation.extend([
            {
                'speaker': msg.speaker,
                'message_content': msg.message_content
            }
            for msg in request.messages
        ])
        
        # 대화 길이 확인 및 청크 처리 옵션 설정
        use_chunking = len(request.messages) > 15
        
        # 공통 대화 처리 로직 호출
        return await process_conversation_logic(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=final_prompt,
            member_names=list(id_to_name.values()),
            id_to_name=id_to_name,
            name_to_id=name_to_id,
            use_chunking=use_chunking
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

@app.post("/api/process-file")
async def process_conversation_from_file(
    background_tasks: BackgroundTasks,
    conversation_file: str = "resources/sample_conversation.json", # 대화 JSON 파일
    prompt_file: str = "resources/input_prompt.yaml", # 1차 프롬프트 파일
    secondary_prompt_file: str = "resources/secondary_prompt.yaml",  # 2차 프롬프트 파일
    final_prompt_file: str = "resources/final_prompt.yaml",  # 3차 프롬프트 파일
    use_chunking: bool = True  # 청크 처리 사용 여부
):
    try:
        # 프롬프트와 대화 로드
        input_prompt, secondary_prompt, final_prompt, conversation = await load_resources(
            prompt_file,
            secondary_prompt_file,
            final_prompt_file,
            conversation_file
        )
        
        # member 정보 추출 및 매핑 생성
        members_text = conversation[0]['message_content']
        
        # ID-이름 매핑이 이미 있는지 확인
        if 'member_mapping:' in members_text:
            # 기존 매핑 정보 사용
            mapping_line = [line for line in members_text.split('\n') if line.startswith('member_mapping:')][0]
            id_to_name = json.loads(mapping_line.replace('member_mapping:', '').strip())
            name_to_id = {name: id for id, name in id_to_name.items()}
            member_names = list(id_to_name.values())
        else:
            # members 정보에서 매핑 생성
            members_line = [line for line in members_text.split('\n') if line.startswith('members:')][0]
            members_str = members_line.replace('members:', '').strip()
            member_names = json.loads(members_str)
            
            # 딕셔너리 형태로 변환 (고정 형식 유지)
            members = [dict(zip(map(str, range(len(member_names))), member_names))]
            
            # ID-이름 매핑 생성
            id_to_name, name_to_id = create_member_mapping(members)
            
            # member_mapping 정보 추가 및 members 정보 대체
            for i, msg in enumerate(conversation):
                if msg['speaker'] == 'system' and i == 0:
                    content = msg['message_content']
                    lines = content.split('\n')
                    
                    # members: 라인 삭제
                    lines = [line for line in lines if not line.startswith('members:')]
                    
                    # member_mapping 및 count 추가
                    lines.append(f"member_count: {len(id_to_name)}")
                    lines.append(f"member_mapping: {json.dumps(id_to_name, ensure_ascii=False)}")
                    
                    # 다시 조합
                    msg['message_content'] = '\n'.join(lines)
                    break
        
        # 공통 대화 처리 로직 호출
        return await process_conversation_logic(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=final_prompt,
            member_names=member_names,
            id_to_name=id_to_name,
            name_to_id=name_to_id,
            use_chunking=use_chunking
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

# FastAPI가 uvicorn을 통해 실행될 때 사용
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True) 

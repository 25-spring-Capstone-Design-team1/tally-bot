import json
import aiofiles
from typing import List, Dict

# 캐시 딕셔너리
conversation_cache = {}

async def load_conversation(file_path: str) -> List[Dict[str, str]]:
    """대화 내용을 비동기로 로드하고 캐시하는 함수"""
    if file_path in conversation_cache:
        return conversation_cache[file_path]
    
    async with aiofiles.open(file_path, 'r', encoding='utf-8') as file:
        content = await file.read()
        json_content = json.loads(content)
        
        # 채팅방 멤버 정보와 대화 내용을 명확한 형식으로 구성
        members = json_content.get('members', [])
        conversation = [{'speaker': 'system', 'message_content': f"members: {members}"}]
        
        # 실제 대화 내용을 명확한 형식으로 추가
        conversation.extend([
            {
                'speaker': msg['speaker'],
                'message_content': msg['message_content']
            }
            for msg in json_content['messages']
        ])
        
        conversation_cache[file_path] = conversation
        return conversation 
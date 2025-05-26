from pydantic import BaseModel
from typing import List, Dict, Any, Optional

# FastAPI와 Pydantic을 사용하여 데이터 모델을 정의합니다.
# Pydantic 모델은 자동 데이터 검증, 직렬화/역직렬화를 제공하여 
# FastAPI에서 요청과 응답 데이터를 처리하는 데 사용됩니다.

class ChatMessage(BaseModel):
    # 개별 채팅 메시지 모델 - FastAPI가 요청에서 자동으로 파싱하고 검증합니다.
    unique_chat_id: Optional[str] = None
    speaker: str
    message_content: str
    timestamp: Optional[str] = None

class ConversationRequest(BaseModel):
    # API 요청 모델 - FastAPI 엔드포인트에서 request: ConversationRequest 형태로 사용됩니다.
    # FastAPI는 수신된 JSON을 자동으로 이 모델로 변환하고 검증합니다.
    chatroom_name: str
    members: List[Dict[str, str]]
    messages: List[ChatMessage]
    prompt_file: str = "resources/input_prompt.yaml" # 1차 프롬프트 파일
    secondary_prompt_file: str = "resources/secondary_prompt.yaml"  # 2차 프롬프트 파일
    final_prompt_file: str = "resources/final_prompt.yaml"  # 3차 프롬프트 파일

class ConversationResponse(BaseModel):
    # API 응답 모델 - FastAPI 엔드포인트에서 response_model=ConversationResponse로 사용됩니다.
    # 이 모델을 통해 FastAPI는 응답 데이터의 유효성을 검증하고 JSON으로 직렬화합니다.
    result: List[Dict[str, Any]] # 1차 결과 필드
    secondary_result: List[Dict[str, Any]]  # 2차 결과 필드
    final_result: List[Dict[str, Any]]  # 3차 결과 필드
"""
연속된 동일 speaker의 메시지를 결합하는 유틸리티 모듈입니다.

카카오톡이나 메신저에서 같은 사람이 연속으로 여러 메시지를 보내는 경우,
이를 하나의 메시지로 결합하여 AI가 더 나은 문맥을 이해할 수 있도록 합니다.
"""

from typing import List, Dict, Any
import copy

def merge_consecutive_messages(messages: List[Dict[str, Any]], merge_separator: str = "\n") -> List[Dict[str, Any]]:
    """
    연속된 동일 speaker의 메시지를 결합합니다.
    
    Args:
        messages (List[Dict[str, Any]]): 원본 메시지 리스트
        merge_separator (str): 메시지 결합 시 사용할 구분자 (기본값: "\\n")
        
    Returns:
        List[Dict[str, Any]]: 결합된 메시지 리스트
        
    Example:
        입력:
        [
            {"speaker": "1", "message_content": "삼겹살 총 8만 천원!", "timestamp": "14:30:21"},
            {"speaker": "1", "message_content": "내가 2만 천원 결제할 테니까 2만원씩 보내줘!", "timestamp": "14:30:23"}
        ]
        
        출력:
        [
            {"speaker": "1", "message_content": "삼겹살 총 8만 천원!\n내가 2만 천원 결제할 테니까 2만원씩 보내줘!", "timestamp": "14:30:21"}
        ]
    """
    
    if not messages:
        return []
    
    merged_messages = []
    current_merged = None
    
    for msg in messages:
        # 시스템 메시지는 결합하지 않음
        if msg.get('speaker') == 'system':
            # 이전에 결합 중이던 메시지가 있다면 추가
            if current_merged:
                merged_messages.append(current_merged)
                current_merged = None
            merged_messages.append(copy.deepcopy(msg))
            continue
        
        # 현재 결합 중인 메시지가 없거나 speaker가 다른 경우
        if not current_merged or current_merged['speaker'] != msg.get('speaker'):
            # 이전에 결합 중이던 메시지가 있다면 추가
            if current_merged:
                merged_messages.append(current_merged)
            
            # 새로운 결합 시작
            current_merged = copy.deepcopy(msg)
        else:
            # 같은 speaker의 연속 메시지 - 내용 결합
            current_merged['message_content'] += merge_separator + msg.get('message_content', '')
            
            # unique_chat_id가 있는 경우 범위로 표시
            if 'unique_chat_id' in current_merged and 'unique_chat_id' in msg:
                start_id = current_merged['unique_chat_id']
                end_id = msg['unique_chat_id']
                if start_id != end_id:
                    current_merged['unique_chat_id'] = f"{start_id}-{end_id}"
            
            # timestamp가 있는 경우 첫 번째 메시지의 timestamp 유지 (시작 시간)
            # 마지막 메시지의 timestamp를 end_timestamp로 저장
            if 'timestamp' in msg:
                current_merged['end_timestamp'] = msg['timestamp']
    
    # 마지막 결합 메시지 추가
    if current_merged:
        merged_messages.append(current_merged)
    
    return merged_messages

def merge_conversation_messages(conversation: List[Dict[str, Any]], merge_separator: str = "\n") -> List[Dict[str, Any]]:
    """
    대화 전체에서 연속된 동일 speaker 메시지를 결합합니다.
    
    Args:
        conversation (List[Dict[str, Any]]): 대화 데이터
        merge_separator (str): 메시지 결합 시 사용할 구분자
        
    Returns:
        List[Dict[str, Any]]: 메시지가 결합된 대화 데이터
    """
    return merge_consecutive_messages(conversation, merge_separator)

def merge_conversation_dict(conversation_dict: Dict[str, Any], merge_separator: str = "\n") -> Dict[str, Any]:
    """
    딕셔너리 형태의 대화 데이터에서 메시지를 결합합니다.
    
    Args:
        conversation_dict (Dict[str, Any]): {"messages": [...], "chatroom_name": "...", ...} 형태
        merge_separator (str): 메시지 결합 시 사용할 구분자
        
    Returns:
        Dict[str, Any]: 메시지가 결합된 대화 딕셔너리
    """
    result = copy.deepcopy(conversation_dict)
    
    if 'messages' in result:
        result['messages'] = merge_consecutive_messages(result['messages'], merge_separator)
    
    return result 
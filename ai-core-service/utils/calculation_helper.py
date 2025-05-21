"""
계산 로직을 처리하는 유틸리티 함수들을 제공하는 모듈입니다.
"""

def generate_standard_calculation(items, members):
    """
    표준 계산 로직을 처리하는 함수입니다.
    hint_type이 'n분의1'인 항목은 프로그래밍 방식으로 처리합니다.
    
    Args:
        items (list): 계산할 항목 목록
        members (list): 멤버 목록
        
    Returns:
        list: 계산된 결과 목록
    """
    result = []
    
    for item in items:
        speaker = item.get("speaker", "")
        hint_type = item.get("hint_type", "")
        
        # 기본 구조 생성 - 모든 필수 필드 포함
        processed_item = {
            "place": item.get("place", ""),
            "payer": speaker,
            "item": item.get("item", ""),
            "amount": item.get("amount", 0),
            "participants": members.copy(),
            "constants": {member: 0 for member in members},
            "ratios": {member: 1 for member in members}
        }
        
        result.append(processed_item)
    
    return result 
"""
계산 로직을 처리하는 유틸리티 함수들을 제공하는 모듈입니다.
"""
import re

def preserve_original_speaker(all_results, original_items):
    """
    원본 항목의 speaker 정보를 보존합니다.
    
    Args:
        all_results (list): 처리된 모든 결과 항목
        original_items (list): 원본 항목 목록
        
    Returns:
        list: speaker 정보가 복원된 결과 항목
    """
    # 원본 그대로 반환 (특별 처리 없음)
    return all_results

def extract_payer_from_hint(hint_phrases):
    """
    hint_phrases에서 실제 결제자 ID를 추출합니다.
    
    Args:
        hint_phrases (list): 힌트 문구 리스트
        
    Returns:
        str or None: 추출된 결제자 ID, 없으면 None
    """
    if not hint_phrases:
        return None
        
    # "X가 결제함", "X가 지불함" 패턴 찾기
    payer_pattern = re.compile(r'(\d+)(?:이|가) (?:결제|지불)')
    
    for phrase in hint_phrases:
        match = payer_pattern.search(phrase)
        if match:
            return match.group(1)
    
    return None

def generate_standard_calculation(items, member_names, id_to_name=None):
    """
    표준 계산 로직을 처리하는 함수입니다.
    hint_type이 'n분의1'인 항목은 프로그래밍 방식으로 처리합니다.
    
    Args:
        items (list): 계산할 항목 목록
        member_names (list): 멤버 이름 목록
        id_to_name (dict): 멤버 ID에서 이름으로의 매핑 {"0": "지훈", "1": "준호", ...}
        
    Returns:
        list: 계산된 결과 목록
    """
    result = []
    
    for item in items:
        speaker = item.get("speaker", "")
        
        # 참가자 목록 설정 (ID 기반으로 처리)
        participants = list(id_to_name.keys()) if id_to_name else member_names.copy()
        
        # speaker가 이름이고 ID로 변환이 필요한 경우
        if id_to_name and speaker in id_to_name.values():
            for member_id, name in id_to_name.items():
                if name == speaker:
                    speaker = member_id
                    break
        
        # hint_phrases에서 실제 결제자 추출 (n분의1 항목의 경우)
        payer = speaker
        if "hint_phrases" in item:
            extracted_payer = extract_payer_from_hint(item.get("hint_phrases", []))
            if extracted_payer:
                payer = extracted_payer
        
        # 기본 구조 생성 - 모든 필수 필드 포함
        processed_item = {
            "place": item.get("place", ""),
            "payer": payer,  # 추출된 결제자 또는 speaker
            "item": item.get("item", ""),
            "amount": item.get("amount", 0),
            "participants": participants,
            "constants": {member: 0 for member in participants},
            "ratios": {member: 1 for member in participants}
        }
        
        result.append(processed_item)
    
    return result 
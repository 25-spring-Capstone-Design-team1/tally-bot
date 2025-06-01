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
        
    # 다양한 payer 추출 패턴들
    patterns = [
        r'(\d+)(?:이|가) (?:결제|지불|계산|냄|샀|사줌)',  # "4가 결제함", "2가 계산함", "1이 냄"
        r'ID(\d+)(?:이|가) (?:결제|지불|계산)',           # "ID4가 지불함"
        r'(\d+)(?:이|가) (?:낸|산|사준)',                # "3이 낸", "2가 산", "4가 사준"
        r'(\d+) 결제',                                  # "4 결제"
        r'(\d+) 지불'                                   # "2 지불"
    ]
    
    for phrase in hint_phrases:
        for pattern in patterns:
            match = re.search(pattern, phrase)
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
        amount = item.get("amount", 0)
        
        # 참가자 목록 설정 (ID 기반으로 처리)
        participants = list(id_to_name.keys()) if id_to_name else member_names.copy()
        
        # speaker가 이름이고 ID로 변환이 필요한 경우
        if id_to_name and speaker in id_to_name.values():
            for member_id, name in id_to_name.items():
                if name == speaker:
                    speaker = member_id
                    break
        
        # 1인당 금액 감지 및 전체 금액으로 변환
        hint_phrases = item.get("hint_phrases", [])
        if hint_phrases:
            for phrase in hint_phrases:
                # 1인당 금액 패턴 확장
                per_person_patterns = [
                    "1인당",
                    "각자", 
                    "한 명당",
                    "개인당",
                    "사람당"
                ]
                
                # 패턴 감지 및 변환
                for pattern in per_person_patterns:
                    if pattern in phrase and ("원씩" in phrase or "지불함" in phrase):
                        # 전체 멤버 수만큼 곱하여 전체 금액으로 변환
                        total_members = len(participants)
                        amount = amount * total_members
                        break
                else:
                    continue
                break
        
        # hint_phrases에서 실제 결제자 추출 (n분의1 항목의 경우)
        payer = speaker
        if hint_phrases:
            extracted_payer = extract_payer_from_hint(hint_phrases)
            if extracted_payer:
                payer = extracted_payer
        
        # 기본 구조 생성 - 모든 필수 필드 포함
        processed_item = {
            "place": item.get("place", ""),
            "payer": payer,  # 추출된 결제자 또는 speaker
            "item": item.get("item", ""),
            "amount": amount,  # 변환된 전체 금액
            "participants": participants,
            "constants": {member: 0 for member in participants},
            "ratios": {member: 1 for member in participants}
        }
        
        result.append(processed_item)
    
    return result 
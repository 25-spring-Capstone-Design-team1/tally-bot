import json
import re
from utils.currency_converter import convert_currency_in_json
from utils.calculation_helper import generate_standard_calculation

def preprocess_conversation_results(result):
    """1차 처리 결과에 대한 통화 변환 처리"""
    return convert_currency_in_json(result)

def extract_items_only(converted_result):
    """2차 프롬프팅을 위한 item 필드만 추출"""
    return [{"item": item.get("item", "")} for item in converted_result]

def is_complex_settlement(item):
    """hint_phrases를 기반으로 복잡한 정산인지 판단"""
    hint_phrases = item.get("hint_phrases", [])
    
    # 빈 배열이면 일반 균등분할 (n분의1)
    if not hint_phrases:
        return False
        
    # hint_phrases가 있으면 복잡한 정산으로 판단
    for phrase in hint_phrases:
        # 금액대납 패턴 (화살표 포함)
        if "→" in phrase:
            return True
        # 기타 복잡한 패턴들
        if any(keyword in phrase for keyword in ["지불", "제외", "배", "1인당"]):
            return True
    
    return False

def parse_hint_phrases_to_settlement(item, secondary_result, id_to_name, name_to_id):
    """hint_phrases를 직접 파싱해서 정산 JSON을 생성하는 함수"""
    speaker = item.get("speaker", "")
    amount = item.get("amount", 0)
    hint_phrases = item.get("hint_phrases", [])
    
    # place 정보 매핑
    place = next((s.get("place", "") for s in secondary_result if s.get("item") == item.get("item")), "")
    
    # 전체 멤버 리스트
    all_members = list(id_to_name.keys()) if id_to_name else []
    
    # 기본 결과 구조
    result = {
        "place": place,
        "payer": speaker,
        "item": item.get("item", ""),
        "amount": amount,
        "participants": all_members.copy(),
        "constants": {member: 0 for member in all_members},
        "ratios": {member: 1 for member in all_members}
    }
    
    # 복합 패턴 처리를 위한 변수들
    designated_payers = set()  # 부담 금액이 지정된 멤버들
    per_person_amount = None   # 1인당 지불 금액
    
    # hint_phrases 파싱
    for phrase in hint_phrases:
        # 1. 금액대납 패턴 (화살표 포함)
        if "→" in phrase:
            arrow_match = re.search(r'(\d+)\s*→\s*(\d+)', phrase)
            if arrow_match:
                debtor = arrow_match.group(1)  # 갚아야 하는 사람
                creditor = arrow_match.group(2)  # 실제 돈을 낸 사람
                
                result["payer"] = creditor
                result["participants"] = [debtor]
                result["constants"] = {debtor: amount}
                result["ratios"] = {debtor: 1}
                return result  # 금액대납은 단독 처리
        
        # 2. 부담 금액 지정 패턴
        payment_match = re.search(r'(\d+)(?:이|가)\s*(\d+)원?\s*지불', phrase)
        if payment_match:
            member_id = payment_match.group(1)
            payment_amount = int(payment_match.group(2))
            
            if member_id in result["constants"]:
                result["constants"][member_id] = payment_amount
                designated_payers.add(member_id)  # 부담 금액 지정된 멤버로 추가
        
        # 3. 1인당 금액 지정 패턴 - 나중에 처리하기 위해 저장만
        if "1인당" in phrase and "지불" in phrase:
            per_person_match = re.search(r'1인당\s*(\d+)원?\s*지불', phrase)
            if per_person_match:
                per_person_amount = int(per_person_match.group(1))
        
        # 4. 참여자 제외 패턴
        exclude_match = re.search(r'(\d+)[은|는]?\s*제외', phrase)
        if exclude_match:
            exclude_id = exclude_match.group(1)
            if exclude_id in result["participants"]:
                result["participants"].remove(exclude_id)
            if exclude_id in result["constants"]:
                del result["constants"][exclude_id]
            if exclude_id in result["ratios"]:
                del result["ratios"][exclude_id]
        
        # 5. 배수 지불 패턴
        ratio_match = re.search(r'(\d+)(?:이|가)\s*(\d+)배\s*지불', phrase)
        if ratio_match:
            member_id = ratio_match.group(1)
            ratio_multiplier = int(ratio_match.group(2))
            if member_id in result["ratios"]:
                result["ratios"][member_id] = ratio_multiplier
        
        # 6. 지불자 지정 패턴
        payer_match = re.search(r'(\d+)(?:이|가)\s*지불', phrase)
        if payer_match and not re.search(r'(\d+)원?\s*지불', phrase):  # 금액이 없는 경우만
            payer_id = payer_match.group(1)
            result["payer"] = payer_id
    
    # 복합 패턴 처리: 1인당 금액이 있고 부담 금액 지정자가 있는 경우
    if per_person_amount is not None and designated_payers:
        # 부담 금액이 지정되지 않은 나머지 참여자들에게만 1인당 금액 적용
        for member in result["participants"]:
            if member not in designated_payers:
                result["constants"][member] = per_person_amount
    elif per_person_amount is not None:
        # 부담 금액 지정자가 없는 경우 모든 참여자에게 1인당 금액 적용 (기존 로직)
        for member in result["participants"]:
            result["constants"][member] = per_person_amount
    
    # 이름을 ID로 변환
    if name_to_id:
        result = convert_names_to_ids(result, name_to_id)
    
    return result

def extract_complex_items(converted_result):
    """복잡한 정산 항목을 추출 (hint_phrases 기반)"""
    complex_items = []
    for item in converted_result:
        if is_complex_settlement(item):
            extract = {
                "speaker": item.get("speaker", ""),
                "amount": item.get("amount", 0),
                "hint_phrases": item.get("hint_phrases", [])
            }
            complex_items.append(extract)
    return complex_items

def map_place_to_complex_items(complex_items, secondary_result, converted_result):
    """2차 결과(place 정보)를 complex_items에 매핑하고 최종 처리를 위한 정보 준비"""
    mapped_complex_items = []
    complex_index = 0
    
    for item in converted_result:
        if is_complex_settlement(item):
            if complex_index < len(complex_items):
                # final 프롬프트용 입력 (speaker, amount, hint_phrases만)
                final_input = complex_items[complex_index]
                
                # 추가 매핑 정보 (place, item)
                item_place = next((s.get("place", "") for s in secondary_result if s.get("item") == item.get("item", "")), "")
                
                mapped_item = {
                    **final_input,  # speaker, amount, hint_phrases
                    "place": item_place,
                    "item": item.get("item", "")
                }
                mapped_complex_items.append(mapped_item)
                complex_index += 1
    
    return mapped_complex_items

def process_complex_results_with_hint_phrases(converted_result, secondary_result, id_to_name, name_to_id):
    """hint_phrases를 직접 파싱해서 복잡한 정산 결과를 처리하는 함수 (final_prompt 대체)"""
    complex_results = []
    
    for item in converted_result:
        if is_complex_settlement(item):
            # hint_phrases를 직접 파싱해서 정산 JSON 생성
            settlement_result = parse_hint_phrases_to_settlement(item, secondary_result, id_to_name, name_to_id)
            complex_results.append(settlement_result)
    
    return complex_results

def process_complex_results(complex_results, mapped_complex_items, name_to_id=None):
    """복잡한 결과에 place, item, amount 매핑 및 특수 케이스 처리"""
    processed_results = []
    
    for i, result in enumerate(complex_results):
        if i < len(mapped_complex_items):
            original = mapped_complex_items[i]
            # 필드 값 매핑
            result["place"] = original.get("place", "")
            result["item"] = original.get("item", "")
            result["amount"] = original.get("amount", 0)
            
            # 이름을 ID로 변환 처리
            if name_to_id:
                result = convert_names_to_ids(result, name_to_id)
            
            # 필드 순서 재정렬
            ordered_result = reorder_result_fields(result)
            processed_results.append(ordered_result)
    
    return processed_results

def convert_names_to_ids(result, name_to_id):
    """결과 내의 이름을 ID로 변환"""
    # payer가 이름인 경우 ID로 변환
    if "payer" in result and result["payer"] in name_to_id:
        result["payer"] = name_to_id[result["payer"]]
    
    # participants 리스트의 이름을 ID로 변환
    if "participants" in result:
        result["participants"] = [
            name_to_id.get(participant, participant) 
            for participant in result["participants"]
        ]
    
    # constants와 ratios 딕셔너리의 키가 이름인 경우 ID로 변환
    for field in ["constants", "ratios"]:
        if field in result:
            new_dict = {}
            for name, value in result[field].items():
                member_id = name_to_id.get(name, name)
                new_dict[member_id] = value
            result[field] = new_dict
    
    # hint_phrases에 이름이 포함된 경우 ID로 변환
    if "hint_phrases" in result:
        for i, phrase in enumerate(result["hint_phrases"]):
            for name, member_id in name_to_id.items():
                # "ID" 접두사 없이 단순 id로 교체
                phrase = phrase.replace(name, member_id)
            result["hint_phrases"][i] = phrase
    
    return result

def reorder_result_fields(result):
    """결과 필드를 지정된 순서로 재정렬"""
    ordered_result = {}
    for key in ["place", "payer", "item", "amount", "participants", "constants", "ratios"]:
        if key in result:
            ordered_result[key] = result[key]
    return ordered_result

def prepare_standard_calculation_items(converted_result, secondary_result):
    """n분의1 계산을 위한 항목 준비 (hint_phrases 기반)"""
    return [{
        **item,
        "place": next((s.get("place", "") for s in secondary_result if s.get("item") == item.get("item")), ""),
        "item": item.get("item", ""),
        "amount": item.get("amount", 0)
    } for item in converted_result if not is_complex_settlement(item)]

def process_all_results(converted_result, secondary_result, complex_results, member_names, id_to_name=None, name_to_id=None):
    """모든 결과를 처리하고 합치기"""
    # 단순 계산 (n분의1) 처리
    standard_items = prepare_standard_calculation_items(converted_result, secondary_result)
    standard_results = generate_standard_calculation(standard_items, member_names, id_to_name)
    
    # 이름을 ID로 변환 (복잡한 결과가 아직 변환되지 않은 경우)
    if name_to_id and complex_results:
        for i, result in enumerate(complex_results):
            if not any(key.startswith("<ID:") for key in result.get("constants", {})):
                complex_results[i] = convert_names_to_ids(result, name_to_id)
    
    # 복잡한 결과가 있으면 합치기
    if complex_results:
        final_result = standard_results + complex_results
        return final_result
    else:
        return standard_results

def process_all_results_without_final_prompt(converted_result, secondary_result, member_names, id_to_name=None, name_to_id=None):
    """hint_phrases를 직접 파싱해서 모든 결과를 처리하는 함수 (final_prompt 사용 안함)"""
    # 단순 계산 (n분의1) 처리
    standard_items = prepare_standard_calculation_items(converted_result, secondary_result)
    standard_results = generate_standard_calculation(standard_items, member_names, id_to_name)
    
    # 복잡한 정산 처리 (hint_phrases 직접 파싱)
    complex_results = process_complex_results_with_hint_phrases(converted_result, secondary_result, id_to_name, name_to_id)
    
    # 모든 결과 합치기
    if complex_results:
        final_result = standard_results + complex_results
        return final_result
    else:
        return standard_results 
import json
from utils.currency_converter import convert_currency_in_json
from utils.calculation_helper import generate_standard_calculation

def preprocess_conversation_results(result):
    """1차 처리 결과에 대한 통화 변환 처리"""
    return convert_currency_in_json(result)

def extract_items_only(converted_result):
    """2차 프롬프팅을 위한 item 필드만 추출"""
    return [{"item": item.get("item", "")} for item in converted_result]

def extract_complex_items(converted_result):
    """n분의1이 아닌 복잡한 항목을 추출"""
    complex_items = []
    for item in converted_result:
        if item["hint_type"] != "n분의1":
            extract = {
                "speaker": item["speaker"],
                "hint_type": item["hint_type"],
                "item": item.get("item", ""),
                "amount": item.get("amount", 0)
            }
            if "hint_phrases" in item:
                extract["hint_phrases"] = item["hint_phrases"]
            complex_items.append(extract)
    return complex_items

def map_place_to_complex_items(complex_items, secondary_result):
    """2차 결과(place 정보)를 complex_items에 매핑"""
    mapped_complex_items = []
    for item in complex_items:
        item_place = next((s.get("place", "") for s in secondary_result if s.get("item") == item.get("item", "")), "")
        mapped_item = {
            "place": item_place,
            "speaker": item["speaker"],
            "item": item.get("item", ""),
            "amount": item.get("amount", 0),
            "hint_type": item["hint_type"],
            "hint_phrases": item.get("hint_phrases", [])
        }
        mapped_complex_items.append(mapped_item)
    return mapped_complex_items

def process_complex_results(complex_results, mapped_complex_items):
    """복잡한 결과에 place, item, amount 매핑 및 특수 케이스 처리"""
    processed_results = []
    for i, result in enumerate(complex_results):
        if i < len(mapped_complex_items):
            original = mapped_complex_items[i]
            # 필드 값 매핑
            result["place"] = original.get("place", "")
            result["item"] = original.get("item", "")
            result["amount"] = original.get("amount", 0)
            
            # hint_type이 "1인당고정"인 경우 특수 처리
            if original.get("hint_type") == "1인당고정" and "payer" in result:
                result = process_fixed_per_person_case(result, original)
            
            # 필드 순서 재정렬
            ordered_result = reorder_result_fields(result)
            processed_results.append(ordered_result)
    return processed_results

def process_fixed_per_person_case(result, original):
    """1인당고정 케이스의 특수 처리"""
    payer = result["payer"]
    
    # participants에서 payer 제거
    if "participants" in result and payer in result["participants"]:
        result["participants"].remove(payer)
    
    # constants에서 payer 제거
    if "constants" in result and payer in result["constants"]:
        del result["constants"][payer]
    
    # ratios에서 payer 제거
    if "ratios" in result and payer in result["ratios"]:
        del result["ratios"][payer]
    
    # participants 수를 세고 amount에 곱하기
    if "participants" in result:
        participants_count = len(result["participants"])
        if participants_count > 0:
            result["amount"] = original.get("amount", 0) * participants_count
    
    return result

def reorder_result_fields(result):
    """결과 필드를 지정된 순서로 재정렬"""
    ordered_result = {}
    for key in ["place", "payer", "item", "amount", "participants", "constants", "ratios"]:
        if key in result:
            ordered_result[key] = result[key]
    return ordered_result

def prepare_standard_calculation_items(converted_result, secondary_result):
    """n분의1 계산을 위한 항목 준비"""
    return [{
        **item,
        "place": next((s.get("place", "") for s in secondary_result if s.get("item") == item.get("item")), ""),
        "item": item.get("item", ""),
        "amount": item.get("amount", 0)
    } for item in converted_result if item["hint_type"] == "n분의1"]

def process_all_results(converted_result, secondary_result, complex_results, members):
    """모든 결과를 처리하고 합치기"""
    # 단순 계산 (n분의1) 처리
    standard_items = prepare_standard_calculation_items(converted_result, secondary_result)
    standard_results = generate_standard_calculation(standard_items, members)
    
    # 복잡한 결과가 있으면 합치기
    if complex_results:
        return standard_results + complex_results
    else:
        return standard_results 
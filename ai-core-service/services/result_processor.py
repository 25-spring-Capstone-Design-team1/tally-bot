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
        if item.get("hint_type") != "n분의1":
            extract = {
                "speaker": item.get("speaker", ""),
                "amount": item.get("amount", 0),
                "hint_type": item.get("hint_type", ""),
                "hint_phrases": item.get("hint_phrases", [])
            }
            complex_items.append(extract)
    return complex_items

def map_place_to_complex_items(complex_items, secondary_result, converted_result):
    """2차 결과(place 정보)를 complex_items에 매핑하고 최종 처리를 위한 정보 준비"""
    mapped_complex_items = []
    complex_index = 0
    
    for item in converted_result:
        if item.get("hint_type") != "n분의1":
            if complex_index < len(complex_items):
                # final 프롬프트용 입력 (speaker, amount, hint_type, hint_phrases만)
                final_input = complex_items[complex_index]
                
                # 추가 매핑 정보 (place, item)
                item_place = next((s.get("place", "") for s in secondary_result if s.get("item") == item.get("item", "")), "")
                
                mapped_item = {
                    **final_input,  # speaker, amount, hint_type, hint_phrases
                    "place": item_place,
                    "item": item.get("item", "")
                }
                mapped_complex_items.append(mapped_item)
                complex_index += 1
    
    return mapped_complex_items

def process_complex_results(complex_results, mapped_complex_items, name_to_id=None):
    """복잡한 결과에 place, item, amount 매핑 및 특수 케이스 처리"""
    processed_results = []
    
    print(f"🔄 복잡한 결과 처리: complex_results={len(complex_results)}개, mapped_complex_items={len(mapped_complex_items)}개")
    
    for i, result in enumerate(complex_results):
        if i < len(mapped_complex_items):
            original = mapped_complex_items[i]
            # 필드 값 매핑
            result["place"] = original.get("place", "")
            result["item"] = original.get("item", "")
            result["amount"] = original.get("amount", 0)
            
            print(f"   ✅ 매핑 완료 [{i+1}]: {original.get('item', 'Unknown')} (amount: {original.get('amount', 0)})")
            
            # 이름을 ID로 변환 처리
            if name_to_id:
                result = convert_names_to_ids(result, name_to_id)
            
            # 필드 순서 재정렬
            ordered_result = reorder_result_fields(result)
            processed_results.append(ordered_result)
        else:
            print(f"   ⚠️ 매핑 실패 [{i+1}]: mapped_complex_items 범위 초과")
    
    if len(complex_results) > len(mapped_complex_items):
        print(f"   ⚠️ 경고: complex_results({len(complex_results)})가 mapped_complex_items({len(mapped_complex_items)})보다 많습니다.")
    
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
    """n분의1 계산을 위한 항목 준비"""
    return [{
        **item,
        "place": next((s.get("place", "") for s in secondary_result if s.get("item") == item.get("item")), ""),
        "item": item.get("item", ""),
        "amount": item.get("amount", 0)
    } for item in converted_result if item.get("hint_type") == "n분의1"]

def process_all_results(converted_result, secondary_result, complex_results, member_names, id_to_name=None, name_to_id=None):
    """모든 결과를 처리하고 합치기"""
    # 단순 계산 (n분의1) 처리
    standard_items = prepare_standard_calculation_items(converted_result, secondary_result)
    standard_results = generate_standard_calculation(standard_items, member_names, id_to_name)
    
    print(f"🔄 최종 결과 합치기:")
    print(f"   📊 표준 결과 (n분의1): {len(standard_results)}개")
    print(f"   📊 복잡한 결과: {len(complex_results) if complex_results else 0}개")
    
    # 이름을 ID로 변환 (복잡한 결과가 아직 변환되지 않은 경우)
    if name_to_id and complex_results:
        for i, result in enumerate(complex_results):
            if not any(key.startswith("<ID:") for key in result.get("constants", {})):
                complex_results[i] = convert_names_to_ids(result, name_to_id)
    
    # 복잡한 결과가 있으면 합치기
    if complex_results:
        final_result = standard_results + complex_results
        print(f"   ✅ 최종 결과: {len(final_result)}개 (표준 {len(standard_results)}개 + 복잡한 {len(complex_results)}개)")
        return final_result
    else:
        print(f"   ✅ 최종 결과: {len(standard_results)}개 (표준 결과만)")
        return standard_results 
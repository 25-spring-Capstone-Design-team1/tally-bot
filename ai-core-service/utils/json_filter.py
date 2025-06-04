def filter_invalid_amounts(json_results):
    """
    AI 출력 JSON에서 amount가 0이하인 항목을 제거하는 함수
    
    Args:
        json_results (list): AI가 출력한 JSON 배열
        
    Returns:
        list: amount가 양수인 항목만 포함된 JSON 배열
        
    Examples:
        >>> results = [
        ...     {"item": "식당", "amount": 15000},
        ...     {"item": "영화", "amount": 0},
        ...     {"item": "택시", "amount": -5000, "hint_phrases": ["4 → 2"]},
        ...     {"item": "커피", "amount": 8000}
        ... ]
        >>> filter_invalid_amounts(results)
        [
            {"item": "식당", "amount": 15000},
            {"item": "커피", "amount": 8000}
        ]
    """
    if not isinstance(json_results, list):
        return json_results
    
    # amount가 양수인 항목만 필터링
    filtered_results = []
    removed_count = 0
    
    for item in json_results:
        if isinstance(item, dict) and 'amount' in item:
            amount = item.get('amount', 0)
            
            # amount가 양수인 경우만 포함
            if isinstance(amount, (int, float)) and amount > 0:
                filtered_results.append(item)
            else:
                removed_count += 1
                print(f"⚠️ 제거된 항목: {item.get('item', 'Unknown')} (amount: {amount})")
        else:
            # amount 키가 없는 경우도 제거
            removed_count += 1
            print(f"⚠️ 제거된 항목: amount 키 없음 - {item}")
    
    if removed_count > 0:
        print(f"📊 총 {removed_count}개 항목이 제거되었습니다.")
    
    return filtered_results


def add_speaker_and_filter(ai_results, conversation_data):
    """
    AI 출력에 speaker를 추가하고 invalid amount를 필터링하는 통합 함수
    
    Args:
        ai_results (list): AI가 출력한 JSON 배열 (speaker 없음)
        conversation_data (list): 원본 대화 데이터 [{"speaker": "1", "message": "..."}, ...]
        
    Returns:
        list: speaker가 추가되고 유효한 amount만 포함된 JSON 배열
    """
    # 1단계: invalid amount 필터링
    filtered_results = filter_invalid_amounts(ai_results)
    
    # 2단계: speaker 매핑
    result_with_speaker = []
    
    for i, result in enumerate(filtered_results):
        # 대화 순서에 따라 speaker 할당
        if i < len(conversation_data):
            result_with_speaker.append({
                "speaker": conversation_data[i]["speaker"],
                **result  # 기존 JSON 내용 유지
            })
        else:
            # conversation_data보다 결과가 많은 경우 (예외 상황)
            print(f"⚠️ 경고: 대화 데이터보다 많은 결과 - {result}")
            result_with_speaker.append(result)
    
    return result_with_speaker


def validate_json_structure(json_results):
    """
    JSON 구조의 유효성을 검증하는 함수
    
    Args:
        json_results (list): 검증할 JSON 배열
        
    Returns:
        tuple: (is_valid: bool, error_messages: list)
    """
    if not isinstance(json_results, list):
        return False, ["결과가 배열 형태가 아닙니다."]
    
    errors = []
    required_fields = ['item', 'amount']
    
    for i, item in enumerate(json_results):
        if not isinstance(item, dict):
            errors.append(f"항목 {i}: 객체 형태가 아닙니다.")
            continue
            
        # 필수 필드 검증
        for field in required_fields:
            if field not in item:
                errors.append(f"항목 {i}: '{field}' 필드가 없습니다.")
        
        # amount 검증
        if 'amount' in item:
            amount = item['amount']
            if not isinstance(amount, (int, float)) or amount <= 0:
                errors.append(f"항목 {i}: amount가 유효하지 않습니다. ({amount})")
        
        # hint_phrases 검증 (선택적)
        if 'hint_phrases' in item:
            hint_phrases = item['hint_phrases']
            if not isinstance(hint_phrases, list):
                errors.append(f"항목 {i}: hint_phrases가 배열 형태가 아닙니다.")
        
        # item 검증
        if 'item' in item:
            if not isinstance(item['item'], str) or not item['item'].strip():
                errors.append(f"항목 {i}: item이 유효하지 않습니다.")
    
    return len(errors) == 0, errors


# 사용 예시
if __name__ == "__main__":
    # 테스트 데이터
    test_results = [
        {"item": "식당", "amount": 15000},
        {"item": "영화", "amount": 0},
        {"item": "택시", "amount": -5000, "hint_phrases": ["4 → 2"]},
        {"item": "커피", "amount": 8000},
        {"item": "잘못된항목"}  # amount 없음
    ]
    
    print("=== 원본 데이터 ===")
    for item in test_results:
        print(item)
    
    print("\n=== 필터링 후 ===")
    filtered = filter_invalid_amounts(test_results)
    for item in filtered:
        print(item)
    
    print("\n=== 구조 검증 ===")
    is_valid, errors = validate_json_structure(filtered)
    print(f"유효성: {is_valid}")
    if errors:
        for error in errors:
            print(f"- {error}") 
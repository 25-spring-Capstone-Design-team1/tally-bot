import json

def format_json_for_console(data_list):
    """각 항목마다 줄바꿈이 있는 형태로 JSON 배열을 출력합니다."""
    result = "[\n"
    for i, item in enumerate(data_list):
        item_str = json.dumps(item, ensure_ascii=False, separators=(',', ':'))
        result += "  " + item_str
        if i < len(data_list) - 1:
            result += ","
        result += "\n"
    result += "]"
    return result

def log_processing_stage(stage_name, data=None):
    """각 처리 단계의 로그를 출력합니다."""
    print(f"\n=== {stage_name} ===")
    if data:
        if isinstance(data, list):
            print(format_json_for_console(data))
        else:
            print(data) 
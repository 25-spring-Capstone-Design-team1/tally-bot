import asyncio
from typing import Dict, List, Any, Union

# 하드코딩된 환율 정보
EXCHANGE_RATES = {
    "EUR": 1400.0,  # 1 EUR = 1400 KRW
    "USD": 1300.0,  # 1 USD = 1300 KRW
    "JPY": 9.0,     # 1 JPY = 9 KRW
    "CNY": 180.0,   # 1 CNY = 180 KRW
    "GBP": 1650.0,  # 1 GBP = 1650 KRW
}

async def convert_to_krw(amount: float, from_currency: str) -> int:
    """
    외화를 원화로 변환하는 함수
    :param amount: 변환할 금액
    :param from_currency: 원래 통화 (EUR, USD, JPY 등)
    :return: 원화로 변환된 금액 (반올림된 정수)
    """
    if from_currency == "KRW":
        return int(amount)
        
    if from_currency not in EXCHANGE_RATES:
        raise ValueError(f"지원하지 않는 통화: {from_currency}")
        
    converted_amount = amount * EXCHANGE_RATES[from_currency]
    return round(converted_amount)  # 반올림하여 정수로 반환

async def convert_currency_in_json(json_data: Union[Dict[str, Any], List[Dict[str, Any]]]) -> Union[Dict[str, Any], List[Dict[str, Any]]]:
    """JSON 데이터 내의 통화를 원화로 변환하는 함수 (변환 후 currency가 KRW인 경우 해당 속성을 제거)"""
    try:
        # 배열인 경우 각 항목을 재귀적으로 처리
        if isinstance(json_data, list):
            return [await convert_currency_in_json(item) for item in json_data]
        
        # 딕셔너리인 경우
        if isinstance(json_data, dict):
            result = {}
            for key, value in json_data.items():
                if isinstance(value, (dict, list)):
                    result[key] = await convert_currency_in_json(value)
                elif isinstance(value, str) and key == 'currency' and value != 'KRW':
                    # 금액 필드 찾기
                    amount_key = next((k for k in json_data.keys() if 'amount' in k.lower()), None)
                    if amount_key and isinstance(json_data[amount_key], (int, float)):
                        # 환율 변환
                        krw_amount = await convert_to_krw(json_data[amount_key], value)
                        result[amount_key] = krw_amount
                        # KRW로 변환된 경우 currency 필드 생략
                else:
                    result[key] = value
            return result
        
        return json_data
    except Exception as e:
        print(f"Error converting currency: {str(e)}")
        return json_data

# 테스트용 코드
async def main():
    # EUR 100을 KRW로 변환 테스트
    krw_amount = await convert_to_krw(100, "EUR")
    print(f"100 EUR = {krw_amount} KRW")

if __name__ == "__main__":
    asyncio.run(main()) 
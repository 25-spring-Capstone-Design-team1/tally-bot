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
            result = json_data.copy()  # 원본 데이터 보존
            
            # currency와 amount 필드가 있는 경우에만 변환
            if 'currency' in result and 'amount' in result:
                currency = result['currency']
                amount = result['amount']
                
                if currency != 'KRW' and isinstance(amount, (int, float)):
                    # 환율 변환
                    krw_amount = await convert_to_krw(amount, currency)
                    result['amount'] = krw_amount
                    # currency 필드 제거
                    del result['currency']
            
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
import asyncio
from load import load_prompt, load_conversation
from services.ai_service import process_conversation
from utils.currency_converter import convert_currency_in_json
import json

async def result_handler(result):
    """처리 결과를 처리하는 콜백 함수"""
    print("\n=== 최종 처리 결과 ===")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    print("=" * 60)
    
    # 통화 변환 처리 (필요시)
    try:
        converted_result = await convert_currency_in_json(result)
        print("\n=== 통화 변환 결과 ===")
        print(json.dumps(converted_result, ensure_ascii=False, indent=2))
        print("=" * 60)
    except Exception as e:
        print(f"통화 변환 오류: {str(e)}")
        print("=" * 60)

async def main():
    try:
        # 1. yaml 설정파일과 대화내역을 병렬로 로드
        input_prompt_task = asyncio.create_task(load_prompt('resources/input_prompt.yaml'))
        conversation_task = asyncio.create_task(load_conversation('resources/sample_conversation.json'))
        
        # 모든 로드 작업이 완료될 때까지 대기
        input_prompt, conversation = await asyncio.gather(
            input_prompt_task,
            conversation_task
        )

        # 2. 대화 처리 (GPT가 대화에서 원래 금액과 통화만 추출)
        # 청크별 결과는 chunk_result_handler 콜백을 통해 main.py에서 처리
        result = await process_conversation(
            conversation, 
            input_prompt,
            callback=result_handler
        )
        
        if not result:
            print("\n처리 결과가 없습니다.")

    except Exception as e:
        print(f"Error in main: {str(e)}")
        import traceback
        print(traceback.format_exc())

if __name__ == "__main__":
    asyncio.run(main()) 
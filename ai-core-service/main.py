import asyncio
from load import load_prompt, load_conversation
from services.ai_service import process_conversation
from utils.currency_converter import convert_currency_in_json
import json

async def main():
    try:
        # 1. yaml 설정파일과 대화내역을 병렬로 로드
        input_prompt_task = asyncio.create_task(load_prompt('config/CoD.yaml'))
        conversation_task = asyncio.create_task(load_conversation('config/tmp.json'))
        
        # 모든 로드 작업이 완료될 때까지 대기
        input_prompt, conversation = await asyncio.gather(
            input_prompt_task,
            conversation_task
        )

        # 2. 대화 처리 (GPT가 대화에서 원래 금액과 통화만 추출)
        result = await process_conversation(conversation, input_prompt)
        
        if result:
            print("\n=== 처리 결과 ===")
            try:
                print(json.dumps(result, ensure_ascii=False, indent=2))
                
                # 3. 통화 변환 (외화를 원화로 자동 변환)
                # try:
                #    converted_result = await convert_currency_in_json(result)
                #    print("\n=== 통화 변환 후 결과 ===")
                #    print(json.dumps(converted_result, ensure_ascii=False, indent=2))
                # except Exception as e:
                #    print(f"\n통화 변환 오류: {str(e)}")
                #    print("원본 결과를 그대로 출력합니다")
            except Exception as e:
                print(f"JSON 직렬화 오류: {str(e)}")
                print("원본 결과:")
                print(result)

    except Exception as e:
        print(f"Error in main: {str(e)}")
        import traceback
        print(traceback.format_exc())

if __name__ == "__main__":
    asyncio.run(main()) 
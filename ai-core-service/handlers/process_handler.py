import asyncio
import json
from fastapi import HTTPException

from load import load_prompt, load_conversation
from services.ai_service import process_conversation, process_summary, process_final
from utils.logging_utils import log_processing_stage
from services.result_processor import (
    preprocess_conversation_results, 
    extract_items_only,
    extract_complex_items,
    map_place_to_complex_items,
    process_complex_results,
    process_all_results
)

async def process_conversation_logic(
    conversation, 
    input_prompt, 
    secondary_prompt, 
    final_prompt,
    members
):
    """대화 처리에 필요한 공통 로직을 처리합니다."""
    
    # 1. 1차 대화 처리
    result = await process_conversation(conversation, input_prompt, callback=None)
    if not result:
        raise HTTPException(status_code=400, detail="처리 결과가 없습니다.")
        
    # 통화 변환 처리
    converted_result = await preprocess_conversation_results(result)
    log_processing_stage("통화 변환 후 결과", converted_result)
    
    # 2. 2차 대화 처리
    items_only = extract_items_only(converted_result)
    secondary_conversation = [
        {'speaker': 'system', 'message_content': "다음은 분석할 항목 목록입니다."},
        {'speaker': 'user', 'message_content': json.dumps(items_only, ensure_ascii=False)}
    ]
    
    secondary_result = await process_summary(secondary_conversation, secondary_prompt, callback=None)
    if not secondary_result:
        raise HTTPException(status_code=400, detail="2차 처리 결과가 없습니다.")
    
    log_processing_stage("2차 처리 결과", secondary_result)
    
    # 3. 복잡한 항목 추출 및 처리
    complex_items = extract_complex_items(converted_result)
    final_result = None
    
    # 복잡한 항목이 있는 경우에만 3차 처리
    if complex_items:
        mapped_complex_items = map_place_to_complex_items(complex_items, secondary_result)
        
        # 3차 프롬프팅을 위한 대화 구성
        members_info = f"members: {json.dumps(members, ensure_ascii=False)}\nmember_count: {len(members)}"
        final_conversation = [
            {'speaker': 'system', 'message_content': f"{members_info}\n\n다음은 분석할 필드 추출 목록입니다."},
            {'speaker': 'user', 'message_content': json.dumps(mapped_complex_items, ensure_ascii=False)}
        ]
        
        # 3차 프롬프팅 처리
        complex_results = await process_final(final_conversation, final_prompt, callback=None)
        
        if complex_results:
            # 복잡한 결과 후처리
            processed_complex_results = process_complex_results(complex_results, mapped_complex_items)
            log_processing_stage("복잡한 항목 목록", processed_complex_results)
            
            # 모든 결과 처리 및 합치기
            final_result = process_all_results(converted_result, secondary_result, processed_complex_results, members)
        else:
            # 복잡한 결과가 없는 경우 표준 결과만 처리
            final_result = process_all_results(converted_result, secondary_result, None, members)
    else:
        # 복잡한 항목이 없는 경우 표준 결과만 처리
        final_result = process_all_results(converted_result, secondary_result, None, members)
    
    if final_result:
        log_processing_stage("최종 처리 결과", f"객체 갯수: {len(final_result)}")
        log_processing_stage("최종 처리 결과", final_result)
    else:
        raise HTTPException(status_code=400, detail="최종 처리 결과가 없습니다.")
    
    # 최종 결과 반환
    return {
        "result": converted_result,
        "secondary_result": secondary_result,
        "final_result": final_result
    }

async def load_resources(
    prompt_file,
    secondary_prompt_file,
    final_prompt_file,
    conversation_file=None,
    conversation=None
):
    """필요한 리소스를 병렬로 로드합니다."""
    tasks = [
        asyncio.create_task(load_prompt(prompt_file)),
        asyncio.create_task(load_prompt(secondary_prompt_file)),
        asyncio.create_task(load_prompt(final_prompt_file))
    ]
    
    if conversation_file:
        tasks.append(asyncio.create_task(load_conversation(conversation_file)))
        input_prompt, secondary_prompt, final_prompt, loaded_conversation = await asyncio.gather(*tasks)
        return input_prompt, secondary_prompt, final_prompt, loaded_conversation
    else:
        input_prompt, secondary_prompt, final_prompt = await asyncio.gather(*tasks)
        return input_prompt, secondary_prompt, final_prompt, conversation 
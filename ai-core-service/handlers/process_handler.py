import asyncio
import json
from fastapi import HTTPException

from load import load_prompt, load_conversation
from services.ai_service import process_conversation, process_summary, process_final
from services.chain_ai_service import ChainAIService
from utils.logging_utils import log_processing_stage
from utils.message_merger import merge_conversation_dict
from services.result_processor import (
    preprocess_conversation_results, 
    extract_items_only,
    extract_complex_items,
    map_place_to_complex_items,
    process_complex_results,
    process_all_results
)

async def split_and_process_conversation(
    conversation, 
    input_prompt, 
    secondary_prompt, 
    final_prompt,
    member_names,
    id_to_name,
    name_to_id,
    chunk_size=10,
    max_system_messages=1
):
    """
    대화를 청크로 분할하여 처리합니다.
    
    Args:
        conversation (list or dict): 전체 대화
        input_prompt (str): 1차 프롬프트
        secondary_prompt (str): 2차 프롬프트
        final_prompt (str): 3차 프롬프트
        member_names (list): 멤버 이름 목록
        id_to_name (dict): ID에서 이름으로 매핑
        name_to_id (dict): 이름에서 ID로 매핑
        chunk_size (int): 각 청크당 대화 메시지 수
        max_system_messages (int): 시스템 메시지 최대 개수 (첫 시스템 메시지만 보존)
        
    Returns:
        dict: 처리 결과
    """
    # conversation이 리스트인지 딕셔너리인지 확인
    if isinstance(conversation, dict):
        # 딕셔너리인 경우 (chatroom_name, members, messages 구조)
        messages = conversation.get("messages", [])
        chatroom_name = conversation.get("chatroom_name", "")
        members = conversation.get("members", [])
    else:
        # 리스트인 경우 (원본 대화 형식)
        messages = conversation
        chatroom_name = ""
        members = []
    
    # 시스템 메시지와 사용자 메시지 분리
    system_messages = []
    user_messages = []
    
    for msg in messages:
        if isinstance(msg, dict) and msg.get('speaker') == 'system' and len(system_messages) < max_system_messages:
            system_messages.append(msg)
        else:
            user_messages.append(msg)
    
    # 사용자 메시지를 청크로 분할 (순서대로)
    message_chunks = [user_messages[i:i+chunk_size] for i in range(0, len(user_messages), chunk_size)]
    
    # 각 청크별 결과 저장
    all_results = []
    
    # 청크별로 처리
    for i, message_chunk in enumerate(message_chunks):
        log_processing_stage(f"청크 {i+1}/{len(message_chunks)} 처리 중", f"메시지 {len(message_chunk)}개")
        
        # 원본 구조 복제하고 messages만 현재 청크로 교체
        # 시스템 메시지 포함
        current_messages = system_messages + message_chunk
        
        # conversation이 원래 딕셔너리였는지 리스트였는지에 따라 다르게 처리
        if isinstance(conversation, dict):
            chunk_conversation = {
                "chatroom_name": chatroom_name,
                "members": members,
                "messages": current_messages
            }
        else:
            # 원본이 리스트였다면 리스트 형태로 처리
            chunk_conversation = current_messages
        
        # 현재 청크 처리
        chunk_result = await process_conversation(chunk_conversation, input_prompt, callback=None, members=members)
        if chunk_result:
            # 결과 변환 및 병합
            converted_chunk = await preprocess_conversation_results(chunk_result)
            
            # 현재 청크 처리 결과 로깅
            log_processing_stage(f"청크 {i+1} 처리 결과", converted_chunk)
            
            all_results.extend(converted_chunk)
        else:
            log_processing_stage(f"청크 {i+1} 처리 결과 없음", "건너뜀")
    
    # 전체 결과 로깅
    log_processing_stage("청크 처리 후 전체 결과", f"항목 {len(all_results)}개")
    log_processing_stage("청크 처리 후 전체 결과 내용", all_results)

    tablets = []
    filtered_out_count = 0
    for result in all_results:
        if result.get("hint_type") != "미정":
            tablets.append(result)
        else:
            filtered_out_count += 1
            log_processing_stage(f"미정 항목 제외", f"item: {result.get('item', 'Unknown')}, speaker: {result.get('speaker', 'Unknown')}")
    
    log_processing_stage("필터링 결과", f"처리 대상: {len(tablets)}개, 제외된 미정 항목: {filtered_out_count}개")

    if tablets:
        return await process_secondary_and_final(
            tablets,
            secondary_prompt,
            final_prompt,
            member_names,
            id_to_name,
            name_to_id
        )
    else:
        log_processing_stage("최종 처리 결과 없음", "빈 결과 반환")
        return {
            "final_result": []
        }

async def process_secondary_and_final(
    converted_result,
    secondary_prompt,
    final_prompt,
    member_names,
    id_to_name,
    name_to_id
):
    """
    2차와 3차 처리를 수행합니다.
    
    Args:
        converted_result (list): 1차 처리된 결과
        secondary_prompt (str): 2차 프롬프트
        final_prompt (str): 3차 프롬프트
        member_names (list): 멤버 이름 목록
        id_to_name (dict): ID에서 이름으로 매핑
        name_to_id (dict): 이름에서 ID로 매핑
        
    Returns:
        dict: 처리 결과
    """
    # 2차 대화 처리
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
        mapped_complex_items = map_place_to_complex_items(complex_items, secondary_result, converted_result)
        
        # 3차 프롬프팅을 위한 입력 데이터 준비 (final 프롬프트에 필요한 필드만)
        final_input_data = []
        for item in mapped_complex_items:
            final_input = {
                "speaker": item["speaker"],
                "amount": item["amount"],
                "hint_type": item["hint_type"],
                "hint_phrases": item.get("hint_phrases", [])
            }
            final_input_data.append(final_input)
        
        # 3차 프롬프팅을 위한 대화 구성
        members_info = f"member_count: {len(id_to_name)}\nmember_mapping: {json.dumps(id_to_name, ensure_ascii=False)}"
        final_conversation = [
            {'speaker': 'system', 'message_content': f"{members_info}\n\n다음은 분석할 필드 추출 목록입니다."},
            {'speaker': 'user', 'message_content': json.dumps(final_input_data, ensure_ascii=False)}
        ]
        
        # 3차 프롬프팅 처리
        complex_results = await process_final(final_conversation, final_prompt, callback=None)
        
        if complex_results:
            # 복잡한 결과 후처리
            processed_complex_results = process_complex_results(complex_results, mapped_complex_items, name_to_id)
            log_processing_stage("복잡한 항목 목록", processed_complex_results)
            log_processing_stage("복잡한 항목 후처리 결과 항목 수", f"{len(processed_complex_results)}개")
            
            # 모든 결과 처리 및 합치기
            final_result = process_all_results(converted_result, secondary_result, processed_complex_results, member_names, id_to_name, name_to_id)
        else:
            log_processing_stage("3차 프롬프팅 결과 없음", "표준 결과만 처리")
            # 복잡한 결과가 없는 경우 표준 결과만 처리
            final_result = process_all_results(converted_result, secondary_result, None, member_names, id_to_name, name_to_id)
    else:
        log_processing_stage("복잡한 항목 없음", "표준 결과만 처리")
        # 복잡한 항목이 없는 경우 표준 결과만 처리
        final_result = process_all_results(converted_result, secondary_result, None, member_names, id_to_name, name_to_id)
    
    if final_result:
        log_processing_stage("최종 처리 결과", f"객체 갯수: {len(final_result)}")
        log_processing_stage("최종 처리 결과", final_result)
    else:
        raise HTTPException(status_code=400, detail="3차 처리 결과가 없습니다.")

    
    # 최종 결과 반환
    return {
        "final_result": final_result
    }

async def process_conversation_logic(
    conversation, 
    input_prompt, 
    secondary_prompt, 
    final_prompt,
    member_names,
    id_to_name,
    name_to_id,
    use_chunking=True
):
    """대화 처리에 필요한 공통 로직을 처리합니다."""
    
    # 메시지 결합 전처리 추가
    original_conversation = conversation
    if isinstance(conversation, dict):
        # 딕셔너리 형태인 경우 메시지 결합
        original_messages = conversation.get("messages", [])
        conversation = merge_conversation_dict(conversation)
        merged_messages = conversation.get("messages", [])
        
        messages = merged_messages
    else:
        # 리스트 형태인 경우 직접 메시지 결합
        from utils.message_merger import merge_conversation_messages
        merged_conversation = merge_conversation_messages(conversation)
        
        conversation = merged_conversation
        messages = merged_conversation
    
    # conversation이 리스트인지 딕셔너리인지 확인
    if isinstance(conversation, dict):
        # 딕셔너리인 경우 (chatroom_name, members, messages 구조)
        messages = conversation.get("messages", [])
    else:
        # 리스트인 경우 (원본 대화 형식)
        messages = conversation
    
    # 시스템 메시지를 제외한 사용자 메시지 수 계산
    user_message_count = len([msg for msg in messages if isinstance(msg, dict) and msg.get('speaker') != 'system'])
    
    if use_chunking and user_message_count > 15:
        # 대화가 긴 경우(15개 이상의 사용자 메시지) 청크로 분할 처리
        return await split_and_process_conversation(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=final_prompt,
            member_names=member_names,
            id_to_name=id_to_name,
            name_to_id=name_to_id
        )
    
    # 1. 1차 대화 처리
    # conversation에서 members 정보 추출
    members = conversation.get("members", []) if isinstance(conversation, dict) else []
    result = await process_conversation(conversation, input_prompt, callback=None, members=members)
    if not result:
        raise HTTPException(status_code=400, detail="처리 결과가 없습니다.")
        
    # 통화 변환 처리
    converted_result = await preprocess_conversation_results(result)
    
    log_processing_stage("통화 변환 후 결과", converted_result)
    
    # 2차 및 3차 처리 진행
    return await process_secondary_and_final(
        converted_result,
        secondary_prompt,
        final_prompt,
        member_names,
        id_to_name,
        name_to_id
    )

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

async def process_conversation_with_sequential_chain(
    conversation, 
    input_prompt, 
    secondary_prompt, 
    final_prompt,
    member_names,
    id_to_name,
    name_to_id,
    use_chunking=True
):
    """SequentialChain을 사용한 효율적인 대화 처리 (개선된 버전)"""
    
    # 메시지 결합 전처리 추가
    if isinstance(conversation, dict):
        # 딕셔너리 형태인 경우 메시지 결합
        original_messages = conversation.get("messages", [])
        conversation = merge_conversation_dict(conversation)
        merged_messages = conversation.get("messages", [])
    else:
        # 리스트 형태인 경우 직접 메시지 결합
        from utils.message_merger import merge_conversation_messages
        merged_conversation = merge_conversation_messages(conversation)
        
        conversation = merged_conversation
    
    # ChainAIService 인스턴스를 매번 새로 생성 (상태 격리)
    chain_service = ChainAIService()
    
    # conversation이 리스트인지 딕셔너리인지 확인
    if isinstance(conversation, dict):
        messages = conversation.get("messages", [])
    else:
        messages = conversation
    
    # 시스템 메시지를 제외한 사용자 메시지 수 계산
    user_message_count = len([msg for msg in messages if isinstance(msg, dict) and msg.get('speaker') != 'system'])
    
    # 처리 시작 로그
    print(f"🔄 SequentialChain 처리 시작 - 메시지 수: {user_message_count}, 청킹 사용: {use_chunking}")
    
    if use_chunking and user_message_count > 15:
        # 대화가 긴 경우 청크로 분할 처리
        log_processing_stage("SequentialChain 청크 처리 시작", f"총 {user_message_count}개 메시지")
        result = await chain_service.process_chunked_with_sequential_chain(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=final_prompt,
            member_names=member_names,
            id_to_name=id_to_name,
            name_to_id=name_to_id
        )
    else:
        # 짧은 대화는 한 번에 처리
        log_processing_stage("SequentialChain 단일 처리 시작", f"총 {user_message_count}개 메시지")
        result = await chain_service.process_with_sequential_chain(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=final_prompt,
            member_names=member_names,
            id_to_name=id_to_name,
            name_to_id=name_to_id
        )
    
    # 처리 완료 로그
    final_count = len(result.get("final_result", [])) if result else 0
    print(f"✅ SequentialChain 처리 완료 - 최종 결과: {final_count}개 항목")
    
    return result 
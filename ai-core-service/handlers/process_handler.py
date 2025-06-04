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
    process_all_results,
    process_all_results_without_final_prompt
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
        # hint_phrases가 있거나 정상적인 정산 항목인 경우 포함
        hint_phrases = result.get("hint_phrases", [])
        if result.get("item") and result.get("amount"):  # 기본 필드가 있으면 포함
            tablets.append(result)
        else:
            filtered_out_count += 1
            log_processing_stage(f"불완전한 항목 제외", f"item: {result.get('item', 'Unknown')}, speaker: {result.get('speaker', 'Unknown')}")
    
    log_processing_stage("필터링 결과", f"처리 대상: {len(tablets)}개, 제외된 불완전 항목: {filtered_out_count}개")

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
    2차 처리만 수행하고 hint_phrases를 직접 파싱합니다.
    
    Args:
        converted_result (list): 1차 처리된 결과
        secondary_prompt (str): 2차 프롬프트
        final_prompt (str): 사용하지 않음 (하위 호환성을 위해 유지)
        member_names (list): 멤버 이름 목록
        id_to_name (dict): ID에서 이름으로 매핑
        name_to_id (dict): 이름에서 ID로 매핑
        
    Returns:
        dict: 처리 결과
    """
    # 2차 대화 처리 (place 정보 추출)
    items_only = extract_items_only(converted_result)
    secondary_conversation = [
        {'speaker': 'system', 'message_content': "다음은 분석할 항목 목록입니다."},
        {'speaker': 'user', 'message_content': json.dumps(items_only, ensure_ascii=False)}
    ]
    
    secondary_result = await process_summary(secondary_conversation, secondary_prompt, callback=None)
    if not secondary_result:
        raise HTTPException(status_code=400, detail="2차 처리 결과가 없습니다.")
    
    log_processing_stage("2차 처리 결과", secondary_result)
    
    # hint_phrases를 직접 파싱해서 모든 결과를 처리 (final_prompt 사용 안함)
    final_result = process_all_results_without_final_prompt(
        converted_result, 
        secondary_result, 
        member_names, 
        id_to_name, 
        name_to_id
    )
    
    if final_result:
        log_processing_stage("최종 처리 결과", f"객체 갯수: {len(final_result)}")
        log_processing_stage("최종 처리 결과", final_result)
    else:
        raise HTTPException(status_code=400, detail="최종 처리 결과가 없습니다.")

    
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

async def process_conversation_with_simplified_chain(
    conversation, 
    input_prompt, 
    secondary_prompt,
    member_names,
    id_to_name,
    name_to_id,
    use_chunking=True
):
    """단순화된 체인을 사용한 효율적인 대화 처리 (final_prompt 없이 hint_phrases 직접 파싱)"""
    
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
    
    # conversation이 리스트인지 딕셔너리인지 확인
    if isinstance(conversation, dict):
        messages = conversation.get("messages", [])
    else:
        messages = conversation
    
    # 시스템 메시지를 제외한 사용자 메시지 수 계산
    user_message_count = len([msg for msg in messages if isinstance(msg, dict) and msg.get('speaker') != 'system'])
    
    if use_chunking and user_message_count > 15:
        # 대화가 긴 경우 청크로 분할 처리 (process와 같은 방식)
        log_processing_stage("단순화된 체인 청크 처리 시작", f"총 {user_message_count}개 메시지")
        return await split_and_process_conversation(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            final_prompt=None,  # final_prompt는 사용하지 않음 (simplified 방식)
            member_names=member_names,
            id_to_name=id_to_name,
            name_to_id=name_to_id
        )
    else:
        # 짧은 대화는 ChainAIService의 단순화된 처리 사용
        log_processing_stage("단순화된 체인 단일 처리 시작", f"총 {user_message_count}개 메시지")
        
        # ChainAIService 인스턴스를 매번 새로 생성 (상태 격리)
        chain_service = ChainAIService()
        
        result = await chain_service.process_with_simplified_chain(
            conversation=conversation,
            input_prompt=input_prompt,
            secondary_prompt=secondary_prompt,
            member_names=member_names,
            id_to_name=id_to_name,
            name_to_id=name_to_id
        )
    
    return result

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
    
    return result 
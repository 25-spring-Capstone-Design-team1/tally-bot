import yaml
import aiofiles
from typing import Dict, Any

# 캐시 딕셔너리
prompt_cache = {}

async def load_prompt(file_path: str) -> Dict[str, Any]:
    """프롬프트 파일을 비동기로 로드하고 캐시하는 함수"""
    if file_path in prompt_cache:
        return prompt_cache[file_path]
    
    async with aiofiles.open(file_path, 'r', encoding='utf-8') as file:
        content = await file.read()
        prompt = yaml.safe_load(content)
        prompt_cache[file_path] = prompt
        return prompt 
# Tally Bot AI Core Service

## 개요 🌟

이 프로젝트는 대화 내용에서 금액과 통화 정보를 추출하고 원화로 변환하는 FastAPI 기반 AI 엔진 서비스입니다.

## 설치 및 실행 방법 🚀

### 환경 설정

1. Python 3.8 이상이 필요합니다.
2. 가상 환경 생성 및 활성화:
   ```bash
   python -m venv venv
   source venv/bin/activate  # Linux/Mac
   venv\Scripts\activate     # Windows
   ```

3. 의존성 설치:
   ```bash
   pip install -r requirements.txt
   ```

4. 환경 변수 설정 (.env 파일 생성):
   ```
   OPENAI_API_KEY=sk-your-api-key
   LANGSMITH_API_KEY=ls-your-api-key
   ```

### 서버 실행

```bash
uvicorn main:app --reload
```

서버는 기본적으로 http://localhost:8000 에서 실행됩니다.


## API 엔드포인트 📡

### 1. 기본 상태 확인
- **GET /** - 서비스 상태 확인

### 2. 대화 내용 처리
- **POST /api/process** - JSON 형식의 대화 내용을 직접 전송하여 처리
  ```json
  {
    "conversation": [
      {"speaker": "system", "message_content": "members: [준호, 소연, 민우]\nmember_count: 3"},
      {"speaker": "준호", "message_content": "택시비 19유로 냈어요."}
    ],
    "prompt_file": "resources/input_prompt.yaml"
  }
  ```

### 3. 파일에서 대화 내용 처리
- **POST /api/process-file** - 기존 파일에서 대화 내용을 로드하여 처리
  - 매개변수:
    - `conversation_file`: 대화 내용 파일 경로 (기본값: "resources/sample_conversation.json")
    - `prompt_file`: 프롬프트 파일 경로 (기본값: "resources/input_prompt.yaml")

## API 문서

FastAPI는 자동으로 API 문서를 생성합니다:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 프로젝트 구조 🏗️

```text
ai-core-service/
├── load/                 # 프롬프트 및 대화 로딩 모듈
├── resources/            # 프롬프트 및 샘플 대화 파일
├── services/             # AI 서비스 및 설정
├── utils/                # 유틸리티 모듈 (통화 변환 등)
├── main.py               # FastAPI 서버 메인 파일
├── requirements.txt      # 의존성 목록
└── README.md             # 프로젝트 설명
```

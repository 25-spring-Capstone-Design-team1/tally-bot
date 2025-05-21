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
    "chatroom_name": "여행 정산방",
    "members": ["준호", "소연", "유진", "민우", "지훈"],
    "messages": [
      {"speaker": "준호", "message_content": "택시비 19유로 냈어요."}
    ],
    "prompt_file": "resources/input_prompt.yaml",
    "secondary_prompt_file": "resources/secondary_prompt.yaml",
    "final_prompt_file": "resources/final_prompt.yaml"
  }
  ```

### 3. 파일에서 대화 내용 처리
- **POST /api/process-file** - 기존 파일에서 대화 내용을 로드하여 처리
  - 매개변수:
    - `conversation_file`: 대화 내용 파일 경로 (기본값: "resources/sample_conversation.json")
    - `prompt_file`: 1차 프롬프트 파일 경로 (기본값: "resources/input_prompt.yaml")
    - `secondary_prompt_file`: 2차 프롬프트 파일 경로 (기본값: "resources/secondary_prompt.yaml")
    - `final_prompt_file`: 최종 프롬프트 파일 경로 (기본값: "resources/final_prompt.yaml")

## API 문서

FastAPI는 자동으로 API 문서를 생성합니다:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 프로젝트 구조 🏗️

```text
ai-core-service/
├── config/               # 애플리케이션 설정 파일
│   ├── app_config.py     # FastAPI 앱 설정
│   └── service_config.py # AI 서비스 환경 설정
├── handlers/             # API 요청 처리 핸들러
│   └── process_handler.py # 대화 처리 로직 핸들러
├── load/                 # 프롬프트 및 대화 로딩 모듈
├── models/               # 데이터 모델 정의
│   └── conversation.py   # 대화 요청/응답 모델
├── resources/            # 프롬프트 및 샘플 대화 파일
│   ├── final_prompt.yaml # 최종 정산 처리 프롬프트
│   ├── input_prompt.yaml # 초기 항목 추출 프롬프트
│   └── secondary_prompt.yaml # 2차 분석 프롬프트
├── services/             # 핵심 비즈니스 로직 서비스
│   ├── ai_service.py     # AI 모델 호출 서비스
│   └── result_processor.py # 결과 처리 서비스 
├── utils/                # 유틸리티 모듈
│   ├── calculation_helper.py # 계산 로직 유틸리티
│   ├── currency_converter.py # 통화 변환 유틸리티
│   └── logging_utils.py  # 로깅 유틸리티
├── main.py               # FastAPI 서버 메인 파일
├── requirements.txt      # 의존성 목록
└── README.md             # 프로젝트 설명
```

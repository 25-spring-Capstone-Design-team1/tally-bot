# Tally Bot AI Core Service

## 개요 🌟

이 프로젝트는 대화 내용에서 금액과 통화 정보를 추출하고 원화로 변환하는 FastAPI 기반 AI 엔진 서비스입니다. 
**DeepEval**을 통합하여 AI 모델의 성능을 체계적으로 평가하고 모니터링할 수 있습니다.

## 주요 기능 ✨

- 💬 **대화 처리**: 채팅 메시지에서 정산 정보 자동 추출
- 💰 **통화 변환**: 다양한 통화를 원화로 자동 변환
- 📊 **AI 평가**: DeepEval을 통한 모델 성능 평가 및 모니터링
- 🔄 **다중 처리 방식**: 단일/청크/체인/멀티턴 처리 지원
- 📈 **성능 분석**: 정확성, 완성도, 관련성 등 다양한 메트릭 평가

## DeepEval 통합 기능 🎯

### 평가 메트릭
- **정확성 평가**: 정산 항목이 대화에서 정확하게 추출되었는지 평가
- **완성도 평가**: 모든 정산 항목이 빠짐없이 추출되었는지 평가
- **관련성 평가**: 답변이 입력과 얼마나 관련있는지 평가
- **편향성 검사**: AI 응답의 편향성 검사
- **독성 검사**: 부적절한 내용 검사

### 커스텀 메트릭
- **정산 정확도**: 정산 계산의 정확성 평가
- **참여자 식별**: 대화 참여자 식별 정확도 평가

## 설치 및 실행 방법 🚀

### 환경 설정

1. Python 3.8 이상이 필요합니다.
2. 가상 환경 생성 및 활성화:
   ```bash
   python -m venv .venv
   source .venv/bin/activate  # Linux/Mac
   .venv\Scripts\activate     # Windows
   ```

3. 의존성 설치:
   ```bash
   pip install -r requirements.txt
   ```

4. 환경 변수 설정:
   ```
   OPENAI_API_KEY=sk-your-api-key
   LANGSMITH_API_KEY=ls-your-api-key
   DEEPEVAL_API_KEY=your-deepeval-api-key
   ```

### DeepEval 설정

1. DeepEval 로그인:
   ```bash
   deepeval login
   ```

2. API 키 설정 (선택사항):
   ```bash
   deepeval set-api-key YOUR_API_KEY
   ```

### 서버 실행

```bash
# 개발 모드
uvicorn main:app --reload

# 프로덕션 모드
nohup uvicorn main:app --host 0.0.0.0 --port 8000 &
```

### 로그 확인

```bash
tail -f nohup.out
```

## API 엔드포인트 📡

```bash
http://tally-bot-ai-backend-alb-2092930451.ap-northeast-2.elb.amazonaws.com/api/process
```

#### 1. 기본 상태 확인
- **GET /** - 서비스 상태 확인

#### 2. 대화 내용 처리
- **POST /api/process** - JSON 형식의 대화 내용을 직접 전송하여 처리
- **POST /api/process-file** - 파일에서 대화 내용을 로드하여 처리
- **POST /api/process-chain** - 체인 방식으로 대화 처리
- **POST /api/process-multi-turn** - 멀티턴 방식으로 대화 처리

### DeepEval 평가 엔드포인트 🔍

#### 1. 단일 대화 평가
```http
POST /api/evaluate-single
```
```json
{
  "conversation": [
    {"speaker": "준호", "message_content": "택시비 19유로 냈어요."}
  ],
  "actual_output": [
    {"amount": 19, "currency": "EUR", "payer": "준호"}
  ],
  "expected_output": [
    {"amount": 19, "currency": "EUR", "payer": "준호"}
  ],
  "model": "gpt-3.5-turbo"
}
```

#### 2. 처리와 함께 평가
```http
POST /api/evaluate-with-processing
```
```json
{
  "chatroom_name": "여행 정산방",
  "members": [{"0":"지훈", "1":"준호", "2":"소연"}],
  "messages": [
    {"speaker": "준호", "message_content": "택시비 19유로 냈어요."}
  ],
  "expected_output": [
    {"amount": 19, "currency": "EUR", "payer": "준호"}
  ],
  "evaluation_model": "gpt-3.5-turbo"
}
```

#### 3. 데이터셋 평가
```http
POST /api/evaluate-dataset
```
```json
{
  "conversations_data": [
    {
      "conversation": [...],
      "actual_output": [...],
      "expected_output": [...]
    }
  ],
  "model": "gpt-3.5-turbo"
}
```

#### 4. 커스텀 메트릭 평가
```http
POST /api/evaluate-custom-metrics
```
```json
{
  "conversation": [...],
  "actual_output": [...],
  "expected_output": [...],
  "model": "gpt-3.5-turbo",
  "use_settlement_accuracy": true,
  "use_participant_identification": true
}
```

#### 5. 사용 가능한 메트릭 조회
```http
GET /api/evaluation-metrics
```

## 테스트 실행 🧪

### DeepEval 테스트 실행

```bash
# 모든 DeepEval 테스트 실행
pytest test_confident_ai_simple.py -v

# 특정 테스트 실행
pytest test_confident_ai_simple.py::test_json_input_gpt3.5-turbo -v

# 모델 비교 테스트
pytest test_confident_ai_simple.py::test_model_comparison -v

# 비용 최적화 테스트
pytest test_confident_ai_simple.py::test_cost_optimized_evaluation -v
```

### 격리 테스트

```bash
# 프로세스 체인 격리 테스트
pytest test_chain_isolation.py -v

# 일반 격리 테스트
pytest test_isolation.py -v

# 프로세스 체인 테스트
pytest test_process_chain.py -v
```

## 평가 메트릭 상세 📊

### 기본 메트릭

1. **정확성 (Accuracy)**
   - 정산 항목이 대화에서 정확하게 추출되었는지 평가
   - 임계값: 0.8
   - 평가 요소: 금액, 참여자, 정산 유형

2. **완성도 (Completeness)**
   - 모든 정산 항목이 빠짐없이 추출되었는지 평가
   - 임계값: 0.7
   - 평가 요소: 누락 항목, 필수 필드, 중복 검사

3. **관련성 (Answer Relevancy)**
   - 답변이 입력과 얼마나 관련있는지 평가
   - 임계값: 0.7

4. **편향성 (Bias)**
   - AI 응답의 편향성 검사
   - 임계값: 0.5

5. **독성 (Toxicity)**
   - 부적절한 내용 검사
   - 임계값: 0.5

### 커스텀 메트릭

1. **정산 정확도 (Settlement Accuracy)**
   - 정산 계산의 정확성 평가
   - 금액 계산, 환율 적용, 분할 계산 정확도

2. **참여자 식별 (Participant Identification)**
   - 대화 참여자 식별 정확도 평가
   - 이름 매칭, 역할 식별, 참여 범위 확인

## 프로젝트 구조 🏗️

```text
ai-core-service/
├── config/                    # 애플리케이션 설정 파일
│   ├── app_config.py         # FastAPI 앱 설정
│   └── service_config.py     # AI 서비스 환경 설정
├── handlers/                  # API 요청 처리 핸들러
│   └── process_handler.py    # 대화 처리 로직 핸들러
├── load/                     # 프롬프트 및 대화 로딩 모듈
├── models/                   # 데이터 모델 정의
│   └── conversation.py       # 대화 요청/응답 모델
├── resources/                # 프롬프트 및 샘플 대화 파일
│   ├── final_prompt.yaml     # 최종 정산 처리 프롬프트
│   ├── input_prompt.yaml     # 초기 항목 추출 프롬프트
│   └── secondary_prompt.yaml # 2차 분석 프롬프트
├── services/                 # 핵심 비즈니스 로직 서비스
│   ├── ai_service.py         # AI 모델 호출 서비스
│   ├── evaluation_service.py # DeepEval 평가 서비스 ⭐
│   ├── multi_turn_ai_service.py # 멀티턴 AI 서비스
│   └── result_processor.py   # 결과 처리 서비스 
├── utils/                    # 유틸리티 모듈
│   ├── calculation_helper.py # 계산 로직 유틸리티
│   ├── currency_converter.py # 통화 변환 유틸리티
│   └── logging_utils.py      # 로깅 유틸리티
├── .deepeval/                # DeepEval 설정 디렉토리 ⭐
│   ├── .deepeval            # API 키 설정
│   ├── .deepeval-cache.json # 캐시 파일
│   └── .deepeval_telemetry.txt # 텔레메트리 설정
├── test_*.py                 # DeepEval 테스트 파일들 ⭐
├── main.py                   # FastAPI 서버 메인 파일
├── requirements.txt          # 의존성 목록 (deepeval 포함)
└── README.md                 # 프로젝트 설명
```

## 사용 예시 💡

### 1. 기본 대화 처리

```python
import requests

# 대화 처리 요청
response = requests.post("http://localhost:8000/api/process", json={
    "chatroom_name": "여행 정산방",
    "members": [{"0":"지훈", "1":"준호", "2":"소연"}],
    "messages": [
        {"speaker": "준호", "message_content": "택시비 19유로 냈어요."}
    ]
})

result = response.json()
print(result)
```

### 2. 평가와 함께 처리

```python
# 처리와 평가를 동시에 수행
response = requests.post("http://localhost:8000/api/evaluate-with-processing", json={
    "chatroom_name": "여행 정산방",
    "members": [{"0":"지훈", "1":"준호", "2":"소연"}],
    "messages": [
        {"speaker": "준호", "message_content": "택시비 19유로 냈어요."}
    ],
    "expected_output": [
        {"amount": 19, "currency": "EUR", "payer": "준호"}
    ]
})

evaluation_result = response.json()
print(f"처리 결과: {evaluation_result['processing_result']}")
print(f"평가 결과: {evaluation_result['evaluation_result']}")
```

### 3. 커스텀 메트릭으로 평가

```python
# 정산 정확도와 참여자 식별 메트릭 사용
response = requests.post("http://localhost:8000/api/evaluate-custom-metrics", json={
    "conversation": [
        {"speaker": "준호", "message_content": "택시비 19유로 냈어요."}
    ],
    "actual_output": [
        {"amount": 19, "currency": "EUR", "payer": "준호"}
    ],
    "use_settlement_accuracy": True,
    "use_participant_identification": True
})

custom_evaluation = response.json()
print(f"정산 정확도: {custom_evaluation['SettlementAccuracy']['score']}")
print(f"참여자 식별: {custom_evaluation['ParticipantIdentification']['score']}")
```

## 성능 모니터링 📈

### 캐시 상태 확인

```bash
curl http://localhost:8000/api/cache/status
```

### 캐시 초기화

```bash
curl -X POST http://localhost:8000/api/cache/clear
```

### 평가 메트릭 조회

```bash
curl http://localhost:8000/api/evaluation-metrics
```

## 서버 도메인

```bash
http://tally-bot-ai-backend-alb-2092930451.ap-northeast-2.elb.amazonaws.com/api/process
```

서버는 기본적으로 http://localhost:8000 에서 실행됩니다.

## API 문서

FastAPI는 자동으로 API 문서를 생성합니다:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 의존성 📦

주요 의존성:
- `fastapi==0.115.12` - 웹 프레임워크
- `deepeval==1.5.0` - AI 모델 평가 프레임워크 ⭐
- `langchain==0.3.25` - LLM 체인 구성
- `openai==1.79.0` - OpenAI API 클라이언트
- `pytest==8.0.0` - 테스트 프레임워크

## 문제 해결 🔧

### DeepEval 관련 문제

1. **API 키 오류**
   ```bash
   # API 키 재설정
   deepeval set-api-key YOUR_NEW_API_KEY
   ```

2. **로그인 문제**
   ```bash
   # 재로그인
   deepeval logout
   deepeval login
   ```

3. **캐시 문제**
   ```bash
   # 캐시 삭제
   rm -rf .deepeval/.deepeval-cache.json
   ```

### 일반적인 문제

1. **환경 변수 누락**: `.env` 파일에 필요한 API 키가 모두 설정되어 있는지 확인
2. **포트 충돌**: 8000번 포트가 이미 사용 중인 경우 다른 포트 사용
3. **의존성 오류**: `pip install -r requirements.txt`로 모든 의존성 재설치


from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

def create_app() -> FastAPI:
    """FastAPI 앱 생성 및 설정"""
    app = FastAPI(
        title="Tally Bot AI Core Service",
        description="""
        ## 정산 대화 분석 및 처리 API
        
        이 API는 다음 기능을 제공합니다:
        
        ### 🎯 주요 기능
        - **대화 분석**: 정산 관련 대화에서 항목별 금액 추출
        - **다중 처리 방식**: 단일 처리, 체인 처리, 파일 기반 처리
        - **평가 시스템**: 추출 결과의 정확도 평가
        
        ### 💡 사용법
        1. `/api/process` - 실시간 대화 데이터 처리
        2. `/api/process-file` - JSON 파일 기반 처리  
        3. `/api/process-chain` - 최적화된 체인 방식 처리
        4. `/api/evaluate-with-processing` - 결과 평가
        
        ### 📊 지원 기능
        - 한국어 복합 금액 처리 (47만 8천원 → 478000)
        - 외화 처리 (19유로 → 19 EUR)
        - 정산 방식 분류 (n분의1, 금액대납, 고정+n분의1)
        """,
        version="1.0.0",
        docs_url="/docs",  # Swagger UI URL
        redoc_url="/redoc",  # ReDoc URL
        openapi_url="/openapi.json",  # OpenAPI JSON URL
        servers=[
            {
                "url": "http://localhost:8000",
                "description": "Development server"
            },
            {
                "url": "https://api.tallybot.com",
                "description": "Production server"
            }
        ]
    )

    # CORS 설정
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    return app 
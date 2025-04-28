# **App Name**: Tally-bot

## Core Features:

- Settlement Overview: Display settlement summaries with totals and visual representations of payment flows, fetched from the Spring Boot backend.
- Transaction Management: Enable viewing, editing, adding, and deleting individual transaction items within a settlement, updating dynamically via API calls.
- Transfer Visualization: Show a graph of optimized transfers, visually comparing pre- and post-optimization scenarios using Recharts.

## Style Guidelines:

- Primary color: Blue (#4A90E2) to reflect the brand's logo and create a sense of trust.
- Secondary color: Yellow (#F8C81E) to highlight key interactive elements and important information.
- Accent: Teal (#008080) for interactive elements and call-to-action buttons.
- Mobile-first responsive design ensuring usability on various screen sizes.
- Use clear and consistent icons from Material-UI to represent actions and data categories.

## Original User Request:
# TallyBot 웹 프론트엔드 개발 요청

## 프로젝트 개요
TallyBot은 카카오톡 채팅 내역을 분석하여 자동으로 정산을 처리하는 서비스입니다. 채팅방에서 수집된 정산 데이터를 시각적으로 보여주고, 사용자가 필요시 수정할 수 있는 웹 인터페이스가 필요합니다.

## 로고 및 디자인 가이드
- 로고: 파란색(#4A90E2) 배경에 흰색 저울과 노란색(#F8C81E) 삼각형 디자인
- 메인 컬러: 파란색(#4A90E2), 노란색(#F8C81E), 흰색(#FFFFFF)
- 디자인 스타일: 깔끔하고 직관적인 UI, 로고 컬러와 조화로운 디자인

## 기술 스택
- React.js (프론트엔드 프레임워크)
- TypeScript (타입 안정성을 위해)
- 상태 관리: Context API (규모를 고려했을 때 적합)
- UI 라이브러리: Material-UI (커스텀 테마로 로고 컬러 적용)
- 차트 라이브러리: Recharts (송금 흐름 시각화)
- 통신: Axios

## 주요 기능 요구사항
1. 정산 결과 요약 페이지
   - 정산 ID별 요약 정보 표시
   - 참여자별 송금 금액 총계
   - 최소 송금 경로를 시각화한 다이어그램

2. 정산 상세 페이지
   - 모든 정산 항목의 목록 표시 (결제자, 정산 대상자, 금액, 항목 등)
   - 항목별 편집 기능
   - 항목 추가/삭제 기능

3. 송금 최적화 시각화
   - 송금 그래프 시각화 (노드: 사용자, 엣지: 송금 흐름)
   - 정산 전/후 비교 시각화 (최적화 효과 표시)

4. 사용자 상호작용
   - 정산 항목 수정 시 실시간 반영
   - 수정 사항 저장 및 재계산 요청 기능
   - 정산 결과 공유 기능 (URL 복사 등)

## API 통합
백엔드 API는 Spring Boot로 구현되어 있으며, 다음 엔드포인트를 사용할 예정입니다 (백엔드 구현 전):
- GET /api/settlements/{id} - 정산 정보 조회
- PUT /api/settlements/{id} - 정산 정보 수정
- POST /api/settlements/{id}/recalculate - 정산 재계산 요청
- GET /api/settlements/{id}/graph - 송금 그래프 정보 조회

## 중요: 데이터 처리
- 샘플 데이터는 예시일 뿐이며, 실제 구현에서는 백엔드 API를 통해 데이터를 동적으로 불러와야 합니다
- 개발 단계에서는 Mock Service Worker 등을 사용하여 API 응답을 시뮬레이션하세요
- 사용자 이름, 정산 항목 등 모든 데이터는 API로부터 동적으로 가져와 표시해야 합니다
- 백엔드 구현 전에는 로컬 Mock 데이터로 개발 진행

## 디자인 요구사항
- 모바일 친화적인 반응형 디자인 (카카오톡 사용자가 휴대폰으로 접근할 가능성이 높음)
- 간결하고 직관적인 UI/UX
- 로고 컬러와 조화로운 디자인 (파란색 #4A90E2, 노란색 #F8C81E, 흰색 #FFFFFF)
- 각 사용자를 시각적으로 구분할 수 있는 프로필 아이콘 또는 색상 체계

## 샘플 데이터 형식
정산 API 응답 예시 (개발용 참고용):
```json
{
  "settlementId": "12345",
  "title": "여행 정산",
  "createdAt": "2025-04-25T14:30:00",
  "participants": ["다빈", "수빈", "민준", "지은"],
  "payments": [
    {
      "id": 1,
      "payer": "다빈",
      "target": ["다빈", "수빈", "민준"],
      "ratio": [0.33, 0.33, 0.34],
      "amount": 30000,
      "item": "점심"
    },
    {
      "id": 2,
      "payer": "수빈",
      "target": ["다빈", "수빈", "민준", "지은"],
      "ratio": [0.25, 0.25, 0.25, 0.25],
      "amount": 80000,
      "item": "숙소"
    }
  ],
  "optimizedTransfers": [
    {
      "from": "다빈",
      "to": "수빈",
      "amount": 10000
    },
    {
      "from": "민준",
      "to": "수빈",
      "amount": 20000
    },
    {
      "from": "지은",
      "to": "수빈",
      "amount": 20000
    }
  ]
}
```

작업 범위 및 산출물
다음 파일들의 구현을 요청합니다:

프로젝트 구조 설정 (폴더 구조, 기본 설정 파일)
README.md (한글 설명과 시작 가이드 포함)
주요 컴포넌트 코드 (상세 한글 주석 포함)
API 통신 모듈 (백엔드 연동 준비)
상태 관리 구현
라우팅 설정

특히 다음 핵심 컴포넌트의 상세 구현이 필요합니다:

SettlementSummary.tsx - 정산 요약 컴포넌트
SettlementDetail.tsx - 정산 상세 내역 컴포넌트
TransferGraph.tsx - 송금 그래프 시각화 컴포넌트
EditablePaymentItem.tsx - 수정 가능한 정산 항목 컴포넌트

추가 요구사항

백엔드 구현 전이므로 필요한 TO-DO 항목 표시
로컬 개발 환경에서 Mock 데이터를 사용한 테스트 방법 제공
단계별 구현 가이드 (readme.md에 포함)
  
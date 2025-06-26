# Tally Bot Chatbot Backend

## 개요 🌟

이 프로젝트는 사용자의 메신저 채팅 데이터를 분석하여 자동으로 정산 결과를 산출하는 TallyBot의 백엔드 서버입니다.
GPT를 통한 대화 분석, 정산 항목 추출, 정산 대상자 간 금전관계 생성 및 그래프 최적화를 통해 송금 횟수를 최소화한 정산 결과를 제공합니다.

## 주요 기능 ✨
* **📦 그룹 및 멤버 관리** : 채팅방에서 발생하는 메시지를 기반으로 그룹 및 참여 멤버 정보를 자동 등록/관리합니다 (/api/group/create).

* **💬 채팅 업로드** : 대화 내용을 정제된 형식으로 서버에 업로드하여 정산에 활용합니다 (/api/chat/upload).

* **🔍 정산 시작** : 특정 시간 구간의 채팅을 기반으로 GPT 분석을 거쳐 결제 내역을 추출하고, DB에 저장합니다 (/api/calculate/start).

* **📊 정산 결과 조회** 

* **간단 요약** : 누가 누구에게 얼마를 보내야 하는지 요약 (/api/calculate/{id}/brief-result)

* **상세 내역** : 항목별 정산 세부 사항 (/api/calculate/{id}/settlements)

* **송금 리스트** : 최적화된 개인 간 금전 관계 (/api/calculate/{id}/transfers)

* **🔄 정산 수정 및 재계산** : 정산 항목 수정(add/update/delete), 수정 후 그래프 최적화 재수행 (/api/update/settlement, /api/calculate/recalculate)

* **🤖 GPT 연동 처리** : 정산 항목 추출은 별도의 AI Core 서버와 연동하여 수행하며, 응답은 RestTemplate을 통해 처리됩니다.

##설치 및 실행 방법 🚀
### 환경 구성
* Java 17

* Spring Boot 3.x

* MySQL

* AWS RDS + Secrets Manager (DB 비밀번호 보안 관리용)

### 실행 방법
```bash
./gradlew bootRun
```
AWS RDS를 사용할 경우 application.yml 대신 SecretsManagerConfig가 활성화되도록 --spring.profiles.active=rds 설정 필요

### 사용하는 API 엔드포인트 📡
* 그룹 및 멤버 생성
` POST /api/group/create ` : 채팅방 ID 및 사용자 이름을 기반으로 고유 그룹 및 멤버 ID를 생성

* 채팅 업로드
` POST /api/chat/upload ` : 메시지 내용과 발신자 정보를 리스트 형태로 전송 → 채팅 DB에 저장

* 정산 시작
`POST /api/calculate/start` : groupId, startTime, endTime을 전달하여 정산 시작 요청 → 비동기 처리 진행

* 정산 결과 조회
`GET /api/calculate/{id}/brief-result` : 요약 결과

`GET /api/calculate/{id}/settlements`: 항목별 내역

`GET /api/calculate/{id}/transfers`: 송금 대상자 리스트

* 정산 상태 변경
`POST /api/calculate/complete` : 상태를 COMPLETED로 표시

`POST /api/calculate/recalculate` : 정산 내역 변경 시 재계산 수행

* 정산 항목 업데이트
`POST /api/update/settlement` : add, update, delete 기능 지원

## 프로젝트 구조 🏗️

* 📂 controller         // REST API 엔드포인트
* 📂 service            // 핵심 비즈니스 로직 (GPT, 최적화 등)
* 📂 repository         // JPA 기반 데이터 접근
* 📂 domain             // 엔티티 정의
* 📂 config             // Spring 설정, 데이터 초기화
* 📂 debtopt            // 정산 최적화 알고리즘 (그래프 기반)
* 📂 exception          // 전역 예외 핸들러

## 핵심 로직 💡
### 💡 정산 흐름

* 채팅 업로드

* 정산 시작 → GPT 호출 → Settlement 생성

* 참여자 간 금액 계산

* 그래프 기반 정산 최적화 (간선 수 최소화)

* 사용자에게 결과 제공 (프론트 혹은 챗봇)

### 💡 최적화 알고리즘

* 그래프 모델링: 참여자 간 금전 관계를 방향성 그래프로 구성

* 오일러 회로 기반 간소화 (Graph.summarize)

* 무의미한 경로 제거, 동일 가중치 통합 등 전략 적용

## 예시 응답 ✉️
### 정산 요청 시


```json
POST /api/calculate/start
{
  "groupId": 1,
  "startTime": "2025-06-23T00:00:00",
  "endTime": "2025-06-24T23:59:59"
} ```
### 정산 완료 후

```json
{
  "groupUrl": "https://tallybot.vercel.app/1",
  "calculateUrl": "https://tallybot.vercel.app/1/settlements/42",
  "transfers": [
    { "payerId": 1, "payeeId": 2, "amount": 15000 },
    { "payerId": 3, "payeeId": 2, "amount": 22000 }
  ]
}
```

## 의존성 📦
* Spring Boot (Web, JPA, Validation)

* MySQL Driver

* Lombok

* AWS SDK (Secrets Manager)

* Jackson (JSON 직렬화)

* SLF4J (로깅)

## 문제 해결 🔧
### 문제	해결 방법
* DB 연결 실패	SecretsManagerConfig에서 로그 확인
* 정산이 동작하지 않음	calculate/start에 보낸 startTime, endTime 범위 내 채팅이 존재하는지 확인
* GPT 응답 없음	GPT 서버 URL 확인 및 로그 추적
* 정산 결과가 이상함	/calculate/{id}/settlements, /transfers 비교로 검증 가능
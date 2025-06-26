# Tally-Bot

## Architecture

<p align="center">
  <img src="./.drawio/tally-bot.drawio.png" alt="시스템 구성도" width="80%" height="80%">
  <br>
  <em>Tally-Bot 프로젝트 전체 아키텍처 구성도</em>
</p>

## 컴포넌트
이 프로젝트는 다음 3가지 주요 컴포넌트로 구성되어 있습니다. 각 페이지의 리드미 링크에서 해당 컴포넌트에 대한 자세한 설명을 확인할 수 있습니다.

### [AI Core Service](./ai-core-service/README.md)

AI 모델을 활용하여 카카오톡 대화에서 정산 관련 내용을 추출합니다.

### [API Backend](./api-backend/README.md)

애플리케이션의 백엔드 API와 비즈니스 로직을 담당합니다. 직접 개발한 최적 송금횟수 탐색 알고리즘을 통해 최종 정산결과를 도출합니다. 

### [Client Frontend](./client-frontend/README.md)

사용자 인터페이스와 클라이언트 측 로직입니다.

### [Chatbot Frontend](./chatbot-frontend/README.md)

사용자와 직접 소통하는 챗봇 로직입니다.

## 시연 영상
https://www.youtube.com/watch?v=scUuIBDw6cU

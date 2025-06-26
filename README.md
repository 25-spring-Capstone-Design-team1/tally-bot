# Tally-Bot

이 프로젝트는 다음 3가지 주요 컴포넌트로 구성되어 있습니다:

## Architecture

<p align="center">
  <img src="./.drawio/tally-bot.drawio.png" alt="시스템 구성도" width="60%" height="60%">
  <br>
  <em>Tally-Bot 프로젝트 전체 아키텍처 구성도</em>
</p>

## 컴포넌트

### [AI Core Service](./ai-core-service/README.md)

AI 모델을 활용한 추론 엔진과 관련 서비스를 제공합니다.

### [API Backend](./api-backend/README.md)

애플리케이션의 백엔드 API와 비즈니스 로직을 담당합니다.

### [Client Frontend](./client-frontend/README.md)

사용자 인터페이스와 클라이언트 측 로직을 구현합니다.

### [Chatbot Frontend](./chatbot-frontend/README.md)

사용자와 직접 소통하는 챗봇 로직을 구현합니다.

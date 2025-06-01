#!/bin/bash

BASE_URL="http://52.79.167.2:8080"

echo "[1] 그룹 생성 - 지훈"
curl -X POST "$BASE_URL/api/group/create" -H "Content-Type: application/json" -d '{
  "groupId": 2,
  "groupName": "테스트 그룹",
  "member": "지훈"
}'
echo

echo "[2] 그룹 멤버 추가 - 준호"
curl -X POST "$BASE_URL/api/group/create" -H "Content-Type: application/json" -d '{
  "groupId": 2,
  "groupName": "테스트 그룹",
  "member": "준호"
}'
echo

echo "[3] 그룹 멤버 추가 - 민지"
curl -X POST "$BASE_URL/api/group/create" -H "Content-Type: application/json" -d '{
  "groupId": 2,
  "groupName": "테스트 그룹",
  "member": "민지"
}'
echo

echo "[4] 그룹 멤버 추가 - 수빈"
curl -X POST "$BASE_URL/api/group/create" -H "Content-Type: application/json" -d '{
  "groupId": 2,
  "groupName": "테스트 그룹",
  "member": "수빈"
}'
echo

echo "[5] 채팅 업로드"
curl -X POST "$BASE_URL/api/chat/upload" -H "Content-Type: application/json" -d '[
  {
    "groupId": 2,
    "timestamp": "2025-05-30 13:20:00",
    "memberId": 4,
    "message": "정산 ㄱ"
  },
  {
    "groupId": 2,
    "timestamp": "2025-05-30 13:21:00",
    "memberId": 5,
    "message": "내가 낼게"
  }
]'
echo

echo "[6] 정산 시작"
curl -X POST "$BASE_URL/api/calculate/start" -H "Content-Type: application/json" -d '{
  "groupId": 2,
  "startTime": "2025-05-30 13:00:00",
  "endTime": "2025-05-30 14:00:00"
}'
echo

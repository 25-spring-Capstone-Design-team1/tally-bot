// src/components/SettlementSummary.tsx
'use client';

import type { ReactElement } from 'react';
import type { Settlement } from '@/services/settlement'; // Settlement 타입 임포트
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'; // Shadcn 카드 컴포넌트
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'; // Shadcn 아바타 컴포넌트
import { ArrowRight, Users, Landmark } from 'lucide-react'; // 아이콘 임포트
import { cn } from '@/lib/utils'; // 유틸리티 함수 (클래스 이름 병합용)

/**
 * SettlementSummary 컴포넌트의 Props 정의
 */
interface SettlementSummaryProps {
  /** 표시할 정산 데이터 */
  settlement: Settlement;
}

/**
 * 사용자 이름의 첫 글자를 반환하는 함수 (아바타 폴백용)
 * @param name - 사용자 이름
 * @returns 이름의 첫 글자 또는 이름이 없을 경우 '?'
 */
const getInitials = (name: string): string => {
  if (!name) return '?';
  return name.substring(0, 1);
};

/**
 * 문자열을 기반으로 고유한 색상 스타일(배경색, 글자색)을 생성하는 함수.
 * 아바타 배경 등에 사용됩니다.
 * @param str - 색상 생성의 기반이 될 문자열 (예: 사용자 이름)
 * @returns 배경색과 글자색을 포함하는 React CSSProperties 객체
 */
const stringToColor = (str: string): React.CSSProperties => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash); // 간단한 해시 함수
  }
  const hue = hash % 360; // 색상(Hue) 결정
  const saturation = 70; // 채도(Saturation)
  const lightness = 80; // 밝기(Lightness) - 파스텔 톤
  const backgroundColor = `hsl(${hue}, ${saturation}%, ${lightness}%)`;
  const color = `hsl(${hue}, ${saturation - 10}%, ${lightness - 60}%)`; // 글자색은 좀 더 어둡게
  return { backgroundColor, color };
};

/**
 * 정산 요약 정보를 표시하는 컴포넌트.
 * 참여자 목록과 최적화된 최종 송금 내역을 두 개의 카드로 나누어 보여줍니다.
 * @param settlement - 표시할 정산 데이터 (participants, optimizedTransfers 포함)
 */
export default function SettlementSummary({ settlement }: SettlementSummaryProps): ReactElement {
  const { participants, optimizedTransfers } = settlement; // 필요한 데이터 구조 분해 할당

  // (선택 사항) 참여자별 빚/받을 돈 계산 (간단 버전, 시각적 표시용)
  // 실제 정확한 계산은 백엔드 또는 useMemo 등을 활용하여 수행하는 것이 좋습니다.
  const balances: { [key: string]: number } = {};
  participants.forEach(p => balances[p] = 0); // 모든 참여자의 잔액을 0으로 초기화

  optimizedTransfers.forEach(transfer => {
      balances[transfer.from] -= transfer.amount; // 보내는 사람은 (-)
      balances[transfer.to] += transfer.amount;   // 받는 사람은 (+)
  });

  // --- 렌더링 ---
  return (
    // 전체 요약 영역: 반응형 그리드 (기본 1열, 중간 크기 화면부터 2열)
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

      {/* 참여자 목록 카드 */}
      <Card className="shadow-sm rounded-lg">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-lg font-semibold">참여자</CardTitle>
          <Users className="h-5 w-5 text-muted-foreground" /> {/* 사용자 아이콘 */}
        </CardHeader>
        <CardContent>
          {/* 참여자 아바타 및 이름 표시 (가로로 나열, 공간 부족 시 줄 바꿈) */}
          <div className="flex flex-wrap gap-4">
            {participants.map((participant) => (
              <div key={participant} className="flex flex-col items-center gap-1">
                {/* 참여자 아바타 */}
                <Avatar className="h-12 w-12 border-2 border-primary/50">
                  {/* TODO: 실제 사용자 프로필 이미지 URL이 있다면 AvatarImage src에 적용 */}
                  <AvatarImage src={`https://picsum.photos/seed/${participant}/48/48`} alt={participant} />
                  {/* 프로필 이미지 없을 경우 폴백: 이름 이니셜과 고유 색상 배경 */}
                  <AvatarFallback
                    className="font-semibold"
                    style={stringToColor(participant)} // 생성된 색상 스타일 적용
                  >
                    {getInitials(participant)} {/* 이름 이니셜 */}
                  </AvatarFallback>
                </Avatar>
                {/* 참여자 이름 */}
                <span className="text-sm font-medium">{participant}</span>
                {/* (선택 사항) 참여자별 잔액 표시 */}
                {/*
                <span className={cn(
                    "text-xs font-semibold",
                    balances[participant] >= 0 ? "text-green-600" : "text-red-600" // 잔액 부호에 따라 색상 변경
                )}>
                    {balances[participant] >= 0 ? '+' : ''}{balances[participant].toLocaleString()}원
                </span>
                */}
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* 최적화된 최종 송금 내역 카드 */}
      <Card className="shadow-sm rounded-lg">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-lg font-semibold">최종 정산 결과</CardTitle>
          <Landmark className="h-5 w-5 text-muted-foreground" /> {/* 은행/송금 아이콘 */}
        </CardHeader>
         {/* 설명 */}
         <CardDescription className="px-6 pb-2 text-sm text-muted-foreground">
           최소한의 송금으로 정산을 완료하는 방법입니다.
         </CardDescription>
        <CardContent>
          {optimizedTransfers.length > 0 ? ( // 송금 내역이 있을 경우
            <ul className="space-y-3"> {/* 송금 항목 간 간격 */}
              {optimizedTransfers.map((transfer, index) => (
                <li key={index} className="flex items-center justify-between p-3 bg-secondary/50 rounded-md">
                  {/* 보내는 사람 정보 */}
                  <div className="flex items-center gap-2">
                     <Avatar className="h-8 w-8 text-xs border">
                       <AvatarFallback style={stringToColor(transfer.from)}>{getInitials(transfer.from)}</AvatarFallback>
                    </Avatar>
                    <span className="font-medium text-sm">{transfer.from}</span>
                  </div>
                  {/* 송금 금액 및 방향 표시 */}
                  <div className="flex flex-col items-center">
                     <span className="text-sm font-semibold text-primary">{transfer.amount.toLocaleString()}원</span>
                     <ArrowRight className="h-4 w-4 text-muted-foreground" /> {/* 오른쪽 화살표 아이콘 */}
                  </div>
                  {/* 받는 사람 정보 */}
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-sm">{transfer.to}</span>
                    <Avatar className="h-8 w-8 text-xs border">
                      <AvatarFallback style={stringToColor(transfer.to)}>{getInitials(transfer.to)}</AvatarFallback>
                    </Avatar>
                  </div>
                </li>
              ))}
            </ul>
          ) : ( // 송금 내역이 없을 경우
            <p className="text-center text-muted-foreground py-4">정산할 내역이 없습니다.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

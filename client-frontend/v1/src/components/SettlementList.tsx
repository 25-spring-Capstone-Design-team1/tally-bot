// src/components/SettlementList.tsx
'use client';

import type { ReactElement } from 'react';
import Link from 'next/link';
import type { SettlementListItem } from '@/services/settlement'; // 정산 목록 아이템 타입 임포트
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'; // Shadcn 카드 컴포넌트
import { Button } from '@/components/ui/button'; // Shadcn 버튼 컴포넌트
import { Users, Calendar, Coins, ArrowRight, CheckCircle, Lock } from 'lucide-react'; // 아이콘 임포트 (CheckCircle, Lock 추가)
import { format } from 'date-fns'; // 날짜 포맷팅 라이브러리
import { ko } from 'date-fns/locale'; // 한국어 로케일
import { Badge } from '@/components/ui/badge'; // Badge 컴포넌트 임포트
import { cn } from '@/lib/utils'; // cn 유틸리티 임포트

/**
 * SettlementList 컴포넌트의 Props 정의
 */
interface SettlementListProps {
  /** 표시할 정산 목록 데이터 배열 */
  settlements: SettlementListItem[];
}

/**
 * 정산 목록을 카드 형태로 표시하는 컴포넌트.
 * 각 카드는 해당 정산의 상세 페이지로 연결됩니다.
 * 완료된 정산은 상태 배지를 표시합니다.
 * @param settlements - 표시할 정산 목록 아이템 배열
 */
export default function SettlementList({ settlements }: SettlementListProps): ReactElement {

  // 정산 목록이 비어있을 경우 메시지 표시
  if (settlements.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        <p className="text-lg">아직 생성된 정산 내역이 없습니다.</p>
        <p className="mt-2">새로운 정산을 시작해보세요!</p>
         {/* 선택 사항: 새 정산 시작 버튼 추가 가능 */}
      </div>
    );
  }

  // 정산 목록을 그리드 형태로 렌더링
  return (
    // 반응형 그리드 레이아웃: 기본 1열, 중간 크기 화면 2열, 큰 화면 3열, 카드 간 간격 설정
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {settlements.map((settlement) => (
        // 각 정산 항목을 카드로 표시
        <Card
          key={settlement.id} // 각 카드에 고유 키 할당
          // 카드 스타일: flex 컬럼 방향, 내용물 사이 공간 분배, 호버 시 그림자 효과 및 테두리 색 변경, 완료 시 약간 흐리게 처리
          className={cn(
            "flex flex-col justify-between hover:shadow-md transition-all duration-200 ease-in-out rounded-lg border border-border hover:border-primary/40",
            settlement.isCompleted && "opacity-70 bg-muted/30" // 완료 시 스타일
            )}
        >
          {/* 카드 헤더: 제목, 생성일, 완료 배지 */}
          <CardHeader>
              <div className="flex justify-between items-start">
                <CardTitle
                  className="text-xl font-semibold text-primary truncate mr-2" // 제목 스타일: 크기, 굵기, 색상, 긴 텍스트 잘림 처리
                  title={settlement.title} // 마우스 호버 시 전체 제목 표시 (툴팁 효과)
                >
                  {settlement.title}
                </CardTitle>
                {/* 완료 상태 배지 */}
                {settlement.isCompleted && (
                   <Badge variant="secondary" className="text-xs bg-green-100 text-green-800 border-green-300">
                      <CheckCircle className="mr-1 h-3 w-3" />
                      완료됨
                   </Badge>
                )}
              </div>
             <CardDescription className="flex items-center text-sm text-muted-foreground pt-1">
                <Calendar className="mr-1.5 h-4 w-4" /> {/* 날짜 아이콘 */}
                {/* 날짜 포맷팅: 'yyyy년 M월 d일' 형식, 한국어 로케일 적용 */}
                {format(new Date(settlement.createdAt), 'yyyy년 M월 d일', { locale: ko })}
            </CardDescription>
          </CardHeader>
          {/* 카드 본문: 참여자 수, 총 금액 */}
          <CardContent className="flex-grow"> {/* 내용 영역이 남은 공간을 채우도록 설정 */}
             <div className="flex items-center space-x-4 text-sm text-muted-foreground">
                 <div className="flex items-center">
                    <Users className="mr-1.5 h-4 w-4 text-sky-600"/> {/* 참여자 아이콘 */}
                    <span>{settlement.participantCount}명 참여</span>
                 </div>
                 <div className="flex items-center">
                    <Coins className="mr-1.5 h-4 w-4 text-amber-600" /> {/* 금액 아이콘 */}
                    {/* 총 금액: 세 자리마다 콤마 표시 */}
                    <span>총 {settlement.totalAmount.toLocaleString()}원</span>
                </div>
            </div>
          </CardContent>
          {/* 카드 푸터: 상세 보기 버튼 */}
          <CardFooter className="border-t pt-4"> {/* 상단 테두리 및 패딩 */}
            <Link href={`/settlements/${settlement.id}`} legacyBehavior passHref>
              {/* Link 컴포넌트로 상세 페이지(/settlements/[id])로 이동 */}
              <Button variant="outline" size="sm" className="w-full"> {/* 버튼 스타일: 외곽선, 작은 크기, 너비 100% */}
                {settlement.isCompleted ? '내역 확인' : '상세 보기'} {/* 완료 여부에 따라 버튼 텍스트 변경 */}
                <ArrowRight className="ml-2 h-4 w-4" /> {/* 오른쪽 화살표 아이콘 */}
              </Button>
            </Link>
          </CardFooter>
        </Card>
      ))}
    </div>
  );
}

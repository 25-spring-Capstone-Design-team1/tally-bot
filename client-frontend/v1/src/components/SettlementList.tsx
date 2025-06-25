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
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { getSettlement } from '@/services/settlement';

/**
 * SettlementList 컴포넌트의 Props 정의
 */
interface SettlementListProps {
  /** 표시할 정산 목록 데이터 배열 */
  settlements: SettlementListItem[];
}

function useSettlementTitleWithCount(calculateId: number): string {
  const [title, setTitle] = useState('로딩 중...');

  useEffect(() => {
    getSettlement(calculateId.toString())
      .then((data) => {
        const payments = data.payments || [];
        const count = payments.length;
        const item = payments[0]?.item || '제목 없음';
        const label = count > 1 ? `${item} 등 ${count}개` : item;
        setTitle(label);
      })
      .catch(() => setTitle('제목 없음'));
  }, [calculateId]);

  return title;
}

/**
 * 정산 목록을 카드 형태로 표시하는 컴포넌트.
 * 각 카드는 해당 정산의 상세 페이지로 연결됩니다.
 * 완료된 정산은 상태 배지를 표시합니다.
 * @param settlements - 표시할 정산 목록 아이템 배열
 */
export default function SettlementList({ settlements }: SettlementListProps): ReactElement {
  const { groupId } = useParams() as { groupId: string };

  if (settlements.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        <p className="text-lg">아직 생성된 정산 내역이 없습니다.</p>
        <p className="mt-2">새로운 정산을 시작해보세요!</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {settlements.map((settlement) => {
        const title = useSettlementTitleWithCount(settlement.calculateId);

        return (
          <Card
            key={settlement.calculateId}
            className={cn(
              'relative flex flex-col justify-between hover:shadow-md transition-all duration-200 ease-in-out rounded-lg border border-border hover:border-primary/40',
              settlement.status === 'COMPLETED' && 'opacity-70 bg-muted/30'
            )}
          >
            <div className="px-6 pt-6">
              <div className="text-xl font-semibold text-primary">{title || '로딩 중...'}</div>
              <div className="mt-1 text-sm text-muted-foreground">
                {format(new Date(settlement.startTime), 'yyyy/MM/dd HH:mm')} ~{' '}
                {format(new Date(settlement.endTime), 'yyyy/MM/dd HH:mm')}
              </div>
            </div>

            <CardContent className="flex-grow min-h-[40px] relative">
              {settlement.status === 'COMPLETED' && (
                <Badge
                  variant="secondary"
                  className="absolute bottom-2 right-2 text-sm bg-green-100 text-green-800 border-green-300"
                >
                  <CheckCircle className="mr-1 h-3 w-3" />
                  완료됨
                </Badge>
              )}
            </CardContent>

            <CardFooter className="border-t pt-4">
              <Link href={`/${groupId}/settlements/${settlement.calculateId}`} legacyBehavior passHref>
                <Button variant="outline" size="sm" className="w-full">
                  상세 보기
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </Link>
            </CardFooter>
          </Card>
        );
      })}
    </div>
  );
}
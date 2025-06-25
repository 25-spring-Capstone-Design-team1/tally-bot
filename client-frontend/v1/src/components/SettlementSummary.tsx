'use client';

import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import type { Settlement } from '@/services/settlement';
import { getGroupMembers, type GroupMember } from '@/services/settlement';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { ArrowRight, Users, Landmark } from 'lucide-react';
import { cn } from '@/lib/utils';

interface SettlementSummaryProps {
  settlement: Settlement;
  groupId: string;
}

const getInitials = (name: string): string => {
  if (!name) return '?';
  return name.substring(0, 1);
};

const stringToColor = (str: string): React.CSSProperties => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  const hue = hash % 360;
  const saturation = 70;
  const lightness = 80;
  const backgroundColor = `hsl(${hue}, ${saturation}%, ${lightness}%)`;
  const color = `hsl(${hue}, ${saturation - 10}%, ${lightness - 60}%)`;
  return { backgroundColor, color };
};

export default function SettlementSummary({ settlement, groupId }: SettlementSummaryProps): ReactElement {
  const { participants, optimizedTransfers } = settlement;
  const [members, setMembers] = useState<GroupMember[]>([]);

  useEffect(() => {
    getGroupMembers(groupId)
      .then(setMembers)
      .catch((err) => console.error('멤버 목록 불러오기 실패:', err));
  }, [groupId]);

  const getNickname = (id: string) => {
    const member = members.find((m) => m.memberId.toString() === id);
    return member?.nickname || id;
  };

  const balances: { [key: string]: number } = {};
  participants.forEach(p => balances[p] = 0);
  optimizedTransfers.forEach(transfer => {
    balances[transfer.from] -= transfer.amount;
    balances[transfer.to] += transfer.amount;
  });

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {/* 참여자 카드 */}
      <Card className="shadow-sm rounded-lg">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-lg font-semibold">참여자</CardTitle>
          <Users className="h-5 w-5 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-4">
            {participants.map((participant) => {
              const name = getNickname(participant);
              return (
                <div key={participant} className="flex flex-col items-center gap-1">
                  <Avatar className="h-12 w-12 border-2 border-primary/50">
                    <AvatarFallback
                      className="font-semibold"
                      style={stringToColor(name)}
                    >
                      {getInitials(name)}
                    </AvatarFallback>
                  </Avatar>
                  <span className="text-sm font-medium">{name}</span>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* 최종 송금 카드 */}
      <Card className="shadow-sm rounded-lg">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-lg font-semibold">최종 정산 결과</CardTitle>
          <Landmark className="h-5 w-5 text-muted-foreground" />
        </CardHeader>
        <CardDescription className="px-6 pb-2 text-sm text-muted-foreground">
          최소한의 송금으로 정산을 완료하는 방법입니다.
        </CardDescription>
        <CardContent>
          {optimizedTransfers.length > 0 ? (
            <ul className="space-y-3">
              {optimizedTransfers.map((transfer, index) => (
                <li key={index} className="flex items-center justify-between p-3 bg-secondary/50 rounded-md">
                  {/* 보내는 사람 */}
                  <div className="flex items-center gap-2">
                    <Avatar className="h-8 w-8 text-xs border">
                      <AvatarFallback style={stringToColor(getNickname(transfer.from))}>
                        {getInitials(getNickname(transfer.from))}
                      </AvatarFallback>
                    </Avatar>
                    <span className="font-medium text-sm">{getNickname(transfer.from)}</span>
                  </div>
                  {/* 금액 */}
                  <div className="flex flex-col items-center">
                    <span className="text-sm font-semibold text-primary">
                      {transfer.amount.toLocaleString()}원
                    </span>
                    <ArrowRight className="h-4 w-4 text-muted-foreground" />
                  </div>
                  {/* 받는 사람 */}
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-sm">{getNickname(transfer.to)}</span>
                    <Avatar className="h-8 w-8 text-xs border">
                      <AvatarFallback style={stringToColor(getNickname(transfer.to))}>
                        {getInitials(getNickname(transfer.to))}
                      </AvatarFallback>
                    </Avatar>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-center text-muted-foreground py-4">정산할 내역이 없습니다.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
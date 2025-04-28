// src/components/SettlementDetail.tsx
'use client';

import type { ReactElement } from 'react';
import { useState } from 'react';
import type { Payment } from '@/services/settlement';
import EditablePaymentItem from '@/components/EditablePaymentItem';
import { Button } from '@/components/ui/button';
import { PlusCircle, Lock } from 'lucide-react'; // Lock 아이콘 임포트
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area'; // 스크롤 가능한 영역 컴포넌트


/**
 * SettlementDetail 컴포넌트의 Props 정의
 */
interface SettlementDetailProps {
  /** 현재 정산에 포함된 결제 항목 목록 */
  payments: Payment[];
  /** 정산 참여자 전체 목록 (항목 추가/수정 시 필요) */
  participants: string[];
  /** 결제 항목 목록이 변경될 때 호출되는 콜백 함수 (변경된 목록을 부모로 전달) */
  onPaymentsChange: (updatedPayments: Payment[]) => void;
  /** 정산 완료 여부 */
  isCompleted: boolean;
}

/**
 * 정산 상세 내역을 표시하고 관리하는 컴포넌트.
 * 개별 정산 항목(`EditablePaymentItem`)을 리스트 형태로 보여주며,
 * 항목 추가, 수정 시작, 수정 완료, 삭제 기능을 관리합니다.
 * 정산이 완료된 경우 수정/추가/삭제 기능을 비활성화합니다.
 */
export default function SettlementDetail({ payments, participants, onPaymentsChange, isCompleted }: SettlementDetailProps): ReactElement {
  // 현재 수정 중인 항목의 ID를 관리하는 상태. null이면 수정 중인 항목 없음.
  const [editingItemId, setEditingItemId] = useState<number | null>(null);

  /**
   * 새 정산 항목 추가 핸들러.
   * '항목 추가' 버튼 클릭 시 호출됩니다. 정산 완료 시 비활성화됩니다.
   */
  const handleAddPayment = () => {
    if (isCompleted) return; // 완료된 정산이면 추가 불가

    // 새 결제 항목 객체 생성 (기본값 설정)
    const newPayment: Payment = {
      id: Date.now(), // 임시 ID (실제 앱에서는 백엔드에서 생성하거나 UUID 사용 권장)
      payer: participants[0] || '', // 참여자 목록의 첫 번째 사람을 기본 결제자로 설정 (없으면 빈 문자열)
      target: [...participants], // 기본적으로 모든 참여자를 정산 대상으로 설정
      ratio: participants.length > 0 ? participants.map(() => 1 / participants.length) : [], // 균등 분배 비율 기본값
      amount: 0, // 금액 기본값 0
      item: '', // 항목명 기본값 빈 문자열
      imageUrl: '', // 이미지 URL 기본값 빈 문자열
    };
    // 변경된 결제 목록 (새 항목 추가)을 부모 컴포넌트로 전달
    onPaymentsChange([...payments, newPayment]);
    // 새로 추가된 항목을 바로 수정 모드로 전환하여 사용자 입력 유도
    setEditingItemId(newPayment.id);
  };

  /**
   * 정산 항목 수정 완료 핸들러.
   * EditablePaymentItem 컴포넌트에서 '저장' 시 호출됩니다. 정산 완료 시 비활성화됩니다.
   * @param updatedPayment - 수정된 결제 항목 데이터
   */
  const handleUpdatePayment = (updatedPayment: Payment) => {
    if (isCompleted) return; // 완료된 정산이면 수정 불가

    // 기존 결제 목록에서 해당 ID를 가진 항목을 찾아 업데이트
    const updatedPayments = payments.map(p =>
      p.id === updatedPayment.id ? updatedPayment : p
    );
    // 변경된 결제 목록을 부모 컴포넌트로 전달
    onPaymentsChange(updatedPayments);
    // 수정 모드 해제
    setEditingItemId(null);
  };

  /**
   * 정산 항목 삭제 핸들러.
   * EditablePaymentItem 컴포넌트에서 '삭제 확인' 시 호출됩니다. 정산 완료 시 비활성화됩니다.
   * @param paymentId - 삭제할 결제 항목의 ID
   */
  const handleDeletePayment = (paymentId: number) => {
    if (isCompleted) return; // 완료된 정산이면 삭제 불가

    // 해당 ID를 가진 항목을 제외한 새 결제 목록 생성
    const updatedPayments = payments.filter(p => p.id !== paymentId);
    // 변경된 결제 목록을 부모 컴포넌트로 전달
    onPaymentsChange(updatedPayments);
    // 만약 삭제된 항목이 현재 수정 중인 항목이었다면, 수정 모드 해제
    if (editingItemId === paymentId) {
        setEditingItemId(null);
    }
  };

  /**
   * 항목 수정 모드 시작 핸들러.
   * EditablePaymentItem 컴포넌트에서 '수정' 아이콘 또는 항목 클릭 시 호출됩니다. 정산 완료 시 비활성화됩니다.
   * @param paymentId - 수정할 결제 항목의 ID
   */
  const handleEditClick = (paymentId: number) => {
    if (isCompleted) return; // 완료된 정산이면 수정 모드 진입 불가
    setEditingItemId(paymentId); // 수정할 항목의 ID를 상태에 저장
  };

  /**
   * 항목 수정 취소 핸들러.
   * EditablePaymentItem 컴포넌트에서 '취소' 버튼 클릭 시 호출됩니다.
   */
   const handleCancelEdit = () => {
     setEditingItemId(null); // 수정 모드 해제
     // TODO: 필요하다면 수정 전 상태로 되돌리는 로직 추가 (현재는 취소 시 변경사항 유지됨)
   };

  // --- 렌더링 ---
  return (
    <Card className="shadow-sm rounded-lg">
      <CardHeader>
        {/* 헤더 제목 및 항목 추가 버튼 */}
        <div className="flex justify-between items-center">
           <CardTitle className="text-lg font-semibold">상세 정산 내역</CardTitle>
           {!isCompleted ? ( // 완료되지 않았을 때만 항목 추가 버튼 활성화
             <Button onClick={handleAddPayment} size="sm" variant="outline">
               <PlusCircle className="mr-2 h-4 w-4" />
               항목 추가
             </Button>
            ) : ( // 완료되었을 때는 비활성화된 버튼 표시 (또는 숨김 처리)
              <Button size="sm" variant="outline" disabled title="완료된 정산은 수정할 수 없습니다.">
                 <Lock className="mr-2 h-4 w-4" />
                 항목 추가 (잠김)
              </Button>
           )}
        </div>
        {/* 설명 */}
        <CardDescription>
            {isCompleted
                ? "이 정산은 완료되어 수정할 수 없습니다."
                : "각 항목을 클릭하여 수정하거나 삭제할 수 있습니다."}
        </CardDescription>
      </CardHeader>
      <CardContent>
        {/* 결제 항목 리스트 (스크롤 가능) */}
        <ScrollArea className="h-[400px] pr-4"> {/* 높이 고정 및 스크롤 적용 */}
          {payments.length > 0 ? ( // 항목이 있을 경우
            <ul className="space-y-4"> {/* 항목 간 간격 */}
              {payments.map((payment) => (
                <li key={payment.id}> {/* 각 항목을 EditablePaymentItem으로 렌더링 */}
                  <EditablePaymentItem
                    payment={payment}
                    participants={participants}
                    isEditing={editingItemId === payment.id} // 현재 항목이 수정 모드인지 전달
                    onEditClick={handleEditClick} // 수정 시작 핸들러 전달
                    onUpdate={handleUpdatePayment} // 수정 완료 핸들러 전달
                    onDelete={handleDeletePayment} // 삭제 핸들러 전달
                    onCancel={handleCancelEdit} // 수정 취소 핸들러 전달
                    isCompleted={isCompleted} // 완료 상태 전달
                  />
                </li>
              ))}
            </ul>
          ) : ( // 항목이 없을 경우
            <p className="text-center text-muted-foreground py-8">추가된 정산 항목이 없습니다.</p>
          )}
        </ScrollArea>
      </CardContent>
    </Card>
  );
}

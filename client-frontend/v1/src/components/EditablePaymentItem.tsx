// src/components/EditablePaymentItem.tsx
'use client';

import type { ReactElement } from 'react';
import { useState, useEffect } from 'react';
import type { Payment } from '@/services/settlement';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Card, CardContent } from '@/components/ui/card';
import { Edit, Save, XCircle, Trash2, GripVertical, Image as ImageIcon, Lock } from 'lucide-react'; // 아이콘 임포트 (ImageIcon, Lock 추가)
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { cn } from '@/lib/utils';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"


/**
 * EditablePaymentItem 컴포넌트의 Props 정의
 */
interface EditablePaymentItemProps {
  /** 표시하거나 수정할 결제 항목 데이터 */
  payment: Payment;
  /** 정산에 참여하는 모든 사용자 목록 (결제자, 대상자 선택용) */
  participants: string[];
  /** 현재 항목이 수정 모드인지 여부 */
  isEditing: boolean;
  /** '수정' 아이콘 또는 항목 클릭 시 호출되는 함수 */
  onEditClick: (id: number) => void;
  /** 수정 모드에서 '저장' 버튼 클릭 시 호출되는 함수 */
  onUpdate: (updatedPayment: Payment) => void;
  /** '삭제' 버튼 클릭 시 호출되는 함수 */
  onDelete: (id: number) => void;
  /** 수정 모드에서 '취소' 버튼 클릭 시 호출되는 함수 */
  onCancel: () => void;
  /** 정산 완료 여부 */
  isCompleted: boolean;
  getNickname: (id: string) => string; 
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
  const lightness = 85; // 밝기(Lightness) - 파스텔톤
  const backgroundColor = `hsl(${hue}, ${saturation}%, ${lightness}%)`;
  const color = `hsl(${hue}, ${saturation - 10}%, ${lightness - 60}%)`; // 글자색은 좀 더 어둡게
  return { backgroundColor, color };
};

/**
 * 수정 가능한 개별 정산 항목 컴포넌트.
 * 보기 모드와 수정 모드를 전환하며, 항목 수정, 저장, 삭제 기능을 제공합니다.
 * 이미지 표시 및 확대 보기 기능을 포함합니다.
 * 정산 완료 시 수정/삭제 기능을 비활성화합니다.
 */
export default function EditablePaymentItem({
  payment,
  participants,
  isEditing,
  onEditClick,
  onUpdate,
  onDelete,
  onCancel,
  isCompleted, // 완료 상태 prop 추가
  getNickname,
}: EditablePaymentItemProps): ReactElement {
  // 수정 중인 결제 항목의 상태 관리
  const [editedPayment, setEditedPayment] = useState<Payment>(payment);
  // 입력 필드 유효성 검사 에러 상태 관리
  const [errors, setErrors] = useState<{ amount?: string; item?: string; target?: string }>({});
  const splitMethod = 'custom';
  /**
   * isEditing 상태가 변경될 때 editedPayment를 초기화/업데이트하고 에러 상태를 초기화합니다.
   */
  useEffect(() => {
    setEditedPayment(payment); // 부모로부터 받은 payment로 상태 초기화
    if(isEditing) {
      setErrors({}); // 수정 모드 진입 시 에러 초기화
    }
  }, [isEditing, payment]);

  /**
   * 입력 필드 변경 핸들러.
   * @param field - 변경된 필드 이름 (Payment 타입의 키)
   * @param value - 변경된 값
   */
  const handleChange = (field: keyof Payment, value: any) => {
    if (isCompleted) return; // 완료된 정산은 수정 불가
    setEditedPayment(prev => ({ ...prev, [field]: value }));
    // 입력값 변경 시 해당 필드의 에러 초기화
    if (errors[field as keyof typeof errors]) {
        setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  /**
   * 정산 대상자 체크박스 변경 핸들러.
   * @param participant - 변경된 참여자 이름
   * @param checked - 체크 여부
   */
  const handleTargetChange = (participant: string, checked: boolean) => {
    const prevTargets = editedPayment.target;
    const prevConstants = editedPayment.constant || [];
  
    let newTargets: string[] = [];
    let newConstants: number[] = [];
  
    if (checked) {
      // 새로 추가
      newTargets = [...prevTargets, participant];
      newConstants = [...prevConstants, 0]; // 기본값 0
    } else {
      // 체크 해제 → 제거
      const removeIndex = prevTargets.indexOf(participant);
      if (removeIndex !== -1) {
        newTargets = prevTargets.filter((_, i) => i !== removeIndex);
        newConstants = prevConstants.filter((_, i) => i !== removeIndex);
      } else {
        newTargets = [...prevTargets];
        newConstants = [...prevConstants];
      }
    }
  
    const newAmount = newConstants.reduce((sum, val) => sum + val, 0);
  
    setEditedPayment(prev => ({
      ...prev,
      target: newTargets,
      constant: newConstants,
      amount: newAmount,
    }));
  };

   /**
    * 수정된 항목 저장 전 유효성 검사 함수.
    * @returns 유효하면 true, 아니면 false
    */
   const validate = (): boolean => {
       const newErrors: { amount?: string; item?: string; target?: string } = {};
       if (!editedPayment.item || editedPayment.item.trim() === '') {
           newErrors.item = '항목명을 입력해주세요.';
       }
       // 금액 유효성 검사 (숫자이며 0보다 큰지)
       if (isNaN(editedPayment.amount) || editedPayment.amount <= 0) {
           newErrors.amount = '금액은 0보다 큰 숫자여야 합니다.';
       }
       if (editedPayment.target.length === 0) {
           newErrors.target = '정산 대상자를 1명 이상 선택해주세요.';
       }
       setErrors(newErrors); // 에러 상태 업데이트
       return Object.keys(newErrors).length === 0; // 에러 객체가 비어있으면 유효
   };

  /**
   * '저장' 버튼 클릭 핸들러.
   * 유효성 검사 후 부모의 onUpdate 함수를 호출합니다.
   */
  const handleSave = () => {
    if (isCompleted) return;
    if (validate()) {
      const updatedConstant = editedPayment.target.map((_, i) => editedPayment.constant?.[i] ?? 0);
  
      const finalEdited = {
        ...editedPayment,
        constant: updatedConstant,
      };
  
      onUpdate(finalEdited);
    }
  };

  // --- 렌더링 ---
  return (
    <Card
      className={cn(
        "w-full min-w-0",
        "transition-all duration-300 ease-in-out",
        isEditing && !isCompleted ? "bg-secondary/30 shadow-md border-primary" : "hover:shadow-sm",
        !isCompleted && !isEditing && "cursor-pointer"
      )}
      onClick={() => {
        if (!isCompleted && !isEditing) {
          onEditClick(payment.id);
        }
      }}
    >
      <CardContent className="p-4 w-full max-w-full overflow-x-hidden min-w-0">
        {isEditing && !isCompleted ? ( // 수정 모드이고 완료되지 않았을 때만 수정 UI 표시
          // --- 수정 모드 UI ---
          <div className="space-y-4">
            {/* 항목명, 금액, 이미지 URL 입력 필드 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 min-w-0"> 
              {/* 항목명 */}
              <div className="md:col-span-1 min-w-0"> 
                <Label htmlFor={`item-${payment.id}`}>항목명</Label>
                <Input
                  id={`item-${payment.id}`}
                  value={editedPayment.item}
                  onChange={(e) => handleChange('item', e.target.value.slice(0, 30))}
                  maxLength={30}
                  className={cn("w-full", errors.item && "border-destructive")}
                  disabled={isCompleted}
                />
                {errors.item && <p className="text-xs text-destructive mt-1">{errors.item}</p>}
              </div>
              {/* 금액 */}
              <div className="md:col-span-1 min-w-0"> 
                 <Label htmlFor={`amount-${payment.id}`}>금액 (원)</Label>
                 <Input
                  id={`amount-${payment.id}`}
                  step={1000}
                  type="number"
                  value={editedPayment.amount}
                  onChange={(e) => handleChange('amount', parseInt(e.target.value) || 0)}
                  readOnly={splitMethod === 'custom'}
                  className={cn("w-full", errors.item && "border-destructive")}
                  disabled={isCompleted}
                  />
                  {errors.amount && <p className="text-xs text-destructive mt-1">{errors.amount}</p>}
               </div>
            </div>

            {/* 결제자, 정산 대상자 선택 필드 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 min-w-0"> 
              {/* 결제자 */}
              <div>
                <Label htmlFor={`payer-${payment.id}`}>결제자</Label>
                <Select
                  value={editedPayment.payer}
                  onValueChange={(value) => handleChange('payer', value)}
                  disabled={isCompleted}
                >
                  <SelectTrigger id={`payer-${payment.id}`} className="w-full">  
                    <SelectValue placeholder="결제자 선택" />
                  </SelectTrigger>
                  <SelectContent>
                    {participants.map(p => (
                      <SelectItem key={p} value={p}>{getNickname(p)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              {/* 정산 대상자 */}
              <div>
                <Label>정산 대상자</Label>
                <div className={cn(
                  "mt-2 p-3 border rounded-md max-h-32 overflow-y-auto",
                  "space-y-2",
                  errors.target && "border-destructive"
                )}>
                {participants.map((p, index) => {
  const isTarget = editedPayment.target.includes(p);
  const targetIndex = editedPayment.target.indexOf(p);
  const value = editedPayment.constant?.[targetIndex] ?? '';

  return (
    <div key={p} className="flex justify-between items-center">
      <div className="flex items-center gap-2">
        <Checkbox
          id={`target-${payment.id}-${p}`}
          checked={isTarget}
          onCheckedChange={(checked) => handleTargetChange(p, !!checked)}
          disabled={isCompleted}
        />
        <label htmlFor={`target-${payment.id}-${p}`}>
          {getNickname(p)}
        </label>
      </div>

      {/* constant 입력은 대상자일 때만 보여줌 */}
      {isTarget ? (
  <Input
    type="number"
    step={1000}
    value={value}
    onChange={(e) => {
      const raw = e.target.value;
      if (raw.length > 8) return;
      const parsed = parseInt(raw);
      if (isNaN(parsed) || parsed < 0) return;

      const newConstants = [...(editedPayment.constant || [])];
      newConstants[targetIndex] = parsed;

      const total = newConstants.reduce((sum, v) => sum + v, 0);

      setEditedPayment(prev => ({
        ...prev,
        constant: newConstants,
        amount: total,
      }));
    }}
    className="w-28 h-8 text-sm"
    disabled={isCompleted}
  />
) : (
  <Input
    type="number"
    value={0}
    className="w-28 h-8 text-sm text-muted-foreground bg-muted cursor-not-allowed"
    disabled
  />
)}
    </div>
  );
})}

                </div>
                 {errors.target && <p className="text-xs text-destructive mt-1">{errors.target}</p>}
              </div>
              <div></div>
            </div>

             {/* 수정/취소/삭제 버튼 */}
             <div className="flex flex-wrap justify-end gap-2 pt-2">
               <Button onClick={handleSave} size="sm" disabled={isCompleted}>
                 <Save className="mr-1 h-4 w-4" /> 저장
               </Button>
               <Button onClick={onCancel} variant="outline" size="sm">
                 <XCircle className="mr-1 h-4 w-4" /> 취소
               </Button>
                {/* 삭제 확인 다이얼로그 */}
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                     <Button variant="destructive" size="sm" disabled={isCompleted}>
                       <Trash2 className="mr-1 h-4 w-4" /> 삭제
                     </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>정말 삭제하시겠습니까?</AlertDialogTitle>
                      <AlertDialogDescription>
                        이 작업은 되돌릴 수 없습니다. '{payment.item}' 항목이 영구적으로 삭제됩니다.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>취소</AlertDialogCancel>
                      <AlertDialogAction onClick={() => onDelete(payment.id)} className="bg-destructive hover:bg-destructive/90">
                        삭제 확인
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
             </div>
          </div>
        ) : (
          // --- 보기 모드 UI ---
          <div className="flex items-start gap-3">
            <Avatar className="h-10 w-10 flex-shrink-0">
              <AvatarFallback style={stringToColor(getNickname(payment.payer))}>
               {getInitials(getNickname(payment.payer))}
              </AvatarFallback>
            </Avatar>

            {/* 내용 */}
            <div className="flex flex-col">
              <p className="font-semibold text-lg leading-tight truncate">{payment.item}</p>
              <p className="text-sm text-muted-foreground leading-snug">
             {getNickname(payment.payer)} 결제 · {payment.target.length}명 정산
              </p>
              <p className="text-sm text-blue-600 font-medium mt-1 whitespace-nowrap">
                {payment.amount.toLocaleString()}원
             </p>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

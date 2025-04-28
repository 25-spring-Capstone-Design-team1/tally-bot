// src/app/settlements/[id]/page.tsx
'use client';

import type { ReactElement } from 'react';
import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getSettlement, recalculateSettlement, updateSettlement, markSettlementComplete, type Settlement } from '@/services/settlement';
import SettlementSummary from '@/components/SettlementSummary';
import SettlementDetail from '@/components/SettlementDetail';
import TransferGraph from '@/components/TransferGraph';
import { Button } from '@/components/ui/button';
import { useToast } from '@/hooks/use-toast';
import { Loader2, Share2, RefreshCw, Save, CheckCircle, Lock } from 'lucide-react'; // 아이콘 임포트
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"; // Alert 컴포넌트 임포트
import {
  AlertDialog, // 확인 다이얼로그 추가
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
 * 정산 페이지 컴포넌트.
 * 요약, 상세 내역, 송금 그래프 탭을 포함하며, 데이터 로딩, 수정, 재계산, 완료 기능을 제공합니다.
 */
export default function SettlementPage(): ReactElement {
  const params = useParams();
  const router = useRouter();
  const { toast } = useToast();
  const settlementId = params.id as string; // URL에서 정산 ID 추출

  const [settlement, setSettlement] = useState<Settlement | null>(null); // 정산 데이터 상태
  const [loading, setLoading] = useState<boolean>(true); // 로딩 상태
  const [error, setError] = useState<string | null>(null); // 오류 상태
  const [isRecalculating, setIsRecalculating] = useState<boolean>(false); // 재계산 중 상태
  const [isSaving, setIsSaving] = useState<boolean>(false); // 저장 중 상태
  const [isCompleting, setIsCompleting] = useState<boolean>(false); // 완료 처리 중 상태

  /**
   * 정산 데이터를 불러오는 함수.
   * settlementId가 변경될 때마다 호출됩니다.
   */
  const fetchSettlement = useCallback(async () => {
    if (!settlementId) return; // ID가 없으면 중단
    setLoading(true);
    setError(null);
    try {
      // TODO: 백엔드 준비 시 실제 API 호출로 교체
      const data = await getSettlement(settlementId); // API 호출
      setSettlement(data); // 상태 업데이트
    } catch (err) {
      setError('정산 정보를 불러오는 데 실패했습니다.'); // 오류 메시지 설정
      console.error(err);
      toast({ // 사용자에게 오류 알림
        title: "오류",
        description: "정산 정보를 불러오지 못했습니다.",
        variant: "destructive",
      });
    } finally {
      setLoading(false); // 로딩 상태 해제
    }
  }, [settlementId, toast]);

  // 컴포넌트 마운트 시 및 fetchSettlement 함수 변경 시 데이터 로드
  useEffect(() => {
    fetchSettlement();
  }, [fetchSettlement]);

  /**
   * 정산 항목 변경 핸들러.
   * SettlementDetail 컴포넌트에서 호출됩니다.
   * @param updatedPayments - 변경된 결제 항목 목록
   */
  const handlePaymentsChange = (updatedPayments: Settlement['payments']) => {
     if (settlement?.isCompleted) return; // 완료된 정산은 수정 불가
    setSettlement(prev => prev ? { ...prev, payments: updatedPayments } : null);
    // TODO: 변경 사항이 있을 때 저장 버튼 활성화 등의 로직 추가 가능
  };

  /**
   * 정산 정보 저장 핸들러.
   * '저장' 버튼 클릭 시 호출됩니다.
   */
  const handleSave = async () => {
    if (!settlement || settlement.isCompleted) return; // 정산 데이터가 없거나 완료되었으면 중단
    setIsSaving(true); // 저장 중 상태로 변경
    try {
      // TODO: 백엔드 준비 시 실제 API 호출로 교체
      const updatedData = await updateSettlement(settlementId, settlement); // API 호출 (업데이트)
      setSettlement(updatedData); // 업데이트된 데이터로 상태 갱신
      toast({
        title: "성공",
        description: "정산 정보가 저장되었습니다.",
      });
      // 저장 후 자동 재계산 (사용자 경험 향상)
      await handleRecalculate(false); // 저장 후 재계산 시에는 토스트 메시지 표시 안 함
    } catch (err) {
      console.error(err);
      toast({
        title: "오류",
        description: "정산 정보 저장에 실패했습니다.",
        variant: "destructive",
      });
    } finally {
      setIsSaving(false); // 저장 중 상태 해제
    }
  };

  /**
   * 정산 재계산 핸들러.
   * '재계산' 버튼 클릭 시 또는 저장 후 호출됩니다.
   * @param showToast - 재계산 성공/실패 시 토스트 메시지 표시 여부 (기본값 true)
   */
  const handleRecalculate = async (showToast = true) => {
    if (!settlementId || settlement?.isCompleted) return; // ID 없거나 완료되었으면 중단
    setIsRecalculating(true); // 재계산 중 상태로 변경
    try {
      // TODO: 백엔드 준비 시 실제 API 호출로 교체
      const recalculatedData = await recalculateSettlement(settlementId); // API 호출 (재계산)
      setSettlement(recalculatedData); // 재계산된 데이터로 상태 갱신
      if (showToast) {
          toast({
              title: "성공",
              description: "정산이 재계산되었습니다.",
          });
      }
    } catch (err) {
      console.error(err);
       if (showToast) {
           toast({
                title: "오류",
                description: "정산 재계산에 실패했습니다.",
                variant: "destructive",
           });
       }
    } finally {
      setIsRecalculating(false); // 재계산 중 상태 해제
    }
  };

  /**
   * 공유 기능 핸들러.
   * '공유' 버튼 클릭 시 호출됩니다. Mock URL을 클립보드에 복사합니다.
   */
  const handleShare = () => {
    const mockUrl = `https://tally.bot/share/${settlementId}`; // Mock URL 생성
    navigator.clipboard.writeText(mockUrl)
      .then(() => {
        toast({
          title: "성공",
          description: "정산 공유 링크(Mock)가 클립보드에 복사되었습니다.",
        });
      })
      .catch(err => {
        console.error('클립보드 복사 실패:', err);
        toast({
          title: "오류",
          description: "링크 복사에 실패했습니다.",
          variant: "destructive",
        });
      });
  };

  /**
   * 정산 완료 처리 핸들러.
   * '완료' 버튼 클릭 시 호출됩니다.
   */
  const handleCompleteSettlement = async () => {
      if (!settlement || settlement.isCompleted) return; // 데이터 없거나 이미 완료면 중단
      setIsCompleting(true);
      try {
          const completedSettlement = await markSettlementComplete(settlementId);
          setSettlement(completedSettlement); // 상태 업데이트
          toast({
              title: "정산 완료",
              description: "정산이 완료 처리되었습니다. 더 이상 수정할 수 없습니다.",
          });
      } catch (err) {
          console.error('정산 완료 처리 실패:', err);
          toast({
              title: "오류",
              description: "정산 완료 처리에 실패했습니다.",
              variant: "destructive",
          });
      } finally {
          setIsCompleting(false);
      }
  };


  // 로딩 중 UI
  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[calc(100vh-10rem)]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2 text-lg">정산 정보를 불러오는 중...</span>
      </div>
    );
  }

  // 오류 발생 시 UI
  if (error) {
    return (
      <div className="text-center text-destructive mt-10">
        <p>{error}</p>
        <Button onClick={fetchSettlement} variant="outline" className="mt-4">
          다시 시도
        </Button>
      </div>
    );
  }

  // 정산 데이터가 없을 경우 UI
  if (!settlement) {
    return <div className="text-center mt-10">정산 정보를 찾을 수 없습니다.</div>;
  }

  // 총 정산 금액 계산 (UI 표시용)
  const totalAmount = settlement.payments.reduce((sum, payment) => sum + payment.amount, 0);
  const isSettlementCompleted = settlement.isCompleted; // 정산 완료 여부

  // 메인 렌더링
  return (
    <div className="container mx-auto py-8 px-4">
      {/* 상단 카드: 제목, 총액, 생성일, 액션 버튼들 */}
      <Card className="mb-8 shadow-lg rounded-lg">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2 bg-primary/10 rounded-t-lg">
          {/* 정산 정보 */}
          <div>
            <CardTitle className="text-2xl font-bold text-primary flex items-center">
              {settlement.title}
              {isSettlementCompleted && <Lock className="ml-2 h-5 w-5 text-muted-foreground" title="완료된 정산"/>} {/* 완료 시 자물쇠 아이콘 */}
            </CardTitle>
            <p className="text-sm text-muted-foreground">
              총 정산 금액: {totalAmount.toLocaleString()}원 | 생성일: {new Date(settlement.createdAt).toLocaleDateString('ko-KR')}
               {isSettlementCompleted && <span className="ml-2 font-semibold text-green-600">(완료됨)</span>}
            </p>
          </div>
          {/* 액션 버튼 그룹 */}
          <div className="flex gap-2">
             {/* 저장 버튼 (완료되지 않았을 때만 보임) */}
             {!isSettlementCompleted && (
               <Button onClick={handleSave} size="sm" disabled={isSaving || isRecalculating || isCompleting}>
                {isSaving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
                저장
               </Button>
             )}
             {/* 재계산 버튼 (완료되지 않았을 때만 보임) */}
             {!isSettlementCompleted && (
                <Button onClick={() => handleRecalculate()} size="sm" variant="outline" disabled={isRecalculating || isSaving || isCompleting}>
                 {isRecalculating ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <RefreshCw className="mr-2 h-4 w-4" />}
                 재계산
                </Button>
             )}
             {/* 완료 버튼 (완료되지 않았을 때만 보임) */}
             {!isSettlementCompleted && (
                <AlertDialog>
                    <AlertDialogTrigger asChild>
                       <Button size="sm" variant="destructive" disabled={isRecalculating || isSaving || isCompleting}>
                         {isCompleting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <CheckCircle className="mr-2 h-4 w-4" />}
                         정산 완료
                       </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>정산을 완료하시겠습니까?</AlertDialogTitle>
                        <AlertDialogDescription>
                          정산을 완료하면 더 이상 항목을 수정하거나 삭제할 수 없습니다. 이 작업은 되돌릴 수 없습니다.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>취소</AlertDialogCancel>
                        <AlertDialogAction onClick={handleCompleteSettlement} className="bg-destructive hover:bg-destructive/90">
                           완료 확인
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
             )}
             {/* 공유 버튼 (항상 보임) */}
             <Button onClick={handleShare} size="sm" variant="secondary">
              <Share2 className="mr-2 h-4 w-4" />
              공유
            </Button>
          </div>
        </CardHeader>
        {/* 탭 콘텐츠 */}
        <CardContent className="pt-6">
          {/* 완료 알림 (완료되었을 때만 보임) */}
          {isSettlementCompleted && (
              <Alert variant="default" className="mb-6 bg-green-50 border-green-200">
                <CheckCircle className="h-4 w-4 !text-green-600" /> {/* 아이콘 색상 강제 적용 */}
                <AlertTitle className="text-green-800">정산 완료됨</AlertTitle>
                <AlertDescription className="text-green-700">
                  이 정산은 완료되었으므로 더 이상 수정할 수 없습니다.
                </AlertDescription>
              </Alert>
          )}

          <Tabs defaultValue="summary" className="w-full">
            {/* 탭 목록 */}
            <TabsList className="grid w-full grid-cols-1 md:grid-cols-3 mb-6">
              <TabsTrigger value="summary">요약</TabsTrigger>
              <TabsTrigger value="details">상세 내역</TabsTrigger>
              <TabsTrigger value="graph">송금 그래프</TabsTrigger>
            </TabsList>

            {/* 요약 탭 */}
            <TabsContent value="summary">
              <SettlementSummary settlement={settlement} />
            </TabsContent>

            {/* 상세 내역 탭 */}
            <TabsContent value="details">
              <SettlementDetail
                payments={settlement.payments}
                participants={settlement.participants}
                onPaymentsChange={handlePaymentsChange} // 변경 사항을 부모로 전달
                isCompleted={isSettlementCompleted} // 완료 상태 전달
              />
            </TabsContent>

            {/* 송금 그래프 탭 */}
            <TabsContent value="graph">
              <TransferGraph settlementId={settlementId} />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}

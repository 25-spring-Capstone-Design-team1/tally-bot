'use client';

import { useEffect, useState } from 'react';
import { getSettlementsList, type SettlementListItem } from '@/services/settlement';
import SettlementList from '@/components/SettlementList';
import { Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';


export default function Home() {
  const [settlements, setSettlements] = useState<SettlementListItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSettlements = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getSettlementsList();
      setSettlements(data);
    } catch (err) {
      setError('정산 목록을 불러오는 데 실패했습니다.');
      console.error(err);
      // Consider adding toast notification here
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSettlements();
  }, []);

  return (
    <div className="container mx-auto py-8 px-4">
       <Card className="mb-8 shadow-lg rounded-lg border-primary/20">
         <CardHeader>
           <CardTitle className="text-2xl font-bold text-primary">TallyBot 정산 현황</CardTitle>
           <CardDescription>진행 중인 정산 내역을 확인하고 관리하세요.</CardDescription>
         </CardHeader>
         <CardContent>
           {loading && (
             <div className="flex justify-center items-center min-h-[200px]">
               <Loader2 className="h-8 w-8 animate-spin text-primary" />
               <span className="ml-2 text-lg text-muted-foreground">정산 목록 로딩 중...</span>
             </div>
           )}
           {error && (
             <div className="text-center text-destructive mt-10">
               <p>{error}</p>
               <Button onClick={fetchSettlements} variant="outline" className="mt-4">
                 다시 시도
               </Button>
             </div>
           )}
           {!loading && !error && (
             <SettlementList settlements={settlements} />
           )}
         </CardContent>
       </Card>

        {/* Optionally add a button to create a new settlement */}
        {/* <div className="mt-8 text-center">
            <Button size="lg">
                <PlusCircle className="mr-2 h-5 w-5" />
                새 정산 시작하기 (카카오톡 분석)
            </Button>
        </div> */}
    </div>
  );
}

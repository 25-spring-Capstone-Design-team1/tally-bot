// src/components/TransferGraph.tsx
'use client';

import type { ReactElement } from 'react';
import { useState, useEffect, useCallback } from 'react';
// Recharts 라이브러리에서 필요한 컴포넌트 임포트
import { ResponsiveContainer, Sankey, Tooltip, Layer, Rectangle } from 'recharts';
// 서비스 함수 및 타입 임포트
import { getTransferGraph, type TransferGraph as GraphData } from '@/services/settlement';
// Shadcn UI 컴포넌트 임포트
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Loader2 } from 'lucide-react'; // 로딩 아이콘
import { useToast } from '@/hooks/use-toast'; // 토스트 훅

/**
 * TransferGraph 컴포넌트의 Props 정의
 */
interface TransferGraphProps {
  /** 그래프 데이터를 조회할 정산 ID */
  settlementId: string;
}

// (참고용) 사용자 이름 기반 색상 생성 함수 (현재는 고정 팔레트 사용)
// const nameToColor = (name: string) => {
//     let hash = 0;
//     for (let i = 0; i < name.length; i++) {
//         hash = name.charCodeAt(i) + ((hash << 5) - hash);
//     }
//     const hue = hash % 360;
//     // 파란색(#4A90E2)과 노란색(#F8C81E) 계열 색상 위주로 생성
//     if (hue < 180) { // 파란색 계열
//         return `hsl(${200 + (hue % 40)}, 70%, ${60 + (hash % 20)}%)`;
//     } else { // 노란색/주황색 계열
//         return `hsl(${40 + (hue % 30)}, 90%, ${55 + (hash % 15)}%)`;
//     }
// };

/** 고정된 색상 팔레트 (디자인 가이드라인 준수 및 일관성 유지) */
const colorPalette = [
    '#4A90E2', // 메인 파랑
    '#F8C81E', // 메인 노랑
    '#87CEEB', // 하늘색 (파랑 계열)
    '#FFD700', // 골드 (노랑 계열)
    '#ADD8E6', // 밝은 파랑
    '#F4A460', // 샌디 브라운 (노랑/주황 계열)
    '#B0E0E6', // 파우더 블루
    '#FFEEAA', // 밝은 노랑
];

/**
 * 노드(참여자)의 색상을 결정하는 함수.
 * 고정된 팔레트에서 순환적으로 색상을 선택합니다.
 * @param name - 사용자 이름 (현재 사용 안 함)
 * @param index - 노드의 인덱스
 * @returns 노드에 적용할 색상 문자열 (HSL 또는 HEX)
 */
const getNodeColor = (name: string, index: number) => {
    return colorPalette[index % colorPalette.length]; // 팔레트 내에서 순환
};


/**
 * Sankey 차트의 노드를 커스텀 렌더링하는 컴포넌트.
 * 노드 사각형과 참여자 이름을 표시합니다.
 * @param props - Recharts에서 전달하는 노드 관련 데이터 및 위치 정보
 */
const SankeyNode = ({ x, y, width, height, index, payload, containerWidth }: any): ReactElement => {
  // 노드가 차트의 오른쪽에 위치하는지 여부 확인 (텍스트 정렬 기준 결정)
  const isOut = x + width / 2 > containerWidth / 2;
  // 노드 색상 결정
  const nodeColor = getNodeColor(payload.name, index); // payload.name 대신 index 사용

  return (
    <Layer key={`CustomNode${index}`}>
      {/* 노드 사각형 */}
      <Rectangle
        x={x}
        y={y}
        width={width}
        height={height}
        fill={nodeColor} // 노드 색상 적용
        fillOpacity="1" // 완전 불투명
        rx={3} // 모서리 둥글게
        ry={3}
      />
      {/* 참여자 이름 텍스트 */}
      <text
        textAnchor={isOut ? 'end' : 'start'} // 노드 위치에 따라 텍스트 정렬 방향 설정
        x={isOut ? x - 6 : x + width + 6} // 노드 옆에 약간의 간격을 두고 텍스트 위치 설정
        y={y + height / 2} // 노드의 세로 중앙에 텍스트 위치
        fontSize="14" // 텍스트 크기
        stroke="#333" // 텍스트 색상 (어두운 회색)
        dominantBaseline="middle" // 세로 정렬 기준
      >
        {payload.name} {/* 참여자 이름 표시 */}
      </text>
      {/* 노드 값(총 유입/유출량) 표시는 Sankey 차트 특성상 복잡하여 생략. Tooltip 활용. */}
    </Layer>
  );
};


/**
 * 송금 흐름을 Sankey 다이어그램으로 시각화하는 컴포넌트.
 * API를 통해 정산 ID에 해당하는 최적화된 송금 데이터를 받아와 그래프를 그립니다.
 * @param settlementId - 그래프 데이터를 조회할 정산 ID
 */
export default function TransferGraph({ settlementId }: TransferGraphProps): ReactElement {
  // 상태 관리: 그래프 데이터, 로딩 상태, 에러 상태
  const [graphData, setGraphData] = useState<GraphData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { toast } = useToast(); // 토스트 알림 훅

  /**
   * 정산 ID를 기반으로 그래프 데이터를 비동기적으로 불러오는 함수.
   * settlementId가 변경될 때마다 호출됩니다.
   */
  const fetchGraphData = useCallback(async () => {
    if (!settlementId) return; // ID 없으면 중단
    setLoading(true);
    setError(null);
    try {
      // TODO: 백엔드 준비 시 실제 API 호출로 교체
      const data = await getTransferGraph(settlementId); // API 호출
      setGraphData(data); // 상태 업데이트
    } catch (err) {
      setError('송금 그래프 정보를 불러오는 데 실패했습니다.'); // 에러 메시지 설정
      console.error(err);
       toast({ // 사용자에게 오류 알림
         title: "오류",
         description: "송금 그래프 정보를 불러오지 못했습니다.",
         variant: "destructive",
       });
    } finally {
      setLoading(false); // 로딩 상태 해제
    }
  }, [settlementId, toast]); // 의존성 배열에 settlementId와 toast 추가

  // 컴포넌트 마운트 시 및 fetchGraphData 함수 변경 시 데이터 로드
  useEffect(() => {
    fetchGraphData();
  }, [fetchGraphData]);


  // 로딩 중 UI
  if (loading) {
    return (
      <Card className="h-[400px] flex justify-center items-center shadow-sm rounded-lg">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2 text-lg">그래프 로딩 중...</span>
      </Card>
    );
  }

  // 에러 발생 또는 데이터 없음 UI
  if (error || !graphData || graphData.nodes.length === 0) {
    return (
      <Card className="h-[400px] flex justify-center items-center shadow-sm rounded-lg">
        <p className="text-muted-foreground">{error || '표시할 송금 데이터가 없습니다.'}</p>
      </Card>
    );
  }

  // API 응답 데이터를 Recharts Sankey 형식으로 변환
   const sankeyNodes = graphData.nodes.map((node, index) => ({
       name: node.id, // 노드 이름 (참여자 ID)
       color: getNodeColor(node.id, index) // 각 노드에 색상 할당
   }));

  const sankeyLinks = graphData.edges.map(edge => ({
    source: graphData.nodes.findIndex(node => node.id === edge.source), // source 노드의 인덱스 찾기
    target: graphData.nodes.findIndex(node => node.id === edge.target), // target 노드의 인덱스 찾기
    value: edge.amount, // 링크(송금) 금액
  }));

  // Recharts Sankey 컴포넌트에 전달할 최종 데이터 객체
  const data = {
    nodes: sankeyNodes,
    links: sankeyLinks,
  };

  // --- 메인 렌더링 ---
  return (
    <Card className="shadow-sm rounded-lg">
      <CardHeader>
        <CardTitle className="text-lg font-semibold">송금 흐름 시각화</CardTitle>
        <CardDescription>최적화된 최종 송금 경로입니다.</CardDescription>
      </CardHeader>
      <CardContent>
        {/* 반응형 컨테이너: 너비 100%, 높이 400px */}
        <ResponsiveContainer width="100%" height={400}>
           {/* Sankey 차트 컴포넌트 */}
           <Sankey
             data={data} // 변환된 데이터 전달
             node={<SankeyNode />} // 커스텀 노드 컴포넌트 사용
             nodePadding={50} // 노드 간의 수직 간격
             margin={{ top: 5, right: 50, bottom: 5, left: 50 }} // 차트 여백
             link={{ stroke: '#B0C4DE', strokeOpacity: 0.5 }} // 링크(송금 흐름) 스타일: 밝은 파랑, 반투명
           >
             {/* 툴팁 컴포넌트: 마우스 호버 시 상세 정보 표시 */}
             <Tooltip
                contentStyle={{ backgroundColor: 'rgba(255, 255, 255, 0.9)', borderRadius: '4px', border: '1px solid #ccc' }} // 툴팁 스타일
                formatter={(value: any, name: any, props: any) => { // 툴팁 내용 포맷터
                    // 링크(엣지)에 대한 툴팁 내용 커스터마이징
                    if (props.payload && props.payload.source && props.payload.target) {
                       const sourceName = props.payload.source.name; // 보내는 사람 이름
                       const targetName = props.payload.target.name; // 받는 사람 이름
                       // 배열 형태로 반환: [툴팁 제목, 툴팁 값]
                       return [`${sourceName} → ${targetName}`, `${Number(value).toLocaleString()}원`];
                    }
                    // 노드 등 다른 요소에 대한 기본 툴팁 동작
                    return [value, name];
                 }}
             />
           </Sankey>
         </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}

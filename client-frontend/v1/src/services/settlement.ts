// src/services/settlement.ts

/**
 * 정산 내 개별 결제 항목을 나타냅니다.
 */
export interface Payment {
  /** 결제의 고유 식별자 */
  id: number;
  /** 결제한 사람 */
  payer: string;
  /** 정산 대상자 목록 */
  target: string[];
  /** 각 대상자에 대한 결제 비율 (길이는 target과 같아야 함) */
  ratio: number[];
  /** 결제 금액 */
  amount: number;
  /** 결제 항목명 (예: "점심 식사", "택시비") */
  item: string;
  /** 결제 관련 이미지 URL (선택 사항) */
  imageUrl?: string;
}

/**
 * 정산 결과 최적화된 송금 단위를 나타냅니다.
 */
export interface OptimizedTransfer {
  /** 돈을 보내는 사람 */
  from: string;
  /** 돈을 받는 사람 */
  to: string;
  /** 송금할 금액 */
  amount: number;
}

/**
 * 하나의 완전한 정산 정보를 나타냅니다.
 */
export interface Settlement {
  /** 정산의 고유 식별자 */
  settlementId: string;
  /** 정산 제목 */
  title: string;
  /** 정산 생성 타임스탬프 (ISO 8601 형식 문자열) */
  createdAt: string;
  /** 정산 참여자 목록 */
  participants: string[];
  /** 정산에 포함된 모든 결제 항목 목록 */
  payments: Payment[];
  /** 계산된 최적화된 송금 목록 */
  optimizedTransfers: OptimizedTransfer[];
  /** 정산 완료 여부 */
  isCompleted: boolean;
}

/**
 * 정산 목록 페이지에 표시될 각 정산의 요약 정보를 나타냅니다.
 */
export interface SettlementListItem {
    /** 정산의 고유 식별자 */
    id: string;
    /** 정산 제목 */
    title: string;
    /** 정산 생성 타임스탬프 (ISO 8601 형식 문자열) */
    createdAt: string;
     /** 총 참여자 수 */
    participantCount: number;
     /** 정산 총 금액 */
    totalAmount: number;
    /** 정산 완료 여부 */
    isCompleted: boolean;
}


/**
 * 정산 목록 요약 정보를 비동기적으로 가져옵니다.
 *
 * @returns SettlementListItem 객체 배열을 포함하는 Promise.
 */
export async function getSettlementsList(): Promise<SettlementListItem[]> {
    // TODO: 백엔드 API 호출 구현 필요
    // API 지연 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 500));

    // 대학생 사용 시나리오에 맞는 Mock 데이터 반환
    return [
        {
            id: 'festival-eve-1',
            title: '대학교 축제 전야제 준비',
            createdAt: '2024-05-15T14:00:00Z',
            participantCount: 10,
            totalAmount: 250000,
            isCompleted: false,
        },
        {
            id: 'student-council-party-2',
            title: '학생회 총회 후 뒤풀이',
            createdAt: '2024-03-10T22:00:00Z',
            participantCount: 25,
            totalAmount: 380000,
            isCompleted: true, // 완료된 정산 예시
        },
         {
            id: 'dinner-mt-3',
            title: '개강 MT 저녁 식사',
            createdAt: '2024-03-02T19:30:00Z',
            participantCount: 12,
            totalAmount: 180000,
            isCompleted: false,
        },
        {
            id: 'taxi-late-night-4',
            title: '시험 기간 밤샘 후 택시비',
            createdAt: '2024-06-12T04:00:00Z',
            participantCount: 4,
            totalAmount: 28000,
            isCompleted: false,
        },
         {
            id: 'summer-trip-5',
            title: '여름 방학 가평 여행',
            createdAt: '2024-07-20T11:00:00Z',
            participantCount: 6,
            totalAmount: 420000,
            isCompleted: false,
        },
        {
            id: 'study-group-snacks-6',
            title: '팀플 스터디 간식비',
            createdAt: '2024-05-28T16:00:00Z',
            participantCount: 5,
            totalAmount: 45000,
            isCompleted: true, // 완료된 정산 예시
        },
        {
            id: 'home-abc', // 추가된 케이스
            title: '집 관련 정산 (Test)',
            createdAt: '2024-08-01T10:00:00Z',
            participantCount: 3,
            totalAmount: 150000,
            isCompleted: false,
        },
    ];
}

// Mock 데이터 저장을 위한 임시 저장소 (실제 앱에서는 DB 사용)
const mockSettlementStore: { [key: string]: Settlement } = {};

/**
 * 주어진 ID에 해당하는 상세 정산 정보를 비동기적으로 가져옵니다.
 * Mock 저장소에 데이터가 없으면 생성하여 저장합니다.
 *
 * @param id - 조회할 정산의 ID.
 * @returns 정산 상세 정보를 포함하는 Settlement 객체의 Promise.
 */
export async function getSettlement(id: string): Promise<Settlement> {
    // TODO: 백엔드 API 호출 구현 필요
    // API 지연 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 300));

    // Mock 저장소에 데이터가 있으면 반환
    if (mockSettlementStore[id]) {
        return mockSettlementStore[id];
    }

    // 저장소에 없으면 Mock 데이터 생성
    let settlementData: Settlement;
    switch (id) {
        case 'festival-eve-1':
            settlementData = {
                settlementId: 'festival-eve-1',
                title: '대학교 축제 전야제 준비',
                createdAt: '2024-05-15T14:00:00Z',
                participants: ['지민', '현수', '수빈', '민지', '태영', '준호', '예은', '승민', '다현', '성진'],
                payments: [
                    { id: 1, payer: '지민', target: ['지민', '현수', '수빈', '민지', '태영', '준호', '예은', '승민', '다현', '성진'], ratio: Array(10).fill(1/10), amount: 80000, item: '주류 및 음료 구매', imageUrl: 'https://picsum.photos/seed/drinks/200/100' },
                    { id: 2, payer: '현수', target: ['지민', '현수', '수빈', '민지', '태영', '준호', '예은', '승민', '다현', '성진'], ratio: Array(10).fill(1/10), amount: 120000, item: '안주 및 간식 구매 (마트)', imageUrl: 'https://picsum.photos/seed/snacks/200/100' },
                    { id: 3, payer: '수빈', target: ['지민', '현수', '수빈', '민지', '태영', '준호', '예은', '승민', '다현', '성진'], ratio: Array(10).fill(1/10), amount: 50000, item: '장식 및 부스 용품' },
                ],
                optimizedTransfers: [], // 초기엔 비어있음, 재계산 필요
                isCompleted: false,
            };
            break; // switch 문 빠져나가기
        case 'student-council-party-2':
            settlementData = {
                settlementId: 'student-council-party-2',
                title: '학생회 총회 후 뒤풀이',
                createdAt: '2024-03-10T22:00:00Z',
                participants: Array.from({ length: 25 }, (_, i) => `학생${i + 1}`), // 25명 학생
                payments: [
                    { id: 101, payer: '학생1', target: Array.from({ length: 25 }, (_, i) => `학생${i + 1}`), ratio: Array(25).fill(1/25), amount: 200000, item: '1차 술집' },
                    { id: 102, payer: '학생5', target: Array.from({ length: 25 }, (_, i) => `학생${i + 1}`), ratio: Array(25).fill(1/25), amount: 180000, item: '2차 노래방', imageUrl: 'https://picsum.photos/seed/karaoke/200/100' },
                ],
                 optimizedTransfers: [],
                isCompleted: true, // 완료된 정산
            };
             break;
      case 'dinner-mt-3':
           settlementData = {
               settlementId: 'dinner-mt-3',
               title: '개강 MT 저녁 식사',
               createdAt: '2024-03-02T19:30:00Z',
               participants: ['철수', '영희', '민수', '지현', '상훈', '보람', '동준', '혜진', '기범', '나리', '승현', '유나'],
               payments: [
                   { id: 201, payer: '철수', target: ['철수', '영희', '민수', '지현', '상훈', '보람', '동준', '혜진', '기범', '나리', '승현', '유나'], ratio: Array(12).fill(1/12), amount: 110000, item: '삼겹살 및 목살', imageUrl: 'https://picsum.photos/seed/bbq/200/100' },
                   { id: 202, payer: '영희', target: ['철수', '영희', '민수', '지현', '상훈', '보람', '동준', '혜진', '기범', '나리', '승현', '유나'], ratio: Array(12).fill(1/12), amount: 70000, item: '주류, 음료, 밥, 찌개' },
               ],
                optimizedTransfers: [],
               isCompleted: false,
           };
            break;
      case 'taxi-late-night-4':
           settlementData = {
               settlementId: 'taxi-late-night-4',
               title: '시험 기간 밤샘 후 택시비',
               createdAt: '2024-06-12T04:00:00Z',
               participants: ['진우', '서아', '건', '하윤'],
               payments: [
                   { id: 301, payer: '진우', target: ['진우', '서아', '건', '하윤'], ratio: [0.25, 0.25, 0.25, 0.25], amount: 28000, item: '집 가는 택시', imageUrl: 'https://picsum.photos/seed/taxi/200/100' },
               ],
                optimizedTransfers: [],
               isCompleted: false,
           };
            break;
      case 'summer-trip-5':
           settlementData = {
               settlementId: 'summer-trip-5',
               title: '여름 방학 가평 여행',
               createdAt: '2024-07-20T11:00:00Z',
               participants: ['재민', '소영', '민규', '지수', '태현', '유정'],
               payments: [
                   { id: 401, payer: '재민', target: ['재민', '소영', '민규', '지수', '태현', '유정'], ratio: Array(6).fill(1/6), amount: 180000, item: '펜션 예약 (1박)' },
                   { id: 402, payer: '소영', target: ['재민', '소영', '민규', '지수', '태현', '유정'], ratio: Array(6).fill(1/6), amount: 100000, item: '바베큐 장보기', imageUrl: 'https://picsum.photos/seed/grocery/200/100' },
                   { id: 403, payer: '민규', target: ['재민', '소영', '민규', '지수', '태현', '유정'], ratio: Array(6).fill(1/6), amount: 90000, item: '수상 레저 이용료', imageUrl: 'https://picsum.photos/seed/leisure/200/100' },
                   { id: 404, payer: '지수', target: ['재민', '소영', '민규', '지수', '태현', '유정'], ratio: Array(6).fill(1/6), amount: 50000, item: '교통비 (주유비)' },
               ],
                optimizedTransfers: [],
               isCompleted: false,
           };
            break;
       case 'study-group-snacks-6':
            settlementData = {
                settlementId: 'study-group-snacks-6',
                title: '팀플 스터디 간식비',
                createdAt: '2024-05-28T16:00:00Z',
                participants: ['하늘', '보검', '지은', '민석', '서현'],
                payments: [
                    { id: 501, payer: '하늘', target: ['하늘', '보검', '지은', '민석', '서현'], ratio: Array(5).fill(1/5), amount: 25000, item: '커피 및 음료', imageUrl: 'https://picsum.photos/seed/coffee/200/100' },
                    { id: 502, payer: '보검', target: ['하늘', '보검', '지은', '민석', '서현'], ratio: Array(5).fill(1/5), amount: 20000, item: '샌드위치 및 빵' },
                ],
                 optimizedTransfers: [],
                isCompleted: true, // 완료된 정산
            };
             break;
        case 'home-abc': // 'home-abc' ID에 대한 Mock 데이터 추가
             settlementData = {
                 settlementId: 'home-abc',
                 title: '집 관련 정산 (Test)',
                 createdAt: '2024-08-01T10:00:00Z',
                 participants: ['민지', '철수', '영희'],
                 payments: [
                     { id: 601, payer: '민지', target: ['민지', '철수', '영희'], ratio: [1/3, 1/3, 1/3], amount: 90000, item: '월세', imageUrl: 'https://picsum.photos/seed/rent/200/100' },
                     { id: 602, payer: '철수', target: ['민지', '철수', '영희'], ratio: [1/3, 1/3, 1/3], amount: 60000, item: '관리비 및 공과금' },
                 ],
                 optimizedTransfers: [],
                 isCompleted: false,
             };
             break;
      default:
          // 여전히 해당하는 ID가 없을 경우 에러 발생 (안전 장치)
          // 또는 기본값 반환 등의 처리 가능
          console.warn(`ID ${id}에 대한 Mock 데이터가 정의되지 않았습니다. 기본값을 사용하거나 에러를 발생시킵니다.`);
          // 여기서는 에러를 발생시키는 대신, 빈 정산 데이터를 반환하도록 수정할 수 있습니다.
          // 예: return { settlementId: id, title: '알 수 없는 정산', createdAt: new Date().toISOString(), participants: [], payments: [], optimizedTransfers: [], isCompleted: false };
          throw new Error(`ID ${id}에 해당하는 정산 정보를 찾을 수 없습니다.`);
  }
  // 생성된 데이터를 저장소에 저장 (이후 요청 시 캐시처럼 사용)
  mockSettlementStore[id] = await recalculateSettlementInternal(settlementData); // 초기 로드 시 재계산
  return mockSettlementStore[id];
}


/**
 * 주어진 ID의 정산 정보를 비동기적으로 업데이트합니다.
 * 업데이트 후 Mock 저장소에도 반영합니다.
 *
 * @param id - 업데이트할 정산의 ID.
 * @param settlement - 업데이트할 정산 데이터 (특히 payments).
 * @returns 업데이트되고 재계산된 정산 상세 정보를 포함하는 Settlement 객체의 Promise.
 */
export async function updateSettlement(id: string, settlement: Settlement): Promise<Settlement> {
  // TODO: 백엔드 API 호출 구현 필요
   // API 지연 시뮬레이션
   await new Promise(resolve => setTimeout(resolve, 400));
   console.log(`정산 업데이트 ${id}:`, settlement); // 업데이트 요청 데이터 로깅

   // Mock 저장소의 데이터를 업데이트
   if (mockSettlementStore[id]) {
       mockSettlementStore[id] = {
           ...mockSettlementStore[id], // 기존 데이터 유지
           payments: settlement.payments, // 결제 내역 업데이트
           // 다른 필드도 업데이트 가능 (예: title)
       };
       // 업데이트 후 재계산하여 반환
       mockSettlementStore[id] = await recalculateSettlementInternal(mockSettlementStore[id]);
       return mockSettlementStore[id];
   } else {
       // 저장소에 없는 경우 에러 처리 (또는 새로 생성)
       throw new Error(`업데이트할 정산 정보(ID: ${id})를 찾을 수 없습니다.`);
   }
}

/**
 * 주어진 ID의 정산을 비동기적으로 재계산하도록 요청합니다.
 * Mock 저장소의 데이터를 사용하여 재계산하고 결과를 저장소에 반영합니다.
 *
 * @param id - 재계산할 정산의 ID.
 * @returns 재계산된 정산 상세 정보를 포함하는 Settlement 객체의 Promise.
 */
export async function recalculateSettlement(id: string): Promise<Settlement> {
    // TODO: 백엔드 API 호출 구현 필요
    // API 지연 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 600));
    console.log(`정산 재계산 요청 ${id}`);

    // Mock 저장소에서 해당 정산 정보 가져오기
    const settlementToRecalculate = mockSettlementStore[id];
    if (!settlementToRecalculate) {
        throw new Error(`재계산할 정산 정보(ID: ${id})를 찾을 수 없습니다.`);
    }

    // 내부 재계산 함수 호출
    const recalculatedSettlement = await recalculateSettlementInternal(settlementToRecalculate);

    // 재계산 결과를 Mock 저장소에 반영
    mockSettlementStore[id] = recalculatedSettlement;

    return recalculatedSettlement;
}

/**
 * (내부 함수) Settlement 객체를 받아 최적화된 송금 내역을 계산하고 업데이트된 Settlement 객체를 반환합니다.
 *
 * @param settlement - 재계산할 Settlement 객체.
 * @returns 최적화된 송금 내역이 포함된 Settlement 객체.
 */
async function recalculateSettlementInternal(settlement: Settlement): Promise<Settlement> {
   // --- Mock 재계산 로직 시작 ---
   const participants = settlement.participants;
   const paymentsToCalculate = settlement.payments;
   const balances: { [key: string]: number } = {}; // 각 참여자의 최종 받을 돈(+) / 내야 할 돈(-)
   participants.forEach(p => balances[p] = 0); // 0으로 초기화

   // 1. 각 결제 항목을 순회하며 참여자별 잔액 계산
   paymentsToCalculate.forEach(payment => {
     // 결제자 유효성 검사
     if (!participants.includes(payment.payer)) return;

     const validTargets = payment.target.filter(t => participants.includes(t)); // 참여자 목록에 있는 유효한 대상자만 필터링
     if (validTargets.length === 0) return; // 유효한 대상자가 없으면 건너뜀

     const totalRatio = payment.ratio.reduce((sum, r) => sum + r, 0); // 비율 합계 계산

     // 1-1. 결제자는 지출한 금액만큼 잔액 증가
     balances[payment.payer] += payment.amount;

     // 1-2. 정산 대상자들은 각자 부담해야 할 금액만큼 잔액 감소
     // 비율 정보가 유효하고 target 배열과 길이가 맞으면 비율대로 분배
     if (payment.ratio.length === validTargets.length && totalRatio > 0) {
        const amountPerRatio = payment.amount / totalRatio; // 비율 1 단위당 금액
        validTargets.forEach((targetPerson, index) => {
            const costShare = payment.ratio[index] * amountPerRatio; // 각 대상자의 부담 비율에 따른 금액
            if (!isNaN(costShare)) { // 계산 결과가 유효한 숫자인지 확인
               balances[targetPerson] -= costShare;
            } else {
                // 비율 계산 오류 시 N빵으로 처리 (대체 로직)
                 balances[targetPerson] -= payment.amount / validTargets.length;
            }
        });
     } else { // 비율 정보가 없거나 유효하지 않으면 N빵으로 분배
         const equalShare = payment.amount / validTargets.length;
         validTargets.forEach(targetPerson => {
            balances[targetPerson] -= equalShare;
         });
     }
   });

   // --- Mock 최적화 로직 시작 (단순화된 최소 비용 흐름 근사) ---
   // 부동 소수점 오차 감안하여 약간의 허용치(tolerance) 설정
   const tolerance = 0.01; // 0.01원 미만 차이는 무시
   const payers = participants.filter(p => balances[p] > tolerance); // 돈을 받아야 하는 사람 목록
   const receivers = participants.filter(p => balances[p] < -tolerance); // 돈을 내야 하는 사람 목록
   const newOptimizedTransfers: OptimizedTransfer[] = []; // 새로 계산된 송금 목록

   // 정렬: 받아야 할 금액이 큰 순서(내림차순), 내야 할 금액이 큰 순서(오름차순, 음수이므로 절대값 기준 내림차순)
   payers.sort((a, b) => balances[b] - balances[a]);
   receivers.sort((a, b) => balances[a] - balances[b]);

   let payerIndex = 0; // 받을 사람 인덱스
   let receiverIndex = 0; // 낼 사람 인덱스

   // 받을 사람과 낼 사람이 모두 남아있는 동안 반복
   while (payerIndex < payers.length && receiverIndex < receivers.length) {
     const payer = payers[payerIndex]; // 현재 받을 사람
     const receiver = receivers[receiverIndex]; // 현재 낼 사람
     const amountToTransfer = Math.min(balances[payer], -balances[receiver]); // 송금 가능 금액 (받을 금액과 내야 할 금액 중 작은 값)

     if (amountToTransfer > tolerance) { // 허용치보다 큰 금액만 송금 처리
         newOptimizedTransfers.push({
             from: receiver, // 보내는 사람 (빚진 사람)
             to: payer,     // 받는 사람 (받을 권리 있는 사람)
             amount: Math.round(amountToTransfer), // 원 단위 반올림
         });
         // 송금 후 잔액 업데이트
         balances[payer] -= amountToTransfer;
         balances[receiver] += amountToTransfer;
     }

     // 현재 받을 사람이 정산 완료되면 다음 받을 사람으로 이동
     if (Math.abs(balances[payer]) < tolerance) {
       payerIndex++;
     }
     // 현재 낼 사람이 정산 완료되면 다음 낼 사람으로 이동
     if (Math.abs(balances[receiver]) < tolerance) {
       receiverIndex++;
     }
   }
   // --- Mock 최적화 로직 끝 ---

   // 재계산 결과 반환 (원본 정보 유지, 최적화된 송금 목록만 업데이트)
   return {
     ...settlement,
     optimizedTransfers: newOptimizedTransfers, // 새로 계산된 최적 송금 목록
   };
}

/**
 * 주어진 ID의 정산을 '완료' 상태로 표시합니다.
 * 완료된 정산은 더 이상 수정할 수 없습니다.
 *
 * @param id - 완료 처리할 정산의 ID.
 * @returns 업데이트된 정산 상세 정보를 포함하는 Settlement 객체의 Promise.
 */
export async function markSettlementComplete(id: string): Promise<Settlement> {
    // TODO: 백엔드 API 호출 구현 필요
    // API 지연 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 300));
    console.log(`정산 완료 처리 요청 ${id}`);

    // Mock 저장소에서 해당 정산 정보 가져오기
    const settlementToComplete = mockSettlementStore[id];
    if (!settlementToComplete) {
        throw new Error(`완료 처리할 정산 정보(ID: ${id})를 찾을 수 없습니다.`);
    }

    // isCompleted 상태를 true로 변경
    settlementToComplete.isCompleted = true;

    // 변경된 상태를 Mock 저장소에 반영
    mockSettlementStore[id] = settlementToComplete;

    return settlementToComplete;
}


/**
 * 송금 그래프의 노드를 나타냅니다. (참여자)
 */
export interface GraphNode {
  /** 노드 ID (참여자 이름) */
  id: string;
}

/**
 * 송금 그래프의 엣지(연결선)를 나타냅니다. (송금)
 */
export interface GraphEdge {
  /** 시작 노드 ID (돈을 보내는 사람) */
  source: string;
  /** 끝 노드 ID (돈을 받는 사람) */
  target: string;
  /** 송금 금액 */
  amount: number;
}

/**
 * 송금 그래프 전체 데이터를 나타냅니다.
 */
export interface TransferGraph {
  /** 그래프의 모든 노드(참여자) 목록 */
  nodes: GraphNode[];
  /** 그래프의 모든 엣지(최적화된 송금) 목록 */
  edges: GraphEdge[];
}

/**
 * 주어진 ID에 해당하는 정산의 *최적화된* 송금 그래프 정보를 비동기적으로 가져옵니다.
 * 이 함수는 `recalculateSettlement` 결과의 `optimizedTransfers`를 기반으로 그래프 데이터를 생성해야 합니다.
 *
 * @param id - 그래프를 조회할 정산의 ID.
 * @returns 최적화된 송금 정보를 기반으로 구성된 TransferGraph 객체의 Promise.
 */
export async function getTransferGraph(id: string): Promise<TransferGraph> {
  // TODO: 백엔드 API 호출 또는 getSettlement/recalculateSettlement 결과 활용 구현 필요
  // API 지연 시뮬레이션
  await new Promise(resolve => setTimeout(resolve, 200));

  // 최신 정산 데이터(특히 optimizedTransfers)를 얻기 위해 Mock 저장소에서 가져오거나 재계산
  let settlement = mockSettlementStore[id];
  if (!settlement) {
      settlement = await getSettlement(id); // 없으면 로드 (이때 재계산됨)
  } else if (settlement.optimizedTransfers.length === 0 && settlement.payments.length > 0 && !settlement.isCompleted) {
      // 저장소에 있지만 최적화된 전송이 없고, 결제가 있으며, 완료되지 않았다면 재계산
      settlement = await recalculateSettlementInternal(settlement);
      mockSettlementStore[id] = settlement; // 재계산 결과 저장
  }


  // 노드 목록 생성 (모든 참여자)
  const nodes = settlement.participants.map(p => ({ id: p }));

  // 엣지 목록 생성 (최적화된 송금 내역 기반)
  const edges = settlement.optimizedTransfers.map(transfer => ({
    source: transfer.from,
    target: transfer.to,
    amount: transfer.amount,
  }));

  // 금액이 0인 엣지는 그래프에 표시하지 않도록 필터링
  const validEdges = edges.filter(edge => edge.amount > 0);

  return { nodes, edges: validEdges }; // 노드와 유효한 엣지 목록 반환
}

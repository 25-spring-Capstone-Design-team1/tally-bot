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
  /** 각 결제 금액*/
  constant: number[];
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
  calculateId: number;
  startTime: string;
  endTime: string;
  status: 'PENDING' | 'COMPLETED';
}

export interface GroupMember {
  memberId: number;
  nickname: string;
}

export interface RawTransfer {
  payerId: number;
  payeeId: number;
  amount: number;
}


/**
 * 정산 목록 요약 정보를 비동기적으로 가져옵니다.
 *
 * @returns SettlementListItem 객체 배열을 포함하는 Promise.
 */
export async function getSettlementsList(groupId: string): Promise<SettlementListItem[]> {
  const res = await fetch(`/api/proxy/settlement-list?groupId=${groupId}`);
  if (!res.ok) throw new Error('정산 목록 불러오기 실패');

  const calculates = await res.json();

  return calculates.map((item: any) => ({
    calculateId: item.calculateId,
    startTime: item.startTime,
    endTime: item.endTime,
    status: item.status,
  }));
}

/**
 * 주어진 ID에 해당하는 상세 정산 정보를 비동기적으로 가져옵니다.
 * Mock 저장소에 데이터가 없으면 생성하여 저장합니다.
 *
 * @param id - 조회할 정산의 ID.
 * @returns 정산 상세 정보를 포함하는 Settlement 객체의 Promise.
 */
function computeConstant(ratio: number[], amount: number): number[] {
  return ratio.map(r => Math.round(r * amount))
}

export async function getSettlement(calculateId: string): Promise<Settlement> {
  const res = await fetch(`/api/proxy/settlement?id=${calculateId}`);
  if (!res.ok) throw new Error(`정산 ${calculateId} 조회 실패`);
  const data = await res.json();

  const participants = data.settlements
    .flatMap((s: any) => s.participantIds)
    .filter((v: any, i: number, arr: any[]) => arr.indexOf(v) === i)
    .map((id: number) => id.toString());

  const payments = data.settlements.map((s: any) => ({
    id: s.settlementId,
    payer: s.payerId.toString(),
    target: s.participantIds.map((id: number) => id.toString()),
    ratio: Object.values(s.ratios),
    constant: Object.values(s.constants),
    amount: s.amount,
    item: s.item,
  }));

  const transferRes = await fetch(`/api/proxy/transfer-result?id=${calculateId}`);
  if (!transferRes.ok) throw new Error('송금 결과 불러오기 실패');
  const transferData = await transferRes.json();
  const optimizedTransfers = transferData.transfers.map((t: any) => ({
    from: t.payerId.toString(),
    to: t.payeeId.toString(),
    amount: t.amount,
  }));

  return {
    settlementId: calculateId,
    title: `정산 ${calculateId}`,
    createdAt: '',
    participants,
    payments,
    optimizedTransfers,
    isCompleted: false,
  };
}

export async function getGroupMembers(groupId: string): Promise<GroupMember[]> {
  const res = await fetch(`/api/proxy/group-members?groupId=${groupId}`);
  if (!res.ok) throw new Error('멤버 목록 불러오기 실패');
  return await res.json();
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
  const res = await fetch(`http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com/api/settlements/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(settlement),
  });

  if (!res.ok) throw new Error('정산 업데이트 실패');
  return await res.json();
}

/**
 * 특정 결제 항목을 수정하거나 삭제하거나 추가합니다.
 *
 * @param payload - 백엔드에 그대로 전달할 update 요청 바디
 */
export async function updateSettlementField(payload: any): Promise<Settlement> {
  const res = await fetch('/api/proxy/settlement-update', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error(`정산 수정 실패 (status ${res.status})`);
  }

  return await res.json();
}

/**
 * 주어진 ID의 정산을 비동기적으로 재계산하도록 요청합니다.
 * Mock 저장소의 데이터를 사용하여 재계산하고 결과를 저장소에 반영합니다.
 *
 * @param id - 재계산할 정산의 ID.
 * @returns 재계산된 정산 상세 정보를 포함하는 Settlement 객체의 Promise.
 */
export async function recalculateSettlement(calculateId: string): Promise<Settlement> {
  const res = await fetch('/api/proxy/settlement-recalculate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ calculateId }),
  });

  if (!res.ok) {
    throw new Error(`재계산 실패 (status ${res.status})`);
  }

  return await res.json();
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
export async function markSettlementComplete(calculateId: string): Promise<Partial<Settlement>> {
  const res = await fetch('/api/proxy/settlement-complete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ calculateId: Number(calculateId) }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`정산 완료 처리 실패: ${text}`);
  }

  return await res.json(); // Partial<Settlement> 형식임 (완전하지 않음)
}

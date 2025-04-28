// src/hooks/use-toast.ts
"use client"

// react-hot-toast 라이브러리에서 영감을 받음
import * as React from "react"

import type {
  ToastActionElement, // 토스트 액션 버튼 타입
  ToastProps,          // 토스트 컴포넌트 기본 Props 타입
} from "@/components/ui/toast"

const TOAST_LIMIT = 1; // 동시에 표시될 최대 토스트 개수
const TOAST_REMOVE_DELAY = 5000; // 토스트가 자동으로 사라지기까지의 시간 (밀리초)

/** Toaster에서 관리하는 토스트 객체 타입 */
type ToasterToast = ToastProps & {
  id: string; // 고유 ID
  title?: React.ReactNode; // 제목
  description?: React.ReactNode; // 설명
  action?: ToastActionElement; // 액션 버튼
}

/** 액션 타입 정의 */
const actionTypes = {
  ADD_TOAST: "ADD_TOAST", // 토스트 추가
  UPDATE_TOAST: "UPDATE_TOAST", // 토스트 업데이트
  DISMISS_TOAST: "DISMISS_TOAST", // 토스트 숨기기 (애니메이션 시작)
  REMOVE_TOAST: "REMOVE_TOAST", // 토스트 제거 (DOM에서 완전히)
} as const

let count = 0; // 토스트 ID 생성을 위한 카운터

/** 고유한 토스트 ID를 생성하는 함수 */
function genId() {
  count = (count + 1) % Number.MAX_SAFE_INTEGER;
  return count.toString();
}

type ActionType = typeof actionTypes; // 액션 타입들의 유니온

/** Reducer 액션 객체 타입 정의 */
type Action =
  | {
      type: ActionType["ADD_TOAST"]; // 타입: 토스트 추가
      toast: ToasterToast; // 추가할 토스트 객체
    }
  | {
      type: ActionType["UPDATE_TOAST"]; // 타입: 토스트 업데이트
      toast: Partial<ToasterToast>; // 업데이트할 토스트 정보 (부분적)
    }
  | {
      type: ActionType["DISMISS_TOAST"]; // 타입: 토스트 숨기기
      toastId?: ToasterToast["id"]; // 숨길 토스트 ID (없으면 모든 토스트)
    }
  | {
      type: ActionType["REMOVE_TOAST"]; // 타입: 토스트 제거
      toastId?: ToasterToast["id"]; // 제거할 토스트 ID (없으면 모든 토스트)
    }

/** 토스트 상태 타입 정의 */
interface State {
  toasts: ToasterToast[]; // 현재 화면에 표시되거나 제거 대기 중인 토스트 배열
}

// 토스트 자동 제거를 위한 타임아웃 ID 저장 맵
const toastTimeouts = new Map<string, ReturnType<typeof setTimeout>>();

/**
 * 지정된 시간 후에 토스트를 제거 큐에 추가하는 함수.
 * 이미 큐에 있다면 아무 작업도 하지 않음.
 * @param toastId - 제거할 토스트의 ID
 */
const addToRemoveQueue = (toastId: string) => {
  if (toastTimeouts.has(toastId)) { // 이미 제거 대기 중이면 중복 실행 방지
    return;
  }

  // 지정된 시간(TOAST_REMOVE_DELAY) 후에 REMOVE_TOAST 액션 디스패치
  const timeout = setTimeout(() => {
    toastTimeouts.delete(toastId); // 타임아웃 맵에서 제거
    dispatch({
      type: "REMOVE_TOAST",
      toastId: toastId,
    });
  }, TOAST_REMOVE_DELAY);

  toastTimeouts.set(toastId, timeout); // 타임아웃 ID 저장
};

/** 토스트 상태를 관리하는 Reducer 함수 */
export const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "ADD_TOAST": // 새 토스트 추가
      return {
        ...state,
        // 새 토스트를 배열 앞에 추가하고, 최대 개수(TOAST_LIMIT)만큼만 유지
        toasts: [action.toast, ...state.toasts].slice(0, TOAST_LIMIT),
      };

    case "UPDATE_TOAST": // 기존 토스트 업데이트
      return {
        ...state,
        // ID가 일치하는 토스트를 찾아 업데이트된 정보 병합
        toasts: state.toasts.map((t) =>
          t.id === action.toast.id ? { ...t, ...action.toast } : t
        ),
      };

    case "DISMISS_TOAST": { // 토스트 숨기기 (제거 애니메이션 시작)
      const { toastId } = action;

      // ! 부수 효과 ! - DISMISS 액션 발생 시 자동 제거 큐에 추가
      // (별도 함수로 분리 가능하나 여기서는 간단하게 처리)
      if (toastId) { // 특정 토스트 ID가 주어졌다면 해당 토스트만
        addToRemoveQueue(toastId);
      } else { // ID가 없으면 현재 모든 토스트를 제거 큐에 추가
        state.toasts.forEach((toast) => {
          addToRemoveQueue(toast.id);
        });
      }

      return {
        ...state,
        // ID가 일치하거나 ID가 없으면 모든 토스트의 open 상태를 false로 변경
        // (Toast 컴포넌트에서 data-[state=closed] 애니메이션 트리거)
        toasts: state.toasts.map((t) =>
          t.id === toastId || toastId === undefined
            ? {
                ...t,
                open: false,
              }
            : t
        ),
      };
    }
    case "REMOVE_TOAST": // 토스트 완전히 제거 (DOM에서 사라짐)
      if (action.toastId === undefined) { // ID가 없으면 모든 토스트 제거
        return {
          ...state,
          toasts: [],
        };
      }
      // ID가 일치하지 않는 토스트만 남김
      return {
        ...state,
        toasts: state.toasts.filter((t) => t.id !== action.toastId),
      };
  }
};

// 상태 변경을 구독할 리스너(컴포넌트의 setState 함수 등) 배열
const listeners: Array<(state: State) => void> = [];

// 메모리에 저장되는 전역 토스트 상태
let memoryState: State = { toasts: [] };

/**
 * 액션을 받아 Reducer를 실행하고, 변경된 상태를 모든 리스너에게 알리는 함수.
 * @param action - 실행할 액션 객체
 */
function dispatch(action: Action) {
  memoryState = reducer(memoryState, action); // Reducer 실행으로 상태 업데이트
  // 모든 리스너에게 변경된 상태 전달
  listeners.forEach((listener) => {
    listener(memoryState);
  });
}

/** 토스트 생성 시 전달하는 옵션 타입 (ID 제외) */
type Toast = Omit<ToasterToast, "id">;

/**
 * 새 토스트를 생성하고 화면에 표시하는 함수.
 * @param props - 토스트 옵션 (title, description, variant 등)
 * @returns 생성된 토스트의 ID와 dismiss, update 함수를 포함하는 객체
 */
function toast({ ...props }: Toast) {
  const id = genId(); // 고유 ID 생성

  // 토스트 업데이트 함수
  const update = (props: ToasterToast) =>
    dispatch({
      type: "UPDATE_TOAST",
      toast: { ...props, id },
    });
  // 토스트 숨기기 함수
  const dismiss = () => dispatch({ type: "DISMISS_TOAST", toastId: id });

  // ADD_TOAST 액션 디스패치 (새 토스트 추가)
  dispatch({
    type: "ADD_TOAST",
    toast: {
      ...props,
      id,
      open: true, // 초기 상태는 열림
      // Toast 컴포넌트의 onOpenChange 콜백 연결 (닫기 버튼 클릭 시 dismiss 호출)
      onOpenChange: (open) => {
        if (!open) dismiss();
      },
    },
  });

  return { // 생성된 토스트 제어 함수 반환
    id: id,
    dismiss,
    update,
  };
}

/**
 * 토스트 상태와 제어 함수를 사용하기 위한 커스텀 훅.
 * 컴포넌트 내에서 이 훅을 호출하여 토스트 상태를 구독하고, toast() 함수 등으로 토스트를 제어할 수 있습니다.
 */
function useToast() {
  // 컴포넌트의 로컬 상태를 메모리의 전역 상태(memoryState)와 동기화
  const [state, setState] = React.useState<State>(memoryState);

  // 컴포넌트 마운트 시 리스너 배열에 setState 함수 추가, 언마운트 시 제거
  React.useEffect(() => {
    listeners.push(setState);
    return () => {
      const index = listeners.indexOf(setState);
      if (index > -1) {
        listeners.splice(index, 1); // 리스너 제거
      }
    };
  }, [state]); // state가 변경될 때마다 effect 재실행 (일반적으로는 불필요하나, 안전하게 추가)

  return {
    ...state, // 현재 토스트 목록(toasts) 반환
    toast,    // 새 토스트 생성 함수 반환
    dismiss: (toastId?: string) => dispatch({ type: "DISMISS_TOAST", toastId }), // 토스트 숨기기 함수 반환
  };
}

export { useToast, toast }; // 훅과 생성 함수 내보내기

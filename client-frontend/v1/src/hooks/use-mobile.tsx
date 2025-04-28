// src/hooks/use-mobile.tsx
import * as React from "react";

// 모바일 화면으로 간주할 최대 너비 (픽셀 단위)
const MOBILE_BREAKPOINT = 768;

/**
 * 현재 브라우저 창 크기가 모바일 환경(MOBILE_BREAKPOINT 미만)인지 여부를 감지하는 커스텀 훅.
 * 화면 크기 변경 시 상태를 업데이트합니다.
 * 서버 사이드 렌더링 시에는 `undefined`를 반환할 수 있으므로, 클라이언트 측에서만 사용해야 합니다.
 *
 * @returns 현재 모바일 환경이면 `true`, 아니면 `false`. 초기 렌더링 시에는 `undefined`일 수 있습니다.
 */
export function useIsMobile() {
  // isMobile 상태: undefined는 초기 상태(서버 렌더링 또는 클라이언트 초기화 전)
  const [isMobile, setIsMobile] = React.useState<boolean | undefined>(undefined);

  React.useEffect(() => {
    // matchMedia API를 사용하여 미디어 쿼리 객체 생성
    const mql = window.matchMedia(`(max-width: ${MOBILE_BREAKPOINT - 1}px)`);

    // 화면 크기 변경 감지 시 호출될 콜백 함수
    const onChange = () => {
      setIsMobile(window.innerWidth < MOBILE_BREAKPOINT); // 현재 창 너비로 상태 업데이트
    };

    // 이벤트 리스너 등록: 화면 크기 변경 시 onChange 함수 실행
    mql.addEventListener("change", onChange);

    // 컴포넌트 마운트 시 초기 상태 설정
    setIsMobile(window.innerWidth < MOBILE_BREAKPOINT);

    // 컴포넌트 언마운트 시 이벤트 리스너 제거 (메모리 누수 방지)
    return () => mql.removeEventListener("change", onChange);
  }, []); // 빈 의존성 배열: 컴포넌트 마운트 시 1회만 실행

  // isMobile 상태 반환 (초기에는 undefined일 수 있으므로 boolean으로 강제 형변환)
  return !!isMobile;
}

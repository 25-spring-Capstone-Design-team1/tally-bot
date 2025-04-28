// src/lib/utils.ts
import { clsx, type ClassValue } from "clsx"; // 클래스 이름 배열/객체를 문자열로 변환하는 라이브러리
import { twMerge } from "tailwind-merge"; // Tailwind 클래스 이름을 병합하고 충돌을 해결하는 라이브러리

/**
 * 여러 개의 클래스 이름 입력값(문자열, 배열, 객체 등)을 받아
 * Tailwind CSS 클래스 이름으로 병합하고 최적화하는 함수.
 * clsx와 tailwind-merge를 함께 사용하여 조건부 클래스 적용과 클래스 충돌 해결을 용이하게 합니다.
 *
 * 예시: cn("p-4", "font-bold", { "bg-red-500": isError }, ["m-2", "text-white"])
 *
 * @param inputs - 병합할 클래스 이름들 (ClassValue 타입: string | number | null | boolean | undefined | {[key: string]: any} | ClassValue[])
 * @returns 병합되고 최적화된 Tailwind CSS 클래스 이름 문자열
 */
export function cn(...inputs: ClassValue[]) {
  // 1. clsx를 사용하여 다양한 형태의 입력값을 하나의 클래스 문자열로 변환
  // 2. twMerge를 사용하여 변환된 문자열 내 Tailwind 클래스 충돌 해결 및 병합
  return twMerge(clsx(inputs));
}

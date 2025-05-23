// src/components/Header.tsx
'use client';
import Link from 'next/link';
import Image from 'next/image';
import { useParams } from 'next/navigation'; 
import { useEffect, useState } from 'react';
import { Scale } from 'lucide-react'; // 아이콘 임포트 (현재 로고 SVG로 대체됨)

/**
 * 애플리케이션 상단 헤더 컴포넌트.
 * 로고와 애플리케이션 이름을 표시하며, 메인 페이지로 링크됩니다.
 */
export default function Header() {
  const params = useParams();
  const [groupId, setGroupId] = useState<string | null>(null);
  useEffect(() => {
    if (params?.groupId && typeof params.groupId === 'string') {
      setGroupId(params.groupId);
    }
  }, [params]);
  const homeUrl = groupId ? `/${groupId}` : '/';
  return (
    // 헤더 영역 스타일: 약간 어두운 배경색, 하단 테두리, 그림자 효과
    <header className="bg-secondary/70 dark:bg-secondary/40 text-secondary-foreground border-b border-border/60 shadow-sm">
      <nav className="container mx-auto px-4 py-3 flex items-center justify-between">
        {/* 로고 및 앱 이름 영역 */}
        <Link href={homeUrl} className="flex items-center gap-2">
          {/* 로고 컨테이너: 파란색 배경, 둥근 모서리 */}
           <div className="p-1 rounded flex items-center justify-center bg-primary">
             {/* SVG 로고: 흰색 저울과 노란색 삼각형 */}
             <svg width="28" height="28" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                 {/* 저울대 */}
                 <path d="M45 85V35H55V85H45Z" fill="white"/>
                 {/* 저울 팔 */}
                 <path d="M15 40L25 35L80 20L85 25L25 45L15 40Z" fill="white"/>
                 <path d="M20 50L30 45L85 30L90 35L30 55L20 50Z" fill="white"/>
                 {/* 저울 접시 (노란색 삼각형) */}
                 <polygon points="20,55 30,75 10,75" fill="#F8C81E" />
                 <polygon points="80,35 90,55 70,55" fill="#F8C81E" />
            </svg>
          </div>
          {/* 애플리케이션 이름 */}
          <span className="text-xl font-bold text-primary">TallyBot</span>
        </Link>
        {/* 네비게이션 링크 추가 영역 (필요 시) */}
        {/* 예: <Link href="/about">소개</Link> */}
      </nav>
    </header>
  );
}

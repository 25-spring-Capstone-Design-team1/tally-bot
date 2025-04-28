import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { Toaster } from '@/components/ui/toaster'; // Import Toaster
import Header from '@/components/Header'; // Import Header component

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'TallyBot',
  description: 'Automated settlement based on KakaoTalk chat history',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={`${inter.className} antialiased flex flex-col min-h-screen`}>
        <Header /> {/* Add the Header */}
        {/* Make main content area flexible and take remaining space */}
        <main className="flex-grow bg-background p-4 md:p-8">
          {children}
        </main>
        <Toaster /> {/* Add the Toaster */}
      </body>
    </html>
  );
}

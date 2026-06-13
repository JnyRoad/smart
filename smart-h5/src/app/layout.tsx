import type { Metadata, Viewport } from 'next'
import Script from 'next/script'
import { QueryProvider } from '@/components/query-provider'
import { AuthBootstrap } from '@/features/auth/auth-bootstrap'
import './globals.css'

export const metadata: Metadata = {
  title: '裕同智慧园区',
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  viewportFit: 'cover',
}

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="zh-CN">
      <body>
        <Script src="/config.js" strategy="beforeInteractive" />
        <AuthBootstrap />
        <QueryProvider>
          <div className="app-frame">{children}</div>
        </QueryProvider>
      </body>
    </html>
  )
}

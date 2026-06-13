'use client'
import { NavBar } from 'antd-mobile'
import { useRouter } from 'next/navigation'

/** Secondary-page shell: NavBar with back arrow + content area. */
export function PageShell({
  title,
  showBack = true,
  onBack,
  children,
}: {
  title: string
  showBack?: boolean
  onBack?: () => void
  children: React.ReactNode
}) {
  const router = useRouter()
  return (
    <div className="flex min-h-dvh flex-col">
      <NavBar
        backIcon={showBack}
        onBack={showBack ? (onBack ?? (() => router.back())) : undefined}
        className="bg-white"
      >
        {title}
      </NavBar>
      <main className="flex-1 p-3">{children}</main>
    </div>
  )
}

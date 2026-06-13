'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getBbsList, type BbsItem } from '@/features/home/api'
import { PageShell } from '@/components/page-shell'
import { getTenantConfig } from '@/lib/config/tenant'
import { isHttpUrl } from '@/lib/format/url'

export default function BbsListPage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const { parkId } = getTenantConfig()

  const list = useQuery({
    queryKey: ['bbs', 'list'],
    // Legacy quirk preserved: the list page sends parkId as `badge`.
    queryFn: () => getBbsList({ current: 1, size: 100, badge: parkId }),
    enabled: authorized,
  })

  useEffect(() => {
    if (list.isError) Toast.show(list.error.message || '网络错误')
  }, [list.isError, list.error])

  function open(item: BbsItem) {
    // contentLinkType: 1 = external link, 2 = rich text, 4 = PDF
    if (item.contentLinkType === 1 && isHttpUrl(item.bbsUrl)) {
      window.location.assign(item.bbsUrl)
    } else if (item.bbsId !== undefined) {
      const isPdf = item.contentLinkType === 4
      router.push(`/home/bbs/${item.bbsId}?isPdf=${isPdf}`)
    }
  }

  if (!authorized) return null
  const records = list.data?.data?.records ?? []

  return (
    <PageShell title="公告列表">
      {list.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : records.length === 0 ? (
        <ErrorBlock status="empty" description="暂无公告" />
      ) : (
        <div className="flex flex-col gap-3">
          {records.map((item, index) => (
            <button
              key={`${item.bbsId}-${index}`}
              type="button"
              onClick={() => open(item)}
              className="flex items-center gap-3 rounded-2xl bg-white p-3 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
            >
              {item.bbsImg && (
                <Image
                  src={item.bbsImg}
                  alt=""
                  width={60}
                  height={60}
                  unoptimized
                  className="h-[60px] w-[60px] shrink-0 rounded-xl object-cover"
                />
              )}
              <span className="min-w-0 flex-1 truncate text-[15px] text-ink">{item.bbsTitle}</span>
              <span aria-hidden className="text-weak">
                ›
              </span>
            </button>
          ))}
        </div>
      )}
    </PageShell>
  )
}

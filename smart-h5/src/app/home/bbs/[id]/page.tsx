'use client'
import { useQuery } from '@tanstack/react-query'
import { SpinLoading, Toast } from 'antd-mobile'
import { useSearchParams } from 'next/navigation'
import { Suspense, use, useEffect } from 'react'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getBbsDetail } from '@/features/home/api'
import { PageShell } from '@/components/page-shell'
import { RichTextBody } from '@/components/rich-text-body'
import { isHttpUrl } from '@/lib/format/url'

function BbsDetailInner({ id }: { id: string }) {
  const authorized = useRequireAuth()
  const isPdf = useSearchParams().get('isPdf') === 'true'

  const detail = useQuery({
    queryKey: ['bbs', 'detail', id],
    queryFn: () => getBbsDetail(id),
    enabled: authorized,
  })

  useEffect(() => {
    if (detail.isError) Toast.show(detail.error.message || '网络错误')
  }, [detail.isError, detail.error])

  if (!authorized) return null
  const info = detail.data?.data

  return (
    <PageShell title="公告详情">
      {detail.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : isPdf ? (
        // Inline PDF preview; a pdfjs-based renderer can replace this later.
        isHttpUrl(info?.previewUrl) ? (
          <iframe
            src={info.previewUrl}
            title="公告 PDF"
            className="h-[80dvh] w-full rounded-2xl bg-white"
          />
        ) : (
          <p className="py-16 text-center text-sm text-weak">PDF 地址无效</p>
        )
      ) : (
        <article className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
          {info?.bbsTitle && <h1 className="mb-3 text-lg font-bold">{info.bbsTitle}</h1>}
          <RichTextBody html={info?.bbsContent ?? ''} />
        </article>
      )}
    </PageShell>
  )
}

export default function BbsDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  return (
    <Suspense>
      <BbsDetailInner id={id} />
    </Suspense>
  )
}

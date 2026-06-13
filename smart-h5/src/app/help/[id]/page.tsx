'use client'
import { useQuery } from '@tanstack/react-query'
import { SpinLoading, Toast } from 'antd-mobile'
import { use, useEffect } from 'react'
import { PageShell } from '@/components/page-shell'
import { RichTextBody } from '@/components/rich-text-body'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getHelpAnswer } from '@/features/help/api'

function HelpDetailInner({ id }: { id: string }) {
  const authorized = useRequireAuth()

  const detail = useQuery({
    queryKey: ['help', 'answer', id],
    queryFn: () => getHelpAnswer(id),
    enabled: authorized,
  })

  useEffect(() => {
    if (detail.isError) Toast.show(detail.error.message || '网络错误')
  }, [detail.isError, detail.error])

  if (!authorized) return null

  return (
    <PageShell title="问题详情">
      {detail.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <article className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
          {detail.data?.data?.questionTitle && (
            <h1 className="mb-3 text-lg font-bold">{detail.data.data.questionTitle}</h1>
          )}
          <RichTextBody html={detail.data?.data?.answerContent ?? ''} />
        </article>
      )}
    </PageShell>
  )
}

export default function HelpDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  return <HelpDetailInner id={id} />
}

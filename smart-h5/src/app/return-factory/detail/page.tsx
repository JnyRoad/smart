'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, ImageViewer, SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { confirmBack, getReleaseDetail } from '@/features/good-release/api'
import { InfoRow, toImageSrc } from '@/features/good-release/detail-blocks'
import { saveDetailSnapshot } from '@/features/good-release/detail-snapshot'
import { isPersonRelease } from '@/features/good-release/dicts'
import { showBackConfirm } from '@/features/good-release/release-status'

function ReturnFactoryDetailInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const id = params.get('id') ?? ''
  const [confirming, setConfirming] = useState(false)

  const detail = useQuery({
    queryKey: ['good-release-detail', id],
    queryFn: () => getReleaseDetail(id),
    enabled: authorized && id !== '',
  })
  const info = detail.data?.code === 0 ? detail.data.data : undefined
  const failed = id === '' || detail.isError || (detail.isSuccess && detail.data.code !== 0)
  const applyMain = info?.applyMain

  function openPersons() {
    saveDetailSnapshot({ persons: info?.personDetailList ?? [] })
    router.push('/good-release/work/detail/persons')
  }

  function openGoods() {
    saveDetailSnapshot({ goods: info?.thingDetailList ?? [] })
    router.push('/good-release/work/detail/goods')
  }

  async function handleConfirm() {
    if (confirming) return
    setConfirming(true)
    try {
      const res = await confirmBack(id)
      if (res.code === 0 && res.data) {
        router.replace('/return-factory?tab=confirmed')
      } else {
        Toast.show(res.message || res.msg || '网络错误')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '网络错误')
    } finally {
      setConfirming(false)
    }
  }

  if (!authorized) return null

  return (
    <PageShell title="返厂确认">
      {failed || (detail.isSuccess && !info) ? (
        <ErrorBlock
          status="default"
          title="加载失败"
          description={detail.data?.message || detail.data?.msg || '网络错误'}
        />
      ) : detail.isPending || !info ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3 pb-20">
          {/* 头部：申请人 | 部门 + 返厂标签 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <div className="flex items-center justify-between border-b border-border-soft py-3">
              <span className="text-[15px] font-bold">
                {info.name}
                <span className="px-2 text-mid">|</span>
                {info.deptName}
              </span>
              <span
                className={`flex-none rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                  applyMain?.sffcDesc === '是' ? 'bg-accent-soft text-accent-ink' : 'bg-surface text-mid'
                }`}
              >
                {applyMain?.sffcDesc === '是' ? '返厂' : '不返厂'}
              </span>
            </div>
            <InfoRow label="放行去处" value={applyMain?.fxqcDesc} />
            <InfoRow label="出发地点" value={applyMain?.fxddDesc} />
            <InfoRow label="到达地点" value={applyMain?.ddddDesc} />
            <InfoRow label="放行事项" value={applyMain?.fxsxDesc} />
            <InfoRow label="放行类别" value={applyMain?.wpfxlbDesc} />
            <InfoRow label="放行人级别" value={applyMain?.sqrjbDesc} />
            <InfoRow
              label="附件"
              value={
                applyMain?.fjsc ? (
                  <button
                    type="button"
                    onClick={() => ImageViewer.show({ image: toImageSrc(applyMain.fjsc ?? '') })}
                    className="relative inline-block h-[72px] w-[72px] overflow-hidden rounded-xl"
                  >
                    <Image src={toImageSrc(applyMain.fjsc)} alt="附件" fill unoptimized className="object-cover" />
                  </button>
                ) : (
                  '-'
                )
              }
            />
          </div>

          {/* 条件块：人员 / 物品标签云（空列表不渲染可点击区） */}
          {isPersonRelease(applyMain?.fxsx)
            ? (info.personDetailList?.length ?? 0) > 0 && (
                <div className="rounded-2xl bg-white px-4 py-3 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
                  <p className="pb-2 text-[13px] font-semibold text-mid">放行人员</p>
                  <button type="button" onClick={openPersons} className="flex flex-wrap gap-2 text-left">
                    {(info.personDetailList ?? []).map((person, index) => (
                      <span key={index} className="rounded-full bg-surface px-3 py-1 text-[13px]">
                        {person.xm}
                      </span>
                    ))}
                  </button>
                </div>
              )
            : (info.thingDetailList?.length ?? 0) > 0 && (
                <div className="rounded-2xl bg-white px-4 py-3 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
                  <p className="pb-2 text-[13px] font-semibold text-mid">放行物品</p>
                  <button type="button" onClick={openGoods} className="flex flex-wrap gap-2 text-left">
                    {(info.thingDetailList ?? []).map((thing, index) => (
                      <span key={index} className="rounded-full bg-surface px-3 py-1 text-[13px]">
                        {thing.wpbm}-{thing.wpmc}
                        {thing.wpsl}
                        {thing.wpdw}
                      </span>
                    ))}
                  </button>
                </div>
              )}

          <ApprovalTimeline
            submitter={info.name ? { name: info.name } : undefined}
            nodes={info.approvalProcess ?? []}
          />

          {showBackConfirm(info) && (
            <div className="fixed inset-x-0 bottom-0 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={confirming}
                onClick={() => void handleConfirm()}
                className="flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                {confirming ? '请稍后…' : '确认返厂'}
              </button>
            </div>
          )}
        </div>
      )}
    </PageShell>
  )
}

export default function ReturnFactoryDetailPage() {
  return (
    <Suspense>
      <ReturnFactoryDetailInner />
    </Suspense>
  )
}

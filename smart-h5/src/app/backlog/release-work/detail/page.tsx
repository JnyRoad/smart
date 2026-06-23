'use client'
import { useQuery } from '@tanstack/react-query'
import { ImageViewer, SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { ImageListUpload } from '@/components/image-list-upload'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { securityUpdateRelease } from '@/features/backlog/api'
import { getReleaseDetail } from '@/features/good-release/api'
import { InfoRow, toImageSrc } from '@/features/good-release/detail-blocks'
import { saveDetailSnapshot } from '@/features/good-release/detail-snapshot'
import { isPersonRelease } from '@/features/good-release/dicts'
import { getTenantConfig } from '@/lib/config/tenant'
import { confirmIrreversible } from '@/lib/confirm-irreversible'

function ReleaseWorkApprovalDetailInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const config = getTenantConfig()
  const id = params.get('id') ?? ''
  const readOnly = params.get('tab') === 'done'
  const fromScan = params.get('scan') === '1'
  const [guardImgs, setGuardImgs] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined

  const detail = useQuery({
    queryKey: ['good-release-detail', id],
    queryFn: () => getReleaseDetail(id),
    enabled: authorized && id !== '',
  })
  const info = detail.data?.code === 0 ? detail.data.data : undefined
  const failed = id === '' || detail.isError || (detail.isSuccess && (detail.data.code !== 0 || !detail.data.data))
  const applyMain = info?.applyMain

  // 旧行为：详情加载失败 toast 后回 /home。
  const warnedRef = useRef(false)
  useEffect(() => {
    if (failed && !warnedRef.current) {
      warnedRef.current = true
      Toast.show(detail.data?.message || '网络错误')
      router.replace('/home')
    }
  }, [failed, detail.data?.message, router])

  // 旧事实：仅 status===2（待保安处理）显示操作。
  const actionable = !readOnly && info?.status === 2
  const needUpload = info?.isUploadImg === 0 && actionable

  function openPersons() {
    saveDetailSnapshot({ persons: info?.personDetailList ?? [] })
    router.push('/good-release/work/detail/persons')
  }

  function openGoods() {
    saveDetailSnapshot({ goods: info?.thingDetailList ?? [] })
    router.push('/good-release/work/detail/goods')
  }

  async function submit(nextStatus: 4 | 5) {
    if (submitting) return
    if (!badge) return Toast.show('获取用户信息失败！')
    if (info?.isUploadImg === 0 && guardImgs.length === 0) {
      Toast.show('请至少上传一张照片')
      return
    }
    // 通过/拒绝不可撤销，执行前二次确认。
    const message = nextStatus === 5 ? '确定拒绝该申请？拒绝后不可撤销' : '确定通过该申请？通过后不可撤销'
    if (!(await confirmIrreversible(message))) return
    setSubmitting(true)
    try {
      const res = await securityUpdateRelease({
        guardOneImg: guardImgs[0] ?? '',
        guardTwoImg: guardImgs[1] ?? '',
        guardThreeImg: guardImgs[2] ?? '',
        id,
        parkId: config.parkId,
        status: nextStatus,
        badge,
        // 旧版提交参数含 remark 但页面无输入项（恒空），照旧不渲染输入。
        remark: '',
      })
      if (res.code === 0 && res.data) {
        // 修正旧版误跳生活区列表的 bug：回本模块「我审批的」。
        router.replace(fromScan ? '/home' : '/backlog/release-work?tab=done')
      } else {
        Toast.show(res.message || res.msg || '网络错误')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '网络错误')
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  return (
    <PageShell title="放行审批">
      {detail.isPending || failed || !info ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3 pb-24">
          {/* 申请人 + 申请信息（批 3 work 详情骨架） */}
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

          {/* 条件块：人员 / 物品标签云 */}
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

          {needUpload && (
            <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              <p className="mb-2 text-[13px] font-semibold text-mid">
                上传物品照片 <span className="text-[#d83b36]">*</span>
              </p>
              <ImageListUpload mode="base64" value={guardImgs} onChange={setGuardImgs} />
            </div>
          )}

          <ApprovalTimeline
            submitter={info.name ? { name: info.name } : undefined}
            nodes={info.approvalProcess ?? []}
          />

          {actionable && (
            <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submit(5)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-[#d83b36] text-base font-semibold text-[#d83b36] disabled:opacity-60"
              >
                拒 绝
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submit(4)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                通 过
              </button>
            </div>
          )}
        </div>
      )}
    </PageShell>
  )
}

export default function ReleaseWorkApprovalDetailPage() {
  return (
    <Suspense>
      <ReleaseWorkApprovalDetailInner />
    </Suspense>
  )
}

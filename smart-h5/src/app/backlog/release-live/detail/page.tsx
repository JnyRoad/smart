'use client'
import { useQuery } from '@tanstack/react-query'
import { ImageViewer, SpinLoading, TextArea, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { StatusIcon } from '@/components/app-icon'
import { ImageListUpload } from '@/components/image-list-upload'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { securityUpdateRelease, updateReleaseStatus } from '@/features/backlog/api'
import { getReleaseDetail } from '@/features/good-release/api'
import { InfoRow, toImageSrc } from '@/features/good-release/detail-blocks'
import { getTenantConfig } from '@/lib/config/tenant'

function ReleaseLiveApprovalDetailInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const config = getTenantConfig()
  const id = params.get('id') ?? ''
  const sort = Number(params.get('sort'))
  const readOnly = params.get('tab') === 'done'
  const fromScan = params.get('scan') === '1'
  const [remark, setRemark] = useState('')
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

  // 旧行为：详情加载失败 toast 后回 /home。
  const warnedRef = useRef(false)
  useEffect(() => {
    if (failed && !warnedRef.current) {
      warnedRef.current = true
      Toast.show(detail.data?.message || '网络错误')
      router.replace('/home')
    }
  }, [failed, detail.data?.message, router])

  const status = info?.status ?? 0
  const actionable = !readOnly && status < 4
  // isUploadImg 0 = 园区要求保安上传照片。
  const needUpload = sort === 3 && info?.isUploadImg === 0 && actionable
  const photos = [info?.oneImg, info?.twoImg, info?.threeImg].filter((img): img is string => !!img)

  async function submitApproval(nextStatus: 2 | 3) {
    if (submitting) return
    if (!badge) return Toast.show('获取用户信息失败！')
    setSubmitting(true)
    try {
      const res = await updateReleaseStatus({ approveBadge: badge, id, status: nextStatus, remark })
      if (res.code === 0 && res.data) {
        router.replace(fromScan ? '/home' : '/backlog/release-live?tab=done')
      } else {
        Toast.show(res.message || res.msg || '网络错误')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '网络错误')
    } finally {
      setSubmitting(false)
    }
  }

  async function submitSecurity(nextStatus: 4 | 5) {
    if (submitting) return
    if (!badge) return Toast.show('获取用户信息失败！')
    if (info?.isUploadImg === 0 && guardImgs.length === 0) {
      Toast.show('请至少上传一张照片')
      return
    }
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
        remark,
      })
      if (res.code === 0 && res.data) {
        router.replace(fromScan ? '/home' : '/backlog/release-live?tab=done')
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
          {/* 申请信息 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <div className="flex items-center justify-between border-b border-border-soft py-3">
              <span className="text-lg font-bold">{info.carrier}</span>
              <div className="relative h-[72px] w-[72px] overflow-hidden rounded-xl bg-surface">
                {info.facePic ? (
                  <Image src={toImageSrc(info.facePic)} alt="人脸照片" fill unoptimized className="object-cover" />
                ) : (
                  <span className="grid h-full w-full place-items-center text-brand" aria-hidden>
                    <StatusIcon name="user" className="h-8 w-8" />
                  </span>
                )}
              </div>
            </div>
            <InfoRow label="物品类型" value={info.articlesTypeName} />
            <InfoRow label="物品名称" value={info.articlesDesc} />
            <InfoRow label="房间信息" value={`${info.dormitoryName ?? ''}${info.roomName ?? ''}` || undefined} />
            <InfoRow label="离厂时间" value={info.plannedDepartureTime} />
            <InfoRow label="车牌号" value={info.licensePlate || undefined} />
            <InfoRow label="备注信息" value={info.remarks || undefined} />
            {photos.length > 0 && (
              <div className="flex gap-2.5 py-2.5">
                {photos.map((img, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => ImageViewer.Multi.show({ images: photos.map(toImageSrc), defaultIndex: index })}
                    className="relative h-[72px] w-[72px] overflow-hidden rounded-xl"
                  >
                    <Image src={toImageSrc(img)} alt="物品照片" fill unoptimized className="object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

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
            <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              <p className="mb-2 text-[13px] font-semibold text-mid">审批意见（选填）</p>
              <div className="rounded-xl border border-border-soft px-3 py-2">
                <TextArea placeholder="请输入内容" value={remark} onChange={setRemark} rows={2} />
              </div>
            </div>
          )}

          {actionable && (sort === 1 || sort === 2) && (
            <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submitApproval(3)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-[#d83b36] text-base font-semibold text-[#d83b36] disabled:opacity-60"
              >
                拒 绝
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submitApproval(2)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                通 过
              </button>
            </div>
          )}
          {actionable && sort === 3 && (
            <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submitSecurity(5)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-[#d83b36] text-base font-semibold text-[#d83b36] disabled:opacity-60"
              >
                拒绝放行
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submitSecurity(4)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                确认放行
              </button>
            </div>
          )}
        </div>
      )}
    </PageShell>
  )
}

export default function ReleaseLiveApprovalDetailPage() {
  return (
    <Suspense>
      <ReleaseLiveApprovalDetailInner />
    </Suspense>
  )
}

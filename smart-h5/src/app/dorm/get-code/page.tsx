'use client'
import { useQuery } from '@tanstack/react-query'
import { Dialog, ErrorBlock, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { FaceUpload } from '@/components/face-upload'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { refreshLockPwd } from '@/features/dorm/api'

/** Regenerates the door-lock code after an on-device face check. */
export default function GetCodePage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  // facePic = checkFace 响应的 resultData.base64（与访客模块消费 data 不同）
  const [facePic, setFacePic] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined
  const baseInfoFailed = baseInfo.isError || (baseInfo.isSuccess && baseInfo.data.code !== 0)

  async function handleGenerate() {
    if (!facePic || !badge) return
    setSubmitting(true)
    try {
      const res = await refreshLockPwd({ badge, facePic })
      if (res.code === 0) {
        void Dialog.alert({ content: '刷新动态码成功！', confirmText: '确定' }).then(() =>
          router.replace('/dorm/lock'),
        )
      } else {
        void Dialog.alert({ title: '错误', content: res.msg ?? res.message ?? '刷新失败' })
      }
    } catch (error) {
      void Dialog.alert({ title: '错误', content: error instanceof Error ? error.message : '刷新失败' })
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  if (baseInfoFailed) {
    return (
      <PageShell title="刷新动态码">
        <div className="py-8">
          <ErrorBlock status="default" title="加载失败" description="员工信息获取失败" />
          <button
            type="button"
            onClick={() => void baseInfo.refetch()}
            className="mx-auto mt-4 flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white"
          >
            重试
          </button>
        </div>
      </PageShell>
    )
  }

  return (
    <PageShell title="刷新动态码">
      <div className="flex flex-col items-center gap-4 pt-6">
        <h2 className="text-[17px] font-bold">刷新动态码</h2>
        <p className="text-[13px] text-mid">需完成人脸识别</p>

        <FaceUpload
          mode="face"
          value={facePic ? 'uploaded' : ''}
          onChange={() => {}}
          onUploaded={(raw) => {
            const base64 = raw.resultData?.base64
            if (base64) {
              setFacePic(base64)
            } else {
              Toast.show('人脸比对结果异常，请重试')
            }
          }}
          label="请拍照"
        />

        {facePic ? (
          <>
            <p className="text-sm font-semibold text-[#16a673]">人脸对比成功</p>
            <button
              type="button"
              disabled={submitting}
              onClick={() => void handleGenerate()}
              className="mt-2 flex h-12 w-2/3 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
            >
              {submitting ? '生成中…' : '生成动态码'}
            </button>
          </>
        ) : (
          <p className="text-xs text-weak">请拍摄正面人脸照片完成比对</p>
        )}
      </div>
    </PageShell>
  )
}

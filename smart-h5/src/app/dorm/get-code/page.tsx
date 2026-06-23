'use client'
import { useQuery } from '@tanstack/react-query'
import { Dialog, ErrorBlock } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { refreshLockPwd } from '@/features/dorm/api'
import { LockFaceCamera } from '@/features/dorm/lock-face-camera'
import styles from './page.module.css'

/** 通过本机人脸核验后刷新宿舍门锁动态码。 */
export default function GetCodePage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  // facePic 是传给 checkFace 的 base64；常规 checkFace 响应只返回 data.photoId。
  const [facePic, setFacePic] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined
  const employeeName = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeName : undefined
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
      void Dialog.alert({ title: '错误', content: toUserMessage(error, '刷新失败') })
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  if (baseInfoFailed) {
    return (
      <div className={styles.page}>
        <FaceScanTopbar onBack={() => router.back()} onClose={() => router.replace('/dorm/lock')} />
        <main className={styles.errorArea}>
          <ErrorBlock status="default" title="加载失败" description="员工信息获取失败" />
          <button type="button" onClick={() => void baseInfo.refetch()} className={styles.retryButton}>
            重试
          </button>
        </main>
      </div>
    )
  }

  return (
    <div className={styles.page} data-testid="lock-face-page">
      <FaceScanTopbar onBack={() => router.back()} onClose={() => router.replace('/dorm/lock')} />

      <section className={styles.context} aria-label="宿舍门锁信息">
        <div className={styles.contextMain}>
          <div>
            <p className={styles.personName}>{employeeName ?? '当前员工'}</p>
            <p className={styles.personMeta}>
              <span className={styles.metaSegment}>工号 {badge ?? '--'}</span>
              <span className={styles.metaSegment}>宿舍门锁</span>
              <span className={styles.metaSegment}>{facePic ? '已完成核验' : '本人核验'}</span>
            </p>
          </div>
          <div className={styles.lockBadge}>
            <span>动态码刷新</span>
            <span>需人脸核验</span>
          </div>
        </div>
      </section>

      <LockFaceCamera
        onCaptured={setFacePic}
        onGenerate={() => void handleGenerate()}
        generating={submitting}
        generateDisabled={!facePic || !badge}
      />
    </div>
  )
}

function FaceScanTopbar({ onBack, onClose }: { onBack: () => void; onClose: () => void }) {
  return (
    <header className={styles.topbar}>
      <button className={styles.navButton} type="button" aria-label="返回" onClick={onBack}>
        <span className={styles.backMark} aria-hidden="true" />
      </button>
      <h1 className={styles.topbarTitle}>刷新动态码</h1>
      <button className={styles.navButton} type="button" aria-label="关闭" onClick={onClose}>
        <span>关闭</span>
      </button>
    </header>
  )
}

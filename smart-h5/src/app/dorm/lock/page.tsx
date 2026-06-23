'use client'
import { useQuery } from '@tanstack/react-query'
import { Dialog, ErrorBlock, SpinLoading, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { PageShell } from '@/components/page-shell'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { getLockPwd, updateLockPwd } from '@/features/dorm/api'
import { decryptFromHex } from '@/lib/crypto/aes'

/** Decrypt result with an explicit failure reason instead of silent ******. */
function safeDecrypt(cipher: string): { code: string; error: string | null } {
  try {
    const plain = decryptFromHex(cipher)
    return plain ? { code: plain, error: null } : { code: '', error: '动态码解析失败' }
  } catch {
    // getKey() throws when securityEncodeKey is missing — a deploy-config error.
    return { code: '', error: '加密密钥未配置，请联系管理员' }
  }
}

function RetryBlock({ description, onRetry }: { description: string; onRetry: () => void }) {
  return (
    <div className="py-8">
      <ErrorBlock status="default" title="加载失败" description={description} />
      <button
        type="button"
        onClick={onRetry}
        className="mx-auto mt-4 flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white"
      >
        重试
      </button>
    </div>
  )
}

/** Door-lock dynamic code: decrypted display + change dialog + face refresh entry. */
export default function LockPage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const [editVisible, setEditVisible] = useState(false)
  const [newPwd, setNewPwd] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const notCheckedInRef = useRef(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined

  const pwd = useQuery({
    queryKey: ['lock-pwd', badge],
    queryFn: () => getLockPwd(badge as string),
    enabled: authorized && badge !== undefined,
  })

  const pwdFailed = pwd.isError || (pwd.isSuccess && pwd.data.code !== 0)
  const cipher = pwd.data?.code === 0 ? (pwd.data.data ?? '') : ''
  const decrypted = cipher ? safeDecrypt(cipher) : { code: '', error: null }

  // Not checked in: the gateway returns an empty pwd with a success code.
  useEffect(() => {
    if (pwd.isSuccess && pwd.data.code === 0 && !pwd.data.data && !notCheckedInRef.current) {
      notCheckedInRef.current = true
      void Dialog.alert({
        content: '您暂未入住智能宿舍，请联系宿管入住！',
        confirmText: '确定',
      }).then(() => router.replace('/dorm'))
    }
  }, [pwd.isSuccess, pwd.data, router])

  useEffect(() => {
    if (decrypted.error) Toast.show(decrypted.error)
  }, [decrypted.error])

  async function handleEditSubmit() {
    if (submitting) return
    if (!/^[0-9]{6}$/.test(newPwd)) {
      Toast.show('请输入6位数字动态码')
      return
    }
    if (newPwd === decrypted.code) {
      Toast.show('请输入跟当前动态码不一样的新的动态码')
      return
    }
    setSubmitting(true)
    try {
      const res = await updateLockPwd({ badge: badge as string, newPwd })
      if (res.code === 0) {
        setEditVisible(false)
        setNewPwd('')
        await pwd.refetch()
      } else {
        void Dialog.alert({ title: '错误', content: res.msg ?? res.message ?? '修改失败' })
      }
    } catch (error) {
      void Dialog.alert({ title: '错误', content: toUserMessage(error, '修改失败') })
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  const baseInfoFailed = baseInfo.isError || (baseInfo.isSuccess && baseInfo.data.code !== 0)

  return (
    <PageShell title="门锁动态码">
      {baseInfoFailed ? (
        <RetryBlock description="员工信息获取失败" onRetry={() => void baseInfo.refetch()} />
      ) : pwdFailed ? (
        <RetryBlock
          description={pwd.data?.message ?? pwd.data?.msg ?? '动态码获取失败'}
          onRetry={() => void pwd.refetch()}
        />
      ) : baseInfo.isPending || pwd.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col items-center gap-6 pt-10">
          <h2 className="text-[17px] font-bold">你的门锁动态码</h2>
          <p className="text-5xl font-bold tracking-[0.3em] text-brand" data-testid="lock-code">
            {decrypted.code || '******'}
          </p>

          <button
            type="button"
            disabled={!decrypted.code}
            onClick={() => setEditVisible(true)}
            className="mt-4 h-12 w-1/2 rounded-[14px] border-[1.5px] border-brand bg-white text-[15px] font-semibold text-brand active:bg-accent-soft disabled:border-light-gray disabled:text-weak"
          >
            修改动态码
          </button>
          <button
            type="button"
            onClick={() => router.push('/dorm/get-code')}
            className="text-[13px] font-semibold text-mid underline"
          >
            刷新动态码（人脸识别）
          </button>
        </div>
      )}

      <Dialog
        visible={editVisible}
        title="修改动态码"
        content={
          <input
            type="text"
            inputMode="numeric"
            maxLength={6}
            placeholder="请输入6位数字动态码"
            value={newPwd}
            onChange={(e) => setNewPwd(e.target.value)}
            className={SMS_INPUT_CLASS}
          />
        }
        closeOnAction={false}
        onClose={() => setEditVisible(false)}
        actions={[
          [
            { key: 'cancel', text: '取消', onClick: () => setEditVisible(false) },
            {
              key: 'ok',
              text: submitting ? '提交中…' : '确定',
              bold: true,
              disabled: submitting,
              onClick: () => void handleEditSubmit(),
            },
          ],
        ]}
      />
    </PageShell>
  )
}

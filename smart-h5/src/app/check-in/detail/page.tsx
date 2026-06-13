'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { getLockPwd } from '@/features/dorm/api'
import { getCheckInRecords } from '@/features/dorm-services/api'
import { decryptFromHex } from '@/lib/crypto/aes'

/** Feature codes 1/3 = success, 0/2/4 = failure (legacy icon rule). */
function featureOk(code: number | undefined): boolean {
  return code === 1 || code === 3
}

function FeatureRow({ label, ok, desc }: { label: string; ok: boolean; desc?: string }) {
  return (
    <div className="flex items-center gap-2 text-[13px]">
      <span aria-hidden>{ok ? '✅' : '❌'}</span>
      <span className="text-mid">{label}</span>
      <span>{desc}</span>
    </div>
  )
}

function safeDecrypt(cipher: string): string {
  try {
    return decryptFromHex(cipher)
  } catch {
    return ''
  }
}

/** Allocation result + door-lock identity-feature status. */
export default function CheckInDetailPage() {
  const authorized = useRequireAuth()

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined

  const records = useQuery({
    queryKey: ['check-in-records', badge],
    queryFn: () => getCheckInRecords(badge as string),
    enabled: authorized && badge !== undefined,
  })
  const pwd = useQuery({
    queryKey: ['lock-pwd', badge],
    queryFn: () => getLockPwd(badge as string),
    enabled: authorized && badge !== undefined,
  })

  if (!authorized) return null

  const rows = records.data?.code === 0 ? (records.data.data ?? []) : []
  const cipher = pwd.data?.code === 0 ? (pwd.data.data ?? '') : ''
  const plainCode = cipher ? safeDecrypt(cipher) : ''
  const recordsFailed = records.isError || (records.isSuccess && records.data.code !== 0)

  return (
    <PageShell title="宿舍申请">
      <SegmentTabs active="list" submitHref="/check-in" listHref="/check-in/detail" />

      <PullToRefresh
        onRefresh={async () => {
          const [recordsResult, pwdResult] = await Promise.all([records.refetch(), pwd.refetch()])
          Toast.show(recordsResult.error || pwdResult.error ? '更新失败' : '更新成功')
        }}
      >
        {recordsFailed ? (
          <ErrorBlock
            status="default"
            title="加载失败"
            description={records.data?.message ?? '分配记录获取失败'}
            className="py-8"
          />
        ) : badge === undefined || records.isPending ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无分配记录" />
        ) : (
          <div className="flex flex-col gap-3">
            {rows.map((record, index) => (
              <div key={index} className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
                <div className="flex justify-between border-b border-border-soft pb-2.5 text-[13px]">
                  <span className="text-mid">宿舍楼栋</span>
                  <span className="font-medium">{record.dormitoryName}</span>
                </div>
                <div className="flex justify-between pt-2 text-[13px]">
                  <span className="text-mid">房间号</span>
                  <span className="font-medium">{record.roomName}房间</span>
                </div>
                <div className="flex justify-between pt-2 text-[13px]">
                  <span className="text-mid">床位</span>
                  <span className="font-medium">{record.bedNumber}号床</span>
                </div>

                <div className="mt-3 flex flex-col gap-1.5 rounded-xl bg-surface p-3">
                  <p className="text-[13px] font-semibold">身份特征检查</p>
                  <FeatureRow
                    label="指纹"
                    ok={featureOk(record.lockPwd?.fingerprintCode)}
                    desc={record.lockPwd?.fingerprintDesc}
                  />
                  <FeatureRow
                    label="动态码"
                    ok={featureOk(record.lockPwd?.dynamicCode)}
                    desc={record.lockPwd?.dynamicCode === 3 ? '已录入' : record.lockPwd?.dynamicDesc}
                  />
                </div>

                {(record.lockPwd?.fingerprintCode ?? 0) !== 0 && plainCode && (
                  <p className="mt-3 text-center text-2xl font-bold tracking-[0.3em] text-brand" data-testid="checkin-lock-code">
                    {plainCode}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </PullToRefresh>
    </PageShell>
  )
}

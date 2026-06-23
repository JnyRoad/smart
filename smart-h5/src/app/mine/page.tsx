'use client'
import { useQuery } from '@tanstack/react-query'
import { Dialog, SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo, unbindWechat } from '@/features/employee/api'
import { AppTileIcon, type AppIconName } from '@/components/app-icon'
import { PageShell } from '@/components/page-shell'
import { clearSession } from '@/lib/auth/token'
import { getTenantConfig } from '@/lib/config/tenant'
import { BADGE_BINDING_PATH, redirectToWechatOAuth } from '@/lib/wechat/oauth'

function MenuItem({
  title,
  iconName,
  onClick,
}: {
  title: string
  iconName: AppIconName
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex min-h-14 w-full items-center gap-3.5 rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
    >
      <AppTileIcon name={iconName} />
      <span className="flex-1 text-[15px] font-bold">{title}</span>
      <span aria-hidden className="text-weak">
        ›
      </span>
    </button>
  )
}

export default function MinePage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const { parkName } = getTenantConfig()

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  useEffect(() => {
    if (baseInfo.isError) Toast.show(baseInfo.error.message || '网络错误')
  }, [baseInfo.isError, baseInfo.error])

  async function handleUnbind() {
    const confirmed = await Dialog.confirm({
      content: '是否确认解除微信绑定？',
      confirmText: '确定',
      cancelText: '取消',
    })
    if (!confirmed) return
    try {
      const res = await unbindWechat()
      if (res.code === 0) {
        Toast.show('解绑成功')
        clearSession()
        redirectToWechatOAuth(BADGE_BINDING_PATH)
      } else {
        Toast.show(res.message ?? '网络错误')
      }
    } catch (error) {
      Toast.show(toUserMessage(error, '网络错误'))
    }
  }

  if (!authorized) return null
  const info = baseInfo.data?.data

  return (
    <PageShell title="个人中心" onBack={() => router.push('/home')}>
      {baseInfo.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {/* 头部信息 → 个人信息页 */}
          <button
            type="button"
            onClick={() => router.push('/mine/detail')}
            className="flex items-center gap-3.5 rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
          >
            {info?.employeePhoto ? (
              <Image
                src={info.employeePhoto}
                alt=""
                width={56}
                height={56}
                unoptimized
                className="h-14 w-14 flex-none rounded-full object-cover"
              />
            ) : (
              <span className="grid h-14 w-14 flex-none place-items-center rounded-full bg-accent-soft text-xl font-bold text-brand">
                {info?.employeeName?.charAt(0) ?? '员'}
              </span>
            )}
            <span className="min-w-0 flex-1">
              <span className="flex items-center gap-2 text-[19px] font-bold">
                {info?.employeeName ?? '-'}
                {info?.employeeSex !== undefined && (
                  <span
                    aria-label={info.employeeSex === 1 ? '女' : '男'}
                    className={`grid h-5 w-5 place-items-center rounded-full text-[11px] text-white ${
                      info.employeeSex === 1 ? 'bg-[#e0578f]' : 'bg-[#2376d9]'
                    }`}
                  >
                    {info.employeeSex === 1 ? '女' : '男'}
                  </span>
                )}
              </span>
              <span className="mt-1 block text-sm text-mid">{info?.mobile ?? '-'}</span>
            </span>
            <span aria-hidden className="text-weak">
              ›
            </span>
          </button>

          {/* 园区横幅 */}
          <div className="flex items-center gap-2.5 rounded-2xl bg-[linear-gradient(120deg,#ec6c00,#f08a2c_60%,#f6a95c)] p-4 text-white shadow-[0_12px_28px_rgba(236,108,0,0.25)]">
            <AppTileIcon name="location" />
            <span className="text-[15px] font-bold">{parkName}</span>
          </div>

          {/* 三列信息 */}
          <div className="grid grid-cols-3 rounded-2xl bg-white px-1.5 py-4 text-center shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            {(
              [
                [info?.deptName, '所属部门'],
                [info?.jobName, '担任职务'],
                [info?.statusDes, '人员状态'],
              ] as const
            ).map(([value, label]) => (
              <div key={label}>
                <div className="text-sm font-bold">{value ?? '-'}</div>
                <div className="mt-1.5 text-xs text-weak">{label}</div>
              </div>
            ))}
          </div>

          {/* 菜单 */}
          <MenuItem
            title="我的宿舍"
            iconName="dorm"
            onClick={() => router.push('/dorm')}
          />
          <MenuItem
            title="帮助中心"
            iconName="help"
            onClick={() => router.push('/help')}
          />
          <MenuItem
            title="微信解绑"
            iconName="wechat"
            onClick={() => void handleUnbind()}
          />

          <button
            type="button"
            onClick={() => router.push('/home')}
            className="mt-3 h-12 w-full rounded-[14px] border-[1.5px] border-brand bg-white text-[15px] font-semibold text-brand active:bg-accent-soft"
          >
            返回
          </button>
        </div>
      )}
    </PageShell>
  )
}

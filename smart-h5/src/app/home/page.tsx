'use client'
import { useQuery } from '@tanstack/react-query'
import { Dialog, SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useRef } from 'react'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo, getEmployeeFullInfo } from '@/features/employee/api'
import {
  getBbsList,
  getDormExitApprovalCount,
  getGoodReleaseApprovalCount,
  getRepairsApprovalCount,
  getServiceModules,
  getWechatSignature,
  getWeather,
} from '@/features/home/api'
import { resolveModuleRoute } from '@/features/home/module-routes'
import { clearSession } from '@/lib/auth/token'
import { getTenantConfig } from '@/lib/config/tenant'
import { scanReleaseCode } from '@/lib/wechat/scan-release'
import { BADGE_BINDING_PATH, redirectToWechatOAuth } from '@/lib/wechat/oauth'

function formatBadge(count: number | undefined): string | null {
  if (!count || count <= 0) return null
  return count > 99 ? '99+' : String(count)
}

function ApprovalBadge({ count }: { count: number | undefined }) {
  const text = formatBadge(count)
  if (!text) return null
  return (
    <span className="absolute -top-1.5 -right-2 min-w-[18px] rounded-full bg-[#d83b36] px-1 text-center text-[11px] leading-[18px] font-semibold text-white">
      {text}
    </span>
  )
}

function GridTile({
  label,
  tip,
  badge,
  onClick,
}: {
  label: string
  tip?: string
  badge?: number
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex min-h-[76px] flex-col items-center justify-center gap-1.5 rounded-xl py-2 active:bg-surface"
    >
      <span className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-accent-soft text-brand">
        <ApprovalBadge count={badge} />
        <span aria-hidden className="text-lg">
          ▦
        </span>
      </span>
      <span className="text-center text-xs leading-tight text-ink">
        {label}
        {tip && <span className="block text-[10px] text-weak">{tip}</span>}
      </span>
    </button>
  )
}

export default function HomePage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const config = getTenantConfig()
  const resignedDialogShown = useRef(false)

  const fullInfo = useQuery({
    queryKey: ['employee', 'fullinfo'],
    queryFn: getEmployeeFullInfo,
    enabled: authorized,
  })
  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const weather = useQuery({
    queryKey: ['weather', config.weatherCity],
    queryFn: () => getWeather(config.weatherCity),
    enabled: authorized,
  })
  const latestBbs = useQuery({
    queryKey: ['bbs', 'latest'],
    queryFn: () => getBbsList({ current: 1, size: 1, parkId: config.parkId }),
    enabled: authorized,
  })
  const serviceModules = useQuery({
    queryKey: ['service-modules'],
    queryFn: getServiceModules,
    enabled: authorized,
  })
  const goodReleaseCount = useQuery({
    queryKey: ['approval-count', 'good-release'],
    queryFn: getGoodReleaseApprovalCount,
    enabled: authorized,
  })
  const repairsCount = useQuery({
    queryKey: ['approval-count', 'repairs'],
    queryFn: getRepairsApprovalCount,
    enabled: authorized,
  })
  const employee = baseInfo.data?.data
  const dormExitCount = useQuery({
    queryKey: ['approval-count', 'dorm-exit', employee?.isSecurityGuard],
    queryFn: () =>
      getDormExitApprovalCount({ parkId: config.parkId, isSecurityGuard: employee?.isSecurityGuard }),
    enabled: authorized && baseInfo.isSuccess,
  })

  // Resigned employee: force logout and restart OAuth at the binding page (legacy behavior).
  useEffect(() => {
    if (employee?.status === 0 && !resignedDialogShown.current) {
      resignedDialogShown.current = true
      void Dialog.alert({
        title: '提示',
        content: '该用户已离职，已为你自动退出登录',
        confirmText: '确定',
      }).then(() => {
        clearSession()
        redirectToWechatOAuth(BADGE_BINDING_PATH)
      })
    }
  }, [employee?.status])

  const anyError =
    fullInfo.isError || baseInfo.isError || serviceModules.isError || latestBbs.isError
  useEffect(() => {
    if (anyError) Toast.show('数据加载失败')
  }, [anyError])

  if (!authorized) return null

  const loading = fullInfo.isPending || serviceModules.isPending
  const weatherInfo = weather.data?.data
  const latest = latestBbs.data?.data?.records?.[0]
  const modules = serviceModules.data?.data?.serviceModule ?? []

  async function handleScanRelease() {
    try {
      const route = await scanReleaseCode({
        appId: config.wxAppId,
        getSignature: async (url) => {
          const res = await getWechatSignature({ url })
          if (res.code !== 0 || !res.data) throw new Error(res.message || '微信配置失败')
          return res.data
        },
      })
      router.push(route)
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '扫码失败')
    }
  }

  const approvalEntries = [
    {
      key: 'good-release-live',
      label: '宿舍物品放行审批',
      href: '/backlog/release-live',
      badge: goodReleaseCount.data?.data?.total,
    },
    {
      key: 'dorm-repairs',
      label: '园区报修审批',
      href: '/backlog/repairs',
      badge: repairsCount.data?.data?.total,
    },
    {
      key: 'dorm-exit',
      label: '退宿审批',
      href: '/backlog/dorm-exit',
      badge: dormExitCount.data?.data?.total,
    },
  ]

  return (
    <div className="min-h-dvh pb-[max(24px,env(safe-area-inset-bottom))]">
      {/* 顶部品牌区 */}
      <div className="bg-[linear-gradient(180deg,#fff1e3_0,rgba(255,241,227,0.4)_70%,transparent_100%)] px-4.5 pt-[max(14px,env(safe-area-inset-top))] pb-6">
        <div className="flex min-h-[42px] items-center text-base font-bold">裕同智慧园区</div>
        <h1 className="mt-2 text-2xl font-bold text-ink">{config.parkName}</h1>
        {weatherInfo?.city && (
          <p className="mt-1 text-[13px] text-mid">
            {weatherInfo.city} {weatherInfo.forecast?.[0]?.type} {weatherInfo.wendu}°C{' '}
            {weatherInfo.forecast?.[0]?.fengxiang}
          </p>
        )}
        <button
          type="button"
          onClick={() => router.push('/mine')}
          className="mt-3 inline-flex min-h-[36px] items-center gap-2 rounded-full bg-brand px-4 text-sm font-semibold text-white active:bg-[#d95f00]"
        >
          <span>{fullInfo.data?.data?.employeeId ?? '—'}</span>
          <span>{fullInfo.data?.data?.employeeName ?? ''}</span>
          <span aria-hidden>›</span>
        </button>
      </div>

      <div className="flex flex-col gap-3 px-3">
        {/* 公告条 */}
        <Link
          href="/home/bbs"
          className="flex items-center gap-2.5 rounded-2xl bg-white px-4 py-3 shadow-[0_16px_40px_rgba(89,87,87,0.10)]"
        >
          <span aria-hidden className="text-brand">
            📢
          </span>
          <span className="min-w-0 flex-1 truncate text-sm text-ink">
            {latest?.bbsTitle ?? '暂无公告'}
          </span>
          <span aria-hidden className="text-weak">
            ›
          </span>
        </Link>

        {/* 审批宫格（固定 3 项，带角标） */}
        <div className="grid grid-cols-4 rounded-2xl bg-white p-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
          {approvalEntries.map((entry) => (
            <GridTile
              key={entry.key}
              label={entry.label}
              badge={entry.badge}
              onClick={() => router.push(entry.href)}
            />
          ))}
        </div>

        {/* 园区服务宫格（后端动态下发） */}
        <div className="rounded-2xl bg-white p-3 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
          <span className="border-b-2 border-brand pb-1 text-[15px] font-bold text-ink">
            园区服务
          </span>
          {loading ? (
            <div className="flex justify-center py-8">
              <SpinLoading color="primary" />
            </div>
          ) : (
            <div className="mt-2 grid grid-cols-4">
              {modules.map((m, index) => {
                const route = resolveModuleRoute(m.moduleUrl)
                const isScan = m.moduleName === '扫码放行'
                return (
                  <button
                    key={`${m.moduleName}-${index}`}
                    type="button"
                    onClick={() => {
                      if (route) {
                        router.push(route)
                      } else if (isScan) {
                        void handleScanRelease()
                      }
                    }}
                    className="flex min-h-[76px] flex-col items-center justify-center gap-1.5 rounded-xl py-2 active:bg-surface"
                  >
                    <span className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-xl bg-accent-soft">
                      {m.moduleIcon ? (
                        <Image
                          src={m.moduleIcon}
                          alt=""
                          width={40}
                          height={40}
                          unoptimized
                          className="h-10 w-10 object-cover"
                        />
                      ) : (
                        <span aria-hidden className="text-brand">
                          ▦
                        </span>
                      )}
                    </span>
                    <span className="text-center text-xs leading-tight text-ink">
                      {m.moduleName}
                    </span>
                  </button>
                )
              })}
              {modules.length === 0 && (
                <p className="col-span-4 py-6 text-center text-sm text-weak">暂无服务</p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

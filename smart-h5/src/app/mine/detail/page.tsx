'use client'
import { useQuery } from '@tanstack/react-query'
import { SpinLoading, Toast } from 'antd-mobile'
import { useEffect } from 'react'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo, type EmployeeBaseInfo } from '@/features/employee/api'
import { PageShell } from '@/components/page-shell'

type FieldRow = readonly [label: string, key: keyof EmployeeBaseInfo]

/** Read-only employee profile, three field groups per the legacy layout. */
const FIELD_GROUPS: readonly FieldRow[][] = [
  [
    ['姓名', 'employeeName'],
    ['工号', 'employeeBadge'],
    ['公司', 'buName'],
  ],
  [
    ['部门', 'deptName'],
    ['职务', 'jobName'],
    ['职层', 'jcheName'],
    ['福利层次', 'welfareLevel'],
  ],
  [
    ['人员状态', 'statusDes'],
    ['人员类型', 'empTypeDes'],
    ['员工性质', 'empAttribute'],
  ],
]

export default function MineDetailPage() {
  const authorized = useRequireAuth()

  // Legacy read the home page's cached store and went blank on direct entry;
  // fetching in-page fixes that.
  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  useEffect(() => {
    if (baseInfo.isError) Toast.show(baseInfo.error.message || '网络错误')
  }, [baseInfo.isError, baseInfo.error])

  if (!authorized) return null
  const info = baseInfo.data?.data

  return (
    <PageShell title="个人信息">
      {baseInfo.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {FIELD_GROUPS.map((group, groupIndex) => (
            <div
              key={groupIndex}
              className="rounded-2xl bg-white px-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]"
            >
              {group.map(([label, key]) => {
                const value = info?.[key]
                return (
                  <div
                    key={label}
                    className="flex items-center justify-between border-b border-border-soft py-3.5 last:border-b-0"
                  >
                    <span className="text-sm text-mid">{label}</span>
                    <span className="text-sm font-medium text-ink">
                      {value === undefined || value === null || value === '' ? '-' : String(value)}
                    </span>
                  </div>
                )
              })}
            </div>
          ))}
        </div>
      )}
    </PageShell>
  )
}

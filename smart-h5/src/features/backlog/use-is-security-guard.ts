import { useQuery } from '@tanstack/react-query'
import { getEmployeeBaseInfo } from '@/features/employee/api'

/** 旧注释事实：isSecurityGuard 0 是保安 / 1 否（搜索按钮仅保安显示）。 */
export function useIsSecurityGuard(enabled: boolean): boolean {
  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled,
  })
  return baseInfo.data?.code === 0 && baseInfo.data.data?.isSecurityGuard === 0
}

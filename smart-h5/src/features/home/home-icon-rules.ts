export type HomeIconName =
  | 'approval'
  | 'checkIn'
  | 'dorm'
  | 'exit'
  | 'fallback'
  | 'release'
  | 'repair'
  | 'returnFactory'
  | 'scan'
  | 'workRelease'

const APPROVAL_ICON_BY_KEY: Record<string, HomeIconName> = {
  'good-release-live': 'release',
  'dorm-repairs': 'repair',
  'dorm-exit': 'exit',
}

const SERVICE_ICON_BY_MODULE_URL: Record<string, HomeIconName> = {
  '/approve': 'approval',
  '/articlesrelease': 'workRelease',
  '/releaseGoods': 'release',
  '/xuchang/checkIn': 'checkIn',
  '/xuchang/dormExit': 'exit',
  '/dormRepairs': 'repair',
  '/dorm': 'dorm',
  '/returnFactory': 'returnFactory',
}

export function resolveApprovalIconName(key: string): HomeIconName {
  return APPROVAL_ICON_BY_KEY[key] ?? 'fallback'
}

export function resolveServiceIconName(
  moduleUrl: string | undefined,
  moduleName: string | undefined,
): HomeIconName {
  if (moduleName === '扫码放行') return 'scan'
  if (!moduleUrl) return 'fallback'
  return SERVICE_ICON_BY_MODULE_URL[moduleUrl] ?? 'fallback'
}

/**
 * Maps the backend-issued `moduleUrl` of 园区服务 modules to the new clean
 * routes (the legacy values mix bare paths and /xuchang/ hash paths).
 */
const MODULE_ROUTE_MAP: Record<string, string> = {
  '/dormRepairs': '/dorm-repairs',
  '/xuchang/checkIn': '/check-in',
  '/xuchang/dormExit': '/dorm-exit',
  '/approve': '/backlog',
  '/releaseGoods': '/good-release/live',
  '/articlesrelease': '/good-release/work',
  '/returnFactory': '/return-factory',
  '/dorm': '/dorm',
}

export function resolveModuleRoute(moduleUrl: string | undefined): string | null {
  if (!moduleUrl) return null
  return MODULE_ROUTE_MAP[moduleUrl] ?? null
}

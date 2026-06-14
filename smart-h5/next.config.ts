import type { NextConfig } from 'next'

/**
 * API proxy to the existing gateway. Development keeps the legacy default
 * remote target; standalone Docker production sets API_PROXY_TARGET at build
 * time so same-origin frontend calls still reach smart-gateway. Production
 * deployments without API_PROXY_TARGET can keep using an external nginx route.
 */
// NOTE: the gateway's 'visitor' module prefix is intentionally NOT proxied:
// it collides with this app's /visitor/* pages (afterFiles rewrites run
// before dynamic routes, so /visitor/records/[id] would be swallowed), and
// the new frontend calls no gateway API under /visitor — its visitor APIs
// live under /platform, /app and /algorithm. Production nginx must likewise
// route /visitor/* to this app, not to smart-gateway.
const GATEWAY_MODULES = ['auth', 'app', 'platform', 'admin', 'algorithm', 'workbench', 'file']

const nextConfig: NextConfig = {
  output: 'standalone',
  // Pin the workspace root: this repo can be checked out as a nested git
  // worktree whose parent directory also holds a pnpm lockfile; root
  // auto-detection would then watch the entire parent tree (extreme CPU).
  turbopack: { root: __dirname },
  async rewrites() {
    const configuredTarget = process.env.API_PROXY_TARGET
    if (process.env.NODE_ENV !== 'development' && !configuredTarget) return []
    const target = configuredTarget ?? 'https://xuchang.szyuto.com'
    return GATEWAY_MODULES.map((module) => ({
      source: `/${module}/:path*`,
      destination: `${target}/${module}/:path*`,
    }))
  },
}

export default nextConfig

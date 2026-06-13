import type { NextConfig } from 'next'

/**
 * Dev-only API proxy to the existing gateway (equivalent to the legacy
 * proxy.js). In production nginx routes these prefixes directly to
 * smart-gateway; the Next server never forwards API traffic.
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
    if (process.env.NODE_ENV !== 'development') return []
    const target = process.env.API_PROXY_TARGET ?? 'https://xuchang.szyuto.com'
    return GATEWAY_MODULES.map((module) => ({
      source: `/${module}/:path*`,
      destination: `${target}/${module}/:path*`,
    }))
  },
}

export default nextConfig

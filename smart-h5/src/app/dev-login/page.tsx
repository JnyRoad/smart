'use client'
import { Toast } from 'antd-mobile'
import { notFound, useRouter } from 'next/navigation'
import { useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { extractToken } from '@/features/auth/dev-token'
import { clearSession, hasValidToken, saveSession } from '@/lib/auth/token'
import { useMounted } from '@/lib/use-mounted'

/**
 * 开发环境专用调试登录：粘贴指定工号的 access_token 一键注入，
 * 以该人员身份调真实网关（dev 代理）。生产构建恒 404。
 */
export default function DevLoginPage() {
  // NODE_ENV 构建期内联：production 构建里这一支恒为真，页面即 404。
  if (process.env.NODE_ENV !== 'development') notFound()

  return <DevLoginInner />
}

function DevLoginInner() {
  const mounted = useMounted()
  const router = useRouter()
  const [input, setInput] = useState('')

  // 外壳直接 SSR（整页等挂载会让首屏 HTML 全空，JS 稍慢就长时间白屏）；
  // 只有读 localStorage 的登录状态在挂载后再判定。
  const loggedIn = mounted ? hasValidToken() : null

  function inject() {
    const token = extractToken(input)
    if (!token) {
      Toast.show('请先粘贴 token')
      return
    }
    saveSession({ accessToken: token })
    router.push('/home')
  }

  return (
    <PageShell title="开发调试登录">
      <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <p className="text-[13px] leading-6 text-mid">
          仅开发环境可用。粘贴目标工号的 access_token（支持原始字符串或 xc-access_token
          信封 JSON），注入后即以该人员身份访问真实网关。
        </p>
        <p className="mt-2 text-[13px] font-semibold">
          当前状态：
          {loggedIn === null ? (
            <span className="text-weak">检测中…</span>
          ) : loggedIn ? (
            <span className="text-[#16a673]">已注入 token</span>
          ) : (
            <span className="text-mid">未登录</span>
          )}
        </p>

        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder='粘贴 token 或 {"dataType":"string","content":"...","datetime":...}'
          rows={5}
          className="mt-3 w-full rounded-xl border border-border-soft p-3 font-mono text-xs"
          data-testid="dev-token-input"
        />

        <button
          type="button"
          onClick={inject}
          className="mt-3 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          注入并进入
        </button>
        {loggedIn && (
          <button
            type="button"
            onClick={() => {
              clearSession()
              router.refresh()
            }}
            className="mt-2 flex h-11 w-full items-center justify-center rounded-[14px] border border-border-soft text-sm font-semibold text-mid"
          >
            清除当前 token（换人调试）
          </button>
        )}

        <div className="mt-4 rounded-xl bg-surface p-3 text-xs leading-5 text-mid">
          <p className="font-semibold">不抓包取 token（电脑端，全程点选）：</p>
          <p>1. PC 安装「微信开发者工具」→ 公众号网页项目 → 打开旧版 H5 地址并完成登录（用哪个微信登录，身份就是谁；需要特定身份时，请用**测试工号**走登录页绑定流程，勿绑定他人在职工号）。</p>
          <p>2. 调试器 → Application → Local Storage → 选中 xc-access_token → 右键复制值。</p>
          <p>3. 粘贴到上方输入框 → 注入并进入。</p>
          <p className="mt-1 font-semibold text-[#d83b36]">
            注意：优先使用测试工号；token 等同登录态，勿外传、勿提交到仓库。
          </p>
        </div>
      </div>
    </PageShell>
  )
}

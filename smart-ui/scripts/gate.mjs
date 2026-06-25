// 本地质量门禁：开发完一键跑全套安全网，确认无回归。
// 不依赖 CI / 不联网：串起 vitest 单测 + lint 基线守卫 + 一组静态契约检查，
// 聚合执行（某步失败也继续跑完其余），最后汇总通过/失败清单，任一红则退出码 1。
// 用法：
//   pnpm gate            # 跑全部步骤，失败退出码 1
// 运行环境：开发者本机（mac / Linux）。spawnSync 用 shell:false 直调，不适配 Windows
// 控制台；许昌那台 Windows 打印机机器只跑 FileReceiver jar，与本门禁无关。
import { spawnSync } from 'node:child_process'
import { resolve } from 'node:path'

const ROOT = resolve(import.meta.dirname, '..')

// 门禁步骤清单：name 用于汇总展示，cmd 是 [可执行文件, ...参数]。
// 统一用 `node scripts/xxx.js` 直调（不走 pnpm script 层）：清单自包含、少一层 pnpm 启动开销。
// vitest 步骤会把 gate.test.mjs 自己也收集进去，二者不递归——该测试只 import 纯函数，不触发 spawnExec/main。
export const STEPS = [
  { name: 'unit (vitest)', cmd: ['pnpm', 'exec', 'vitest', 'run'] },
  { name: 'lint-baseline', cmd: ['node', 'scripts/check-lint-baseline.mjs'] },
  { name: 'platform-router-fingerprint', cmd: ['node', 'scripts/check-platform-router-fingerprint.mjs'] },
  { name: 'admin-search', cmd: ['node', 'scripts/check-admin-search.js'] },
  { name: 'bundle', cmd: ['node', 'scripts/check-bundle-optimization.js'] },
  { name: 'isc-card-fast-add-ui', cmd: ['node', 'scripts/check-isc-card-fast-add-ui.js'] },
  { name: 'isc-card-ui', cmd: ['node', 'scripts/check-isc-card-ui.js'] },
  { name: 'isc-device-id-readonly', cmd: ['node', 'scripts/check-isc-device-id-readonly.js'] },
  { name: 'records-contracts', cmd: ['node', 'scripts/check-records-contracts.js'] }
]

// 真实执行器：跑一条命令，stdio 直通（实时可见日志），返回是否成功与耗时。
function spawnExec (cmd) {
  const start = Date.now()
  const r = spawnSync(cmd[0], cmd.slice(1), { cwd: ROOT, stdio: 'inherit' })
  // 命令拉不起来（如 ENOENT）时 r.status 为 null、r.error 有值，显式区分"断言失败"与"启动失败"
  if (r.error) console.error(`步骤启动失败：${cmd.join(' ')} —— ${r.error.message}`)
  return { ok: r.status === 0 && !r.error, ms: Date.now() - start }
}

// 纯编排：按序跑完所有步骤（聚合，不快速失败），返回逐步结果与总体是否通过。
// exec 可注入，便于单测；签名 exec(cmd, name) => { ok, ms }。
export function runSteps (steps, exec) {
  const results = []
  for (const step of steps) {
    const { ok, ms } = exec(step.cmd, step.name)
    results.push({ name: step.name, ok, ms })
  }
  return { results, allPassed: results.every((r) => r.ok) }
}

// 纯函数：把结果渲染成汇总文本，通过标 ✓、失败标 ✗。
export function formatSummary (results) {
  const lines = results.map((r) => `  ${r.ok ? '✓' : '✗'} ${r.name} (${r.ms}ms)`)
  const failed = results.filter((r) => !r.ok).map((r) => r.name)
  const tail = failed.length
    ? `\n门禁未通过，失败步骤：${failed.join(', ')}`
    : '\n门禁通过：全部步骤绿。'
  return ['质量门禁结果：', ...lines].join('\n') + tail
}

function main () {
  console.log('开始本地质量门禁……\n')
  const { results, allPassed } = runSteps(STEPS, spawnExec)
  console.log('\n' + formatSummary(results))
  process.exit(allPassed ? 0 : 1)
}

// 仅作为 CLI 运行时执行 main（被测试 import 时不执行）
if (process.argv[1] && process.argv[1].endsWith('gate.mjs')) main()

// lint 基线守卫：对比逐文件 warning 数，只在"某文件增加"或"新文件带 warning"时失败。
// 用法：
//   node scripts/check-lint-baseline.mjs            # 比对，违规则退出码 1
//   node scripts/check-lint-baseline.mjs --update    # 重新生成基线 JSON
import { execFileSync } from 'node:child_process'
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { relative, resolve } from 'node:path'

const ROOT = resolve(import.meta.dirname, '..')
const BASELINE_PATH = resolve(ROOT, 'docs/lint-baseline.json')

// 跑 eslint 输出 JSON，聚合成 { 相对路径: warningCount }
export function collectWarnings () {
  let raw
  try {
    raw = execFileSync(
      'pnpm',
      ['exec', 'eslint', '--ext', '.js,.vue', 'src', '-f', 'json'],
      { cwd: ROOT, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 }
    )
  } catch (e) {
    // eslint 有 warning/error 时退出码非 0，但 stdout 仍是有效 JSON
    raw = e.stdout
    if (!raw) throw e
  }
  const report = JSON.parse(raw)
  const counts = {}
  for (const f of report) {
    // 统一用正斜杠的相对路径作为 key，保证跨平台基线一致、可比对
    if (f.warningCount > 0) counts[relative(ROOT, f.filePath).split('\\').join('/')] = f.warningCount
  }
  return counts
}

// 纯函数：对比基线与当前，返回违规清单（可单测）
export function diffAgainstBaseline (baseline, current) {
  const violations = []
  for (const file of Object.keys(current)) {
    const base = baseline[file] || 0
    if (current[file] > base) {
      violations.push({ file, baseline: base, current: current[file] })
    }
  }
  return violations
}

function main () {
  const update = process.argv.includes('--update')
  const current = collectWarnings()
  if (update) {
    const sorted = Object.fromEntries(Object.keys(current).sort().map((k) => [k, current[k]]))
    writeFileSync(BASELINE_PATH, JSON.stringify(sorted, null, 2) + '\n')
    console.log(`baseline updated: ${Object.keys(sorted).length} files`)
    return
  }
  if (!existsSync(BASELINE_PATH)) {
    console.error('缺少 docs/lint-baseline.json，先运行：node scripts/check-lint-baseline.mjs --update')
    process.exit(1)
  }
  const baseline = JSON.parse(readFileSync(BASELINE_PATH, 'utf8'))
  const violations = diffAgainstBaseline(baseline, current)
  if (violations.length) {
    console.error('lint 基线被突破（只允许减少，不允许新增）：')
    for (const v of violations) console.error(`  ${v.file}: ${v.baseline} → ${v.current}`)
    process.exit(1)
  }
  console.log('lint 基线 OK：无文件新增 warning')
}

// 仅作为 CLI 运行时执行 main（被测试 import 时不执行）
if (process.argv[1] && process.argv[1].endsWith('check-lint-baseline.mjs')) main()

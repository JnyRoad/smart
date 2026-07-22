import { readdir, readFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

export const FORBIDDEN_ANONYMOUS_PATTERNS = new Set([
  '/**',
  '/staff/**',
  '/articlesrelease/**',
  '/api/**',
])

/**
 * 返回匿名白名单中禁止出现的精确路径，避免把正常的精确回调路径误报为风险项。
 */
export function findForbiddenIgnoreUrls(urls) {
  if (!Array.isArray(urls) || urls.some((url) => typeof url !== 'string')) {
    throw new TypeError('ignore-urls must be an array of strings')
  }

  return urls.filter((url) => FORBIDDEN_ANONYMOUS_PATTERNS.has(url))
}

function getIndentation(line) {
  return line.length - line.trimStart().length
}

function getYamlListValue(line) {
  const matched = line.match(/^\s*-\s*(?:"([^"]+)"|'([^']+)'|([^\s#]+))/)
  if (!matched) {
    return null
  }

  return matched[1] || matched[2] || matched[3]
}

function getYamlFlowListValues(line) {
  const matched = line.match(/^ignore-urls\s*:\s*\[(.*)]\s*(?:#.*)?$/)
  if (!matched) {
    return null
  }

  return matched[1]
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean)
    .map((value) => value.replace(/^(?:"([^"]*)"|'([^']*)')$/, '$1$2'))
}

/**
 * 扫描本地 Nacos YAML 基线，只记录 Data ID、行号和命中的匿名路径，避免输出配置中的秘密。
 */
export async function scanConfigDirectory(directory) {
  const entries = await readdir(directory, { withFileTypes: true })
  const findings = []

  for (const entry of entries.filter((candidate) => candidate.isFile() && /\.ya?ml$/i.test(candidate.name)).sort((left, right) => left.name.localeCompare(right.name))) {
    const fileName = entry.name
    const lines = (await readFile(path.join(directory, fileName), 'utf8')).split(/\r?\n/)
    let ignoreUrlsIndentation = null

    lines.forEach((line, index) => {
      const trimmed = line.trim()
      if (trimmed.startsWith('#') || trimmed === '') {
        return
      }

      const indentation = getIndentation(line)
      const flowListValues = getYamlFlowListValues(trimmed)
      if (flowListValues !== null) {
        for (const ignoredPath of findForbiddenIgnoreUrls(flowListValues)) {
          findings.push({
            dataId: fileName,
            fileName,
            line: index + 1,
            path: ignoredPath,
          })
        }
        ignoreUrlsIndentation = null
        return
      }

      if (/^ignore-urls\s*:(?:\s+#.*)?$/.test(trimmed)) {
        ignoreUrlsIndentation = indentation
        return
      }

      if (ignoreUrlsIndentation === null) {
        return
      }

      if (indentation <= ignoreUrlsIndentation) {
        ignoreUrlsIndentation = null
        return
      }

      const ignoredPath = getYamlListValue(line)
      if (ignoredPath && FORBIDDEN_ANONYMOUS_PATTERNS.has(ignoredPath)) {
        findings.push({
          dataId: fileName,
          fileName,
          line: index + 1,
          path: ignoredPath,
        })
      }
    })
  }

  return findings
}

async function main() {
  const directory = process.argv[2]
  if (!directory) {
    console.error('Usage: node scripts/security/check-nacos-ignore-urls.mjs <nacos-config-directory>')
    process.exitCode = 2
    return
  }

  const findings = await scanConfigDirectory(directory)
  if (findings.length === 0) {
    console.log('No forbidden anonymous ignore-urls patterns found.')
    return
  }

  for (const finding of findings) {
    console.error(`${finding.dataId}:${finding.line} ignore-urls ${finding.path}`)
  }
  process.exitCode = 1
}

if (process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])) {
  main().catch((error) => {
    console.error(`Failed to scan Nacos config directory: ${error.message}`)
    process.exitCode = 2
  })
}

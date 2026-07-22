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

function getYamlFlowScalarValue(value) {
  const trimmedValue = value.trim()
  if (!trimmedValue) {
    return null
  }

  const quotedValue = trimmedValue.match(/^(?:"([^"]*)"|'([^']*)')$/)
  if (quotedValue) {
    return quotedValue[1] || quotedValue[2]
  }

  if (/^[^\s#]+$/.test(trimmedValue)) {
    return trimmedValue
  }

  throw new Error('Unsupported flow ignore-urls value')
}

function getYamlFlowListEntries(lines, startIndex) {
  const matched = lines[startIndex].trim().match(/^ignore-urls\s*:\s*\[(.*)$/)
  if (!matched) {
    return null
  }

  const entries = []
  let content = matched[1]

  for (let index = startIndex; index < lines.length; index += 1) {
    if (!content.trim().startsWith('#')) {
      const closingBracketIndex = content.indexOf(']')
      const valuesText = closingBracketIndex === -1 ? content : content.slice(0, closingBracketIndex)

      for (const rawValue of valuesText.split(',')) {
        const ignoredPath = getYamlFlowScalarValue(rawValue)
        if (ignoredPath) {
          entries.push({ line: index + 1, path: ignoredPath })
        }
      }

      if (closingBracketIndex !== -1) {
        return { endIndex: index, entries }
      }
    }

    content = lines[index + 1] || ''
  }

  throw new Error('Unterminated flow ignore-urls list')
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

    for (let index = 0; index < lines.length; index += 1) {
      const line = lines[index]
      const trimmed = line.trim()
      if (trimmed.startsWith('#') || trimmed === '') {
        continue
      }

      const indentation = getIndentation(line)
      let flowList
      try {
        flowList = getYamlFlowListEntries(lines, index)
      } catch (error) {
        throw new Error(`Unsupported ignore-urls syntax in ${fileName}:${index + 1}`)
      }
      if (flowList !== null) {
        for (const entry of flowList.entries) {
          if (!FORBIDDEN_ANONYMOUS_PATTERNS.has(entry.path)) {
            continue
          }
          findings.push({
            dataId: fileName,
            fileName,
            line: entry.line,
            path: entry.path,
          })
        }
        ignoreUrlsIndentation = null
        index = flowList.endIndex
        continue
      }

      if (/^ignore-urls\s*:(?:\s+#.*)?$/.test(trimmed)) {
        ignoreUrlsIndentation = indentation
        continue
      }

      if (ignoreUrlsIndentation === null) {
        continue
      }

      if (indentation <= ignoreUrlsIndentation) {
        ignoreUrlsIndentation = null
        continue
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
    }
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

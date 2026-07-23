import { readdir, readFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { LineCounter, isMap, isScalar, isSeq, parseDocument } from 'yaml'

export const FORBIDDEN_ANONYMOUS_PATTERNS = new Set([
  '/**',
  '/staff/**',
  '/articlesrelease/**',
  '/api/**',
])

/**
 * 匿名白名单中的业务通配会让后续新增端点自动绕过认证；仅健康探针允许使用精确的 actuator 通配。
 */
export function isForbiddenBusinessWildcard(url) {
  return typeof url === 'string' && url.includes('*') && url !== '/actuator/**'
}

/**
 * 返回匿名白名单中禁止出现的精确路径，避免把正常的精确回调路径误报为风险项。
 */
export function findForbiddenIgnoreUrls(urls) {
  if (!Array.isArray(urls) || urls.some((url) => typeof url !== 'string')) {
    throw new TypeError('ignore-urls must be an array of strings')
  }

  return urls.filter((url) => FORBIDDEN_ANONYMOUS_PATTERNS.has(url) || isForbiddenBusinessWildcard(url))
}

function getLineNumber(node, lineCounter) {
  const startOffset = node?.range?.[0]
  return Number.isInteger(startOffset) ? lineCounter.linePos(startOffset).line : null
}

function createUnsupportedValueError(fileName, node, lineCounter) {
  const line = getLineNumber(node, lineCounter)
  return new Error(`Unsupported ignore-urls value in ${fileName}${line ? `:${line}` : ''}`)
}

function getObjectPath(value, pathSegments) {
  let currentValue = value
  for (const pathSegment of pathSegments) {
    if (!currentValue || typeof currentValue !== 'object' || Array.isArray(currentValue)) {
      return undefined
    }
    currentValue = currentValue[pathSegment]
  }
  return currentValue
}

function sequenceMatchesPaths(node, paths) {
  return isSeq(node)
    && node.items.length === paths.length
    && node.items.every((item, index) => isScalar(item) && item.value === paths[index])
}

function findMatchingIgnoreUrlsSequences(node, paths, matches) {
  if (isMap(node)) {
    for (const pair of node.items) {
      if (isScalar(pair.key) && pair.key.value === 'ignore-urls' && sequenceMatchesPaths(pair.value, paths)) {
        matches.push(pair.value)
      }
      findMatchingIgnoreUrlsSequences(pair.value, paths, matches)
    }
    return
  }

  if (isSeq(node)) {
    for (const item of node.items) {
      findMatchingIgnoreUrlsSequences(item, paths, matches)
    }
  }
}

function findSourceIgnoreUrlsSequence(document, paths) {
  const directNode = document.getIn(['security', 'oauth2', 'client', 'ignore-urls'], true)
  if (sequenceMatchesPaths(directNode, paths)) {
    return directNode
  }

  const matches = []
  findMatchingIgnoreUrlsSequences(document.contents, paths, matches)
  return matches.length === 1 ? matches[0] : null
}

function getIgnoreUrlsEntries(content, fileName) {
  const lineCounter = new LineCounter()
  const document = parseDocument(content, {
    lineCounter,
    merge: true,
    prettyErrors: false,
    strict: true,
    uniqueKeys: true,
  })
  if (document.errors.length > 0) {
    throw new Error(`Invalid YAML in ${fileName}`)
  }

  let semanticConfig
  try {
    semanticConfig = document.toJS({ maxAliasCount: 100 })
  } catch (error) {
    throw createUnsupportedValueError(fileName, null, lineCounter)
  }

  const paths = getObjectPath(semanticConfig, ['security', 'oauth2', 'client', 'ignore-urls'])
  if (paths === undefined) {
    return []
  }

  const directNode = document.getIn(['security', 'oauth2', 'client', 'ignore-urls'], true)
  if (!Array.isArray(paths) || paths.some((pathValue) => typeof pathValue !== 'string')) {
    throw createUnsupportedValueError(fileName, directNode, lineCounter)
  }

  const sourceSequence = findSourceIgnoreUrlsSequence(document, paths)
  if (!sourceSequence) {
    throw createUnsupportedValueError(fileName, directNode, lineCounter)
  }

  return paths.map((pathValue, index) => {
    const line = getLineNumber(sourceSequence.items[index], lineCounter)
    if (!line) {
      throw createUnsupportedValueError(fileName, sourceSequence.items[index], lineCounter)
    }

    return { line, path: pathValue }
  })
}

/**
 * 扫描本地 Nacos YAML 基线，只记录 Data ID、行号和命中的匿名路径，避免输出配置中的秘密。
 */
export async function scanConfigDirectory(directory) {
  const directoryEntries = await readdir(directory, { withFileTypes: true })
  const findings = []
  const yamlFiles = directoryEntries
    .filter((entry) => entry.isFile() && /\.ya?ml$/i.test(entry.name))
    .sort((left, right) => left.name.localeCompare(right.name))

  for (const entry of yamlFiles) {
    const fileName = entry.name
    const content = await readFile(path.join(directory, fileName), 'utf8')
    const ignoreUrlsEntries = getIgnoreUrlsEntries(content, fileName)

    for (const ignoreUrlsEntry of ignoreUrlsEntries) {
      if (!FORBIDDEN_ANONYMOUS_PATTERNS.has(ignoreUrlsEntry.path)
        && !isForbiddenBusinessWildcard(ignoreUrlsEntry.path)) {
        continue
      }

      findings.push({
        dataId: fileName,
        fileName,
        line: ignoreUrlsEntry.line,
        path: ignoreUrlsEntry.path,
      })
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
    console.error(error.message)
    process.exitCode = 2
  })
}

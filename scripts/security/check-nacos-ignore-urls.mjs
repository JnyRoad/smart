import { readdir, readFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { LineCounter, isMap, isScalar, isSeq, parseDocument } from 'yaml'

/**
 * 允许表来源是 docker/nacos/config/dev 中已审计的 Data ID 基线。没有跨 Data ID 的全局 permit；新增匿名路由必须同时更新此表并完成安全审查。
 */
export const ALLOWED_IGNORE_URLS_BY_DATA_ID = new Map([
  ['smart-app.yml', new Set([
    '/guide/welcome',
    '/sms/visitor/send',
    '/sms/visitor/verify',
    '/sms/login/send',
    '/password/mobile/query',
    '/password/sms/send',
    '/password/verify',
    '/password/verify/face',
    '/password/update',
    '/park/location/auto',
    '/park/list',
    '/wechat/xc/banging/badge',
    '/wechat/visit/checkBlackVisitor',
    '/wechat/visit/checkFace',
    '/agreement/service',
    '/setting/version/check',
    '/yht/user/badge',
  ])],
  ['smart-platform.yml', new Set([
    '/actuator/health',
    '/admittance/apply/get/openId',
    '/admittance/visitor-face/capability',
    '/admittance/visitor-face/crop',
    '/admittance/visitor-action/capability',
    '/admittance/visitor-entry/receptionist',
    '/admittance/visitor-entry/precheck',
    '/admittance/visitor-entry/apply',
    '/admittance/visitor-entry/options/cause',
    '/admittance/visitor-entry/options/vehicle-cert',
    '/admittance/visitor-entry/options/area-options',
    '/admittance/apply/app/listMyApply',
    '/admittance/apply/app/applyDetail',
    '/admittance/apply/app/approvalProgress',
    '/admittance/apply/app/passCode',
    '/admittance/visitor-truck/verify-sms',
    '/admittance/visitor-truck/options/cause',
    '/admittance/visitor-truck/apply',
    '/regist/save/identification',
    '/regist/face/crop',
    '/regist/face/add',
  ])],
  ['smart-upms-biz.yml', new Set(['/actuator/health'])],
])

/**
 * 判断一条匿名路由是否是对应 Data ID 已登记的公开入口。未知 Data ID 没有精确路径豁免。
 */
export function isAllowedIgnoreUrl(dataId, url) {
  return ALLOWED_IGNORE_URLS_BY_DATA_ID.get(dataId)?.has(url) === true
}

/**
 * 返回未被公开路由策略明确允许的路径，避免运行时 permitAll 放行未知业务端点。
 */
export function findForbiddenIgnoreUrls(urls, dataId) {
  if (!Array.isArray(urls) || urls.some((url) => typeof url !== 'string')) {
    throw new TypeError('ignore-urls must be an array of strings')
  }

  return urls.filter((url) => !isAllowedIgnoreUrl(dataId, url))
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
      if (isAllowedIgnoreUrl(fileName, ignoreUrlsEntry.path)) {
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

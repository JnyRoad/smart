import { readdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { parseDocument } from 'yaml'

const SCRIPT_DIRECTORY = path.dirname(fileURLToPath(import.meta.url))
const REPOSITORY_ROOT = path.resolve(SCRIPT_DIRECTORY, '../..')
const SECURITY_ANNOTATIONS = new Set([
  'Deprecated',
  'Inner',
  'NonceReplayProtected',
  'OpenApi',
  'SignatureVerified',
  'TimestampVerified',
])

/**
 * Task 8 只盘点可能被匿名放行的服务。这里的目录与 Data ID 显式绑定，避免误把其他业务模块纳入发布门禁。
 */
export const DEFAULT_TARGETS = [
  {
    service: 'smart-data',
    configNames: ['smart-data.yml'],
    controllerDirectory: path.join(REPOSITORY_ROOT, 'smart-module/smart-data/smart-data-biz/src/main/java'),
  },
  {
    service: 'smart-algorithm',
    configNames: ['smart-algorithm.yml'],
    controllerDirectory: path.join(REPOSITORY_ROOT, 'smart-module/smart-algorithm/smart-algorithm-biz/src/main/java'),
  },
  {
    service: 'smart-push',
    configNames: ['smart-push.yml'],
    controllerDirectory: path.join(REPOSITORY_ROOT, 'smart-module/smart-push/smart-push-biz/src/main/java'),
  },
  {
    service: 'smart-dispatcher',
    configNames: ['smart-dispatcher.yml'],
    controllerDirectory: path.join(REPOSITORY_ROOT, 'smart-module/smart-dispatcher/smart-dispatcher-biz/src/main/java'),
  },
  {
    service: 'smart-schedule',
    configNames: ['smart-schedule.yml'],
    controllerDirectory: path.join(REPOSITORY_ROOT, 'smart-module/smart-schedule/src/main/java'),
    // 调度任务只在进程内由 Spring 调度器执行，不应存在 HTTP Controller。
    noHttpControllers: true,
  },
  {
    service: 'smart-bridge',
    configNamePattern: /^smart-bridge-biz-.*\.yml$/,
    controllerDirectory: path.join(REPOSITORY_ROOT, 'smart-module/smart-bridge/smart-bridge-biz/src/main/java'),
  },
  {
    service: 'smart-bridge-isc',
    configNamePattern: /^smart-bridge-isc.*\.yml$/,
    controllerDirectory: path.join(REPOSITORY_ROOT, 'smart-module/smart-bridge-isc/smart-bridge-isc-biz/src/main/java'),
  },
]

function normalizePath(value) {
  const combined = `/${value || ''}`.replace(/\/+/g, '/')
  return combined.length > 1 ? combined.replace(/\/$/, '') : combined
}

const MAPPING_ANNOTATIONS = new Set([
  'RequestMapping',
  'GetMapping',
  'PostMapping',
  'PutMapping',
  'DeleteMapping',
  'PatchMapping',
])

function annotationName(text) {
  const match = text.match(/^\s*@([A-Za-z_$][\w$]*)\b/)
  return match?.[1] ?? null
}

function annotationArguments(text) {
  const start = text.indexOf('(')
  const end = text.lastIndexOf(')')
  return start === -1 ? '' : text.slice(start + 1, end)
}

function parenthesesBalance(text) {
  let balance = 0
  let quote = null
  let escaped = false
  for (const character of text) {
    if (quote) {
      if (!escaped && character === quote) quote = null
      escaped = !escaped && character === '\\'
      continue
    }
    if (character === '"' || character === "'") {
      quote = character
    } else if (character === '(') {
      balance += 1
    } else if (character === ')') {
      balance -= 1
    }
  }
  return balance
}

function splitTopLevel(text) {
  const parts = []
  let start = 0
  let curlyBalance = 0
  let parentheses = 0
  let quote = null
  let escaped = false
  for (let index = 0; index < text.length; index += 1) {
    const character = text[index]
    if (quote) {
      if (!escaped && character === quote) quote = null
      escaped = !escaped && character === '\\'
      continue
    }
    if (character === '"' || character === "'") {
      quote = character
    } else if (character === '{') {
      curlyBalance += 1
    } else if (character === '}') {
      curlyBalance -= 1
    } else if (character === '(') {
      parentheses += 1
    } else if (character === ')') {
      parentheses -= 1
    } else if (character === ',' && curlyBalance === 0 && parentheses === 0) {
      parts.push(text.slice(start, index).trim())
      start = index + 1
    }
  }
  parts.push(text.slice(start).trim())
  return parts.filter(Boolean)
}

function parseLiteralPathExpression(expression) {
  const value = expression.trim()
  const literal = value.match(/^(["'])(.*?)\1$/)
  if (literal) return [literal[2]]
  if (!value.startsWith('{') || !value.endsWith('}')) return null
  const entries = splitTopLevel(value.slice(1, -1))
  if (entries.length === 0) return null
  const paths = []
  for (const entry of entries) {
    const entryLiteral = entry.trim().match(/^(["'])(.*?)\1$/)
    if (!entryLiteral) return null
    paths.push(entryLiteral[2])
  }
  return paths
}

function parseMappingPaths(annotation) {
  const argumentsText = annotationArguments(annotation.text).trim()
  if (!argumentsText) return { paths: [''] }

  const values = []
  let hasPathOrValue = false
  for (const part of splitTopLevel(argumentsText)) {
    const named = part.match(/^(path|value)\s*=\s*([\s\S]*)$/)
    if (named) {
      hasPathOrValue = true
      const literals = parseLiteralPathExpression(named[2])
      if (!literals) {
        return { reason: 'path or value is not a string literal' }
      }
      values.push(...literals)
      continue
    }
    if (!part.includes('=')) {
      const literals = parseLiteralPathExpression(part)
      if (!literals) return { reason: 'mapping path is not a string literal' }
      values.push(...literals)
    }
  }

  if (!hasPathOrValue && values.length === 0) return { paths: [''] }
  return { paths: values }
}

function readAnnotation(lines, startIndex) {
  const firstLine = lines[startIndex]
  const name = annotationName(firstLine)
  if (!name) return null
  let endIndex = startIndex
  let text = firstLine.trim()
  let balance = parenthesesBalance(text)
  while (balance > 0 && endIndex + 1 < lines.length) {
    endIndex += 1
    text += `\n${lines[endIndex].trim()}`
    balance += parenthesesBalance(lines[endIndex])
  }
  return {
    endIndex,
    name,
    text,
    unclosed: balance !== 0,
  }
}

function methodDeclaration(line) {
  return /\b(public|protected|private)\b/.test(line)
    && /\(/.test(line)
    && !/\bclass\b/.test(line)
}

function hasAnnotation(annotations, annotation) {
  return annotations.some((value) => value === annotation)
}

function joinPaths(basePath, endpointPath) {
  return normalizePath(`${basePath || ''}/${endpointPath || ''}`)
}

function collectOpenApiScopes(annotationDetails) {
  return annotationDetails
    .filter((annotation) => annotation.name === 'OpenApi')
    .flatMap((annotation) => {
      const argumentsText = annotationArguments(annotation.text).trim()
      if (!argumentsText) return []
      const parts = splitTopLevel(argumentsText)
      const valuePart = parts.find((part) => /^value\s*=/.test(part))
      if (valuePart) {
        return parseLiteralPathExpression(valuePart.replace(/^value\s*=\s*/, '')) ?? []
      }
      if (parts.length === 1 && !parts[0].includes('=')) {
        return parseLiteralPathExpression(parts[0]) ?? []
      }
      return []
    })
}

/**
 * 静态证据只允许来自 Java 可执行代码。保留换行以便正则边界稳定，注释和字符串均改为空白，不能作为安全证明。
 */
function stripJavaCommentsAndStrings(source) {
  let output = ''
  let index = 0
  let state = 'code'
  while (index < source.length) {
    const character = source[index]
    const next = source[index + 1]
    const nextTwo = source[index + 2]
    if (state === 'code') {
      if (character === '/' && next === '/') {
        output += '  '
        index += 2
        state = 'line-comment'
      } else if (character === '/' && next === '*') {
        output += '  '
        index += 2
        state = 'block-comment'
      } else if (character === '"' && next === '"' && nextTwo === '"') {
        output += '   '
        index += 3
        state = 'text-block'
      } else if (character === '"') {
        output += ' '
        index += 1
        state = 'string'
      } else if (character === "'") {
        output += ' '
        index += 1
        state = 'character'
      } else {
        output += character
        index += 1
      }
    } else if (state === 'line-comment') {
      output += character === '\n' ? '\n' : ' '
      index += 1
      if (character === '\n') state = 'code'
    } else if (state === 'block-comment') {
      if (character === '*' && next === '/') {
        output += '  '
        index += 2
        state = 'code'
      } else {
        output += character === '\n' ? '\n' : ' '
        index += 1
      }
    } else if (state === 'text-block') {
      if (character === '"' && next === '"' && nextTwo === '"') {
        output += '   '
        index += 3
        state = 'code'
      } else {
        output += character === '\n' ? '\n' : ' '
        index += 1
      }
    } else {
      output += character === '\n' ? '\n' : ' '
      if (character === '\\' && index + 1 < source.length) {
        output += source[index + 1] === '\n' ? '\n' : ' '
        index += 2
      } else if ((state === 'string' && character === '"') || (state === 'character' && character === "'")) {
        state = 'code'
        index += 1
      } else {
        index += 1
      }
    }
  }
  return output
}

function collectSignatureEvidence(annotationDetails, methodSource) {
  const annotationNames = new Set(annotationDetails.map((annotation) => annotation.name))
  const executableSource = stripJavaCommentsAndStrings(methodSource)
  const evidence = []
  const has = (annotation, pattern) => annotationNames.has(annotation) || pattern.test(executableSource)
  if (has('SignatureVerified', /\b(?:verify|validate)(?:Request)?Signature\s*\(|\b(?:signatureVerifier|signatureValidator)\s*\.\s*verify\s*\(/)) {
    evidence.push('signature')
  }
  if (has('TimestampVerified', /\b(?:verify|validate)(?:Request)?Timestamp\s*\(|\b(?:timestampVerifier|timestampValidator|replayGuard)\s*\.\s*(?:verify|validate)Timestamp\s*\(/)) {
    evidence.push('timestamp')
  }
  if (has('NonceReplayProtected', /\b(?:check|verify|validate)Nonce\s*\(|\b(?:nonceReplayGuard|nonceVerifier|nonceValidator|replayGuard)\s*\.\s*(?:check|verify|validate)Nonce\s*\(/)) {
    evidence.push('nonce')
  }
  return evidence
}

function readMethodSource(lines, startIndex) {
  let braceBalance = 0
  let bodyStarted = false
  const collected = []
  for (let index = startIndex; index < lines.length; index += 1) {
    const line = lines[index]
    collected.push(line)
    for (const character of line) {
      if (character === '{') {
        braceBalance += 1
        bodyStarted = true
      } else if (character === '}') {
        braceBalance -= 1
      }
    }
    if (bodyStarted && braceBalance === 0) {
      return { endIndex: index, source: collected.join('\n') }
    }
    if (!bodyStarted && line.includes(';')) {
      return { endIndex: index, source: collected.join('\n') }
    }
  }
  return { endIndex: lines.length - 1, source: collected.join('\n') }
}

/**
 * 路由语法解析必须只看真实 Java 代码。注释、字符字面量和文本块以空白替换；普通字符串保留，供 Spring 注解读取路径。
 * 词法状态机同时保证字符串中的 `//`、`/*` 不会误切换到注释状态。
 */
function filterJavaSourceForRouteParsing(source) {
  let output = ''
  let index = 0
  let state = 'code'
  while (index < source.length) {
    const character = source[index]
    const next = source[index + 1]
    const nextTwo = source[index + 2]
    if (state === 'code') {
      if (character === '/' && next === '/') {
        output += '  '
        index += 2
        state = 'line-comment'
      } else if (character === '/' && next === '*') {
        output += '  '
        index += 2
        state = 'block-comment'
      } else if (character === '"' && next === '"' && nextTwo === '"') {
        output += '   '
        index += 3
        state = 'text-block'
      } else if (character === '"') {
        output += character
        index += 1
        state = 'string'
      } else if (character === "'") {
        output += ' '
        index += 1
        state = 'character'
      } else {
        output += character
        index += 1
      }
    } else if (state === 'line-comment') {
      output += character === '\n' ? '\n' : ' '
      index += 1
      if (character === '\n') state = 'code'
    } else if (state === 'block-comment') {
      if (character === '*' && next === '/') {
        output += '  '
        index += 2
        state = 'code'
      } else {
        output += character === '\n' ? '\n' : ' '
        index += 1
      }
    } else if (state === 'text-block') {
      if (character === '"' && next === '"' && nextTwo === '"') {
        output += '   '
        index += 3
        state = 'code'
      } else {
        output += character === '\n' ? '\n' : ' '
        index += 1
      }
    } else if (state === 'string') {
      output += character
      if (character === '\\' && index + 1 < source.length) {
        output += source[index + 1]
        index += 2
      } else if (character === '"') {
        index += 1
        state = 'code'
      } else {
        index += 1
      }
    } else {
      output += character === '\n' ? '\n' : ' '
      if (character === '\\' && index + 1 < source.length) {
        output += source[index + 1] === '\n' ? '\n' : ' '
        index += 2
      } else if (character === "'") {
        index += 1
        state = 'code'
      } else {
        index += 1
      }
    }
  }
  return output
}

/**
 * 解析控制器中可静态识别的 Spring 路由。解析器故意保守：无法解析的复杂注解不会被臆造成可匿名路由。
 */
export function parseControllerSourceDetailed(source, sourcePath) {
  const routes = []
  const unparsedMappings = []
  let classPaths = ['']
  let classAnnotationDetails = []
  let pendingAnnotationDetails = []
  let pendingMappings = []
  const lines = filterJavaSourceForRouteParsing(source).split(/\r?\n/)

  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index].trim()
    if (!line || line.startsWith('//') || line.startsWith('*') || line.startsWith('/*')) {
      continue
    }

    const annotation = readAnnotation(lines, index)
    if (annotation) {
      const annotationStartIndex = index
      index = annotation.endIndex
      if (MAPPING_ANNOTATIONS.has(annotation.name)) {
        const parsedMapping = annotation.unclosed
          ? { reason: 'mapping annotation is not closed' }
          : parseMappingPaths(annotation)
        if (parsedMapping.reason) {
          unparsedMappings.push({
            annotation: annotation.name,
            line: annotationStartIndex + 1,
            reason: parsedMapping.reason,
            sourcePath,
          })
        } else {
          pendingMappings.push(...parsedMapping.paths)
        }
      } else if (SECURITY_ANNOTATIONS.has(annotation.name)) {
        pendingAnnotationDetails.push(annotation)
      }
      continue
    }

    if (/\bclass\s+\w+/.test(line)) {
      classPaths = pendingMappings.length > 0 ? pendingMappings : ['']
      classAnnotationDetails = pendingAnnotationDetails
      pendingAnnotationDetails = []
      pendingMappings = []
      continue
    }

    if (methodDeclaration(line) && pendingMappings.length > 0) {
      const method = readMethodSource(lines, index)
      const annotationDetails = [...classAnnotationDetails, ...pendingAnnotationDetails]
      for (const endpointPath of pendingMappings) {
        for (const classPath of classPaths) {
          routes.push({
            annotationDetails,
            annotations: annotationDetails.map((annotationDetail) => annotationDetail.name),
            openApiScopes: collectOpenApiScopes(annotationDetails),
            path: joinPaths(classPath, endpointPath),
            signatureEvidence: collectSignatureEvidence(annotationDetails, method.source),
            sourcePath,
          })
        }
      }
      pendingAnnotationDetails = []
      pendingMappings = []
      index = method.endIndex
      continue
    }

    pendingAnnotationDetails = []
    pendingMappings = []
  }

  return { routes, unparsedMappings }
}

/**
 * 保持简化 API 供路由语义单测使用；完整库存必须使用带未解析映射证据的 detailed 版本。
 */
export function parseControllerSource(source, sourcePath) {
  return parseControllerSourceDetailed(source, sourcePath).routes
}

/**
 * 给出目标暴露面，而不是根据现有匿名配置推测安全性。签名回调只有同时具备三项证据才可进入匿名白名单。
 */
export function classifyRoute(route) {
  if (hasAnnotation(route.annotations ?? [], 'Inner')) {
    return {
      exposure: 'internal',
      nacosIgnoreUrl: false,
      requires: ['service-token', 'FROM_IN'],
    }
  }

  const signatureEvidence = new Set(route.signatureEvidence ?? [])
  const verifiedCallback = ['signature', 'timestamp', 'nonce'].every((item) => signatureEvidence.has(item))
    && (hasAnnotation(route.annotations ?? [], 'SignatureVerified') || /(callback|webhook|handle|notify)/i.test(route.path ?? ''))
  if (verifiedCallback) {
    return {
      exposure: 'callback-signed',
      nacosIgnoreUrl: true,
      requires: ['signature', 'timestamp', 'nonce'],
    }
  }

  if (hasAnnotation(route.annotations ?? [], 'Deprecated')) {
    return { exposure: 'retired', nacosIgnoreUrl: false, requires: [] }
  }

  return {
    exposure: 'external-authenticated',
    nacosIgnoreUrl: false,
    requires: ['user-or-client-token'],
  }
}

async function listFiles(directory) {
  try {
    const entries = await readdir(directory, { withFileTypes: true })
    const nested = await Promise.all(entries.map(async (entry) => {
      const entryPath = path.join(directory, entry.name)
      if (entry.isDirectory()) return listFiles(entryPath)
      return entry.isFile() ? [entryPath] : []
    }))
    return nested.flat()
  } catch (error) {
    if (error.code === 'ENOENT') return []
    throw error
  }
}

function getObjectPath(value, keys) {
  return keys.reduce((current, key) => current && typeof current === 'object' ? current[key] : undefined, value)
}

async function readIgnoreUrls(configDirectory, configName) {
  const content = await readFile(path.join(configDirectory, configName), 'utf8')
  const document = parseDocument(content, { merge: true, prettyErrors: false, strict: true, uniqueKeys: true })
  if (document.errors.length > 0) {
    throw new Error(`Invalid YAML in ${configName}`)
  }
  const ignoreUrls = getObjectPath(document.toJS({ maxAliasCount: 100 }), ['security', 'oauth2', 'client', 'ignore-urls'])
  if (ignoreUrls === undefined) return []
  if (!Array.isArray(ignoreUrls) || ignoreUrls.some((item) => typeof item !== 'string')) {
    throw new Error(`Unsupported ignore-urls value in ${configName}`)
  }
  return ignoreUrls
}

function matchesIgnoreUrl(routePath, ignoreUrl) {
  if (ignoreUrl.endsWith('/**')) {
    return routePath === ignoreUrl.slice(0, -3) || routePath.startsWith(ignoreUrl.slice(0, -2))
  }
  return routePath === ignoreUrl
}

function isCallbackCandidate(route) {
  return /(callback|webhook|handle|notify)/i.test(route.path)
    && !hasAnnotation(route.annotations, 'Inner')
}

function isInternalPathCandidate(route) {
  return /(^|\/)inner(?:\/|$)/i.test(route.path)
    && !hasAnnotation(route.annotations, 'Inner')
}

function hasServerOpenApi(route) {
  return (route.openApiScopes ?? []).includes('server')
}

function routeReview(route, ignoreUrls) {
  let base = classifyRoute(route)
  const anonymousMatches = ignoreUrls.filter((ignoreUrl) => matchesIgnoreUrl(route.path, ignoreUrl))
  const blockers = []
  const internalCandidate = isInternalPathCandidate(route)

  if (internalCandidate) {
    base = {
      exposure: 'internal',
      nacosIgnoreUrl: false,
      requires: ['service-token', 'FROM_IN'],
    }
    blockers.push('内部候选路径缺少 @Inner 与 @OpenApi(server) 静态证据')
  }

  if (base.exposure === 'internal') {
    if (!internalCandidate && !hasServerOpenApi(route)) {
      blockers.push('内部端点缺少 @OpenApi(server) 静态证据')
    }
    if (anonymousMatches.length > 0) {
      blockers.push('当前 ignore-urls 匹配内部端点')
    }
  } else if (isCallbackCandidate(route) && base.exposure !== 'callback-signed') {
    blockers.push('未知厂商回调：缺少签名、时间窗和 nonce 重放检查证据')
    if (anonymousMatches.length > 0) blockers.push('当前 ignore-urls 允许匿名访问')
  } else if (anonymousMatches.length > 0 && base.exposure !== 'callback-signed') {
    blockers.push('当前 ignore-urls 匹配该路由，尚未证明认证收口')
  }

  return {
    ...base,
    anonymousMatches,
    blockers,
    status: blockers.length > 0 ? 'BLOCKED' : 'REVIEW_REQUIRED',
  }
}

function escapeCell(value) {
  return String(value).replaceAll('|', '\\|').replaceAll('\n', '<br>')
}

function formatRouteRow(route) {
  const blockers = route.blockers.length === 0 ? '待人工核验调用方与认证链' : route.blockers.join('；')
  return `| ${escapeCell(route.service)} | \`${escapeCell(route.path)}\` | ${escapeCell(route.exposure)} | ${escapeCell(route.status)} | ${escapeCell(route.annotations.join(', ') || '-')} | ${escapeCell(route.requires.join(', ') || '-')} | ${escapeCell(route.anonymousMatches.join(', ') || '-')} | ${escapeCell(blockers)} | \`${escapeCell(route.sourcePath)}\` |`
}

function renderInventoryMarkdown(inventory) {
  const lines = [
    '# 服务路由静态库存（Task 8 第一阶段）',
    '',
    '> 本文由 `node scripts/security/build-public-route-inventory.mjs` 离线生成。它只读取本仓控制器注解与本地 Nacos 基线，不连接生产、不输出任何密钥或个人信息。',
    '',
    '## 判定规则',
    '',
    '- `internal`：源码存在 `@Inner`；上线前仍必须有精确的 `@OpenApi("server")`、服务令牌与 `FROM_IN`。',
    '- `callback-signed`：仅当对应方法静态识别到签名、时间窗与 nonce 重放保护三项源码证据时，才可作为精确匿名白名单候选。',
    '- `external-authenticated`：默认目标是用户或客户端认证，不能因当前 `ignore-urls` 而视为匿名安全。',
    '- `retired`：源码明确标记为废弃，仍需在发布前确认没有调用方。',
    '- `BLOCKED`：证据不足或当前匿名配置与目标暴露面冲突；尤其未知厂商回调不会被假设为安全。',
    '',
    '## 服务汇总',
    '',
    '| 服务 | Data ID | 控制器路由数 | 阻断项 |',
    '| --- | --- | ---: | ---: |',
  ]

  for (const service of inventory.services) {
    lines.push(`| ${escapeCell(service.service)} | ${escapeCell(service.configNames.join(', ') || '未找到配置')} | ${service.routeCount} | ${service.blockingCount} |`)
  }

  lines.push('', '## 路由明细', '', '| 服务 | 路径 | 目标分类 | 状态 | 源码注解 | 上线要求 | 当前匿名匹配 | 阻断或待核验证据 | 来源 |', '| --- | --- | --- | --- | --- | --- | --- | --- | --- |')
  for (const route of inventory.routes) lines.push(formatRouteRow(route))

  if (inventory.serviceFindings.length > 0) {
    lines.push('', '## 服务级阻断项', '')
    for (const finding of inventory.serviceFindings) {
      lines.push(`- **${escapeCell(finding.service)}**：${escapeCell(finding.message)}`)
    }
  }

  lines.push('', '## 下一阶段要求', '', '1. 对每个 `BLOCKED` 路由补齐调用方、签名或内部契约证据后再改变本地 Nacos。', '2. 不得因静态盘点通过而删除任一 `/**`；每个 Data ID 需独立完成签名/令牌探针和灰度。', '3. 本库存不证明生产 Nacos 或生产网络隔离状态，生产发布仍按发布清单执行。', '')
  return lines.join('\n')
}

async function resolveConfigNames(configDirectory, target) {
  if (target.configNames) return target.configNames
  const entries = await readdir(configDirectory, { withFileTypes: true })
  return entries
    .filter((entry) => entry.isFile() && target.configNamePattern.test(entry.name))
    .map((entry) => entry.name)
    .sort()
}

/**
 * 建立可审计库存。返回值包含 Markdown 和结构化阻断项，便于测试与 CI 在不泄漏配置内容的前提下失败关闭。
 */
export async function buildInventory({
  configDirectory = path.join(REPOSITORY_ROOT, 'docker/nacos/config/dev'),
  targets = DEFAULT_TARGETS,
} = {}) {
  const routes = []
  const services = []
  const serviceFindings = []

  for (const target of targets) {
    const configNames = await resolveConfigNames(configDirectory, target)
    const configIgnoreUrls = []
    for (const configName of configNames) {
      const ignoreUrls = await readIgnoreUrls(configDirectory, configName)
      configIgnoreUrls.push(...ignoreUrls)
    }

    const controllerFiles = (await listFiles(target.controllerDirectory))
      .filter((file) => file.endsWith('Controller.java'))
      .sort()
    if (controllerFiles.length === 0 && !target.noHttpControllers) {
      serviceFindings.push({
        service: target.service,
        message: '未找到 Controller 源码，无法证明匿名白名单安全，禁止收口配置',
      })
    }

    if (controllerFiles.length > 0 && target.noHttpControllers) {
      serviceFindings.push({
        service: target.service,
        message: '该服务声明不提供 HTTP 入口，但发现 Controller 源码，禁止匿名白名单收口',
      })
    }

    const serviceRoutes = []
    for (const controllerFile of controllerFiles) {
      const source = await readFile(controllerFile, 'utf8')
      const sourcePath = path.relative(REPOSITORY_ROOT, controllerFile)
      const parsedController = parseControllerSourceDetailed(source, sourcePath)
      for (const unresolvedMapping of parsedController.unparsedMappings) {
        serviceFindings.push({
          service: target.service,
          message: `${unresolvedMapping.sourcePath}:${unresolvedMapping.line} ${unresolvedMapping.annotation}: ${unresolvedMapping.reason}`,
        })
      }
      for (const route of parsedController.routes) {
        serviceRoutes.push({
          ...route,
          service: target.service,
          ...routeReview(route, configIgnoreUrls),
        })
      }
    }
    if (controllerFiles.length > 0 && serviceRoutes.length === 0) {
      serviceFindings.push({
        service: target.service,
        message: '发现 Controller 源码但未解析到任何路由，无法证明匿名白名单安全，禁止收口配置',
      })
    }
    serviceRoutes.sort((left, right) => left.path.localeCompare(right.path) || left.sourcePath.localeCompare(right.sourcePath))
    routes.push(...serviceRoutes)
    services.push({
      service: target.service,
      configNames,
      routeCount: serviceRoutes.length,
      blockingCount: serviceRoutes.filter((route) => route.status === 'BLOCKED').length
        + serviceFindings.filter((finding) => finding.service === target.service).length,
    })
  }

  const inventory = {
    hasBlockingFindings: routes.some((route) => route.status === 'BLOCKED') || serviceFindings.length > 0,
    routes,
    serviceFindings,
    services,
  }
  inventory.markdown = renderInventoryMarkdown(inventory)
  return inventory
}

async function main() {
  const outputFile = path.join(REPOSITORY_ROOT, 'docs/security/2026-07-22-service-route-inventory.md')
  const inventory = await buildInventory()
  await writeFile(outputFile, inventory.markdown)
  console.log(`Generated ${path.relative(REPOSITORY_ROOT, outputFile)} with ${inventory.routes.length} routes.`)
  if (inventory.hasBlockingFindings) {
    console.error('Route inventory has BLOCKED findings; do not change Nacos anonymous routes.')
    process.exitCode = 1
  }
}

if (process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])) {
  main().catch((error) => {
    console.error(error.message)
    process.exitCode = 2
  })
}

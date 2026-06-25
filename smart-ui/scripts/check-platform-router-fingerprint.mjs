// 平台路由指纹守卫：锁定 src/router/platform/index.js 的路由集合、顺序和静态字段。
// --update 只允许本地手工刷新基线，门禁/CI 命令必须使用默认比对模式，避免自动吞掉路由回归。
import { parse } from '@babel/parser'
import { existsSync, readFileSync, writeFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { dirname, join, normalize } from 'node:path/posix'

const ROOT = resolve(import.meta.dirname, '..')
const ROUTE_FILE = 'src/router/platform/index.js'
const BASELINE_FILE = 'docs/platform-router-fingerprint.json'
const ROUTE_PATH = resolve(ROOT, ROUTE_FILE)
const BASELINE_PATH = resolve(ROOT, BASELINE_FILE)
const FINGERPRINT_VERSION = 1
const KNOWN_ROUTE_FIELDS = new Set(['path', 'name', 'redirect', 'component', 'children', 'meta'])

export function normalizeSource (source, node) {
  return source.slice(node.start, node.end).replace(/\s+/g, ' ').trim()
}

function parseModule (source) {
  return parse(source, {
    sourceType: 'module',
    plugins: ['dynamicImport']
  })
}

function getKeyName (key) {
  if (!key) return null
  if (key.type === 'Identifier') return key.name
  if (key.type === 'StringLiteral') return key.value
  if (key.type === 'NumericLiteral') return String(key.value)
  return null
}

function findProperty (objectNode, name) {
  if (!objectNode || objectNode.type !== 'ObjectExpression') return null
  return objectNode.properties.find((property) => {
    if (property.type !== 'ObjectProperty' && property.type !== 'ObjectMethod') return false
    return getKeyName(property.key) === name
  }) || null
}

function getPropertyValue (property) {
  if (!property) return null
  return property.type === 'ObjectProperty' ? property.value : property
}

function buildImportBindings (ast) {
  const bindings = new Map()
  for (const statement of ast.program.body) {
    if (statement.type !== 'ImportDeclaration') continue
    for (const specifier of statement.specifiers) {
      if (specifier.type === 'ImportDefaultSpecifier') {
        bindings.set(specifier.local.name, { source: statement.source.value, imported: 'default' })
        continue
      }
      if (specifier.type === 'ImportSpecifier') {
        bindings.set(specifier.local.name, {
          source: statement.source.value,
          imported: getKeyName(specifier.imported)
        })
        continue
      }
      bindings.set(specifier.local.name, { source: statement.source.value, imported: '*' })
    }
  }
  return bindings
}

function getImportSource (importBindings, name) {
  const binding = importBindings.get(name)
  return binding ? binding.source : null
}

function buildLocalRouteBindings (ast) {
  const bindings = new Map()
  for (const statement of ast.program.body) {
    if (statement.type !== 'VariableDeclaration') continue
    for (const declaration of statement.declarations) {
      if (declaration.id.type === 'Identifier' && declaration.init) {
        bindings.set(declaration.id.name, declaration.init)
      }
    }
  }
  return bindings
}

function findDefaultRouteArray (context) {
  const { ast, localRouteBindings } = context
  const defaultExport = ast.program.body.find((statement) => statement.type === 'ExportDefaultDeclaration')
  if (!defaultExport) throw new Error('平台路由文件缺少 export default')
  if (defaultExport.declaration.type === 'ArrayExpression') return defaultExport.declaration
  if (
    defaultExport.declaration.type === 'Identifier' &&
    localRouteBindings.has(defaultExport.declaration.name)
  ) {
    return localRouteBindings.get(defaultExport.declaration.name)
  }
  if (defaultExport.declaration.type !== 'ArrayExpression') {
    throw new Error('平台路由 export default 必须是数组字面量')
  }
  return defaultExport.declaration
}

function readLazyImportSource (node) {
  if (!node) return null
  if (node.type === 'ImportExpression') return readStringLiteral(node.source)
  if (node.type === 'CallExpression' && node.callee.type === 'Import') {
    return readStringLiteral(node.arguments[0])
  }
  if (node.type === 'ArrowFunctionExpression') return readLazyImportSource(node.body)
  if (node.type === 'ReturnStatement') return readLazyImportSource(node.argument)
  if (node.type === 'BlockStatement') {
    const returnStatement = node.body.find((statement) => statement.type === 'ReturnStatement')
    return returnStatement ? readLazyImportSource(returnStatement) : null
  }
  return null
}

function readStringLiteral (node) {
  if (!node) return null
  if (node.type === 'StringLiteral') return node.value
  if (node.type === 'TemplateLiteral' && node.expressions.length === 0) {
    return node.quasis[0].value.cooked
  }
  return null
}

function readComponent (node, source, importBindings) {
  if (!node) return null
  if (node.type === 'Identifier') {
    return {
      type: 'identifier',
      name: node.name,
      importSource: getImportSource(importBindings, node.name)
    }
  }
  const lazyImportSource = readLazyImportSource(node)
  if (lazyImportSource) {
    return {
      type: 'lazyImport',
      source: lazyImportSource
    }
  }
  return {
    type: node.type,
    source: normalizeSource(source, node)
  }
}

function readStaticValue (node, source, importBindings) {
  if (!node) return null
  if (node.type === 'StringLiteral') return node.value
  if (node.type === 'NumericLiteral') return node.value
  if (node.type === 'BooleanLiteral') return node.value
  if (node.type === 'NullLiteral') return null
  if (node.type === 'Identifier') {
    return {
      type: 'identifier',
      name: node.name,
      importSource: getImportSource(importBindings, node.name)
    }
  }
  if (node.type === 'UnaryExpression' && node.operator === '-' && node.argument.type === 'NumericLiteral') {
    return -node.argument.value
  }
  if (node.type === 'TemplateLiteral' && node.expressions.length === 0) {
    return node.quasis[0].value.cooked
  }
  if (node.type === 'ArrayExpression') {
    return node.elements.map((element) => readStaticValue(element, source, importBindings))
  }
  if (node.type === 'ObjectExpression') {
    return readStaticObject(node, source, importBindings)
  }
  return {
    type: node.type,
    source: normalizeSource(source, node)
  }
}

function readStaticObject (objectNode, source, importBindings) {
  const result = {}
  for (const property of objectNode.properties) {
    if (property.type === 'SpreadElement') {
      result['...'] = readStaticValue(property.argument, source, importBindings)
      continue
    }
    const key = getKeyName(property.key) || normalizeSource(source, property.key)
    result[key] = readStaticValue(getPropertyValue(property), source, importBindings)
  }
  return result
}

function readRouteField (routeNode, field, source, importBindings) {
  return readStaticValue(getPropertyValue(findProperty(routeNode, field)), source, importBindings)
}

function readChildren (routeNode, context) {
  const childrenNode = getPropertyValue(findProperty(routeNode, 'children'))
  if (!childrenNode) return null
  if (childrenNode.type !== 'ArrayExpression') {
    return {
      type: 'expression',
      value: readStaticValue(childrenNode, context.source, context.importBindings)
    }
  }
  return {
    type: 'array',
    routes: collectRoutesFromArray(childrenNode, context)
  }
}

function readExtraFields (routeNode, context) {
  const extra = {}
  for (const property of routeNode.properties) {
    if (property.type !== 'ObjectProperty' && property.type !== 'ObjectMethod') continue
    const key = getKeyName(property.key)
    if (!key || KNOWN_ROUTE_FIELDS.has(key)) continue
    extra[key] = readStaticValue(getPropertyValue(property), context.source, context.importBindings)
  }
  return Object.keys(extra).length ? extra : null
}

function buildRouteFingerprint (routeNode, context) {
  if (!routeNode || routeNode.type !== 'ObjectExpression') {
    return {
      type: routeNode ? routeNode.type : 'MissingRouteNode',
      source: routeNode ? normalizeSource(context.source, routeNode) : null
    }
  }

  const fingerprint = {
    path: readRouteField(routeNode, 'path', context.source, context.importBindings),
    name: readRouteField(routeNode, 'name', context.source, context.importBindings),
    redirect: readRouteField(routeNode, 'redirect', context.source, context.importBindings),
    component: readComponent(getPropertyValue(findProperty(routeNode, 'component')), context.source, context.importBindings),
    meta: readRouteField(routeNode, 'meta', context.source, context.importBindings),
    children: readChildren(routeNode, context)
  }
  const extra = readExtraFields(routeNode, context)
  if (extra) fingerprint.extra = extra
  return fingerprint
}

function readModuleFromDisk (relativePath) {
  const absolutePath = resolve(ROOT, relativePath)
  return existsSync(absolutePath) ? readFileSync(absolutePath, 'utf8') : null
}

function resolveRelativeModule (fromFile, importSource, readModule) {
  const basePath = normalize(join(dirname(fromFile), importSource))
  const candidates = [basePath, `${basePath}.js`, `${basePath}/index.js`]
  return candidates.find((candidate) => readModule(candidate) !== null) || null
}

function createModuleContext (source, routeFile, options = {}) {
  const ast = parseModule(source)
  return {
    ast,
    source,
    routeFile,
    readModule: options.readModule || readModuleFromDisk,
    moduleCache: options.moduleCache || new Map(),
    importBindings: buildImportBindings(ast),
    localRouteBindings: buildLocalRouteBindings(ast)
  }
}

function findNamedExportValue (context, exportedName) {
  for (const statement of context.ast.program.body) {
    if (statement.type === 'ExportNamedDeclaration' && statement.declaration) {
      if (statement.declaration.type === 'VariableDeclaration') {
        for (const declaration of statement.declaration.declarations) {
          if (declaration.id.type === 'Identifier' && declaration.id.name === exportedName) {
            return declaration.init
          }
        }
      }
      continue
    }
    if (statement.type !== 'ExportNamedDeclaration') continue
    for (const specifier of statement.specifiers) {
      if (getKeyName(specifier.exported) !== exportedName) continue
      const localName = getKeyName(specifier.local)
      return localName && context.localRouteBindings.has(localName)
        ? context.localRouteBindings.get(localName)
        : null
    }
  }
  return null
}

function findDefaultExportValue (context) {
  const defaultExport = context.ast.program.body.find((statement) => statement.type === 'ExportDefaultDeclaration')
  if (!defaultExport) return null
  if (
    defaultExport.declaration.type === 'Identifier' &&
    context.localRouteBindings.has(defaultExport.declaration.name)
  ) {
    return context.localRouteBindings.get(defaultExport.declaration.name)
  }
  return defaultExport.declaration
}

function resolveImportedRouteReference (binding, context) {
  if (!binding || !binding.source.startsWith('.')) return null

  const importedRouteFile = resolveRelativeModule(context.routeFile, binding.source, context.readModule)
  if (!importedRouteFile) {
    throw new Error(`无法解析路由模块：${context.routeFile} -> ${binding.source}`)
  }

  const cacheKey = `${importedRouteFile}#${binding.imported}`
  if (context.moduleCache.has(cacheKey)) return context.moduleCache.get(cacheKey)

  const importedSource = context.readModule(importedRouteFile)
  const importedContext = createModuleContext(importedSource, importedRouteFile, {
    readModule: context.readModule,
    moduleCache: context.moduleCache
  })
  const importedNode = binding.imported === 'default'
    ? findDefaultExportValue(importedContext)
    : findNamedExportValue(importedContext, binding.imported)
  if (!importedNode) {
    throw new Error(`路由模块 ${importedRouteFile} 缺少导出：${binding.imported}`)
  }

  const routes = resolveRouteReference(importedNode, importedContext)
  context.moduleCache.set(cacheKey, routes)
  return routes
}

function resolveRouteReference (node, context) {
  if (!node) return null
  if (node.type === 'ArrayExpression') return collectRoutesFromArray(node, context)
  if (node.type === 'ObjectExpression') return [buildRouteFingerprint(node, context)]
  if (node.type !== 'Identifier') return null

  if (context.localRouteBindings.has(node.name)) {
    return resolveRouteReference(context.localRouteBindings.get(node.name), context)
  }

  return resolveImportedRouteReference(context.importBindings.get(node.name), context)
}

function collectRoutesFromArray (arrayNode, context) {
  const routes = []
  for (const element of arrayNode.elements) {
    if (!element) continue
    if (element.type === 'SpreadElement') {
      const spreadRoutes = resolveRouteReference(element.argument, context)
      routes.push(...(spreadRoutes || [buildRouteFingerprint(element, context)]))
      continue
    }
    const resolvedRoutes = resolveRouteReference(element, context)
    routes.push(...(resolvedRoutes || [buildRouteFingerprint(element, context)]))
  }
  return routes
}

function buildDuplicatePathPositions (routes) {
  const positionsByPath = {}
  routes.forEach((route, index) => {
    if (typeof route.path !== 'string') return
    positionsByPath[route.path] = positionsByPath[route.path] || []
    positionsByPath[route.path].push(index)
  })

  return Object.fromEntries(
    Object.entries(positionsByPath)
      .filter(([, positions]) => positions.length > 1)
      .sort(([left], [right]) => left.localeCompare(right))
  )
}

export function buildPlatformRouterFingerprintFromSource (source, routeFile = ROUTE_FILE, options = {}) {
  const context = createModuleContext(source, routeFile, options)
  const routes = collectRoutesFromArray(findDefaultRouteArray(context), context)

  return {
    version: FINGERPRINT_VERSION,
    routeFile,
    topLevelRouteCount: routes.length,
    duplicateTopLevelPaths: buildDuplicatePathPositions(routes),
    routes
  }
}

function formatValue (value) {
  if (typeof value === 'undefined') return 'undefined'
  return JSON.stringify(value)
}

function isPlainObject (value) {
  return value && typeof value === 'object' && !Array.isArray(value)
}

function compareValue (baseline, current, path, diffs) {
  if (Object.is(baseline, current)) return

  if (Array.isArray(baseline) || Array.isArray(current)) {
    if (!Array.isArray(baseline) || !Array.isArray(current)) {
      diffs.push(`${path} expected ${formatValue(baseline)} but got ${formatValue(current)}`)
      return
    }
    if (baseline.length !== current.length) {
      diffs.push(`${path} length expected ${baseline.length} but got ${current.length}`)
    }
    const length = Math.min(baseline.length, current.length)
    for (let index = 0; index < length; index += 1) {
      compareValue(baseline[index], current[index], `${path}[${index}]`, diffs)
    }
    return
  }

  if (isPlainObject(baseline) || isPlainObject(current)) {
    if (!isPlainObject(baseline) || !isPlainObject(current)) {
      diffs.push(`${path} expected ${formatValue(baseline)} but got ${formatValue(current)}`)
      return
    }
    const keys = [...new Set([...Object.keys(baseline), ...Object.keys(current)])].sort()
    for (const key of keys) {
      const childPath = path ? `${path}.${key}` : key
      compareValue(baseline[key], current[key], childPath, diffs)
    }
    return
  }

  diffs.push(`${path} expected ${formatValue(baseline)} but got ${formatValue(current)}`)
}

export function diffFingerprint (baseline, current) {
  const diffs = []
  compareValue(baseline, current, '', diffs)
  return diffs
}

function main () {
  const update = process.argv.includes('--update')
  const current = buildPlatformRouterFingerprintFromSource(readFileSync(ROUTE_PATH, 'utf8'))

  if (update) {
    writeFileSync(BASELINE_PATH, JSON.stringify(current, null, 2) + '\n')
    console.log(`平台路由指纹基线已更新：${current.topLevelRouteCount} 个顶层路由`)
    return
  }

  if (!existsSync(BASELINE_PATH)) {
    console.error(`缺少 ${BASELINE_FILE}，先手工运行：node scripts/check-platform-router-fingerprint.mjs --update`)
    process.exit(1)
  }

  const baseline = JSON.parse(readFileSync(BASELINE_PATH, 'utf8'))
  const diffs = diffFingerprint(baseline, current)
  if (diffs.length) {
    console.error('平台路由指纹不一致：')
    diffs.slice(0, 80).forEach((diff) => console.error(`  ${diff}`))
    if (diffs.length > 80) console.error(`  ... 另有 ${diffs.length - 80} 条差异`)
    console.error('如确认为有意新增/调整路由，请在独立 PR 中手工刷新基线并说明原因。')
    process.exit(1)
  }

  const duplicateGroups = Object.keys(current.duplicateTopLevelPaths).length
  console.log(`平台路由指纹 OK：${current.topLevelRouteCount} 个顶层路由，${duplicateGroups} 组重复顶层 path 已锁定。`)
}

if (process.argv[1] && process.argv[1].endsWith('check-platform-router-fingerprint.mjs')) main()

const fs = require('fs')
const path = require('path')
const zlib = require('zlib')
const parser = require('@babel/parser')
const vueCompiler = require('vue-template-compiler')

const root = path.resolve(__dirname, '..')

const sourceChecks = [
  {
    file: 'src/router/views/index.js',
    forbidden: /webpackChunkName:\s*["']page["']/,
    message: 'route views must not merge unrelated lazy pages into the shared "page" chunk'
  },
  {
    file: 'src/router/page/index.js',
    forbidden: /webpackChunkName:\s*["']page["']/,
    message: 'static page routes must not merge unrelated lazy pages into the shared "page" chunk'
  },
  {
    file: 'src/main.js',
    forbidden: /import\s+VueAwesomeSwiper\s+from\s+['"]vue-awesome-swiper['"]/,
    message: 'swiper must be loaded by the pages that need it, not by the app entry'
  },
  {
    file: 'src/main.js',
    forbidden: /import\s+Print\s+from\s+['"]vue-print-nb['"]/,
    message: 'print plugin must be loaded by the pages that need it, not by the app entry'
  },
  {
    file: 'src/main.js',
    forbidden: /import\s+vueJsonTreeView\s+from\s+['"]vue-json-tree-view['"]/,
    message: 'json tree view must be loaded by the pages that need it, not by the app entry'
  },
  {
    file: 'src/router/avue-router.js',
    forbidden: /require\(\[/,
    message: 'dynamic menu routes must use webpack import() splitting instead of expression require()'
  }
]

const configChecks = [
  {
    file: 'vue.config.js',
    required: /splitChunks/,
    message: 'webpack splitChunks configuration is required'
  },
  {
    file: 'vue.config.js',
    required: /maxAssetSize/,
    message: 'webpack asset-size budget is required'
  },
  {
    file: 'vue.config.js',
    required: /maxEntrypointSize/,
    message: 'webpack entrypoint-size budget is required'
  }
]

function findSourceFiles(dir) {
  return fs.readdirSync(path.join(root, dir), { withFileTypes: true }).flatMap(entry => {
    const relativePath = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      return findSourceFiles(relativePath)
    }
    return /\.(js|vue)$/.test(entry.name) ? [relativePath] : []
  })
}

function getScriptContent(file) {
  const content = fs.readFileSync(path.join(root, file), 'utf8')
  if (file.endsWith('.vue')) {
    const component = vueCompiler.parseComponent(content)
    return component.script ? component.script.content : ''
  }
  return content
}

function parseScript(file) {
  return parser.parse(getScriptContent(file), {
    sourceType: 'unambiguous',
    plugins: [
      'classProperties',
      'decorators-legacy',
      'dynamicImport',
      'jsx',
      'nullishCoalescingOperator',
      'objectRestSpread',
      'optionalChaining'
    ]
  })
}

function walkAst(node, visit) {
  if (!node || typeof node !== 'object') {
    return
  }

  visit(node)

  Object.keys(node).forEach(key => {
    if (key === 'loc' || key === 'start' || key === 'end') {
      return
    }
    const child = node[key]
    if (Array.isArray(child)) {
      child.forEach(item => walkAst(item, visit))
      return
    }
    walkAst(child, visit)
  })
}

function isLocalLrzBundlePath(source) {
  const normalized = source.replace(/\\/g, '/')
  return /(^|\/)lrz\.all\.bundle(\.js)?$/.test(normalized)
}

function findForbiddenLrzImports(file) {
  const forbiddenSources = new Set()
  const ast = parseScript(file)

  walkAst(ast, node => {
    if (node.type === 'ImportDeclaration' && node.source && node.source.value) {
      const source = node.source.value
      if (source === 'lrz' || isLocalLrzBundlePath(source)) {
        forbiddenSources.add(source)
      }
    }

    if (
      node.type === 'CallExpression' &&
      node.callee &&
      node.callee.type === 'Identifier' &&
      node.callee.name === 'require' &&
      node.arguments &&
      node.arguments[0] &&
      node.arguments[0].type === 'StringLiteral'
    ) {
      const source = node.arguments[0].value
      if (source === 'lrz' || isLocalLrzBundlePath(source)) {
        forbiddenSources.add(source)
      }
    }
  })

  return [...forbiddenSources]
}

const failures = []

sourceChecks.forEach(check => {
  const fullPath = path.join(root, check.file)
  const content = fs.readFileSync(fullPath, 'utf8')
  if (check.forbidden.test(content)) {
    failures.push(`${check.file}: ${check.message}`)
  }
})

configChecks.forEach(check => {
  const fullPath = path.join(root, check.file)
  const content = fs.readFileSync(fullPath, 'utf8')
  if (!check.required.test(content)) {
    failures.push(`${check.file}: ${check.message}`)
  }
})

const lrzImports = findSourceFiles('src').map(file => ({
  file,
  sources: findForbiddenLrzImports(file)
}))

const localLrzBundleImports = lrzImports
  .filter(item => item.sources.some(isLocalLrzBundlePath))
  .map(item => item.file)

const staticLrzImports = lrzImports
  .filter(item => item.sources.includes('lrz'))
  .map(item => item.file)

if (localLrzBundleImports.length > 0) {
  failures.push(
    [
      'lrz must be imported from the npm package instead of src/util/lrz.all.bundle.js',
      ...localLrzBundleImports.map(file => `  ${file}`)
    ].join('\n')
  )
}

if (staticLrzImports.length > 0) {
  failures.push(
    [
      'lrz must be loaded through src/util/load-lrz.js to keep it out of the app entrypoint',
      ...staticLrzImports.map(file => `  ${file}`)
    ].join('\n')
  )
}

const distJsDir = path.join(root, 'dist', 'js')
if (fs.existsSync(distJsDir)) {
  const oversizedAssets = fs.readdirSync(distJsDir)
    .filter(fileName => fileName.endsWith('.js'))
    .map(fileName => {
      const filePath = path.join(distJsDir, fileName)
      const rawBytes = fs.statSync(filePath).size
      const gzipBytes = zlib.gzipSync(fs.readFileSync(filePath)).length
      return { fileName, rawBytes, gzipBytes }
    })
    .filter(asset => asset.rawBytes > 1024 * 1024 || asset.gzipBytes > 384 * 1024)

  oversizedAssets.forEach(asset => {
    failures.push(
      `dist/js/${asset.fileName}: ${(asset.rawBytes / 1024).toFixed(1)} KiB raw, ` +
      `${(asset.gzipBytes / 1024).toFixed(1)} KiB gzip exceeds bundle budget`
    )
  })
}

if (failures.length > 0) {
  console.error('Bundle optimization check failed:')
  failures.forEach(failure => console.error(`- ${failure}`))
  process.exit(1)
}

console.log('Bundle optimization check passed')

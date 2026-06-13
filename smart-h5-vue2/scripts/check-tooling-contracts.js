const fs = require('fs')
const path = require('path')
const assert = require('assert')

const rootDir = path.resolve(__dirname, '..')

const packageJson = JSON.parse(fs.readFileSync(path.join(rootDir, 'package.json'), 'utf8'))
const nvmrc = fs.readFileSync(path.join(rootDir, '.nvmrc'), 'utf8').trim()
const readme = fs.readFileSync(path.join(rootDir, 'README.md'), 'utf8')
const gitignore = fs.readFileSync(path.join(rootDir, '.gitignore'), 'utf8')
const pnpmWorkspace = fs.readFileSync(path.join(rootDir, 'pnpm-workspace.yaml'), 'utf8')

assert.strictEqual(packageJson.packageManager, 'pnpm@11.4.0', 'packageManager must pin pnpm 11.4.0')
assert.strictEqual(packageJson.engines.node, '>=22', 'Node engine must support Node 22 and newer')
assert.strictEqual(packageJson.engines.pnpm, '>=11 <12', 'pnpm engine must stay on pnpm 11')
for (const dependency of [
  'crypto-js',
  'pdfjs-dist',
  'vue-resize-sensor'
]) {
  assert(packageJson.dependencies[dependency], `${dependency} must be a direct dependency for pnpm builds`)
}

for (const dependency of [
  'babel-loader',
  'cache-loader',
  'css-loader',
  'eslint-loader',
  'file-loader',
  'thread-loader',
  'url-loader',
  'vue-loader',
  'vue-style-loader',
  'worker-loader'
]) {
  assert(packageJson.devDependencies[dependency], `${dependency} must be a direct dependency for pnpm builds`)
}
assert.strictEqual(nvmrc, '22', '.nvmrc must use Node 22 as the baseline version')
assert(fs.existsSync(path.join(rootDir, 'pnpm-lock.yaml')), 'pnpm-lock.yaml must be committed for deterministic installs')

for (const command of [
  'pnpm install',
  'pnpm run serve',
  'pnpm run build',
  'pnpm run lint',
  'pnpm run test'
]) {
  assert(readme.includes(command), `README.md must document ${command}`)
}

assert(!/\bnpm install\b/.test(readme), 'README.md must not document npm install')
assert(!/\bnpm run\b/.test(readme), 'README.md must not document npm run commands')
assert(!/^pnpm-lock\.yaml$/m.test(gitignore), '.gitignore must not ignore pnpm-lock.yaml')
assert(!/set this to true or false/.test(pnpmWorkspace), 'pnpm-workspace.yaml must not keep approve-builds placeholders')

for (const dependency of [
  "'@parcel/watcher': true",
  'core-js: true',
  'ejs: true',
  'fsevents: true',
  'husky: true',
  'swiper: true',
  'yorkie: true'
]) {
  assert(pnpmWorkspace.includes(dependency), `pnpm-workspace.yaml must explicitly allow ${dependency}`)
}

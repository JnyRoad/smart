#!/usr/bin/env node

import { randomBytes } from 'node:crypto'
import { execFile } from 'node:child_process'
import { access, chmod, writeFile } from 'node:fs/promises'
import { createServer } from 'node:net'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { promisify } from 'node:util'

const here = dirname(fileURLToPath(import.meta.url))
const defaultOutput = resolve(here, '../docker/app-demo/.env.local')
const defaultGatewayPort = 19990
const defaultNacosPort = 18849
const execFileAsync = promisify(execFile)

function parseArguments(args) {
  let outputPath = defaultOutput
  let gatewayPort = defaultGatewayPort
  let nacosPort = defaultNacosPort
  for (let index = 0; index < args.length; index += 1) {
    const argument = args[index]
    const value = args[index + 1]
    if (argument === '--output' && value) { outputPath = resolve(value); index += 1; continue }
    if (argument === '--gateway-port' && value) { gatewayPort = Number(value); index += 1; continue }
    if (argument === '--nacos-port' && value) { nacosPort = Number(value); index += 1; continue }
    throw new Error(`不支持或缺少参数值：${argument}`)
  }
  for (const port of [gatewayPort, nacosPort]) {
    if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('端口必须是 1 到 65535 的整数')
  }
  if (gatewayPort === nacosPort) throw new Error('网关端口和 Nacos 端口不能相同')
  return { outputPath, gatewayPort, nacosPort }
}

function password() {
  let part = ''
  while (part.length < 24) part += randomBytes(24).toString('base64url').replaceAll(/[^A-Za-z0-9]/g, '')
  return `A9a${part.slice(0, 24)}`
}

async function outputMustNotExist(path) {
  try { await access(path) } catch (error) { if (error?.code === 'ENOENT') return; throw error }
  throw new Error(`环境文件已存在，拒绝覆盖：${path}`)
}

async function dockerResourceMustNotExist() {
  const checks = [
    ['container', 'ls', '--all', '--filter', 'name=^/smart-app-demo-', '--format', '{{.Names}}'],
    ['volume', 'ls', '--filter', 'name=^smart-app-demo-', '--format', '{{.Name}}'],
    ['network', 'ls', '--filter', 'name=^smart-app-demo-network$', '--format', '{{.Name}}'],
  ]
  for (const args of checks) {
    let output
    try { output = await execFileAsync('docker', args, { encoding: 'utf8', timeout: 10000 }) } catch {
      throw new Error('Docker 资源探测失败，拒绝创建本机演示配置')
    }
    if (output.stdout.trim()) throw new Error('检测到 smart-app-demo 已有资源，拒绝创建新凭据或接管资源')
  }
}

async function portMustBeFree(port) {
  const server = createServer()
  try {
    await new Promise((resolveListen, rejectListen) => {
      server.once('error', rejectListen)
      server.listen({ host: '127.0.0.1', port, exclusive: true }, resolveListen)
    })
  } catch (error) {
    if (error?.code === 'EADDRINUSE') throw new Error(`127.0.0.1:${port} 已被占用，拒绝创建配置`)
    throw error
  } finally {
    if (server.listening) await new Promise((resolveClose, rejectClose) => server.close(error => error ? rejectClose(error) : resolveClose()))
  }
}

async function bcrypt(value) {
  let result
  try { result = await execFileAsync('htpasswd', ['-bnBC', '10', '', value], { encoding: 'utf8', timeout: 10000 }) }
  catch { throw new Error('未找到可用的 htpasswd，无法安全生成本机演示密码哈希') }
  const separator = result.stdout.indexOf(':')
  const hash = separator >= 0 ? result.stdout.slice(separator + 1).trim() : ''
  // 当前 Smart 依赖的 Spring Security 5.1 只接受 $2a$；Apache htpasswd 在部分系统上生成 $2y$。
  const compatibleHash = hash.startsWith('$2y$') ? `$2a$${hash.slice(4)}` : hash
  if (!compatibleHash.startsWith('$2a$') || compatibleHash.length !== 60) {
    throw new Error('htpasswd 未返回兼容的 BCrypt 哈希，拒绝创建配置')
  }
  return compatibleHash
}

async function main() {
  const { outputPath, gatewayPort, nacosPort } = parseArguments(process.argv.slice(2))
  await outputMustNotExist(outputPath)
  await dockerResourceMustNotExist()
  await Promise.all([portMustBeFree(gatewayPort), portMustBeFree(nacosPort)])

  const oraclePassword = password()
  const schemaPassword = password()
  const oauthSecret = password()
  const userPassword = password()
  const userHash = await bcrypt(userPassword)
  // Compose 读取 --env-file 时会展开 $；写为 $$ 后由 Oracle 种子脚本还原为 BCrypt 所需的 $。
  const composeEscapedUserHash = userHash.split('$').join('$$')
  const content = [
    '# 仅限 smart-app-demo 本机虚构数据；不得提交或复制到其他环境。',
    `DOCKER_PLATFORM=${process.arch === 'arm64' ? 'linux/arm64' : 'linux/amd64'}`,
    `SMART_APP_DEMO_GATEWAY_HOST_PORT=${gatewayPort}`,
    `SMART_APP_DEMO_NACOS_HOST_PORT=${nacosPort}`,
    `SMART_APP_DEMO_ORACLE_SYS_PASSWORD=${oraclePassword}`,
    'SMART_APP_DEMO_DB_USERNAME=SMART_APP_DEMO',
    `SMART_APP_DEMO_DB_PASSWORD=${schemaPassword}`,
    `SMART_APP_DEMO_OAUTH_CLIENT_SECRET=${oauthSecret}`,
    `SMART_APP_DEMO_USER_PASSWORD=${userPassword}`,
    `SMART_APP_DEMO_USER_PASSWORD_BCRYPT=${composeEscapedUserHash}`,
    '',
  ].join('\n')
  await writeFile(outputPath, content, { encoding: 'utf8', flag: 'wx', mode: 0o600 })
  await chmod(outputPath, 0o600)
  console.log(`已创建隔离本机演示配置：${outputPath}`)
  console.log(`仅计划监听 127.0.0.1:${gatewayPort}（网关）与 127.0.0.1:${nacosPort}（Nacos）`)
}

main().catch(error => { console.error(`生成失败：${error.message}`); process.exitCode = 1 })

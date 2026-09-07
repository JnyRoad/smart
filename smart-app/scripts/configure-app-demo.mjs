#!/usr/bin/env node

import { readFile, writeFile } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const here = dirname(fileURLToPath(import.meta.url))
const runtimePath = resolve(here, '../config/runtime.uts')
const environmentPath = resolve(here, '../../docker/app-demo/.env.local')

function parseEnvironment(content) {
  const values = {}
  for (const line of content.split(/\r?\n/)) {
    if (!line || line.startsWith('#')) continue
    const separator = line.indexOf('=')
    if (separator > 0) values[line.slice(0, separator)] = line.slice(separator + 1)
  }
  return values
}

function replaceExactly(content, previous, next) {
  const occurrences = content.split(previous).length - 1
  if (occurrences != 1) throw new Error('运行时配置不是预期状态，拒绝覆盖')
  return content.replace(previous, next)
}

async function enable() {
  const environment = parseEnvironment(await readFile(environmentPath, 'utf8'))
  const port = environment.SMART_APP_DEMO_GATEWAY_HOST_PORT ?? ''
  if (!/^[1-9][0-9]{0,4}$/.test(port) || Number(port) > 65535) throw new Error('本机网关端口配置无效')
  const runtime = await readFile(runtimePath, 'utf8')
  const configured = replaceExactly(
    runtime,
    "  apiBaseUrl: '',\n  allowLoopbackHttp: false,",
    `  apiBaseUrl: 'http://127.0.0.1:${port}',\n  allowLoopbackHttp: true,`,
  )
  await writeFile(runtimePath, configured, 'utf8')
  console.log(`已启用仅本机回环的 App 演示网关：127.0.0.1:${port}`)
}

async function disable() {
  const runtime = await readFile(runtimePath, 'utf8')
  const configured = runtime.replace(
    /  apiBaseUrl: 'http:\/\/127\.0\.0\.1:[1-9][0-9]{0,4}',\n  allowLoopbackHttp: true,/,
    "  apiBaseUrl: '',\n  allowLoopbackHttp: false,",
  )
  if (configured === runtime) throw new Error('未发现已启用的本机回环配置，拒绝修改')
  await writeFile(runtimePath, configured, 'utf8')
  console.log('已恢复 App 的默认安全运行时配置')
}

async function main() {
  const action = process.argv[2]
  if (action === '--enable') return enable()
  if (action === '--disable') return disable()
  throw new Error('仅支持 --enable 或 --disable')
}

main().catch(error => { console.error(`配置失败：${error.message}`); process.exitCode = 1 })

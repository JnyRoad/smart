#!/usr/bin/env node

import { readFile } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { createClientApi } from '../services/client-api.uts'

const here = dirname(fileURLToPath(import.meta.url))
const environmentPath = resolve(here, '../../docker/app-demo/.env.local')

function expect(condition, message) {
  if (!condition) throw new Error(message)
}

function parseEnvironment(content) {
  const values = {}
  for (const line of content.split(/\r?\n/)) {
    if (!line || line.startsWith('#')) continue
    const separator = line.indexOf('=')
    if (separator > 0) values[line.slice(0, separator)] = line.slice(separator + 1)
  }
  return values
}

async function main() {
  const environment = parseEnvironment(await readFile(environmentPath, 'utf8'))
  const port = environment.SMART_APP_DEMO_GATEWAY_HOST_PORT ?? ''
  const password = environment.SMART_APP_DEMO_USER_PASSWORD ?? ''
  expect(/^[1-9][0-9]{0,4}$/.test(port), '本机网关端口配置无效')
  expect(password.length >= 12, '本机演示账号密码配置无效')
  const api = createClientApi(`http://127.0.0.1:${port}`, async request => {
    const response = await fetch(request.url, {
      method: request.method,
      headers: request.headers,
      body: request.body == null ? undefined : JSON.stringify(request.body),
    })
    const contentType = response.headers.get('content-type') ?? ''
    return { status: response.status, body: contentType.includes('application/json') ? await response.json() : null }
  }, true)
  const identities = [
    ['APP_EMPLOYEE', 'employee'],
    ['APP_OUTSOURCE', 'outsourced'],
    ['APP_DISPATCH', 'dispatched'],
  ]
  for (const [staffNo, employmentType] of identities) {
    const session = await api.login(staffNo, password)
    expect(session.identity.staffNo === staffNo, `${staffNo} 身份不匹配`)
    expect(session.identity.employmentType === employmentType, `${staffNo} 用工类别不匹配`)
  }
  const employee = await api.login('APP_EMPLOYEE', password)
  const apps = await api.apps(employee.token)
  expect(apps.some(app => app.id === 'item-pass-apply'), 'App 未取得物品放行申请模块')
  console.log('通过：uni-app x 客户端适配器已连接本机网关并验证三类人员登录与模块目录。')
}

main().catch(error => { console.error(`App 本机接口验收失败：${error.message}`); process.exitCode = 1 })

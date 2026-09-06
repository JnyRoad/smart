#!/usr/bin/env node

import { randomUUID } from 'node:crypto'
import { readFile } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const here = dirname(fileURLToPath(import.meta.url))
const envPath = resolve(here, '.env.local')

function assertion(condition, message) {
  if (!condition) throw new Error(message)
}

function parseEnvironment(content) {
  const entries = {}
  for (const line of content.split(/\r?\n/)) {
    if (!line || line.startsWith('#')) continue
    const separator = line.indexOf('=')
    if (separator <= 0) continue
    entries[line.slice(0, separator)] = line.slice(separator + 1)
  }
  return entries
}

async function environment() {
  const values = parseEnvironment(await readFile(envPath, 'utf8'))
  const port = values.SMART_APP_DEMO_GATEWAY_HOST_PORT
  const password = values.SMART_APP_DEMO_USER_PASSWORD
  assertion(/^[1-9][0-9]{0,4}$/.test(port ?? ''), '本机网关端口配置无效')
  assertion(typeof password === 'string' && password.length >= 12, '本机演示账号密码配置无效')
  return { baseUrl: `http://127.0.0.1:${port}`, password }
}

async function request(baseUrl, path, { method = 'GET', token = '', body = undefined, key = '' } = {}) {
  const headers = { Accept: 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (key) headers['Idempotency-Key'] = key
  const response = await fetch(`${baseUrl}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const contentType = response.headers.get('content-type') ?? ''
  const json = contentType.includes('application/json') ? await response.json() : null
  return { status: response.status, json }
}

function expectStatus(name, result, expected) {
  assertion(result.status === expected, `${name}：期望 HTTP ${expected}，实际 ${result.status}`)
  return result.json
}

async function login(baseUrl, password, staffNo) {
  const result = await request(baseUrl, '/api/v1/sessions', {
    method: 'POST', body: { staffNo, password },
  })
  const session = expectStatus(`${staffNo} 统一登录`, result, 200)
  assertion(typeof session?.token === 'string' && session.token.length > 20, `${staffNo} 未取得有效会话`)
  const expiresAt = session?.expiresAt
  assertion(
    Number.isFinite(expiresAt) || (typeof expiresAt === 'string' && /^[0-9]{13}$/.test(expiresAt)),
    `${staffNo} 未取得有效会话期限`,
  )
  return session.token
}

async function identity(baseUrl, token, staffNo, employmentType) {
  const me = expectStatus(`${staffNo} 读取身份`, await request(baseUrl, '/api/v1/me', { token }), 200)
  assertion(me?.staffNo === staffNo, `${staffNo} 返回了错误身份`)
  assertion(me?.employmentType === employmentType, `${staffNo} 人员来源映射错误`)
  return me
}

function unique(prefix) {
  return `${prefix}:${randomUUID()}`
}

async function verifyReleaseFlow(baseUrl, password) {
  const employee = await login(baseUrl, password, 'APP_EMPLOYEE')
  const employeeIdentity = await identity(baseUrl, employee, 'APP_EMPLOYEE', 'employee')
  assertion(Array.isArray(employeeIdentity.permissions) && employeeIdentity.permissions.includes('item-pass:apply'), '员工未取得 item-pass:apply 权限')
  const apps = expectStatus('读取员工模块目录', await request(baseUrl, '/api/v1/me/apps', { token: employee }), 200)
  assertion(Array.isArray(apps) && apps.some(app => app?.id === 'item-pass-apply'), '员工未取得物品放行申请模块')
  const options = expectStatus('读取物品放行申请选项', await request(baseUrl, '/api/v1/item-passes/posts', { token: employee }), 200)
  assertion(Array.isArray(options?.posts) && options.posts.length >= 2, '物品放行申请选项不足')

  const created = expectStatus('提交物品放行申请', await request(baseUrl, '/api/v1/item-passes', {
    method: 'POST', token: employee, key: unique('demo-release-create'),
    body: {
      title: '本机演示保密物品放行', reason: 'Docker API 闭环验收',
      fromPostId: 'security-east', toPostId: 'security-west', supplierName: '', visitorName: '',
      materials: '演示设备', seals: ['SEAL-DEMO-001'],
    },
  }), 200)
  assertion(typeof created?.id === 'string' && created.id.startsWith('REL-'), '物品放行申请未返回单据标识')
  const releaseId = created.id

  const supervisor = await login(baseUrl, password, 'APP_SUPERVISOR')
  await identity(baseUrl, supervisor, 'APP_SUPERVISOR', 'employee')
  const approved = expectStatus('主管审批通过', await request(baseUrl, `/api/v1/item-passes/${encodeURIComponent(releaseId)}/actions`, {
    method: 'POST', token: supervisor, key: unique('demo-release-approve'),
    body: { action: 'approve', postId: '', comment: '本机演示审批通过' },
  }), 200)
  assertion(approved?.status === 'approved', '申请未进入已审批状态')

  const security = await login(baseUrl, password, 'APP_SECURITY')
  await identity(baseUrl, security, 'APP_SECURITY', 'outsourced')
  const originQueue = expectStatus('查询东门安检待办', await request(baseUrl, '/api/v1/item-passes?scope=execute&postId=security-east', { token: security }), 200)
  assertion(Array.isArray(originQueue) && originQueue.some(item => item?.id === releaseId), '东门安检待办未包含已审批单据')
  const departed = expectStatus('东门执行物品放行', await request(baseUrl, `/api/v1/item-passes/${encodeURIComponent(releaseId)}/actions`, {
    method: 'POST', token: security, key: unique('demo-release-depart'),
    body: {
      action: 'depart', postId: 'security-east', comment: '本机演示出发核验',
      execution: { mode: 'lock', escortProof: '', lockNo: 'LOCK-DEMO-001' },
    },
  }), 200)
  assertion(departed?.status === 'transporting', '单据未进入运输中状态')
  const destinationQueue = expectStatus('查询西门安检待办', await request(baseUrl, '/api/v1/item-passes?scope=execute&postId=security-west', { token: security }), 200)
  assertion(Array.isArray(destinationQueue) && destinationQueue.some(item => item?.id === releaseId), '西门安检待办未包含运输中单据')
  const completed = expectStatus('西门确认到达', await request(baseUrl, `/api/v1/item-passes/${encodeURIComponent(releaseId)}/actions`, {
    method: 'POST', token: security, key: unique('demo-release-arrive'),
    body: {
      action: 'arrive', postId: 'security-west', comment: '本机演示到达核验',
      execution: { mode: 'lock', escortProof: '', lockNo: 'LOCK-DEMO-001' },
    },
  }), 200)
  assertion(completed?.status === 'completed', '单据未完成闭环')
  const detail = expectStatus('申请人读取完成单据', await request(baseUrl, `/api/v1/item-passes/${encodeURIComponent(releaseId)}`, { token: employee }), 200)
  assertion(detail?.id === releaseId && detail?.status === 'completed', '申请人无法读取完成单据')
  const records = expectStatus('申请人查询物品放行记录', await request(baseUrl, '/api/v1/item-passes', { token: employee }), 200)
  assertion(Array.isArray(records) && records.some(item => item?.id === releaseId && item?.status === 'completed'), '物品放行记录未持久化')
  return security
}

async function verifySupplierFlow(baseUrl, securityToken) {
  const verify = async () => expectStatus('核验供应商厂牌', await request(baseUrl, '/api/v1/visitor-checks', {
    method: 'POST', token: securityToken, body: { credentialCode: '900000001', postId: 'security-east' },
  }), 200)
  const record = async (verification, direction) => expectStatus(`登记供应商${direction === 'enter' ? '进入' : '离开'}`, await request(baseUrl, '/api/v1/visitor-passes', {
    method: 'POST', token: securityToken, key: unique(`demo-visitor-${direction}`),
    body: { verificationId: verification.id, postId: 'security-east', direction },
  }), 200)

  const first = await verify()
  assertion(typeof first?.id === 'string' && Array.isArray(first.allowedDirections), '厂牌核验未返回可登记方向')
  const firstDirection = first.allowedDirections.includes('enter') ? 'enter' : 'leave'
  assertion(first.allowedDirections.includes(firstDirection), '首次厂牌核验无可用方向')
  const firstEvent = await record(first, firstDirection)
  const second = await verify()
  const secondDirection = firstDirection === 'enter' ? 'leave' : 'enter'
  assertion(second?.allowedDirections?.includes(secondDirection), '厂牌复核未返回相反方向')
  const secondEvent = await record(second, secondDirection)
  const events = expectStatus('查询供应商通行记录', await request(baseUrl, '/api/v1/visitor-passes', { token: securityToken }), 200)
  assertion(Array.isArray(events) && events.some(event => event?.id === firstEvent?.id)
    && events.some(event => event?.id === secondEvent?.id), '供应商进出记录未持久化')
  const smsCode = await request(baseUrl, '/api/v1/visitor-checks', {
    method: 'POST', token: securityToken, body: { credentialCode: '123456', postId: 'security-east' },
  })
  expectStatus('拒绝以六位预约码替代厂牌人员记录 ID', smsCode, 404)
}

async function main() {
  const { baseUrl, password } = await environment()
  const outsource = await login(baseUrl, password, 'APP_OUTSOURCE')
  await identity(baseUrl, outsource, 'APP_OUTSOURCE', 'outsourced')
  const dispatched = await login(baseUrl, password, 'APP_DISPATCH')
  await identity(baseUrl, dispatched, 'APP_DISPATCH', 'dispatched')
  const security = await verifyReleaseFlow(baseUrl, password)
  await verifySupplierFlow(baseUrl, security)
  const invalid = await request(baseUrl, '/api/v1/sessions', {
    method: 'POST', body: { staffNo: `APP_INVALID_${randomUUID().replaceAll('-', '')}`, password },
  })
  expectStatus('拒绝未知人员统一登录', invalid, 401)
  console.log('通过：三类人员统一登录、物品申请审批与双岗执行、供应商厂牌进出记录、预约码拒绝与未知账号拒绝。')
}

main().catch(error => {
  console.error(`本机演示 API 验收失败：${error.message}`)
  process.exitCode = 1
})

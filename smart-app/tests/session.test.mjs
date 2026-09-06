import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createClientApi } from '../services/client-api.uts'
import { createSessionController } from '../state/session-controller.uts'
const identity = { subjectId: 'test-1', staffNo: '0001', displayName: '测试人员', employmentType: 'employee', organization: '测试单位', permissions: [], posts: [] }
const payload = { token: 'test-token', expiresAt: Date.now() + 60000, identity }
const apps = [{ id: 'item-pass-apply', category: '物品放行', title: '物品放行申请', description: '提交申请', permission: 'item-pass:apply', sort: 10 }]

test('没有集成地址时不发送登录请求', async () => {
  let sent = false
  const api = createClientApi('', async () => { sent = true })
  assert.ok(api, '需要可调用的客户端服务')
  await assert.rejects(() => api.login('0001', 'secret'), /尚未配置/)
  assert.equal(sent, false)
})
test('真实登录密码仅在POST正文，拒绝明文远程地址', async () => {
  const requests = []
	const api = createClientApi('https://api.example.invalid', async r => {
    requests.push(r)
		if (requests.length === 1) return { status: 200, body: { token: payload.token, expiresAt: payload.expiresAt } }
		if (requests.length === 2) return { status: 200, body: identity }
		return { status: 200, body: apps }
	})
  assert.ok(api, '需要可调用的客户端服务')
	await api.login('0001', 'secret')
  assert.equal(requests.length, 2)
  assert.equal(requests[0].method, 'POST')
	assert.equal(requests[0].url, 'https://api.example.invalid/api/v1/sessions')
  assert.equal(requests[0].url.includes('secret'), false)
  assert.equal(requests[0].body.password, 'secret')
  assert.equal(requests[1].method, 'GET')
	assert.equal(requests[1].url, 'https://api.example.invalid/api/v1/me')
  assert.equal(requests[1].body, null)
  assert.match(requests[1].headers.Authorization, /^Bearer /)
	assert.deepEqual(await api.apps(payload.token), apps)
	assert.equal(requests[2].url, 'https://api.example.invalid/api/v1/me/apps')
	await assert.rejects(() => createClientApi('http://api.example.invalid', async () => {}).login('1', '2'), /HTTPS/)
})
test('登录期限兼容 Smart 网关传输的十进制毫秒字符串，并只在显式本机开关时允许回环 HTTP', async () => {
  const expiresAt = String(Date.now() + 60_000)
  const api = createClientApi('http://127.0.0.1:19990', async request => {
    if (request.url.endsWith('/sessions')) return { status: 200, body: { token: payload.token, expiresAt } }
    return { status: 200, body: identity }
  }, true)
  const result = await api.login('0001', 'secret')
  assert.equal(result.expiresAt, Number(expiresAt))
  await assert.rejects(
    () => createClientApi('http://127.0.0.1:19990', async () => ({ status: 200, body: {} })).login('0001', 'secret'),
    /HTTPS|回环/,
  )
})
test('工作台模块必须是服务端授权的固定元数据', async () => {
	const api = createClientApi('https://example.invalid', async () => {
		return { status: 200, body: [{ id: 'unsafe', route: '/anything' }] }
	})
	await assert.rejects(() => api.apps('token'), /工作台响应/)
})
test('拒绝缺失身份与凭据的成功响应，不伪造默认身份', async () => {
  const api = createClientApi('https://example.invalid', async () => ({ status: 200, body: {} }))
  assert.ok(api, '需要可调用的客户端服务')
  await assert.rejects(() => api.login('1', '2'), /登录响应/)
})
test('业务写入收到401后不重试', async () => {
  let count = 0
  const api = createClientApi('https://example.invalid', async () => { count++; return { status: 401, body: {} } })
  assert.ok(api, '需要可调用的客户端服务')
  await assert.rejects(() => api.act('token', 'r1', 'depart', 'p1', 'note', 'operation-1'), /登录已失效/)
  assert.equal(count, 1)
})
test('退出后旧登录响应不得恢复身份', async () => {
  let resolve
  const api = { login: () => new Promise(r => { resolve = r }) }
  const changed = []
  const c = createSessionController(api, session => changed.push(session))
  assert.ok(c, '需要会话控制器')
  const pending = c.login('1', '2')
  c.logout()
  resolve(payload)
  await pending
  assert.equal(c.current(), null)
  assert.equal(changed.at(-1), null)
})
test('演示会话没有真实凭据，过期真实会话自动清除', async () => {
  const c = createSessionController({ login: async () => ({ ...payload, expiresAt: Date.now()-1 }) }, () => {})
  assert.ok(c, '需要会话控制器')
  c.enterDemo(identity)
  assert.equal(c.current().mode, 'demo')
  assert.equal(c.current().token, '')
  await assert.rejects(() => c.login('1', '2'), /过期/)
  assert.equal(c.current(), null)
})

test('成功状态也必须拒绝畸形单据列表和提交结果', async () => {
  for (const body of [[null], [{ id: 'r1', kind: 'item-pass' }]]) {
    const api = createClientApi('https://example.invalid', async () => ({ status: 200, body }))
    await assert.rejects(() => api.list('token'), /单据响应/)
  }
  const api = createClientApi('https://example.invalid', async () => ({ status: 200, body: {} }))
  await assert.rejects(() => api.submit('token', {}, 'op'), /申请|供应商|厂牌/)
})

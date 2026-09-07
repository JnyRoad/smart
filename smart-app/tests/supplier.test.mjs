import { test } from 'node:test'
import assert from 'node:assert/strict'

import {
	DEMO_SUPPLIER_BADGES,
	verifyDemoSupplierBadge,
	createSupplierPassage,
	applySupplierPassage,
	cloneSupplierVerification,
	validateSupplierVerification,
	supplierTimeValue,
} from '../core/supplier-access.uts'
import { createClientApi } from '../services/client-api.uts'

const operator = {
	subjectId: 'demo-security-001',
	displayName: '演示安检',
	employmentType: 'employee',
	posts: [{ id: 'east-gate', name: '东门', parkId: 'demo-park', parkName: '演示园区' }],
}

const verificationResponse = {
	id: 'verification-1',
	badgeId: 'DEMO-BADGE-001',
	visitorName: '演示访客',
	supplierName: '演示供应商',
	admissionId: 'admission-1',
	postId: 'east-gate',
	areaName: '东门保密区',
	validFrom: '2026-09-05T00:00:00.000Z',
	validUntil: '2026-09-06T00:00:00.000Z',
	expiresAt: '2026-09-05T01:00:00.000Z',
	allowed: true,
	reason: '',
	presence: 'outside',
	allowedDirections: ['enter', 'leave'],
}

const passageResponse = {
	id: 'event-1',
	verificationId: 'verification-1',
	badgeId: 'DEMO-BADGE-001',
	visitorName: '演示访客',
	supplierName: '演示供应商',
	admissionId: 'admission-1',
	postId: 'east-gate',
	areaName: '东门保密区',
	direction: 'enter',
	operatorName: '演示安检',
	occurredAt: '2026-09-05T00:10:00.000Z',
}

const firstVerificationResponse = {
	...verificationResponse,
	id: 'verification-first',
	badgeId: '9223372036854775807',
	presence: 'unknown',
	allowedDirections: ['enter', 'leave'],
}

test('供应商厂牌核验只接受预置有效凭证，且核验本身不产生事件', () => {
	const result = verifyDemoSupplierBadge('DEMO-BADGE-001', 'east-gate', 1_000_000)
	assert.equal(result.allowed, true)
	assert.equal(result.badgeId, 'DEMO-BADGE-001')
	assert.equal(result.postId, 'east-gate')
	assert.deepEqual(result.allowedDirections, ['enter', 'leave'])
	assert.equal(result.presence, 'outside')
	assert.equal(result.events, undefined)
	assert.equal(verifyDemoSupplierBadge('UNKNOWN-BADGE', 'east-gate', 1_000_000), null)
})

test('过期、无权限和跨岗位厂牌不能通过核验或登记', () => {
	const expired = verifyDemoSupplierBadge('DEMO-BADGE-EXPIRED', 'east-gate', 1_000_000)
	assert.equal(expired.allowed, false)
	assert.match(expired.reason, /过期/)
	assert.equal(verifyDemoSupplierBadge('DEMO-BADGE-DENIED', 'east-gate', 1_000_000).allowed, false)
	const crossPost = verifyDemoSupplierBadge('DEMO-BADGE-001', 'west-gate', 1_000_000)
	assert.equal(crossPost.allowed, false)
	assert.match(crossPost.reason, /岗位|区域/)
	assert.equal(validateSupplierVerification(crossPost, operator, 1_000_000), false)
})

test('进入和离开是独立事件，重复方向幂等且不能越过当前状态', () => {
	const verification = verifyDemoSupplierBadge('DEMO-BADGE-001', 'east-gate', 1_000_000)
	const first = createSupplierPassage(verification, operator, 'east-gate', 'enter', 'idem-enter', 1_000_001)
	assert.equal(first.direction, 'enter')
	assert.equal(first.badgeId, 'DEMO-BADGE-001')
	const inside = applySupplierPassage(verification, 'enter')
	assert.equal(inside.presence, 'inside')
	assert.throws(
		() => createSupplierPassage(inside, operator, 'east-gate', 'enter', 'idem-enter-duplicate', 1_000_002),
		/已在.*内|重复|状态/,
	)
	const leave = createSupplierPassage(inside, operator, 'east-gate', 'leave', 'idem-leave', 1_000_003)
	assert.equal(leave.direction, 'leave')
	assert.equal(applySupplierPassage(inside, 'leave').presence, 'outside')
})

test('首次核验保留未知状态，并允许服务端明确授权的进入或离开作为首个事件', () => {
	const now = Date.parse('2026-09-05T00:10:00.000Z')
	assert.equal(validateSupplierVerification(firstVerificationResponse, operator, now), true)
	assert.equal(cloneSupplierVerification(firstVerificationResponse).presence, 'unknown')

	for (const [direction, expectedPresence] of [['enter', 'inside'], ['leave', 'outside']]) {
		const event = createSupplierPassage(
			firstVerificationResponse,
			operator,
			'east-gate',
			direction,
			'idem-first-' + direction,
			now,
		)
		assert.equal(event.direction, direction)
		assert.equal(applySupplierPassage(firstVerificationResponse, direction).presence, expectedPresence)
	}

	assert.throws(
		() => createSupplierPassage(
			{ ...firstVerificationResponse, allowedDirections: ['enter'] },
			operator,
			'east-gate',
			'leave',
			'idem-first-denied',
			now,
		),
		/不允许.*方向/,
	)
})

test('核心核验与状态转换拒绝unknown之外的畸形状态', () => {
	const malformed = { ...firstVerificationResponse, presence: 'pending' }
	const now = Date.parse('2026-09-05T00:10:00.000Z')
	assert.equal(validateSupplierVerification(malformed, operator, now), false)
	assert.throws(() => cloneSupplierVerification(malformed), /状态/)
	assert.throws(() => applySupplierPassage(malformed, 'enter'), /状态/)
})

test('厂牌核验结果在岗位切换、身份切换或有效期结束后失效', () => {
	const verification = verifyDemoSupplierBadge('DEMO-BADGE-001', 'east-gate', 1_000_000)
	assert.equal(validateSupplierVerification(verification, operator, 1_000_001), true)
	assert.equal(validateSupplierVerification(verification, null, 1_000_001), false)
	assert.equal(validateSupplierVerification(verification, { ...operator, posts: [{ ...operator.posts[0], id: 'west-gate' }] }, 1_000_001), false)
	assert.equal(validateSupplierVerification(verification, operator, supplierTimeValue(verification.expiresAt) + 1), false)
})

test('演示厂牌种子保持固定编号，过期和拒绝种子均不授予权限', () => {
	assert.deepEqual(
		DEMO_SUPPLIER_BADGES.map(item => item.badgeId),
		['DEMO-BADGE-001', 'DEMO-BADGE-EXPIRED', 'DEMO-BADGE-DENIED'],
	)
	assert.equal(DEMO_SUPPLIER_BADGES.find(item => item.badgeId === 'DEMO-BADGE-EXPIRED').allowed, false)
	assert.equal(DEMO_SUPPLIER_BADGES.find(item => item.badgeId === 'DEMO-BADGE-DENIED').allowed, false)
})

test('供应商真实API使用独立核验、事件和查询端点，并强制事件幂等键', async () => {
	const requests = []
	const api = createClientApi('https://example.invalid', async request => {
		requests.push(request)
		if (request.method === 'GET') return { status: 200, body: [passageResponse] }
		if (request.url.endsWith('/visitor-checks')) return { status: 200, body: verificationResponse }
		return { status: 200, body: passageResponse }
	})
	assert.deepEqual(await api.verifySupplierBadge('token-1', 'DEMO-BADGE-001', 'east-gate'), verificationResponse)
	assert.deepEqual(await api.recordSupplierPassage('token-1', 'verification-1', 'east-gate', 'enter', 'idem-1'), passageResponse)
	assert.deepEqual(await api.listSupplierPassages('token-1'), [passageResponse])
	assert.equal(requests[0].method, 'POST')
	assert.equal(requests[0].url, 'https://example.invalid/api/v1/visitor-checks')
	assert.deepEqual(requests[0].body, { credentialCode: 'DEMO-BADGE-001', postId: 'east-gate' })
	assert.equal(requests[1].url, 'https://example.invalid/api/v1/visitor-passes')
	assert.equal(requests[1].headers['Idempotency-Key'], 'idem-1')
	assert.deepEqual(requests[1].body, { verificationId: 'verification-1', postId: 'east-gate', direction: 'enter' })
	assert.equal(requests[2].method, 'GET')
	assert.equal(requests[2].url, 'https://example.invalid/api/v1/visitor-passes')
	await assert.rejects(() => api.recordSupplierPassage('token-1', 'verification-1', 'east-gate', 'enter', ''), /幂等键/)
})

test('供应商真实API接受首次未知状态并原样转发长人员记录ID', async () => {
	const requests = []
	const api = createClientApi('https://example.invalid', async request => {
		requests.push(request)
		return { status: 200, body: firstVerificationResponse }
	})
	assert.deepEqual(
		await api.verifySupplierBadge('token-1', '9223372036854775807', 'east-gate'),
		firstVerificationResponse,
	)
	assert.deepEqual(requests[0].body, {
		credentialCode: '9223372036854775807',
		postId: 'east-gate',
	})
})

test('供应商真实API拒绝畸形核验/事件响应并沿用401认证保护', async () => {
	const malformed = createClientApi('https://example.invalid', async request => ({
		status: 200,
		body: request.url.endsWith('/visitor-checks') ? { ...verificationResponse, allowedDirections: ['hack'] } : {},
	}))
	await assert.rejects(() => malformed.verifySupplierBadge('token-1', 'code', 'east-gate'), /核验响应/)

	const malformedPresence = createClientApi('https://example.invalid', async () => ({
		status: 200,
		body: { ...verificationResponse, presence: 'pending' },
	}))
	await assert.rejects(() => malformedPresence.verifySupplierBadge('token-1', 'code', 'east-gate'), /核验响应/)

	let count = 0
	const unauthorized = createClientApi('https://example.invalid', async () => {
		count += 1
		return { status: 401, body: {} }
	})
	await assert.rejects(() => unauthorized.listSupplierPassages('token-1'), /登录已失效/)
	assert.equal(count, 1)
})

test('供应商API拒绝旧申请提交及与本次岗位/方向不绑定的响应', async () => {
	let count = 0
	const oldApplicationApi = createClientApi('https://example.invalid', async () => {
		count += 1
		return { status: 200, body: {} }
	})
	await assert.rejects(() => oldApplicationApi.submit('token-1', { kind: 'supplier' }, 'old-op'), /供应商|厂牌/)
	assert.equal(count, 0)

	const wrongPost = createClientApi('https://example.invalid', async () => ({
		status: 200,
		body: { ...verificationResponse, postId: 'west-gate' },
	}))
	await assert.rejects(() => wrongPost.verifySupplierBadge('token-1', 'DEMO-BADGE-001', 'east-gate'), /核验响应/)

	const wrongEvent = createClientApi('https://example.invalid', async () => ({
		status: 200,
		body: { ...passageResponse, direction: 'leave' },
	}))
	await assert.rejects(() => wrongEvent.recordSupplierPassage('token-1', 'verification-1', 'east-gate', 'enter', 'idem-2'), /通行事件响应/)
})

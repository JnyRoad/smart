import { test } from 'node:test'
import assert from 'node:assert/strict'
import { registerHooks } from 'node:module'

import * as workflow from '../core/workflow.uts'
import * as supplier from '../core/supplier-access.uts'
import { createClientApi } from '../services/client-api.uts'

registerHooks({
	resolve(name, context, next) {
		if (name === 'vue') {
			return {
				url: 'data:text/javascript,export const reactive = value => value; export const nextTick = callback => Promise.resolve().then(callback)',
				shortCircuit: true,
			}
		}
		return next(name, context)
	},
})

const storage = new Map()
let requests = []
let responseStatus = 200
let responseBody = []

globalThis.uni = {
	getStorageSync: key => storage.get(key),
	setStorageSync: (key, value) => storage.set(key, value),
	switchTab: () => {},
	reLaunch: () => {},
	navigateTo: () => {},
	showToast: () => {},
	request: options => {
		requests.push(options)
		const body = options.url.endsWith('/api/v1/sessions')
			? { token: responseBody.token, expiresAt: responseBody.expiresAt }
			: options.url.endsWith('/api/v1/me')
				? responseBody.identity
				: responseBody
		options.success({ statusCode: responseStatus, data: body })
	},
}

const { runtimeConfig } = await import('../config/runtime.uts')
runtimeConfig.apiBaseUrl = 'https://example.invalid'
const state = await import('../state/session.uts')

const security = {
	subjectId: 'security-1',
	staffNo: 'SEC-1',
	displayName: '演示安检',
	employmentType: 'employee',
	organization: '演示单位',
	permissions: ['item-pass:execute', 'item-pass:read', 'supplier:execute', 'supplier:read'],
	posts: [
		{ id: 'east-gate', name: '东门', parkId: 'demo-park', parkName: '演示园区' },
		{ id: 'west-gate', name: '西门', parkId: 'demo-park', parkName: '演示园区' },
	],
}

const approvalOnly = {
	...security,
	subjectId: 'approval-only-1',
	staffNo: 'APPROVER-1',
	displayName: '仅审批演示人员',
	permissions: ['item-pass:approve'],
}

function application(overrides = {}) {
	return {
		id: 'phase8-application',
		kind: 'item-pass',
		title: 'Phase8 演示放行',
		reason: '验证现场执行',
		fromPostId: 'east-gate',
		toPostId: 'west-gate',
		supplierName: '',
		visitorName: '',
		materials: '演示资料',
		seals: ['00001234'],
		applicantId: 'applicant-1',
		applicantName: '演示申请人',
		status: 'approved',
		timeline: [],
		...overrides,
	}
}

function escortExecution(proof = 'demo-escort-proof') {
	return { mode: 'escort', escortProof: proof, lockNo: '' }
}

function lockExecution(lockNo = '000012') {
	return { mode: 'lock', escortProof: '', lockNo }
}

test('领域层强制押运二选一并让抵达沿用出发方式', () => {
	const approved = application()
	const departedByEscort = workflow.transitionApplication(
		approved,
		security,
		'east-gate',
		'depart',
		'',
		escortExecution(),
	)
	assert.equal(departedByEscort.status, 'transporting')
	assert.deepEqual(departedByEscort.transport, { mode: 'escort', lockNo: '' })

	const arrivedByEscort = workflow.transitionApplication(
		departedByEscort,
		security,
		'west-gate',
		'arrive',
		'',
		escortExecution('new-arrival-proof'),
	)
	assert.equal(arrivedByEscort.status, 'completed')
	assert.deepEqual(arrivedByEscort.transport, { mode: 'escort', lockNo: '' })

	assert.throws(
		() => workflow.transitionApplication(approved, security, 'east-gate', 'depart', '', null),
		/押运|锁|核验|方式/,
	)
	assert.throws(
		() => workflow.transitionApplication(
			approved,
			security,
			'east-gate',
			'depart',
			'',
			{ mode: 'escort', escortProof: 'proof', lockNo: '0001' },
		),
		/二选一|同时|锁号|方式/,
	)
	assert.throws(
		() => workflow.transitionApplication(
			approved,
			security,
			'east-gate',
			'depart',
			'',
			{ mode: 'lock', escortProof: '', lockNo: '   ' },
		),
		/锁号|不能为空/,
	)
	assert.throws(
		() => workflow.transitionApplication(
			approved,
			security,
			'east-gate',
			'depart',
			'',
			{ mode: 'escort', escortProof: 'proof', lockNo: 123 },
		),
		/格式|参数/,
	)

	const departedByLock = workflow.transitionApplication(
		approved,
		security,
		'east-gate',
		'depart',
		'',
		lockExecution('000012'),
	)
	assert.deepEqual(departedByLock.transport, { mode: 'lock', lockNo: '000012' })
	assert.throws(
		() => workflow.transitionApplication(
			departedByLock,
			security,
			'west-gate',
			'arrive',
			'',
			lockExecution('12'),
		),
		/一致|锁号|登记/,
	)
	const arrivedByLock = workflow.transitionApplication(
		departedByLock,
		security,
		'west-gate',
		'arrive',
		'',
		lockExecution('000012'),
	)
	assert.equal(arrivedByLock.transport.lockNo, '000012')
})

test('供应商核验新增人员字段支持默认值、演示占位和独立数组副本', () => {
	const verification = supplier.verifyDemoSupplierBadge('DEMO-BADGE-001', 'east-gate', 1_000_000)
	assert.equal(verification.photoUrl, '')
	assert.equal(verification.visitorPhone, '未提供（演示）')
	assert.equal(verification.hostName, '未提供（演示）')
	assert.equal(verification.hostPhone, '未提供（演示）')
	assert.deepEqual(verification.authorizedAreas, ['演示园区东门保密区'])

	const cloned = supplier.cloneSupplierVerification(verification)
	cloned.allowedDirections.push('enter')
	cloned.authorizedAreas.push('演示园区西门保密区')
	assert.equal(verification.allowedDirections.length, 2)
	assert.deepEqual(verification.authorizedAreas, ['演示园区东门保密区'])

	const withoutOptionalFields = { ...verification }
	delete withoutOptionalFields.photoUrl
	delete withoutOptionalFields.visitorPhone
	delete withoutOptionalFields.hostName
	delete withoutOptionalFields.hostPhone
	delete withoutOptionalFields.authorizedAreas
	const normalized = supplier.cloneSupplierVerification(withoutOptionalFields)
	assert.equal(normalized.photoUrl, '')
	assert.equal(normalized.visitorPhone, '')
	assert.equal(normalized.hostName, '')
	assert.equal(normalized.hostPhone, '')
	assert.deepEqual(normalized.authorizedAreas, [])
	assert.equal(
		supplier.validateSupplierVerification(
			{ ...withoutOptionalFields, photoUrl: 42 },
			security,
			1_000_000,
		),
		false,
	)
	assert.equal(
		supplier.validateSupplierVerification(
			{ ...withoutOptionalFields, authorizedAreas: ['东门', 1] },
			security,
			1_000_000,
		),
		false,
	)
})

test('真实API保留完整供应商字段并拒绝新增字段的错误类型', async () => {
	const response = {
		id: 'verification-phase8',
		badgeId: 'BADGE-PHASE8',
		visitorName: '实时访客',
		supplierName: '实时单位',
		admissionId: 'admission-phase8',
		postId: 'east-gate',
		areaName: '东门保密区',
		validFrom: '2026-09-05T00:00:00.000Z',
		validUntil: '2026-09-06T00:00:00.000Z',
		expiresAt: '2026-09-05T01:00:00.000Z',
		allowed: true,
		reason: '',
		presence: 'outside',
		allowedDirections: ['enter', 'leave'],
		photoUrl: '',
		visitorPhone: '未提供（演示）',
		hostName: '未提供（演示）',
		hostPhone: '未提供（演示）',
		authorizedAreas: ['东门保密区'],
	}
	const api = createClientApi('https://example.invalid', async request => {
		assert.equal(request.url.endsWith('/visitor-checks'), true)
		return { status: 200, body: response }
	})
	assert.deepEqual(await api.verifySupplierBadge('token', 'BADGE-PHASE8', 'east-gate'), response)

	for (const malformedField of [
		{ photoUrl: 1 },
		{ visitorPhone: 1 },
		{ hostName: 1 },
		{ hostPhone: 1 },
		{ authorizedAreas: ['东门', 1] },
	]) {
		const malformed = createClientApi('https://example.invalid', async () => ({
			status: 200,
			body: { ...response, ...malformedField },
		}))
		await assert.rejects(
			() => malformed.verifySupplierBadge('token', 'BADGE-PHASE8', 'east-gate'),
			/核验响应/,
		)
	}
})

test('岗位前置只加载当前岗位可办理物品，普通申请列表不受限制', async () => {
		assert.equal(typeof state.hasExecutionPost, 'function')
		assert.equal(typeof state.loadExecutionApplications, 'function')
		assert.equal(typeof state.findScannedApplication, 'function')

		responseStatus = 200
		responseBody = {
			token: 'phase8-token',
			expiresAt: Date.now() + 60_000,
			identity: security,
		}
		await state.login('SEC-1', 'secret')
		requests = []
		assert.equal(state.hasExecutionPost('item-pass:execute'), false)
		await state.loadExecutionApplications()
		assert.equal(requests.length, 0)

		state.selectPost('east-gate')
		assert.equal(state.hasExecutionPost('item-pass:execute'), true)
		assert.equal(state.hasExecutionPost('supplier:execute'), true)
		responseBody = [
			application({ id: 'approved-east', status: 'approved', seals: ['00000001'] }),
			application({
				id: 'transporting-to-east',
				status: 'transporting',
				fromPostId: 'west-gate',
				toPostId: 'east-gate',
				seals: ['00000002'],
				transport: { mode: 'lock', lockNo: '000002' },
			}),
			application({ id: 'approved-west', status: 'approved', fromPostId: 'west-gate', seals: ['00000003'] }),
		]
		requests = []
		await state.loadExecutionApplications()
		assert.equal(requests.length, 1)
		assert.deepEqual(state.clientState.applications.map(item => item.id), [
			'approved-east',
			'transporting-to-east',
		])
		assert.equal(state.findScannedApplication('00000001').id, 'approved-east')
		assert.equal(state.findScannedApplication('00000003'), undefined)

		state.clientState.postId = ''
		assert.equal(state.findScannedApplication('00000001'), undefined)
	})

test('旧岗位执行列表响应不得覆盖切换后的岗位结果', async () => {
	responseStatus = 200
	responseBody = { token: 'phase8-race-token', expiresAt: Date.now() + 60_000, identity: security }
	await state.login('SEC-1', 'secret')
	state.selectPost('east-gate')
	const originalRequest = uni.request
	const pending = []
	uni.request = options => pending.push(options)
	try {
		const oldLoad = state.loadExecutionApplications()
		assert.equal(pending.length, 1)
		state.selectPost('west-gate')
		pending[0].success({
			statusCode: 200,
			data: [
				application({
					id: 'stale-west-scope',
					fromPostId: 'west-gate',
					toPostId: 'east-gate',
					status: 'approved',
				}),
			],
		})
		await oldLoad
		assert.deepEqual(state.clientState.applications, [])
	} finally {
		uni.request = originalRequest
	}
})

test('演示刷卡证明绑定账号岗位单据动作，清除后不能继续使用', async () => {
	assert.equal(typeof state.recordDemoEscortSwipe, 'function')
	assert.equal(typeof state.clearEscortVerification, 'function')

	state.enterDemo('demo-supervisor-001')
	await state.loadApplications()
	await state.actOnApplication('demo-release-pending', 'approve', '')
	state.enterDemo('demo-security-001')
	state.selectPost('east-gate')
	await state.loadExecutionApplications()
	const proof = state.recordDemoEscortSwipe('demo-release-pending', 'depart')
	assert.equal(typeof proof, 'string')
	assert.ok(proof.length > 0)
	const replacementProof = state.recordDemoEscortSwipe('demo-release-pending', 'depart')
	await assert.rejects(
		() => state.actOnApplication('demo-release-pending', 'depart', '', escortExecution(proof)),
		/无效|失效|重新刷卡/,
	)
	await assert.rejects(
		() => state.actOnApplication(
			'demo-release-pending',
			'depart',
			'',
			{ mode: 'lock', escortProof: replacementProof, lockNo: '0001' },
		),
		/二选一|同时|刷卡证明/,
	)
	state.clearEscortVerification()
	await assert.rejects(
		() => state.actOnApplication('demo-release-pending', 'depart', '', escortExecution(replacementProof)),
		/刷卡|核验|证明|失效/,
	)

	const freshProof = state.recordDemoEscortSwipe('demo-release-pending', 'depart')
	state.selectPost('west-gate')
	await assert.rejects(
		() => state.actOnApplication('demo-release-pending', 'depart', '', escortExecution(freshProof)),
		/岗位|状态|权限/,
	)
	state.enterDemo('demo-security-001')
	state.selectPost('east-gate')
	await state.loadExecutionApplications()
	const accountProof = state.recordDemoEscortSwipe('demo-release-pending', 'depart')
	state.enterDemo('demo-security-001')
	await assert.rejects(
		() => state.actOnApplication('demo-release-pending', 'depart', '', escortExecution(accountProof)),
		/岗位|状态|权限/,
	)
	state.selectPost('east-gate')
	await state.loadExecutionApplications()
	const finalProof = state.recordDemoEscortSwipe('demo-release-pending', 'depart')
	const departedResult = await state.actOnApplication(
		'demo-release-pending',
		'depart',
		'',
		escortExecution(finalProof),
	)
	assert.equal(departedResult.status, 'transporting')
	assert.deepEqual(departedResult.transport, { mode: 'escort', lockNo: '' })
	assert.equal(state.clientState.applications.some(item => item.id === 'demo-release-pending'), false)
	state.selectPost('west-gate')
	await state.loadExecutionApplications()
	const departed = state.clientState.applications.find(item => item.id === 'demo-release-pending')
	assert.deepEqual(departed.transport, { mode: 'escort', lockNo: '' })
	const arrivalProof = state.recordDemoEscortSwipe('demo-release-pending', 'arrive')
	const arrivedResult = await state.actOnApplication(
		'demo-release-pending',
		'arrive',
		'',
		escortExecution(arrivalProof),
	)
	assert.equal(arrivedResult.status, 'completed')
	assert.deepEqual(arrivedResult.transport, { mode: 'escort', lockNo: '' })
	assert.equal(state.clientState.applications.some(item => item.id === 'demo-release-pending'), false)
})

test('动作参数写入请求正文并参与不同操作的幂等区分', async () => {
	const response = application({ status: 'transporting', transport: { mode: 'lock', lockNo: '000012' } })
	let captured
	const api = createClientApi('https://example.invalid', async request => {
		captured = request
		return { status: 200, body: response }
	})
	await api.act('token', response.id, 'depart', 'east-gate', '', 'operation-1', lockExecution('000012'))
	assert.deepEqual(captured.body.execution, lockExecution('000012'))
})

test('物品API在请求前拒绝供应商进出动作并拒绝inside单据状态', async () => {
	let calls = 0
	const actionApi = createClientApi('https://example.invalid', async () => {
		calls += 1
		return { status: 200, body: application({ id: 'legacy-supplier-action', status: 'inside', transport: null }) }
	})
	await assert.rejects(
		() => actionApi.act('token', 'legacy-supplier-action', 'enter', 'east-gate', '', 'legacy-enter', null),
		/动作|无效|物品/,
	)
	await assert.rejects(
		() => actionApi.act('token', 'legacy-supplier-action', 'leave', 'east-gate', '', 'legacy-leave', null),
		/动作|无效|物品/,
	)
	assert.equal(calls, 0)

	const insideListApi = createClientApi('https://example.invalid', async () => ({
		status: 200,
		body: [application({ id: 'legacy-inside-status', status: 'inside', transport: null })],
	}))
	await assert.rejects(() => insideListApi.list('token'), /单据响应/)
})

test('真实模式将定位锁或押运人工卡提交给专用接口，服务端负责最终核验', async () => {
	responseStatus = 200
	responseBody = { token: 'phase8-live-token', expiresAt: Date.now() + 60_000, identity: security }
	await state.login('SEC-1', 'secret')
	state.selectPost('east-gate')
	const lockItem = application({ id: 'live-approved-lock', seals: ['00007890'] })
	const escortItem = application({ id: 'live-approved-escort', seals: ['00007891'] })
	responseBody = [lockItem, escortItem]
	await state.loadExecutionApplications()
	requests = []
	const original = uni.request
	uni.request = options => {
		requests.push(options)
		if (options.url.endsWith('/actions')) {
			const source = options.data.execution.mode == 'lock' ? lockItem : escortItem
			options.success({ statusCode: 200, data: application({ ...source, status: 'transporting', transport: options.data.execution.mode == 'lock' ? { mode: 'lock', lockNo: '00007890' } : { mode: 'escort', lockNo: '' } }) })
			return
		}
		if (options.url.includes('scope=execute')) {
			options.success({ statusCode: 200, data: [] })
			return
		}
		throw new Error('意外的真实执行请求')
	}
	try {
		assert.equal((await state.actOnApplication(lockItem.id, 'depart', '', lockExecution('00007890'))).status, 'transporting')
		state.clientState.applications = [escortItem]
		assert.equal((await state.actOnApplication(escortItem.id, 'depart', '', escortExecution('ESCORT-CARD-001'))).status, 'transporting')
		const actions = requests.filter(request => request.url.endsWith('/actions'))
		assert.equal(actions.length, 2)
		assert.deepEqual(actions[0].data.execution, lockExecution('00007890'))
		assert.deepEqual(actions[1].data.execution, escortExecution('ESCORT-CARD-001'))
	} finally { uni.request = original }
})

test('仅审批权限的成功动作从已验证响应返回独立快照，畸形写入结果要求查记录', async () => {
	const originalRequest = uni.request
	const approvePending = application({
		id: 'live-approve-only',
		status: 'pending',
		applicantId: 'different-applicant',
		transport: null,
	})
	const approveResponse = application({
		id: approvePending.id,
		status: 'approved',
		transport: null,
		timeline: [{ id: 'approved-event', title: '审批通过', actor: '仅审批演示人员', at: '2026-09-05T01:00:00.000Z' }],
	})
	const rejectPending = application({
		id: 'live-reject-only',
		status: 'pending',
		applicantId: 'different-applicant',
		transport: null,
	})
	const rejectResponse = application({
		id: rejectPending.id,
		status: 'rejected',
		transport: null,
		timeline: [{ id: 'rejected-event', title: '驳回：材料不完整', actor: '仅审批演示人员', at: '2026-09-05T01:00:00.000Z' }],
	})
	const queued = []
	uni.request = options => {
		requests.push(options)
		const body = queued.shift()
		options.success({ statusCode: 200, data: body })
	}
	try {
		queued.push({ token: 'approval-only-token', expiresAt: Date.now() + 60_000 }, approvalOnly)
		await state.login('APPROVER-1', 'secret')
		queued.push([approvePending])
		await state.loadApplications()
		queued.push(approveResponse, [])
		const approved = await state.actOnApplication(approvePending.id, 'approve', '同意')
		assert.equal(approved.status, 'approved')
		assert.equal(approved.id, approvePending.id)
		assert.equal(state.clientState.applications.length, 0)
		assert.notEqual(approved, approveResponse)
		approved.timeline[0].title = '页面修改不得回写'
		approved.seals.push('页面新增封条')
		assert.equal(approveResponse.timeline[0].title, '审批通过')
		assert.deepEqual(approveResponse.seals, ['00001234'])

		queued.push({ token: 'approval-only-token-2', expiresAt: Date.now() + 60_000 }, approvalOnly)
		await state.login('APPROVER-1', 'secret')
		queued.push([rejectPending])
		await state.loadApplications()
		queued.push(rejectResponse, [])
		const rejected = await state.actOnApplication(rejectPending.id, 'reject', '材料不完整')
		assert.equal(rejected.status, 'rejected')
		assert.equal(rejected.id, rejectPending.id)
		assert.equal(state.clientState.applications.length, 0)
		assert.notEqual(rejected, rejectResponse)

		for (const invalidResponse of [
			application({ id: 'other-id', status: 'approved', transport: null }),
			application({ id: approvePending.id, status: 'rejected', transport: null }),
			{ ...approveResponse, title: 42 },
		]) {
			const invalidApi = createClientApi('https://example.invalid', async () => ({
				status: 200,
				body: invalidResponse,
			}))
			await assert.rejects(
				() => invalidApi.act('token', approvePending.id, 'approve', 'east-gate', '', 'invalid-response', null),
				/先查询记录|响应格式|写入结果/,
			)
		}
		const unknownApi = createClientApi('https://example.invalid', async () => ({ status: 200, body: null }))
		await assert.rejects(
			() => unknownApi.act('token', approvePending.id, 'approve', 'east-gate', '', 'unknown-response', null),
			/未知|先查询记录/,
		)
	} finally {
		uni.request = originalRequest
	}
})

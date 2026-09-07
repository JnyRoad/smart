/** 验证申请候选与安检执行岗位隔离；所有真实模式请求仅使用内存传输替身。 */
import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { registerHooks, stripTypeScriptTypes } from 'node:module'
import { createClientApi } from '../services/client-api.uts'

registerHooks({
	resolve(name, context, next) {
		if (name === 'vue') {
			return {
				url: 'data:text/javascript,export const reactive=value=>value;export const nextTick=callback=>Promise.resolve().then(callback)',
				shortCircuit: true,
			}
		}
		return next(name, context)
	},
})

const EAST_GATE = {
	id: 'east gate/一号',
	name: '测试园区东门',
	parkId: 'test-park',
	parkName: '测试园区',
}
const WEST_GATE = {
	id: 'west-gate',
	name: '测试园区西门',
	parkId: 'test-park',
	parkName: '测试园区',
}

/** 构造完整物品单据，避免部分响应让边界测试绕过真实格式校验。 */
function application(overrides = {}) {
	return {
		id: 'release-phase10',
		kind: 'item-pass',
		title: '测试物品放行',
		reason: '集成候选验证',
		fromPostId: EAST_GATE.id,
		toPostId: WEST_GATE.id,
		supplierName: '',
		visitorName: '',
		materials: '测试样品一箱',
		seals: ['00001234'],
		applicantId: 'live-applicant',
		applicantName: '测试申请人',
		status: 'pending',
		transport: null,
		timeline: [{ id: 'timeline-1', title: '已提交', actor: '测试申请人', at: '2026-09-05T00:00:00.000Z' }],
		...overrides,
	}
}

test('申请候选与现场列表使用各自GET契约，空岗位不发送现场请求', async () => {
	const requests = []
	const api = createClientApi('https://example.invalid', async request => {
		requests.push(request)
		if (request.url.endsWith('/item-passes/posts')) {
			return { status: 200, body: { posts: [EAST_GATE, WEST_GATE] } }
		}
		return { status: 200, body: [application({ status: 'approved' })] }
	})

	assert.equal(typeof api.applicationOptions, 'function')
	assert.equal(typeof api.listExecution, 'function')
	assert.deepEqual(await api.applicationOptions('token'), { posts: [EAST_GATE, WEST_GATE] })
	await api.list('token')
	await api.listExecution('token', EAST_GATE.id)
	assert.equal(requests[0].url, 'https://example.invalid/api/v1/item-passes/posts')
	assert.equal(requests[1].url, 'https://example.invalid/api/v1/item-passes')
	assert.equal(
		requests[2].url,
		'https://example.invalid/api/v1/item-passes?scope=execute&postId=east%20gate%2F%E4%B8%80%E5%8F%B7',
	)

	const count = requests.length
	await assert.rejects(() => api.listExecution('token', '   '), /岗位/)
	assert.equal(requests.length, count)
})

test('申请候选成功响应必须是完整Post集合', async () => {
	for (const body of [[], {}, { posts: null }, { posts: [{ id: 'east-gate' }] }]) {
		const api = createClientApi('https://example.invalid', async () => ({ status: 200, body }))
		assert.equal(typeof api.applicationOptions, 'function')
		await assert.rejects(() => api.applicationOptions('token'), /申请岗位|候选|响应/)
	}
})

test('单据点位名称快照可缺省兼容，但存在时必须是字符串', async () => {
	const legacy = application({ id: 'legacy-without-post-names' })
	const named = application({
		id: 'named-posts',
		fromPostName: EAST_GATE.name,
		toPostName: WEST_GATE.name,
	})
	for (const item of [legacy, named]) {
		const api = createClientApi(
			'https://example.invalid',
			async () => ({ status: 200, body: [item] }),
		)
		assert.deepEqual(await api.list('token'), [item])
	}
	for (const item of [
		application({ fromPostName: 7 }),
		application({ toPostName: null }),
	]) {
		const api = createClientApi(
			'https://example.invalid',
			async () => ({ status: 200, body: [item] }),
		)
		await assert.rejects(() => api.list('token'), /单据响应/)
	}
})

test('提交契约不发送客户端自称的点位名称快照', async () => {
	let request = null
	const result = application({
		fromPostName: EAST_GATE.name,
		toPostName: WEST_GATE.name,
	})
	const api = createClientApi('https://example.invalid', async value => {
		request = value
		return { status: 200, body: result }
	})
	const draft = {
		kind: 'item-pass',
		title: result.title,
		reason: result.reason,
		fromPostId: result.fromPostId,
		toPostId: result.toPostId,
		fromPostName: '客户端伪造起点',
		toPostName: '客户端伪造终点',
		supplierName: '',
		visitorName: '',
		materials: result.materials,
		seals: result.seals.slice(),
	}
	await api.submit('token', draft, 'operation-name-snapshot')
	assert.equal(Object.hasOwn(request.body, 'fromPostName'), false)
	assert.equal(Object.hasOwn(request.body, 'toPostName'), false)
})

const requestLog = []
let responseStatus = 200
let responseBody = null
globalThis.uni = {
	getStorageSync: () => undefined,
	setStorageSync: () => {},
	switchTab: () => {},
	reLaunch: () => {},
	navigateTo: () => {},
	showToast: () => {},
	request: options => {
		requestLog.push(options)
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

/** 登录一个无安检岗位的普通申请人，证明候选不会写回身份授权。 */
async function signIn(subjectId = 'live-applicant') {
	responseStatus = 200
	responseBody = {
		token: 'phase10-token-' + subjectId,
		expiresAt: Date.now() + 60_000,
		identity: {
			subjectId,
			staffNo: subjectId,
			displayName: '测试申请人',
			employmentType: 'employee',
			organization: '测试单位',
			permissions: ['item-pass:apply'],
			posts: [],
		},
	}
	await state.login(subjectId, 'test-password')
	requestLog.length = 0
}

test('演示申请候选来自独立目录且不伪装为非安检身份的执行岗位', async () => {
	for (const subjectId of [
		'demo-employee-001',
		'demo-outsourced-001',
		'demo-dispatched-001',
	]) {
		state.enterDemo(subjectId)
		await state.loadApplicationOptions()
		assert.equal(state.applicationPosts().length, 2)
		assert.deepEqual(state.availablePosts(), [])
		assert.deepEqual(state.clientState.identity.posts, [])
	}
	state.enterDemo('demo-supervisor-001')
	assert.deepEqual(state.availablePosts(), [])
	state.enterDemo('demo-security-001')
	assert.equal(state.availablePosts().length, 2)
})

test('无执行岗位的员工加载独立候选后仍可提交合法申请', async () => {
	await signIn()
	assert.equal(typeof state.loadApplicationOptions, 'function')
	assert.equal(typeof state.applicationPosts, 'function')
	const originalRequest = uni.request
	const created = application()
	uni.request = options => {
		requestLog.push(options)
		if (options.url.endsWith('/item-passes/posts')) {
			options.success({ statusCode: 200, data: { posts: [EAST_GATE, WEST_GATE] } })
			return
		}
		if (options.method == 'POST' && options.url.endsWith('/item-passes')) {
			options.success({ statusCode: 200, data: created })
			return
		}
		if (options.method == 'GET' && options.url.endsWith('/item-passes')) {
			options.success({ statusCode: 200, data: [created] })
			return
		}
		throw new Error('意外的申请请求：' + options.url)
	}
	try {
		await state.loadApplicationOptions()
		assert.deepEqual(state.applicationPosts(), [EAST_GATE, WEST_GATE])
		assert.deepEqual(state.availablePosts(), [])
		assert.deepEqual(state.clientState.identity.posts, [])
		const id = await state.submitApplication({
			kind: 'item-pass',
			title: created.title,
			reason: created.reason,
			fromPostId: EAST_GATE.id,
			toPostId: WEST_GATE.id,
			supplierName: '',
			visitorName: '',
			materials: created.materials,
			seals: created.seals.slice(),
		})
		assert.equal(id, created.id)
		assert.equal(requestLog.filter(request => request.method == 'POST').length, 1)
	} finally {
		uni.request = originalRequest
	}
})

test('演示新申请从有效候选派生名称且不能接受草稿自称的名称', async () => {
	state.enterDemo('demo-employee-001')
	await state.loadApplicationOptions()
	const candidates = state.applicationPosts()
	const fromPost = candidates.find(post => post.id == 'east gate/一号') ?? candidates[0]
	const toPost = candidates.find(post => post.id != fromPost.id)
	const id = await state.submitApplication({
		kind: 'item-pass',
		title: '演示名称快照申请',
		reason: '验证名称由候选生成',
		fromPostId: fromPost.id,
		toPostId: toPost.id,
		fromPostName: '草稿伪造起点',
		toPostName: '草稿伪造终点',
		supplierName: '',
		visitorName: '',
		materials: '测试物料',
		seals: ['00004567'],
	})
	const created = state.clientState.applications.find(item => item.id == id)
	assert.equal(created.fromPostName, fromPost.name)
	assert.equal(created.toPostName, toPost.name)
	assert.deepEqual(state.clientState.identity.posts, [])
})

test('候选加载失败清空状态，明确重试成功且不回退身份岗位', async () => {
	await signIn('retry-applicant')
	assert.equal(typeof state.loadApplicationOptions, 'function')
	assert.ok(state.applicationOptionsState != null)
	const originalRequest = uni.request
	let attempts = 0
	uni.request = options => {
		requestLog.push(options)
		attempts += 1
		options.success(
			attempts == 1
				? { statusCode: 503, data: {} }
				: { statusCode: 200, data: { posts: [EAST_GATE, WEST_GATE] } },
		)
	}
	try {
		await state.loadApplicationOptions()
		assert.deepEqual(state.applicationPosts(), [])
		assert.match(state.applicationOptionsState.error, /服务暂时不可用/)
		await state.loadApplicationOptions()
		assert.deepEqual(state.applicationPosts(), [EAST_GATE, WEST_GATE])
		assert.equal(state.applicationOptionsState.error, '')
		assert.deepEqual(state.clientState.identity.posts, [])
		assert.equal(attempts, 2)
	} finally {
		uni.request = originalRequest
	}
})

test('切换账号后丢弃旧账号的候选迟到响应', async () => {
	await signIn('old-applicant')
	assert.equal(typeof state.loadApplicationOptions, 'function')
	assert.ok(state.applicationOptionsState != null)
	const originalRequest = uni.request
	const pending = []
	let nextIdentity = 'old-applicant'
	uni.request = options => {
		requestLog.push(options)
		if (options.url.endsWith('/item-passes/posts')) {
			pending.push(options)
			return
		}
		if (options.url.endsWith('/sessions') || options.url.endsWith('/me')) {
			const identity = {
				subjectId: nextIdentity,
				staffNo: nextIdentity,
				displayName: '新申请人',
				employmentType: 'employee',
				organization: '测试单位',
				permissions: ['item-pass:apply'],
				posts: [],
			}
			options.success({
				statusCode: 200,
				data: options.url.endsWith('/sessions')
					? { token: 'token-' + nextIdentity, expiresAt: Date.now() + 60_000 }
					: identity,
			})
			return
		}
		throw new Error('意外的账号切换请求')
	}
	try {
		const oldLoad = state.loadApplicationOptions()
		assert.equal(pending.length, 1)
		nextIdentity = 'new-applicant'
		await state.login(nextIdentity, 'test-password')
		pending[0].success({ statusCode: 200, data: { posts: [EAST_GATE, WEST_GATE] } })
		await oldLoad
		assert.equal(state.clientState.identity.subjectId, nextIdentity)
		assert.deepEqual(state.applicationPosts(), [])
		assert.equal(state.applicationOptionsState.error, '')
		assert.equal(state.applicationOptionsState.loading, false)
	} finally {
		uni.request = originalRequest
	}
})

test('提交前再次拒绝候选集合之外的起终点且不发POST', async () => {
	await signIn('validate-applicant')
	assert.equal(typeof state.loadApplicationOptions, 'function')
	const originalRequest = uni.request
	uni.request = options => {
		requestLog.push(options)
		if (options.url.endsWith('/item-passes/posts')) {
			options.success({ statusCode: 200, data: { posts: [EAST_GATE, WEST_GATE] } })
			return
		}
		throw new Error('候选校验失败后不应发送提交请求')
	}
	try {
		await state.loadApplicationOptions()
		requestLog.length = 0
		await assert.rejects(
			() => state.submitApplication({
				kind: 'item-pass',
				title: '非法岗位申请',
				reason: '验证客户端再次校验',
				fromPostId: EAST_GATE.id,
				toPostId: 'not-in-options',
				supplierName: '',
				visitorName: '',
				materials: '测试物料',
				seals: ['00009999'],
			}),
			/申请岗位|候选|授权范围/,
		)
		assert.equal(requestLog.filter(request => request.method == 'POST').length, 0)
	} finally {
		uni.request = originalRequest
	}
})

const applicationSource = readFileSync(
	new URL('../pages/application/application.uvue', import.meta.url),
	'utf8',
)
const applicationScript = applicationSource.match(/<script setup lang="uts">([\s\S]*?)<\/script>/)[1]
const applicationExecutable = stripTypeScriptTypes(
	applicationScript.replace(/^import[^\n]*\n/gm, ''),
	{ mode: 'strip' },
)

/** 执行真实申请页脚本，生命周期与候选服务使用可控内存实现。 */
function mountApplicationPage() {
	const lifecycle = {}
	const optionsState = { posts: [], loading: false, error: '' }
	const applicationCandidates = [EAST_GATE, WEST_GATE]
	const executionPosts = [{ id: 'security-only', name: '安检专用岗', parkId: 'test', parkName: '测试' }]
	const counters = { loads: 0, submits: 0 }
	const dependencies = {
		ref: value => ({ value }),
		reactive: value => value,
		computed: read => ({ get value() { return read() } }),
		nextTick: callback => Promise.resolve().then(callback),
		onLoad: callback => { lifecycle.load = callback },
		onShow: callback => { lifecycle.show = callback },
		guard: () => true,
		availablePosts: () => executionPosts,
		applicationOptionsState: optionsState,
		applicationPosts: () => optionsState.posts,
		loadApplicationOptions: async () => {
			counters.loads += 1
			optionsState.loading = true
			optionsState.error = ''
			optionsState.posts = []
			await Promise.resolve()
			optionsState.loading = false
			if (counters.loads == 1) optionsState.error = '候选加载失败，请重试'
			else optionsState.posts = applicationCandidates
		},
		submitApplication: async () => { counters.submits += 1; return 'created-id' },
		errorMessage: issue => issue.message,
		uni: { switchTab: () => {}, redirectTo: () => {} },
	}
	const createPage = new Function(
		...Object.keys(dependencies),
		applicationExecutable + `
			return {
				draft,
				posts,
				submit,
				error,
				retryOptions: typeof retryOptions == 'undefined' ? null : retryOptions,
				optionsReady: typeof optionsReady == 'undefined' ? { value: true } : optionsReady,
			}
		`,
	)
	const page = createPage(...Object.values(dependencies))
	lifecycle.load({ kind: 'item-pass' })
	return { page, lifecycle, optionsState, applicationCandidates, counters }
}

test('申请页使用独立候选，失败保留草稿、禁用提交并可明确重试', async () => {
	const mounted = mountApplicationPage()
	mounted.page.draft.title = '保留中的申请标题'
	await mounted.lifecycle.show()
	assert.equal(mounted.counters.loads, 1)
	assert.equal(mounted.page.draft.title, '保留中的申请标题')
	assert.deepEqual(mounted.page.posts.value, [])
	assert.match(mounted.optionsState.error, /失败|重试/)
	assert.equal(mounted.page.optionsReady.value, false)
	await mounted.page.submit()
	assert.equal(mounted.counters.submits, 0)
	assert.equal(typeof mounted.page.retryOptions, 'function')
	await mounted.page.retryOptions()
	assert.equal(mounted.counters.loads, 2)
	assert.deepEqual(mounted.page.posts.value, mounted.applicationCandidates)
	assert.equal(mounted.page.draft.title, '保留中的申请标题')
	assert.equal(mounted.page.optionsReady.value, true)
})

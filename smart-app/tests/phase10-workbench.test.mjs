/** 验证工作台/待办不能通过普通列表绕过当前执行岗位范围。 */
import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { registerHooks, stripTypeScriptTypes } from 'node:module'

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
	id: 'east-gate',
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

/** 构造完整服务端单据，保持 API 响应格式校验参与测试。 */
function application(overrides = {}) {
	return {
		id: 'release-workbench',
		kind: 'item-pass',
		title: '测试物品放行',
		reason: '工作台岗位范围验证',
		fromPostId: EAST_GATE.id,
		toPostId: WEST_GATE.id,
		supplierName: '',
		visitorName: '',
		materials: '测试物料',
		seals: ['00001234'],
		applicantId: 'other-applicant',
		applicantName: '其他申请人',
		status: 'pending',
		transport: null,
		timeline: [{ id: 'timeline-1', title: '已提交', actor: '其他申请人', at: '2026-09-05T00:00:00.000Z' }],
		...overrides,
	}
}

/** 删除页面脚本中的单行或多行 import，让测试执行真实组合式逻辑。 */
function withoutImports(source) {
	const output = []
	let inImport = false
	for (const line of source.split('\n')) {
		if (!inImport && /^\s*import\b/.test(line)) {
			inImport = !/\bfrom\s+['"][^'"]+['"]\s*$/.test(line) &&
				!/^\s*import\s+['"][^'"]+['"]\s*$/.test(line)
			continue
		}
		if (inImport) {
			if (/\bfrom\s+['"][^'"]+['"]\s*$/.test(line)) inImport = false
			continue
		}
		output.push(line)
	}
	return output.join('\n')
}

function pageScript(relativeUrl) {
	const source = readFileSync(new URL(relativeUrl, import.meta.url), 'utf8')
	const script = source.match(/<script setup lang="uts">([\s\S]*?)<\/script>/)[1]
	return stripTypeScriptTypes(withoutImports(script), { mode: 'strip' })
}

/** 挂载真实工作台或待办脚本，分别统计旧加载器与岗位安全加载器调用。 */
function mountPage(kind) {
	const lifecycle = {}
	const calls = { ordinary: 0, workbench: 0, authorized: 0 }
	const navigations = []
	const identity = { subjectId: 'self', employmentType: 'employee', organization: '测试单位' }
	const applications = [
		application({ id: 'self-1', applicantId: 'self' }),
		application({ id: 'self-2', applicantId: 'self' }),
		application({ id: 'self-3', applicantId: 'self' }),
		application({ id: 'self-4', applicantId: 'self' }),
		application({ id: 'other', applicantId: 'other' }),
	]
	const dependencies = {
		ref: value => ({ value }),
		computed: read => ({ get value() { return read() } }),
		onShow: callback => { lifecycle.show = callback },
		clientState: { identity, applications, loading: false, error: '' },
		guard: () => true,
		loadAuthorizedApps: async () => { calls.authorized += 1 },
		loadApplications: async () => { calls.ordinary += 1 },
		loadWorkbenchApplications: async () => { calls.workbench += 1 },
		favorites: () => [],
		todos: () => [],
		uni: { navigateTo: options => { navigations.push(options.url) }, switchTab: () => {} },
	}
	const executable = pageScript(
		kind == 'workbench'
			? '../pages/workbench/workbench.uvue'
			: '../pages/todos/todos.uvue',
	)
	const create = new Function(
		...Object.keys(dependencies),
		executable + (kind == 'workbench'
			? '\nreturn { refresh, recent, openDetail }'
			: '\nreturn { entries }'),
	)
	return { page: create(...Object.values(dependencies)), lifecycle, calls, navigations }
}

test('工作台和待办实际改用岗位安全加载器，最近申请仍只取本人三条', async () => {
	const workbench = mountPage('workbench')
	await workbench.lifecycle.show()
	await workbench.page.refresh()
	assert.equal(workbench.calls.workbench, 2)
	assert.equal(workbench.calls.ordinary, 0)
	assert.deepEqual(workbench.page.recent.value.map(item => item.id), ['self-1', 'self-2', 'self-3'])

	const todosPage = mountPage('todos')
	await todosPage.lifecycle.show()
	assert.equal(todosPage.calls.workbench, 1)
	assert.equal(todosPage.calls.ordinary, 0)
})

test('工作台最近申请打开详情时显式使用只读视图', () => {
	const workbench = mountPage('workbench')
	workbench.page.openDetail('ordinary-only-executable')
	assert.deepEqual(workbench.navigations, [
		'/pages/detail/detail?id=ordinary-only-executable&view=read',
	])
})

const requestLog = []
let loginIdentity = null
globalThis.uni = {
	getStorageSync: () => undefined,
	setStorageSync: () => {},
	switchTab: () => {},
	reLaunch: () => {},
	navigateTo: () => {},
	showToast: () => {},
	request: options => {
		requestLog.push(options)
		if (!options.url.endsWith('/sessions') && !options.url.endsWith('/me'))
			throw new Error('测试未配置业务请求响应')
		options.success({
			statusCode: 200,
			data: options.url.endsWith('/sessions')
				? { token: 'workbench-token', expiresAt: Date.now() + 60_000 }
				: loginIdentity,
		})
	},
}

const { runtimeConfig } = await import('../config/runtime.uts')
runtimeConfig.apiBaseUrl = 'https://example.invalid'
const state = await import('../state/session.uts')

/** 登录指定授权组合，每项测试显式决定执行岗位集合。 */
async function signIn({ subjectId = 'operator', permissions, posts = [] }) {
	loginIdentity = {
		subjectId,
		staffNo: subjectId,
		displayName: '测试操作人',
		employmentType: 'employee',
		organization: '测试单位',
		permissions,
		posts,
	}
	await state.login(subjectId, 'test-password')
	requestLog.length = 0
}

test('执行专用账号未选岗时不发普通或执行请求，也不产生执行待办', async () => {
	await signIn({ permissions: ['item-pass:execute'], posts: [EAST_GATE, WEST_GATE] })
	assert.equal(typeof state.loadWorkbenchApplications, 'function')
	await state.loadWorkbenchApplications()
	assert.equal(requestLog.length, 0)
	assert.deepEqual(state.clientState.applications, [])
	assert.deepEqual(state.todos(), [])
})

test('混合审批执行权限未选岗时只查普通列表并保留审批待办', async () => {
	await signIn({ permissions: ['item-pass:approve', 'item-pass:execute'], posts: [EAST_GATE] })
	assert.equal(typeof state.loadWorkbenchApplications, 'function')
	const originalRequest = uni.request
	const pendingApproval = application({ id: 'pending-approval' })
	uni.request = options => {
		requestLog.push(options)
		assert.equal(options.url, 'https://example.invalid/api/v1/item-passes')
		options.success({
			statusCode: 200,
			data: [pendingApproval, application({ id: 'approved-other', status: 'approved' })],
		})
	}
	try {
		await state.loadWorkbenchApplications()
		assert.equal(requestLog.length, 1)
		assert.deepEqual(state.clientState.applications.map(item => item.id), ['pending-approval'])
		assert.deepEqual(state.todos().map(item => item.id), ['pending-approval'])
	} finally {
		uni.request = originalRequest
	}
})

test('混合权限有岗时分别请求普通数据和当前岗位执行数据后安全合并', async () => {
	await signIn({
		subjectId: 'mixed-user',
		permissions: ['item-pass:apply', 'item-pass:approve', 'item-pass:execute'],
		posts: [EAST_GATE, WEST_GATE],
	})
	state.selectPost(EAST_GATE.id)
	assert.equal(typeof state.loadWorkbenchApplications, 'function')
	const originalRequest = uni.request
	const selfPending = application({ id: 'self-pending', applicantId: 'mixed-user' })
	const approval = application({ id: 'approval-pending' })
	const executable = application({
		id: 'execute-east',
		applicantId: 'mixed-user',
		status: 'approved',
		fromPostId: EAST_GATE.id,
	})
	const ordinaryOnlyExecutable = application({
		id: 'ordinary-only-executable',
		applicantId: 'mixed-user',
		status: 'approved',
		fromPostId: EAST_GATE.id,
	})
	const wrongPost = application({
		id: 'execute-west',
		status: 'approved',
		fromPostId: WEST_GATE.id,
	})
	uni.request = options => {
		requestLog.push(options)
		if (options.url == 'https://example.invalid/api/v1/item-passes') {
			options.success({
				statusCode: 200,
				data: [selfPending, approval, executable, ordinaryOnlyExecutable],
			})
			return
		}
		assert.equal(
			options.url,
			'https://example.invalid/api/v1/item-passes?scope=execute&postId=east-gate',
		)
		options.success({ statusCode: 200, data: [executable, wrongPost] })
	}
	try {
		await state.loadWorkbenchApplications()
		assert.deepEqual(requestLog.map(request => request.url), [
			'https://example.invalid/api/v1/item-passes',
			'https://example.invalid/api/v1/item-passes?scope=execute&postId=east-gate',
		])
		assert.deepEqual(state.clientState.applications.map(item => item.id), [
			'self-pending',
			'approval-pending',
			'execute-east',
			'ordinary-only-executable',
		])
		assert.deepEqual(state.todos().map(item => item.id), ['approval-pending', 'execute-east'])
	} finally {
		uni.request = originalRequest
	}
})

test('切岗后的失败请求保持空结果，旧岗位迟到成功不能恢复执行单', async () => {
	await signIn({ permissions: ['item-pass:execute'], posts: [EAST_GATE, WEST_GATE] })
	assert.equal(typeof state.loadWorkbenchApplications, 'function')
	state.selectPost(EAST_GATE.id)
	const originalRequest = uni.request
	const pending = []
	uni.request = options => { requestLog.push(options); pending.push(options) }
	try {
		const eastLoad = state.loadWorkbenchApplications()
		assert.equal(pending.length, 1)
		state.selectPost(WEST_GATE.id)
		const westLoad = state.loadWorkbenchApplications()
		assert.equal(pending.length, 2)
		assert.match(pending[0].url, /postId=east-gate$/)
		assert.match(pending[1].url, /postId=west-gate$/)
		pending[1].success({ statusCode: 503, data: {} })
		await westLoad
		pending[0].success({
			statusCode: 200,
			data: [application({ id: 'stale-east', status: 'approved', fromPostId: EAST_GATE.id })],
		})
		await eastLoad
		assert.deepEqual(state.clientState.applications, [])
		assert.match(state.clientState.error, /服务暂时不可用/)
	} finally {
		uni.request = originalRequest
	}
})

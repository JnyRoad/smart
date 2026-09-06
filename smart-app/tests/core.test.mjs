import { test } from 'node:test'
import assert from 'node:assert/strict'

import {
	MODULES,
	visibleModules,
	hasPermission,
	resolveModule,
	favoriteModules,
} from '../core/catalog.uts'
import { normalizeScan, createScanReceiver } from '../core/scan.uts'
import {
	validateDraft,
	canAct,
	transitionApplication,
} from '../core/workflow.uts'
import { DEMO_IDENTITIES, createDemoApplications } from '../core/demo.uts'

/** 按人员来源取得虚构身份，测试只读固定演示目录。 */
function identityByType(employmentType) {
	return DEMO_IDENTITIES.find(identity => identity.employmentType === employmentType)
}

/** 按演示显示名取得虚构身份，避免测试构造真实个人资料。 */
function identityByName(name) {
	return DEMO_IDENTITIES.find(identity => identity.displayName === name)
}

/** 构造一份满足放行规则的测试草稿，并允许单项覆盖。 */
function releaseDraft(overrides = {}) {
	return {
		kind: 'item-pass',
		title: '演示放行申请',
		reason: '演示物品送达',
		fromPostId: 'east-gate',
		toPostId: 'west-gate',
		supplierName: '',
		visitorName: '',
		materials: '演示资料一箱',
		seals: ['seal-demo-1'],
		...overrides,
	}
}

/** 构造一份申请工作流测试单据，默认使用演示正式员工申请。 */
function application(overrides = {}) {
	const employee = identityByType('employee')
	return {
		id: 'application-test-1',
		...releaseDraft(),
		applicantId: employee.subjectId,
		applicantName: employee.displayName,
		status: 'pending',
		timeline: [],
		...overrides,
	}
}

test('演示身份固定覆盖正式、外包、派遣、主管和安检授权', () => {
	assert.equal(DEMO_IDENTITIES.length, 5)
	assert.deepEqual(
		DEMO_IDENTITIES.map(identity => identity.employmentType).filter(Boolean).sort(),
		['employee', 'employee', 'employee', 'outsourced', 'dispatched'].sort(),
	)

	const employee = identityByType('employee')
	const outsourced = identityByType('outsourced')
	const dispatched = identityByType('dispatched')
	const supervisor = identityByName('演示主管')
	const security = identityByName('演示安检')
	for (const identity of [employee, outsourced, dispatched]) {
		assert.equal(hasPermission(identity, 'item-pass:apply'), true)
		assert.equal(hasPermission(identity, 'item-pass:read'), true)
		assert.equal(hasPermission(identity, 'supplier:apply'), false)
		assert.equal(hasPermission(identity, 'supplier:approve'), false)
	}
	assert.equal(hasPermission(supervisor, 'item-pass:approve'), true)
	assert.equal(hasPermission(supervisor, 'supplier:approve'), false)
	assert.equal(hasPermission(security, 'item-pass:execute'), true)
	assert.equal(hasPermission(security, 'supplier:execute'), true)
	assert.ok(employee.displayName.includes('演示'))
})

test('权限只接受精确权限码，未知权限和空权限默认拒绝', () => {
	const roleShapedIdentity = {
		subjectId: 'role-only',
		permissions: [],
		role: '主管',
		posts: [],
	}
	assert.equal(hasPermission(roleShapedIdentity, 'item-pass:approve'), false)
	assert.equal(hasPermission({ permissions: ['item-pass:*'] }, 'item-pass:apply'), false)
	assert.equal(hasPermission({ permissions: ['item-pass:apply'] }, 'item-pass:approve'), false)
	assert.equal(hasPermission({ permissions: [] }, 'item-pass:read'), false)
	assert.equal(hasPermission(null, 'item-pass:read'), false)
	assert.equal(hasPermission({ permissions: ['unknown:grant'] }, 'unknown:grant'), false)
})

test('模块目录移除供应商申请审批并保留独立核验记录入口', () => {
	assert.equal(MODULES.length, 6)
	assert.deepEqual(
		MODULES.map(module => module.id),
		[
			'item-pass-apply',
			'item-pass-approve',
			'item-pass-execute',
			'item-pass-records',
			'visitor-badge-check',
			'visitor-passage-records',
		],
	)
	for (const module of MODULES) {
		assert.ok(module.title)
		assert.ok(module.description)
		assert.ok(module.category === '物品放行' || module.category === '保密区通行')
		assert.match(module.route, /^\/pages\/(application|records|supplier-access|supplier-records)\//)
		assert.equal(resolveModule(module.id).id, module.id)
	}
	assert.equal(resolveModule('remote-route-from-server'), null)
	assert.equal(resolveModule('supplier-apply'), null)
	assert.equal(resolveModule('supplier-approve'), null)
	assert.deepEqual(visibleModules(null), [])
})

test('模块搜索、分类和收藏始终受当前身份权限过滤', () => {
	const employee = identityByType('employee')
	const supervisor = identityByName('演示主管')
	const security = identityByName('演示安检')

	assert.deepEqual(
		visibleModules(employee).map(module => module.id),
		['item-pass-apply', 'item-pass-records'],
	)
	assert.deepEqual(
		visibleModules(supervisor, '审批').map(module => module.id),
		['item-pass-approve'],
	)
	assert.deepEqual(
		visibleModules(security, '', '保密区通行').map(module => module.id),
		['visitor-badge-check', 'visitor-passage-records'],
	)
	assert.deepEqual(visibleModules(employee, 'execute'), [])

	const allAccessIdentity = {
		subjectId: 'all-access-demo',
		permissions: MODULES.map(module => module.permission),
		posts: [],
	}
	const favorites = favoriteModules(allAccessIdentity, [
		'item-pass-apply',
		'item-pass-apply',
		'not-registered',
		...MODULES.map(module => module.id),
	])
	assert.equal(favorites.length, 6)
	assert.deepEqual(
		favorites.map(module => module.id),
		MODULES.map(module => module.id),
	)
	assert.deepEqual(
		favoriteModules(employee, ['item-pass-approve', 'visitor-badge-check', 'item-pass-apply', 'item-pass-apply']),
		[resolveModule('item-pass-apply')],
		)
})

test('扫码只去除末尾换行，保留前导零并拒绝边界异常', () => {
	assert.equal(normalizeScan('000012345\r\n'), '000012345')
	assert.equal(normalizeScan('000012345\n\r\n'), '000012345')
	assert.equal(normalizeScan('NO-END-MARKER'), 'NO-END-MARKER')
	assert.equal(normalizeScan('https://example.invalid/scan?id=0001'), 'https://example.invalid/scan?id=0001')
	assert.throws(() => normalizeScan(''), /空白/)
	assert.throws(() => normalizeScan(' \t\n'), /空白/)
	assert.throws(() => normalizeScan('scan\u0001value'), /控制字符/)
	assert.throws(() => normalizeScan('x'.repeat(4097)), /4096/)
	assert.equal(normalizeScan('x'.repeat(4096) + '\r\n').length, 4096)
})

test('扫码接收器合并CR、LF和confirm尾随事件且保留后续前导零条码', () => {
	const received = []
	const receiver = createScanReceiver((value, source) => received.push({ value, source }))

	assert.equal(receiver.receive('00001234\r', 'hardware', 'input', 1000), true)
	assert.equal(receiver.receive('\n', 'hardware', 'input', 1001), false)
	assert.equal(receiver.receive('', 'hardware', 'confirm', 1002), false)
	assert.equal(receiver.receive('00001234', 'hardware', 'confirm', 1003), false)
	assert.deepEqual(received, [{ value: '00001234', source: 'hardware' }])

	assert.equal(receiver.receive('00000056\n', 'hardware', 'input', 2000), true)
	assert.equal(receiver.receive('\r', 'hardware', 'input', 2001), false)
	assert.equal(receiver.receive('', 'hardware', 'confirm', 2002), false)
	assert.equal(receiver.receive('00000078', 'camera', 'camera', 2003), true)
	assert.deepEqual(received, [
		{ value: '00001234', source: 'hardware' },
		{ value: '00000056', source: 'hardware' },
		{ value: '00000078', source: 'camera' },
	])

	const repeatedReceived = []
	const repeatedReceiver = createScanReceiver((value, source) => repeatedReceived.push({ value, source }))
	assert.equal(repeatedReceiver.receive('00000999\r\n', 'hardware', 'input', 3000), true)
	assert.equal(repeatedReceiver.receive('00000999\r\n', 'hardware', 'input', 3001), false)
	assert.equal(repeatedReceiver.receive('', 'hardware', 'confirm', 3002), false)
	assert.deepEqual(repeatedReceived, [{ value: '00000999', source: 'hardware' }])

	const reversedReceived = []
	const reversedReceiver = createScanReceiver((value, source) => reversedReceived.push({ value, source }))
	assert.equal(reversedReceiver.receive('00000111\r', 'hardware', 'input', 4000), true)
	assert.equal(reversedReceiver.receive('', 'hardware', 'confirm', 4001), false)
	assert.equal(reversedReceiver.receive('\n', 'hardware', 'input', 4002), false)
	assert.deepEqual(reversedReceived, [{ value: '00000111', source: 'hardware' }])
})

test('扫码接收器不会把新的空确认永久忽略', () => {
	const receiver = createScanReceiver(() => {})
	assert.equal(receiver.receive('00000001', 'hardware', 'confirm', 3000), true)
	assert.throws(() => receiver.receive('', 'hardware', 'confirm', 3001), /空白/)

	assert.equal(receiver.receive('00000002\r\n', 'hardware', 'input', 4000), true)
	assert.throws(() => receiver.receive('', 'hardware', 'confirm', 5001), /空白/)
})

test('放行草稿校验覆盖必填字段、不同岗位、物料和封条', () => {
	assert.equal(validateDraft(releaseDraft()), null)
	assert.match(validateDraft(releaseDraft({ title: '' })), /标题/)
	assert.match(validateDraft(releaseDraft({ reason: '   ' })), /事由/)
	assert.match(validateDraft(releaseDraft({ fromPostId: 'west-gate' })), /岗位/)
	assert.match(validateDraft(releaseDraft({ toPostId: '' })), /岗位/)
	assert.match(validateDraft(releaseDraft({ materials: '' })), /物料/)
	assert.match(validateDraft(releaseDraft({ seals: [] })), /封条/)
	assert.match(validateDraft(releaseDraft({ seals: ['seal-demo-1', '   '] })), /封条/)
	assert.match(validateDraft(releaseDraft({ seals: ['seal-demo-1', 'seal-demo-1'] })), /封条/)
	assert.equal(validateDraft(releaseDraft({ seals: ['seal-demo-1', 'seal-demo-2'] })), null)
})

test('供应商旧申请草稿直接拒绝，资格只能通过厂牌核验取得', () => {
	const draft = releaseDraft({
		kind: 'supplier',
		title: '演示通行申请',
		reason: '供应商送货',
		fromPostId: '',
		toPostId: 'west-gate',
		supplierName: '演示供应商',
		visitorName: '演示访客',
		materials: '',
		seals: [],
	})
	assert.match(validateDraft(draft), /供应商|厂牌|核验/)
	assert.match(validateDraft({ ...draft, supplierName: '' }), /供应商|厂牌|核验/)
	assert.match(validateDraft({ ...draft, visitorName: '' }), /供应商|厂牌|核验/)
	assert.match(validateDraft({ ...draft, toPostId: '' }), /供应商|厂牌|核验/)
	assert.match(validateDraft({ ...draft, kind: 'unknown' }), /业务类型/)
})

test('审批只允许具备精确审批权限的非申请人操作', () => {
	const employee = identityByType('employee')
	const supervisor = identityByName('演示主管')
	const pending = application({ applicantId: employee.subjectId, applicantName: employee.displayName })
	assert.equal(canAct(pending, supervisor, '', 'approve'), true)
	assert.equal(canAct(pending, employee, '', 'approve'), false)
	assert.equal(
		canAct({ ...pending, applicantId: supervisor.subjectId }, supervisor, '', 'approve'),
		false,
	)
	assert.equal(canAct({ ...pending, status: 'approved' }, supervisor, '', 'approve'), false)
})

test('执行必须同时满足状态、业务动作、授权岗位和岗位所属关系', () => {
	const security = identityByName('演示安检')
	const approvedRelease = application({ status: 'approved' })
	const transportingRelease = { ...approvedRelease, status: 'transporting' }
	assert.equal(canAct(approvedRelease, security, 'east-gate', 'depart'), true)
	assert.equal(canAct(approvedRelease, security, 'west-gate', 'depart'), false)
	assert.equal(canAct(approvedRelease, security, 'east-gate', 'arrive'), false)
	assert.equal(canAct(transportingRelease, security, 'west-gate', 'arrive'), true)
	assert.equal(canAct(transportingRelease, security, 'east-gate', 'arrive'), false)
	assert.equal(canAct(approvedRelease, identityByType('employee'), 'east-gate', 'depart'), false)
	assert.equal(
		canAct(approvedRelease, { ...security, posts: [{ id: 'other-park-gate', name: '异园岗位', parkId: 'other', parkName: '异园' }] }, 'other-park-gate', 'depart'),
		false,
	)

	const legacySupplier = application({
		kind: 'supplier',
		fromPostId: '',
		toPostId: 'west-gate',
		supplierName: '演示供应商',
		visitorName: '演示访客',
		materials: '',
		seals: [],
		status: 'approved',
	})
	const insideSupplier = { ...legacySupplier, status: 'inside' }
	assert.equal(canAct(legacySupplier, security, 'west-gate', 'enter'), false)
	assert.equal(canAct(legacySupplier, security, 'east-gate', 'enter'), false)
	assert.equal(canAct(legacySupplier, security, 'west-gate', 'depart'), false)
	assert.equal(canAct(insideSupplier, security, 'west-gate', 'leave'), false)
})

test('放行流转按状态推进并为每次操作追加时间线且保持输入不变', () => {
	const employee = identityByType('employee')
	const supervisor = identityByName('演示主管')
	const security = identityByName('演示安检')
	const pending = application({ timeline: [{ id: 'created', title: '已提交', actor: employee.displayName, at: '2026-09-05T00:00:00.000Z' }] })
	const original = structuredClone(pending)

	const approved = transitionApplication(pending, supervisor, '', 'approve')
	assert.equal(approved.status, 'approved')
	assert.equal(approved.timeline.length, 2)
	assert.equal(approved.timeline.at(-1).actor, supervisor.displayName)
	assert.notDeepEqual(approved, original)
	assert.deepEqual(pending, original)

	const transporting = transitionApplication(
		approved,
		security,
		'east-gate',
		'depart',
		'',
		{ mode: 'escort', escortProof: 'test-depart-proof', lockNo: '' },
	)
	assert.equal(transporting.status, 'transporting')
	assert.equal(transporting.timeline.length, 3)
	const completed = transitionApplication(
		transporting,
		security,
		'west-gate',
		'arrive',
		'',
		{ mode: 'escort', escortProof: 'test-arrive-proof', lockNo: '' },
	)
	assert.equal(completed.status, 'completed')
	assert.equal(completed.timeline.length, 4)
	assert.equal(completed.timeline.at(-1).actor, security.displayName)
	assert.throws(() => transitionApplication(completed, security, 'west-gate', 'arrive'), /不允许|状态/)
})

test('旧供应商申请流转一律拒绝，改由独立厂牌事件登记', () => {
	const employee = identityByType('employee')
	const supervisor = identityByName('演示主管')
	const security = identityByName('演示安检')
	const pending = application({
		kind: 'supplier',
		fromPostId: '',
		toPostId: 'west-gate',
		supplierName: '演示供应商',
		visitorName: '演示访客',
		materials: '',
		seals: [],
		applicantId: employee.subjectId,
		applicantName: employee.displayName,
	})
	assert.throws(() => transitionApplication(pending, supervisor, '', 'reject'), /不允许|供应商|厂牌/)
	assert.throws(() => transitionApplication(pending, supervisor, '', 'reject', '资料不完整'), /不允许|供应商|厂牌/)
	assert.throws(() => transitionApplication(pending, supervisor, '', 'approve'), /不允许|供应商|厂牌/)
})

test('演示应用是显式少量种子且申请人引用演示正式员工', () => {
	const first = createDemoApplications()
	const second = createDemoApplications()
	assert.equal(first.length, 1)
	assert.deepEqual(first, second)
	assert.deepEqual(first.map(item => item.status).sort(), ['pending'])
	const employee = identityByType('employee')
	for (const item of first) {
		assert.equal(item.kind, 'item-pass')
		assert.equal(item.applicantId, employee.subjectId)
		assert.ok(item.applicantName.includes('演示'))
		assert.ok(item.timeline.length >= 1)
	}
})

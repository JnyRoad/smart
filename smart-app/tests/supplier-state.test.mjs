import { test } from 'node:test'
import assert from 'node:assert/strict'
import { registerHooks } from 'node:module'

registerHooks({
	resolve(name, context, next) {
		if (name === 'vue') return { url: 'data:text/javascript,export const reactive = value => value; export const nextTick = callback => Promise.resolve().then(callback)', shortCircuit: true }
		return next(name, context)
	},
})

globalThis.uni = {
	getStorageSync: () => undefined,
	setStorageSync: () => {},
	switchTab: () => {},
	reLaunch: () => {},
	navigateTo: () => {},
	showToast: () => {},
	request: options => options.success({ statusCode: 200, data: [] }),
}

const state = await import('../state/session.uts')

test('供应商状态提供独立核验、明确进入离开和事件查询签名', async () => {
	state.enterDemo('demo-security-001')
	state.selectPost('east-gate')
	await state.loadSupplierPassages()
	const initialCount = state.supplierState.events.length
	await state.verifySupplierBadge('DEMO-BADGE-001')
	assert.equal(state.supplierState.verification.allowed, true)
	assert.equal(state.supplierState.events.length, initialCount)
	const entered = await state.recordSupplierPassage('enter')
	assert.equal(entered.direction, 'enter')
	assert.equal(state.supplierState.verification, null)
	assert.equal(state.supplierState.events.length, initialCount + 1)
	const repeated = await state.recordSupplierPassage('enter')
	assert.equal(repeated, null)
	assert.match(state.supplierState.error, /核验/)
	assert.equal(state.supplierState.events.length, initialCount + 1)
	await state.verifySupplierBadge('DEMO-BADGE-001')
	assert.equal(state.supplierState.verification.presence, 'inside')
	const left = await state.recordSupplierPassage('leave')
	assert.equal(left.direction, 'leave')
	assert.equal(state.supplierState.verification, null)
	assert.equal(state.supplierState.events.length, initialCount + 2)
	await state.loadSupplierPassages()
	assert.equal(state.supplierState.events.length, initialCount + 2)
	await state.verifySupplierBadge('DEMO-BADGE-001')
	const enteredAgain = await state.recordSupplierPassage('enter')
	assert.notEqual(enteredAgain.id, entered.id)
	assert.equal(state.supplierState.events.length, initialCount + 3)
})

test('供应商拒绝、过期和跨岗位核验不能登记，岗位切换会清除成功核验', async () => {
	state.enterDemo('demo-security-001')
	state.selectPost('east-gate')
	await state.verifySupplierBadge('DEMO-BADGE-DENIED')
	assert.equal(state.supplierState.verification.allowed, false)
	const before = state.supplierState.events.length
	assert.equal(await state.recordSupplierPassage('enter'), null)
	assert.equal(state.supplierState.events.length, before)
	await state.verifySupplierBadge('DEMO-BADGE-EXPIRED')
	assert.equal(state.supplierState.verification.allowed, false)
	assert.equal(await state.recordSupplierPassage('enter'), null)
	await state.verifySupplierBadge('DEMO-BADGE-001')
	assert.equal(state.supplierState.verification.allowed, true)
	state.selectPost('west-gate')
	assert.equal(state.supplierState.verification, null)
	await state.verifySupplierBadge('DEMO-BADGE-001')
	assert.equal(state.supplierState.verification.allowed, false)
})

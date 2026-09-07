/** 执行真实记录页脚本；相机与生命周期以可控桩验证迟到回调，不替代真机验收。 */
import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { stripTypeScriptTypes } from 'node:module'
import { normalizeScan } from '../core/scan.uts'
import { canAct } from '../core/workflow.uts'

const source = readFileSync(new URL('../pages/records/records.uvue', import.meta.url), 'utf8')
const script = source.match(/<script setup lang="uts">([\s\S]*?)<\/script>/)[1]
// 模拟 APP 条件编译，保留真正的相机回调函数；浏览器用例另验 H5 不支持提示。
const appScript = script.replace(/\/\/ #ifdef H5 \|\| MP-ALIPAY[\s\S]*?\/\/ #endif/g, '')
const executable = stripTypeScriptTypes(appScript.replace(/^import[^\n]*\n/gm, ''), { mode: 'strip' })

function mountRecords({ post = 'east-gate', mode = 'hardware' } = {}) {
  const identity = { subjectId: 'security', permissions: ['item-pass:execute'], posts: [{ id: 'east-gate' }, { id: 'west-gate' }] }
  const item = { id: 'release-001', kind: 'item-pass', title: '样品移交', applicantId: 'employee', applicantName: '申请人甲', seals: ['00001234'], status: 'approved', fromPostId: 'east-gate', toPostId: 'west-gate' }
  const state = { identity, postId: post, scanMode: mode, applications: [item, { ...item, id: 'release-002', title: '其他资料', seals: ['00005678'] }], loading: false, error: '' }
  const lifecycle = {}
  const watchers = []
  const counters = { loads: 0, navigations: 0, cameras: [] }
  const deps = {
    ref: value => ({ value }),
    computed: read => ({ get value() { return read() } }),
    nextTick: callback => Promise.resolve().then(callback),
    watch: (read, callback) => watchers.push({ read, callback, value: read() }),
    onLoad: callback => { lifecycle.load = callback },
    onShow: callback => { lifecycle.show = callback },
    onReady: callback => { lifecycle.ready = callback },
    onHide: callback => { lifecycle.hide = callback },
    onUnload: callback => { lifecycle.unload = callback },
    clientState: state,
    guard: () => state.identity != null,
    hasExecutionPost: () => state.identity != null && state.postId.length > 0,
    postName: () => state.postId,
    loadApplications: async () => { counters.loads += 1 },
    loadExecutionApplications: async () => { counters.loads += 1 },
    errorMessage: error => error.message,
    normalizeScan,
    canAct,
    uni: { navigateTo: () => { counters.navigations += 1 }, switchTab: () => {}, scanCode: options => counters.cameras.push(options) },
  }
  const create = new Function(...Object.keys(deps), executable + '\nreturn {query, entries, onSearchInput, confirmSearch, clearSearch, cameraSearch, searchFocused, searchError};')
  const page = create(...Object.values(deps))
  lifecycle.load({ kind: 'item-pass', view: 'execute' })
  lifecycle.show()
  lifecycle.ready?.()
  const flush = async () => {
    for (const watcher of watchers) {
      const current = watcher.read()
      if (current !== watcher.value) { watcher.callback(current, watcher.value); watcher.value = current }
    }
    await Promise.resolve()
    await Promise.resolve()
  }
  return { page, state, lifecycle, counters, flush }
}

test('现场输入即时筛选已加载列表，不等回车、不增加请求或跳转', async () => {
  const { page, counters, flush } = mountRecords()
  await flush()
  assert.equal(page.searchFocused.value, true)
  assert.equal(counters.loads, 1)
  page.onSearchInput({ detail: { value: '000012' } })
  assert.deepEqual(page.entries.value.map(item => item.id), ['release-001'])
  page.onSearchInput({ detail: { value: '其他' } })
  assert.deepEqual(page.entries.value.map(item => item.id), ['release-002'])
  page.confirmSearch({ detail: { value: '00001234\r\n' } })
  assert.equal(page.query.value, '00001234')
  assert.equal(counters.loads, 1)
  assert.equal(counters.navigations, 0)
  page.clearSearch()
  await flush()
  assert.equal(page.query.value, '')
  assert.equal(page.entries.value.length, 2)
  assert.equal(page.searchFocused.value, true)
})

test('未选岗位不加载、不接收输入、不聚焦也不调用相机', async () => {
  const { page, counters, flush } = mountRecords({ post: '' })
  await flush()
  page.onSearchInput({ detail: { value: '00001234' } })
  page.confirmSearch({ detail: { value: '00001234' } })
  page.cameraSearch()
  assert.equal(counters.loads, 0)
  assert.equal(counters.cameras.length, 0)
  assert.equal(page.query.value, '')
  assert.equal(page.searchFocused.value, false)
  assert.deepEqual(page.entries.value, [])
})

test('相机只更新同一搜索框，保留前导零且不跳详情、不触发执行', () => {
  const { page, counters } = mountRecords({ mode: 'camera' })
  page.cameraSearch()
  assert.equal(counters.cameras.length, 1)
  counters.cameras[0].success({ result: '00001234\n' })
  counters.cameras[0].complete()
  assert.equal(page.query.value, '00001234')
  assert.deepEqual(page.entries.value.map(item => item.id), ['release-001'])
  assert.equal(counters.loads, 1)
  assert.equal(counters.navigations, 0)
})

test('离页、换岗、换账号及切换扫码方式均丢弃相机迟到结果', async () => {
  for (const change of [
    mounted => mounted.lifecycle.hide(),
    mounted => { mounted.state.postId = 'west-gate' },
    mounted => { mounted.state.identity = { ...mounted.state.identity, subjectId: 'another' } },
    mounted => { mounted.state.scanMode = 'hardware' },
  ]) {
    const mounted = mountRecords({ mode: 'camera' })
    mounted.page.cameraSearch()
    change(mounted)
    await mounted.flush()
    mounted.counters.cameras[0].success({ result: '00001234' })
    mounted.counters.cameras[0].fail({ errMsg: 'late failure' })
    mounted.counters.cameras[0].complete()
    assert.equal(mounted.page.query.value, '')
    assert.equal(mounted.page.searchError.value, '')
  }
})

test('相机取消保留查询，错误码明确提示，加载期间不会打开相机', () => {
  const { page, state, counters } = mountRecords({ mode: 'camera' })
  page.onSearchInput({ detail: { value: '样品' } })
  state.loading = true
  page.cameraSearch()
  assert.equal(counters.cameras.length, 0)
  state.loading = false
  page.cameraSearch()
  counters.cameras[0].fail({ errMsg: 'scanCode:fail cancel' })
  counters.cameras[0].complete()
  assert.equal(page.query.value, '样品')
  assert.equal(page.searchError.value, '')
  page.cameraSearch()
  counters.cameras[1].success({ result: '\u0000bad' })
  counters.cameras[1].complete()
  assert.match(page.searchError.value, /控制字符/)
  assert.equal(page.query.value, '样品')
})

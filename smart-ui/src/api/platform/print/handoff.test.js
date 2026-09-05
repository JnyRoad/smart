import { beforeEach, expect, it, vi } from 'vitest'
import UserLogin from '@/page/login/userlogin.vue'
import { printLoginTarget, printLoginDestination } from './handoff'
const state = vi.hoisted(() => ({ guard: null }))
vi.mock('@/router/router', () => ({ default: { beforeEach: fn => { state.guard = fn }, afterEach: vi.fn(), onError: vi.fn(), $avueRouter: {} } }))
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
vi.mock('@/store', () => ({ default: { getters: { website: { lockPage: '/lock' }, access_token: '', roles: [] }, state: { tags: { tagList: [] } } } }))
vi.mock('@/util/store', () => ({ getStore: vi.fn(), setStore: vi.fn(), removeStore: vi.fn() }))
vi.mock('@/error-reporter', () => ({ reportCaughtError: vi.fn() }))
vi.mock('nprogress', () => ({ default: { configure: vi.fn(), start: vi.fn(), done: vi.fn() } }))
const target = { path: '/platform/print/jobs/visitor', query: { parkId: '1', subjects: JSON.stringify([{ subjectType: 'ADMITTANCE', subjectId: '21' }]) } }
beforeEach(() => vi.clearAllMocks())
it('实际匿名守卫保留白名单打印交接，实际账号登录成功后回到同批人员', async () => {
  await import('@/permission')
  const next = vi.fn(); state.guard({ ...target, fullPath: target.path, meta: { isAuth: true } }, {}, next)
  expect(next).toHaveBeenCalledWith(printLoginTarget(target))
  const route = next.mock.calls[0][0]; const push = vi.fn()
  UserLogin.methods.handleLogin.call({
    $refs: { loginForm: { validate: fn => fn(true) } }, rememberPwd: false, clearPwd: vi.fn(), loginForm: { username: 'synthetic-user', password: 'Synthetic1!Password' },
    $store: { dispatch: vi.fn(async () => ({ status: 200, data: { access_token: 'synthetic-test-only', parkList: [] } })) },
    checkPassword: () => false, $router: { push }, $route: route, tagWel: { value: '/welcome' }, $message: { error: vi.fn() }, refreshCode: vi.fn()
  })
  await new Promise(resolve => setTimeout(resolve, 0)); expect(push).toHaveBeenCalledWith(target)
})
it('登录回跳只接受固定访客路径与白名单ID，不接受任意URL或敏感字段', () => {
  expect(printLoginTarget({ ...target, path: 'https://example.com' })).toBeNull()
  expect(printLoginTarget({ ...target, query: { ...target.query, code: 'secret' } })).toBeNull()
  expect(printLoginDestination({ printHandoff: JSON.stringify({ path: 'https://example.com', ...target.query }) })).toBeNull()
  expect(printLoginDestination({ printHandoff: 'bad' })).toBeNull()
  expect(printLoginDestination(printLoginTarget(target).query)).toEqual(target)
})

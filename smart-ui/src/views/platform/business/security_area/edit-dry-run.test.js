import { beforeEach, describe, expect, it, vi } from 'vitest'
import Vue from 'vue'
import { mount } from '@vue/test-utils'
import ElementUI from 'element-ui'

Vue.use(ElementUI)

const editWhiteListObj = vi.fn()
const editAreaListObj = vi.fn()
const getWhiteListObj = vi.fn()
const getOaArea = vi.fn()

vi.mock('./_service', () => ({
  bsSecurityAreaApi: {
    editWhiteListObj,
    editAreaListObj,
    getWhiteListObj,
    getOaArea,
    doSyncTask: vi.fn()
  }
}))

vi.mock('@/api/platform/_publicService', () => ({
  getStaffDetail: vi.fn()
}))

vi.mock('./components/batchAuth', () => ({ default: { name: 'AuthDialog' } }))
vi.mock('./components/delAuth', () => ({ default: { name: 'DelAuthDialog' } }))

const component = (await import('./edit.vue')).default

describe('保密区自动删权演练配置', () => {
  beforeEach(() => {
    editWhiteListObj.mockReset()
    editAreaListObj.mockReset()
    getWhiteListObj.mockReset()
    getOaArea.mockReset()
    editWhiteListObj.mockResolvedValue({ data: { code: 0, data: true } })
    editAreaListObj.mockResolvedValue({ data: { code: 0, data: true } })
    getOaArea.mockResolvedValue({ data: { code: 0, data: [] } })
  })

  it('读取没有 dryRun 的存量配置时按关闭演练展示', async () => {
    getWhiteListObj.mockResolvedValue({
      data: {
        code: 0,
        data: {
          parkId: 1001,
          deleteDay: 30,
          isWhiteList: 0,
          whiteList: []
        }
      }
    })
    const context = {
    areaForm: { parkId: 1001 },
    whiteListForm: {},
      $set: (target, key, value) => { target[key] = value },
      normalizeDryRun: component.methods.normalizeDryRun
    }

    await component.methods.getWhiteListConfig.call(context)

    expect(context.whiteListForm.dryRun).toBe(0)
  })

  it('保存时透传 dryRun，演练开关打开保持为 1', async () => {
    const context = {
      areaList: [],
      areaForm: { parkId: 1001 },
      whiteListForm: {
        parkId: 1001,
        deleteDay: 30,
        isWhiteList: 0,
        dryRun: 1,
        whiteList: []
      },
      $message: vi.fn(),
      initData: vi.fn(),
      normalizeDryRun: component.methods.normalizeDryRun
    }

    await component.methods.saveInfo.call(context)

    expect(editWhiteListObj).toHaveBeenCalledWith(expect.objectContaining({ dryRun: 1 }))
  })

  it('真实挂载 el-switch 时存量缺失 dryRun 也能首次切换为选中', async () => {
    getWhiteListObj.mockResolvedValue({
      data: {
        code: 0,
        data: {
          parkId: 1001,
          deleteDay: 30,
          isWhiteList: 0,
          whiteList: []
        }
      }
    })
    const wrapper = mount(component, {
      mocks: {
        $route: { params: { parkId: '0' } },
        $router: { push: vi.fn() },
        $message: vi.fn(),
        $notify: { error: vi.fn() },
        validatenull: value => value === null || value === undefined || value === ''
      },
      stubs: {
        parkSelect: true,
        AuthDialog: true,
        DelAuthDialog: true
      }
    })

    await Vue.nextTick()
    await Promise.resolve()
    await Vue.nextTick()

    const switchWrapper = wrapper.find('.el-switch')
    expect(switchWrapper.exists()).toBe(true)
    expect(switchWrapper.classes()).not.toContain('is-checked')
    expect(Object.getOwnPropertyDescriptor(wrapper.vm.whiteListForm, 'dryRun').get).toBeTypeOf('function')

    await switchWrapper.trigger('click')
    await Vue.nextTick()

    expect(switchWrapper.classes()).toContain('is-checked')
    wrapper.destroy()
  })
})

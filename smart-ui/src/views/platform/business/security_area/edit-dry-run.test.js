import { beforeEach, describe, expect, it, vi } from 'vitest'
import Vue from 'vue'
import { mount } from '@vue/test-utils'
import ElementUI from 'element-ui'

Vue.use(ElementUI)

const editWhiteListObj = vi.fn()
const editAreaListObj = vi.fn()
const getWhiteListObj = vi.fn()
const getOaArea = vi.fn()
const getStaffDetail = vi.fn()

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
  getStaffDetail
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
    getStaffDetail.mockReset()
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

    await switchWrapper.trigger('click')
    await Vue.nextTick()

    expect(switchWrapper.classes()).toContain('is-checked')
    wrapper.destroy()
  })

  it('真实挂载后添加白名单会保留人员并清空临时输入', async () => {
    getStaffDetail.mockResolvedValue({ data: { code: 0, data: { id: 'staff-1', name: '张三' } } })
    getWhiteListObj.mockResolvedValue({
      data: {
        code: 0,
        data: {
          parkId: 1001,
          deleteDay: 30,
          isWhiteList: 1,
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

    // 固定无关表单校验，通过工号查询、人员回填与添加按钮验证真实输入清空。
    wrapper.vm.$refs.whiteListForm.validateField = (field, callback) => callback('', {})
    const badgeInput = wrapper.find('input[placeholder="点击输入工号，按回车查询姓名"]')
    const nameInput = wrapper.find('input[placeholder="姓名自动获取"]')
    await badgeInput.setValue('A001')
    await badgeInput.trigger('keyup.enter')
    await Promise.resolve()
    await Vue.nextTick()

    expect(getStaffDetail).toHaveBeenCalledWith('A001')
    expect(badgeInput.element.value).toBe('A001')
    expect(nameInput.element.value).toBe('张三')

    await wrapper.find('.line-btn .el-button').trigger('click')
    await Vue.nextTick()

    expect(wrapper.vm.whiteListForm.whiteList).toStrictEqual([{
      staffId: 'staff-1',
      staffBadge: 'A001',
      staffName: '张三'
    }])
    expect(wrapper.vm.whiteListForm.staffId).toBe('')
    expect(wrapper.vm.whiteListForm.staffBadge).toBe('')
    expect(wrapper.vm.whiteListForm.staffName).toBe('')
    expect(badgeInput.element.value).toBe('')
    expect(nameInput.element.value).toBe('')
    wrapper.destroy()
  })
})

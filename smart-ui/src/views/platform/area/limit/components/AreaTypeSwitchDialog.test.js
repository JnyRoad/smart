import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Vue from 'vue'
import ElementUI from 'element-ui'
import AreaTypeSwitchDialog from './AreaTypeSwitchDialog.vue'
import { switchAreaType } from '@/api/platform/area/limit'

Vue.use(ElementUI)

vi.mock('@/api/platform/area/limit', () => ({
  switchAreaType: vi.fn()
}))

describe('AreaTypeSwitchDialog', () => {
  beforeEach(() => {
    switchAreaType.mockReset()
  })

  it('展示当前性质到目标性质的变更方向', async () => {
    const wrapper = mount(AreaTypeSwitchDialog, {
      propsData: {
        visible: true,
        authority: { id: 1, authorityName: '保密_1F2区超黑面检机', areaType: 1 }
      }
    })
    // el-dialog 的内容靠 transition 控制渲染时机，挂载后要等一个 tick 才能拿到弹窗正文
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('保密区域')
    expect(wrapper.text()).toContain('公共区域')
  })

  it('切换成功时触发 success 事件并关闭弹窗', async () => {
    switchAreaType.mockResolvedValue({ data: { data: { success: true, conflicts: [] } } })
    const wrapper = mount(AreaTypeSwitchDialog, {
      propsData: {
        visible: true,
        authority: { id: 1, authorityName: '保密_1F2区超黑面检机', areaType: 1 }
      }
    })

    await wrapper.find('.area-type-switch__confirm').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(switchAreaType).toHaveBeenCalledWith({ id: 1, areaType: 0 })
    expect(wrapper.emitted('success')).toBeTruthy()
    expect(wrapper.emitted('update:visible')[0][0]).toBe(false)
  })

  it('存在冲突时展示冲突设备清单并禁用确认按钮', async () => {
    switchAreaType.mockResolvedValue({
      data: {
        data: {
          success: false,
          conflicts: [
            { deviceId: 'device-A', deviceName: '1F-2区-超黑面检机-03', conflictAuthorityId: 200, conflictAuthorityName: '门禁_AB栋连廊' }
          ]
        }
      }
    })
    const wrapper = mount(AreaTypeSwitchDialog, {
      propsData: {
        visible: true,
        authority: { id: 1, authorityName: '保密_1F2区超黑面检机', areaType: 1 }
      }
    })

    await wrapper.find('.area-type-switch__confirm').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('1F-2区-超黑面检机-03')
    expect(wrapper.text()).toContain('门禁_AB栋连廊')
    expect(wrapper.emitted('success')).toBeFalsy()
    expect(wrapper.find('.area-type-switch__confirm').attributes('disabled')).toBe('disabled')
  })
})

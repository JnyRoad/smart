import { shallowMount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const stubs = {
  ElButton: {
    props: ['disabled', 'loading', 'icon', 'type', 'plain'],
    template: '<button class="button-stub" :disabled="disabled" :data-loading="String(!!loading)" :data-icon="icon || \'\'" @click="$emit(\'click\')"><slot></slot></button>'
  }
}

const component = (await import('./PageToolbar.vue')).default

function mountToolbar(propsData = {}) {
  return shallowMount(component, {
    propsData: {
      selectedPark: null,
      staffLoading: false,
      submitting: false,
      canSubmit: false,
      submitCountText: '',
      ...propsData
    },
    stubs
  })
}

function buttonByText(wrapper, text) {
  return wrapper.findAll('.button-stub').filter(button => button.text().includes(text)).at(0)
}

describe('isc card fast add page toolbar', () => {
  it('renders the existing page title, description, and action buttons', () => {
    const wrapper = mountToolbar()

    expect(wrapper.text()).toContain('ISC卡片快速维护')
    expect(wrapper.text()).toContain('先选择园区和员工，再刷卡或批量粘贴卡号；确认无误后提交，系统会自动同步到ISC。')
    expect(buttonByText(wrapper, '搜索员工').attributes('data-icon')).toBe('el-icon-search')
    expect(buttonByText(wrapper, '清空').attributes('data-icon')).toBe('el-icon-delete')
    expect(buttonByText(wrapper, '批量粘贴').attributes('data-icon')).toBe('el-icon-document')
    expect(buttonByText(wrapper, '批量提交').attributes('data-icon')).toBe('el-icon-check')
    expect(buttonByText(wrapper, '查看同步任务').attributes('data-icon')).toBe('el-icon-refresh')
  })

  it('keeps search and paste disabled until a park is selected', () => {
    const wrapper = mountToolbar()

    expect(buttonByText(wrapper, '搜索员工').attributes('disabled')).toBe('disabled')
    expect(buttonByText(wrapper, '批量粘贴').attributes('disabled')).toBe('disabled')
    expect(buttonByText(wrapper, '清空').attributes('disabled')).toBeUndefined()
    expect(buttonByText(wrapper, '查看同步任务').attributes('disabled')).toBeUndefined()
  })

  it('keeps search locked while staff search is loading', () => {
    const wrapper = mountToolbar({
      selectedPark: { parkId: 'P001' },
      staffLoading: true
    })

    expect(buttonByText(wrapper, '搜索员工').attributes('disabled')).toBe('disabled')
    expect(buttonByText(wrapper, '批量粘贴').attributes('disabled')).toBeUndefined()
  })

  it('renders submit count, loading state, and disabled state from the parent', () => {
    const wrapper = mountToolbar({
      selectedPark: { parkId: 'P001' },
      submitting: true,
      canSubmit: false,
      submitCountText: '(2)'
    })
    const submitButton = buttonByText(wrapper, '批量提交')

    expect(submitButton.text()).toContain('批量提交(2)')
    expect(submitButton.attributes('data-loading')).toBe('true')
    expect(submitButton.attributes('disabled')).toBe('disabled')
  })

  it('emits all toolbar actions without mutating parent state directly', async () => {
    const wrapper = mountToolbar({
      selectedPark: { parkId: 'P001' },
      canSubmit: true,
      submitCountText: '(1)'
    })

    await buttonByText(wrapper, '搜索员工').trigger('click')
    await buttonByText(wrapper, '清空').trigger('click')
    await buttonByText(wrapper, '批量粘贴').trigger('click')
    await buttonByText(wrapper, '批量提交').trigger('click')
    await buttonByText(wrapper, '查看同步任务').trigger('click')

    expect(wrapper.emitted('search-staff')).toHaveLength(1)
    expect(wrapper.emitted('reset')).toHaveLength(1)
    expect(wrapper.emitted('open-paste')).toHaveLength(1)
    expect(wrapper.emitted('submit')).toHaveLength(1)
    expect(wrapper.emitted('open-tasks')).toHaveLength(1)
  })
})

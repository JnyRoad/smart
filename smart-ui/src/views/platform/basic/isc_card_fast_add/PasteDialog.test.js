import { shallowMount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const stubs = {
  ElDialog: {
    props: [
      'title',
      'visible',
      'closeOnClickModal',
      'closeOnPressEscape',
      'showClose'
    ],
    template: '<div class="dialog-stub"><slot></slot><slot name="footer"></slot></div>'
  },
  ElInput: {
    props: ['value', 'disabled', 'placeholder'],
    template: '<textarea class="input-stub" :disabled="disabled" :placeholder="placeholder" :value="value" @input="$emit(\'input\', $event.target.value)"></textarea>'
  },
  ElButton: {
    props: ['disabled', 'loading'],
    template: '<button class="button-stub" :disabled="disabled" @click="$emit(\'click\')"><slot></slot></button>'
  }
}

const component = (await import('./PasteDialog.vue')).default

function mountDialog(propsData = {}) {
  return shallowMount(component, {
    propsData: {
      visible: true,
      resolving: false,
      text: '',
      rows: [],
      errors: [],
      visibleErrors: [],
      placeholder: '10288 1024388812\n10290 1024388845',
      ...propsData
    },
    stubs
  })
}

describe('isc card fast add paste dialog', () => {
  it('renders the guide, example, placeholder, and empty status', () => {
    const wrapper = mountDialog()

    expect(wrapper.text()).toContain('按行粘贴工号和卡号')
    expect(wrapper.text()).toContain('10288 1024388812')
    expect(wrapper.text()).toContain('等待粘贴数据')
    expect(wrapper.find('.input-stub').attributes('placeholder')).toBe('10288 1024388812\n10290 1024388845')
  })

  it('shows validation summary and caps visible error rows', () => {
    const wrapper = mountDialog({
      rows: [{ line: 1 }, { line: 2 }],
      errors: [{ line: 2, message: '卡号格式不正确' }, { line: 3, message: '缺少工号或卡号' }],
      visibleErrors: [{ line: 2, message: '卡号格式不正确' }]
    })

    expect(wrapper.text()).toContain('已识别2/ 200行')
    expect(wrapper.text()).toContain('发现2条问题')
    expect(wrapper.text()).toContain('第2行：卡号格式不正确')
    expect(wrapper.text()).toContain('还有1条问题未显示')
  })

  it('emits text, close, and confirm events without mutating parent state directly', async () => {
    const wrapper = mountDialog({
      rows: [{ line: 1 }],
      text: '10288 1024388812'
    })

    await wrapper.find('.input-stub').setValue('10290 1024388845')
    await wrapper.findAll('.button-stub').at(0).trigger('click')
    await wrapper.findAll('.button-stub').at(1).trigger('click')

    expect(wrapper.emitted('update:text')[0]).toStrictEqual(['10290 1024388845'])
    expect(wrapper.emitted('update:visible')[0]).toStrictEqual([false])
    expect(wrapper.emitted('confirm')).toHaveLength(1)
  })

  it('locks closing, input, and confirm action while resolving', () => {
    const wrapper = mountDialog({
      resolving: true,
      rows: [{ line: 1 }]
    })

    expect(wrapper.findComponent(stubs.ElDialog).props('closeOnClickModal')).toBe(false)
    expect(wrapper.findComponent(stubs.ElDialog).props('closeOnPressEscape')).toBe(false)
    expect(wrapper.findComponent(stubs.ElDialog).props('showClose')).toBe(false)
    expect(wrapper.find('.input-stub').attributes('disabled')).toBe('disabled')
    expect(wrapper.findAll('.button-stub').at(1).attributes('disabled')).toBe('disabled')
  })
})

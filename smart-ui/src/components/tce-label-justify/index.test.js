import { describe, expect, it } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import TceLabelJustify from './index.vue'

// SFC smoke test: proves the vitest + @vitejs/plugin-vue2 + @vue/test-utils
// harness can compile and mount real single-file components.
describe('tce-label-justify', () => {
  it('renders one element per label character', () => {
    const wrapper = shallowMount(TceLabelJustify, {
      propsData: { label: '姓名' }
    })
    const chars = wrapper.findAll('.tce-label-justify-inner div')
    expect(chars.length).toBe(2)
    expect(wrapper.text()).toContain('姓')
    expect(wrapper.text()).toContain('名')
  })

  it('renders the fill text when fill is set', () => {
    const wrapper = shallowMount(TceLabelJustify, {
      propsData: { label: '工号', fill: '：' }
    })
    expect(wrapper.find('.tce-label-justify-fill').text()).toBe('：')
  })

  it('pads with full-width spaces when len is set and fill is empty', () => {
    const wrapper = shallowMount(TceLabelJustify, {
      propsData: { label: '部门', len: 3 }
    })
    // text() trims, and U+3000 counts as whitespace - assert on raw textContent
    expect(wrapper.find('.tce-label-justify-fill').element.textContent).toBe('　　　')
  })
})

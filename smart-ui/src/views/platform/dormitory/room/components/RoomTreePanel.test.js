import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const treeFilter = vi.fn()

const stubs = {
  ElScrollbar: {
    props: ['native'],
    template: '<section class="scrollbar-stub"><slot /></section>'
  },
  ElInput: {
    props: ['placeholder', 'clearable', 'size', 'value'],
    template: '<label class="input-stub" :data-placeholder="placeholder || \'\'"><slot name="append" /></label>'
  },
  ElButton: {
    props: ['type', 'icon'],
    template: '<button class="button-stub" :data-icon="icon || \'\'"><slot /></button>'
  },
  ElTree: {
    name: 'ElTree',
    props: {
      data: Array,
      props: Object,
      filterNodeMethod: Function,
      highlightCurrent: Boolean,
      nodeKey: String,
      accordion: Boolean
    },
    data() {
      return {
        slotNode: {
          level: 2,
          label: 'A栋',
          parent: {
            parent: true
          }
        },
        slotData: {
          id: 20,
          label: 'A栋'
        }
      }
    },
    template: '<div class="my-menu-tree"><slot :node="slotNode" :data="slotData" /></div>',
    methods: {
      filter: treeFilter
    }
  }
}

const component = (await import('./RoomTreePanel.vue')).default

function mountTreePanel(propsData = {}) {
  return shallowMount(component, {
    propsData: {
      treeData: [
        {
          id: 10,
          label: '许昌园区',
          children: [
            {
              id: 20,
              label: 'A栋',
              children: [
                { id: 30, label: '3' }
              ]
            }
          ]
        }
      ],
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      ...propsData
    },
    stubs
  })
}

describe('room tree panel', () => {
  it('renders the existing dormitory tree shell and forwards the tree props', () => {
    const wrapper = mountTreePanel()
    const tree = wrapper.findComponent({ name: 'ElTree' })

    expect(wrapper.text()).toContain('选择楼栋及楼层')
    expect(wrapper.find('.input-stub').attributes('data-placeholder')).toBe('输入关键字进行过滤')
    expect(tree.props('data')).toBe(wrapper.props('treeData'))
    expect(tree.props('props')).toBe(wrapper.props('defaultProps'))
    expect(tree.props('nodeKey')).toBe('id')
    expect(tree.props('highlightCurrent')).toBe(true)
    expect(tree.props('accordion')).toBe(true)
    expect(wrapper.text()).toContain('编辑')
    expect(wrapper.text()).toContain('删除')
    expect(wrapper.text()).toContain('新增楼层')
  })

  it('keeps tree filtering local to the tree component', async () => {
    treeFilter.mockClear()
    const wrapper = mountTreePanel()

    expect(wrapper.vm.filterNode('A', { label: 'A栋' })).toBe(true)
    expect(wrapper.vm.filterNode('Z', { label: 'A栋' })).toBe(false)

    await wrapper.setData({ filterText: 'A' })

    expect(treeFilter).toHaveBeenCalledWith('A')
  })

  it('forwards node click and node action events without touching page state', () => {
    const wrapper = mountTreePanel()
    const data = { id: 20, label: 'A栋' }
    const node = { level: 2 }
    const treeNode = { expanded: false }

    wrapper.vm.emitNodeClick(data, node, treeNode)
    wrapper.vm.emitNodeAction(data, 'EDI', node)

    expect(wrapper.emitted('node-click')).toStrictEqual([[data, node, treeNode]])
    expect(wrapper.emitted('node-action')).toStrictEqual([[data, 'EDI', node]])
  })
})

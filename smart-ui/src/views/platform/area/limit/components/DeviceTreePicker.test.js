import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Vue from 'vue'
import ElementUI from 'element-ui'
import DeviceTreePicker from './DeviceTreePicker.vue'

Vue.use(ElementUI)

function buildTreeData () {
  return [
    {
      id: 'building-a',
      label: 'A栋',
      children: [
        {
          id: 'floor-a1',
          label: '1F',
          children: [
            { id: 'device-a1-01', label: '1F-2区-超黑面检机-01' },
            { id: 'device-a1-03', label: '1F-2区-超黑面检机-03' }
          ]
        }
      ]
    },
    {
      id: 'building-b',
      label: 'B栋',
      children: [
        {
          id: 'floor-b3',
          label: '3F',
          children: [
            { id: 'device-b3-01', label: '3F-成品仓闸机-01' }
          ]
        }
      ]
    }
  ]
}

describe('DeviceTreePicker', () => {
  it('搜索到设备后勾选，会通过 v-model 输出选中的设备id数组', async () => {
    // 树默认不展开任何分支（没有 default-expand-all 了），
    // 所以先搜索让 el-tree 自动展开命中项的祖先节点，再去勾选，
    // 这也是真实用户在“搜索 + 勾选”这条路径上的操作方式
    const wrapper = mount(DeviceTreePicker, {
      propsData: { treeData: buildTreeData(), value: [] }
    })

    await wrapper.find('.device-tree-picker__search input').setValue('超黑面检机-01')
    await wrapper.vm.$nextTick()

    const checkboxes = wrapper.findAll('.el-checkbox__original')
    const targetIndex = wrapper.findAll('.el-tree-node__label')
      .wrappers.findIndex(label => label.text() === '1F-2区-超黑面检机-01')
    await checkboxes.at(targetIndex).setChecked()

    expect(wrapper.emitted('input')[0][0]).toEqual(['device-a1-01'])
  })

  it('已选设备面板平铺展示，不按楼栋分组，点击 x 会移除', async () => {
    const wrapper = mount(DeviceTreePicker, {
      propsData: {
        treeData: buildTreeData(),
        value: ['device-a1-01', 'device-b3-01']
      }
    })
    await wrapper.vm.$nextTick()

    // el-icon-close 是图标字体，通过 CSS 伪元素渲染字形，.text() 拿不到它，
    // 所以已选面板每一行的文本内容就是纯设备名
    const selectedLabels = wrapper.findAll('.device-tree-picker__selected-list li').wrappers.map(li => li.text())
    expect(selectedLabels).toEqual(['1F-2区-超黑面检机-01', '3F-成品仓闸机-01'])

    await wrapper.find('.device-tree-picker__selected-list li .el-icon-close').trigger('click')
    expect(wrapper.emitted('input')[0][0]).toEqual(['device-b3-01'])
  })

  it('搜索框过滤后只显示命中的叶子节点', async () => {
    const wrapper = mount(DeviceTreePicker, {
      propsData: { treeData: buildTreeData(), value: [] }
    })

    await wrapper.find('.device-tree-picker__search input').setValue('成品仓')
    await wrapper.vm.$nextTick()

    // 叶子节点(真正的设备)靠 .el-tree-node__expand-icon 上的 is-leaf 类判断——
    // 分支节点(楼栋/楼层)不管是否命中搜索，都会带着 .el-tree-node__children 容器，
    // 用它来判断“是不是叶子”并不可靠，已经在规划阶段用真实 DOM 验证过这一点
    const visibleLeafLabels = wrapper.findAll('.el-tree-node')
      .wrappers.filter(node => {
        const isVisible = !node.classes('is-hidden')
        const isLeaf = node.find('.el-tree-node__expand-icon').classes('is-leaf')
        return isVisible && isLeaf
      })
      .map(node => node.find('.el-tree-node__label').text())
    expect(visibleLeafLabels).toEqual(['3F-成品仓闸机-01'])
  })

  it('已选设备包含预先勾选的设备时，会自动展开到对应楼栋楼层', async () => {
    const wrapper = mount(DeviceTreePicker, {
      propsData: { treeData: buildTreeData(), value: ['device-b3-01'] }
    })
    await wrapper.vm.$nextTick()

    const buildingBNode = wrapper.findAll('.el-tree-node').wrappers
      .find(node => node.find('.el-tree-node__label').text() === 'B栋')
    expect(buildingBNode.classes()).toContain('is-expanded')
  })

  it('分支节点(楼栋/楼层)的勾选框被禁用，不会把分支 id 混进 value', async () => {
    // 预先勾选一个叶子设备，让树自动展开到 A栋/1F，这样分支节点会渲染出来，
    // 不需要额外模拟展开操作
    const wrapper = mount(DeviceTreePicker, {
      propsData: { treeData: buildTreeData(), value: ['device-a1-01'] }
    })
    await wrapper.vm.$nextTick()

    const buildingANode = wrapper.findAll('.el-tree-node').wrappers
      .find(node => node.find('.el-tree-node__label').text() === 'A栋')
    const buildingACheckbox = buildingANode.find('.el-checkbox__original')

    // 断言 1：分支节点的勾选框在 DOM 上就是 disabled，用户点不了
    expect(buildingACheckbox.attributes('disabled')).toBeTruthy()

    // 断言 2：即使强行触发 change 事件模拟点击，也不会有 input 事件带出 building-a 这个分支 id，
    // 双重保险里的“过滤 checkedKeys”兜底同样生效
    await buildingACheckbox.setChecked()
    const emittedInputs = wrapper.emitted('input') || []
    const anyEmitContainsBranchId = emittedInputs.some(callArgs => callArgs[0].includes('building-a'))
    expect(anyEmitContainsBranchId).toBe(false)
  })
})

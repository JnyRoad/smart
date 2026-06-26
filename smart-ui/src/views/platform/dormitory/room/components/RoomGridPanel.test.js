import { shallowMount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const stubs = {
  ElCheckbox: {
    props: ['label', 'indeterminate', 'value'],
    template: '<label class="checkbox-stub"><slot /></label>'
  },
  ElCheckboxGroup: {
    props: ['value'],
    template: '<div class="checkbox-group-stub"><slot /></div>'
  },
  ElDropdown: {
    template: '<div class="dropdown-stub"><slot /><slot name="dropdown" /></div>'
  },
  ElDropdownMenu: {
    template: '<div class="dropdown-menu-stub"><slot /></div>'
  },
  ElDropdownItem: {
    template: '<button class="dropdown-item-stub" @click="$emit(\'click\')"><slot /></button>'
  }
}

const component = (await import('./RoomGridPanel.vue')).default

function mountGridPanel(propsData = {}) {
  return shallowMount(component, {
    propsData: {
      hasData: true,
      tableData: [
        { id: 1, roomName: '301', bedTotal: 4, roomSex: 0, isDormitoryRoom: 0 },
        { id: 2, roomName: '302', bedTotal: 2, roomSex: 1, isDormitoryRoom: 1 }
      ],
      checkedRoom: [1],
      checkAll: false,
      isIndeterminate: true,
      ...propsData
    },
    stubs,
    filters: {
      f_roomGenderClass(value) {
        return value === 0 ? 'man' : 'woman'
      }
    }
  })
}

function dropdownItemByText(wrapper, text) {
  return wrapper.findAll('.dropdown-item-stub').filter(item => item.text().includes(text)).at(0)
}

describe('room grid panel', () => {
  it('renders the no-data message without room actions when no floor data is available', () => {
    const wrapper = mountGridPanel({
      hasData: false,
      tableData: []
    })

    expect(wrapper.text()).toContain('当前条件下暂无住宿信息（请选择具体楼层）')
    expect(wrapper.text()).not.toContain('全选')
    expect(wrapper.find('.room-list').exists()).toBe(false)
  })

  it('renders the existing legend, room cards, lock marker, and row actions', () => {
    const wrapper = mountGridPanel()

    expect(wrapper.classes()).toContain('room-grid-panel')
    expect(wrapper.text()).toContain('全选')
    expect(wrapper.text()).toContain('男宿')
    expect(wrapper.text()).toContain('女宿')
    expect(wrapper.text()).toContain('夫妻/混住')
    expect(wrapper.text()).toContain('不参与分配')
    expect(wrapper.text()).toContain('301')
    expect(wrapper.text()).toContain('4人间')
    expect(wrapper.text()).toContain('302')
    expect(wrapper.text()).toContain('2人间')
    expect(wrapper.find('.lock').exists()).toBe(true)
    expect(dropdownItemByText(wrapper, '编辑房间').exists()).toBe(true)
    expect(dropdownItemByText(wrapper, '删除房间').exists()).toBe(true)
  })

  it('forwards selection and room action events without mutating parent-owned state', () => {
    const wrapper = mountGridPanel()
    const firstRoom = wrapper.props('tableData')[0]

    wrapper.vm.emitCheckAllInput(true)
    wrapper.vm.emitCheckAllChange(true)
    wrapper.vm.emitCheckedRoomInput([1, 2])
    wrapper.vm.emitRoomChange([1, 2])
    wrapper.vm.emitEditRoom(firstRoom)
    wrapper.vm.emitDeleteRoom(firstRoom)

    expect(wrapper.emitted('update-check-all')).toStrictEqual([[true]])
    expect(wrapper.emitted('check-all-change')).toStrictEqual([[true]])
    expect(wrapper.emitted('update-checked-room')).toStrictEqual([[[1, 2]]])
    expect(wrapper.emitted('room-change')).toStrictEqual([[[1, 2]]])
    expect(wrapper.emitted('edit-room')).toStrictEqual([[firstRoom]])
    expect(wrapper.emitted('delete-room')).toStrictEqual([[firstRoom]])
  })
})

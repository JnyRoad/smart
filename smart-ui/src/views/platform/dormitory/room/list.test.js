import { shallowMount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const fetchRoomList = vi.fn()
const floorList = vi.fn()
const delObj = vi.fn()
const putObj = vi.fn()
const putBatchObj = vi.fn()
const putSDBatchObj = vi.fn()
const dormTypeApi = vi.fn()
const allDormitoryType = vi.fn()
const getBedNum = vi.fn()
const fetchSDTempList = vi.fn()
const toolbarResetFields = vi.fn()

vi.mock('@/api/platform/dormitory/room', () => ({
  fetchRoomList,
  floorList,
  delObj,
  putObj,
  putBatchObj,
  putSDBatchObj,
  dormTypeApi,
  allDormitoryType,
  getBedNum,
  fetchSDTempList
}))

vi.mock('@/api/platform/dormitory/dormitory', () => ({
  putDormObj: vi.fn(),
  addObj: vi.fn(),
  delDormObj: vi.fn(),
  getDormObj: vi.fn()
}))

vi.mock('@/api/platform/dormitory/floor', () => ({
  delFloor: vi.fn(),
  addFloor: vi.fn(),
  getFloor: vi.fn(),
  updateDormitoryFloor: vi.fn(),
  getFloorStartNum: vi.fn()
}))

vi.mock('@/util/excel', () => ({
  excel: vi.fn()
}))

vi.mock('echarts', () => ({ default: {} }))

const component = (await import('./list.vue')).default
const mountedWrappers = []

function createResponse(data) {
  return { data: { data } }
}

async function flushPromises() {
  await Promise.resolve()
  await Promise.resolve()
}

function mountRoomList() {
  const wrapper = shallowMount(component, {
    stubs: {
      ElScrollbar: { template: '<div><slot /></div>' },
      ElTree: { template: '<div class="my-menu-tree"></div>' },
      ElForm: { template: '<form><slot /></form>' },
      ElFormItem: {
        props: ['label'],
        template: '<div><span>{{ label }}</span><slot /></div>'
      },
      ElSelect: { template: '<div><slot /></div>' },
      ElOption: {
        props: ['label'],
        template: '<span>{{ label }}</span>'
      },
      ElInput: { template: '<input />' },
      ElButton: { template: '<button><slot /></button>' },
      ElDialog: {
        props: ['title'],
        template: '<section><h2>{{ title }}</h2><slot /><slot name="footer" /></section>'
      },
      ElCheckbox: { template: '<label><slot /></label>' },
      ElCheckboxGroup: { template: '<div><slot /></div>' },
      ElDropdown: { template: '<div><slot /></div>' },
      ElDropdownMenu: { template: '<div><slot /></div>' },
      ElDropdownItem: { template: '<div><slot /></div>' },
      RoomGenderSelect: { template: '<div class="room-gender-select-stub"></div>' },
      RoomTreePanel: {
        name: 'RoomTreePanel',
        props: ['treeData', 'defaultProps'],
        template: '<section class="room-tree-panel-stub my-menu-tree"><span>选择楼栋及楼层</span></section>'
      },
      RoomGridPanel: {
        name: 'RoomGridPanel',
        props: ['hasData', 'tableData', 'checkedRoom', 'checkAll', 'isIndeterminate'],
        template: `
          <section class="room-grid-panel-stub">
            <template v-if="hasData">
              <span>全选</span>
              <span>男宿</span>
              <span>女宿</span>
              <span>夫妻/混住</span>
              <span>不参与分配</span>
              <span>导出表格</span>
              <span>批量设置房间类型</span>
              <span>批量设置房间水电模板</span>
              <span>编辑房间</span>
            </template>
            <span v-else>当前条件下暂无住宿信息（请选择具体楼层）</span>
          </section>
        `
      },
      RoomSearchToolbar: {
        name: 'RoomSearchToolbar',
        props: ['searchForm', 'allDormTypeList', 'hasData', 'exportLoading'],
        template: `
          <section class="room-search-toolbar-stub topForm">
            <span>房间</span>
            <span>是否参与分配</span>
            <span>是否参与计算</span>
            <span>宿舍分类</span>
            <span>房间属性</span>
            <button>搜索</button>
            <button>清空</button>
            <template v-if="hasData">
              <button>导出表格</button>
              <button>批量设置房间类型</button>
              <button>批量设置房间水电模板</button>
            </template>
          </section>
        `,
        methods: {
          resetFields: toolbarResetFields
        }
      }
    },
    mocks: {
      $notify: vi.fn(),
      $message: vi.fn(),
      $msgbox: vi.fn(),
      $confirm: vi.fn(),
      $router: { push: vi.fn() }
    }
  })
  mountedWrappers.push(wrapper)
  return wrapper
}

describe('dormitory room list page safety net', () => {
  afterEach(() => {
    mountedWrappers.splice(0).forEach(wrapper => wrapper.destroy())
  })

  beforeEach(() => {
    fetchRoomList.mockReset()
    floorList.mockReset()
    delObj.mockReset()
    putObj.mockReset()
    putBatchObj.mockReset()
    putSDBatchObj.mockReset()
    dormTypeApi.mockReset()
    allDormitoryType.mockReset()
    getBedNum.mockReset()
    fetchSDTempList.mockReset()
    toolbarResetFields.mockReset()

    fetchRoomList.mockResolvedValue(createResponse([]))
    floorList.mockResolvedValue(createResponse([
      {
        id: 10,
        label: '许昌园区',
        children: [
          { id: 20, label: 'A栋', children: [] }
        ]
      }
    ]))
    allDormitoryType.mockResolvedValue(createResponse([{ id: 1, typeName: '员工宿舍' }]))
    dormTypeApi.mockResolvedValue(createResponse([{ id: 1, typeName: '员工宿舍' }]))
    getBedNum.mockResolvedValue(createResponse({ bedTotal: 4 }))
    fetchSDTempList.mockResolvedValue(createResponse([{ id: 9, templateName: '默认模板' }]))
  })

  it('mounts the search form, room tree, and empty-room state without changing created API calls', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    expect(floorList).toHaveBeenCalledTimes(1)
    expect(allDormitoryType).toHaveBeenCalledTimes(1)
    expect(fetchRoomList).not.toHaveBeenCalled()
    expect(wrapper.vm.parkId).toBe(10)
    expect(wrapper.vm.dormitoryId).toBe(20)
    expect(wrapper.vm.floorId).toBe(null)
    expect(wrapper.vm.defaultKey).toBe(20)
    expect(wrapper.find('.topForm').exists()).toBe(true)
    expect(wrapper.find('.my-menu-tree').exists()).toBe(true)
    const treePanel = wrapper.findComponent({ name: 'RoomTreePanel' })
    expect(treePanel.exists()).toBe(true)
    expect(treePanel.props('treeData')).toBe(wrapper.vm.treeData)
    expect(treePanel.props('defaultProps')).toBe(wrapper.vm.defaultProps)
    expect(wrapper.text()).toContain('是否参与分配')
    expect(wrapper.text()).toContain('是否参与计算')
    expect(wrapper.text()).toContain('宿舍分类')
    expect(wrapper.text()).toContain('房间属性')
    expect(wrapper.text()).toContain('当前条件下暂无住宿信息（请选择具体楼层）')
    const toolbar = wrapper.findComponent({ name: 'RoomSearchToolbar' })
    const gridPanel = wrapper.findComponent({ name: 'RoomGridPanel' })
    expect(toolbar.exists()).toBe(true)
    expect(toolbar.props('searchForm')).toBe(wrapper.vm.searchForm)
    expect(toolbar.props('allDormTypeList')).toBe(wrapper.vm.allDormTypeList)
    expect(toolbar.props('hasData')).toBe(false)
    expect(toolbar.props('exportLoading')).toBe(false)
    expect(gridPanel.exists()).toBe(true)
    expect(gridPanel.props('hasData')).toBe(false)
    expect(gridPanel.props('tableData')).toBe(wrapper.vm.tableData)
    expect(gridPanel.props('checkedRoom')).toBe(wrapper.vm.checkedRoom)
    expect(gridPanel.props('checkAll')).toBe(false)
    expect(gridPanel.props('isIndeterminate')).toBe(false)
  })

  it('keeps room export and batch edit entries visible when a floor has rooms', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    await wrapper.setData({
      parkId: 10,
      dormitoryId: 20,
      floorId: 30,
      tableData: [
        { id: 1, roomName: '301', bedTotal: 4, roomSex: 0, isDormitoryRoom: 0 }
      ]
    })

    expect(wrapper.text()).toContain('导出表格')
    expect(wrapper.text()).toContain('批量设置房间类型')
    expect(wrapper.text()).toContain('批量设置房间水电模板')
    expect(wrapper.text()).toContain('编辑房间')
    const gridPanel = wrapper.findComponent({ name: 'RoomGridPanel' })
    expect(gridPanel.props('hasData')).toBe(true)
    expect(gridPanel.props('tableData')).toBe(wrapper.vm.tableData)
  })

  it('keeps toolbar events delegated to the existing page methods', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    await wrapper.setData({
      parkId: 10,
      dormitoryId: 20,
      floorId: 30
    })
    const toolbar = wrapper.findComponent({ name: 'RoomSearchToolbar' })

    toolbar.vm.$emit('update-search-field', { field: 'roomSex', value: '2' })
    expect(wrapper.vm.searchForm.roomSex).toBe('2')

    fetchRoomList.mockClear()
    toolbar.vm.$emit('search')
    await flushPromises()

    expect(fetchRoomList).toHaveBeenCalledWith({
      asc: 'room_name',
      parkId: 10,
      dormitoryId: 20,
      floorId: 30,
      roomSex: '2'
    })

    fetchRoomList.mockClear()
    toolbar.vm.$emit('reset')
    await flushPromises()

    expect(fetchRoomList).toHaveBeenCalledWith({
      asc: 'room_name',
      parkId: 10,
      dormitoryId: 20,
      floorId: 30
    })
    expect(toolbarResetFields).toHaveBeenCalledTimes(1)
  })

  it('keeps room grid events delegated to the existing page methods', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    const gridPanel = wrapper.findComponent({ name: 'RoomGridPanel' })
    const room = { id: 1, roomName: '301', bedTotal: 4, roomSex: 0, isDormitoryRoom: 0 }
    const handleEditSpy = vi.spyOn(wrapper.vm, 'handleEdit').mockImplementation(() => {})
    const rowDelSpy = vi.spyOn(wrapper.vm, 'rowDel').mockImplementation(() => {})

    await wrapper.setData({
      tableData: [room]
    })

    gridPanel.vm.$emit('update-checked-room', [1])
    expect(wrapper.vm.checkedRoom).toStrictEqual([1])

    gridPanel.vm.$emit('update-check-all', true)
    expect(wrapper.vm.checkAll).toBe(true)

    gridPanel.vm.$emit('check-all-change', true)
    expect(wrapper.vm.checkedRoom).toStrictEqual([1])
    expect(wrapper.vm.isIndeterminate).toBe(false)

    gridPanel.vm.$emit('room-change', [1])
    expect(wrapper.vm.checkAll).toBe(true)
    expect(wrapper.vm.isIndeterminate).toBe(false)

    gridPanel.vm.$emit('edit-room', room)
    gridPanel.vm.$emit('delete-room', room)

    expect(handleEditSpy).toHaveBeenCalledWith(room)
    expect(rowDelSpy).toHaveBeenCalledWith(room)
  })
})

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
      RoomGenderSelect: { template: '<div class="room-gender-select-stub"></div>' }
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
    expect(wrapper.text()).toContain('是否参与分配')
    expect(wrapper.text()).toContain('是否参与计算')
    expect(wrapper.text()).toContain('宿舍分类')
    expect(wrapper.text()).toContain('房间属性')
    expect(wrapper.text()).toContain('当前条件下暂无住宿信息（请选择具体楼层）')
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
  })
})

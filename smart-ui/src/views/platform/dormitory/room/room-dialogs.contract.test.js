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
      ElForm: {
        name: 'ElForm',
        props: ['rules', 'model', 'labelWidth', 'labelPosition'],
        template: '<form><slot /></form>'
      },
      ElFormItem: {
        props: ['label', 'prop'],
        template: '<div><span>{{ label }}</span><slot /></div>'
      },
      ElDialog: {
        name: 'ElDialog',
        props: ['title', 'visible'],
        template: '<section><h2>{{ title }}</h2><slot /><slot name="footer" /></section>'
      },
      ElButton: {
        props: ['loading', 'plain', 'type'],
        template: '<button @click="$emit(\'click\')"><slot /></button>'
      },
      ElInput: { template: '<input />' },
      ElOption: {
        props: ['label'],
        template: '<span>{{ label }}</span>'
      },
      ElSelect: { template: '<div><slot /></div>' },
      RoomGenderSelect: { template: '<div class="room-gender-select-stub"></div>' },
      RoomGridPanel: { template: '<section />' },
      RoomSearchToolbar: { template: '<section />' },
      RoomTreePanel: { template: '<section />' },
      RoomDormitoryDialog: {
        name: 'RoomDormitoryDialog',
        props: ['title', 'visible', 'form', 'rules', 'loading'],
        template: `
          <section class="room-dormitory-dialog-stub">
            <span>宿舍楼名称</span>
            <button @click="$emit('close')">取 消</button>
            <button @click="$emit('submit')">确 定</button>
          </section>
        `
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

describe('room dialog contracts', () => {
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
    floorList.mockResolvedValue(createResponse([{ id: 10, label: '许昌园区', children: [] }]))
    allDormitoryType.mockResolvedValue(createResponse([]))
    dormTypeApi.mockResolvedValue(createResponse([]))
    getBedNum.mockResolvedValue(createResponse({ bedTotal: 4 }))
    fetchSDTempList.mockResolvedValue(createResponse([]))
  })

  it('keeps the four dialog shells, titles, form models, and rule bindings', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    const dialogs = wrapper.findAllComponents({ name: 'ElDialog' })
    const forms = wrapper.findAllComponents({ name: 'ElForm' })

    const dormitoryDialog = wrapper.findComponent({ name: 'RoomDormitoryDialog' })

    expect(dialogs).toHaveLength(3)
    expect(forms).toHaveLength(3)
    expect(dormitoryDialog.exists()).toBe(true)
    expect(dialogs.wrappers.map(dialog => dialog.props('title'))).toStrictEqual([
      '编辑房间',
      wrapper.vm.editTitle,
      wrapper.vm.floorTitle
    ])
    expect(dialogs.wrappers.map(dialog => dialog.props('visible'))).toStrictEqual([
      false,
      false,
      false
    ])
    expect(forms.at(0).props()).toMatchObject({
      rules: wrapper.vm.editRules,
      model: wrapper.vm.editForm,
      labelWidth: '120px'
    })
    expect(forms.at(1).props()).toMatchObject({
      rules: wrapper.vm.batchEditRules,
      model: wrapper.vm.batchEditForm,
      labelWidth: '120px'
    })
    expect(forms.at(2).props()).toMatchObject({
      rules: wrapper.vm.floorAddRules,
      model: wrapper.vm.floorForm,
      labelWidth: '80px'
    })
    expect(dormitoryDialog.props()).toMatchObject({
      title: wrapper.vm.dormTitle,
      visible: false,
      rules: wrapper.vm.dormRules,
      form: wrapper.vm.dormForm,
      loading: false
    })

    await wrapper.setData({ editFloor: true })
    expect(wrapper.findAllComponents({ name: 'ElForm' }).at(2).props('rules')).toBe(wrapper.vm.floorEditRules)
  })

  it('keeps dialog field labels and conditional batch/floor fields visible under existing flags', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    expect(wrapper.text()).toContain('房间号')
    expect(wrapper.text()).toContain('是否参与分配')
    expect(wrapper.text()).toContain('是否参与计算')
    expect(wrapper.text()).toContain('宿舍分类')
    expect(wrapper.text()).toContain('床位数')
    expect(wrapper.text()).toContain('房间属性')
    expect(wrapper.text()).toContain('水电分摊模板')
    expect(wrapper.text()).toContain('离职结算模板')
    expect(wrapper.text()).toContain('起始编号')
    expect(wrapper.text()).toContain('楼层数量')
    expect(wrapper.text()).toContain('宿舍楼名称')

    await wrapper.setData({ isHandelSD: true, editFloor: true })

    expect(wrapper.text()).toContain('水电分摊模板')
    expect(wrapper.text()).toContain('楼层编号')
    expect(wrapper.text()).toContain('房间数量')
  })

  it('keeps dormitory dialog field updates delegated to the existing dorm form object', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    const dormitoryDialog = wrapper.findComponent({ name: 'RoomDormitoryDialog' })

    dormitoryDialog.vm.$emit('update-form-field', { field: 'dormitoryName', value: 'B栋' })

    expect(wrapper.vm.dormForm.dormitoryName).toBe('B栋')
  })

  it('keeps each dialog footer delegated to the existing reset and submit methods', async () => {
    const wrapper = mountRoomList()
    await flushPromises()

    const resetEditForm = vi.spyOn(wrapper.vm, 'resetEditForm').mockImplementation(() => {})
    const editSubmit = vi.spyOn(wrapper.vm, 'editSubmit').mockImplementation(() => {})
    const resetBatchEditForm = vi.spyOn(wrapper.vm, 'resetBatchEditForm').mockImplementation(() => {})
    const batchEditSubmit = vi.spyOn(wrapper.vm, 'batchEditSubmit').mockImplementation(() => {})
    const resetFloorForm = vi.spyOn(wrapper.vm, 'resetFloorForm').mockImplementation(() => {})
    const floorSubmit = vi.spyOn(wrapper.vm, 'floorSubmit').mockImplementation(() => {})
    const resetDormForm = vi.spyOn(wrapper.vm, 'resetDormForm').mockImplementation(() => {})
    const dormSubmit = vi.spyOn(wrapper.vm, 'dormSubmit').mockImplementation(() => {})
    const dialogs = wrapper.findAllComponents({ name: 'ElDialog' })
    const dormitoryDialog = wrapper.findComponent({ name: 'RoomDormitoryDialog' })

    await dialogs.at(0).findAll('button').at(0).trigger('click')
    await dialogs.at(0).findAll('button').at(1).trigger('click')
    await dialogs.at(1).findAll('button').at(0).trigger('click')
    await dialogs.at(1).findAll('button').at(1).trigger('click')
    await dialogs.at(2).findAll('button').at(0).trigger('click')
    await dialogs.at(2).findAll('button').at(1).trigger('click')
    await dormitoryDialog.findAll('button').at(0).trigger('click')
    await dormitoryDialog.findAll('button').at(1).trigger('click')

    expect(resetEditForm).toHaveBeenCalledWith('editForm')
    expect(editSubmit).toHaveBeenCalledWith('editForm')
    expect(resetBatchEditForm).toHaveBeenCalledWith('batchEditForm')
    expect(batchEditSubmit).toHaveBeenCalledWith('batchEditForm')
    expect(resetFloorForm).toHaveBeenCalledWith('floorForm')
    expect(floorSubmit).toHaveBeenCalledWith('floorForm')
    expect(resetDormForm).toHaveBeenCalledWith('dormForm')
    expect(dormSubmit).toHaveBeenCalledWith('dormForm')
  })
})

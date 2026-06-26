import { describe, expect, it } from 'vitest'
import {
  createBatchEditRules,
  createDormRules,
  createFloorAddRules,
  createFloorEditRules,
  createRoomEditRules,
  validateFloorCount,
  validateFloorStartNumber,
  validateRoomCount
} from './room-rules'

describe('room form rule factories', () => {
  it('构造楼层新增校验规则时保留旧字段、文案、触发方式和 validator 引用', () => {
    const rules = createFloorAddRules()
    const nextRules = createFloorAddRules()

    expect(rules).toStrictEqual({
      startNum: [
        { required: true, message: '请输入起始编号', trigger: 'blur' },
        { validator: validateFloorStartNumber, trigger: 'blur' }
      ],
      floorNum: [
        { required: true, message: '请输入楼层数量', trigger: 'blur' },
        { validator: validateFloorCount, trigger: 'blur' }
      ]
    })
    expect(rules).not.toBe(nextRules)
    expect(rules.startNum).not.toBe(nextRules.startNum)
    expect(rules.floorNum).not.toBe(nextRules.floorNum)
    expect(rules.startNum[0]).not.toBe(nextRules.startNum[0])
  })

  it('构造楼层编辑校验规则时保留房间数量规则', () => {
    const rules = createFloorEditRules()
    const nextRules = createFloorEditRules()

    expect(rules).toStrictEqual({
      roomNum: [
        { required: true, message: '请输入房间数量', trigger: 'blur' },
        { validator: validateRoomCount, trigger: 'blur' }
      ]
    })
    expect(rules).not.toBe(nextRules)
    expect(rules.roomNum).not.toBe(nextRules.roomNum)
  })

  it('构造楼栋校验规则时保留旧文案和 blur 触发方式', () => {
    const rules = createDormRules()
    const nextRules = createDormRules()

    expect(rules).toStrictEqual({
      dormitoryName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }]
    })
    expect(rules).not.toBe(nextRules)
    expect(rules.dormitoryName).not.toBe(nextRules.dormitoryName)
  })

  it('构造批量编辑校验规则时只保留水电模板必填规则', () => {
    const rules = createBatchEditRules()
    const nextRules = createBatchEditRules()

    expect(rules).toStrictEqual({
      sdTemplateId: [{ required: true, message: '请选择水电模板', trigger: 'change' }]
    })
    expect(rules).not.toBe(nextRules)
    expect(rules.sdTemplateId).not.toBe(nextRules.sdTemplateId)
  })

  it('构造房间编辑校验规则时保留旧字段、文案和触发方式', () => {
    const rules = createRoomEditRules()
    const nextRules = createRoomEditRules()

    expect(rules).toStrictEqual({
      roomName: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
      isDormitoryRoom: [{ required: true, message: '请选择是否参与分配', trigger: 'change' }],
      isCount: [{ required: true, message: '请选择是否参与计算', trigger: 'change' }],
      roomType: [{ required: true, message: '请选择宿舍分类', trigger: 'change' }],
      bedTotal: [{ required: true, message: '请输入床位数', trigger: 'blur' }],
      roomSex: [{ required: true, message: '请选择房间属性', trigger: 'change' }],
      sdTemplateId: [{ required: true, message: '请选择水电模板', trigger: 'change' }]
    })
    expect(rules).not.toBe(nextRules)
    expect(rules.roomName).not.toBe(nextRules.roomName)
    expect(rules.sdTemplateId).not.toBe(nextRules.sdTemplateId)
  })
})

import { beforeEach, describe, expect, it } from 'vitest'
import { useVisitorFlow } from './flow-store'

beforeEach(() => {
  useVisitorFlow.getState().reset()
  localStorage.clear()
})

describe('visitor flow store', () => {
  it('patch 与增删改', () => {
    const s = useVisitorFlow.getState()
    s.patchHost({ receptionistName: '赵经理' })
    s.patchVisitor({ visitorName: '王五' })
    s.addFellow({ fellowName: '李四', fellowPhotoId: 'p1', certNo: '11010519491231002X' })
    s.updateFellow(0, { fellowName: '李四四', fellowPhotoId: 'p1', certNo: '11010519491231002X' })
    s.addCar({ plate: '豫A12345', name: '王五', certType: { code: 2, desc: '身份证复印件' }, certImg: 'img1' })
    s.removeCar(0)
    s.setFactoryAreas('NEW01', { list: ['A1'], custom: '三楼会议室' })
    const cur = useVisitorFlow.getState()
    expect(cur.host.receptionistName).toBe('赵经理')
    expect(cur.visitor.visitorName).toBe('王五')
    expect(cur.fellows[0]?.fellowName).toBe('李四四')
    expect(cur.cars).toHaveLength(0)
    expect(cur.areasByFactory['NEW01']?.list).toEqual(['A1'])
  })

  it('removeFellow 按下标删除', () => {
    const s = useVisitorFlow.getState()
    s.addFellow({ fellowName: 'A', fellowPhotoId: '1', certNo: 'x' })
    s.addFellow({ fellowName: 'B', fellowPhotoId: '2', certNo: 'y' })
    s.removeFellow(0)
    expect(useVisitorFlow.getState().fellows.map((f) => f.fellowName)).toEqual(['B'])
  })

  it('persist 到 localStorage（key=visitor-flow），reset 清空', () => {
    useVisitorFlow.getState().patchVisitor({ visitorName: '王五' })
    expect(localStorage.getItem('visitor-flow')).toContain('王五')
    useVisitorFlow.getState().reset()
    expect(useVisitorFlow.getState().visitor.visitorName).toBe('')
  })

  it('replaceAreas 整体替换区域选择', () => {
    const s = useVisitorFlow.getState()
    s.setFactoryAreas('NEW01', { list: ['A1', 'DEAD'], custom: '' })
    s.replaceAreas({ NEW01: { list: ['A1'], custom: '' } })
    expect(useVisitorFlow.getState().areasByFactory).toEqual({ NEW01: { list: ['A1'], custom: '' } })
  })
})

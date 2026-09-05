import { printRequest } from './client'
const subjectId = value => { if (typeof value === 'number' && !Number.isSafeInteger(value)) throw new Error('访客标识精度异常，请在新工作台重新搜索人员'); return String(value || '') }
import { parseVisitorSelection } from './visitor-selection'
export { parseVisitorSelection } from './visitor-selection'
export const getCutover = parkId => printRequest({ url: '/cutover', method: 'get', params: { parkId } })
/** 状态不可读或回退暂停时不降级打印；新通道交由获准操作员核对真实预览。 */
export async function dispatchVisitorPrint(visitor, subjectType, legacy, navigate) {
  const parkId = String(visitor.parkId || '')
  if (!/^[1-9]\d{0,9}$/.test(parkId)) throw new Error('访客缺少有效园区，请联系管理员核对')
  const state = await getCutover(parkId)
  if (state && state.visitorMode === 'LEGACY' && state.legacyVisitorAllowed === true) return legacy()
  if (!state || state.visitorMode !== 'TEMPLATE' || state.newJobCreationEnabled !== true) throw new Error('本园区打印暂时停止，请联系打印操作员；请勿重复提交')
  if (!['VISITOR', 'ADMITTANCE'].includes(subjectType)) throw new Error('无法确认访客申请来源')
  const subjects = [{ subjectType, subjectId: subjectId(visitor.id) }, ...(visitor.fellowVisitorList || []).filter(person => subjectType !== 'ADMITTANCE' || Number(person.isMain || 0) === 0).map(person => ({ subjectType: subjectType + '_COMPANION', subjectId: subjectId(person.id) }))]
  const encoded = JSON.stringify(subjects); parseVisitorSelection(encoded)
  return navigate({ path: '/platform/print/jobs/visitor', query: { parkId, subjects: encoded } })
}

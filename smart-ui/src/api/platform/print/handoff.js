import { parseVisitorSelection } from './visitor-selection'
const path = '/platform/print/jobs/visitor'
/** 登录中只保留固定打印目的地的园区和记录ID，不接收任意URL或人员内容。 */
function selection(query) {
  if (!query || Object.keys(query).some(key => !['parkId', 'subjects'].includes(key)) || typeof query.parkId !== 'string' || !/^[1-9]\d{0,9}$/.test(query.parkId)) return null
  try { return { parkId: query.parkId, subjects: JSON.stringify(parseVisitorSelection(query.subjects)) } } catch (_) { return null }
}
export function printLoginTarget(to) {
  if (!to || to.path !== path) return null
  const query = selection(to.query)
  return query ? { path: '/login', query: { printHandoff: JSON.stringify(query) } } : null
}
export function printLoginDestination(query) {
  if (!query || typeof query.printHandoff !== 'string' || query.printHandoff.length > 30000) return null
  try { const checked = selection(JSON.parse(query.printHandoff)); return checked ? { path, query: checked } : null } catch (_) { return null }
}

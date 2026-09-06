const visitorTypes = ['VISITOR', 'VISITOR_COMPANION', 'ADMITTANCE', 'ADMITTANCE_COMPANION']
/** 交接只带明确来源和记录ID，浏览器中的人员内容永远不能成为打印快照。 */
export function parseVisitorSelection(value) {
  if (typeof value !== 'string' || value.length > 20000) throw new Error('访客选择格式无效')
  let subjects
  try { subjects = JSON.parse(value) } catch (_) { throw new Error('访客选择格式无效') }
  if (!Array.isArray(subjects) || !subjects.length || subjects.length > 100) throw new Error('一次请选择 1 至 100 位访客')
  const seen = new Set()
  for (const item of subjects) {
    if (!item || Object.keys(item).some(key => !['subjectType', 'subjectId'].includes(key)) || !visitorTypes.includes(item.subjectType) || typeof item.subjectId !== 'string' || !/^[1-9]\d{0,18}$/.test(item.subjectId)) throw new Error('访客选择格式无效')
    const key = item.subjectType + ':' + item.subjectId
    if (seen.has(key)) throw new Error('访客选择存在重复记录')
    seen.add(key)
  }
  return subjects
}

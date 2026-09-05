import { assertSinglePageTemplate } from './template-shape'
import { applyPersonPhotoBindings } from './person-photo'
export const personTypes = { EMPLOYEE: '正式员工', OUTSOURCED: '外包人员', DISPATCHED: '派遣人员', SUPPLIER: '供应商人员', VISITOR: '访客' }
const classifications = { EMPLOYEE: 'STAFF_DEFAULT', OUTSOURCED: 'OUTSOURCE_DEFAULT', DISPATCHED: 'DISPATCH_DEFAULT', SUPPLIER: 'SUPPLIER_DEFAULT', VISITOR: 'VISITOR_NORMAL' }
export const clone = value => JSON.parse(JSON.stringify(value))

/** 只新建一面；介质尺寸须在发布和设备验收中再确认。 */
export function newTemplateDraft(printItemType, personType, faceRole) {
  if (printItemType === 'VISITOR_SLIP' && (faceRole !== 'FRONT' || personType !== 'VISITOR')) throw new Error('访客仅支持单面凭条')
  const page = printItemType === 'STAFF_CARD'
    ? { widthMm: 85.6, heightMm: 53.98, orientation: 'LANDSCAPE', maxPageCount: 1 }
    : { widthMm: 58, heightMm: 76, orientation: 'PORTRAIT', maxPageCount: 1 }
  return { name: '', printItemType, personType, classificationCode: classifications[personType], faceRole, sideCount: 1,
    layoutJson: { schemaVersion: 1, faceRole, sideCount: 1, basePdfRef: null, schemas: [[]] },
    fieldSchemaJson: { fields: [] }, pageSpecJson: page, resourceManifest: [] }
}

/** 业务版本转换为单面画布；受控底板还未授权解析时明确拒绝，避免丢失底图后保存。 */
export function canvasFromVersion(version) {
  const layout = version.layoutJson
  if (!layout || layout.basePdfRef) throw new Error('模板底板需要经过服务端授权加载，当前不能直接编辑')
  const page = version.pageSpecJson
  const canvas = { basePdf: { width: page.widthMm, height: page.heightMm, padding: [0, 0, 0, 0] }, schemas: clone(layout.schemas) }
  assertSinglePageTemplate(canvas, version.printItemType || 'STAFF_CARD')
  applyPersonPhotoBindings(canvas, version.fieldSchemaJson || { fields: [] })
  return canvas
}

/** 保存布局时保留原字段约束及资源清单，后端仍执行权威校验。 */
export function draftFromCanvas(draft, canvas) {
  assertSinglePageTemplate(canvas, draft.printItemType)
  if (typeof canvas.basePdf !== 'object' || canvas.basePdf.width !== draft.pageSpecJson.widthMm || canvas.basePdf.height !== draft.pageSpecJson.heightMm) throw new Error('画布尺寸与模板介质不一致')
  const saved = { ...clone(draft), layoutJson: { ...clone(draft.layoutJson), schemas: clone(canvas.schemas) } }
  applyPersonPhotoBindings(saved.layoutJson, saved.fieldSchemaJson, false)
  return saved
}

/** 所有已发布历史版本均可独立选择，不以是否关联过滤候选。 */
export function publishedChoices(templates, faceRole) {
  return templates.filter(item => item.lifecycleStatus === 'ACTIVE' && item.faceRole === faceRole).flatMap(item => (item.versions || [])
    .filter(version => (version.versionStatus || version.status) === 'PUBLISHED')
    .map(version => ({ ...version, templateId: item.templateId, templateName: item.name, printItemType: item.printItemType,
      personType: item.personType, classificationCode: item.classificationCode, faceRole: item.faceRole,
      label: `${item.name} · v${version.versionNo}` })))
}

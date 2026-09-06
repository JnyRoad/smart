// 业务保存转换必须保持单面、资源及字段定义，不能把画布内部结构直接当后端版本。
import { expect, it } from 'vitest'
import { canvasFromVersion, draftFromCanvas, newTemplateDraft, publishedChoices } from './template-model'

it('正面保存保留字段绑定及受控资源，重新打开仍是同一单面', () => {
  const draft = newTemplateDraft('STAFF_CARD', 'EMPLOYEE', 'FRONT')
  draft.fieldSchemaJson = { fields: [{ key: 'staffName', required: true }] }
  draft.resourceManifest = [{ objectId: 'photo', purpose: 'PHOTO' }]
  const canvas = canvasFromVersion(draft)
  canvas.schemas[0].push({ name: 'staffName', type: 'text', content: '示例员工' })
  const saved = draftFromCanvas(draft, canvas)
  expect(saved.layoutJson.schemas[0][0].content).toBe('示例员工')
  expect(saved.fieldSchemaJson).toEqual(draft.fieldSchemaJson)
  expect(saved.resourceManifest).toEqual(draft.resourceManifest)
  expect(canvasFromVersion(saved).basePdf.width).toBe(85.6)
  expect(draft.layoutJson.schemas[0]).toHaveLength(0)
})

it('拒绝单模板额外页面和未受控底板', () => {
  const draft = newTemplateDraft('VISITOR_SLIP', 'VISITOR', 'FRONT')
  expect(() => draftFromCanvas(draft, { basePdf: {}, schemas: [[], []] })).toThrow()
  expect(() => canvasFromVersion({ ...draft, layoutJson: { ...draft.layoutJson, basePdfRef: { objectId: 'x' } } })).toThrow('底板')
  expect(() => newTemplateDraft('VISITOR_SLIP', 'VISITOR', 'BACK')).toThrow('单面')
})

it('未关联已发布版本仍可选择，归档及草稿不成为打印候选', () => {
  const options = publishedChoices([
    { templateId: 'new', name: '新模板', faceRole: 'FRONT', lifecycleStatus: 'ACTIVE', versions: [{ templateVersionId: 'published', versionNo: 1, status: 'PUBLISHED' }, { templateVersionId: 'draft', status: 'DRAFT' }] },
    { templateId: 'old', lifecycleStatus: 'ARCHIVED', versions: [{ templateVersionId: 'old-v', status: 'PUBLISHED' }] }
  ], 'FRONT')
  expect(options.map(item => item.templateVersionId)).toEqual(['published'])
})
it('新访客模板默认尺寸与随附 Brother 图像容器的58×76毫米打印区域一致', () => {
  expect(newTemplateDraft('VISITOR_SLIP', 'VISITOR', 'FRONT').pageSpecJson).toMatchObject({ widthMm: 58, heightMm: 76 })
})

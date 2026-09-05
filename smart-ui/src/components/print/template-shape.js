// 设计器的最小业务面数约束，服务端发布/生成时仍需独立复验。

/** 校验单页设计模板；打印输出面数由模板组合单独决定。 */
export function assertSinglePageTemplate(template, printItemType) {
  const expected = { STAFF_CARD: 1, VISITOR_SLIP: 1 }[printItemType]
  if (!expected) throw new Error('不支持的打印物类型')
  if (!template || !Array.isArray(template.schemas) || template.schemas.length !== expected ||
      template.schemas.some(page => !Array.isArray(page))) {
    throw new Error('每份模板只能设计一面，请分别创建正面和背面模板')
  }
}

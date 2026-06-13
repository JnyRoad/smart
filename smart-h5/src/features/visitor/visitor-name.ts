/**
 * 访客/随行姓名格式校验。规则取自网关 save 接口的拒绝文案：
 * 「访客姓名输入汉字、英文、数字及下划线1-30个字符」。
 * 之前前端无此校验，错误拖到最后提交才由后端报出；现各输入页点下一步前先校验。
 */
const NAME_PATTERN = /^[一-龥A-Za-z0-9_]{1,30}$/

export const VISITOR_NAME_MESSAGE = '访客姓名输入汉字、英文、数字及下划线1-30个字符'

export function validateVisitorName(name: string): { ok: boolean; message: string } {
  if (NAME_PATTERN.test(name)) return { ok: true, message: '' }
  return { ok: false, message: VISITOR_NAME_MESSAGE }
}

/**
 * 去除全部空白字符（含半角 \s 与全角空格 U+3000）。用于本就不应包含任何
 * 空格的字段：姓名、证件号、车牌、手机号——尤其是从微信/文档复制粘贴时
 * 常带入首尾或中间空格，需在校验与提交前彻底清掉。
 */
export function stripSpaces(s: string): string {
  return s.replace(/[\s　]/g, '')
}

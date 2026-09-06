/** 识别DCloud原生样式诊断，避免工具退出码为零时把被忽略的样式误判为可用。 */
export function hasCompilerErrors(output) {
  const normalized = output.replace(/\u001b\[[0-9;]*m/g, '').replace(/[\u200b\ufeff]/g, '')
  return /\[plugin:uni:[^\]]+\][^\n]*(?:ERROR:|Invalid selector)/.test(normalized)
}

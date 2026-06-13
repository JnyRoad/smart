/**
 * 开发调试登录的 token 提取：支持原始 token 字符串、`xc-access_token`
 * 信封 JSON（`{dataType, content, datetime}`）或包含该信封的整段文本。
 */
export function extractToken(input: string): string | null {
  const text = input.trim()
  if (!text) return null
  try {
    const parsed: unknown = JSON.parse(text)
    if (
      typeof parsed === 'object' &&
      parsed !== null &&
      'content' in parsed &&
      typeof (parsed as { content: unknown }).content === 'string'
    ) {
      return (parsed as { content: string }).content
    }
  } catch {
    // 非 JSON：按原始 token 处理。
  }
  // 整段文本里嵌着信封（例如从 devtools 复制整行键值）
  const match = text.match(/"content"\s*:\s*"([^"]+)"/)
  if (match?.[1]) return match[1]
  return text
}

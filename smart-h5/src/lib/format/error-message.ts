import { ApiError } from '@/lib/api/http'

/**
 * 把任意异常转成可安全展示给用户的提示文案。
 *
 * 判定依据：只有 ApiError 的 message 才是后端返回的业务提示（如「该访客已登记」），
 * 面向用户、可直接展示；其它任何异常（普通 Error、TypeError、第三方库异常、
 * 字符串、null/undefined 等）都可能携带技术细节（堆栈关键字、HTTP 细节、
 * 内部实现），一律折叠为调用方给的兜底文案，避免把技术错误暴露给用户。
 */
export function toUserMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) return error.message
  return fallback
}

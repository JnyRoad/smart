/**
 * 旧短信链接只携带可猜测的申请 id，不能作为通行码授权。统一跳转到短信验证后签发
 * queryToken 的申请记录流，再由服务端按手机号校验申请归属。
 */
export function visitorRecordAccessPath(applyId: string): string | null {
  const normalized = applyId.trim()
  return normalized ? `/visitor/records?redirect=${encodeURIComponent(normalized)}` : null
}

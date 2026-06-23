import { Dialog } from 'antd-mobile'

// 同一时刻只允许一个不可逆确认框存在。
// 必要性：调用方是 `if (!(await confirmIrreversible(...))) return` 后才 setSubmitting(true)，
// 确认框挂起期间页面的 submitting 仍为 false，用户连点会并发产生多个确认框，各自确认后
// 都能越过双提交防护进入 API。把并发守卫收在这一处，比在每个页面加 ref 更简单可靠。
let confirming = false

/**
 * 不可逆操作（审批拒绝/通过、放行、返厂等）执行前的二次确认。
 * 确认返回 true，取消返回 false；调用方据此决定是否继续业务请求。
 * 已有确认框正在显示时再次调用直接返回 false（拒绝并发，防双提交）。
 */
export async function confirmIrreversible(message: string): Promise<boolean> {
  if (confirming) return false
  confirming = true
  try {
    return await Dialog.confirm({
      content: message,
      confirmText: '确定',
      cancelText: '取消',
    })
  } finally {
    confirming = false
  }
}

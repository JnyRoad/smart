import { Dialog } from 'antd-mobile'

/**
 * 不可逆操作（审批拒绝/通过、放行、返厂等）执行前的二次确认。
 * 确认返回 true，取消返回 false；调用方据此决定是否继续业务请求。
 */
export function confirmIrreversible(message: string): Promise<boolean> {
  return Dialog.confirm({
    content: message,
    confirmText: '确定',
    cancelText: '取消',
  })
}

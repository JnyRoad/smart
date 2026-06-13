import type { DormExitDetail } from '@/features/dorm-services/api'
import type { ProcessNode } from '@/features/dorm-services/process'

/**
 * 退宿审批侧时间线归一化：审批详情把时间线放在 processRecord 下，
 * 用户侧详情用 approvalProcess——两者都接受，防空白时间线。
 */
export function exitProcessNodes(detail: Pick<DormExitDetail, 'processRecord' | 'approvalProcess'>): ProcessNode[] {
  return detail.processRecord ?? detail.approvalProcess ?? []
}

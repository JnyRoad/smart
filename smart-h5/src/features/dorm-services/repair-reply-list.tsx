'use client'
import type { RepairReply } from './api'

/** 维修结果回复记录卡（报修详情与报修审批详情共用）。 */
export function RepairReplyList({ replies }: { replies: RepairReply[] | null | undefined }) {
  if (!replies?.length) return null
  return (
    <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
      <h2 className="mb-2 text-[15px] font-bold">维修结果</h2>
      {replies.map((reply, index) => (
        <div key={index} className="border-b border-border-soft py-2.5 last:border-b-0">
          <div className="flex items-center justify-between">
            <span
              className={`text-sm font-semibold ${
                reply.replyStatusDesc === '维修成功' ? 'text-[#16a673]' : 'text-[#d83b36]'
              }`}
            >
              {reply.replyStatusDesc}
            </span>
            <span className="text-xs text-weak">{reply.replyTime}</span>
          </div>
          <p className="mt-1 text-[13px] text-mid">
            {reply.replyName}：{reply.replyDesc}
          </p>
        </div>
      ))}
    </div>
  )
}

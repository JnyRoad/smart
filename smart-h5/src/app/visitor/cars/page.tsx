'use client'
import { ErrorBlock } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { PageShell } from '@/components/page-shell'
import { useMounted } from '@/lib/use-mounted'

/**
 * Vehicle list for the visitor application. Note: like the legacy app, the
 * info page has no entry to this route (commented out upstream); it stays
 * reachable by direct URL and its data is still mapped into the submit body.
 */
export default function CarListPage() {
  const router = useRouter()
  const mounted = useMounted()
  const { cars, removeCar } = useVisitorFlow()

  if (!mounted) return null

  return (
    <PageShell title="来访车辆">
      <p className="mb-3 px-1 text-sm text-mid">已添加车辆（{cars.length}辆）</p>

      {cars.length === 0 ? (
        <ErrorBlock status="empty" title="暂无车辆信息" description="请点击下方按钮添加车辆" />
      ) : (
        <div className="flex flex-col gap-3">
          {cars.map((car, index) => (
            <div
              key={`${car.plate}-${index}`}
              className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]"
            >
              <div className="flex items-center justify-between">
                <span className="text-[15px] font-bold">{car.plate}</span>
                <span className="flex">
                  <button
                    type="button"
                    onClick={() => router.push(`/visitor/cars/add?index=${index}`)}
                    className="min-h-11 px-2 text-sm font-semibold text-[#2376d9]"
                  >
                    编辑
                  </button>
                  {/* Legacy behavior: delete without a confirm dialog. */}
                  <button
                    type="button"
                    onClick={() => removeCar(index)}
                    className="min-h-11 px-2 text-sm font-semibold text-[#d83b36]"
                  >
                    删除
                  </button>
                </span>
              </div>
              <p className="mt-1 text-[13px] text-mid">司机姓名: {car.name}</p>
            </div>
          ))}
        </div>
      )}

      <div className="mt-4 flex gap-3">
        <button
          type="button"
          onClick={() => router.push('/visitor/cars/add')}
          className="h-12 flex-1 rounded-[14px] border-[1.5px] border-brand bg-white text-[15px] font-semibold text-brand active:bg-accent-soft"
        >
          新增车辆
        </button>
        <button
          type="button"
          onClick={() => router.push('/visitor/info')}
          className="h-12 flex-1 rounded-[14px] bg-brand text-[15px] font-semibold text-white active:bg-[#d95f00]"
        >
          确 定
        </button>
      </div>
    </PageShell>
  )
}

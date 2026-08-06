'use client'
import { useQuery } from '@tanstack/react-query'
import { Checkbox, SpinLoading, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import type { FactoryAreaConfig } from '@/features/visitor/api'
import { clearAreaOptionsCache, loadAreaOptions } from '@/features/visitor/area-options'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { getTenantConfig } from '@/lib/config/tenant'
import { useMounted } from '@/lib/use-mounted'

/** Selection form for one resolved factory; remounted per factoryType via key. */
function AreaForm({ factory }: { factory: FactoryAreaConfig & { factoryType: string } }) {
  const router = useRouter()
  const setFactoryAreas = useVisitorFlow((s) => s.setFactoryAreas)
  const areas = factory.areas ?? []
  // Echo the stored selection, intersected with the currently valid codes.
  const [selected, setSelected] = useState<string[]>(() => {
    const stored = useVisitorFlow.getState().areasByFactory[factory.factoryType]
    const valid = new Set(areas.map((a) => a.code))
    return (stored?.list ?? []).filter((code) => valid.has(code))
  })
  const [custom, setCustom] = useState(
    () => useVisitorFlow.getState().areasByFactory[factory.factoryType]?.custom ?? '',
  )

  const allSelected = areas.length > 0 && selected.length === areas.length

  function handleConfirm() {
    setFactoryAreas(factory.factoryType, { list: selected, custom })
    router.back()
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between rounded-2xl bg-white px-4 py-3 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <span className="text-sm text-mid">
          已选 {selected.length}/{areas.length}
        </span>
        <button
          type="button"
          onClick={() => setSelected(allSelected ? [] : areas.map((a) => a.code))}
          className="text-sm font-semibold text-brand"
        >
          {allSelected ? '取消全选' : '全选'}
        </button>
      </div>

      <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        {areas.length === 0 ? (
          <p className="py-8 text-center text-sm text-weak">暂无可选区域</p>
        ) : (
          <Checkbox.Group value={selected} onChange={(v) => setSelected(v as string[])}>
            <div className="flex flex-col gap-3">
              {areas.map((a) => (
                <Checkbox key={a.code} value={a.code}>
                  <span className="text-sm">{a.name}</span>
                </Checkbox>
              ))}
            </div>
          </Checkbox.Group>
        )}
      </div>

      <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="custom-loc">
          详细位置
        </label>
        <input
          id="custom-loc"
          placeholder="可补充详细位置（选填）"
          value={custom}
          onChange={(e) => setCustom(e.target.value)}
          className={SMS_INPUT_CLASS}
        />
      </div>

      <div className="flex gap-3">
        <button
          type="button"
          onClick={() => {
            setSelected([])
            setCustom('')
          }}
          className="h-12 flex-1 rounded-[14px] border-[1.5px] border-brand bg-white text-[15px] font-semibold text-brand active:bg-accent-soft"
        >
          重置
        </button>
        <button
          type="button"
          onClick={handleConfirm}
          className="h-12 flex-1 rounded-[14px] bg-brand text-[15px] font-semibold text-white active:bg-[#d95f00]"
        >
          确定
        </button>
      </div>
    </div>
  )
}

/**
 * Area multi-select for one factory. Entered from the info page with
 * `?type={areaFlag}&factoryType=&parkId=`; selection is written back to the
 * flow store on confirm.
 */
function AreaSelectionInner() {
  const params = useSearchParams()
  const config = getTenantConfig()
  const mounted = useMounted()
  const factoryTypeParam = params.get('factoryType') ?? ''
  const areaFlagParam = params.get('type') ?? ''
  const parkIdParam = Number(params.get('parkId'))
  const parkId = Number.isFinite(parkIdParam) && parkIdParam > 0 ? parkIdParam : config.parkId

  const options = useQuery({
    queryKey: ['visitor', 'area-options', parkId],
    queryFn: () => loadAreaOptions(parkId),
  })

  const factory = options.data?.find((f) =>
    factoryTypeParam ? f.factoryType === factoryTypeParam : String(f.areaFlag ?? '') === areaFlagParam,
  )

  // Config present but this factory is missing: legacy clears the cache and warns.
  const factoryMissing = options.isSuccess && (!factory || !factory.factoryType)
  useEffect(() => {
    if (factoryMissing) {
      clearAreaOptionsCache(parkId)
      Toast.show('授权区域配置不可用，请联系管理员')
    }
  }, [factoryMissing, parkId])

  if (!mounted) return null

  return (
    <PageShell title="添加授权进入区域">
      {options.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : factory?.factoryType ? (
        <AreaForm
          key={factory.factoryType}
          factory={factory as FactoryAreaConfig & { factoryType: string }}
        />
      ) : (
        <p className="py-16 text-center text-sm text-weak">授权区域配置不可用，请联系管理员</p>
      )}
    </PageShell>
  )
}

export default function AreaSelectionPage() {
  return (
    <Suspense>
      <AreaSelectionInner />
    </Suspense>
  )
}

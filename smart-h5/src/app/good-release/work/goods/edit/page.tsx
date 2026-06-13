'use client'
import { DatePicker, Picker, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { PlateInput } from '@/components/plate-input'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { YSFS_OPTIONS, ysfsLabel } from '@/features/good-release/dicts'
import { StaffSearchPopup } from '@/features/good-release/staff-search-popup'
import { useWorkDraft, type WorkGood } from '@/features/good-release/work-draft'
import { useMounted } from '@/lib/use-mounted'

function formatDate(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function Field({
  label,
  required = false,
  children,
}: {
  label: string
  required?: boolean
  children: React.ReactNode
}) {
  return (
    <div className="mt-3">
      <label className="mb-1.5 block text-[13px] font-semibold text-mid">
        {label} {required && <span className="text-[#d83b36]">*</span>}
      </label>
      {children}
    </div>
  )
}

function WorkGoodEditInner() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const params = useSearchParams()
  const indexParam = params.get('index')
  const editIndex = indexParam === null ? null : Number(indexParam)
  const goods = useWorkDraft((s) => s.goods)
  const addGood = useWorkDraft((s) => s.addGood)
  const updateGood = useWorkDraft((s) => s.updateGood)
  const editing = editIndex !== null ? (goods[editIndex] ?? null) : null

  const [wpbm, setWpbm] = useState(editing?.wpbm ?? '')
  const [wpmc, setWpmc] = useState(editing?.wpmc ?? '')
  const [wpdw, setWpdw] = useState(editing?.wpdw ?? '')
  const [wpsl, setWpsl] = useState(editing?.wpsl ?? '')
  const [jsdw, setJsdw] = useState(editing?.jsdw ?? '')
  const [fxrq, setFxrq] = useState(editing?.fxrq ?? '')
  const [bz, setBz] = useState(editing?.bz ?? '')
  const [ysfs, setYsfs] = useState<number | undefined>(editing?.ysfs)
  const [staff, setStaff] = useState<{ name: string; xm: string | number } | null>(
    editing?.name ? { name: editing.name, xm: editing.xm } : null,
  )
  const [cph, setCph] = useState(editing?.cph ?? '')
  const [dateVisible, setDateVisible] = useState(false)
  const [ysfsVisible, setYsfsVisible] = useState(false)
  const [searchVisible, setSearchVisible] = useState(false)

  function handleSubmit() {
    if (!wpbm) return Toast.show('请输入资产编码')
    if (!wpmc) return Toast.show('请输入名称')
    if (!wpdw) return Toast.show('请输入单位')
    if (!wpsl) return Toast.show('请输入数量')
    if (!jsdw) return Toast.show('请输入接收单位')
    if (!fxrq) return Toast.show('请选择放行日期')
    if (ysfs === undefined) return Toast.show('请选择运输方式')
    const good: WorkGood = {
      wpbm,
      wpmc,
      wpdw,
      wpsl,
      jsdw,
      fxrq,
      bz,
      ysfs,
      xm: staff?.xm ?? '',
      name: staff?.name ?? '',
      cph: cph.length <= 1 ? '' : cph,
    }
    if (editIndex !== null && editing) {
      updateGood(editIndex, good)
    } else {
      addGood(good)
    }
    router.push('/good-release/work/goods')
  }

  if (!authorized || !mounted) return null

  return (
    <PageShell title={editing ? '修改放行物品' : '添加放行物品'}>
      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <p className="text-[14px] font-bold">物品信息</p>
        <Field label="资产编码" required>
          <input placeholder="请输入" value={wpbm} onChange={(e) => setWpbm(e.target.value)} className={SMS_INPUT_CLASS} />
        </Field>
        <Field label="名称" required>
          <input placeholder="请输入" value={wpmc} onChange={(e) => setWpmc(e.target.value)} className={SMS_INPUT_CLASS} />
        </Field>
        <Field label="单位" required>
          <input placeholder="请输入" value={wpdw} onChange={(e) => setWpdw(e.target.value)} className={SMS_INPUT_CLASS} />
        </Field>
        <Field label="数量" required>
          <input placeholder="请输入" value={wpsl} onChange={(e) => setWpsl(e.target.value)} className={SMS_INPUT_CLASS} />
        </Field>
      </section>

      <section className="mt-3 rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <p className="text-[14px] font-bold">放行信息</p>
        <Field label="接收单位" required>
          <input placeholder="请输入" value={jsdw} onChange={(e) => setJsdw(e.target.value)} className={SMS_INPUT_CLASS} />
        </Field>
        <Field label="放行日期" required>
          <button type="button" onClick={() => setDateVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {fxrq || <span className="text-weak">请选择放行日期</span>}
          </button>
          <DatePicker
            visible={dateVisible}
            onClose={() => setDateVisible(false)}
            precision="day"
            onConfirm={(d) => setFxrq(formatDate(d))}
          />
        </Field>
        <Field label="备注(原因)">
          <input placeholder="选填" value={bz} onChange={(e) => setBz(e.target.value)} className={SMS_INPUT_CLASS} />
        </Field>
        <Field label="运输方式" required>
          <button type="button" onClick={() => setYsfsVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {ysfs !== undefined ? ysfsLabel(ysfs) : <span className="text-weak">请选择</span>}
          </button>
          <Picker
            visible={ysfsVisible}
            onClose={() => setYsfsVisible(false)}
            columns={[YSFS_OPTIONS.map((o) => ({ label: o.label, value: String(o.value) }))]}
            onConfirm={(v) => {
              if (typeof v[0] === 'string') setYsfs(Number(v[0]))
            }}
          />
        </Field>
      </section>

      <section className="mt-3 rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)] pb-24">
        <p className="text-[14px] font-bold">经手人信息</p>
        <Field label="姓名">
          <button type="button" onClick={() => setSearchVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {staff?.name ?? <span className="text-weak">请输入</span>}
          </button>
        </Field>
        <Field label="车牌号">
          <PlateInput value={cph} onChange={setCph} />
        </Field>
      </section>

      <div className="fixed inset-x-0 bottom-0 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
        <button
          type="button"
          onClick={handleSubmit}
          className="flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          {editing ? '确认修改放行物品' : '确认添加放行物品'}
        </button>
      </div>

      {searchVisible && (
        <StaffSearchPopup
          visible
          onClose={() => setSearchVisible(false)}
          onPicked={({ name, id }) => setStaff({ name, xm: id })}
        />
      )}
    </PageShell>
  )
}

export default function WorkGoodEditPage() {
  return (
    <Suspense>
      <WorkGoodEditInner />
    </Suspense>
  )
}

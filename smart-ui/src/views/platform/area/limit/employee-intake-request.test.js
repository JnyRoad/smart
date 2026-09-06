import { beforeEach, describe, expect, it, vi } from 'vitest'
import { pendingEmployeeIntake, submitEmployeeIntake } from './employee-intake-request'
const intent = { kind: 'REMOVE_ROWS', authId: 9, rowIds: [7, 5, 7] }
const receipt = key => ({ data: { data: { requestKey: key, mode: 'RELIABLE', submitted: true, operationKey: 'real-operation' } } })
function memorySlots () {
  const rows = new Map()
  let tail = Promise.resolve()
  return {
    transact: (user, work) => {
      const result = tail.then(() => {
        const before = rows.has(user) ? JSON.parse(JSON.stringify(rows.get(user))) : undefined
        const next = work(before)
        rows.set(user, JSON.parse(JSON.stringify(next.record)))
        return next.value
      })
      tail = result.catch(() => undefined)
      return result
    }
  }
}
const deferred = () => { let resolve, reject; const promise = new Promise((res, rej) => { resolve = res; reject = rej }); return { promise, resolve, reject } }
const tick = async () => { for (let step = 0; step < 20; step++) await Promise.resolve() }
let storage, capability, send, options, slots
beforeEach(() => {
  const data = new Map(); storage = { getItem: key => data.get(key) || null, setItem: (key, value) => data.set(key, value), removeItem: key => data.delete(key) }
  capability = vi.fn().mockResolvedValue({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: true } } })
  send = vi.fn((body, key) => Promise.resolve(receipt(key)))
  slots = memorySlots()
  options = { actorId: 7, intent, storage, slots, capability, send, isCurrent: () => true }
})
describe('人员请求稳定键', () => {
  it('响应丢失后同原键重试，绕过变化后的capability', async () => {
    send.mockRejectedValueOnce(new Error('网络断开'))
    await expect(submitEmployeeIntake(options)).rejects.toThrow('网络断开')
    const saved = await pendingEmployeeIntake(7, storage, slots)
    expect(saved).not.toBeNull(); expect(saved.requestKey).toMatch(/^[A-Za-z0-9_-]{16,64}$/)
    capability.mockRejectedValue(new Error('组已删除'))
    await submitEmployeeIntake({ ...options, intent: { ...intent, rowIds: [5, 7] } })
    expect(send.mock.calls[1][1]).toBe(send.mock.calls[0][1]); expect(capability).toHaveBeenCalledTimes(1)
    expect(await pendingEmployeeIntake(7, storage, slots)).toBeNull()
  })
  it('成功后的新意图创建新键', async () => {
    await submitEmployeeIntake(options); await submitEmployeeIntake(options)
    expect(send.mock.calls[0][1]).toEqual(expect.any(String)); expect(send.mock.calls[1][1]).not.toBe(send.mock.calls[0][1])
  })
  it('未知结果不允许变更选择并另建请求', async () => {
    send.mockRejectedValueOnce(new Error('未知'))
    await expect(submitEmployeeIntake(options)).rejects.toThrow()
    await expect(submitEmployeeIntake({ ...options, intent: { ...intent, rowIds: [8] } })).rejects.toThrow('先重试')
    expect(send).toHaveBeenCalledTimes(1)
  })
  it('按实际actor隔离，存储只含必要规范意图', async () => {
    send.mockRejectedValueOnce(new Error('未知'))
    await expect(submitEmployeeIntake(options)).rejects.toThrow()
    expect(await pendingEmployeeIntake(8, storage, slots)).toBeNull()
    const saved = await pendingEmployeeIntake(7, storage, slots)
    expect(Object.keys(saved).sort()).toEqual(['intent', 'requestKey', 'version'])
    expect(saved.intent).toEqual({ kind: 'REMOVE_ROWS', authId: 9, rowIds: [5, 7] })
  })
  it('keyed收到legacy或不匹配key不能当成功并遗忘原键', async () => {
    send.mockResolvedValue({ data: { data: { mode: 'LEGACY', submitted: true } } })
    await expect(submitEmployeeIntake(options)).rejects.toThrow('未确认')
    expect(await pendingEmployeeIntake(7, storage, slots)).not.toBeNull()
  })
  it('明确未启用只使用旧无key契约，cap失败不盲降级', async () => {
    capability.mockResolvedValue({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: false } } })
    await submitEmployeeIntake(options); expect(send).toHaveBeenCalledWith(intent, undefined)
    capability.mockRejectedValue(new Error('能力未确认'))
    await expect(submitEmployeeIntake(options)).rejects.toThrow('能力未确认'); expect(send).toHaveBeenCalledTimes(1)
  })
  it('cap等待时切换页面不发送也不保存新意图', async () => {
    await expect(submitEmployeeIntake({ ...options, isCurrent: () => false })).rejects.toThrow('已切换')
    expect(send).not.toHaveBeenCalled(); expect(await pendingEmployeeIntake(7, storage, slots)).toBeNull()
  })
  it('Axios兼容模式返回409对象时保留服务端未受理原因，不能当未知成功', async () => {
    send.mockResolvedValue({ status: 409, data: { code: 409, msg: 'KEYED_UNSUPPORTED：当前未启用，本次未受理' } })
    await expect(submitEmployeeIntake(options)).rejects.toThrow('KEYED_UNSUPPORTED')
    expect(await pendingEmployeeIntake(7, storage, slots)).toBeNull()
  })

  it.each([false, true])('同actor两页cap交错，不同intent=%s时不覆盖原未知键', async different => {
    const caps = [deferred(), deferred()], replies = [deferred(), deferred()], keys = []
    const calls = [0, 1].map(index => submitEmployeeIntake({ ...options,
      intent: different && index === 1 ? { ...intent, rowIds: [8] } : intent,
      capability: () => caps[index].promise,
      send: (body, key) => { keys[index] = key; return replies[index].promise }
    }).catch(error => error))
    await tick(); caps[0].resolve({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: true } } }); await tick()
    const original = (await pendingEmployeeIntake(7, storage, slots)).requestKey
    caps[1].resolve({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: true } } }); await tick()
    if (different) { expect(keys[1]).toBeUndefined(); expect((await calls[1]).message).toContain('先重试') }
    else expect(keys[1]).toBe(original)
    replies[0].reject(new Error('已受理响应丢失')); if (!different) replies[1].reject(new Error('响应丢失'))
    await Promise.all(calls)
    expect((await pendingEmployeeIntake(7, storage, slots)).requestKey).toBe(original)
  })
  it.each([false, true])('cap=false等待期间出现未知键，同intent=%s仍先检查已有槽', async same => {
    const waiting = deferred(), pending = deferred(), keys = []
    const second = submitEmployeeIntake({ ...options, intent: same ? intent : { ...intent, rowIds: [8] }, capability: () => waiting.promise,
      send: (body, key) => { keys.push(key); return pending.promise }
    }).catch(error => error)
    await tick()
    const first = submitEmployeeIntake({ ...options, send: (body, key) => { keys.push(key); return pending.promise } }).catch(error => error)
    await tick(); const original = (await pendingEmployeeIntake(7, storage, slots)).requestKey
    waiting.resolve({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: false } } }); await tick()
    if (!same) expect(keys).toEqual([original])
    if (!same) expect((await second).message).toContain('先重试')
    expect(keys).toEqual(same ? [original, original] : [original])
    pending.reject(new Error('未知')); await Promise.all([first, second])
    expect((await pendingEmployeeIntake(7, storage, slots)).requestKey).toBe(original)
  })
  it.each([400, 409])('旧%s迟到只能结束自己的key，不清除后发未知新key', async status => {
    const normalized = { kind: 'REMOVE_ROWS', authId: 9, rowIds: [5, 7] }
    storage.setItem('smart:employee-intake:v1:7', JSON.stringify({ version: 1, requestKey: 'original-saved-key', intent: normalized }))
    const replies = [deferred(), deferred(), deferred()], keys = []
    const run = index => submitEmployeeIntake({ ...options, send: (body, key) => { keys[index] = key; return replies[index].promise } }).catch(error => error)
    const first = run(0), second = run(1); await tick()
    const refused = () => Object.assign(new Error('明确未受理'), { response: { status, data: { msg: status === 409 ? 'KEYED_UNSUPPORTED：未启用' : '参数无效' } } })
    replies[0].reject(refused()); await first
    expect(await pendingEmployeeIntake(7, storage, slots)).toBeNull()
    const fresh = submitEmployeeIntake({ ...options, intent: { ...intent, rowIds: [8] }, send: (body, key) => { keys[2] = key; return replies[2].promise } }).catch(error => error)
    await tick(); expect(keys[2]).not.toBe(keys[0])
    replies[1].reject(refused()); await second
    expect((await pendingEmployeeIntake(7, storage, slots)).requestKey).toBe(keys[2])
    replies[2].reject(new Error('新请求已受理但响应丢失')); await fresh
    expect((await pendingEmployeeIntake(7, storage, slots)).requestKey).toBe(keys[2])
  })
  it('迁入旧localStorage未知键且结束后不再复活，不删除旧数据', async () => {
    const legacy = JSON.stringify({ version: 1, requestKey: 'original-saved-key', intent: { kind: 'REMOVE_ROWS', authId: 9, rowIds: [5, 7] } })
    storage.setItem('smart:employee-intake:v1:7', legacy)
    await submitEmployeeIntake(options)
    expect(send.mock.calls[0][1]).toBe('original-saved-key'); expect(capability).not.toHaveBeenCalled()
    expect(storage.getItem('smart:employee-intake:v1:7')).toBe(legacy)
    expect(await pendingEmployeeIntake(7, storage, slots)).toBeNull()
  })
  it('新旧并存不同未知键时保留双方并阻止覆盖', async () => {
    send.mockRejectedValueOnce(new Error('新未知')); await expect(submitEmployeeIntake(options)).rejects.toThrow()
    storage.setItem('smart:employee-intake:v1:7', JSON.stringify({ version: 1, requestKey: 'different-legacy-key', intent: { kind: 'REMOVE_ROWS', authId: 9, rowIds: [9] } }))
    await expect(submitEmployeeIntake(options)).rejects.toThrow('旧页面')
    expect(send).toHaveBeenCalledTimes(1)
  })
  it('存储open或提交失败不得外发新键', async () => {
    await expect(submitEmployeeIntake({ ...options, slots: { transact: () => Promise.reject(new Error('存储不可用')) } })).rejects.toThrow('存储不可用')
    expect(capability).not.toHaveBeenCalled(); expect(send).not.toHaveBeenCalled()
    let call = 0
    await expect(submitEmployeeIntake({ ...options, slots: { transact: (user, work) => ++call === 1 ? slots.transact(user, work) : Promise.reject(new Error('事务提交失败')) } })).rejects.toThrow('事务提交失败')
    expect(send).not.toHaveBeenCalled()
  })

})

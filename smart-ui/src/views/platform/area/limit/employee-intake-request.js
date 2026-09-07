// 每个登录用户只有一个权威未确认槽；IndexedDB事务保护同源跨页面写入。
const storageKey = actorId => `smart:employee-intake:v1:${actorId}`
function actor (id) {
  if (!Number.isInteger(Number(id)) || Number(id) <= 0) throw new Error('登录用户尚未确认，请刷新后再操作')
  return String(Number(id))
}
function normalize (input) {
  const authId = Number(input.authId)
  if (!Number.isSafeInteger(authId) || authId <= 0 || authId > 2147483647 || !['REMOVE_ROWS', 'CLEAR_AUTHORITY'].includes(input.kind)) throw new Error('人员权限请求无效')
  const rowIds = input.kind === 'CLEAR_AUTHORITY' ? [] : [...new Set((input.rowIds || []).map(Number))].sort((a, b) => a - b)
  if (input.kind === 'REMOVE_ROWS' && (!rowIds.length || rowIds.some(id => !Number.isSafeInteger(id) || id <= 0 || id > 2147483647))) throw new Error('所选来源无效')
  return { kind: input.kind, authId, rowIds }
}
function validatePending (saved) {
  if (!saved || saved.version !== 1 || !/^[A-Za-z0-9_-]{16,64}$/.test(saved.requestKey) || JSON.stringify(normalize(saved.intent)) !== JSON.stringify(saved.intent)) throw new Error('上次请求记录异常，请联系管理员核验，勿重新提交')
  return saved
}
function legacyPending (user, storage) {
  const raw = storage.getItem(storageKey(user))
  if (!raw) return null
  let saved
  try { saved = JSON.parse(raw) } catch (_) { throw new Error('上次请求记录无法读取，请联系管理员核验，勿重新提交') }
  return validatePending(saved)
}
// 同一object store的readwrite事务由浏览器跨页面串行；不使用localStorage自制锁。
export function createEmployeeIntakeSlotStore (factory = window.indexedDB) {
  return {
    transact (user, work) {
      return new Promise((resolve, reject) => {
        if (!factory) { reject(new Error('浏览器请求存储不可用，请保留原意图后重试')); return }
        const opening = factory.open('smart-employee-intake-v1', 1)
        let settled = false
        opening.onupgradeneeded = () => opening.result.createObjectStore('actors')
        opening.onerror = () => { settled = true; reject(new Error('浏览器请求存储无法打开，请保留原意图后重试')) }
        opening.onblocked = () => { settled = true; reject(new Error('请求存储等待旧页面释放，请关闭旧页面后重试')) }
        opening.onsuccess = () => {
          const database = opening.result
          if (settled) { database.close(); return }
          let transaction, value, failure
          try {
            transaction = database.transaction('actors', 'readwrite')
            transaction.oncomplete = () => { database.close(); resolve(value) }
            transaction.onabort = () => { database.close(); reject(failure || new Error('请求存储事务未提交，请保留原意图后重试')) }
            const store = transaction.objectStore('actors')
            const loading = store.get(user)
            loading.onsuccess = () => {
              try {
                const next = work(loading.result)
                value = next.value
                store.put(next.record, user)
              } catch (error) { failure = error; transaction.abort() }
            }
          } catch (error) { database.close(); reject(error) }
        }
      })
    }
  }
}
function reconcile (record, user, storage) {
  const row = record || { legacyKey: null, pending: null }
  if (row.pending) validatePending(row.pending)
  const legacy = legacyPending(user, storage)
  if (legacy && legacy.requestKey !== row.legacyKey) {
    if (row.pending && (row.pending.requestKey !== legacy.requestKey || JSON.stringify(row.pending.intent) !== JSON.stringify(legacy.intent))) throw new Error('旧页面另有未确认请求，请先核验两次请求，不能覆盖原键')
    row.pending = legacy
    row.legacyKey = legacy.requestKey
  }
  return row
}
function updateSlot (user, storage, slots, change) {
  return slots.transact(user, previous => {
    const record = reconcile(previous, user, storage)
    return { record, value: change(record) }
  })
}
export async function pendingEmployeeIntake (actorId, storage = window.localStorage, slots = createEmployeeIntakeSlotStore()) {
  if (actorId == null) return null
  const user = actor(actorId)
  return updateSlot(user, storage, slots, row => row.pending)
}
export async function submitEmployeeIntake ({ actorId, intent, storage = window.localStorage, slots = createEmployeeIntakeSlotStore(), capability, send, isCurrent = () => true }) {
  const user = actor(actorId)
  const match = row => {
    if (row.pending && JSON.stringify(row.pending.intent) !== JSON.stringify(normalize(intent))) throw new Error('请先重试上次未确认提交，再发起新的操作')
    return row.pending
  }
  let saved = await updateSlot(user, storage, slots, match)
  if (!saved) {
    const response = await capability(intent.authId)
    const cap = response && response.data && response.data.data
    // 能力等待后在同一权威事务重新读取。即使cap=false，也不能绕过期间出现的未知请求。
    saved = await updateSlot(user, storage, slots, row => {
      if (!isCurrent()) throw new Error('权限组或登录用户已切换，本次尚未提交')
      const pending = match(row)
      if (pending) return pending
      if (!cap || cap.intakeVersion !== 1 || typeof cap.reliableIntakeEnabled !== 'boolean') throw new Error('受理能力未确认，本次尚未提交')
      if (!cap.reliableIntakeEnabled) return null
      const bytes = new Uint8Array(16); window.crypto.getRandomValues(bytes)
      row.pending = { version: 1, requestKey: Array.from(bytes, item => item.toString(16).padStart(2, '0')).join(''), intent: normalize(intent) }
      return row.pending
    })
    // 分配事务成功提交后才允许外发，存储失败不会以新键写入服务端。
    if (!isCurrent()) throw new Error('权限组或登录用户已切换，请返回原权限组重试')
    if (!saved) return send(intent, undefined)
  }
  if (!isCurrent()) throw new Error('权限组或登录用户已切换，请返回原权限组重试')
  const finishOwnKey = () => updateSlot(user, storage, slots, row => {
    if (row.pending && row.pending.requestKey === saved.requestKey) row.pending = null
  })
  let response
  try {
    response = await send(saved.intent, saved.requestKey)
    // 项目 Axios 兼容模式会 resolve 非200响应；在此保留状态和服务端原因。
    if (response && response.status && response.status !== 200) {
      const failure = new Error((response.data && response.data.msg) || '请求未确认')
      failure.response = response
      throw failure
    }
  } catch (error) {
    const status = error && error.response && error.response.status
    const message = error && error.response && error.response.data && error.response.data.msg
    // 只有明确的参数拒绝或可靠路径未启用可结束未受理意图；其它结果继续保留原键。
    if (status === 400 || (status === 409 && typeof message === 'string' && message.startsWith('KEYED_UNSUPPORTED：'))) await finishOwnKey()
    throw error
  }
  const receipt = response && response.data && response.data.data
  const accepted = receipt && receipt.mode === 'RELIABLE' && receipt.submitted === true && typeof receipt.operationKey === 'string' && receipt.operationKey.trim() && receipt.operationKey !== 'NO_CHANGE'
  const unchanged = receipt && receipt.mode === 'NO_CHANGE' && receipt.submitted === false && receipt.operationKey == null
  if (!receipt || receipt.requestKey !== saved.requestKey || (!accepted && !unchanged)) throw new Error('提交结果未确认，请使用原请求重试，勿创建新请求')
  // 晚回执只清除自己的键，不能清除同用户随后创建的另一意图。
  await finishOwnKey()
  return response
}

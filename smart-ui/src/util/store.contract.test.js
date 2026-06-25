import { beforeEach, describe, expect, it } from 'vitest'

const EXPECTED_EXPORTS = [
  'clearStore',
  'getAllStore',
  'getStore',
  'removeStore',
  'setStore'
].sort()

function rawLocalItem (name) {
  return JSON.parse(window.localStorage.getItem(`Smart-${name}`))
}

describe('storage/store module compatibility', () => {
  beforeEach(() => {
    window.localStorage.clear()
    window.sessionStorage.clear()
  })

  it('新 storage 路径与旧 store 路径暴露完全相同的命名导出且无 default export', async () => {
    const storageModule = await import('./storage')
    const storeModule = await import('./store')

    expect(Object.keys(storageModule).filter((key) => key !== 'default').sort()).toEqual(EXPECTED_EXPORTS)
    expect(Object.keys(storeModule).filter((key) => key !== 'default').sort()).toEqual(EXPECTED_EXPORTS)
    expect(storageModule.default).toBeUndefined()
    expect(storeModule.default).toBeUndefined()
  })

  it('旧 store 路径 re-export 新 storage 路径的同一批函数引用', async () => {
    const storageModule = await import('./storage')
    const storeModule = await import('./store')

    for (const name of EXPECTED_EXPORTS) {
      expect(storeModule[name]).toBe(storageModule[name])
    }
  })

  it('保持 setStore/getStore/removeStore 的 localStorage 既有行为', async () => {
    const { getStore, removeStore, setStore } = await import('./storage')

    setStore({ name: 'plain', content: 'abc' })
    expect(rawLocalItem('plain')).toMatchObject({
      dataType: 'string',
      content: 'abc'
    })
    expect(getStore({ name: 'plain' })).toBe('abc')

    removeStore({ name: 'plain' })
    expect(getStore({ name: 'plain' })).toBeUndefined()
  })

  it('保持 sessionStorage 优先于 localStorage 的读取顺序', async () => {
    const { getStore, setStore } = await import('./storage')

    setStore({ name: 'same-key', content: 'local-value' })
    setStore({ name: 'same-key', content: 'session-value', type: 'session' })

    expect(getStore({ name: 'same-key' })).toBe('session-value')
  })

  it('保持 debug 模式返回原始包装对象', async () => {
    const { getStore, setStore } = await import('./storage')

    setStore({ name: 'debug-item', content: { nestedValue: 1 } })

    expect(getStore({ name: 'debug-item', debug: true })).toMatchObject({
      dataType: 'object',
      content: { nestedValue: 1 }
    })
  })

  it('保持 number、boolean、object 的既有反序列化行为', async () => {
    const { getStore, setStore } = await import('./storage')

    setStore({ name: 'num', content: 12 })
    setStore({ name: 'bool-true', content: true })
    setStore({ name: 'bool-false', content: false })
    setStore({ name: 'obj', content: { nested: 'value' } })

    expect(getStore({ name: 'num' })).toBe(12)
    expect(getStore({ name: 'bool-true' })).toBe(true)
    expect(getStore({ name: 'bool-false' })).toBe(false)
    expect(getStore({ name: 'obj' })).toEqual({ nested: 'value' })
  })

  it('保持无法 JSON.parse 时直接返回原始字符串的兼容行为', async () => {
    const { getStore } = await import('./storage')

    window.localStorage.setItem('Smart-legacy-raw', 'raw-value')

    expect(getStore({ name: 'legacy-raw' })).toBe('raw-value')
  })
})

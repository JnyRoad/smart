import { describe, expect, it } from 'vitest'

// object.js 是无外部依赖的纯函数模块，直接测试，无需 mock。
import { diff } from './object'

describe('diff', () => {
  describe('非对象比较', () => {
    it('两个相等的原始值返回 true', () => {
      expect(diff(1, 1)).toBe(true)
      expect(diff('a', 'a')).toBe(true)
    })

    it('两个不等的原始值返回 false', () => {
      expect(diff(1, 2)).toBe(false)
      expect(diff('a', 'b')).toBe(false)
    })

    it('一侧为对象、一侧为原始值返回 false', () => {
      expect(diff({}, 1)).toBe(false)
      expect(diff(1, {})).toBe(false)
    })
  })

  describe('忽略 close 字段（路由 tag 的可关闭标记，不参与身份判断）', () => {
    it('仅 close 不同时视为相等', () => {
      const stored = { label: '首页', value: '/home', close: true }
      const incoming = { label: '首页', value: '/home' }
      expect(diff(stored, incoming)).toBe(true)
    })
  })

  // 缺陷 1：原实现首行 `delete obj1.close` 会直接修改调用方传入的 obj1，
  // 在 vuex 的 tagList 上抹掉已有 tag 的 close 字段，造成状态污染。
  describe('不可变：不修改入参', () => {
    it('比较后仍保留 obj1 的 close 字段', () => {
      const stored = { label: '首页', value: '/home', close: true }
      const incoming = { label: '首页', value: '/home' }

      diff(stored, incoming)

      expect('close' in stored).toBe(true)
      expect(stored.close).toBe(true)
    })

    it('不向任一入参写入或删除字段', () => {
      const left = { id: 1, nested: { age: 2 } }
      const right = { id: 1, nested: { age: 2 } }

      diff(left, right)

      expect(left).toStrictEqual({ id: 1, nested: { age: 2 } })
      expect(right).toStrictEqual({ id: 1, nested: { age: 2 } })
    })
  })

  // 缺陷 2：原实现在 for...in 中遇到第一个嵌套对象属性就 `return diff(...)`，
  // 后续属性被整体跳过，导致深比较不完整。
  describe('深比较完整性：任一子属性不等即 false', () => {
    it('第一个嵌套属性相等、但其后的普通属性不等时返回 false', () => {
      const left = { nested: {}, flag: 1 }
      const right = { nested: {}, flag: 2 }
      expect(diff(left, right)).toBe(false)
    })

    it('同路径 tag 但 query 不同时返回 false（params 为首个嵌套属性，query 在其后）', () => {
      const tagA = { label: '用户', value: '/user', params: {}, query: { id: '1' }, group: [] }
      const tagB = { label: '用户', value: '/user', params: {}, query: { id: '2' }, group: [] }
      expect(diff(tagA, tagB)).toBe(false)
    })

    it('多个嵌套属性全部相等时返回 true', () => {
      const tagA = { label: '用户', value: '/user', params: { tab: 'a' }, query: { id: '1' }, group: [] }
      const tagB = { label: '用户', value: '/user', params: { tab: 'a' }, query: { id: '1' }, group: [] }
      expect(diff(tagA, tagB)).toBe(true)
    })

    it('深层嵌套属性不等时返回 false', () => {
      const left = { meta: { auth: { role: 'admin' } } }
      const right = { meta: { auth: { role: 'guest' } } }
      expect(diff(left, right)).toBe(false)
    })
  })

  describe('键数量不同直接返回 false', () => {
    it('obj1 比 obj2 多一个有效字段时返回 false', () => {
      const left = { label: '首页', value: '/home', extra: 1 }
      const right = { label: '首页', value: '/home' }
      expect(diff(left, right)).toBe(false)
    })
  })

  describe('键名集合不一致（即便键数量相同）返回 false', () => {
    it('等长但键名不同时返回 false', () => {
      expect(diff({ foo: 1, bar: 2 }, { foo: 1, baz: 2 })).toBe(false)
    })

    it('等长、键名不同、对应值同为 undefined 时仍返回 false', () => {
      // 防止仅凭值全等误判：{alpha,keep} 与 {beta,keep} 是不同对象，应判不等
      expect(diff({ alpha: undefined, keep: 1 }, { beta: undefined, keep: 1 })).toBe(false)
    })
  })
})

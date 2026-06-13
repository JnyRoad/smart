import { act, renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { useListPager } from './use-list-pager'

interface Row {
  id: number
}

function deferred<T>() {
  let resolve!: (v: T) => void
  const promise = new Promise<T>((r) => (resolve = r))
  return { promise, resolve }
}

describe('useListPager', () => {
  it('loadMore 翻页合并，filterKey 切换重置', async () => {
    const fetcher = vi.fn(async (page: number, key: string) => ({
      rows: [{ id: page * 10 + Number(key) }] as Row[],
      pages: 2,
    }))
    const { result, rerender } = renderHook(
      ({ k }) => useListPager<Row, string>({ filterKey: k, fetchPage: (p) => fetcher(p, k) }),
      { initialProps: { k: '1' } },
    )
    await act(() => result.current.loadMore())
    expect(result.current.rows).toEqual([{ id: 11 }])
    expect(result.current.loadedOnce).toBe(true)
    await act(() => result.current.loadMore())
    expect(result.current.rows).toEqual([{ id: 11 }, { id: 21 }])
    expect(result.current.hasMore).toBe(false)

    rerender({ k: '2' })
    expect(result.current.rows).toEqual([])
    expect(result.current.loadedOnce).toBe(false)
  })

  it('过期响应不污染列表（filterKey 切换后丢弃旧 in-flight）', async () => {
    const slow = deferred<{ rows: Row[]; pages: number }>()
    const { result, rerender } = renderHook(
      ({ k }) =>
        useListPager<Row, string>({
          filterKey: k,
          fetchPage: () =>
            k === 'old' ? slow.promise : Promise.resolve({ rows: [{ id: 99 }], pages: 1 }),
        }),
      { initialProps: { k: 'old' } },
    )
    let oldLoad: Promise<void> | undefined
    act(() => {
      oldLoad = result.current.loadMore()
    })
    rerender({ k: 'new' })
    await act(() => result.current.loadMore())
    slow.resolve({ rows: [{ id: 1 }], pages: 5 })
    await act(async () => {
      await oldLoad
    })
    await waitFor(() => expect(result.current.rows).toEqual([{ id: 99 }]))
    expect(result.current.pages).toBe(1)
  })

  it('refresh 重置到第一页', async () => {
    const fetcher = vi.fn(async (page: number) => ({ rows: [{ id: page }] as Row[], pages: 3 }))
    const { result } = renderHook(() =>
      useListPager<Row, string>({ filterKey: '', fetchPage: fetcher }),
    )
    await act(() => result.current.loadMore())
    await act(() => result.current.loadMore())
    expect(result.current.rows).toEqual([{ id: 1 }, { id: 2 }])
    await act(() => result.current.refresh())
    expect(result.current.rows).toEqual([{ id: 1 }])
    expect(result.current.hasMore).toBe(true)
  })
})

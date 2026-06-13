'use client'
import { useLayoutEffect, useRef, useState } from 'react'

export interface PageResult<Row> {
  rows: Row[]
  pages: number
}

/**
 * Paginated-list state machine shared by the list pages (InfiniteScroll +
 * PullToRefresh). A filterKey change resets the list and invalidates
 * in-flight responses — the month-switch race guard, generalized.
 */
export function useListPager<Row, Key>({
  filterKey,
  fetchPage,
}: {
  filterKey: Key
  fetchPage: (page: number) => Promise<PageResult<Row>>
}) {
  const [rows, setRows] = useState<Row[]>([])
  const [loadedOnce, setLoadedOnce] = useState(false)
  const [paging, setPaging] = useState({ current: 0, pages: 1 })
  // Latest key for staleness checks in async handlers. useLayoutEffect runs
  // synchronously inside the commit, so no microtask (a resolving stale
  // promise) can slip in between the list reset and the ref update — a plain
  // useEffect would leave a paint-length window where a stale response passes
  // the guard after the reset already ran.
  const keyRef = useRef(filterKey)
  useLayoutEffect(() => {
    keyRef.current = filterKey
  }, [filterKey])

  // Reset on filter change: the official "adjusting state when a prop
  // changes" render-time pattern (race-free, no extra effect pass).
  const [prevKey, setPrevKey] = useState(filterKey)
  if (prevKey !== filterKey) {
    setPrevKey(filterKey)
    setRows([])
    setLoadedOnce(false)
    setPaging({ current: 0, pages: 1 })
  }

  async function runLoad(target: number, reset: boolean) {
    const issuedKey = filterKey
    const res = await fetchPage(target)
    // A stale response from a previous filter must not pollute the list.
    if (issuedKey !== keyRef.current) return
    setPaging({ current: target, pages: res.pages })
    setRows((prev) => (reset ? res.rows : [...prev, ...res.rows]))
    setLoadedOnce(true)
  }

  return {
    rows,
    loadedOnce,
    pages: paging.pages,
    hasMore: paging.current < paging.pages,
    loadMore: () => runLoad(loadedOnce ? paging.current + 1 : 1, !loadedOnce),
    refresh: () => runLoad(1, true),
  }
}

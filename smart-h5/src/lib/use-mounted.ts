'use client'
import { useSyncExternalStore } from 'react'

const noopSubscribe = () => () => {}

/**
 * False during SSR/hydration, true after mount. Used to gate forms bound to
 * persisted client-only state (localStorage drafts) without effect-driven
 * setState.
 */
export function useMounted(): boolean {
  return useSyncExternalStore(
    noopSubscribe,
    () => true,
    () => false,
  )
}

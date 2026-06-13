'use client'
import { unstableSetRender } from 'antd-mobile'
import { createRoot, type Root } from 'react-dom/client'

/**
 * antd-mobile v5 imperative APIs (Toast/Dialog/...) call the React 18
 * render/unmount entry points that React 19 removed. This is the official
 * adapter from https://mobile.ant.design/guide/v5-for-19.
 */
type ContainerWithRoot = (Element | DocumentFragment) & { _reactRoot?: Root }

unstableSetRender((node, container) => {
  const target = container as ContainerWithRoot
  target._reactRoot ||= createRoot(target)
  const root = target._reactRoot
  root.render(node)
  return async () => {
    await new Promise((resolve) => setTimeout(resolve, 0))
    root.unmount()
  }
})

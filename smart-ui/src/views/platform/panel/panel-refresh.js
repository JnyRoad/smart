export function clearPanelRefresh(component) {
  if (component.timeOut) {
    clearTimeout(component.timeOut)
    component.timeOut = undefined
  }
}

export function schedulePanelRefresh(component, routePath, refresh, delay = 60000) {
  clearPanelRefresh(component)
  if (component.$route.path === routePath) {
    component.timeOut = setTimeout(refresh, delay)
  }
}

// 按需加载同源的独立 pdfme 运行时，避免旧 Webpack 解析其现代语法。
let runtimePromise
const runtimeVersion = '6.1.12'

/** 检查资源版本与入口，防止发布时混用旧的静态文件。 */
function validRuntime(runtime) {
  return runtime && runtime.version === runtimeVersion && typeof runtime.mountDesigner === 'function'
}

/** 多个宿主共享一次加载；失败和超时后清理资源，下一次调用可以重试。 */
export function loadPdfmeRuntime() {
  if (validRuntime(window.SmartPdfme)) return Promise.resolve(window.SmartPdfme)
  if (runtimePromise) return runtimePromise
  runtimePromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.type = 'module'
    script.dataset.pdfmeRuntime = runtimeVersion
    script.src = `${process.env.BASE_URL || './'}print-designer/runtime.js?v=${runtimeVersion}`
    const timer = setTimeout(() => finish(new Error('模板设计器加载超时')), 15000)

    /** 完成单次资源请求，失败时不保留失效脚本或 Promise。 */
    function finish(error) {
      clearTimeout(timer)
      script.onload = null
      script.onerror = null
      if (error) {
        script.remove()
        runtimePromise = undefined
        reject(error)
      } else {
        resolve(window.SmartPdfme)
      }
    }

    script.onload = () => finish(validRuntime(window.SmartPdfme) ? null : new Error('模板设计器资源版本不匹配'))
    script.onerror = () => finish(new Error('模板设计器资源加载失败'))
    document.head.appendChild(script)
  })
  return runtimePromise
}

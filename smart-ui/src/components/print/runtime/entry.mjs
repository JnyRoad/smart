// 只由现代构建器处理此入口；输出同源 ES module，由 Vue 宿主按需加载。
import { Designer } from '@pdfme/ui'
import { PDFME_VERSION } from '@pdfme/common'
import { text, image, rectangle, line, ellipse, barcodes } from '@pdfme/schemas'
import { assertSinglePageTemplate } from '../template-shape.js'

const plugins = { '文字': text, '图片': image, '背景': rectangle, '直线': line, '椭圆': ellipse, '二维码': barcodes.qrcode, '条形码': barcodes.code128 }

/** 挂载真实 pdfme 画布并限制面数；编辑只输出快照，不访问人员或打印设备。 */
export function mountDesigner({ domContainer, template, printItemType, font, onChange, onError }) {
  assertSinglePageTemplate(template, printItemType)
  let accepted = structuredClone(template)
  let disposed = false
  let editTimer
  let recoveryTimer
  let designer = createDesigner()
  domContainer.addEventListener('input', markPendingEdit, true)
  domContainer.addEventListener('change', markPendingEdit, true)

  /** 上游 6.1.12 属性面板延迟 100ms 提交；稳定前拒绝读取可能过期的快照。 */
  function markPendingEdit() {
    clearTimeout(editTimer)
    editTimer = setTimeout(() => { editTimer = undefined }, 200)
  }

  /** 从最后合法模板重建实例，使非法增删页留下的页游标也得到复位。 */
  function createDesigner() {
    const instance = new Designer({ domContainer, template: structuredClone(accepted), plugins, options: { lang: 'zh', font, zoomLevel: 1.2 } })
    instance.onChangeTemplate(handleChange)
    return instance
  }

  /** 合法编辑发布独立快照；非法面数先阻止保存，再于当前 React 事件结束后恢复。 */
  function handleChange(changed) {
    if (disposed || recoveryTimer !== undefined) return
    try {
      assertSinglePageTemplate(changed, printItemType)
    } catch (error) {
      recoveryTimer = setTimeout(() => {
        recoveryTimer = undefined
        if (disposed) return
        designer.destroy()
        designer = createDesigner()
        onChange(structuredClone(accepted))
      }, 0)
      onError(error)
      return
    }
    accepted = structuredClone(changed)
    onChange(structuredClone(changed))
  }

  return {
    /** 只交付上游已稳定提交的模板；销毁、恢复中或输入尚未应用时明确拒绝。 */
    getTemplate() {
      if (disposed) throw new Error('模板设计器已销毁')
      if (recoveryTimer !== undefined) throw new Error('正在恢复固定业务面，请稍后保存')
      if (editTimer !== undefined) throw new Error('正在应用属性修改，请稍后保存')
      return structuredClone(designer.getTemplate())
    },
    /** 同步撤销恢复任务与 DOM 监听，避免离开页面后出现新画布。 */
    destroy() {
      if (disposed) return
      disposed = true
      clearTimeout(editTimer)
      clearTimeout(recoveryTimer)
      domContainer.removeEventListener('input', markPendingEdit, true)
      domContainer.removeEventListener('change', markPendingEdit, true)
      designer.destroy()
    }
  }
}

window.SmartPdfme = { version: PDFME_VERSION, mountDesigner }

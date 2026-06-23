import { validatenull } from './validate'
import request from '@/router/axios'

// 从 util.js 拆出的 DOM / 窗口 / 图片地址处理工具。
// 由 util.js re-export，保证 @/util/util 对外路径与公共面不变。
// 注意：handleImg 依赖 @/router/axios，原本 util.js 顶层的副作用导入随之迁移到这里，
// util.js 仍经 re-export 间接触发该导入（契约测试已覆盖）。

/**
 * 动态插入 css
 */
export const loadStyle = url => {
  const link = document.createElement('link')
  link.type = 'text/css'
  link.rel = 'stylesheet'
  link.href = url
  const head = document.getElementsByTagName('head')[0]
  head.appendChild(link)
}

/**
 * 打开居中的小窗口
 */
export const openWindow = (url, title, width, height) => {
  // Fixes dual-screen position                            Most browsers       Firefox
  const dualScreenLeft = window.screenLeft !== undefined ? window.screenLeft : screen.left
  const dualScreenTop = window.screenTop !== undefined ? window.screenTop : screen.top

  const viewWidth = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width
  const viewHeight = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height

  const left = ((viewWidth / 2) - (width / 2)) + dualScreenLeft
  const top = ((viewHeight / 2) - (height / 2)) + dualScreenTop
  const newWindow = window.open(url, title, 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=yes, copyhistory=no, width=' + width + ', height=' + height + ', top=' + top + ', left=' + left)

  // Puts focus on the newWindow
  if (window.focus) {
    newWindow.focus()
  }
}

/**
 *  <img> <a> src 处理
 * @returns {PromiseLike<T | never> | Promise<T | never>}
 */
export function handleImg(fileName, id) {
  return validatenull(fileName) ? null : request({
    url: '/admin/file/' + fileName,
    method: 'get',
    responseType: 'blob'
  }).then((response) => { // 处理返回的文件流
    let blob = response.data;
    let img = document.getElementById(id);
    img.src = URL.createObjectURL(blob);
    window.setTimeout(function () {
      window.URL.revokeObjectURL(blob)
    }, 0)
  })
}

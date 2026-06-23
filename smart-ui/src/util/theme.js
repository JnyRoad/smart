// 从 util.js 拆出的主题/灰度类工具（操作 document.body）。
// 由 util.js re-export，保证 @/util/util 对外路径与公共面不变。

/**
 * 设置灰度模式
 */
export const toggleGrayMode = (status) => {
  if (status) {
    document.body.className = document.body.className + ' grayMode'
  } else {
    document.body.className = document.body.className.replace(' grayMode', '')
  }
}
/**
 * 设置主题
 */
export const setTheme = (name) => {
  document.body.className = name
}

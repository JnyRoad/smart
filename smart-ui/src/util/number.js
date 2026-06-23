// 从 util.js 拆出的数值处理类纯函数（无外部依赖）。
// 由 util.js re-export，保证 @/util/util 对外路径与公共面不变。

/**
 * 生成随机 len 位数字
 */
export const randomLenNum = (len, date) => {
  let random = ''
  random = Math.ceil(Math.random() * 100000000000000).toString().substr(0, len || 4)
  if (date) random = random + Date.now()
  return random
}

/**
 * 数字千分位格式化（如 1234567 -> 1,234,567）
 */
export function formatNumber(num) {
  let reg = /(?=(\B)(\d{3})+$)/g
  return num.toString().replace(reg, ',')
}

/**
 * 获取小数后两位的百分比，不带 '%'
 */
export function getProportion(num, sum) {
  if (sum <= 0) {
    sum = 1
  }
  return Math.round((num / sum) * 100 * 100) / 100
}

/**
 * 浮点数相减，arg1 为较大数（规避浮点精度误差）
 */
export function floatNumMinus(arg1, arg2) {
  var decimals1, decimals2, factor, maxDecimals
  try { decimals1 = arg1.toString().split('.')[1].length } catch (err) { decimals1 = 0 }
  try { decimals2 = arg2.toString().split('.')[1].length } catch (err) { decimals2 = 0 }
  factor = Math.pow(10, Math.max(decimals1, decimals2))
  maxDecimals = (decimals1 >= decimals2) ? decimals1 : decimals2
  return ((arg1 * factor - arg2 * factor) / factor).toFixed(maxDecimals)
}

/** 手动下发权限的默认结束日期。 */
export const DEFAULT_PERMISSION_END_DATE = '2030-12-31'

/**
 * 格式化本地日期，避免 Date#toISOString 因时区产生跨日偏差。
 *
 * @param {Date} date 待格式化的本地日期
 * @returns {string} yyyy-MM-dd 格式日期
 */
export function formatPermissionDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 生成人工下发窗口的默认日期范围。
 *
 * @param {Date} now 当前本地时间，测试时可显式传入
 * @returns {[string, string]} [当天, 2030-12-31]
 */
export function getDefaultPermissionDateRange(now = new Date()) {
  return [formatPermissionDate(now), DEFAULT_PERMISSION_END_DATE]
}

/**
 * 判断日期范围是否完整且起始日期不晚于结束日期。
 *
 * 日期控件的 value-format 固定为 yyyy-MM-dd，因此可安全按字符串比较。
 *
 * @param {unknown} dateRange 日期范围
 * @returns {boolean} 是否可提交
 */
export function isPermissionDateRangeValid(dateRange) {
  return Array.isArray(dateRange) && dateRange.length === 2 &&
    typeof dateRange[0] === 'string' && dateRange[0] !== '' &&
    typeof dateRange[1] === 'string' && dateRange[1] !== '' &&
    dateRange[0] <= dateRange[1]
}

/**
 * 将界面日期范围转换为后端请求字段。
 *
 * @param {[string, string]} dateRange 已校验的日期范围
 * @returns {{startTime: string, endTime: string}} 接口请求字段
 * @throws {Error} 日期范围不完整或倒置时阻止发起请求
 */
export function buildPermissionDatePayload(dateRange) {
  if (!isPermissionDateRangeValid(dateRange)) {
    throw new Error('权限结束日期不能早于开始日期')
  }
  return {
    startTime: dateRange[0],
    endTime: dateRange[1]
  }
}

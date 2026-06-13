// 时间转换
// 例 当前时间为:2019-11-1 55:40 => 今天 55:40

import {
  transformDate
} from './date.js'

export default function (date) {
  try {
    const tt = transformDate(date)
    const today = new Date().getDate()
    const days = parseInt((new Date().getTime() - tt.getTime()) / 86400000)
    const year = tt.getFullYear()
    const mouth = tt.getMonth() + 1
    const day = tt.getDate()
    const time = tt.getHours() < 10 ? '0' + tt.getHours() : tt.getHours()
    const min = tt.getMinutes() < 10 ? '0' + tt.getMinutes() : tt.getMinutes()
    if (days > 2) {
      return year + '-' + mouth + '-' + day + ' ' + time + ':' + min
    }
    const offset = Math.abs(today - day)
    switch (offset) {
      case 0:
        return '今天 ' + time + ':' + min
      case 1:
        return '昨天 ' + time + ':' + min
      case 2:
        return '前天 ' + time + ':' + min
      default:
        return year + '-' + mouth + '-' + day + ' ' + time + ':' + min
    }
  } catch (e) {}
  return date
}

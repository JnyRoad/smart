// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// 例子：
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
const dateFormat = function (date = new Date(), fmt = 'yyyy-MM-dd') {
  var o = {
    'M+': date.getMonth() + 1, // 月份
    'd+': date.getDate(), // 日
    'h+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    S: date.getMilliseconds() // 毫秒
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  for (var k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
    }
  }
  return fmt
}

/**
 * 小于十补位
 * @param {Object} num
 */
// const formatSmallNum = function (num) {
//   var newnum = parseInt(num)
//   if (newnum < 10) { // 调整日小于10时的格式
//     newnum = 0 + '' + newnum
//   }
//   return newnum
// }

/**
 * 根据天数进行计算时间
 * @param {Object} diff
 */
const dateCalculationDay = function (date, diff = 0, fmt = 'yyyy-MM-dd') {
  if (Object.prototype.toString.call(date) === '[object String]') {
    date = transformDate(date)
  }
  if (!date) {
    date = new Date()
  }
  const timeStamp = date.valueOf()
  const newDate = new Date(timeStamp + diff * 1000 * 60 * 60 * 24)
  return {
    date: newDate,
    value: dateFormat(newDate, fmt)
  }
}

/**
 * 获取当前月的天数
 * @param {Object} month 月份
 */
function getDates (month, year) {
  switch (month) {
    case 4:
    case 6:
    case 9:
    case 11:
      return 30
    case 2:
      return (year % 4 === 0) && (year % 100 !== 0 || year % 400 === 0) ? 29 : 28 // 判断闰年
    default:
      return 31
  }
}

/**
 * 转换2019-9-9 00:00:00 为 2019/9/9 00:00:00 兼容ios
 */
const transformDate = function (str) {
  if (!str) {
    return new Date()
  }
  return new Date(str.replace(/-/g, '/'))
}

export {
  transformDate,
  dateFormat,
  dateCalculationDay,
  getDates
}

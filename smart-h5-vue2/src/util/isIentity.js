/* eslint-disable */
/**
 * identity校验
 * @param {Object} identityId 号码
 */

const identity = function(identityId) {
  if (process.env.NODE_ENV === 'development') { // 开发环境或调试模式 不完全校验
    if (identityId.length === 18) {
      return true
    }
    return false
  }
  var format =
    /^(([1][1-5])|([2][1-3])|([3][1-7])|([4][1-6])|([5][0-4])|([6][1-5])|([7][1])|([8][1-2]))\d{4}(([1][9]\d{2})|([2]\d{3}))(([0][1-9])|([1][0-2]))(([0][1-9])|([1-2][0-9])|([3][0-1]))\d{3}[0-9xX]$/
  // 号码规则校验
  if (!format.test(identityId)) {
    return
    // return {
    //   'status': 0,
    //   'msg': '号码不合规'
    // };
  }
  // 区位码校验
  // 出生年月日校验  前正则限制起始年份为1900;
  var year = identityId.substr(6, 4) // 年
  var month = identityId.substr(10, 2) // 月
  var date = identityId.substr(12, 2) // 日
  var time = Date.parse(month + '-' + date + '-' + year) // 日期时间戳date
  var now_time = Date.parse(new Date()) // 当前时间戳
  var dates = (new Date(year, month, 0)).getDate() // 当月天数
  if (time > now_time || date > dates) {
    return
    // return {
    //   'status': 0,
    //   'msg': '出生日期不合规'
    // }
  }
  // 校验码判断
  var c = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2) // 系数
  var b = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2') // 校验码对照表
  var id_array = identityId.split('')
  var sum = 0
  for (var k = 0; k < 17; k++) {
    sum += parseInt(id_array[k]) * parseInt(c[k])
  }
  if (id_array[17].toUpperCase() !== b[sum % 11].toUpperCase()) {
    return
    // return {
    //   'status': 0,
    //   'msg': '校验码不合规'
    // }
  }
  return true
  // return {
  //   'status': 1,
  //   'msg': '校验通过'
  // }
}

/**
 * 根据正确的证件号提取相关信息
 * @param {Object} ic
 */
export const getInfo = function(ic) {
  let isic = identity(ic)
  const resultData = {
    birth: '',
    sex: '',
    age: ''
  }
  if (!isic) {
    return resultData
  }
  ic = ic + ''
  // 获取出生日期
  resultData.birth = ic.substring(6, 10) + '-' + ic.substring(10, 12) + '-' + ic.substring(12, 14)
  // 获取性别
  resultData.sex = ic.slice(14, 17) % 2 ? '男' : '女' // 1代表男性，2代表女性
  // 获取年龄
  let myDate = new Date()
  let month = myDate.getMonth() + 1
  let day = myDate.getDate()
  let age = myDate.getFullYear() - ic.substring(6, 10) - 1
  let userMonth = +ic.substring(10, 12)
  let userDay = +ic.substring(12, 14)
  if (userMonth < month || (userMonth === month && userDay <= day)) { // 生日已过加1
    age++
  }
  resultData.age = age
  return resultData
}

export default identity

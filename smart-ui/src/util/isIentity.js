/**
 * 身份证校验
 * @param {Object} identityId 身份证号码
 */
const identity = function (identityId) {
  var format =
    /^(([1][1-5])|([2][1-3])|([3][1-7])|([4][1-6])|([5][0-4])|([6][1-5])|([7][1])|([8][1-2]))\d{4}(([1][9]\d{2})|([2]\d{3}))(([0][1-9])|([1][0-2]))(([0][1-9])|([1-2][0-9])|([3][0-1]))\d{3}[0-9xX]$/
  // 号码规则校验
  if (!format.test(identityId)) {
    return
    // return {
    //   'status': 0,
    //   'msg': '身份证号码不合规'
    // };
  }
  // 区位码校验
  // 出生年月日校验  前正则限制起始年份为1900;
  var year = identityId.substr(6, 4) // 身份证年
  var month = identityId.substr(10, 2) // 身份证月
  var date = identityId.substr(12, 2) // 身份证日
  var time = Date.parse(month + '-' + date + '-' + year) // 身份证日期时间戳date
  var nowTime = Date.parse(new Date()) // 当前时间戳
  var dates = (new Date(year, month, 0)).getDate() // 身份证当月天数
  if (time > nowTime || date > dates) {
    return
    // return {
    //   'status': 0,
    //   'msg': '出生日期不合规'
    // }
  }
  // 校验码判断
  var c = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]// 系数
  var b = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'] // 校验码对照表
  var idArray = identityId.split('')
  var sum = 0
  for (var k = 0; k < 17; k++) {
    sum += parseInt(idArray[k]) * parseInt(c[k])
  }
  if (idArray[17].toUpperCase() !== b[sum % 11].toUpperCase()) {
    return
    // return {
    //   'status': 0,
    //   'msg': '身份证校验码不合规'
    // }
  }
  return true
  // return {
  //   'status': 1,
  //   'msg': '校验通过'
  // }
}

export default identity

import { validatenull } from '@/util/validate'
import { getLabel } from '@/util/util'
import { enumStaffStatus } from "@/const/crud/platform/enum";

function pluralize (time, label) {
  if (time === 1) {
    return time + label
  }
  return time + label + 's'
}

/**
 * 日期格式化
 */
export function dateFormat (date) {
  let format = 'yyyy-MM-dd hh:mm:ss'
  if (date != 'Invalid Date') {
    var o = {
      'M+': date.getMonth() + 1, // month
      'd+': date.getDate(), // day
      'h+': date.getHours(), // hour
      'm+': date.getMinutes(), // minute
      's+': date.getSeconds(), // second
      'q+': Math.floor((date.getMonth() + 3) / 3), // quarter
      'S': date.getMilliseconds() // millisecond
    }
    if (/(y+)/.test(format)) {
      format = format.replace(RegExp.$1,
        (date.getFullYear() + '').substr(4 - RegExp.$1.length))
    }
    for (var k in o) {
      if (new RegExp('(' + k + ')').test(format)) {
        format = format.replace(RegExp.$1,
          RegExp.$1.length == 1 ? o[k]
            : ('00' + o[k]).substr(('' + o[k]).length))
      }
    }
    return format
  }
  return ''
}
/**
 * 日期格式化2
 */
export function dateFormat2 (dateString) {
  let date;
  if(validatenull(dateString)){
    return '';
  }else{
    date = new Date(dateString);
  }

  let format = 'yyyy-MM-dd'
  if (date != 'Invalid Date') {
    var o = {
      'M+': date.getMonth() + 1, // month
      'd+': date.getDate(), // day
      'h+': date.getHours(), // hour
      'm+': date.getMinutes(), // minute
      's+': date.getSeconds(), // second
      'q+': Math.floor((date.getMonth() + 3) / 3), // quarter
      'S': date.getMilliseconds() // millisecond
    }
    if (/(y+)/.test(format)) {
      format = format.replace(RegExp.$1,
        (date.getFullYear() + '').substr(4 - RegExp.$1.length))
    }
    for (var k in o) {
      if (new RegExp('(' + k + ')').test(format)) {
        format = format.replace(RegExp.$1,
          RegExp.$1.length == 1 ? o[k]
            : ('00' + o[k]).substr(('' + o[k]).length))
      }
    }
    return format
  }
  return ''
}

export function timeAgo (time) {
  const between = Date.now() / 1000 - Number(time)
  if (between < 3600) {
    return pluralize(~~(between / 60), ' minute')
  } else if (between < 86400) {
    return pluralize(~~(between / 3600), ' hour')
  } else {
    return pluralize(~~(between / 86400), ' day')
  }
}

export function parseTime (time, cFormat) {
  if (arguments.length === 0) {
    return null
  }

  if ((time + '').length === 10) {
    time = +time * 1000
  }

  const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    date = new Date(parseInt(time))
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    if (key === 'a') return ['一', '二', '三', '四', '五', '六', '日'][value - 1]
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

export function formatTime (time, option) {
  time = +time * 1000
  const d = new Date(time)
  const now = Date.now()

  const diff = (now - d) / 1000

  if (diff < 30) {
    return '刚刚'
  } else if (diff < 3600) { // less 1 hour
    return Math.ceil(diff / 60) + '分钟前'
  } else if (diff < 3600 * 24) {
    return Math.ceil(diff / 3600) + '小时前'
  } else if (diff < 3600 * 24 * 2) {
    return '1天前'
  }
  if (option) {
    return parseTime(time, option)
  } else {
    return d.getMonth() + 1 + '月' + d.getDate() + '日' + d.getHours() + '时' + d.getMinutes() + '分'
  }
}

/* 数字 格式化 */
export function nFormatter (num, digits) {
  const si = [
    { value: 1E18, symbol: 'E' },
    { value: 1E15, symbol: 'P' },
    { value: 1E12, symbol: 'T' },
    { value: 1E9, symbol: 'G' },
    { value: 1E6, symbol: 'M' },
    { value: 1E3, symbol: 'k' }
  ]
  for (let i = 0; i < si.length; i++) {
    if (num >= si[i].value) {
      return (num / si[i].value + 0.1).toFixed(digits).replace(/\.0+$|(\.[0-9]*[1-9])0+$/, '$1') + si[i].symbol
    }
  }
  return num.toString()
}

export function html2Text (val) {
  const div = document.createElement('div')
  div.innerHTML = val
  return div.textContent || div.innerText
}

export function toThousandslsFilter (num) {
  return (+num || 0).toString().replace(/^-?\d+/g, m => m.replace(/(?=(?!\b)(\d{3})+$)/g, ','))
}

/**------------------------------------------------------------------------------------------------------------
 * 入园审批状态
 * 0-审批中
 * 1-通过
 * 2-未通过
 */
export function entryExamineFormat (status) {
  if(status==0){
    return '审批中'
  }else if(status==1){
    return '通过'
  }else if(status==2){
    return '未通过'
  }
}

/**
 * 入园审批状态,
 * 0-审批中-commonStatus1//sass for循环的i是非0数
 * 1-通过-commonStatus2
 * 2-未通过-commonStatus3
 */

export function entryExamineClassFormat (status) {
  return 'commonStatus'+ (Number(status)+1);
}
/**-----------------------------
 * 招聘状态
 * 0-招聘结束
 * 1-招聘中
 * 2-招聘暂停
 */

export function recruitStatusFormat (status) {
  if(status==0){
    return '招聘结束'
  }else if(status==1){
    return '招聘中'
  }else if(status==2){
    return '招聘暂停'
  }
}

/**
 * 招聘状态,
 * 0-招聘结束-recruitStatus1//sass for循环的i是非0数
 * 1-招聘中-recruitStatus2
 * 2-停止招聘-recruitStatus3
 */

export function recruitStatusClassFormat (status) {
  return 'recruitStatus'+ (Number(status)+1);
}
/**-----------------------------------------------------
 * 应聘状态
 * 0-已投递
 * 1-已邀请
 * 2-已拒绝
 * 3-已录取
 * 4-未录取
 * 5-已入职
 * 6-未入职
 * 7-待复试，暂定
 * 默认是0
 */
export function applyStatusFormat (status) {
  if(status==0){
    return '已投递'
  }else if(status==2){
    return '已邀请'
  }else if(status==1){
    return '已拒绝'
  }else if(status==3){
    return '待入职'
  }else if(status==4){
    return '待复试'
  }else if(status==5){
    return '已入职'
  }else if(status==6){
    return '已入库'
  }
}

/**
 * 应聘状态,
 * 0-已投递-applyStatus1//sass for循环的i是非0数
 * 1-已邀请-applyStatus2
 * 2-已拒绝-applyStatus3
 * 3-已录取-applyStatus4
 * 4-未录取-applyStatus5
 * 5-已入职-applyStatus6
 * 6-未入职-applyStatus7
 * 7-待复试-applyStatus8
 * 默认是0
 */

export function applyStatusClassFormat (status) {
  return 'applyStatus'+ (Number(status)+1);
}
/**
 * 访客预约状态
 * 0:审批通过
 * 1:已拒绝
 * 2:待审批
 * 3:已到达
 * 4:超时未到
 * 5:已离开
 * 6:预约超时
 */
export function visitorStatusFormat (status) {
  if(status==0){
    return '审批通过'
  }else if(status==1){
    return '已拒绝'
  }else if(status==2){
    return '待审批'
  }else if(status==3){
    return '已到达'
  }else if(status==4){
    return '超时未到'
  }else if(status==5){
    return '已离开'
  }else if(status==6){
    return '预约超时'
  }
}
/**
 * 访客预约状态
 * 0:审批通过 -visitorStatus1
 * 1:已拒绝-visitorStatus2
 * 2:待审批-visitorStatus3
 * 3:已到达-visitorStatus4
 * 4:超时未到-visitorStatus5
 * 5:已离开-visitorStatus6
 * 6:预约超时-visitorStatus7
 */
export function visitorStatusClassFormat (status) {
  return 'visitorStatus'+ (Number(status)+1);
}

/**
 * 设备接通状态
 * 1:未连接
 * 2:接通
 * 3:断开
 */
export function deviceStatusFormat (status) {
  if(status==0){
    return '未连接'
  }else if(status==2){
    return '接通'
  }else if(status==1){
    return '断开'
  }
}
/**
 * 设备接通状态
 * 1:未连接 - deviceStatus1
 * 2:接通 - deviceStatus2
 * 3:断开 - deviceStatus3
 */
export function deviceStatusClassFormat (status) {
  return 'deviceStatus'+ status;
}
/**
 * 性别显示
 * 0:男
 * 1:女
 * 2:未知
 */
export function genderInit (gender) {
  const typeOption = {
    0: '男',
    1: '女',
    2: '未知'
  }
  return typeOption[gender]
}
/**
 * 房间性别显示
 * 0:男
 * 1:女
 * 2:夫妻/家属
 * 2:其他
 */
export function roomGenderInit (gender) {
  const typeOption = {
    0: '男',
    1: '女',
    2: '夫妻/家属',
    3: '其他'
  }
  return typeOption[gender]
}
/**
 * 员工状态
 * 0:已离职
 * 1:在职
 * 2:试用
 * 3:实习
 * 4:临时人员
 * -1:未知
 */
export function staffStatusInit (status) {
  return getLabel(enumStaffStatus, status)
}


/**
 * 工资签收状态
 * 0:未签收
 * 1:已签收
 */
export function salaryStatusInit (status) {
  if(status===0){
    return '未签收'
  }else if(status===1){
    return '已签收'
  }
}
//将日期格式化成 yyyy.MM.dd
export function parserDate (date) {
  let format = 'yyyy.MM.dd'
  if (date != 'Invalid Date') {
    var o = {
      'M+': date.getMonth() + 1, // month
      'd+': date.getDate(), // day
      'h+': date.getHours(), // hour
      'm+': date.getMinutes(), // minute
      's+': date.getSeconds(), // second
      'q+': Math.floor((date.getMonth() + 3) / 3), // quarter
      'S': date.getMilliseconds() // millisecond
    }
    if (/(y+)/.test(format)) {
      format = format.replace(RegExp.$1,
        (date.getFullYear() + '').substr(4 - RegExp.$1.length))
    }
    for (var k in o) {
      if (new RegExp('(' + k + ')').test(format)) {
        format = format.replace(RegExp.$1,
          RegExp.$1.length == 1 ? o[k]
            : ('00' + o[k]).substr(('' + o[k]).length))
      }
    }
    return format
  }
  return ''
};


export function formatNumber(num) {
  if(validatenull(num))
  return
  let reg = /(?=(\B)(\d{3})+$)/g;
  return num.toString().replace(reg, ",");
};

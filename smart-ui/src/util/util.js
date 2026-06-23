import {validatenull} from './validate'

// util.js 已按领域拆分为独立模块，这里统一 re-export，保证 @/util/util 对外公共面与调用方完全不变。
export { serialize, getObjType, deepClone, diff, isObjectValueEqual, isArrayFn } from './object'
export { randomLenNum, formatNumber, getProportion, floatNumMinus } from './number'
export { toggleGrayMode, setTheme } from './theme'
export { encryption } from './crypto'
export { fullscreenToggel, listenfullscreen, fullscreenEnable, reqFullScreen, exitFullScreen } from './fullscreen'
export { loadStyle, openWindow, handleImg } from './dom'

/**
 * 递归寻找子类的父类
 */

export const findParent = (menu, id) => {
  for (let i = 0; i < menu.length; i++) {
    if (menu[i].children.length != 0) {
      for (let j = 0; j < menu[i].children.length; j++) {
        if (menu[i].children[j].id == id) {
          return menu[i]
        } else {
          if (menu[i].children[j].children.length != 0) {
            return findParent(menu[i].children[j].children, id)
          }
        }
      }
    }
  }
}

/**
 * 根据字典的value显示label
 */
export const findByvalue = (dic, value) => {
  let result = ''
  if (validatenull(dic)) return value
  if (typeof (value) === 'string' || typeof (value) === 'number' || typeof (value) === 'boolean') {
    let index = 0
    index = findArray(dic, value)
    if (index != -1) {
      result = dic[index].label
    } else {
      result = value
    }
  } else if (value instanceof Array) {
    result = []
    let index = 0
    value.forEach(ele => {
      index = findArray(dic, ele)
      if (index != -1) {
        result.push(dic[index].label)
      } else {
        result.push(value)
      }
    })
    result = result.toString()
  }
  return result
}
/**
 * 根据字典的value查找对应的index
 */
export const findArray = (dic, value) => {
  for (let i = 0; i < dic.length; i++) {
    if (dic[i].value == value) {
      return i
    }
  }
  return -1
}

export function dateFormat(date = new Date(), fmt = 'yyyy-MM-dd') {
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

//获取 月份
export function getDateMonth(){
  const date = new Date();
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  month = month > 9 ? month : '0' + month;
  return `${year}-${month}`;
}

//获取上月月份
export function getDatePreMonth(){
  const date = new Date();
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  if(month==1){
    year = date.getFullYear() - 1;
    month = 12;
  }else{
    month = month -1;
  }
  month = month > 9 ? month : '0' + month;
  return `${year}-${month}`;
}

//获取今日
export function getDatePreDay(){
  const date = new Date();
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();
  month = month > 9 ? month : '0' + month;
  return `${year}-${month}-${day}`;
}


/**
 * 根据value 获取label值
 */
export function getLabel(arr,value) {
  if(arr&&arr.length>0){
    let obj = arr.find(item => {
      if (item.value === value) {
        return item
      }
    })
    if(obj){
      return obj.label
    }
  }
};

// 从 util.js 拆出的对象/数组/类型判断类纯函数（无外部依赖）。
// 由 util.js re-export，保证 @/util/util 对外路径与公共面不变。

// 表单序列化
export const serialize = data => {
  let list = []
  Object.keys(data).forEach(ele => {
    list.push(`${ele}=${data[ele]}`)
  })
  return list.join('&')
}
export const getObjType = obj => {
  var toString = Object.prototype.toString
  var map = {
    '[object Boolean]': 'boolean',
    '[object Number]': 'number',
    '[object String]': 'string',
    '[object Function]': 'function',
    '[object Array]': 'array',
    '[object Date]': 'date',
    '[object RegExp]': 'regExp',
    '[object Undefined]': 'undefined',
    '[object Null]': 'null',
    '[object Object]': 'object'
  }
  if (obj instanceof Element) {
    return 'element'
  }
  return map[toString.call(obj)]
}
/**
 * 对象深拷贝
 */
export const deepClone = data => {
  var type = getObjType(data)
  var obj
  if (type === 'array') {
    obj = []
  } else if (type === 'object') {
    obj = {}
  } else {
    // 不再具有下一层次
    return data
  }
  if (type === 'array') {
    for (var i = 0, len = data.length; i < len; i++) {
      obj.push(deepClone(data[i]))
    }
  } else if (type === 'object') {
    for (var key in data) {
      obj[key] = deepClone(data[key])
    }
  }
  return obj
}
/**
 * 判断两个路由 tag 是否相等（递归深比较）。
 *
 * close 是 tag 的可关闭标记（UI 派生字段，由 setFistTag 写入），不参与
 * “是否同一路由”的身份判断，比较时两侧一律忽略。
 *
 * 注意：本函数不修改入参——通过过滤键集合而非 delete 原对象，避免污染
 * 调用方（如 vuex 的 tagList）；深比较要求所有子属性都相等才返回 true。
 */
export const diff = (obj1, obj2) => {
  const isObj1 = obj1 instanceof Object
  const isObj2 = obj2 instanceof Object
  if (!isObj1 || !isObj2) {
    // 任一侧不是对象时，按原始值全等比较
    return obj1 === obj2
  }

  // 忽略 close 字段后比较自身可枚举键的数量
  const keys1 = Object.keys(obj1).filter(key => key !== 'close')
  const keys2 = Object.keys(obj2).filter(key => key !== 'close')
  if (keys1.length !== keys2.length) {
    return false
  }

  // 任一子属性不等即 false，全部相等才 true；
  // obj2 必须含有该键，避免「键数相等但键名不同」时（如值同为 undefined）漏判
  return keys1.every(
    attr => Object.prototype.hasOwnProperty.call(obj2, attr) && diff(obj1[attr], obj2[attr])
  )
}
/**
 * 判断路由是否相等
 */
export const isObjectValueEqual = (objA, objB) => {
  let result = true
  Object.keys(objA).forEach(ele => {
    const type = typeof (objA[ele])
    if (type === 'string' && objA[ele] !== objB[ele]) result = false
    else if (type === 'object' && JSON.stringify(objA[ele]) !== JSON.stringify(objB[ele])) result = false
  })
  return result
}
// 判断是否为数组
export function isArrayFn(value){
  if (typeof Array.isArray === "function") {
    return Array.isArray(value);
  }else{
    return Object.prototype.toString.call(value) === "[object Array]";
  }
}

import { isEmail, isMobile, isFilterPassword } from './validate'
const { isIentity } = './isIentity'

/**
 * 非空验证
 * @param {*} param0
 */
export const vempty = function ({ trigger = 'blur', message = '请填写内容' } = {}) {
  return {
    message: message,
    trigger: trigger,
    required: true
  }
}

/**
 * 验证身份证号
 * @param {*} param0
 */
export const visIentity = function ({ trigger = 'blur', message = '身份证号格式不正确' } = {}) {
  return {
    message: message,
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      if (!isIentity(value)) {
        callback(new Error())
      }
      callback()
    }
  }
}

/**
 * 验证邮箱
 * @param {*} param0
 */
export const vemail = function ({ trigger = 'blur', message = '邮箱格式不正确' } = {}) {
  return {
    message: message,
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      if (!isEmail(value)) {
        callback(new Error())
      }
      callback()
    }
  }
}

/**
 * 手机号
 * @param {*} param0
 */
export const vmobile = function ({ trigger = 'blur', message = '手机格式不正确' } = {}) {
  return {
    message: message,
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      if (!isMobile(value)) {
        callback(new Error())
      }
      callback()
    }
  }
}

/**
 * 密码校验
 * @param {*} param0
 */
export const vpassword = function ({ trigger = 'blur', message = '密码不得包含\'&=\'字符' } = {}) {
  return {
    message: message,
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      if (!isFilterPassword(value)) {
        callback(new Error())
      }
      callback()
    }
  }
}

/**
 * 是否数字
 * @param {*} param0
 */
export const vnumber = function ({ trigger = 'blur', message = '请输入数字' } = {}) {
  return {
    message: message,
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      if (!/\d+/.test(value)) {
        callback(new Error())
      }
      callback()
    }
  }
}

/**
 * 控制数字范围
 * @param {*} param0
 */
export const vlimitNum = function ({ trigger = 'blur', message, min = 1, max = 999999 } = {}) {
  return {
    type: 'number',
    message: message || (`请将范围控制在 ${min} 到 ${max}`),
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      if (value < min || value > max) {
        callback(new Error())
      }
      callback()
    }
  }
}

/**
 * 数组长度
 * @param {*} param0
 */
export const vlimitArray = function ({ trigger = 'blur', message, min = 1, max = 999999 } = {}) {
  return {
    type: 'array',
    message: message || (`请将范围控制在 ${min} 到 ${max}`),
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      let len = 0
      if (value) {
        len = value.length
      }
      if (len < min || len > max) {
        callback(new Error())
      }
      callback()
    }
  }
}

/**
 * 数组对象是否有值
 * @param {*} param0
 */
export const vlimitArrayObj = function ({ trigger = 'blur', message, min = 1, max = 999999 } = {}) {
  return {
    type: 'array',
    message: message || (`请将范围控制在 ${min} 到 ${max}`),
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      // if (value) {
      //   len = value.length
      // }
      // console.log('len', value)
      value.map(item => {
        if (!item.value) {
          callback(new Error())
        }
      })
      callback()
    }
  }
}

/**
 * 控制字符串长度
 * @param {*} param0
 */
export const vlimitString = function ({ trigger = 'blur', message = '', min = 1, max = 20 } = {}) {
  return {
    type: 'string', min: min, max: max, message: message || (`长度在 ${min} 到 ${max} 个字符`), trigger: trigger
  }
}
/**
 * 校验密码包含字母和数字和长度不能小于8位
 * @param {*} param0
 */
export const vlimitPwd = function ({ trigger = 'blur', message } = {}) {
  return {
    type: 'string',
    message: message,
    trigger: trigger,
    validator (rule, value, callback, source, options) {
      const regNumber = /\d+/ // 验证0-9的任意数字最少出现1次。
      const regString = /[a-zA-Z]+/ // 验证大小写26个字母任意字母最少出现1次。
      if (value === '') {
        return callback(new Error('请输入密码')) // 这里的代表验证不通过
      }
      if (value.length < 8) {
        return callback(new Error('密码长度不能小于8位')) // 这里的代表验证不通过
      }
      if (regNumber.test(value) && regString.test(value)) {
        return callback() // 这里的代表验证通过
      } else {
        return callback(new Error('密码必须包含字母或者数字')) // 这里的代表验证不通过
      }
    }
  }
}

export default {
  vempty, visIentity, vemail, vpassword, vmobile, vnumber, vlimitNum, vlimitArray, vlimitString, vlimitPwd
}

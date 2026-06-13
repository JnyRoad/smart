import axios from 'axios'
import {
  BASEURL,
  PROXYS
} from '@/conf'
import store from '@/store'

const requestTasks = {}

// 全局授权header
const authorizationHeader = {
  Authorization: `Bearer `
}
const interceptors = {
  response: null,
  request: null
}

/**
 * 构建 request
 */
const request = function (config, option = {}) {
  if (option.requestTask && requestTasks[option.requestTask]) {
    requestTasks[option.requestTask].abort()
    requestTasks[option.requestTask] = null
  }
  let interceptorsConfig = config
  if (interceptors.request) {
    const c = interceptors.request(config)
    if (!c) {
      throw new Error('interceptors.request 处理失败')
    }
    interceptorsConfig = c
  }
  const {
    url,
    params = {},
    data = {},
    type = 'GET',
    module = undefined
  } = interceptorsConfig

  if (module !== undefined && module === '') {
    throw new Error(`${url.split('//')[1]},请设置module参数`)
  }
  if (!PROXYS[module]) {
    throw new Error('module在PROXYS不存在')
  }
  option.module = PROXYS[module]
  return new Promise((resolve, reject) => {
    goRequest(url, resolve, reject, data, params, type, option)
  })
}

/**
 * 写入TOKEN
 * @param {Object} data
 */
const refreshAuthorizationHeader = function (data, isTrue) {
  let token
  if (data) {
    token = data
  } else {
    // TODO: 通过缓存获取token
    token = store.getters.access_token
  }
  if (token) {
    if (isTrue) {
      authorizationHeader.Authorization = `Basic ${token}`
    } else {
      authorizationHeader.Authorization = `Bearer ${token}`
    }
  }
}

const setHttpInterceptors = function ({
  response = null,
  request = null
}) {
  Object.assign(interceptors, {
    response,
    request
  })
}

refreshAuthorizationHeader()

/**
 * uni.request 封装
 * method 不区分大小写
 * option.header 自定义
 */
const goRequest = function (url, resolve, reject, data = {}, params = {}, method = 'GET', option = {}) {
  let sendParams = {}
  const sendData = data
  let headers = Object.assign({}, {
    'content-type': 'application/json', // 默认值
    'X-Requested-With': 'XMLHttpRequest'
  }, option.header)

  if (headers['content-type'] === 'application/x-www-form-urlencoded') {
    sendParams = params
  } else {
    sendParams = {
      // _t: new Date().getTime(),
      ...params
    }
  }

  // 默认所有请求添加authorization
  if (option.authorization !== false) {
    headers = Object.assign({}, headers, authorizationHeader)
    // console.log('aythor----',authorizationHeader)
    if (headers.Authorization.length < 10) {
      this.$tceMobile.toast('授权失败')
      throw new Error('Authorization 授权失败')
    }
  }

  if (option.module) { // 基础平台定制代码，根据指定的模块调用api
    const newUrl = url.replace(BASEURL, '')
    url = `${BASEURL}/${PROXYS[option.module]}${newUrl}`
  }

  // 对分页post请求进行处理
  if (method.toUpperCase() === 'POST' && data.size && data.current) {
    if (url.indexOf('?') > 0) {
      url = `${url}&current=${data.current}&size=${data.size}`
    } else {
      url = `${url}?current=${data.current}&size=${data.size}`
    }
  }

  if (method.toUpperCase() === 'GET') {
    Object.assign(sendParams, data)
  }

  const axiosInstance = axios.create({
    baseURL: `${BASEURL}`,
    timeout: option.timeout || 60000 * 3,
    headers: headers,
    validateStatus: function (status) {
      return true // default
    }
  })

  // if (interceptors.request) {
  //   axiosInstance.interceptors.request.use(function (config) {
  //     return interceptors.request(config)
  //   }, function (error) {
  //     return Promise.reject(error)
  //   })
  // }

  if (interceptors.response) {
    axiosInstance.interceptors.response.use(function (res) {
      return interceptors.response(res, {
        option: option,
        isLog: option.isLog
      })
    }, function (error) {
      return Promise.reject(error)
    })
  }

  const requestTask = axiosInstance({
    url: url,
    params: sendParams,
    data: sendData,
    method: method.toUpperCase()
  }).then((res) => {
    // ! 强制替换
    if (res.data) {
      const dataString = JSON.stringify(res.data).replace(/&#40;/g, '(').replace(/&#41;/g, ')').replace(/&#39;/g, '\'').replace(/&lt;/g, '<').replace(/&gt;/g, '>')
      res.data = JSON.parse(dataString)
    }
    const originalStatusCode = res.status
    res.statusCode = originalStatusCode
    if (option.interceptors) {
      res = option.interceptors(res, {
        originalStatusCode: originalStatusCode,
        isErrorAuthorizationCollect: option.isErrorAuthorizationCollect,
        url: url,
        data: data
      })
    }
    resolve(res)
  }, (err) => {
    reject(err)
  })

  if (option.requestTask) {
    requestTasks[option.requestTask] = requestTask
  }
}

export {
  authorizationHeader
}
export default {
  authorizationHeader,
  request,
  setHttpInterceptors,
  refreshAuthorizationHeader
}

const DEV_ENV = 'development'
const DEBUG = process.env.NODE_ENV === DEV_ENV
const APPNAME = 'SMART_WEB_APP' // 程序名称

// serve 服务ip地址或域名
const BASEURL = ''

// 查看图片地址
// /image/get/base64/code/{imageCode} 返回base64
// /image/view/{imageId} 返回二进制
const GET_IMAGE_URL = `/platform/image/view`

const WS = 'ws://192.168.8.105:8700/endpoint'

const APPIP = `wx5c0d26056102d41e`
// 线上5000021 测试10322
const PARKID = 5000021 //
// const PARKID = 10322

/**
 * api 代理配置
 */
const PROXYS = {
  platform: 'platform', // 管理台模块
  app: 'app', // 移动端部分模块
  auth: 'auth',
  algorithm: 'algorithm'
}

/**
 * 全局应用缓存KEY
 */
const ENUMSTORE = {
  TOKEN: APPNAME + '_TOKEN', // token
  TOKEN_TIME: APPNAME + '_TIME', //  TOKEN_TIME.current当前时间; TOKEN_TIME.expire token过期时间;
  REFRESH_TOKEN: APPNAME + '_REFRESH_TOKEN', // 刷新token
  USER_INFO: APPNAME + '_USER_INFO', // 用户信息
  PARKINFO: APPNAME + '_PARKINFO', // 园区信息
  ACTIVE_PARK: APPNAME + '_ACTIVE_PARK' // 用户当前绑定的企业(+园区)
}

export {
  APPNAME,
  DEBUG,
  BASEURL,
  GET_IMAGE_URL,
  PROXYS,
  ENUMSTORE,
  WS,
  APPIP,
  PARKID
}

export default {
  APPNAME,
  DEBUG,
  BASEURL,
  GET_IMAGE_URL,
  PROXYS,
  ENUMSTORE,
  WS,
  APPIP,
  PARKID
}

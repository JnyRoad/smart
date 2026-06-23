// util.js 已彻底按领域拆分为独立模块，本文件仅作兼容入口统一 re-export，
// 保证 @/util/util 对外公共面与调用方完全不变。
// 注意：加载本模块仍会经 ./dom 间接触发 @/router/axios 的副作用导入（契约测试已覆盖）。
export { serialize, getObjType, deepClone, diff, isObjectValueEqual, isArrayFn } from './object'
export { randomLenNum, formatNumber, getProportion, floatNumMinus } from './number'
export { toggleGrayMode, setTheme } from './theme'
export { encryption } from './crypto'
export { fullscreenToggel, listenfullscreen, fullscreenEnable, reqFullScreen, exitFullScreen } from './fullscreen'
export { loadStyle, openWindow, handleImg } from './dom'
export { dateFormat, getDateMonth, getDatePreMonth, getDatePreDay } from './datetime'
export { findParent, findByvalue, findArray, getLabel } from './dict'

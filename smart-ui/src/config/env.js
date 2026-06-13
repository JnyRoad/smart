// 配置编译环境和线上环境之间的切换

const env = process.env;
// 图表库为avue套地址
let iconfontVersion = ['567566_qo5lxgtishg', '667895_vf6hgm08ubf'] //系统默认的
let iconfontUrl = `//at.alicdn.com/t/font_$key.css`;
let codeUrl = `${window.location.origin}/code`;
let actUrl = `${window.location.origin}/act/modeler.html?modelId=`;
export {
  actUrl,
  iconfontUrl,
  iconfontVersion,
  codeUrl,
  env
}

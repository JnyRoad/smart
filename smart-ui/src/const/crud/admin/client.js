
import { baseTableOption } from '../_base'

const DIC = {
  vaild: [{
    label: '否',
    value: 'false'
  }, {
    label: '是',
    value: 'true'
  }]
}
/**
 * 根据后端返回的权威 capability scope 目录创建表单配置。
 *
 * @param {Array<{value: string, label: string, deprecated?: boolean}>} scopeOptions 授权域选项
 * @returns {object} Avue 客户端管理表格配置
 */
export function createTableOption (scopeOptions = []) {
  return {
  ...baseTableOption,
  indexLabel: '序号',
  viewBtn: true,
  column: [{
    width: 150,
    label: 'App ID',
    prop: 'clientId',
    align: 'center',
    sortable: true,
    rules: [{
      required: true,
      message: '请输入 App ID',
      trigger: 'blur'
    }]
  }, {
    label: 'App Secret',
    prop: 'clientSecret',
    align: 'center',
    sortable: true,
    overHidden: true,
    width: 120,
    rules: [{
      required: true,
      message: '请输入 App Secret',
      trigger: 'blur'
    }]
  }, {
    // scope 落库仍是逗号分隔字符串（后端 SysOauthClientDetails.scope 为 String），
    // 这里用多选下拉采集，字符串<->数组的转换在 index.vue 的 before-open / 保存回调里做。
    label: '授权域',
    prop: 'scope',
    align: 'center',
    type: 'select',
    multiple: true,
    dicData: scopeOptions,
    overHidden: true,
    rules: [{
      required: true,
      message: '请选择授权域',
      trigger: 'change'
    }]
  }, {
    label: '授权模式',
    prop: 'authorizedGrantTypes',
    align: 'center',
    overHidden: true,
    rules: [{
      required: true,
      message: '请输入授权模式',
      trigger: 'blur'
    }]
  }, {
    label: '回调地址',
    prop: 'webServerRedirectUri',
    align: 'center',
    hide: true
  }, {
    label: '权限',
    prop: 'authorities',
    align: 'center',
    hide: true
  }, {
    label: '自动放行',
    prop: 'autoapprove',
    align: 'center',
    type: 'radio',
    dicData: DIC.vaild,
    rules: [{
      required: true,
      message: '请选择是否放行',
      trigger: 'blur'
    }]
  }, {
    label: '令牌时效',
    prop: 'accessTokenValidity',
    align: 'center',
  }, {
    label: '刷新时效',
    prop: 'refreshTokenValidity',
    align: 'center',
  }, {
    // 授权园区不是数据库原生字段，落库时会被序列化进 additionalInformation 的
    // allowedParkIds 键；列表/表单都用自定义 slot（见 index.vue 的
    // slot="allowedParkIdsForm"）渲染多选框，这里只登记字段位置和校验。
    label: '授权园区',
    prop: 'allowedParkIds',
    align: 'center',
    hide: true,
    formsolt: true,
    span: 24
  }, {
    label: '扩展信息',
    prop: 'additionalInformation',
    align: 'center',
    hide: true,
    editDisplay: false,
    addDisplay: false
  }, {
    label: '资源ID',
    prop: 'resourceIds',
    align: 'center',
    hide: true
  }]
  }
}

// 页面首次渲染前尚未请求目录时保持空选项；弹窗打开前会替换为后端实时目录。
export const tableOption = createTableOption()

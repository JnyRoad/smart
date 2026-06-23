import { baseTableOption } from '../_base'

export const tableOption = {
  ...baseTableOption,
  indexLabel: '序号',
  menuWidth: 150,
  showClomnuBtn: false,
  searchSize: 'mini',
  viewBtn: true,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [{
    label: '类型',
    prop: 'type',
    type: 'select',
    dicUrl: '/admin/dict/type/log_type',
    search: true
  }, {
    label: '标题',
    prop: 'title'
  }, {
    label: 'IP地址',
    prop: 'remoteAddr'
  }, {
    label: '请求方式',
    prop: 'method'
  }, {
    label: '客户端',
    prop: 'serviceId'
  }, {
    width: 80,
    label: '请求时间',
    prop: 'time'
  }, {
    width: 150,
    label: '创建时间',
    prop: 'createTime',
    type: 'datetime',
    format: 'yyyy-MM-dd HH:mm',
    valueFormat: 'yyyy-MM-dd HH:mm:ss'
  }]
}

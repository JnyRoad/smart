/**
 * 组织管理列表配置
 * @author yang.chuan <yang.chuan@bjtce.com>
 * @date 2020-10-09
 */

export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '组织名称',
      prop: 'compName'
    },
    {
      label: '管理员用户名',
      prop: 'userName'
    },
    {
      label: '从属园区',
      prop: 'parkName'
    },
    {
      label: '企业类型',
      prop: 'compType',
      solt: true
    }
  ]
}

/**
 * 人员管理列表配置
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
      label: '工号',
      prop: 'badge'
    },
    {
      label: '姓名',
      prop: 'name'
    },
    {
      label: '职层',
      prop: 'jcheName'
    },
    {
      label: '岗位',
      prop: 'jobName'
    },
    {
      label: '状态',
      prop: 'status',
      solt: true
    },
    {
      label: '人脸照片',
      prop: 'faceImgUrl',
      solt: true
    }
  ]
}

export const tableOptionBatchDel = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menu: false,
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
  selection: true,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '工号',
      prop: 'badge'
    },
    {
      label: '姓名',
      prop: 'name'
    }
  ]
}

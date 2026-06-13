export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 245,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: true,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth: '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: 'id',
      prop: 'id',
      hide: true
    },
    {
      label: '园区',
      prop: 'parkName'
    },
    {
      label: '工号',
      prop: 'staffBadge',
    }, {
      label: '姓名',
      prop: 'staffName'
    },
    {
      label: 'BU',
      prop: 'compName',
      solt: true
    },
    {
      label: '房间喜好',
      prop: 'likeTypeDesc'
    },
    {
      label: '备注信息',
      prop: 'applyRemark'
    },
    {
      label: '状态',
      prop: 'status',
      solt: true
    },
    {
      label: '申请时间',
      prop: 'applyDate',
      width: 110
    }
  ]
}
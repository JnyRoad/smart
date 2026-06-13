export const tableOption = {
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  align: 'center',
  editBtn: false,
  delBtn: false,
  addBtn: false,
  column: [
	  {
      label: '主键',
      prop: 'id',
      hide: true
    },
	  {
      label: '员工工号',
      prop: 'badge'
    },
	  {
      label: '员工姓名',
      prop: 'name'
    },
	  {
      label: 'BU',
      prop: 'compName'
    },
	  {
      label: '部门',
      prop: 'depName'
    },
	  {
      label: '所属园区',
      prop: 'parkName'
    },
    {
      label: '申请原因',
      prop: 'reason'
    },
	  {
      label: '申请时间',
      prop: 'createTime'
    },
	  {
      label: '办理状态',
      prop: 'stateDesc'
    },
    {
      label: '办理状态code',
      prop: 'state',
      hide: true
    },
	  {
      label: '领取地址',
      prop: 'address',
      hide: true
    },
	  {
      label: '备注',
      prop: 'remark',
      hide: true
    }
  ]
}

export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 120,
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
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label:'id',
      prop:'id',
      hide:true

    },
    {
      label: '所属园区',
      prop: 'parkName'
    },
    {
      label: '姓名',
      prop: 'personName'
    },
	  {
      label: '身份证号',
      prop: 'cardNo',
      width: 200
    },
    {
      label: '创建人',
      prop: 'createUser'
    },
    {
      label: '原因',
      prop: 'reason'
    },
	  {
      label: '创建时间',
      prop: 'createTime',
      width: 170
    }
  ]
}
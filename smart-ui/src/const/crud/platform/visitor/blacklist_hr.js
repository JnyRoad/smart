export const tableHrOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menu: false,
  menuAlign: 'center',
  menuWidth: 280,
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
      label: '姓名',
      prop: 'name'
    },
	  {
      label: '身份证号',
      prop: 'certno'
    },
	  {
      label: '创建时间',
      prop: 'alterTime'
    }
  ]
}
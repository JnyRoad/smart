export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  menuWidth: 250,
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
      label: '所在园区',
      prop: 'parkName'

    },
    {
      label: '公司名称',
      prop: 'name',
      type: 'input'
    },
    {
      label: '法人',
      prop: 'legalPerson'

    },
	  {
      label: '备注',
      prop: 'remark'
    }
    ,
	  {
      label: '园区id',
      prop: 'parkId',
      hide:true
    }

    ,
	  {
      label: '税务识别号',
      prop: 'numbers',
      hide:true
    }

  ]
}
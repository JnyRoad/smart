export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 100,
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
  column: [
	  {
      label: '姓名',
      prop: 'name'
    },
    {
      label: '性别',
      prop: 'sex',
      type: 'select',
      width: '50px',
      dicData:[{
        label:'男',
        value: 0
      },
      {
        label:'女',
        value: 1
      },
      {
        label:'未知',
        value:2
      }]
    },
    {
      label: '民族',
      prop: 'nation'
    },
    {
      label: '证件号',
      prop: 'certno'
    },
	  {
      label: '出生日期',
      prop: 'birth',
      width: '120px',
    },
    {
      label: '户籍地址',
      prop: 'homeAddress'
    },
    {
      label: '签发机关',
      prop: 'police'
    },
    {
      label: '证件有效期（起）',
      prop: 'validDateFm',
      width: '140px'
    },
    {
      label: '证件有效期（止）',
      prop: 'validDate',
      width: '140px'
    }
  ]
}
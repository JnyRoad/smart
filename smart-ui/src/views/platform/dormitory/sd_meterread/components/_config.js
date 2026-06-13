export const tableOption = {
  border: false,
  index: false,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menu: false,
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
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '修改项',
      prop: 'categoryId',
      solt: true
    },
    // {
    //   label: '重置月份',
    //   prop: 'meterMonth',
    // },
	  {
      label: '修改前',
      prop: 'preMonthNum',
      solt: true
    },
    {
      label: '修改后',
      prop: 'revPreMonthNum',
    },
    {
      label: '修改人',
      prop: 'meterUser',
      type: 'input'
    },
    {
      label: '修原因',
      prop: 'remark',
      type: 'input'
    },
	  {
      label: '修改时间',
      prop: 'createTime',
      width: 180
    }
  ]
}
export const tableOptionDay = {
  border: false,
  index: false,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menu: false,
  menuAlign: 'center',
  menuWidth: 220,
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
      label: '人员',
      prop: 'staffName',
      solt: true
    },
    // {
    //   label: '重置月份',
    //   prop: 'meterMonth',
    // },
	  {
      label: '修改前',
      prop: 'oldDays',
    },
    {
      label: '修改后',
      prop: 'newDays',
    },
    {
      label: '修改人',
      prop: 'meterName',
    },
    {
      label: '修改原因',
      prop: 'remark',
    },
	  {
      label: '修改时间',
      prop: 'createTime',
      width: 180
    }
  ]
}

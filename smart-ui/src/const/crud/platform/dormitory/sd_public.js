export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 180,
    labelWidth: 150,
    align: 'center',
    refreshBtn: false,
    columnBtn: false,
    searchBtn: false,
    showClomnuBtn: false,
    searchSize: 'mini',
    dialogWidth : '600px',
    addBtn: false,
    editBtn: false,
    delBtn: false,
    viewBtn: false,
    column: [
    {
      label: '月份',
      prop: 'meterMonth'
    },{
      label: '上月表盘读数',
      prop: 'preMonthNum',
      solt: true
    },{
      label: '当月表盘读数',
      prop: 'curMonthNum',
      solt: true
    },
    {
      label: '月度用量',
      prop: 'perMonth',
      solt: true
    },{
      label: '公摊人均用量',
      prop: 'avgNum'
    }]
  }
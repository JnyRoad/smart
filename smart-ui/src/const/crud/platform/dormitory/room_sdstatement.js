export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 120,
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
      label: '园区',
      prop: 'parkName'
    },{
      label: '楼栋',
      prop: 'dormitoryName'
    },
    {
      label: '楼层',
      prop: 'floorName'
    },{
      label: '房间号',
      prop: 'roomName'
    },{
      label: '抄表月份',
      prop: 'meterMonth'
    },
    {
      label: '状态',
      prop: 'statementStatus',
      solt: true
    }]

  }
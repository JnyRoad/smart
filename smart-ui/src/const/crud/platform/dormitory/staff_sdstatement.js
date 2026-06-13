export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 120,
    menu:false,
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
      label: '工号',
      prop: 'badge'
    },{
      label: '姓名',
      prop: 'name'
    },
    {
      label: 'BU',
      prop: 'compName'
    },{
      label: '部门',
      prop: 'depName'
    },{
      label: '入住日期',
      prop: 'inTime',
      solt: true
    },{
      label: '房号',
      prop: 'roomName'
    },{
      label: '住宿天',
      prop: 'inDays'
    },{
      label: '日平均金额',
      prop: 'avgFee',
      width: 100
    },{
      label: '个人扣款',
      prop: 'fee'
    },
    {
      label: '扣款月份',
      prop: 'meterMonth'
    },
    {
      label: '扣款来源园区',
      prop: 'parkName'
    }]
  }
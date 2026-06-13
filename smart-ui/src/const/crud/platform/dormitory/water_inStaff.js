export const tableOption = {
    border: false,
    index: false,
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
      label: '房间号',
      prop: 'roomName',
      width: 100
    },
    {
      label: '工号',
      prop: 'badge'
    },{
      label: '姓名',
      prop: 'name'
    },{
      label: '员工状态',
      prop: 'status',
      solt: true
    },{
      label: '住房数量',
      prop: 'inRoomNum'
    },
    {
      label: 'BU',
      prop: 'compName'
    },
    {
      label: '中心',
      prop: 'depAbbr'
    },{
      label: '部门',
      prop: 'depName'
    },{
      label: '入职日期',
      prop: 'createTime',
      solt: true,
      width: 100
    },{
      label: '入住日期',
      prop: 'inTime',
      solt: true,
      width: 100
    },{
      label: '退宿日期',
      prop: 'outTime',
      solt: true,
      width: 100
    },{
      label: '结算天数',
      prop: 'inDays'
    },{
      label: '标记天数',
      prop: 'remarkDays'
    },{
      label: '住宿天数',
      prop: 'countDays'
    },{
      label: '日平均金额',
      prop: 'avgFee',
      width: 100
    },{
      label: '水电超标金额',
      prop: 'fee',
      width: 115
    },
    {
      label: '补贴',
      prop: 'asAllowance',
      solt: true,
    },
    {
      label: '扣款月份',
      prop: 'meterMonth',
      solt: true,
      width: 100
    }]
  }

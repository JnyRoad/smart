export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
  labelWidth: 100,
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
  menu:false,
  column: [{
    label: '卡片号',
    prop: 'cardNo',
    width: 180
  },{
    label: '车主姓名',
    prop: 'personName',
    type: 'input'
  },{
    label: '员工号',
    prop: 'badge',
  },{
    label: '车牌号',
    prop: 'general',
    type: 'input'
  },{
    label: '设备',
    prop: 'deviceName',
  },{
    label: '设备类型',
    prop: 'deviceType',
    solt: true
  },{
    label: '所在区域',
    prop: 'areaName',
  },{
    label: '下发时间',
    prop: 'startTime',
    width: 170
  },{
    label: '业务类型',
    prop: 'serviceType',
    type: 'select',
    dicData:[{
      label:'员工车辆',
      value:1
    },
    {
      label:'公司车辆',
      value:2
    },
    {
      label:'非员工车辆',
      value:3
    },
    {
      label:'访客预约',
      value:4
    },
    {
      label:'物流车预约',
      value:5
    },
    {
      label:'入厂申请',
      value:6
    }]
  },{
    label: '状态',
    prop: 'taskType',
    type: 'select',
    dicData:[{
      label:'待处理',
      value:0
    },
    {
      label:'已处理',
      value:1
    },
    {
      label:'失败',
      value:2
    },
    {
      label:'处理中',
      value:3
    }]
  },{
    label: '创建时间',
    prop: 'createTime',
    width: 170
  }
]
}

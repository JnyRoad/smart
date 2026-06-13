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

  column: [{
    label: '员工号',
    prop: 'badge',
  },{
    label: '姓名',
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
    prop: 'overTime',
    width: 170
  },{
    label: '业务类型',
    prop: 'serviceType',
    type: 'select',
    dicData:[{
      label:'变更员工权限',
      value:1
    },
    {
      label:'APP信息完善',
      value:2
    },
    {
      label:'访客预约',
      value:3
    },
    {
      label:'员工扫码登记',
      value:4
    },
    {
      label:'招聘入职',
      value:5
    },
    {
      label:'入厂申请',
      value:6
    },
    {
      label:'导入员工照片',
      value:7
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
      label:'已处理',
      value:4
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
    label: '任务类型',
    prop: 'actionDesc',
    width: 100
  },{
    label: '创建时间',
    prop: 'createTime',
    width: 170
  }
  ,
  {
    label: '下发结果',
    prop: 'remark',
    overHidden: true
  }
]
}

export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menu:true,
  menuAlign: 'center',
  menuWidth: 70,
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
  column: [
  {
    label: 'id',
    prop: 'id',
    hide: true
  },{
    label: '所属园区',
    prop: 'parkName'

  },
  {
    label: '楼栋',
    prop: 'dormitoryName'

  },

  {
    label: '房间号',
    prop: 'roomName',
    span: 24,
    width: 70
  },
  {
    label: '床位号',
    prop: 'bedNumber',
    span: 24,
    width: 70
  },
  {
    label: '工号',
    prop: 'staffBadge',
    type: 'input',
    span: 24
  }, {
    label: '员工姓名',
    prop: 'staffName',
    type: 'input',
    span: 24
  }, {
    label: '性别',
    prop: 'sex',
    type: 'select',
    width: 50,
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
  },{
    label: 'BU',
    prop: 'compName',
    span: 24,
    solt: true,
  },{
    label: '组织单位',
    prop: 'depName',
    span: 24,
    solt: true,
  },{
    label: '岗位名称',
    prop: 'jobName',
    type: 'select',
    span: 24,
    solt: true,
  },
  {
    label: '职层',
    prop: 'jcheName',
    type: 'select',
    width: 100,
    span: 24
  },
  {
    label: '离职类型',
    prop: 'leaType',
    type: 'select',
    span: 24
  },
  {
    label: '退宿类型',
    prop: 'type',
    type: 'select',
    width: 80,
    span: 24,
    solt: true
  },
  {
    label: '入住时间',
    prop: 'inTime',
    solt: true,
    span: 24,
    width: 110
  },
  {
    label: '退宿时间',
    prop: 'time',
    solt: true,
    span: 24,
    width: 110
  },
  {
    label: '退宿操作人',
    prop: 'optUser',
    width: 110,
    span: 24
  },
  {
    label: '入住操作时间',
    prop: 'inCreateTime',
    span: 24,
    width: 110
  },
  {
    label: '入住操作人',
    prop: 'inOptUser',
    width: 110,
    span: 24
  }
]
}
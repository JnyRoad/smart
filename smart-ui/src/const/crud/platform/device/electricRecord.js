export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menu: false,
  menuAlign: 'center',
  menuWidth: 300,
  labelWidth: 100,
  align: 'center',
  refreshBtn: true,
  columnBtn: true,
  searchBtn: true,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth : '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [{
    label: '更换前设备通信地址',
    prop: 'beforeAddress',
  },{
    label: '更换前设备序号',
    prop: 'beforeSeq',
  },{
    label: '更换前下行通道',
    prop: 'beforePort',
  },{
    label: '更换前倍率',
    prop: 'beforeRatio',
  },{
    label: '更换前集中器',
    prop: 'beforeConcentrator',
  },{
    label: '更换后设备通信地址',
    prop: 'afterAddress',
  },{
    label: '更换后设备序号',
    prop: 'afterSeq',
  },{
    label: '更换后下行通道',
    prop: 'afterPort',
  },{
    label: '更换后倍率',
    prop: 'afterRatio',
  },{
    label: '更换后集中器',
    prop: 'afterConcentrator',
  },{
    label: '更换时间',
    prop: 'createTime',
  },{
    label: '更换人',
    prop: 'createUserName',
  }]
}

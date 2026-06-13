export const insidertbOpt = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
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
  props: {
    label: 'label',
    value: 'value'
  },
  column: [

    {
      label: '所属园区',
      prop: 'parkName'

    },
    {
    label: '姓名',
    prop: 'personName',
    type: 'input'
  },{
    label: '工号',
    prop: 'badge',
    type: 'input'
  }, {
    label: '出入地点',
    prop: 'areaName'
  }, {
    label: '设备名称',
    prop: 'deviceName'
  },
  {
    label: '出入地点',
    prop: 'areaId',
    type: 'select',
    hide:true
  },
  {
    label: '出入类型',
    prop: 'eventType',
    type: 'select',
    // dicUrl: '/admin/dict/type/log_type',
    dicData: [{
      label: '进门',
      value: 1
    }, {
      label: '出门',
      value: 2
    }]
  },   {
    label: '出入时间',
    prop: 'snapTime',
    type: 'datetime',
    format: 'yyyy-MM-dd HH:mm:ss',
    valueFormat: 'yyyy-MM-dd HH:mm:ss',
    width: 160,
    more: true
  }, {
    label: 'BU',
    prop: 'compName',
    solt: true
  },{
    label: '体温',
    prop: 'faceTemperature',
    width: 80
  },
  {
    label: 'BU',
    prop: 'compId',
    type: 'select',
    hide:true
  }
]
}
export const outsidertbOpt = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
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
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '所属园区',
      prop: 'parkName'

    },
    {
    label: '姓名',
    prop: 'personName',
    type: 'input'
  }, {
    label: '出入地点',
    prop: 'areaName'
  }, {
    label: '设备名称',
    prop: 'deviceName'
  },
  {
    label: '出入地点',
    prop: 'areaId',
    type: 'select',
    hide:true
  },
  {
    label: '出入类型',
    prop: 'eventType',
    type: 'select',
    // dicUrl: '/admin/dict/type/log_type',
    dicData: [{
      label: '进门',
      value: 1
    }, {
      label: '出门',
      value: 2
    }]
  },   {
    label: '出入时间',
    prop: 'snapTime',
    type: 'datetime',
    format: 'yyyy-MM-dd HH:mm:ss',
    valueFormat: 'yyyy-MM-dd HH:mm:ss',
    width: 160,
    more: true
  }, {
    label: '所属单位',
    prop: 'company'
  },{
    label: '体温',
    prop: 'faceTemperature',
    width: 80
  }]
}

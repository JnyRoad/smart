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
      label: '车主',
      prop: 'driverName',
      type: 'input',
      solt: true,
    },
	  {
      label: '车牌号',
      prop: 'vehiclePlate',
      type: 'input'
    },
    {
      label: '出入地点',
      prop: 'areaName'
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
    },
	  {
      label: '出入时间',
      prop: 'snapTime',
      type: 'datetime',
      more:true
    },
    {
      label: 'BU',
      prop: 'compName'

    },
    {
      label: '手机号',
      prop: 'driverPhone'
    },
    {
      label: '方式',
      prop: 'letPass',
      type: 'select',
      dicData: [{
        label: '手动',
        value: 0
      }, {
        label: '自动',
        value: 1
      }]
    },
    {
      label: '权限',
      prop: 'authority',
      type: 'select',
      dicData: [{
        label: '有',
        value: 0
      }, {
        label: '无',
        value: 1
      }]
    },
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
    label: '车主',
    prop: 'driverName',
    solt: true,
    type: 'input'
  }, {
    label: '车牌号',
    prop: 'vehiclePlate',
    type: 'input'
  }, {
    label: '出入地点',
    prop: 'areaName'
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

  }, {
    width: 150,
    label: '出入时间',
    prop: 'snapTime',
    type: 'datetime',
    format: 'yyyy-MM-dd HH:mm',
    valueFormat: 'yyyy-MM-dd HH:mm:ss',
    more:true
  }, {
    label: '所属单位',
    prop: 'company'
  }, {
    label: '手机号',
    prop: 'driverPhone',
    type: 'input'
  },
  {
    label: '方式',
    prop: 'letPass',
    type: 'select',
    dicData: [{
      label: '手动',
      value: 0
    }, {
      label: '自动',
      value: 1
    }]
  },
  {
    label: '权限',
    prop: 'authority',
    type: 'select',
    dicData: [{
      label: '有',
      value: 0
    }, {
      label: '无',
      value: 1
    }]
  },
]}

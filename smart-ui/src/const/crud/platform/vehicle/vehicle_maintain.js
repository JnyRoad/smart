export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 200,
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
    selection: false,
    props: {
      label: 'label',
      value: 'value'
    },
    column: [
      {
        label: '主键',
        prop: 'id',
        type: 'input',
        hide: true
      },
      {
        label: '联系人',
        prop: 'name',
        type: 'input'
      },{
        label: '联系方式',
        prop: 'phone',
        type: 'input'
      },
        {
        label: '车牌号',
        prop: 'vehiclePlate',
        type: 'input'
      },
        {
        label: '车牌类型',
        prop: 'vehicleTypeName',
        type: 'select'
      },,
      {
      label: '车牌状态',
      prop: 'cardState',
      type: 'select'
    },
      {
        label: '开始时间',
        prop: 'startDate',
        type: 'select'
      },
        {
        label: '结束时间',
        prop: 'endDate',
        type: 'select'
      },
      {
        label: '所属园区',
        prop: 'parkName',
        type: 'select'
      },
        {
        label: '创建时间',
        prop: 'createTime',
        type: 'select'
      },
        {
        label: '创建人',
        prop: 'createTime'
      }
    ]
  }

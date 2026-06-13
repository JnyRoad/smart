/**
 * 开门记录
 */

 export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    indexWidth: 100,
    indexFixed: true,
    stripe: true,
    menu:false,
    menuAlign: 'center',
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
        label: '门锁名称',
        prop: 'deviceName'
      },
      {
        label: '绑定房间',
        prop: 'deviceArea'
      },
      {
        label: '人员编号',
        prop: 'personNum'
      },
      {
        label: '姓名',
        prop: 'personName'
      },
      {
        label: '开门方式',
        prop: 'openTypeDesc'
      },
      {
        label: '开门时间',
        prop: 'openTime'
      }
    ]
  }

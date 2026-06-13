/**
 * 门锁绑定
 */

 export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    indexWidth: 100,
    indexFixed: true,
    stripe: true,
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
        label: '状态',
        prop: 'connectStatusDesc'
      },
      {
        label: '绑定房间',
        prop: 'deviceArea'
      }
    ]
  }

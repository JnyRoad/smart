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
    menu:true,
    menuWidth: 240,
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
      // {
      //   label: '绑定房间',
      //   prop: 'home'
      // },
      {
        label: '工号',
        prop: 'personNum'
      },
      {
        label: '授权人员',
        prop: 'personName'
      },
      {
        label: '手机号',
        prop: 'personPhone'
      },
      {
        label: '授权时间',
        prop: 'validTime',
        width:310
        // solt: true
      },
      // {
      //   label: '授权开始时间',
      //   prop: 'validTimeStart'
      // },
      // {
      //   label: '授权结束时间',
      //   prop: 'validTimeEnd'
      // },
      {
        label: '状态',
        prop: 'statusDesc'
      },
      {
        label: '备注',
        prop: 'keyList',
        solt: true
      }

    ]
  }

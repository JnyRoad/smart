export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 100,
    labelWidth: 150,
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
      label: '工号',
      prop: 'badge'
    },{
      label: '反馈人',
      prop: 'staffName'
    },{
      label: 'BU',
      prop: 'compName'
    },{
      label: '部门',
      prop: 'depName'
    },
    {
      label: '反馈人电话',
      prop: 'staffPhone'
    },
    {
      label: '反馈类型',
      prop: 'appealTypeDesc'
    },
    // {
    //   label: '状态',
    //   prop: 'statusDesc',
    //   solt: true,
    //   width: 130
    // },
    {
      label: '状态',
      prop: 'statusDesc'
    },
    {
      label: '是否已转交',
      prop: 'ischange',
      solt: true
    },
    {
      label: '反馈时间',
      prop: 'createTime'
    }]

  }
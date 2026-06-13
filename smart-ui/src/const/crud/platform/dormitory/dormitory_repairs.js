export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 150,
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
      label: '报修人',
      prop: 'name'
    },{
      label: 'BU',
      prop: 'compName'
    },{
      label: '部门',
      prop: 'depName'
    },
    {
      label: '维修区域',
      prop: 'rangeTypeDesc'
    },
    {
      label: '维修类别',
      prop: 'repairTypeDesc'
    },
    {
      label: '楼栋',
      prop: 'dormitoryName'
    },
    {
      label: '房间',
      prop: 'roomName'
    },
    {
      label: '状态',
      prop: 'statusDesc'
    }]

  }
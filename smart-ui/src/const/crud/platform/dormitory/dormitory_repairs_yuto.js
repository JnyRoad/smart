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
      label: '维修区域',
      prop: 'rangeTypeDesc'
    },
    {
      label: '维修类别',
      prop: 'repairTypeDesc'
    },
    {
      label: '所在楼栋',
      prop: 'dormitoryName'
    },
    {
      label: '所在房间',
      prop: 'roomName'
    },
    {
      label: '故障描述',
      prop: 'faultDesc',
    },
    {
      label: '申请时间',
      prop: 'createTime'
    },
    {
      label: '申请人',
      prop: 'name'
    },
    {
      label: '状态',
      prop: 'statusDesc'
    }]

  }

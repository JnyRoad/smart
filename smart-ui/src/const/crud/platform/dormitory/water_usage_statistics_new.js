export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 120,
    menu:false,
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
      label: '所在区域',
      prop: 'areaName'
    },{
      label: '设备名称',
      prop: 'deviceName'
    },{
      label: '表类型',
      prop: 'sdType',
      solt: true
    },{
      label: '设备标签',
      prop: 'deviceTag',
    },{
      label: '通讯地址',
      prop: 'commAddress',
      empty: '-',
      width: 180
    },{
      label: '关联集中器',
      prop: 'concentratorName',
      width: 160
    },{
      label: '查询开始时间',
      prop: 'startDate',
      width: 130
    },{
      label: '查询结束时间',
      prop: 'endDate',
      width: 130
    },
    {
      label: '查询段起数',
      prop: 'startNum',
      width: 160
    },
    {
      label: '查询段止数',
      prop: 'endNum',
      width: 160
    },
    {
      label: '查询段累计用量',
      prop: 'sumNum',
      width: 180
    }]
  }
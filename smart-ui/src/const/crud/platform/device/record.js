export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menu: false,
    menuAlign: 'center',
    menuWidth: 300,
    labelWidth: 100,
    align: 'center',
    refreshBtn: true,
    columnBtn: true,
    searchBtn: true,
    showClomnuBtn: false,
    searchSize: 'mini',
    dialogWidth : '600px',
    addBtn: false,
    editBtn: false,
    delBtn: false,
    viewBtn: false,
    column: [{
      label: '当前读数',
      prop: 'currentReading',
    },
	  {
      label: '状态',
      prop: 'isError',
      type: 'select',
      dicData:[{
        label: '正常',
        value: 0
      },
      {
        label: '异常',
        value: 1
      }]
    },{
      label: '采集时间',
      prop: 'collectTime',
    }]
  }
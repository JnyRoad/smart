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
      label: '操作动作',
      prop: 'actionDesc',
    },{
      label: '操作人',
      prop: 'createUserName',
      with: 140
    },{
      label: '操作时间',
      prop: 'createTime',
      with: 200
    }]
  }
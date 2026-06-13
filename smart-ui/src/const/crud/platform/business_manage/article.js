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
      label: '工号',
      prop: 'badge'
    },
    {
      label: '申请人',
      prop: 'name'
    },
    {
      label: 'BU',
      prop: 'compName'
    },
    {
      label: '部门',
      prop: 'deptName'
    },
    {
      label: '放行事项',
      prop: 'releaseItemDesc',
    },
    {
      label: '流程编号',
      prop: 'processId'
    },
    {
      label: '创建时间',
      prop: 'createTime'
    }]

  }

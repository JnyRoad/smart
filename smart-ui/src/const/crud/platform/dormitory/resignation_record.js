export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    stripe: true,
    menuAlign: 'center',
    menuWidth: 100,
    labelWidth: 100,
    align: 'center',
    refreshBtn: false,
    columnBtn: false,
    searchBtn: true,
    showClomnuBtn: false,
    searchSize: 'mini',
    dialogWidth: '600px',
    addBtn: false,
    editBtn: false,
    delBtn: false,
    viewBtn: false,
    column: [
      {
        label: 'id',
        prop: 'id',
        hide: true
      },
      {
        label: '园区',
        prop: 'parkName'
      },
      {
        label: '工号',
        prop: 'badge',
      }, {
        label: '姓名',
        prop: 'name'
      },
      {
        label: 'BU',
        prop: 'bu'
      },
      {
        label: '部门',
        prop: 'dept'
      },
      {
        label: '个人扣款',
        prop: 'fee'
      },
      {
        label: '离职日期',
        prop: 'leaveDate',
        width: 160
      },
      {
        label: '状态',
        prop: 'status',
        type: 'select',
        dicData: [{
          label: '成功',
          value: 1
        }]
      },
      {
        label: '生成时间',
        prop: 'createTime',
        width: 160
      }
    ]
  }
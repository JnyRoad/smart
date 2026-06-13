export const tableOption = {
    border: false,
    index: true,
    indexLabel: '序号',
    indexWidth: 100,
    indexFixed: true,
    stripe: true,
    menuAlign: 'center',
    menuWidth: 150,
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
        label: '园区',
        prop: 'parkName'
      },
      {
        label: '宿舍信息',
        prop: 'dormitoryName'
      },
      {
        label: '退宿原因',
        prop: 'quitReasonDesc',
      },
        {
        label: '预计离开时间',
        prop: 'applyLeaveTime',
        type:'datetime',
        width: 165
        // format: "yyyy-MM-dd"
      },
        {
        label: '备注',
        prop: 'remark'
      },
      {
        label: '申请人工号',
        prop: 'badge',
      },
        {
        label: '申请人姓名',
        prop: 'name',
      },

      {
        label: '申请时间',
        prop: 'createTime',
        width: 165
      },
      {
        label: '审批节点',
        prop: 'statusDesc',
      },
      {
        label: '是否处理',
        prop: 'isHandle',
      },
    ]
  }

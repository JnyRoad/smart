export const tableOption = {
    border: true,
    index: true,
    indexLabel: '序号',
    stripe: false,
    menuAlign: 'center',
    align: 'center',
    editBtn: false,
    delBtn: false,
    addBtn: false,
    column: [
      {
        label: 'ID',
        prop: 'id',
        hide: true
      },
      {
        label: 'parkId',
        prop: 'parkId',
        hide: true
      },
        {
        label: '园区',
        prop: 'parkName'
      },
        {
        label: '创建时间',
        prop: 'createTime'
      },
    ]
  }

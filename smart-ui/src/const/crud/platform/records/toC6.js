export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  menuWidth: 250,
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
      label: '记录流水号',
      prop: 'id',
      type: 'input'
    },
    {
      label: '工号',
      prop: 'empNo',
      type: 'input'
    },
    {
      label: '是否已同步过',
      prop: 'isDispose',

       type: 'select',
       dicData:[{
         label: '未同步',
         value: 0
       },
       {
         label: '已同步',
         value: 1
       }]
    },


    {
      label: '创建时间',
      prop: 'createTime',
      width: 165
    }
  ]
}
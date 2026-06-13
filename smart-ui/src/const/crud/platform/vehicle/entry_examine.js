export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
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
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '主键',
      prop: 'id',
      type: 'input',
      hide: true
    },
    {
      label: '车牌号',
      prop: 'vehiclePlate',
      type: 'input'
    },
    {
      label: '车主',
      prop: 'name'
    },
    {
      label: '所属部门',
      prop: 'depName'
    },
    {
      label: '手机号',
      prop: 'phone'
    },
    {
      label: '福利层级',
      prop: 'welfareLevel'
    },
	  {
      label: '申请园区',
      prop: 'parkName',
      type: 'select'
    },
	  {
      label: '申请时间',
      prop: 'createTime',
      more: true,
      type: 'date',
      valueFormat: 'yyyy-MM-dd'
    },
	  {
      label: '申请状态',
      prop: 'applyStatus',
      align: 'left',
      // solt: true,
      type: 'select',
      dicData:[
        {
          label:'审批中',
          value:0
        },{
          label:'已审批',
          value:1
        },{
          label:'已拒绝',
          value:2
        }
      ]
    }
  ]
}
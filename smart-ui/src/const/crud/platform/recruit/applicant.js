export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 290,
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
  selection: true,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '所属园区',
      prop: 'parkName',
      type: 'input'
    },
	  {
      label: '姓名',
      prop: 'name',
      type: 'input'
    },
    {
      label: '性别',
      prop: 'sex',
      width: 50,
      type: 'select',
      dicData:[{
        label:'男',
        value: 0
      },
      {
        label:'女',
        value: 1
      },
      {
        label:'未知',
        value:2
      }]
    },
    {
      label: '年龄',
      width: 60,
      prop: 'age',
      type: 'select'
    },
	  {
      label: '手机号',
      prop: 'phone',
      type: 'input'
    },
	  {
      label: '岗位名称',
      prop: 'jobName',
      type: 'select'
    },
	 {
      label: '职层',
      prop: 'jcheName'
    },
	  {
      label: '投递时间',
      prop: 'rangTime',
      type: 'datetime',
      hide:true,
      valueFormat:"yyyy-MM-dd HH:mm:ss",
    }
    ,
	  {
      label: '投递时间',
      prop: 'applyDate'

    }
  ]
}

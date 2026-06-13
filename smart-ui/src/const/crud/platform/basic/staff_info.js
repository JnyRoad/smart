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
  selection: true,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '工号',
      prop: 'badge',
      type: 'input'
    },
    {
      label: '姓名',
      prop: 'name',
      type: 'input'
    },
    {
      label: 'BU',
      prop: 'compId',
      type: 'select',
      hide:true
    },
	  {
      label: '部门',
      prop: 'compId',
      type: 'select',
      hide:true
    },
	  {
      label: '职层',
      prop: 'jcheId',
      type: 'select',
      hide:true
    },
    {
      label: '岗位',
      prop: 'jobId',
      type: 'select',
      hide:true
    },
    {
      label: 'id',
      prop: 'id',
      hide:true
    },
    {
      label: 'BU',
      prop: 'compName',
    },
    {
      label: '中心',
      prop: 'depAbbr',
    },
	  {
      label: '部门',
      prop: 'depName',
    },
	  {
      label: '职层',
      prop: 'jcheName',
    },
    {
      label: '岗位',
      prop: 'jobName',
    },
    {
      label: '入职日期',
      prop: 'createTime',
      width: 165
    },
	  {
      label: '员工状态',
      prop: 'status',
      type: 'select',
      solt: true
    },
    {
      label: '所属园区',
      prop: 'parkName',
    },
    {
      label: '是否有照片',
      prop: 'facePicId',
      type: 'select',
      solt: true
    },
    {
      label: '通关权限',
      prop: 'deviceAuth',
      solt: true,
      overHidden: true
    },
    {
      label: 'APP权限',
      prop: 'appAuth',
      solt: true,
      overHidden: true
    }
  ]
}

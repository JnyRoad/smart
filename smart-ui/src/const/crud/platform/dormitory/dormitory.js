export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
  labelWidth: 100,
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
  column: [{
    label: '宿舍楼名称',
    prop: 'dormitoryName',
    rules: [{
      required: true,
      message: '请输入宿舍楼名称',
      trigger: 'blur'
    }],
    span: 24
  }
  ,
  {
    label: '所属园区',
    prop: 'parkName',
    addVisdiplay:false,
    editVisdiplay:false

  }
  , {
    label: '所属园区',
    prop: 'parkId',
    formsolt: true,
    type: 'select',
    hide:true,
    rules: [{
      required: true,
      message: '请选择所属园区',
      trigger: 'change'
    }],
    span: 24
  }

]
}
export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 220,
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
  selection: true,
  column: [
  {
    label: '授权人员',
    prop: 'personName'
  },{
    label: '身份证',
    prop: 'idCard'
  },
  {
    label: '所属公司',
    prop: 'companyName'
  },
  {
    label: '园区',
    prop: 'parkName'
  }]
}
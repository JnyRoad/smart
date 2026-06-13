export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 300,
  labelWidth: 100,
  align: 'center',
  refreshBtn: true,
  columnBtn: true,
  searchBtn: true,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth : '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [{
    label: '卡号',
    prop: 'cardNo',
  },{
    label: '姓名',
    prop: 'name',
  },{
    label: '工号',
    prop: 'badge',
  },{
    label: '人脸图片',
    prop: 'faceImage',
    solt: true,
  },{
    label: '创建时间',
    prop: 'createTime',
  }]
}
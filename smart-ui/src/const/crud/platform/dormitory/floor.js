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
  column: [
  {
    label: '所属园区',
    prop: 'parkId',
    type: 'select',
    hide:true
  },{
    label: '所属楼栋',
    prop: 'dormitoryId',
    type: 'select',
    hide:true
  },
  {
    label: '园区名称',
    prop: 'parkName'
  },

  {
    label: '楼栋名称',
    prop: 'dormitoryName'
  },
  {
    label: '楼层编号',
    type: 'input',
    prop: 'floorName'
  },{
    label: '起始编号',
    type: 'input',
    prop: 'startNum',
    hide: true
  },{
    label: '楼层数量',
    type: 'input',
    prop: 'floorNum',
    hide: true
  },{
    label: '房间数量',
    type: 'input',
    prop: 'roomNum'
  }]

}
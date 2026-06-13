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
    label: '房间号',
    type: 'input',
    solt: true,
    prop: 'roomName',
  }, {
    label: '是否参与分配',
    prop: 'isDormitoryRoom',
    type: 'select',
    dicData: [{
      label: '是',
      value: 0
    }, {
      label: '否',
      value: 1
    }]
  }, {
    label: '宿舍分类',
    prop: 'typeName',
    type: 'select',
    solt: true,
  },{
    label: '床位数',
    type: 'input',
    prop: 'bedTotal'
  },
 {
    label: '实住人数',
    prop: 'usedBed'
  },
  {
    label: '差异人数',
    prop: 'freeBed'
  },

{
    label: '房间属性',
    prop: 'roomSex',
    type: 'select',
    dicData: [{
      label: '男',
      value: 0
    }, {
      label: '女',
      value: 1
    },{
      label: '夫妻/家属',
      value: 2
    }, {
      label: '其他',
      value: 3
    }]
  },
  {
    label: '所属园区',
    prop: 'parkName'
  }

]

}
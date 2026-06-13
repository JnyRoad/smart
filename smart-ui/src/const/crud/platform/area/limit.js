export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 280,
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
      hide:true
    },
    {
      label:'权限策略名称',
      prop:'authorityName'
    },
    {
      label:'类型',
      prop:'type',
      type: 'select',
      dicData:[{
        label:'人员',
        value:1
      },
      {
        label:'车辆',
        value:3
      }]
    },
    {
      label:'权限性质',
      prop:'areaType',
      type: 'select',
      dicData:[{
        label:'公共区域',
        value:0
      },
      {
        label:'保密区域',
        value:1
      }]
    },
    {
      label: '备注',
      prop: 'remark'
    }
    ,
    {
      label: '所属园区',
      prop: 'parkName'
    }
  ]
}
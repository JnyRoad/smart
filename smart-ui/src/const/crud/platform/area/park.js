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
      label:'id',
      prop:'id',
      hide:true

    },
    {
      label: '园区名称',
      prop: 'parkName'
    },
	  {
      label: '园区地址',
      prop: 'parkAddress'
    },
    {
      label: '转发服务url',
      prop: 'bridgeUrl'
    },
    {
      label: '园区定位范围（米）',
      prop: 'radius'
    },
	  {
      label: '经度',
      prop: 'parkLongitude'
    },
	  {
      label: '纬度',
      prop: 'parkLatitude'
    },
    {
      label: '咨询电话',
      prop: 'parkPhone'
    }
  ]
}
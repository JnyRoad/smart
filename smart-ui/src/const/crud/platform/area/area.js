export const areaOption = {
  border: false,
  stripe: true,
  menu: false,
  menuAlign: 'center',
  menuWidth: 150,
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
      label: 'parkid',
      prop: 'parkId',
      hide:true
    },
    {
      label: 'id',
      prop: 'id',
      hide:true
    },
    {
      label: '所属园区',
      prop: 'parkName'
    },
    {
      label: '区域名称',
      prop: 'areaName'
    },
    {
      label: '上级区域id',
      prop: 'pid',
      hide:true
    },
	  {
      label: '上级区域名称',
      prop: 'parentAreaName',
      solt:true
    },
	  {
      label: '经度',
      prop: 'areaLongitude'
    }
    ,
	  {
      label: '纬度',
      prop: 'areaLatitude'
    },
    {
      label: '备注',
      prop: 'remark'
    }
  ]
}
export const personOption = {
  border: false,
  stripe: true,
  index: true,
  indexLabel: '序号',
  menuAlign: 'center',
  menu: false,
  align: 'center',
  refreshBtn: true,
  columnBtn: true,
  searchBtn: true,
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
      label: '工号',
      prop: 'workid'
    },
	  {
      label: '姓名',
      prop: 'staff_name'
    },
	  {
      label: 'BU',
      prop: 'bu'
    },
	  {
      label: '部门',
      prop: 'department'
    },
	  {
      label: '职层',
      prop: 'layer'
    },
	  {
      label: '岗位',
      prop: 'position'
    }
  ]
}
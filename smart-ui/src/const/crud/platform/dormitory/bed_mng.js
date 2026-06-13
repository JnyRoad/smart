export const tableOption = {
  border: false,
  index: false,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 220,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: true,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth: '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  selection: true,
  column: [
    {
      label: 'id',
      prop: 'id',
      hide: true
    },
    {
      label: '所属园区',
      prop: 'parkName',
      hide: true
    },
    {
      label: '楼栋',
      prop: 'dormitoryName'
    },
    {
      label: '房间',
      prop: 'roomName',
      width: 60
    }, {
      label: '类型',
      prop: 'dormitoryTypeName'
    }, {
      label: '标记',
      prop: 'remark',
      solt: true
    },
    {
      label: '类型id',
      prop: 'roomType',
      hide: true
    },
    {
      label: '铺位',
      prop: 'bedNumber',
      width: 50,
      hide: true
    },
    {
      label: '床位',
      prop: 'bedName',
      width: 80,
      solt: true
    },
    {
      label: '是否锁定',
      prop: 'delFlag',
      width: 80,
      solt: true
    },
    {
      label: '工号',
      prop: 'staffBadge',
      solt: true,
      width: 80
    }, {
      label: '姓名',
      prop: 'name',
      solt: true
    },
    {
      label: 'roomSex',
      prop: 'roomSex',
      hide: true
    },
    {
      label: 'bedId',
      prop: 'bedId',
      hide: true
    },
    {
      label: '性别',
      prop: 'sex',
      width: 50,
      type: 'select',
      dicData: [{
        label: '男',
        value: 0
      },
      {
        label: '女',
        value: 1
      },
      {
        label: '未知',
        value: 2
      }]
    },{
      label: '家属',
      prop: 'family',
      solt: true
    },
    // {
    //   label: 'BU',
    //   prop: 'compName',
    //   solt: true,
    // },
    // {
    //   label: '组织单位',
    //   prop: 'depName',
    //   solt: true,
    // },
    {
      label: '岗位/部门',
      prop: 'jobName',
      width: 100,
      solt: true,
    },
    {
      label: '入住日期',
      prop: 'createTime',
      solt: true,
      width: 110
    }, {
      label: '员工状态',
      prop: 'status',
      solt: true,
      type: 'select',
      width: 80
    },
    {
      label: '备注',
      prop: 'simpleRemark',
      solt: true
    },
    {
      label: '入职日期',
      prop: 'joinDate',
      solt: true,
      width: 110
    },
    {
      label: '离职日期',
      prop: 'leaDate',
      solt: true,
      width: 110
    }
  ]
}
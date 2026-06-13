const validateTypeName = (rule, value, callback) => {
  var rename = /^[\u4e00-\u9fa5_a-zA-Z0-9]{1,30}$/;
 if(!rename.test(value))
 {
   callback(new Error('宿舍类型名称只允许汉字、字母与数字的组合,最长为30个字符'));
 } else {
   callback();
 }
};

const validateBedTotal = (rule, value, callback) => {
  var rename = /^[0-9]*$/;
  if(!rename.test(value)){
    callback(new Error('不能输入小数和负数'));
  } else {
    if(value>100){
      callback(new Error('床位数量最大值为100'));
    }else{
      callback();
    }
  }
};


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
    label: '分类类型',
    prop: 'typeName',
    rules: [{
      required: true,
      message: '请输入宿舍分类名称',
      trigger: 'blur'
    },
    { validator: validateTypeName, trigger: 'blur' }],
    span: 24
  },
  {
    label: '对应职层',
    type: 'select',
    prop: 'jches',
    formsolt: true,
    hide:true,
    span:24

  },
  {
    label: '对应职层',
    prop: 'jcheName',
    addVisdiplay:false,
    editVisdiplay:false
  },

  {
    label: '床位数量',
    prop: 'bedTotal',
    rules: [{
      required: true,
      message: '请输入床位数量',
      trigger: 'blur'
    },
    { validator: validateBedTotal, trigger: 'blur' }],
    span: 24
  },
  {
    label: '所属园区',
    prop: 'parkName',
    addVisdiplay:false,
    editVisdiplay:false

  },

  {
    label: '所属园区',
    type: 'select',
    prop: 'parkId',
    formsolt: true,
    hide:true,
    rules: [{
      required: true,
      message: '请选择所属园区',
      trigger: 'change'
    },
    { required: true, message: '请选择所属园区', trigger: 'change' }],
    span:24

  },
]
}
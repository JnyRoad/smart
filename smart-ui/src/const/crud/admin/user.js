import { getDetails } from '@/api/admin/user'
import { validatePwd } from "@/util/password";
import { validatenull } from '@/util/validate'
var validateUsername = (rule, value, callback) => {
  getDetails(value).then(response => {
    if (window.boxType === 'edit') callback()
    let result = response.data.data
    if (result !== null) {
      callback(new Error('用户名已经存在'))
    } else {
      callback()
    }
  })
}
var validatePassWord = (rule, value, callback) => {
  if (validatenull(value)) {
    // 编辑时密码留空表示不修改密码
    if (window.boxType === 'add') {
      callback(new Error('请输入密码'))
    } else {
      callback()
    }
    return
  }
  let r = validatePwd(value);
  if (r[0]) {
    callback(new Error(r[1]))
  } else {
    callback();
  }
}
export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  editBtn: false,
  delBtn: false,
  align: 'center',
  addBtn: false,
  column: [{
    fixed: true,
    label: 'id',
    prop: 'userId',
    span: 24,
    hide: true,
    editDisabled: true,
    addVisdiplay: false
  }, {
    fixed: true,
    label: '用户名',
    prop: 'username',
    editDisabled: true,
    solt: true,
    search: true,
    span: 24,
    rules: [{
      required: true,
      message: '请输入用户名'
    },
    {
      min: 3,
      max: 20,
      message: '长度在 3 到 20 个字符',
      trigger: 'blur'
    },
    { validator: validateUsername, trigger: 'blur' }
    ]
  },
  {
    label: '姓名',
    prop: 'fullName',
    value: '',
    search: true,
    span: 24,
    rules: [{
      required: true,
      message: '请输入姓名'
    }
    ]
  },
  {
    label: '密码',
    prop: 'password',
    type: 'password',
    value: '',
    formsolt: true,
    hide: true,
    span: 24,
    rules: [
      { validator: validatePassWord, trigger: 'blur' }
    ]
  },
  // {
  //   label: '所属部门',
  //   prop: 'deptId',
  //   formsolt: true,
  //   solt: true,
  //   span: 24,
  //   hide: true,
  //   rules: [{
  //     required: true,
  //     message: '请选择部门',
  //     trigger: 'blur'
  //   }]
  // },
   {
    label: '手机号',
    prop: 'phone',
    type: 'phone',
    value: '',
    search: true,
    span: 24,
    rules: [{
      min: 6,
      max: 20,
      message: '长度在 11 个字符',
      trigger: 'blur'
    }]
  }, {
    label: '关联园区',
    prop: 'park',
    formsolt: true,
    solt: true,
    overHidden: true,
    span: 24,
    rules: [{
      required: true,
      message: '请选择园区',
      trigger: 'blur'
    }]
  },{
    label: '角色',
    prop: 'role',
    formsolt: true,
    solt: true,
    overHidden: true,
    span: 24,
    rules: [{
      required: true,
      message: '请选择角色',
      trigger: 'blur'
    }]
  }, {
    label: '角色名称',
    prop: 'roleName',
    search: true,
    hide: true,
    addVisdiplay: false,
    editVisdiplay: false,
    span: 24
  }, {
    label: '状态',
    prop: 'lockFlag',
    type: 'select',
    solt: true,
    span: 24,
    rules: [{
      required: true,
      message: '请选择状态',
      trigger: 'blur'
    }],
    dicData: [{
      label: '有效',
      value: '0'
    }, {
      label: '锁定',
      value: '9'
    }]
  }, {
    width: 180,
    label: '创建时间',
    prop: 'createTime',
    type: 'datetime',
    format: 'yyyy-MM-dd HH:mm',
    valueFormat: 'yyyy-MM-dd HH:mm:ss',
    editDisabled: true,
    addVisdiplay: false,
    span: 24
  }]
}

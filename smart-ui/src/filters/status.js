import { getLabel } from '@/util/util'
import { enumStaffStatus } from "@/const/crud/platform/enum";

/**------------------------------------------------------------------------------------------------------------
 * 入园审批状态
 * 0-审批中
 * 1-通过
 * 2-未通过
 */
export function entryExamineFormat (status) {
  if(status==0){
    return '审批中'
  }else if(status==1){
    return '通过'
  }else if(status==2){
    return '未通过'
  }
}

/**
 * 入园审批状态,
 * 0-审批中-commonStatus1//sass for循环的i是非0数
 * 1-通过-commonStatus2
 * 2-未通过-commonStatus3
 */

export function entryExamineClassFormat (status) {
  return 'commonStatus'+ (Number(status)+1);
}
/**-----------------------------
 * 招聘状态
 * 0-招聘结束
 * 1-招聘中
 * 2-招聘暂停
 */

export function recruitStatusFormat (status) {
  if(status==0){
    return '招聘结束'
  }else if(status==1){
    return '招聘中'
  }else if(status==2){
    return '招聘暂停'
  }
}

/**
 * 招聘状态,
 * 0-招聘结束-recruitStatus1//sass for循环的i是非0数
 * 1-招聘中-recruitStatus2
 * 2-停止招聘-recruitStatus3
 */

export function recruitStatusClassFormat (status) {
  return 'recruitStatus'+ (Number(status)+1);
}
/**-----------------------------------------------------
 * 应聘状态
 * 0-已投递
 * 1-已邀请
 * 2-已拒绝
 * 3-已录取
 * 4-未录取
 * 5-已入职
 * 6-未入职
 * 7-待复试，暂定
 * 默认是0
 */
export function applyStatusFormat (status) {
  if(status==0){
    return '已投递'
  }else if(status==2){
    return '已邀请'
  }else if(status==1){
    return '已拒绝'
  }else if(status==3){
    return '待入职'
  }else if(status==4){
    return '待复试'
  }else if(status==5){
    return '已入职'
  }else if(status==6){
    return '已入库'
  }
}

/**
 * 应聘状态,
 * 0-已投递-applyStatus1//sass for循环的i是非0数
 * 1-已邀请-applyStatus2
 * 2-已拒绝-applyStatus3
 * 3-已录取-applyStatus4
 * 4-未录取-applyStatus5
 * 5-已入职-applyStatus6
 * 6-未入职-applyStatus7
 * 7-待复试-applyStatus8
 * 默认是0
 */

export function applyStatusClassFormat (status) {
  return 'applyStatus'+ (Number(status)+1);
}
/**
 * 访客预约状态
 * 0:审批通过
 * 1:已拒绝
 * 2:待审批
 * 3:已到达
 * 4:超时未到
 * 5:已离开
 * 6:预约超时
 * 7:已作废
 */
export function visitorStatusFormat (status) {
  if(status==0){
    return '审批通过'
  }else if(status==1){
    return '已拒绝'
  }else if(status==2){
    return '待审批'
  }else if(status==3){
    return '已到达'
  }else if(status==4){
    return '超时未到'
  }else if(status==5){
    return '已离开'
  }else if(status==6){
    return '预约超时'
  }else if(status==7){
    return '已作废'
  }
}
/**
 * 访客预约状态
 * 0:审批通过 -visitorStatus1
 * 1:已拒绝-visitorStatus2
 * 2:待审批-visitorStatus3
 * 3:已到达-visitorStatus4
 * 4:超时未到-visitorStatus5
 * 5:已离开-visitorStatus6
 * 6:预约超时-visitorStatus7
 * 7:已作废-visitorStatus8
 */
export function visitorStatusClassFormat (status) {
  return 'visitorStatus'+ (Number(status)+1);
}

/**
 * 设备接通状态
 * 1:未连接
 * 2:接通
 * 3:断开
 */
export function deviceStatusFormat (status) {
  if(status==0){
    return '未连接'
  }else if(status==2){
    return '接通'
  }else if(status==1){
    return '断开'
  }
}
/**
 * 设备接通状态
 * 1:未连接 - deviceStatus1
 * 2:接通 - deviceStatus2
 * 3:断开 - deviceStatus3
 */
export function deviceStatusClassFormat (status) {
  return 'deviceStatus'+ status;
}
/**
 * 性别显示
 * 0:男
 * 1:女
 * 2:未知
 */
export function genderInit (gender) {
  const typeOption = {
    0: '男',
    1: '女',
    2: '未知'
  }
  return typeOption[gender]
}
/**
 * 房间性别显示
 * 0:男
 * 1:女
 * 2:夫妻/家属
 * 2:其他
 */
export function roomGenderInit (gender) {
  const typeOption = {
    0: '男',
    1: '女',
    2: '夫妻/家属',
    3: '其他'
  }
  return typeOption[gender]
}
/**
 * 员工状态
 * 0:已离职
 * 1:在职
 * 2:试用
 * 3:实习
 * 4:临时人员
 * -1:未知
 */
export function staffStatusInit (status) {
  return getLabel(enumStaffStatus, status)
}


/**
 * 工资签收状态
 * 0:未签收
 * 1:已签收
 */
export function salaryStatusInit (status) {
  if(status===0){
    return '未签收'
  }else if(status===1){
    return '已签收'
  }
}

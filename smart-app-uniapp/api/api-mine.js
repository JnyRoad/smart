import axios from '@/tools/request'
import {
	API_EMPLOYEE_BASEINFO,
	API_EMPLOYEE_FULLINFO,
	API_EMPLOYEE_RELATION_UPDATE,
	API_EMPLOYEE_ROOM_DETAIL,
	API_EMPLOYEE_QRCODE,
	API_EMPLOYEE_ROOM_APPLY,
	API_EMPLOYEE_VEHICLE_BASEINFO,
	API_EMPLOYEE_VEHICLE_AUTH_DETAIL,
	API_EMPLOYEE_VEHICLE_AUTH_PARK,
	API_EMPLOYEE_VEHICLE_AUTH_APPLY,
	API_EMPLOYEE_VEHICLE_ADD,
	API_EMPLOYEE_VEHICLE_COLOR_TYPE,
	API_EMPLOYEE_VEHICLE_TYPE,
	API_EMPLOYEE_VEHICLE_DELETE,
	API_GUIDE_HELP_QUESTION_LIST,
	API_GUIDE_HELP_QUESTION_ANSWER,
	API_SETTING_SUGGEST,
	API_SETTING_ABOUTUS,
	API_SETTING_VERSION_CHECK,
	API_EMPLOYEE_ROOM_OUT,
	API_EMPLOYEE_OUT_ROMM_APPLY,
	API_EMPLOYEE_ALLOWANCE,
	API_EMPLOYEE_OUT_ROOM_DETAIL,
	API_SETTING_OLDPHONE_SEND,
	API_SETTING_OLDPHONE_VERIFY,
	API_SETTING_NEWPHONE_SEND,
	API_SETTING_NEWPHONE_CONFIRM,
	API_EMPLOYEE_OUT_ROMM_APPLY_DETAIL
} from '@/config/apis'
// 用户信息相关接口
// 员工
const employee = {
	//获取外宿详情
	getOutDetail() {
		return axios.get(API_EMPLOYEE_OUT_ROMM_APPLY_DETAIL)
	},
	// 获取员工基本信息
	baseInfo (id) {
		if (id) {
			return axios.get(`${API_EMPLOYEE_BASEINFO}?employeeId=${id}`)
		} else {
			return axios.get(API_EMPLOYEE_BASEINFO)
		}
		
	},
	// 获取员工完整信息
	fullInfo () {
		return axios.get(API_EMPLOYEE_FULLINFO)
	},
	// 紧急联系人修改
	relationUpdate (obj) {
		return axios.post(API_EMPLOYEE_RELATION_UPDATE,obj)
	},
	// 查看员工宿舍信息
	roomDetail () {
		return axios.post(API_EMPLOYEE_ROOM_DETAIL)
	},
	// 获取员工二维码
	qrcode () {
		return axios.get(API_EMPLOYEE_QRCODE)
	},
	// 员工申请内宿
	inRoomApply (obj) {
		return axios.post(API_EMPLOYEE_ROOM_APPLY,obj)
	},
	// 员工外宿协议
	outRoomAgrement (parkId) {
		return axios.get(`${API_EMPLOYEE_ROOM_OUT}?parkId=${parkId}`)
	},
	// 员工申请外宿
	applyOutRoom (obj) {
		return axios.post(API_EMPLOYEE_OUT_ROMM_APPLY, obj)
	},
	// 获取外宿补贴信息
	getOutRoomAllowance () {
		return axios.get(API_EMPLOYEE_ALLOWANCE)
	},
	// 获取员工外宿信息
	getOutRoomDetail () {
		return axios.get(API_EMPLOYEE_OUT_ROOM_DETAIL)
	}
} 

// 车辆
const vehicle = {
	// 获取车辆信息
	baseInfo (page) {
		return axios.get(`${API_EMPLOYEE_VEHICLE_BASEINFO}?current=${page.current}&size=${page.size}`)
	},
	authDetail (obj) {
		return axios.post(API_EMPLOYEE_VEHICLE_AUTH_DETAIL, obj)
	},
	// 获取车辆通行权限列表
	authParkList (obj) {
		return axios.post(API_EMPLOYEE_VEHICLE_AUTH_PARK, obj)
	},
	// 申请车辆通行权限
	authApply (obj) {
		return axios.post(API_EMPLOYEE_VEHICLE_AUTH_APPLY,obj)
	},
	// 添加车辆
	addCar (obj) {
		return axios.post(API_EMPLOYEE_VEHICLE_ADD,obj)
	},
	// 获取车辆颜色类型列表
	colorType () {
		return axios.get(API_EMPLOYEE_VEHICLE_COLOR_TYPE)
	},
	// 获取车辆类型列表
	carType () {
		return axios.get(API_EMPLOYEE_VEHICLE_TYPE)
	},
	// 移出车辆信息
	deleteCar(obj) {
		return axios.post(API_EMPLOYEE_VEHICLE_DELETE, obj)
	}
}

// 帮助中心
const helpCenter = {
	// 获取常见问题列表
	requestList (page) {
		return axios.get(`${API_GUIDE_HELP_QUESTION_LIST}?current=${page.current}&size=${page.size}`)
	},
	// 获取问题详情
	requestAnswer (questionId) {
		return axios.get(`${API_GUIDE_HELP_QUESTION_ANSWER}/${questionId}`)
	},
}

// 设置
const setting = {
	// 提交意见反馈
	suggest (obj) {
		return axios.post(API_SETTING_SUGGEST,obj)
	},
	// 关于我们
	aboutUs () {
		return axios.get(API_SETTING_ABOUTUS)
	},
	// 版本检查
	versionCheck (version) {
		return axios.get(`${API_SETTING_VERSION_CHECK}?version=${version}`)
	},
	// 向当前认证账号的旧手机号发送验证码，前端不再持有工号或旧手机号。
	sendOldPhoneCode () {
		return axios.post(API_SETTING_OLDPHONE_SEND)
	},
	// 验证旧手机号；成功状态由服务端绑定当前会话保存。
	verifyOldPhone (smsCode) {
		return axios.post(API_SETTING_OLDPHONE_VERIFY, { smsCode })
	},
	// 新手机号发送短信验证码，服务端会检查旧手机号验证状态。
	newPhoneSendCode (mobile) {
		return axios.post(API_SETTING_NEWPHONE_SEND, { mobile })
	},
	// 更新手机号码，服务端同时校验旧手机号授权和新手机号验证码。
	updataPhone (obj) {
		return axios.post(API_SETTING_NEWPHONE_CONFIRM, obj)
	}
}

export {
	employee,
	vehicle,
	helpCenter,
	setting
}

// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息 
import {
	API_APPLICATION_DIMISSION,
	API_APPLICATION_DIMISSION_REASON,
	API_APPLICATION_DIMISSION_TYPE,
	API_PROCESS_DIMISSION_GET,
	API_PROCESS_DIMISSION_RECORD_LIST,
	API_PROCESS_DIMISSION_RECORD_DETAIL,
	API_APPLICATION_APPROVE_DORM,
	API_APPLICATION_APPROVE_INSURANCE,
	API_APPLICATION_APPROVE_ATTENDANCE,
	API_APPREST_BALANCE_ANNUAL_GET,
	API_PROCESS_DIMISSION_RECORD_WORKHAND,
	API_APPLICATION_APPROVE_WORKHAND,
	API_APPLICATION_APPROVE_START,
	API_APPLICATION_APPROVE_COMMIT,
	API_APPLICATION_APPROVE_WORKHAND_SUBMIT
} from '@/config/apis'

// 导出 离职 接口信息
export default {
	// 发起离职申请
	applyDimission (obj) {
		return axios.post(API_APPLICATION_DIMISSION,obj)
	},
	// 获取离职原因列表
	dimissionReason (obj) {
		return axios.get(API_APPLICATION_DIMISSION_REASON,obj)
	},
	// 获取离职类型列表
	dimissionTypeList (obj) {
		return axios.get(API_APPLICATION_DIMISSION_TYPE,obj)
	},
	// 获取年假天数
	dimissionRestTime (obj) {
		return axios.get(API_APPREST_BALANCE_ANNUAL_GET,obj)
	},
	// 获取离职流程信息
	processDimission (obj) {
		return axios.post(API_PROCESS_DIMISSION_GET,obj)
	},
	// 获取员工离职记录
	dimissionRecordList (page,dimissionApplyType) {
		return axios.get(`${API_PROCESS_DIMISSION_RECORD_LIST}?current=${page.current}&size=${page.size}&dimissionApplyType=${dimissionApplyType}`)
	},
	// 查看员工离职记录详情
	dimissionRecordDetail (processId) {
		return axios.get(`${API_PROCESS_DIMISSION_RECORD_DETAIL}/${processId}`)
	},
	// 查看工作交接
	dimissionRecordWorkhand (processId) {
		return axios.get(`${API_PROCESS_DIMISSION_RECORD_WORKHAND}/${processId}`)
	},
	// 获取交接内容
	workHnadGet (processId) {
		return axios.get(`${API_APPLICATION_APPROVE_WORKHAND}/${processId}`)
	},
	// 宿管离职审批
	approveDorm (obj) {
		return axios.post(API_APPLICATION_APPROVE_DORM,obj)
	},
	// 社保员离职审批
	approveInsurance (obj) {
		return axios.post(API_APPLICATION_APPROVE_INSURANCE,obj)
	},
	// 考勤员离职审批
	approveAttendance (obj) {
		return axios.post(API_APPLICATION_APPROVE_ATTENDANCE,obj)
	},
	// 开始离职交接
	exchangeJobStart(processId) {
		return axios.get(`${API_APPLICATION_APPROVE_START}/${processId}`)
	},
	// 提交工作交接
	exchangeJobCommit(processId) {
		return axios.get(`${API_APPLICATION_APPROVE_COMMIT}/${processId}`)
	},
	// 确认工作交接
	workHandSubmit (obj) {
		return axios.post(API_APPLICATION_APPROVE_WORKHAND_SUBMIT, obj)
	}
}
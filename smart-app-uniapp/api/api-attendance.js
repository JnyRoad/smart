// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_APPLICATION_ATTENDANCE_LIST,
	API_APPLICATION_ATTENDANCE_DETAIL,
	API_APPLICATION_ATTENDANCEERROR_DETAIL,
	API_APPLICATION_ATTENDANCESUCCESS_DETAIL,
	API_APPLICATION_ATTENDANCE_PATCH_QUERY,
	API_APPLICATION_ATTENDANCE_PATCH_REASON,
	API_APPLICATION_ATTENDANCE_PATCH,
	API_PROCESS_ATTENDANCE_PATCH_RECORD_LIST,
	API_APPLICATION_ATTENDANCE_PATCH_PATCHCOUNT,
	API_APPLICATION_ATTENDANCE_RECORD_DETAIL,
	API_APPLICATION_ATTENDANCE_GETSKYPAY,
	API_APPLICATION_ATTENDANCE_MONTH_LIST,
	APP_AGREEMENT_SERVICE
} from '@/config/apis'

// 导出 考勤 接口信息
export default {
	// 考情列表
	list (obj) {
		return axios.post(API_APPLICATION_ATTENDANCE_LIST,obj)
	},
	// 考勤正常详情
	successDetail (obj) {
		return axios.post(API_APPLICATION_ATTENDANCESUCCESS_DETAIL,obj)
	},
	// 考勤异常详情
	errorDetail (obj) {
		return axios.post(API_APPLICATION_ATTENDANCEERROR_DETAIL,obj)
	},
	// 获取补卡信息
	patchQuery(obj) {
		return axios.post(API_APPLICATION_ATTENDANCE_PATCH_QUERY,obj)
	},
	// 获取补卡原因列表
	patchReason() {
		return axios.get(API_APPLICATION_ATTENDANCE_PATCH_REASON)
	},
	// 发起补卡申请
	patch(obj) {
		return axios.post(API_APPLICATION_ATTENDANCE_PATCH,obj)
	},
	// 获取补卡记录
	patchRecordList(page) {
		return axios.get(`${API_PROCESS_ATTENDANCE_PATCH_RECORD_LIST}?current=${page.current}&size=${page.size}`)
	},
	// 获取当月补卡次数
	patchCount(obj) {
		return axios.post(API_APPLICATION_ATTENDANCE_PATCH_PATCHCOUNT, obj)
	},
	// 获取补卡流程
	infoFlow(obj) {
		return axios.post("/app/process/attendance/record/infoFlow", obj)
	},
	// 补卡详情
	patchDetail (obj) {
		return axios.get(`${API_APPLICATION_ATTENDANCE_RECORD_DETAIL}?recordId=${obj.recordId}`)
	},
	// 考勤汇总
	patchSkyPay (obj) {
		return axios.post(API_APPLICATION_ATTENDANCE_GETSKYPAY, obj)
	},
	getMonth (obj) {
		return axios.post(API_APPLICATION_ATTENDANCE_MONTH_LIST, obj)
	},
	//app协议
	getService (parkId) {
		return axios.get(`${APP_AGREEMENT_SERVICE}?parkId=${parkId}`)
	}
}

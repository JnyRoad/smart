// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_APPLICATION_TRAVEL,
	API_PROCESS_TRAVEL_RECORD_LIST,
	API_PROCESS_TRAVEL_RECORD_DETAIL,
	API_PROCESS_TRAVEL_RECORD_INFODAY,
	API_PROCESS_TRAVEL_RECORD_INFOREPORT,
	API_PROCESS_TRAVEL_RECORD_INFOFLOW
} from '@/config/apis'

// 导出 出差 接口信息
export default {
	// 发起出差申请 （废弃）
	travelApply (obj) {
		return axios.post(API_APPLICATION_TRAVEL,obj)
	},
	// 获取出差记录
	travelRecordList (page,obj) {
		return axios.get(`${API_PROCESS_TRAVEL_RECORD_LIST}?current=${page.current}&size=${page.size}`,obj)
	},
	// 获取出差记录详情
	travelRecordDetail (recordId) {
		return axios.post(API_PROCESS_TRAVEL_RECORD_DETAIL,{recordId: recordId})
	},
	// 查看日程
	travelInfoDay (recordId) {
		return axios.post(API_PROCESS_TRAVEL_RECORD_INFODAY,{recordId: recordId})
	},
	// 查看出差报告
	travelInfoReport (recordId) {
		return axios.post(API_PROCESS_TRAVEL_RECORD_INFOREPORT,{recordId: recordId})
	},
	// 查看出差流程
	travelInfoFlow (recordId) {
		return axios.post(API_PROCESS_TRAVEL_RECORD_INFOFLOW,{recordId: recordId})
	}
}
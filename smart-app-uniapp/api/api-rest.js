// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_APPLICATION_REST,
	API_APPLICATION_REST_TYPE,
	API_REST_BALANCE_ADJUST_GET,
	API_PROCESS_REST_RECORD_LIST,
	API_PROCESS_REST_RECORD_DETAIL
} from '@/config/apis'

// 导出 调休 接口信息
export default {
	// 发起调休申请
	restApply (obj) {
		return axios.post(API_APPLICATION_REST,obj)
	},
	// 获取休假类型列表
	restType () {
		return axios.get(API_APPLICATION_REST_TYPE)
	},
	// 获取可调休天数
	restDay (obj) {
		return axios.get(API_REST_BALANCE_ADJUST_GET, obj)
	},
	// 获取调休记录
	restRecordList (page) {
		return axios.get(`${API_PROCESS_REST_RECORD_LIST}?current=${page.current}&size=${page.size}`)
	},
	// 查看调休记录详情
	restRecordDetail (obj) {
		return axios.post(API_PROCESS_REST_RECORD_DETAIL,obj)
	}
}
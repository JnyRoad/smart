// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_APPLICATION_VACATE,
	API_APPLICATION_VACATE_TYPE,
	API_PROCESS_VACATE_RECORD_LIST,
	API_PROCESS_VACATE_RECORD_DETAIL,
	API_APPLICATION_CLASS_QUERY,
	API_APPLICATION_VACATE_UNIT
} from '@/config/apis'

// 导出 请假 接口信息
export default {
	// 发起请假申请
	vacateApply (obj) {
		return axios.post(API_APPLICATION_VACATE,obj)
	},
	// 获取请假假类型列表
	vacateType () {
		return axios.get(API_APPLICATION_VACATE_TYPE)
	},
	// 获取请假记录
	vacateRecordList (page) {
		return axios.get(`${API_PROCESS_VACATE_RECORD_LIST}?current=${page.current}&size=${page.size}`)
	},
	// 查看请假记录详情
	vacateRecordDetail (obj) {
		return axios.post(API_PROCESS_VACATE_RECORD_DETAIL,obj)
	},
	// 查看班次信息
	classQuery (obj) {
		return axios.post(API_APPLICATION_CLASS_QUERY,obj)
	},
	// 获取请假时长单位
	vacateUnit (vacateCode) {
		return axios.get(`${API_APPLICATION_VACATE_UNIT}?vacateCode=${vacateCode}`, )
	}
}

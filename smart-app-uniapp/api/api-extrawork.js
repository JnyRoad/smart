// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_APPLICATION_EXTRAWORK,
	API_APPLICATION_EXTRAWORK_CLASS_TYPE,
	API_PROCESS_EXTRAWORK_RECORD_LIST,
	API_PROCESS_EXTRAWORK_RECORD_DETAIL,
	API_APPLICATION_EXTRAWORK_TYPE
} from '@/config/apis'

// 导出 加班 接口信息
export default {
	// 发起加班申请
	extraworkApply (obj) {
		return axios.post(API_APPLICATION_EXTRAWORK,obj)
	},
	// 加班申请加班类别
	extraworkClassType () {
		return axios.get(API_APPLICATION_EXTRAWORK_CLASS_TYPE)
	},
	// 加班类型
	extraworkType () {
		return axios.get(API_APPLICATION_EXTRAWORK_TYPE)
	},
	// 获取加班记录
	extraworkRecordList (page) {
		return axios.get(`${API_PROCESS_EXTRAWORK_RECORD_LIST}?current=${page.current}&size=${page.size}`)
	},
	// 查看加班记录详情
	extraworkRecordDetail (obj) {
		return axios.post(API_PROCESS_EXTRAWORK_RECORD_DETAIL,obj)
	}
}
// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_PROCESS_PREVIEW,
	API_APPROVE_RECORD,
	API_APPLICATION_APPROVE_NORMAL
} from '@/config/apis'

// 导出 审批 接口信息
export default {
	// 预览审批流程
	processPreview (obj) {
		return axios.post(`${API_PROCESS_PREVIEW}/obj`)
	},
	// 待审批离职请求列表
	approveRecord (obj) {
		return axios.post(API_APPROVE_RECORD,obj)
	},
	// 普通审批操作
	approveNormal (obj) {
		return axios.post(API_APPLICATION_APPROVE_NORMAL,obj)
	}
}
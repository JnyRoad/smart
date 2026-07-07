// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_VISIT_LIST,
	API_VISIT_REASON_LIST,
	API_VISIT_DETAIL,
	API_VISIT_ADD,
	API_VISIT_MEMBER_LIST,
	API_VISIT_MEMBER_ADD,
	API_VISIT_APPROVE,
	APT_VISTI_APPROVALCOUNT
} from '@/config/apis'

// 导出 访客预约 接口信息
import qs from 'qs'
export default {
	// 获取来访预约列表
	visitList (page,visitListType) {
		return axios.post(`${API_VISIT_LIST}?current=${page.current}&size=${page.size}`,{visitListType: visitListType})
	},
	// 来访者是由列表
	visitReasonList () {
		return axios.get(API_VISIT_REASON_LIST)
	},
	// 查看来访预约详情
	visitDetail (obj) {
		return axios.post(API_VISIT_DETAIL,obj)
	},
	// 添加访客预约
	visitAdd (obj) {
		return axios.post(API_VISIT_ADD,obj)
	},
	// 添加随行人员
	memberAdd(obj) {
		return axios.post(API_VISIT_MEMBER_ADD,obj)
	},
	// 预约审批
	visitApprove (obj) {
		return axios.post(API_VISIT_APPROVE,obj)
	},
	// 查看随行人员列表
	memberList (obj) {
		return axios.post(API_VISIT_MEMBER_LIST,obj)
	},
	// 待我审批的数量
	approvalNum () {
		return axios.get(APT_VISTI_APPROVALCOUNT)
	}
}
// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_RECRUIT_LIST,
	API_RECRUIT_DETAIL,
	API_RECRUIT_UPDATE
} from '@/config/apis'

// 导出 职位管理 接口信息
export default {
	// 获取招聘岗位列表
	recruitList (obj) {
		return axios.post(API_RECRUIT_LIST,obj)
	},
	// 查看岗位招聘信息详情
	recruitDetail (obj) {
		return axios.post(API_RECRUIT_DETAIL,obj)
	},
	// 修改岗位招聘信息
	recruitUpdate (obj) {
		return axios.post(API_RECRUIT_UPDATE,obj)
	}
}
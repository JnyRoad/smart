// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_INFORMATION_FILE_LIST,
	API_INFORMATION_FILE_DETAIL
} from '@/config/apis'

// 导出 新员工须知 接口信息
export default {
	// 获取资料列表
	informationList (page, parkId) {
		return axios.get(`${API_INFORMATION_FILE_LIST}/${parkId}?current=${page.current}&size=${page.size}`)
	},
	// 获取资料详情
	informationDetail(noteId) {
		return axios.get(`${API_INFORMATION_FILE_DETAIL}/${noteId}`)
	}
	
}
import axios from '@/tools/request'
import {
	API_APPSERVICE_MODULE_LIST,
	API_MESSAGE_DELETE
} from '@/config/apis'
// 导出 引导页 接口信息
export default {
	// 获取模块菜单信息
	getSeriveList() {
		return axios.get(API_APPSERVICE_MODULE_LIST)
	},
	// 删除消息
	getDelete(obj) {
		console.log(obj);
		return axios.get(`${API_MESSAGE_DELETE}?recordId=${obj.recordId}`)
	}
}
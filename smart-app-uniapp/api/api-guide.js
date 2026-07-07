import axios from '@/tools/request'
import {
	API_GUIDE_WELECOME,
	API_GUIDE_OPERATION
} from '@/config/apis'
// 导出 引导页 接口信息
export default {
	// 获取欢迎页内容
	getWelcomeInfo(obj) {
		return axios.get(API_GUIDE_WELECOME,obj)
	},
	// 获取引导操作内容
	getOperation(obj) {
		return axios.post(API_GUIDE_OPERATION,obj)
	}
}
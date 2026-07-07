// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_PLUS_NEWS_LIST,
	API_PLUS_NEWS_CHANGE,
	API_PLUS_NEW_COUNT,
	API_PLUS_ALL_READ,
	API_PLUS_ALL_DETELE
} from '@/config/apis'

// 消息推送
export default {
	getPlusList (page, deviceNo) {
		return axios.get(`${API_PLUS_NEWS_LIST}?deviceNo=${deviceNo}&current=${page.current}&size=${page.size}`)
	},
	// 修改消息状态
	changePlusNews (recordId) {
		return axios.get(`${API_PLUS_NEWS_CHANGE}?recordId=${recordId}`)
	},
	// 获取消息数量
	plusNewsCount (deviceNo) {
		return axios.get(`${API_PLUS_NEW_COUNT}?deviceNo=${deviceNo}`)
	},
	readNewsAll (deviceNo) {
		return axios.get(`${API_PLUS_ALL_READ}?deviceNo=${deviceNo}`)
	},
	deteleNewsAll (deviceNo) {
		return axios.get(`${API_PLUS_ALL_DETELE}?deviceNo=${deviceNo}`)
	}
}
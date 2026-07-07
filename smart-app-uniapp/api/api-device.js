// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_DEVICE_REGISTER
} from '@/config/apis'

export default {
	postDevice (obj) {
		return axios.post(API_DEVICE_REGISTER, obj)
	}
}
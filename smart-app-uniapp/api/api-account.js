// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_LOGIN,
	API_LOGIN_FACE,
	API_REFRESH_TOKEN,
	API_LOGIN_OUT,
	API_EMPLOYEE_GETSALAYTYPE,
	API_PREFECT
} from '@/config/apis'

// 导出 登录 接口信息
export default {
	// 常规的用户密码登录
	login (obj) {
		return axios.post(`${API_LOGIN}?username=${obj.username}&password=${obj.password}&randomStr=${obj.randomStr}&grant_type=password&scope=server`)
	},
	// 薪资计算类型
	getSalayType (obj) {
		return axios.get(`${API_EMPLOYEE_GETSALAYTYPE}/${obj.badge}`)
	},
	// 刷脸操作
	loginFace (facePhoto, deviceNo) {
		return axios.post(`${API_LOGIN_FACE}?grant_type=ocr`, {facePhoto: facePhoto, deviceNo: deviceNo})
	},
	// 刷新token
	refreshToken (obj) {
		return axios.post(`${API_REFRESH_TOKEN}?refresh_token=${obj.refresh_token}&grant_type=refresh_token&scope=server`)
	},
	// 退出登录
	doLoginOut () {
		return axios.delete(API_LOGIN_OUT)
	},
	// token
	getToken () {
		return axios.get("/app/icbc/eaccount")
	}
}

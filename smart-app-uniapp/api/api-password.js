// 导入请求封装函数
import axios from '@/tools/request'

// 按需引入所需对应地址信息
import {
	API_SMS_SEND,
	API_SMS_VERIFY,
	API_PASSWORD_UPDATE,
	API_PASSWORD_MOBILE_QUERY,
	API_PASSWORD_VERIFY_FACE
} from '@/config/apis'

// 导出 密码找回 接口信息
export default {
	// 发送短息验证码
	sendSms (badge) {
		return axios.get(`${API_SMS_SEND}?badge=${badge}`)
	},
	// 校验短信验证码
	verifySms (obj) {
		return axios.get(`${API_SMS_VERIFY}?smsCode=${obj.smsCode}&badge=${obj.badge}`)
	},
	// 设置密码
	updatePassword (obj) {
		return axios.put(`${API_PASSWORD_UPDATE}?username=${obj.username}&password=${obj.password}&updateAuthCode=${obj.updateAuthCode}`)
	},
	// 通过工号获取手机号码
	mobileQuery (badge) {
		return axios.get(`${API_PASSWORD_MOBILE_QUERY}?badge=${badge}`)
	},
	// 通过人脸修改密码
	verifyFaceToPassword (face, deviceNo) {
		return axios.post(`${API_PASSWORD_VERIFY_FACE}`,{facePhoto: face, deviceNo: deviceNo})
	}
}

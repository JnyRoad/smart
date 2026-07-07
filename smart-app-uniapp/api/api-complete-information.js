// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_OCRR_IDENTIFICATION,
	API_OCR_FACE,
	API_PERFECT_CHECK_FACE,
	API_FACE_UPDATE,
	API_PREFECT
} from '@/config/apis'

// 导出 完善信息 接口信息
export default {
	// 身份证OCR识别
	identification (obj) {
		return axios.post(API_OCRR_IDENTIFICATION,obj)
	},
	// 人脸与身份证对比
	verifyFace (obj) {
		return axios.post(API_OCR_FACE,obj)
	},
	// 信息完整性检测
	checkFace() {
		return axios.get(API_PERFECT_CHECK_FACE)
	},
	// 更新用户头像
	updateAvatar (obj) {
		return axios.post(API_FACE_UPDATE, obj)
	},
	getpre (obj) {
		return axios.post(API_PREFECT, obj)
	}
}
// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_APPLICATION_LIST,
	API_APPLICATION_DETAIL,
	API_APPLICATION_OPERATION,
	API_APPLICATION_RECORD,
	API_APPLICATION_FACE_LIST,
	API_APPLICATION_OTPTTYPE_LIST
} from '@/config/apis'

// 导出 招聘管理 接口信息
export default {
	// 获取简历列表
	applicationList (page,obj) {
		return axios.post(`${API_APPLICATION_LIST}?current=${page.current}&size=${page.size}`,obj)
	},
	// 查看简历详情
	applicationDetail (obj) {
		return axios.post(API_APPLICATION_DETAIL,obj)
	},
	// 简历筛选操作
	applicationOperation (obj) {
		return axios.post(API_APPLICATION_OPERATION,obj)
	},
	// 查询应聘记录
	applicationRecord (obj) {
		return axios.post(API_APPLICATION_RECORD,obj)
	},
	// 人脸搜索简历
	applicationFaceList (obj) {
		return axios.post(API_APPLICATION_FACE_LIST,obj)
	},
	// 招聘管理状态列表
	optoTypeList () {
		return axios.get(API_APPLICATION_OTPTTYPE_LIST)
	}
}
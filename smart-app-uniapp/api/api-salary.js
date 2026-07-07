// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_SALARY_LIST,
	API_SALARY_DETAIL,
	API_WAGE_SIGN_SAVE
} from '@/config/apis'

// 导出 工资 相关接口信息
export default {
	// 获取工资条列表
	list (page,obj) {
		return axios.post(`${API_SALARY_LIST}?current=${page.current}&size=${page.size}`,obj)
	},
	// 查看工资条详情
	detail (obj) {
		return axios.post(API_SALARY_DETAIL,obj)
	},
	// 工资签收
	signSave(obj) {
		return axios.post(API_WAGE_SIGN_SAVE,obj)
	}
}
// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_CONSUME_RECORD_COUNT,
	API_CONSUME_RECORD_LIST
} from '@/config/apis'

// 导出 登录 接口信息
export default {
	// 消费记录总金额
	consumeCount ( obj ) {
		return axios.post(API_CONSUME_RECORD_COUNT, obj)
	},
	// 消费记录列表
	consumeList ( obj ) {
		return axios.post(`${API_CONSUME_RECORD_LIST}?current=${obj.current}&size=${obj.size}`, {'queryDate' : obj.queryDate, 'acctType' : obj.acctType})
	}
}

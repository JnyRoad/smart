// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_LOCATION_AUTO,
	API_PARK_LIST
} from '@/config/apis'

let defaultLat = 22.928319 // 定位失败 默认大岭山 纬度
let defaultLong = 113.799832 // 定位失败 默认大岭山 经度

// 导出 定位 接口信息
export default {
	// 经纬度定位
	getLocation (obj,type) {
		if (type == 1) { // 定位成功 定位地址
			return axios.post(API_LOCATION_AUTO, obj)
		} else { // 定位失败 默认大岭山
			const defaultLoaction = {
				longitude: defaultLong,
				latitude: defaultLat
			}
			return axios.post(API_LOCATION_AUTO, defaultLoaction)
		}
		
	},
	// 获取园区列表
	getParkList (obj) {
		return axios.post(`${API_PARK_LIST}?current=${obj.current}&size=${obj.size}`)
	}
	
}

// 导入请求封装函数
import axios from '@/tools/request'
// 按需引入所需对应地址信息
import {
	API_HOME_BANNER,
	API_HOME_MENU,
	API_HOME_BBS_LIST,
	API_HOME_BBS_DETAIL,
	API_HOME_NEWS_LIST,
	API_HOME_NEWS_DETAIL,
	API_HOME_PARK_GENERRAL_LIST,
	API_HOME_PARK_GENERRAL_DETAIL,
	API_HOME_ACTIVITY_LIST,
	API_HOME_ACTIVITY_DETAIL,
	API_HOME_INSTRODUCE_LIST,
	API_HOME_INSTRODUCE_DETAIL,
	API_HOME_CULTURE_LIST,
	API_HOME_CULTURE_DETAIL
} from '@/config/apis'

// 导出 首页 接口信息
export default {
	// 获取 banner 列表
	banner (parkId) {
		return axios.get(`${API_HOME_BANNER}?parkId=${parkId}`)
	},
	// 获取首页导航菜单列表
	menu (parkId) {
		return axios.get(`${API_HOME_MENU}?parkId=${parkId}`)
	},
	// 获取公告列表
	bbsList (page,parkId) {
		return axios.get(`${API_HOME_BBS_LIST}?current=${page.current}&size=${page.size}&parkId=${parkId}`)
	},
	// 查看公告详情
	bbsDetail (id) {
		return axios.get(`${API_HOME_BBS_DETAIL}/${id}`)
	},
	// 获取新闻列表
	newsList (page, parkId) {
		return axios.get(`${API_HOME_NEWS_LIST}?current=${page.current}&size=${page.size}&parkId=${parkId}`)
	},
	// 查看新闻详情
	newsDetail (newsId) {
		return axios.get(`${API_HOME_NEWS_DETAIL}/${newsId}`)
	},
	// 概况列表
	generalList (page, parkId) {
		return axios.get(`${API_HOME_PARK_GENERRAL_LIST}?current=${page.current}&size=${page.size}&parkId=${parkId}`)
	},
	// 概况详情
	generalDetail (generalId) {
		return axios.get(`${API_HOME_PARK_GENERRAL_DETAIL}/${generalId}`)
	},
	// 园区活动列表
	activityList (page, parkId) {
		return axios.get(`${API_HOME_ACTIVITY_LIST}?current=${page.current}&size=${page.size}&parkId=${parkId}`)
	},
	// 园区活动详情
	activityDetail (activityId) {
		return axios.get(`${API_HOME_ACTIVITY_DETAIL}/${activityId}`)
	},
	// 裕同集团简介列表
	instroduceList (page, parkId) {
		return axios.get(`${API_HOME_INSTRODUCE_LIST}?current=${page.current}&size=${page.size}&parkId=${parkId}`)
	},
	// 裕同集团简介详情
	instroduceDetail (instroduceId) {
		return axios.get(`${API_HOME_INSTRODUCE_DETAIL}/${instroduceId}`)
	},
	// 裕同文化列表
	cultureList (page, parkId) {
		return axios.get(`${API_HOME_CULTURE_LIST}?current=${page.current}&size=${page.size}&parkId=${parkId}`)
	},
	// 裕同文化详情
	cultureDetail (cultureId) {
		return axios.get(`${API_HOME_CULTURE_DETAIL}/${cultureId}`)
	}
}
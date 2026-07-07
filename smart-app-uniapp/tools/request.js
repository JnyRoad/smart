import Vue from 'vue'
import {
	USER_TOKEN
} from '@/tools/storage'


const that = new Vue() // 此定义完全是为了调用全局的自定义动画
// 导入大佬封装好的请求插件 详情请查看 node_modules 的参数
import axios from 'uni-request'
// 环境配置：服务器地址统一来源，禁止在此写死域名（切换环境见 config/env.js）
import env from '@/config/env.js'
// 定义请求的全局api路径
axios.defaults.baseURL = env.API_BASE_URL
axios.defaults.headers['Content-Type'] = 'application/json'
// 请求超时
axios.defaults.timeout = 30000
// 定义请求次数
let requestUrl = ''
// 请求拦截器
axios.interceptors.request.use(request => {
	requestUrl = request.url
	that.$loading(true)
	// 判断判断是否有登录信息
	// 注意：此处严禁打印 token，登录令牌不能进设备日志
	let token = uni.getStorageSync(USER_TOKEN)
	if (requestUrl.indexOf('/auth/oauth/token') !== -1 || requestUrl.indexOf('/auth/ocr/token/face') !== -1) {
		request.headers.common['Authorization'] = 'Basic c21hcnQ6c21hcnQ='
		request.headers.common['TENANT_ID'] = '1'
	} else {
		if (token) {
			request.headers.common['Authorization'] = `Bearer ${token}`
		}
	}
	// 一下代码为处理将数组类型数据发送json对象被转换成了对象数据导致报错
	let temObj = {}
	let arr = []
	for (let el in request.data) {
		if (JSON.stringify(temObj) === '{}') {
			if (Object.prototype.toString.call(request.data[el]) == '[object Object]') {
				temObj = request.data[el]
				Object.keys(temObj).forEach(el => {
					arr.push(temObj[el])
				})
				request.data[el] = arr
			}
		}
	}
	if (!request.url) {
		that.$loading(false)
	}
	return request
}, err => {
	console.log(err);
	that.$loading(false)
	return Promise.reject(err)
})

setTimeout(function() {
	if (that.$store.state.showLoading) {
		console.log(that.$store.state.showLoading);
		that.$loading(false)
	}
},20000)

// 响应拦截器
axios.interceptors.response.use(response => {
	that.$loading(false)
	if (requestUrl.indexOf('/auth/oauth/token') !== -1) {
		return response
	}
	if (requestUrl.indexOf('/app/icbc/eaccount') !== -1) {
		return response
	}
	if (requestUrl.indexOf('/auth/ocr/token/face') !== -1) {
		if (response.status === 200) {
			return response
		} else {
			that.$ytHint.toast({
				title: response.data.msg,
				duration: 1500
			})
		}
		return
	}
	if (response.status === 200) {
		if (response.data.code === 0) {
			return response
		} else if (response.data.code === 1) {
			that.$ytHint.toast({
				title: JSON.stringify(response.data),
				duration: 1500
			})
		}
	} else if (response.status === 500) {
		that.$ytHint.toast({
			title: response.data.msg,
			duration: 1500
		})
	} else if (response.status === 401 || response.status === 426) {
		if (that.$store.state.restart) return
		that.$store.commit('restart',true)
	}
	
}, err => {
	that.$loading(false)
	if (err.errMsg === 'request:fail timeout') {
		that.$ytHint.toast({
			titie: '网络请求超时'
		})
		that.$loading(false)
	}
	return Promise.reject(err)
})

export default axios

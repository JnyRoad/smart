// 未使用
import ytHint from '@/config/handling.js'

// 检查用户网络状态
export default {
	checkNetwork() {
		uni.getNetworkType({
			success: res => {
				if (res.networkType === 'none') {
					ytHint.toast({
						title: '网络连接失败,请检查您的网络'
					})
				}
			}
		})
	},
	// 给用户一个弹窗提示带震动
	vibrate (obj) {
		uni.vibrateLong({
		    success: () => {
		        ytHint.toast({
					title: obj.title
				})
		    }
		})
	}
}
import {storage} from '@/tools/storage.js'
const restart = {
	// #ifdef APP-PLUS || H5
	onShow () {
		const isNetErrPage = storage.getSync('isNetErrPage')
		if (isNetErrPage != 'true') {
			this.checkResponse()
		}
	},
	// #endif
	methods: {
		// 页面显示先看有没有连上网
		checkResponse() {
			uni.getNetworkType({
				success: res => {
					if (res.networkType === 'none') {
						storage.set('isNetErrPage','true')
						setTimeout(()=> {
							uni.reLaunch({
								url: '/pages/page/neterr/neterr'
							})
						},200)
					} else if (res.networkType === '2g') {
						uni.showToast({
							title: '您当前网络环境较差',
							icon: 'none'
						})
					} else {
						if (this.$store.state.restart) {
							uni.removeStorageSync('isNetErrPage')
							setTimeout(() => {
								uni.reLaunch({
									url: '/pages/page/logins/login/login'
								})
							},200)
						}
					}
				}
			})
			
		},
		
	}
}
export default restart
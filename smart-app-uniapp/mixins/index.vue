<!-- 混入 解决在小程序端各类机型 刘海屏导航栏的高度问题 -->
<script>
export default {
	data () {
		return {
			isIOS: true,
			titleHeight: 0,
			statusBarHeight: 0
		}
	},
	methods: {
		// #ifdef MP-WEIXIN || H5
		getTitleHeight() {
			uni.getSystemInfo({
				success: res => {
					this.statusBarHeight = res.statusBarHeight
					if (res.system.indexOf('Android') !== -1) {
						this.titleHeight = 48 + this.statusBarHeight
						this.isIOS = false
					} else if (res.system.indexOf('iOS') !== -1) {
						this.titleHeight = 44 + this.statusBarHeight
						this.isIOS = true
					}
				}
			})
		},
		// #endif
		// #ifdef APP-PLUS
		getTitleHeight() {
			const res = uni.getSystemInfoSync()
			const system = res.platform
			this.statusBarHeight = res.statusBarHeight
			if (system === 'android') {
				this.titleHeight = 48 + this.statusBarHeight
				this.isIOS = false
			} else if (system === 'ios') {
				this.titleHeight = 44 + this.statusBarHeight
				this.isIOS = true
			}
		}
		// #endif
	}
}
</script>

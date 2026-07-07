<template>
	<view class="mw bgf">
		<view class="vcc mt100">
			<image class="w490 h410" src="/static/img/mine/net-err.png" mode=""></image>
			<text class="f32pc4 mt30">网络出现问题，请确认网络是否接入</text>
		</view>
		<view @click="relink"  class="hcc mw mt80">
			<view class="yt-btn hcc cf f32">重新连接</view>
		</view>
	</view>
</template>

<script>
	export default {
		onLoad() {
			this.$loading(false)
		},
		methods: {
			relink() {
				uni.getNetworkType({
					success: res => {
						if (res.networkType === 'none') {
							this.$ytHint.toast({
								title: '网络连接失败,请检查您的网络'
							})
						} else {
							this.$ytHint.toast({
								title: '网络连接成功'
							})
							this.$loading(false)
							uni.removeStorageSync('isNetErrPage')
							setTimeout(() => {
								uni.reLaunch({
									url: '/pages/page/logins/login/login'
								})
							},1500)
						}
					}
				})
			}
		},
	}
</script>

<style lang="scss" scoped>
	.w490 {
		width: 490upx;
	}
	.h410 {
		height: 410upx;
	}
	.yt-btn {
		width: 360upx;
		height: 108upx;
		border-radius: 54upx;
		background-color: $main-color;
		color: #fff;
	}
</style>

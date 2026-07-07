<template>
	<view class="yt-page bgf">
		<view class="vcc success mw">
			<image class="w160 h160 mb30" src="/static/img/serive/recruit/Invitation.png"></image>
			<text class="f32pc2">提交离职交接书成功</text>
		</view>
		<view class="vcc mw mt80">
			<yt-button @click="feedBack" btnText="返回离职记录列表" />
			<view class="w100 h40"></view>
			<yt-button navUrl="/pages/page/tabbar/home/home" btnText="返回首页" btnBgColor="#fff" btnTextColor="#508BFF"  />
		</view>
	</view>
</template>

<script>
	import ytButton from '@/components/yt-button/index.vue';
	export default {
		components: {
			'yt-button': ytButton
		},
		data() {
			return {
				title: '',
			}
		},
		onLoad(e) {
			
			uni.setNavigationBarTitle({
				title: '提交成功'
			})
		},
		methods: {
			feedBack() {
				uni.redirectTo({
					url: './leave-record?type=0'
				})
			}
		},
	}
</script>

<style scoped>
	.success {
		margin-top: 200upx;
	}
</style>

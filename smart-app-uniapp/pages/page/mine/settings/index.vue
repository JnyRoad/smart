<template>
	<view class="yt-page">	
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item navUrl="./userXie" showImage=false title="APP用户使用服务协议"></yt-list-item>
			<yt-list-item navUrl="./account-safe" showImage=false title="账户与安全"></yt-list-item>
			<!-- <yt-list-item navUrl="./opinion-feedback" showImage=false title="意见与反馈"></yt-list-item> -->
			<yt-list-item navUrl="./about-us" hideLastBorder=true showImage=false title="关于我们"></yt-list-item>
		</yt-list>
		<view @click="loginout" class="hcc mt80">
			<yt-button btnText="退出当前账号" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue';
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import account from '@/api/api-account.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-button': ytbutton
		},
		methods: {
			loginout() {
				account.doLoginOut().then(res => {
					if (res.data.data) {
						this.$ytHint.toast({
							title: '退出登录成功'
						})
						uni.reLaunch({
							url: '/pages/page/logins/login/login'
						})
					} else {
						this.$ytHint.toast({
							title: '退出登录失败'
						})
					}
				})
				
			}
		},
	}
</script>


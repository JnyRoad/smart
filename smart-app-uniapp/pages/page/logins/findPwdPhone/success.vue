<template>
	<view class="vcc mw bgf">
	    <image class="w160 h160" src="/static/img/login/set.png" alt=""></image>
	    <p class="success ">设置成功</p>
	    <view class="hcc mw mt80 mb120" @click="cancelBack"><yt-button  btnText="返回登录" btnWidth="180" btnHeight="54"></yt-button></view>
	</view>
</template>

<script>
	import ytButton from '@/components/yt-button/index.vue'
	export default {
		components: {
			'yt-button': ytButton
		},
		methods: {
			cancelBack () {
				uni.reLaunch({
					url: '../login/login'
				})
			}
		},
	}
</script>

<style>
</style>

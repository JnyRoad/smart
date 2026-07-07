<template>
	<view @touchmove.stop.prevent="moveHandle" class="fixed mw mh bottom0 left-50 guide">
		<image @click="tomine" class="w542 h350 abs image1" src="/static/img/mine/mine-guide1.png" mode=""></image>
		<view @click="toMydorm" class="box abs"></view>
	</view>
</template>

<script>
	// 初次打开页面的引导页
	export default {
		methods: {
			toMydorm() {
				this.$emit('click')
			}
		},
	}
</script>

<style scoped>
	.guide {
		z-index: 999;
		background-color: rgba(0, 0, 0, .6);
	}
	.w542 {
		width: 542upx;
	} 
	.h350 {
		height: 350upx;
	}
	.box {
		width: 690upx;
		height: 120upx;
		background-color: rgba(255, 255, 255, 0.4);
		border-radius: 20upx;
		top: 570upx;
		left: 0;
		right: 0;
		margin: 0 auto;
	}
	.image1 {
		top: 760upx;
		left: 0;
		right: 0;
		margin: 0 auto;
	}
</style>

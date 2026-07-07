<template>
	<view @touchmove.stop.prevent="moveHandle" class="fixed mw mh bottom0 left-50 guide">
		<image @click="tomine" class="w542 h350 abs image1" src="/static/img/index/index-guide1.png" mode=""></image>
		<image class="w206 h308 abs image2" src="/static/img/index/index-guide2.png" mode=""></image>
		<view class="f32 abs cf text">点击我的</view>
	</view>
</template>

<script>
	// 初次打开页面的引导页
	export default {
		name: 'guide-index',
		methods: {
			// 阻止蒙版下面页面内容滚动
			moveHandle() {
				return
			},
			tomine() {
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
	.w206 {
		width: 206upx;
	}
	.h308 {
		height: 308upx;
	}
	.image1 {
		/* #ifdef H5 */
		 bottom: 440upx;
		 /* #endif */
		/* #ifdef APP-PLUS */
		 bottom: 340upx;
		/* #endif */
		
		left: 30upx;
	}
	.image2 {
		/* #ifdef H5 */
		 bottom: 140upx;
		 /* #endif */
		/* #ifdef APP-PLUS */
		 bottom: 40upx;
		/* #endif */
		right: 180upx;
	}
	.text {
		right: 30upx;
		/* #ifdef H5 */
		 bottom: 100upx;
		 /* #endif */
		/* #ifdef APP-PLUS */
		 bottom: 0;
		/* #endif */
	}
</style>

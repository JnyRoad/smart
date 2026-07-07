<template>
	<view class="shareit-main rel">
		<view class="abs mw mh secvice-bg">
			<image src="/static/img/mine/service-bg.png" class="mw mh"></image>
		</view>
		<view class="sancode">
			<image src="/static/img/mine/fenxianma.png" class="sancode-image"></image>
			<text class="sancode-text">扫描二维码,下载APP</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				
			};
		}
	}
</script>

<style lang="scss">
	page {
		width: 100%;
		height: 100%;
	}
	.shareit-main {
		width: 100%;
		height: 100%;
	}
	.secvice-bg {
		z-index: -1;
	}
	.sancode{
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
	}
	.sancode-image {
		margin-top: 50upx;
		width: 400upx;
		height: 400upx;
	}
	.sancode-text {
		color: #fff;
		font-size:30upx;
		margin-top:20upx;
	}
</style>

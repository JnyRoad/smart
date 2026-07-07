<template>
	<view class="shareit-main rel">
		<view class="abs mw mh secvice-bg">
			<view class="sancode">
				<image :src="qrcode" class="sancode-image"></image>
				<text class="sancode-text">扫描通行</text>
				<text class="sancode-text-2">请在支持二维码扫描的设备下使用,如园区门口的闸机</text>
			</view>
			<text class="tishi">使用须知: 请将二维码正对设备屏幕</text>
		</view>
		<image src="/static/img/mine/saomajinmen.png" class="mw mh bg-image"></image>
	</view>
</template>

<script>
import { employee } from '@/api/api-mine.js'; // 请求接口的参数
export default {
	data() {
		return {
			qrcode: ''
			// screenLight: 0,
		};
	},
	onLoad() {
		this.getQRcode();
		// // #ifdef APP-PLUS
		// uni.getScreenBrightness({
		// 	success: res => {
		// 		this.screenLight = res.value
		// 	}
		// });
		// uni.setScreenBrightness({
		// 	value: 1
		// });
		// // #endif
	},
	onUnload() {
		// // #ifdef APP-PLUS
		// uni.setScreenBrightness({
		// 	value: this.screenLight
		// });
		// // #endif
	},
	methods: {
		async getQRcode() {
			const res = await employee.qrcode();
			this.qrcode = res.data.data.qrcode;
		}
	}
};
</script>

<style lang="scss" scoped>
	page {
		width: 100%;
		height: 100%;
	}
	.shareit-main {
		width: 100%;
		height: 100%;
	}
	.secvice-bg {
		z-index: 1;
		width: 100%;
		height: 100%;
	}
	.sancode{
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		// z-index: 999;
	}
	.bg-image {
		position: fixed;
		top: 0;
		left: 0;
		bottom: 0;
		right: 0;
	}
	.sancode-image {
		margin-top: 180upx;
		width: 400upx;
		height: 400upx;
	}
	.sancode-text {
		font-size:34upx;
		font-weight: 600;
		margin-top:10upx;
	}
	.sancode-text-2 {
		font-size:30upx;
		color: #aaa;
		display: inline-block;
		width: 400upx;
		text-align: center;
	}
	.tishi{
		position: fixed;
		left: 20%;
		bottom: 15%;
		color: #aaa;
		z-index: 1000;
	}
</style>

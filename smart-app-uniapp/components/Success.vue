<template>
	<view class="vcc">
		<img class="wh162 mt120" :src="icon" alt="">
		<p class="success mb80">{{successText}}</p>
		<slot></slot>
		<view class="btn-row mt200">
			<yt-button @click="onClick" btnWidth="180" btnHeight="54" :btnText="btnText"></yt-button>
		</view>
	</view>
</template>
<script>
	// 处理成功页面
	import ytbutton from '@/components/yt-button/index.vue';
	export default {
		components: {
			'yt-button': ytbutton
		},
		props: {
			icon: {
				type: String,
				default: ''
			},
			btnUrl: {
				type: String,
				default: ''
			},
			successText: {
				type: String,
				default: ''
			},
			btnText: {
				type: String,
				default: ''
			}
		},
		data() {
			return {
				
			}
		},
		methods: {
			onClick() {
				this.$emit('click')
			}
		}
	}
</script>

<style scope>
.wh162 {
	width: 162upx;
	height: 162upx;
}
.success {
	padding-top: 30upx;
	font-size: 30upx;
	color: #3f85bc;
}
.mt300 {
	margin-top: 300upx;
}
</style>
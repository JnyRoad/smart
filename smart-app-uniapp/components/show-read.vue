<template>
	<view v-if="showRead" class="read-state">
		<image class="mw mh" src="/static/img/serive/news-read.png" mode=""></image>
	</view>
</template>

<script>
	// 暂时消息模块的消息已读的提示
	export default {
		props: {
			showRead: {
				type: Boolean,
				default: false
			},
		},
	}
</script>

<style scoped>
	.read-state {
		position: absolute;
		top: 30%;
		left: 50%;
		transform: translateX(-50%);
		width: 250upx;
		height: 250upx;
		overflow: hidden;
	}
</style>

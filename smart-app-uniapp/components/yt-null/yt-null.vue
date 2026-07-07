<template>
	<view  class="fixed no-data vcc">
		<image class="w230 h200 mb30" src="/static/img/serive/recruit/null.png" mode=""></image>
		<view class="vcc f32pc2">{{notice}}</view>
	</view>
</template>

<script>
	export default {
		props: {
			notice: {
				type: String,
				default: ''
			}
		}
	}
</script>

<style scoped>
	.no-data {
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
	}
</style>

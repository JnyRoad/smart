<template>
	<view :class="['abs w145 h140',ytClass]">
		<image src="/static/img/mine/pass-icon.png" mode=""></image>
	</view>
</template>

<script>
	// 审批成功组件 暂未使用
	export default {
		name: 'yt-approval-success',
		props: {
			ytClass: Array
		}
	}
</script>

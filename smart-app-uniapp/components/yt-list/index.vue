<template>
	<view class="list" :class="ytClass">
		<slot></slot>
	</view>
</template>
<script>
	export default {
		name: 'yt-list',
		props: {
			ytClass: {
				type: Array
			}
		}
	}
</script>
<style scoped>
	.list {
		background-color: #fff;
		position: relative;
		display: flex;
		flex-direction: column;
	}

</style>
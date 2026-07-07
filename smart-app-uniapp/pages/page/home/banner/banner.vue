<template>
	<view class="">
		<web-view :src="link"></web-view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				link: ''
			}
		},
		onLoad(e) {
			this.link = decodeURIComponent(e.linkUrl)
		}
	}
</script>

<style></style>

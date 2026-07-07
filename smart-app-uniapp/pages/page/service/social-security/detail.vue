<template>
	<view class="">
		<!-- <web-view :src="link"></web-view> -->
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
			console.log(decodeURIComponent(e.link));
			this.link = decodeURIComponent(e.link)
			this.launchApp()
		},
		methods: {
			launchApp() {
				const that = this
				
			}
		},
	}
</script>

<style>
</style>

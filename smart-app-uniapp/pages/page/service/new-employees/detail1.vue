<template>
	<view class="mw bgf">
		<web-view :webview-styles="webviewStyles" v-if="link" :src="link"></web-view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			link: '',
			webviewStyles: {
				progress: {
					color: '#508BFF'
				}
			}
		};
	},
	onLoad(e) {
		if (e.link) {
			this.link = decodeURIComponent(e.link)
		}
	}
};
</script>

<style>
</style>

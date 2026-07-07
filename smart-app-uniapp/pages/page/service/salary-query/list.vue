<template>
	<view>
		<web-view :src="url"></web-view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				url: '/hybrid/html/salary-query.html'
			}
		},
		onLoad(e) {
			// this.url = decodeURIComponent(e.url)
		}
	}
</script>
<template>
	<view class="">
		<web-view :src="link"></web-view>
	</view>
</template>

<script>
	import { storage } from '@/tools/storage.js'
	import account from '@/api/api-account.js'
	export default {
		data() {
			return {
				link: ''
			}
		},
		onLoad (e) {
			let token = storage.getSync(storage.USER_TOKEN)
			let link = decodeURIComponent(e.link);
			account.getToken().then( (res) => {
				console.log(res);
			})
			this.link = link
			if (e.fuck) {
				this.link = link+'&Token='+token
			}
			console.log(this.link);

			if (e.title) {
				uni.setNavigationBarTitle({
					title: e.title
				})
			}
		}
	}
</script>

<style>
</style>

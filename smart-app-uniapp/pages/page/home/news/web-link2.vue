<template>
	<view class="bgf mw">
		<web-view src="/hybrid/html/desc.html"></web-view>
	</view>
</template>

<script>

	import account from '@/api/api-account.js'
	export default {
		components: {
		},
		data() {
			return {
				info: {}
			}
		},
		onLoad() {
			this.getBisc()

		},
		methods: {
			async getBisc() {
				try{
					const res = await account.getToken()
					this.info = res
					console.log(res);
					uni.setStorageSync('UPARSE', res.data);
					console.log(uni.getStorageSync('UPARSE'));
				}catch(e){
					//TODO handle the exception
					console.log(e);
					throw e
				}
			}
		},
	}
</script>

<style></style>

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
					if (!res.data.data) {
						this.$ytHint.toast({ title: '银行实名服务正在安全升级中' })
					}
				}catch(e){
					this.$ytHint.toast({ title: '银行实名服务正在安全升级中' })
				}
			}
		},
	}
</script>

<style></style>

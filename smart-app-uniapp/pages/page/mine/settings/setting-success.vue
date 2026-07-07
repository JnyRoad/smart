<template>
	<view class="yt-page bgf">
		<yt-success icon="static/img/login/set.png" btnUrl="../../logins/login/login" btnText="返回登录" successText="设置成功"></yt-success>
	</view>
</template>

<script>
	import success from '@/components/Success.vue';
	export default {
		components: {
			'yt-success': success	
		},
	}
</script>

<style lang="scss" scoped>
	
</style>

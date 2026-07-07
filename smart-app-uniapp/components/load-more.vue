<template>
	<view @click="onClick" class="hcc f30pc2 pt40 pb60">
		加载更多＞＞
	</view>
</template>
<script>
	export default {
		data() {
			return {
				
			}
		},
		methods: {
			onClick() {
				this.$emit('click')
			}
		}
	}
</script>

<style scope>

</style>
<template>
	<view class="yt-page bgf">
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item navUrl="./verify-tel" showImage="false" title="更换手机号"></yt-list-item>
			<yt-list-item navUrl="../../logins/findPwdPhone/findPwdPhone" showImage="false" title="修改密码" hideLastBorder="false"></yt-list-item>
		</yt-list>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import { storage } from '@/tools/storage.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem
		},
		data () {
			return {
				showFace: false,
				tel: ''
			}
		},
		onLoad() {
			this.tel = storage.getSync(storage.USER_TEL)
		}
	}
</script>

<style lang="scss" scoped>
	
</style>

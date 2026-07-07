<template>
	<view class="yt-page">
		<view class="bgf pt30 pl30 pb30 pr30">
			<view class="mw h400 borderEc pt30 pl30 pb30 pr30">
				<textarea class="mw mh" v-model="content" value="" placeholder="请输入您的问题" />
			</view>
		</view>
		<view class="mw hcc mt100">
			<yt-button @click="submit" btnText="提交" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytbutton from '@/components/yt-button/index.vue'
	import {setting} from '@/api/api-mine.js'
	export default {
		components: {
			'yt-button': ytbutton
		},
		data () {
			return {
				content:''
			}
		},
		methods: {
			submit() {
				if (this.content === '') {
					this.$ytHint.toast({
						title: '提交内容不能为空',
						mask: true
					})
					return
				}
				const obj = {
					content: this.content
				}
				setting.suggest(obj).then( res => {
					this.content = ''
					this.$ytHint.toast({
						title: '提交成功',
						icon: 'success'
					})
				})
			}
		},
	}
</script>

<style lang="scss" scoped>
	
</style>

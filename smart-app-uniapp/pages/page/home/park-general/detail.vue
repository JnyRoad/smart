<template>
	<view class="bgf mw pl30 pr30">
		<view v-if="!showWebView" class="mw pb30 bgf">
			<view v-if="info.generalContent" class="mw tc">
				<uParse :content="info.generalContent" />
			</view>
			<yt-loading></yt-loading>
		</view>
		<web-view v-if="showWebView" :src="link"></web-view>
	</view>
</template>

<script>
	import uParse from '@/components/uParse/src/wxParse.vue'
	import indexInfo from '@/api/api-index-info.js'
	export default {
		components: {
			uParse
		},
		data() {
			return {
				info: {},
				showWebView: false,
				link: ''
			}
		},
		onLoad(e) {
			if (e.link) {
				this.link = decodeURIComponent(e.link)
				this.showWebView = true
			} else if (e.generalId) {
				this.generalDetail(e.generalId)
			}
		},
		methods: {
			async generalDetail(id) {
				try{
					const res = await indexInfo.generalDetail(id)
					this.info = res.data.data
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			}
		},
	}
</script>

<style></style>

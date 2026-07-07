<template>
	<view class="bgf">
		<view v-if="!showWebView" class="mw pr30 pl30 pb30 bgf">
			<view v-if="info.instroduceContent" class="mw tc">
				<uParse :content="info.instroduceContent" />
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
			} else if (e.instroduceId) {
				this.instroduceDetail(e.instroduceId)
			}
			
		},
		methods: {
			async instroduceDetail(id) {
				try{
					const res = await indexInfo.instroduceDetail(id)
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

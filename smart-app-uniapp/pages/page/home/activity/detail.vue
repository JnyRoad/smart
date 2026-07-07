<template>
	<view class="bgf mw">
		<view v-if="!showWebView" class="pr30 pl30 pb30 bgf">
			<view v-if="info.activityContent" class="mw tc">
				<!-- <uParse :content="info.activityContent" /> -->
				<rich-text :nodes="info.activityContent"></rich-text>
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
			} else if (e.activityId) {
				this.activityDetail(e.activityId)
			}
			
		},
		methods: {
			async activityDetail(id) {
				try{
					const res = await indexInfo.activityDetail(id)
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

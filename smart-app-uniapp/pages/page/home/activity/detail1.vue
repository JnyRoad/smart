<template>
	<view class="mw bgf">
		<!-- #ifdef APP-PLUS -->
		<web-view :webview-styles="webviewStyles" v-if="hasPdf" :src="pdfUrl"></web-view>
		<!-- #endif -->
		<yt-loading></yt-loading>
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
				pdfUrl : '',
				hasPdf: false ,// 判断文章中是否有pdf文件下载
				webviewStyles: {
					progress: {
						color: '#508BFF'
					}
				}
			}
		},
		onLoad(e) {
			this.activityDetail(e.activityId)
		},
		methods: {
			async activityDetail(id) {
				try{
					const res = await indexInfo.activityDetail(id)
					this.info = res.data.data
					if (res.data.data.enclosureName) {
						this.hasPdf = true;
						this.pdfUrl = res.data.data.previewUrl;
						return
					}
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

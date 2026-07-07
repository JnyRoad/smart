<template>
	<view class="bgf mw">
		<view v-if="!showWebView" class="mw pr30 pl30 pb30 bgf">
			<view v-if="info.bbsContent" class="mw tc">
				<!-- <uParse :content="captchaSrc" /> -->
				<rich-text :nodes="captchaSrc"></rich-text>
			</view>
			<yt-loading></yt-loading>
		</view>
		<web-view v-if="showWebView" :src="link"></web-view>
	</view>
</template>

<script>
	import uParse from '@/components/uParse/src/wxParse.vue'
	import indexInfo from '@/api/api-index-info.js'
	import account from '@/api/api-account.js'
	export default {
		components: {
			uParse
		},
		data() {
			return {
				info: {},
				showWebView: false,
				link: '',
				content : ''
			}
		},
		computed:{
			captchaSrc(){
				return this.content.replace(/[\r\n]/g, "");
			}
		},
		onLoad(e) {
			if (e.link) {
				this.link = decodeURIComponent(e.link)
				this.showWebView = true
			} else if (e.bbsId) {
				this.getBbsDetail(e.bbsId)
			} else if (bisc) {
				this.getBisc()
			}
		},
		methods: {
			async getBbsDetail(id) {
				try{
					const res = await indexInfo.bbsDetail(id)
					this.info = res.data.data
					this.content = this.info.bbsContent
			
					// this.info.bbsContent = content.replace(/<img [^>]*src=['"]([^'"]+)[^>]*>/ig,'<img src="../../../../static/XIAZAI.jpg" />');
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			async getBisc() {
				try{
					const res = await account.getToken()
					this.info = res
				}catch(e){
					//TODO handle the exception
				}
			}
		},
	}
	
</script>

<style></style>

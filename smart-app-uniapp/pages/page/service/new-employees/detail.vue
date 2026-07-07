<template>
	<view class="mw bgf">
		<!-- #ifdef APP-PLUS -->
		<web-view :webview-styles="webviewStyles" v-if="hasPdf" :src="pdfUrl"></web-view>
		<!-- #endif -->
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import information from '@/api/api-new-employee.js'
export default {
	data() {
		return {
			noteId: -1,
			contentStr: '',
			showWebView: false,
			hasPdf: false, // 判断文章中是否有pdf文件下载
			pdfUrl: '', // pdf的下载地址
			link: '',
			webviewStyles: {
				progress: {
					color: '#508BFF'
				}
			}
		};
	},
	onLoad(e) {
		if (e.noteId) {
			this.noteId = e.noteId
			this.getDetail(e.noteId)
			uni.setNavigationBarTitle({
				title: e.noteName
			})
		}
	},
	onHide() {
		uni.hideLoading();
	},
	methods: {
		async getDetail() {
			try {
				const res = await information.informationDetail(this.noteId);
				if (res.data.data.enclosureName) {
					this.hasPdf = true;
					this.pdfUrl = res.data.data.previewUrl;
					return
				}
				this.hasPdf = false
				var reg = /<!--\[if\s+gte\s+mso\s+9\]>(?:(?!<!\[endif]\-->)[\s\S])*<!\[endif]\-->/gi;
				const Str = res.data.data.noteContent.replace(reg, '');
				this.contentStr = Str;
				uni.setStorageSync('UPARSE', Str);
			} catch (e) {
				//TODO handle the exception
			}
		}
	}
};
</script>

<style scoped>
.image {
	width: 100%;
	height: 400upx;
}
</style>

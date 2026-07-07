<template>
	<view class="mw bgf">
		<!-- #ifdef H5 -->
		<view v-if="contentStr" class="pt20 pb20"><view v-html="contentStr"></view></view>
		<!-- #endif -->
		<!-- #ifdef APP-PLUS -->
		<web-view :webview-styles="webviewStyles" src="/hybrid/html/detail.html"></web-view>
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

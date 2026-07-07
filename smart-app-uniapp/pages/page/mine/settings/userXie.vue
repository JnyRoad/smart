<template>
	<view class="mw bgf" style="padding: 10upx;">
		<!-- #ifdef H5 -->
		<uParse :content="contentStr" />
		<!-- #endif -->
		<!-- #ifdef APP-PLUS -->
		<uParse :content="contentStr" />
		<!-- #endif -->
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import attendance from '@/api/api-attendance.js'
import { storage } from '@/tools/storage.js'
import uParse from '@/components/uParse/src/wxParse.vue'
export default {
	
	components: {
		uParse
	},
	data() {
		return {
			parkId : '',
			contentStr : ''
		};
	},
	onLoad() {
		this.parkId = storage.getSync(storage.PARK_ID)
		this.getDetail()
		uni.setNavigationBarTitle({
			title: 'APP用户使用服务协议'
		})
	},
	onHide() {
		uni.hideLoading();
	},
	methods: {
		async getDetail() {
			try {
				const res = await attendance.getService(this.parkId);
				console.log(res.data);
				this.contentStr = res.data.data.agreeContent
			} catch (e) {
				//TODO handle the exception
				console.log(e);
				throw e
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

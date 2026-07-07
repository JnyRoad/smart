<template>
	<view class="yt-page bgf">
		<view class="logo vcc">
			<view class="icon">
				<image :src="aboutus.logoPictureUrl" mode=""></image>
			</view>
			<text class="f32pc">{{aboutus.appVerson}}</text>
		</view>
		<!-- <yt-list :ytClass="['pr30 pl30']">
			<yt-list-item @click="getVerson"  showArrow=false showImage=false title="检测新版本"></yt-list-item>
		</yt-list> -->
		<view class="pr30 pl30 hbc" style="height: 120upx;width: 100%;border-bottom: 1px solid #E0E0E0;">
			<text class="blod" style="font-size: 16px;color: #333333;font-weight: bold;" @click="getVerson">检测新版本</text>
			<text v-if="upload">有新版本</text>
			<text v-if="!upload">已是最新</text>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import {setting} from '@/api/api-mine.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem
		},
		data () {
			return {
				aboutus: {
					logoPictureUrl: '/static/img/mine/yt-icon.png',
					appVerson: ''
				},
				upload : null
			}
		},
		onLoad() {
			// this.getAboutus()
			if (uni.getStorageSync('gengxing')) {
				this.upload = true
			} else {
				this.upload = false
			}
			const that = this
			// #ifdef APP-PLUS
			plus.runtime.getProperty(plus.runtime.appid, function(widgetInfo) {
				that.aboutus.appVerson = widgetInfo.version
			})
			// #endif
		},
		methods: {
			async getAboutus () {
				try{
					const res = await setting.aboutUs()
					console.log(res);
					if (!res) return
					this.aboutus = res.data.data
					
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// #ifdef APP-PLUS
			async getVerson() {
				const that = this
				plus.runtime.getProperty(plus.runtime.appid, function(widgetInfo) {
					setting.versionCheck(widgetInfo.version).then(res => {
						var data = res.data.data;
						if (data.isNeedUpdate) {
							that.$ytHint.modal({
								content: 'APP有新版本可更新 是否更新？',
								confirm: () => {
									plus.nativeUI.showWaiting("更新包下载中，请稍后...");  
									uni.downloadFile({
										url: data.patchUrl,
										success: (downloadResult) => {
											if (downloadResult.statusCode === 200) {
												plus.runtime.install(downloadResult.tempFilePath, {}, function() {
													plus.nativeUI.closeWaiting();
													plus.nativeUI.alert("应用资源更新完成！应用即将重启", function() {
														plus.runtime.restart();
													});
												}, function(e) {
													plus.nativeUI.closeWaiting();
													console.log("更新失败[" + e.code + "]：" + e.message);
													plus.nativeUI.alert("APP更新失败，请联系有关人员进行反馈");
												});
											}
										}
									});
								}
							})
						} else {
							that.$ytHint.toast({
								title: '当前APP处于最新版本'
							})
						}
					})
				});
			}
			// #endif
		},
	}
</script>

<style lang="scss" scoped>
	.yt-page {
		.logo {
			width: 100%;
			padding: 30upx 0;
			border-bottom: 1rpx solid $uni-border-color;
			.icon {
				width: 260upx;
				height: 260upx;
				border-radius: 50%;
				margin-bottom: 40upx;
				background-color: $uni-border-color;
				overflow: hidden;
			}
		}
	}
</style>

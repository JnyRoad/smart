<template>
	<view class="w750 bgf">
		<!-- 自定义导航栏 -->
		<view class="home-head mw rel">
			<!-- 轮播图 -->
			<view class="banner">
				<swipers @click="toBannerDetail" v-if="bannerList.length>0" :list="bannerList" />
			</view>
<!-- 			<custom-navigator class="abs mw top0 left0" :titleText="parkName" statusColor="transparent" :showLeftButton="false" :showRightButton="true">
				<navigator url="../../home/address-list/address-list" hover-class="none" class="f32">选择园区</navigator>
			</custom-navigator> -->
		</view>
		<!-- 8格图 -->
		<indexIcon4 @click="toMenu" :list="menuList" />
		<view class="mw" style="height: 130upx;padding:32upx;" v-if="newsListshow">
			<view class="mh mw hlc" style="border: 1px solid #e0e0e0;box-shadow: 0 0 16px rgba(0,0,0,.2);overflow: hidden;">
				<view class="mh hcc" style="width: 15%;border-right: 1px solid #E0E0E0;height: 70%;">
					<image src="/static/laba.png" style="height: 100%;width: 50%;"></image>
				</view>
				<view class="mh pl20 pr20" style="width: 85%;" :class="newsListtrue ? 'guand' : ''">
					<block v-for="(item, index) in gonggaoList" :key="index">
						<view class="mh hlc mw" style="color: #999999;">
						<text class="ellipsis" @click="urlNew(item.activityId,item.contentLinkType)">{{item.activityTitle}}</text>
						</view>
					</block>
					<!-- <view class="mh hlc ellipsis" style="color: #999999;" v-if="newsList[0]">{{newsList[1].newsTitle}}</view> -->
				</view>
			</view>
		</view>
		<!-- 拓展菜单 -->
		<home-expand></home-expand>
		<!--  新闻动态-->
		<view class="hlc pl40 mb30">
			<view class="image mb20">
				<image src="/static/img/mine/ercode-1.png" mode=""></image>
			</view>
			<image class="w36 h42" src="/static/img/index/news.png" alt=""></image>
			<text class="pl30 f32pc">新闻动态</text>
		</view>
		<mediaList @click="toNewsDetail" :list="newsList" />
		<!-- 加载更多 -->
		<loadMore @click="getMore" />
		<!-- <guide-index v-if="!hasGiuded" @click="toMinePage" /> -->
		<!-- 下载等待框 暂废弃 -->
		<view v-if="hasDownLoading" class="down-progress">
			<view class="mask"></view>
			<view class="content">
				<view class="view_box">
					<iCircle
						:percent="circlePercent"
						:size="200"
						:stroke-color="color"
						BgId="BgId"
						InId="InId"
					>
						<text v-if="circlePercent == 100" class="f40 suc-color">下载完成</text>
						<text v-else class="f40 main-color">{{ circlePercent }}%</text>
						<view slot="canvas">
							<canvas
								class="CanvasBox strokeCanvas"
								canvas-id="BgId"
							></canvas>
							<canvas
								class="CanvasBox trailCanvas"
								canvas-id="InId"
							></canvas>
						</view>
					</iCircle>
				</view>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	// 首页菜单
	import customNavigator from '@/components/custom-navigator.vue'
	import swipers from '@/components/swiper.vue'; // 轮播
	import indexIcon4 from '@/components/icon4.vue'; // 菜单
	import homeExpand from '@/components/home-expand.vue' // 首页拓展菜单
	import mediaList from '@/components/mediaList.vue'; // 新闻
	import loadMore from '@/components/load-more.vue'; // 加载更多
	import guideIndex from '@/components/guide-index.vue'; // 首页引导
	// import iCircle from '@/components/xiaoran-circle/xiaoran-circle.vue'; // 仪表盘组件
	// #ifdef APP-PLUS
	import exitApp from '@/mixins/exitApp.vue'; // 触发双击回退的提示的混入
	// #endif
	// 接口
	import location from '@/api/api-location.js'; //定位园区接口 location.getLocation, location.getParkList
	import indexInfo from '@/api/api-index-info.js'; //首页接口
	import plusNews from '@/api/api-plusnews.js' // 推送接口
	import loginAccount from '@/api/api-account.js' // 刷新token
	import {
		HASGUIDED,
		storage
	} from '@/tools/storage.js';
	import device from '@/api/api-device.js' // 

	import {
		setting
	} from '@/api/api-mine.js'
	import {locationAuthortyPermissions} from '@/config/authority.js'
	export default {
		// #ifdef APP-PLUS
		mixins: [exitApp],
		// #endif
		components: {
			customNavigator,
			swipers,
			indexIcon4,
			mediaList,
			loadMore,
			guideIndex,
			homeExpand
		},
		data() {
			return {
				isAutoPlay: false, // 控制轮播组件自动轮播
				newsListtrue : false,
				newsListshow : false,
				bannerList: [],
				gonggaoList : [],
				newsList: [],
				menuList: [],
				locationMap: {
					longitude: 0,
					latitude: 0
				},
				parkName: '', // 园区名
				parkId: 26, // 园区id
				hasGiuded: true, // 是否已经延时过引导
				deviceNo: '' ,// 推送设备号
				hasDownLoading: false ,// 更新
				curDwonWidth: 0, // 更新进度
				hasDown: false ,// 是否下载完成
				circlePercent: 0 ,// 当前的下载进度
			};
		},
		onNavigationBarButtonTap(e) {
			uni.navigateTo({
				url: '../../home/address-list/address-list'
			});
		},
		onLoad() {			
			const localCache = storage.getSync('HomeLocalCache') // 如果首页不加载接口读取缓存 读取首页缓存
			if (localCache) {
				this.readHomeLocalCache(localCache)
			} else {
				// #ifdef H5
				this.getLocationAddr()
				// #endif
			}
			// app初始化消息角标
			uni.removeTabBarBadge({
				index: 2
			})
			const that = this
			// 读取缓存数据
			// #ifdef APP-PLUS
			this.deviceNo = plus.device.uuid.split(',')[0]
			plus.navigator.closeSplashscreen() // 关闭启动图
			// #endif
			//#ifdef APP-PLUS
			this.upDataApp() // 检查更新
			this.getPlusNewsCount() // 获取未读消息
			// #endif
			
		},
		onShow() {
			// 是否引导完毕
			this.hasGiuded = storage.getSync(HASGUIDED) || false
			// 获取本地存储的园区名及id
			this.parkName = storage.getSync(storage.PARK_NAME);
			console.log(this.parkName);
			uni.setNavigationBarTitle({
				title:this.parkName
			})
			const parkId = storage.getSync(storage.PARK_ID)
			// 监听本地缓存是否触发了地址修改
			const addressChange = storage.getSync('addressChange') // 定位变化
			if (addressChange) {
				uni.removeStorageSync('addressChange') // 修改完毕删除 否则下次进入依旧会重新加载页面数据
				this.parkId = parkId // 同步到页面
				this.getBanner() // 加载banner数据
				this.getMenuList() // 加载菜单导航
				this.getNewsList() // 获取新闻动态数据3条
				this.getPlusNewsCount() // 获取消息的未读数量
				this.getClient()
				this.activityList()
			}
		},
		// 应用下载进度仪表盘 赞废弃
		// computed: {
		// 	// 更新进度环
		// 	color() {
		// 		let color = '#508BFF';
		// 		if (this.circlePercent == 100) {
		// 			color = '#76C289';
		// 		}
		// 		return color;
		// 	}
		// },
		methods: {
			urlNew (id,type) {
				uni.navigateTo({
					url : `/pages/page/home/activity/detail?activityId=${id}`
				})
			},
			// 读取缓存
			readHomeLocalCache (localCache) {
				this.bannerList = localCache.banner
				// 配置菜单导航
				localCache.menu.forEach((el, index) => {
					if (el.contentLinkType === 3) {
						el.link = this.menuFlitter(el.moduleUrl)
					} else if (el.contentLinkType === 1) {
						el.link = el.moduleUrl
					}
					el.moduleId = index + 1
				})
				this.menuList = localCache.menu
				this.newsList = localCache.news
			},
			// #ifdef APP-PLUS
			// 检查版本更新 并下载安装
			upDataApp() {
				const that = this
				// 调用h5+ api 获取当前的应用版本
				plus.runtime.getProperty(plus.runtime.appid, function(widgetInfo) {
					// 调取接口检查更新
					setting.versionCheck(widgetInfo.version).then(res => {
						if (!res) return
						var data = res.data.data;
						if (data.isNeedUpdate) {
							uni.setStorageSync('gengxing', true)
							that.$ytHint.modal({
								content: 'APP有新版本可更新 是否更新？',
								cancelText : '稍后升级',
								confirmText : '立即升级',
								confirm: () => {
									// that.hasDownLoading = true
									plus.nativeUI.showWaiting("更新包下载中，请稍后...");  
									const downloadTask = uni.downloadFile({
										url: data.patchUrl,
										success: (downloadResult) => {
											if (downloadResult.statusCode === 200) {
												plus.runtime.install(downloadResult.tempFilePath, {}, function() {
													// that.hasDownLoading = false
													// that.hasDown = true
													plus.nativeUI.closeWaiting();
													plus.nativeUI.alert("应用资源更新完成！应用即将重启", function() {
														console.log(storage.getSync(storage.USER_TOKEN));
														plus.runtime.restart();
													});
												}, function(e) {
													// that.hasDownLoading = false
													plus.nativeUI.closeWaiting()
													plus.nativeUI.alert("APP更新失败，请检查您的网络");
												});
											}
										}
									});
									downloadTask.onProgressUpdate((res) => {
										that.curDwonWidth = res.progress // 进度百分比
									});
								},
								cancel: () => {
									plus.runtime.quit();
								}
							})
						} else {
							uni.setStorageSync('gengxing', false)
							that.getLocationAddr() // 没有更新获取定位
						}
					})
				});
			},
			// 获取设备号
			getClient() {
				const info = plus.push.getClientInfo();
				// console.log(info)
				uni.getSystemInfo({
					success: res => {
						let devicePushId = ''
						let osType = 1
						if (res.platform === 'android') {
							devicePushId = info.clientid
							osType = 1

						} else {
							devicePushId = info.token
							osType = 2
						}
						if (this.deviceNo == '') this.deviceNo = plus.device.uuid.split(',')[0] // 防止第一次获取不到
						const obj = {
							deviceName: res.model,
							deviceNo: this.deviceNo,
							devicePushId: devicePushId,
							osType: osType
						}
						this.deviceInfoPost(obj)
					}
				});
			},
			// 发送设备信息
			async deviceInfoPost(obj) {
				try {
					const res = await device.postDevice(obj)
					console.log(res);
					if (!res) return
				} catch (e) {
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// #endif
			// 定位
			getLocationAddr() {
				const that = this;
				if (!storage.getSync('shoucidingwei')) {
					uni.showModal({
						title: '温馨提示',
						content: '第一次进入,将获取您得位置信息!',
						success: (res) => {
							if (res.confirm) {
								storage.setSync('shoucidingwei',true)
								uni.getLocation({
									type: 'wgs84',
									success: function(res) {
										uni.hideToast() // 防止打开 app 有无关弹窗
										that.locationMap.longitude = res.longitude
										that.locationMap.latitude = res.latitude
										that.getNowlocation(1)  /// 1表示打开了定位成功
									},
									fail: function() { // 第一次 定位失败
										// 定位失败获取本地是否有缓存的定位信息
										const lat = storage.getSync(storage.USER_LAT)
										const long = storage.getSync(storage.USER_LONG)
										// 如果没有 默认定位到大岭山
										if (!lat || !long) {
											that.getNowlocation(0) // 没有记录上一次 定位信息  0 则定位默认地址
											return
										}
										// 如果有 自动定位到上次定位的园区
										that.locationMap.longitude = long
										that.locationMap.latitude = lat
										that.getNowlocation(1)
									}
								});
							}else if (res.cancel) {
								uni.showModal({
									title: '温馨提示',
									content: '获取不到您得定位,是否前往园区列表选择!',
									showCancel: false,
									success: (res) => {
										if (res.confirm) {
											uni.navigateTo({
												url: '/pages/page/home/address-list/address-list'
											})
										}
									}
								})
							}}
					})
				} else {
					uni.getLocation({
						type: 'wgs84',
						success: function(res) {
							uni.hideToast() // 防止打开 app 有无关弹窗
							that.locationMap.longitude = res.longitude
							that.locationMap.latitude = res.latitude
							that.getNowlocation(1)  /// 1表示打开了定位成功
							console.log('定位成功');
						},
						fail: function() { // 第一次 定位失败
							// 定位失败获取本地是否有缓存的定位信息
							const lat = storage.getSync(storage.USER_LAT)
							const long = storage.getSync(storage.USER_LONG)
							console.log('定位失败');
							// 如果没有 默认定位到大岭山
							if (!lat || !long) {
								that.getNowlocation(0) // 没有记录上一次 定位信息  0 则定位默认地址
								return
							}
							// 如果有 自动定位到上次定位的园区
							that.locationMap.longitude = long
							that.locationMap.latitude = lat
							that.getNowlocation(0)
						}
					});
				}
			},
			// banner
			async getBanner() {
				this.bannerList = []
				try{
					const res = await indexInfo.banner(this.parkId);
					if (!res) return
					this.bannerList = res.data.data;
				}catch(e){
					//TODO handle the exception
				}
			},
			//导航菜单列表
			async getMenuList() {
				this.menuList = []
				try{
					const res = await indexInfo.menu(this.parkId);
					if (!res) return
					res.data.data.forEach((el, index) => {
						if (el.contentLinkType === 3) {
							el.link = this.menuFlitter(el.moduleUrl)
						} else if (el.contentLinkType === 1) {
							el.link = el.moduleUrl
						}
						el.moduleId = index + 1
					})
					this.menuList = res.data.data;
					console.log(this.menuList);
				}catch(e){
					//TODO handle the exception
				}
			},
			// 新闻列表
			async getNewsList() {
				this.newsList = []
				const page = {
					current: 1,
					size: 3
				}
				try {
					const res = await indexInfo.newsList(page, this.parkId);
					if (!res) return
					this.newsList = res.data.data.records;
				} catch (e) {
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			async activityList() {
				this.gonggaoList = []
				const page = {
					current: 1,
					size: 3
				}
				try {
					const res = await indexInfo.activityList(page, this.parkId);
					if (!res) return
					this.gonggaoList = res.data.data.records;
					console.log(this.gonggaoList);
					// console.log(this.gonggaoList.length);
					if (this.gonggaoList.length > 0) {
						this.newsListshow = true
					} else {
						this.newsListshow = false
					}
					if (this.gonggaoList.length > 1) {
						this.newsListtrue = true
					}
				} catch (e) {
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 定位
			async getNowlocation(type) { // type 为 1 表示定位成功 为 0 失败  默认大岭山
				console.log(type);
				try {
					const res = await location.getLocation(this.locationMap,type)
					if (!res) return
					const data =  res.data.data
					if (data.parkId == 'null' || data.parkId == null || data.parkId == undefined || data.parkId == 'undefined' || data.parkId == '') {
						uni.showModal({
							title: '温馨提示',
							content: '检测到您的定位不在裕同集团的厂区范围,是否前往园区列表选择',
							showCancel: false,
							success: (res) => {
								if (res.confirm) {
									uni.navigateTo({
										url: '/pages/page/home/address-list/address-list'
									})
								}
							}
						})
						return
					}
					this.parkId = data.parkId
					this.parkName = data.parkName
					uni.setNavigationBarTitle({
						title: data.parkName
					})
					this.getBanner()
					this.getMenuList()
					this.getNewsList()
					this.getPlusNewsCount()
					this.getClient()
					this.activityList()
					this.$store.commit('setAddress', this.parkName)
					storage.set(storage.PARK_NAME, this.parkName)
					storage.set(storage.PARK_ID, this.parkId)
					storage.set(storage.USER_LAT, data.parkLatitude)
					storage.set(storage.USER_LONG, data.parkLongitude)
					// 数据缓存
					setTimeout(() => {
						const localCache = {
							banner: this.bannerList,
							menu: this.menuList,
							news: this.newsList
						}
						storage.set('HomeLocalCache',localCache)
					},1000)
				} catch (e) {
					console.log(e);
					throw e
				}
			},
			// 获取消息
			async getPlusNewsCount () {
				if (typeof plus === 'undefined' || !plus.device) {
					return
				}
				if (this.deviceNo == '') this.deviceNo = plus.device.uuid.split(',')[0]
				try{
					const res = await plusNews.plusNewsCount(this.deviceNo)
					if (!res) return
					if (res.data.data.unReadTotal != 0) {
						uni.setTabBarBadge({
							index: 2,
							text: `${res.data.data.unReadTotal}`
						})
					} else {
						uni.removeTabBarBadge({
							index: 2
						})
					}
					
				}catch(e){
					//TODO handle the exception
				}
			},
			// 前往各个菜单
			toMenu(e) {
				let link = ''
				let title = ''
				console.log(this.menuList);
				if (e) {
					for (let el of this.menuList) {
						if (el.moduleId == e) {
							link = el.link;
							title = el.moduleName
							if (link === '') {
								this.$ytHint.toast({
									title: '暂未开放,敬请期待'
								});
								return
							}
							if (el.contentLinkType === 3) {
								uni.navigateTo({
									url: link
								});
							} else if (el.contentLinkType === 1) {
								let url = encodeURIComponent(link)
								uni.navigateTo({
									url: `../../home/news/web-link?title=${title}&link=${url}`
								});
							}

							break;
						}
					}
				}
			},
			// 新闻详情
			toNewsDetail(item) {
				if (item.contentLinkType == 1) {
					const link = encodeURIComponent(item.newsUrl)
					uni.navigateTo({
						url: `../../home/news/newsDetail?link=${link}`
					})
				} else if (item.contentLinkType == 2) {
					uni.navigateTo({
						url: `../../home/news/newsDetail?newsId=${item.newsId}`
					})
				}

			},
			// getMore
			getMore() {
				uni.navigateTo({
					url: '/pages/page/home/news/newsList'
				})
			},
			toBannerDetail(e) {
				uni.navigateTo({
					url: `../../home/banner/banner?pictureId=${e.pictureId}&linkUrl=${e.linkUrl}`
				})
			},
			toMinePage () {
				this.hasGiuded = true
				uni.switchTab({
					url: '../mine/mine'
				})
			},
			// 模块定向跳转
			menuFlitter(link) {
				let url = ''
				switch (link) {
					case '/parkbbs':
						url = '../../home/notice/list'
						break;
					case '/parknews':
						url = '../../home/news/newsList'
						break;
					case '/parkgeneral':
						url = '../../home/park-general/list'
						break;
					case '/parkactivity':
						url = '../../home/activity/list'
						break;
					case '/parkinstroduce':
						url = '../../home/instroduce/list'
						break;
					case '/parkculture':
						url = '../../home/culture/list'
						break;
					case '/parknavigate':
						url = ''
						break;
					case '/parkvr':
						url = ''
						break;
					default:
						break;
				}
				return url
			}
		}
	};
</script>

<style lang="scss">
	.ellipsis {
	  overflow: hidden;
	  text-overflow: ellipsis;
	  white-space: nowrap;
	}
	.guand {
		animation:change 5s linear 0s normal none infinite;
	}
	@-webkit-keyframes change{
		0% {
			transform: translateY(0);
		}
		10% {
			transform: translateY(0);
		}
		20% {
			transform: translateY(0);
		}
		30% {
			transform: translateY(0upx);
		}
		40% {
			transform: translateY(0upx);
		}
		50% {
			transform: translateY(-65upx);
		}
		60% {
			transform: translateY(-65upx);
		}
		70% {
			transform: translateY(-65upx);
		}
		80% {
			transform: translateY(-65upx);
		}
		90% {
			transform: translateY(-65upx);
		}
		100% {
			transform: translateY(0);
		}
	}
	// @-webkit-keyframes names{
	// 	0%{
	// 	transform: translateY(0upx);
	// 	}
	// 	100%{
	// 	transform: translateY(-65upx);
	// 	}
	// }
	// .guand-1 {
	// 	animation:names 3s infinite;
	// }
	.home-head {
		width: 100%;
		height: 400upx;
	}
	.down-progress {
		position: fixed;
		width: 100%;
		height: 100%;
		left: 0;
		top: 0;
		.mask {
			position: absolute;
			width: 100%;
			height: 100%;
			background-color: rgba(0,0,0,0.5);
		}
		.content {
			display: flex;
			flex-direction: column;
			justify-content: flex-end;
			align-items: center;
			position: absolute;
			left: 50%;
			top: 50%;
			width: 690upx;
			height: 400upx;
			padding: 40upx;
			transform: translate(-50%,-50%);
			background-color: #fff;
			border-radius: 20upx;
			.view_box {
				width: 100%;
				display: flex;
				align-items: center;
				justify-content: center;
			}
			.CanvasBox {
				width: 100%;
				height: 100%;
				position: absolute;
				top: 0px;
				left: 0px;
				display: flex;
				justify-content: center;
				align-items: center;
			}
		}
		
	}
	.banner {
		position: absolute;
		left: 50%;
		bottom: 50upx;
		transform: translateX(-50%);
		height: 340upx;
		width: 100%;
	}
</style>

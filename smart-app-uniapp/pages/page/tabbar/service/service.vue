<!-- <template>
	<view class="mw bgf pl50 pr50 pb20">
	<view class="cur-page">
		<view v-if="true" class="rel mh" >
			<view id="myapp" class="rel mw myapp rad10">
				<view class="f34pc mw hlc h80 bold">园区服务</view>
				<block v-for="(item, index3) in myAppList" :key="index3">
					<view @click="myAppToPages(item.moduleUrl,item.contentLinkType,item.moduleName,index)" class="item1">
						<view  class="vcc mw mh rel">
							<image class="icon81" :src="item.moduleIcon"></image>
							<view class="f26pc mt20">{{ item.moduleName }}</view>
						</view>
					</view>
				</block>
			</view>
			<view class="mw other-module rad10">
				<block v-for="(item1,index1) in otherList" :key="index1">
					<view class="f34pc mw hlc h80 bold">{{item1.hubModuleName}}</view>
					<block v-for="(item2,index2) in item1.subModule" :key="index2">
						<view @click="toPages(item2.moduleUrl,item2.moduleName, item2.contentLinkType)" v-if="index2<4" class="v-icon" :style="{'background-image': 'url('+item2.moduleIcon+')'}">
							<view class="f26 cf hcc rel">{{item2.moduleName}}</view>
						</view>
						<view v-else @click="toPages(item2.moduleUrl,item2.moduleName, item2.contentLinkType)" class="h-icon hbc">
							<view class="f32pc">{{item2.moduleName}}</view>
							<image class="icon81 round" :src="item2.moduleIcon" mode=""></image>
						</view>
					</block>
				</block>
			</view>
		</view>
		<view v-if="false" class="mw mh rel">
			<view class="service-bg">
				<image class="mw mh" mode="aspectFill" src="/static/img/serive/service-bg.png"></image>
			</view>
			<view class="title-contents">  
			    <view class="top-view status"></view>  
			    <view class="_top titles hcc">
			        <view class="mw mh rel">
			        	<view class="tab-head hcc">
			        		<view @click="selectModule(0)" class="tab-item rel">
			        			<view class="title">园区服务</view>
			        			<view v-show="curTabIndex==0" class="slider-bar"></view>
			        		</view>
			        		<block v-for="(item1,index1) in otherList" :key="index1">
			        			<view @click="selectModule(index1+1)" class="tab-item rel">
			        				<view class="title">{{item1.hubModuleName}}</view>
			        				<view v-show="curTabIndex==index1+1" class="slider-bar"></view>
			        			</view>
			        		</block>
			        	</view>
			        </view>
			    </view>  
			</view>
			<view class="service-content">
				<block v-for="(item, index3) in moduleList" :key="index3">
					<view class="module pl15 pr15 pt15 pb15">
						<view @click="myAppToPages(item.moduleUrl,item.contentLinkType,item.moduleName,index)" class="item1">
							<view  class="vcc mw mh rel">
								<image class="icon-wh" :src="item.moduleIcon"></image>
								<view class="f24 abs bottom10 left-50 cf">{{ item.moduleName }}</view>
							</view>
						</view>
					</view>
				</block>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template> -->
<template>
	<view class="mw bgf pl50 pr50 pb20">
		<view class="rel mh" >
			<view id="myapp" class="rel mw myapp rad10">
				<view class="f34pc mw hlc h80 bold">园区服务</view>
				<block v-for="(item, index3) in myAppList" :key="index3">
					<view @click="myAppToPages(item.moduleUrl,item.contentLinkType,item.moduleName,index3)" class="item1">
						<view  class="vcc mw mh rel">
							<image class="icon81" :src="item.moduleIcon"></image>
							<view class="f26pc mt20">{{ item.moduleName }}</view>
						</view>
					</view>
				</block>
			</view>
			<view class="mw other-module rad10">
				<block v-for="(item1,index1) in otherList" :key="index1">
					<view class="f34pc mw hlc h80 bold">{{item1.hubModuleName}}</view>
					<block v-for="(item,index2) in item1.subModule" :key="index2">
						<!-- <view @click="myAppToPages(item.moduleUrl,item.contentLinkType,item.moduleName,index2)" v-if="index2<4" class="v-icon" 
						:style="{'background-image': 'url('+item.moduleIcon+')'}">
							<view class="f26 cf hcc rel">{{item.moduleName}}</view>
						</view> -->
						<view @click="myAppToPages(item.moduleUrl,item.contentLinkType,item.moduleName,index2)" v-if="index2<4" class="v-icon"
						>
							<image :src="item.moduleIcon" style="border-radius: 50%;width: 90upx;height: 90upx"></image>
							<view class="f26 hcc">{{item.moduleName}}</view>
						</view>
						<view v-else @click="myAppToPages(item.moduleUrl,item.contentLinkType,item.moduleName,index2)" class="h-icon hbc">
							<view class="f32pc">{{item.moduleName}}</view>
							<image class="icon81 round" :src="item.moduleIcon" mode=""></image>
						</view>
					</block>
				</block>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import servieIcon4 from '@/components/servie-icon4.vue';
import exitApp from '@/mixins/exitApp.vue';
import serive from '@/api/api-serive.js';
import myMixin from '@/mixins/index.vue'
import {storage} from '@/tools/storage.js'
export default {
	mixins: [exitApp,myMixin],
	components: {
		servieIcon4
	},
	data() {
		return {
			myAppList: [], // 组模块
			otherList: [] ,// 其他模块
			curTabIndex: 0 // 当前模块
		}
	},
	onLoad() {
		const serviceCache = storage.getSync('serviceCache')
		if (serviceCache) {
			this.getServiceLocalCache(serviceCache)
		}
		this.getSeriveList()
	},
	computed: {
		// moduleList() {
		// 	let list = []
		// 	if (this.myAppList.length) {
		// 		if (this.curTabIndex !== 0) {
		// 			list = this.otherList[this.curTabIndex-1].subModule
		// 		} else {
		// 			list = this.myAppList
		// 		}
		// 		return list
		// 	}
		// }
	},
	methods: {
		// 读取缓存
		getServiceLocalCache (serviceCache) {
			// serviceCache.appList.forEach(el => {
			// 	if (el.contentLinkType == 3) {
			// 		el.moduleUrl = this.flitterUrl(el.moduleUrl)
			// 	} else if (el.contentLinkType == 1) {
			// 		el.moduleUrl = el.moduleUrl
			// 	}
			// })
			this.myAppList = serviceCache.appList
			// serviceCache.otherList.forEach(el1 => {
			// 	el1.subModule.forEach(el2 => {
			// 		if (el2.contentLinkType === 3) {
			// 			el2.moduleUrl = this.flitterUrl(el2.moduleUrl)
			// 		}
			// 	})
			//  })
			this.otherList = serviceCache.otherList
		},
		// 列表
		async getSeriveList() {
			const res = await serive.getSeriveList();
			res.data.data.serviceModule.forEach(el => {
				if (el.contentLinkType == 3) {
					el.moduleUrl = this.flitterUrl(el.moduleUrl)
				} else if (el.contentLinkType == 1) {
					el.moduleUrl = el.moduleUrl
				}
			})
			this.myAppList = res.data.data.serviceModule
			this.list = res.data.data.serviceModule
			res.data.data.extraModule.forEach(el1 => {
				el1.subModule.forEach(el2 => {
					if (el2.contentLinkType === 3) {
						el2.moduleUrl = this.flitterUrl(el2.moduleUrl)
					}
				})
			 })
			this.otherList = res.data.data.extraModule
			setTimeout(() =>{
				const obj = {
					appList: this.myAppList,
					otherList: this.otherList
				}
				storage.set('serviceCache',obj)
			})
		},
		// 选择模块
		selectModule(type) {
			console.log(type);
			this.curTabIndex = type
		},
		myAppToPages (url, contentLinkType,title,index) {
			if (url.indexOf('/app/icbc/eaccount') > 0) {
				uni.navigateTo({
					url: '../../home/news/web-link2'
				});
				return
			}
			let salaryTypeName = storage.getSync(storage.SALARYTYPENAME)
			if (!url ) {
				this.$ytHint.toast({
					title: '暂未开放,敬请期待'
				})
				return
			}
			if (contentLinkType === 3) {
				if (url == '../../service/resize-rest/apply' || url == '../../service/extrawork/apply') {
					if (salaryTypeName != '月薪' && salaryTypeName != '月薪1') {
						this.$ytHint.toast({
							title:"该模块给月薪人员使用"
						})
						return
					}
				}
				uni.navigateTo({
					url: url
				})
			} else if (contentLinkType === 1) {
				let link = encodeURIComponent(url)
				uni.navigateTo({
					url: `../../home/news/web-link?title=${title}&link=${link}`
				});
			}
		},
		// 匹配模块链接跳转
		flitterUrl (url) {
			let linkUrl = ''
			switch (url){
				case '/attendance':
					linkUrl = '../../service/checking-in/checking-in-status'
					break;
				case '/salary':
					linkUrl = '../../service/salary-query/index'
					break
				case '/employeenote':
					linkUrl = '../../service/new-employees/index'
					break
				case '/vacate':
					linkUrl = '../../service/ask-leave/apply'
					break
				case '/rest':
					linkUrl = '../../service/resize-rest/apply'
					break
				case '/job':
					linkUrl = '../../service/recruit/recruit-index'
					break
				case '/demission/nomaly':
					linkUrl = '../../service/leave/leave-apply?type=0' // 正常离职
					break
				case '/demission/anomaly':
					linkUrl = '../../service/leave/leave-apply?type=1' // 异常离职
					break	
				case '/visit':
					linkUrl = '../../service/visitor-appointment/my-visitor'
					break
				case '/extrawork':
					linkUrl = '../../service/extrawork/apply'
					break
				case '/travel':
					linkUrl = '../../service/on-business/record'
					break
				case '/approve':
					linkUrl = '../../service/wait-approve/index'
					break
				case '/consume/record':
					linkUrl = '../../service/expense-record/index'
					break;
				case '/socialhouser':
					linkUrl = '../../service/social-security/index'
					break
				case '/parkbbs':
					linkUrl = '../../home/notice/list'
					break;
				case '/parknews':
					linkUrl = '../../home/news/newsList'
					break;	
				case '/parkgeneral':
					linkUrl = '../../home/park-general/list'
					break;
				case '/parkactivity':
					linkUrl = '../../home/activity/list'
					break;	
				case '/parkinstroduce':
					linkUrl = '../../home/instroduce/list'
					break;
				case '/parkculture':
					linkUrl = '../../home/culture/list'
					break;	
				case '/parknavigate':
					linkUrl = ''
					break;
				case '/parkvr':
					linkUrl = ''
					break;		
				default:
					break;
			}
			return linkUrl
		}
	}
};
</script>

<style lang="scss" scoped>
.yt-title {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	box-sizing: border-box;
	padding: 14upx 0;
	z-index: 999;
	background-color: #fff;
	.title {
		position: absolute;
		left: 70px;
		right: 70px;
		bottom: 0;
		text-align: center;
		z-index: 999;
		/* #ifdef H5 */
		font-weight: bold;
		font-size: 32upx;
		/* #endif */
		/* #ifdef APP-PLUS  */
		font-size: 32upx;
		/* #endif */
		/* #ifdef MP-WEIXIN */
		font-size: 26upx;
		/* #endif */
		color: #333;
		&.ios {
			height: 88upx;
		}
		&.android {
			height: 96upx;
		}
		.btn-text {
			right: -110upx;
		}
	}
}	
.item1 {
	display: inline-block;
	width: 162upx;
	height: 190upx;
}
.icon81 {
	width: 88upx;
	height: 88upx;
}
.move {
	z-index: 999;
}
.close {
	top: -10upx;
	right: 35upx;
}
.myapp-edit {
	max-height: 640upx;
	min-height: 460upx;
	box-sizing: border-box;
	border-bottom: 2upx solid #ececec;
}

.other {
	height: 400upx;
}

.other-item {
	width: 33.33%;
}

.tab-item {
	display: inline-block;
	padding: 0 20upx;
}

.scroll {
	position: absolute;
	left: 0;
	right: 0;
	bottom: 0;
	&::-webkit-scrollbar {
		display: none;
	}
}
@keyframes animatelr {
	0% {transform: rotateZ(-2deg)}
	50% {transform: rotateZ(2deg)}
	100% {transform: rotateZ(-2deg)}
}

@keyframes animatetb {
	0% {transform: translateY(0)}
	50% {transform: translateY(4px)}
	100% {transform: translateY(0)}
}

.animatetb {
	animation: animatetb 1.2s infinite ease-in-out;
}

.animatelr {
	animation: animatelr 1.2s infinite ease-in-out;
}

.other-module {
	display: flex;
	justify-content: space-between;
	flex-wrap: wrap;
}

.other-icon {
	width: 130upx;
	height: 150upx;
}

.v-icon {
	// position: relative;
	width: 130upx;
	height: 150upx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	align-items: center;
	// border-radius: 10upx;
	// overflow: hidden;
	// background-repeat: no-repeat;
	// background-position: center top;
	// background-size: 160upx 180upx;
	// box-shadow: 0px 10upx 20upx 0px rgba(185, 185, 185, 0.38);
}

.h-icon {
	width: 300upx;
	height: 130upx;
	padding: 18upx 20upx;
	box-sizing: border-box;
	background-color: #fff;
	border-radius: 10upx;
	margin-top: 20upx;
	box-shadow: 0px 10upx 20upx 0px rgba(185, 185, 185, 0.38);
}

// .cur-page {
// 	position: fixed;
// 	left: 0;
// 	top: 0;
// 	width: 100%;
// 	height: 100%;
// }
// 
// .top-view{  
//     width: 100%;  
//     position: fixed;  
//     top: 0;
// }
// 
// .titles{  
// 	position: relative;
// 	width: 100%;
// 	height: 120upx;
//     top: var(--status-bar-height);  
// }
// 	
// 
// .service-content {
// 	position: absolute;
// 	width: 100%;
// 	top: calc(var(--status-bar-height) + 44px);
// 	left: 0;
// 	padding: 26upx;
// }
// 
// .tab-head {
// 	width: 100%;
// 	height: 90upx;
// 	.tab-item {
// 		height: 50upx;
// 		.title {
// 			padding: 0 10upx;
// 			color: #fff;
// 			font-size: 32upx;
// 		}
// 		.slider-bar {
// 			position: absolute;
// 			bottom: 0;
// 			left: 0;
// 			width: 100%;
// 			height: 10upx;
// 			background-color: rgba($main-color, 0.6);
// 		}
// 	}
// }
// 
// .module {
// 	display: inline-block;
// 	width: 175upx;
// 	height: 175upx;
// 	padding: 15upx;
// }
// 
// .item1 {
// 	display: inline-block;
// 	width: 145upx;
// 	height: 145upx;
// }
// .icon-wh {
// 	width: 145upx;
// 	height: 145upx;
// }
// 
// .service-bg {
// 	position: absolute;
// 	left: 0;
// 	top: 0;
// 	width: 100%;
// 	height: 100%;
// }
// 
// 
// .other {
// 	height: 400upx;
// }
// 
// .other-item {
// 	width: 33.33%;
// }
// 
// .other-module {
// 	display: flex;
// 	justify-content: space-between;
// 	flex-wrap: wrap;
// }
// 
</style>

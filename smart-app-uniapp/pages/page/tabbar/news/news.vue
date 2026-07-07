<template>
	<view class="mw">
		<view :style="statusBar" style="background-color: #FFFFFF;" class="toubu">
			<view class="rel toubu-1" :style="statusBar">
				<text style="font-size: 16px;font-weight: 900;transform: translateX(20upx);">消息</text>
				<text style="font-size: 16px;font-weight: 900;position: absolute;right: 20upx;" @click="showvalue = !showvalue">{{showvalue ? '完成' : '管理'}}</text>
			</view>
		</view>
		<view class="" :style="height">
			
		</view>
		<view v-if="!isNull" class="mw pl30 pr30 pt20" style="z-index: 0;">
				<block v-for="(item,index) in plusList" :key="index">
					<!-- <scroll-new :item="item" @detail="toDetail" @deleteNews="deleteNews" :scrollLeft="scrollLeft"></scroll-new> -->
					<view class="mb20"
					>
						<view class="item-news bgf item rad10 rel">
							<view class="mw mh inline-block left-news"
							:class="[theIndex==index?'open':oldIndex==index?'close':'']" 
							@touchstart="touchStart(index,$event)" @touchmove="touchMove(index,$event)" @touchend="touchEnd(index,$event)">
								<view @click="toDetail(item)" class="mw rel hbc mh rad10">
									<view class="w120 h120 ml30 hcc f28 round cf" :class="[item.readState == 1?'bgec':'main-bg']">{{item.businessTypeDesc}}</view>
									<view class="content vlb mh pr10">
										<text class="f32pc mw text1">{{item.msgTitle}}</text>
										<text class="f28pc4 mw text1">{{item.msgContent}}</text>
										<text class="hrc f30pc4 mw">{{item.createDate}} {{item.createTime}}</text>
									</view>
									<view v-if="item.readState == 0" class="abs top30 left30 w20 h20 main-bg round"></view>
								</view>
							</view>
							<view class="mh inline-block right-news abs" @click="deleteNews(item.recordId, index)">
								<view class="delete mh hcc" :class="theIndex==index? 'delete-red' : ''">
									删除
								</view>
							</view>
						</view>
					</view>
				</block>
		</view>
		<yt-null v-if="isNull" notice="暂未消息通知"></yt-null>
		<yt-loading></yt-loading>
		<radio-group @change="radioChange" v-if="showvalue">
			<view style="height: 150upx;background-color: #FFFFFF;width: 100%;position: fixed;bottom: 0;z-index: 998;" class="hlc">
				<view class="hcc" style="width: 50%;">
					<label class="hlc">
						<view>
							<radio value="1" />
						</view>
						<view>全部删除</view>
					</label>
				</view>
				<view class="hcc" style="width: 50%;">
					<label class="hlc">
						<view>
							<radio value="2" />
						</view>
						<view>全部标记已读</view>
					</label>
				</view>
			</view>
		</radio-group>
	</view>
</template>

<script>
	import scrollNew from '@/components/scroll-news.vue'
	import ytNull from '@/components/yt-null/yt-null.vue';
	import {
		businessType
	} from '@/config/dictionaries.js'
	import {
		dateUtils
	} from '@/common/js/util.js'
	import plusNews from '@/api/api-plusnews.js'
	import salAry from '@/api/api-serive.js'
	export default {
		components: {
			ytNull,
			scrollNew
		},
		data() {
			return {
				// scrollLeft: 0,
				plusNewsNum: 0, // 推送消息未读消息数
				curTabIndex: 0,
				headTabbar: ['待我审批的', '我已审批的'],
				plusList: [], // 推送消息列表
				page: {
					current: 1,
					size: 10
				},
				hasMore: true, // 下拉加载 更多
				isNull: false, // 是否有数据
				deviceNo: 0 ,// 设配编号
				//控制滑动效果
				oldIndex : null,
				theIndex : null,
				isStop:false,
				showvalue : false,
				statusBar : '',
				height : ''
				
			}
		},
		onLoad () {
			uni.getSystemInfo({
				success :(e) => {
					let height = 100 + e.statusBarHeight
					let top = e.statusBarHeight
					if (e.statusBarHeight > 0) {
						top = e.statusBarHeight + 10
					} else {
						top = 'padding-top:' + 14 + 'upx;'
					}
					this.statusBar = 'padding-top:' + top + 'upx;'//状态栏高度
					this.height = 'height:' + height + 'upx;'
				}
			})
		},
		onShow() {
			// #ifdef APP-PLUS
			this.deviceNo = plus.device.uuid.split(',')[0]
			// #endif
			// #ifdef H5
			this.deviceNo = '867520043801512' // h5里面运行默认设备号
			// #endif
			this.page.current = 1 
			this.hasMore = true
			this.isNull = false
			this.getPlusNewsList()
		},
		onPullDownRefresh() {
			this.hasMore = true // 下拉刷新重置数据
			this.isNull = false
			this.page.current = 1
			this.getPlusNewsList()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据了'
				})
				return
			}
			this.page.current++
			this.getPlusNewsList()
		},
		computed : {
			list () {
				this.plusList.forEach( (data) => {
					data.scrollLeft = 0
				})
				console.log(this.plusList);
				return this.plusList
			},
		},
		methods: {
			radioChange (e) {
				console.log(e.detail.value);
				if (e.detail.value == 1) {
					this.deteleAll()
					this.plusList = []
				} else {
					this.readAll()
				}
			},
			async readAll () {
				try{
					const res = await plusNews.readNewsAll(this.deviceNo)
					this.page.current = 1
					this.hasMore = true
					this.isNull = false
					this.getPlusNewsList()
				}catch(e){
					//TODO handle the exception
					console.log(e);
					throw e
				}
			},
			async deteleAll () {
				try{
					const res = await plusNews.deteleNewsAll(this.deviceNo)
					this.page.current = 1
					this.hasMore = true
					this.isNull = false
					this.getPlusNewsList()
				}catch(e){
					//TODO handle the exception
					console.log(e);
					throw e
				}
			},
			// 消息列表
			async getPlusNewsList() {
				if (this.deviceNo == 0 || this.deviceNo == '') {
					this.deviceNo = plus.device.uuid.split(',')[0]
				}
				try {
					const res = await plusNews.getPlusList(this.page, this.deviceNo)
					uni.stopPullDownRefresh()
					if (!res) return
					this.getPlusNewsCount()
					if (this.page.current === 1) {
						if (!res.data.data.records.length) {
							this.isNull = true
							return
						}
						// 匹配该消息类型
						res.data.data.records.forEach(el => {
							el.businessTypeDesc = businessType[el.businessType]
						})
						this.plusList = res.data.data.records
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
							return
						}
						// 匹配该消息类型
						res.data.data.records.forEach(el => {
							el.businessTypeDesc = businessType[el.businessType]
						})
						this.plusList.push(...res.data.data.records)
					}
				} catch (e) {
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 获取消息数量
			async getPlusNewsCount () {
				try{
					const res = await plusNews.plusNewsCount(this.deviceNo)
					uni.stopPullDownRefresh()
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
			// 点击消息为已读
			async changePlusNewsStatus (recordId) {
				try{
					const res = await plusNews.changePlusNews(recordId)
					if (!res) this.$loading(false)
				}catch(e){
					//TODO handle the exception
				}
			},
				//控制左滑删除效果-begin
			touchStart(index,event){
				//多点触控不触发
				console.log(event);
				if(event.touches.length>1){
					this.isStop = true;
					console.log(event);
					return ;
				}
				this.oldIndex = this.theIndex;
				this.theIndex = null;
				//初始坐标
				this.initXY = [event.touches[0].pageX,event.touches[0].pageY];
			},
			touchMove(index,event){
				//多点触控不触发
				if(event.touches.length>1){
					this.isStop = true;
					return ;
				}
				let moveX = event.touches[0].pageX - this.initXY[0];
				let moveY = event.touches[0].pageY - this.initXY[1];
				
				if(this.isStop||Math.abs(moveX)<10){
					return ;
				}
				if (Math.abs(moveY) > Math.abs(moveX)){
					// 竖向滑动-不触发左滑效果
					this.isStop = true;
					return;
				}
				
				if(moveX<0){
					this.theIndex = index;
					this.isStop = true;
				}else if(moveX>0){
					console.log(moveX);
					if(this.theIndex!=null&&this.oldIndex==this.theIndex){
						this.oldIndex = index;
						this.theIndex = null;
						// this.isStop = true;
						setTimeout(()=>{
							this.oldIndex = null;
						},150)
					}
				}
			},
			touchEnd(index,$event){
				//结束禁止触发效果
				this.isStop = false;
			},
			//控制左滑删除效果-end
			// 区别跳转
			toDetail(e) {
				let link = ''
				const paramsArr = e.extraParam.split('||')
				switch (parseInt(e.businessType)) {
					case 1:
						link = `/pages/page/service/visitor-appointment/visitor-detail?type=1&visitState=2&visitId=${e.businessId}&readState=${e.readState}&recordId=${e.recordId}`
						break;
					case 2:
						link = `/pages/page/service/recruit/recruit-index`
						if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						break;
					case 3:
						const approveStatus = paramsArr[3].split('=')[1]
						const leaveStatus = paramsArr[2].split('=')[1]
						if (leaveStatus == 0) {
							if (approveStatus == 3 || approveStatus == 4 || approveStatus == 5) {
								link = `/pages/page/service/leave/leave-handover-detail?processId=${e.businessId}&approveStatus=${approveStatus}&readState=${e.readState}`
							} else {
								link = `/pages/page/service/leave/leave-detail?processId=${e.businessId}&recordTitle=离职详情&type=0&approveStatus=${approveStatus}&readState=${e.readState}`
								if (e.readState == 0 && (approveStatus == 0 || approveStatus == 2 || approveStatus == 2)) this.changePlusNewsStatus(e.recordId)
							}
						} else if (leaveStatus == 1) {
							link = `/pages/page/service/leave/leave-detail?processId=${e.businessId}&recordTitle=离职详情&type=1&approveStatus=${approveStatus}&readState=${e.readState}`				
							if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						}
						break;
					case 6:
						link = `/pages/page/mine/mydorm/out-dorm-success?readState=${e.readState}`
						if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						break;
					case 7:
						link = `/pages/page/service/ask-leave/detail?recordId=${e.businessId}&readState=${e.readState}`
						if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						break;
					case 8:
						link = `/pages/page/service/resize-rest/detail?recordId=${e.businessId}&readState=${e.readState}`
						if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						break;
					case 9:
						link = `/pages/page/service/extrawork/detail?recordId=${e.businessId}&readState=${e.readState}`
						if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						break;
					case 10:
						console.log(paramsArr);
						const attendanceStatus = paramsArr[2].split('=')[1]
						if (attendanceStatus == 1) {
							link = `/pages/page/service/checking-in/record`
						} else if (attendanceStatus == 2) {
							link = `/pages/page/service/checking-in/checking-in-status?readState=${e.readState}`
						}
						if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						break;
					case 11:
						link = `/pages/page/service/on-business/detail?recordId=${e.businessId}&readState=${e.readState}`
						if (e.readState == 0) this.changePlusNewsStatus(e.recordId)
						break;	
					default:
						break;
				}
				uni.navigateTo({
					url: link
				})
			}
			// 删除消息
			,
			deleteNews (recordId, index) {
				let that = this
				this.plusList[index].scrollLeft = 0
				this.$ytHint.modal({
					content : "确认删除?",
					confirm: () => {
						salAry.getDelete( { recordId : recordId } ).then( (res) => {
							if ( res.data.success ) {
								this.$ytHint.toast({
									title:'删除成功'
								})
								this.oldIndex = null;
								this.theIndex = null;
								setTimeout( () => {
									this.hasMore = true // 下拉刷新重置数据
									this.isNull = false
									this.page.current = 1
									this.getPlusNewsList()
								}, 500)
							}
						})
					},
					cancel : () => {
						console.log("取消");
						this.scrollLeft = 0
						console.log(this.scrollLeft);
					}
				})
			},
		}
	}
</script>
<style scoped lang="scss">
	.toubu {
		position: fixed;
		left: 0;
		overflow: hidden;
		z-index: 998;
		width: 100%;
	}
	.toubu-1{
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 14upx 10upx;
		height: 90upx;
		width: 100%;
		background-color: #FFFFFF;
	}
	scroll-view{
		width: 100%;
		height: 170upx;
		white-space: nowrap;
	}
	.item-news{
		height: 170upx;
		width: 100.2%;
		overflow: hidden;
	}
	.left-news {
		@keyframes showMenu {
			0% {transform: translateX(0);}100% {transform: translateX(-30%);}
		}
		@keyframes closeMenu {
			0% {transform: translateX(-30%);}100% {transform: translateX(0);}
		}
		&.open{
			animation: showMenu 0.15s linear both;
		}
		&.close{
			animation: closeMenu 0.15s linear both;
		}
		z-index: 3;
		position: absolute;
		background-color: #fff;
		left: 0;
	}
	.right-news {
		right: 0;
		z-index: 2;
	}
	.inline-block{
		display: inline-block;
	}
	.item {
		box-shadow: 0px 2px 24px 0px rgba(185, 185, 185, 0.48);
	}
	.content {
		width: 500upx;
	}
	.delete{
		width: 150upx;
		color: #FFF;
		background-color: #fff;
		&.delete-red{
			background-color: red;
		}
	}
	
</style>

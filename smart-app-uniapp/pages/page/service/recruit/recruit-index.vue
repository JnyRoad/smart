<template>
	<view class="vcc fixed mw mh">
		<view class="tab-header mw bgf">
			<scroll-view id="tab-bar rel" class="uni-swiper-tab" scroll-with-animation="true" scroll-x :scroll-left="scrollLeft">
				<view v-for="(tab, index) in tabBars" :key="index" class="swiper-tab-list" :class="tabIndex == index ? 'main-color' : ''" @click="tapTab(index, tab.applyState)">
					{{ tab.applyStateDesc }}
				</view>
			</scroll-view>
			<view class="hbc pt20 pb20 mw overflow-y">
				<view @click="changeAgeOrder" class="hcc f1">
					<text class="f32pc">年龄</text>
					<image :class="{ angleTransfrom: ageOrder=='1' ? true : false }" class="ml10 w30 h25 angle" src="/static/img/serive/recruit/down1.png"></image>
				</view>
				<view @click="changeDeliverOrder" class="hcc f1">
					<text class="f32pc">投递时间</text>
					<image :class="{ angleTransfrom: deliverOrder=='1' ? true : false }" class=" ml10 w30 h25 angle" src="/static/img/serive/recruit/down1.png"></image>
				</view>
				<view @click="flitterCondition" class="hcc f1">
					<image class="mr10 w30 h30" src="/static/img/serive/recruit/screen.png"></image>
					<text class="f32pc">筛选</text>
				</view>
			</view>
		</view>
		<swiper :current="tabIndex" class="swiper-box bgec f1" circular :duration="500" @change="changeTab" @transition="transition">
			<block v-for="(tab, inx) in tabBars" :key="inx">
				<swiper-item class="mh mw">
					<scroll-view v-if="list.length > 0" @scrolltolower="loadMore" class="mw mh " scroll-y>
						<block v-for="(item, index) in list" :key="index">
							<view @click="detail(item.applicationId)" class="hlc mt20 card pl30 ml30 mb30 mw">
								<view :id="'img'+item.applicationId" class="wh228"><image class="wh228" lazy-load :src="item.applicantPhoto" ></image></view>
								<view class="f1 pr40 pl20 ">
									<view class="vlb">
										<view class="f28pc4 hlc">
											职位:
											<span class="ml26 f30pc1">{{ item.jobName }}</span>
										</view>
										<view class="pl80 f30pc1">{{ item.applicantName }}</view>
										<view class="pl80 f30pc1">{{ item.applicantGender }} | {{ item.applicantEducation }} | {{ item.applicantAge }}岁</view>
									</view>
									<view class="hbc mt10">
										<view class="pc6">{{ applyStatus }}</view>
										<view v-if="applyState !== 5" class="button1" @click.stop="addPLan(item.applicationId,item.applicantPhoto)">
											{{ item.isSelect ? '取消备选' : '加入备选' }}
										</view>
									</view>
								</view>
							</view>
						</block>
					</scroll-view>
					<yt-null v-if="isNull" :notice="'暂时没有'+tab.applyStateDesc+'的简历'"></yt-null>
				</swiper-item>
			</block>
		</swiper>
		<!-- 操作 -->
		<view v-if="applyState !== 5" class="fb h100">
			<view class="hlc mh">
				<view @click="planUrl" class="hcc pt20 w200 pb20 rel mh">
					<image class="wh50"  src="/static/img/serive/recruit/Invitation/alternative.png" alt=""></image>
					<view class="badge abs err-bg cf f24 round hcc" :class="{animateText: hasAnimate}">{{ alternativeNum }}</view>
				</view>
				<view class="hrc f1 mh">
					<block v-for="(item, index) in bottomList" :key="index">
						<view @click="dispose(item.operationType)" class="pr30 pl30 h100 hcc cf" :class="[bottomList.length == 2 ? 'f1' : '', item.bgclass]">{{ item.text }}</view>
					</block>
				</view>
			</view>
		</view>
		<view v-if="showSelect" class="selectbox vcc fixed mw mh left0 top0">
			<view @click="showSelect = !showSelect" class="mask abs mw mh left0 top0"></view>
			<view :class="{animateBox: showSelect}" class="select-content vcc rel bgf rad10">
				<navigator url="./recruit-search" hover-class="none" class="f1 hcc mw h100 f32pc2 border-bottom">搜索</navigator>
				<navigator url="./recruit-face" hover-class="none" class="f1 hcc mw h100 f32pc2">拍照找简历</navigator>
			</view>
		</view>
		<recruit-flitter @fillterData="getFillterData" @close="getStatus" :visible="showDawer" />
		<yt-loading />
		<view v-if="hasAnimate" :class="{animateToBottom: hasAnimate}"  class="fixed" :style="{width: animateStyle.width, height: animateStyle.height, left: animateStyle.left, top: animateStyle.top}">
			<image class="mw mh"  :src="animateImg" mode=""></image>
		</view>
	</view>
</template>
<script>
import recruitflitter from '@/components/recruit-flitter.vue';
import recruitment from '@/api/api-recruitment-management.js';
import ytNull from '@/components/yt-null/yt-null.vue';
import dimission from '@/api/api-dimission.js';
import { recruitCtrl } from '@/testjs/recruit/recruit.js';
import { RECURIT_APPLY, storage } from '@/tools/storage.js';
export default {
	components: {
		'recruit-flitter': recruitflitter,
		'yt-null': ytNull
	},
	data() {
		return {
			scrollLeft: 0,
			tabIndex: 0,
			applyState: 0, // 应聘状态
			sexArr: ['男', '女'],
			ageOrder: '0', // 年龄 升序
			deliverOrder: '1', // 投递时间 升序
			showSelect: false, // 点击导航栏加号 弹出框
			showDawer: false, // 筛选 显示底部抽屉
			tabBars: [], // 头部状态筛选
			list: [],
			bottomList: [],
			info: {
				jobId: '', //筛选条件-招聘岗位ID@接口备注
				applyState: 0, //应聘状态@详见数据字典，接口备注
				ageStart: 18, //筛选条件-起始年龄
				ageEnd: 50, //筛选条件-结束年龄
				gender: '', //筛选条件-性别(0-男,1-女)
				ageOrder: '', //年龄排序(0-升序,1-降序)
				deliverOrder: '1', //投递时间排序(0-升序,1-降序)
				applicantName: '', //应聘者名称
				applicantMobile: '' //应聘者联系电话
			}, // 简历筛选操作
			page: {
				current: 1,
				size:10
			}, // 分页
			hasMore: true,
			applyStatus: '已投递',
			applicationIdList: [], // 备选ID
			alternativeNum: 0,
			hastab: true,
			hasChange: false,
			animateImg: '',
			hasAnimate: false,
			animatePos: {},
			isNull: false
		};
	},
	onLoad() {
		this.getTabarList();
		this.bottomList = this.fillterStatus();
	},
	onShow() {
		this.isNull = false
		storage.removeSync(RECURIT_APPLY);
		const isRefresh = uni.getStorageSync('isRefresh');
		if (isRefresh) {
			this.page.current = 1
			this.hasMore = true
			this.list = []
			uni.removeStorageSync('isRefresh');
			this.getlist();
			this.alternativeNum = 0;
		}
	},
	computed: {
		animateStyle() {
			let winWidth = uni.getSystemInfoSync().screenWidth
			let animateStyle = {}
			animateStyle.width = `${this.animatePos.width}px`,
			animateStyle.height = `${this.animatePos.height}px`,
			animateStyle.left = `${this.tabIndex*winWidth + this.animatePos.left}px`,
			animateStyle.top = `${this.animatePos.top}px`
			return animateStyle
		}
	},
	// 导航栏按钮事件
	onNavigationBarButtonTap(e) {
		this.showSelect = true;
	},
	onHide() {
		// 监听页面关闭
		this.showSelect = false;
	},
	methods: {
		transition (e) {
			// console.log(e.detail);
		},
		// 获取头部状态筛选列表
		async getTabarList() {
			try {
				const res = await recruitment.optoTypeList();
				if (!res) return
				this.tabBars = res.data.data;
				// this.tabBars.sort(this.sortId) // 排序
				this.getlist();
			} catch (e) {
				throw e;
			}
		},
		loadMore() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据了!'
				})
				return
			}
			this.page.current++;
			this.getlist();
		},
		// 轮播滚动
		changeTab(e) {
			console.log(e.target.current);
			this.isNull = false
			this.page.current = 1
			this.tapTab(e.target.current);
			this.hastab = false;
			this.changeList(e);
		},
		// 切换分类导航
		tapTab(e, index) {
			console.log(index);
			this.isNull = false
			this.page.current = 1
			this.hastab = true;
			if (this.tabIndex == e) return;
			this.changeList(e, index);
		},
		// 统一处理时间
		changeList(e, index) {
			this.hasMore = true
			if (!this.hastab) return;
			this.list = [];
			this.applicationIdList = [];
			let tabIndex = -1;
			tabIndex = parseInt(e);
			this.tabIndex = tabIndex;
			console.log(this.tabIndex);
			this.info.applyState = index;

			this.getlist(); // 调用当前条件下的招聘信息列表
			// for (let el of this.tabBars) {
			// 	console.log(el);
			// 	console.log(inx);
			// 	if (el.applyState == tabIndex) {
			// 		this.applyStatus = el.applyStateDesc;
			// 		break;
			// 	}
			// }
			this.tabBars.forEach((el,inx) => {
				if (inx == this.tabIndex) {
					this.applyStatus = el.applyStateDesc
					this.applyState = el.applyState
				}
			})
			this.alternativeNum = 0;
			this.bottomList = this.fillterStatus();
			const windowWidth = uni.getSystemInfoSync().windowWidth;
			const leng = this.tabBars.length;
			this.scrollLeft = ((tabIndex - 2) * windowWidth) / leng;
			console.log(tabIndex);
		},
		// 年龄排序 默认为空
		changeAgeOrder() {
			this.deliverOrder = '';
			this.info.deliverOrder = '';
			if (this.ageOrder == '0') {
				this.ageOrder = '1';
				this.info.ageOrder = '1';
				this.getlist();
			} else if (this.ageOrder == '1') {
				this.ageOrder = '0';
				this.info.ageOrder = '0';
				this.getlist();
			} else if (this.ageOrder == '') {
				this.ageOrder = '0';
				this.info.ageOrder = '0';
				this.getlist();
			}
			
		},
		// 投递时间排序
		changeDeliverOrder() {
			this.ageOrder = '';
			this.info.ageOrder = '';
			if (this.deliverOrder == '0') {
				this.deliverOrder = '1';
				this.info.deliverOrder = '1';
				this.getlist();
			} else if (this.deliverOrder == '1') {
				this.deliverOrder = '0';
				this.info.deliverOrder = '0';
				this.getlist();
			} else if (this.deliverOrder == '') {
				this.deliverOrder = '0';
				this.info.deliverOrder = '0';
				this.getlist();
			}
		},
		//获取简历列表
		async getlist() {
			try {
				const res = await recruitment.applicationList(this.page, this.info);
				if (!res) return
				if (this.page.current === 1) {
					this.alternativeNum = 0
					if (res.data.data.records.length === 0) {
						this.isNull = true
						return
					}
					res.data.data.records.forEach(el => {
						el.isSelect = false;
					});
					this.list = res.data.data.records;
				} else {
					if (!res.data.data.records.length) {
						this.hasMore = false
						return
					} 
					res.data.data.records.forEach(el => {
						el.isSelect = false
					})
					this.list.push(...res.data.data.records)
				}
				
			} catch (e) {
				if (e.errMsg) {
					this.$ytHint.toast({
						title: '网络超时'
					})
				}
				throw e
			}
		},
		// 详情
		detail(applicationId) {
			uni.navigateTo({
				url: `../recruit/recruit-detail?applicationId=${applicationId}&applyState=${this.applyState}`
			});
		},
		// 加入备选
		addPLan(applicationId,img) {
			let id = `#img${applicationId}`
			
			for (let el of this.list) {
				if (el.applicationId === applicationId) {
					if (el.isSelect) {
						el.isSelect = false;
						this.alternativeNum--;
						this.$ytHint.toast({
							title: '已取消备选'
						});
						this.hasAnimate = false
					} else {
						el.isSelect = true;
						this.alternativeNum++;
						this.$ytHint.toast({
							title: '加入备选成功!'
						});
						this.hasAnimate = true
						this.animateImg = img
						this.getWidHeiById(id)
						setTimeout(() => {
							this.hasAnimate = false
						},1700)
					}
					break;
				}
			}
		},
		// 跳转备选
		planUrl() {
			if (!this.alternativeNum) return;
			this.savePlanList();
			uni.navigateTo({
				url: `./recruit-plan-index?applyState=${parseInt(this.applyState)}`
			});
		},
		// 跳转统一处理页面
		dispose(operationType) {
			if (!this.alternativeNum) {
				this.$ytSystem.vibrate({
					title: '至少备选一位员工'
				});
				return;
			}
			this.savePlanList();
			uni.navigateTo({
				url: `./refuse-entry?operationType=${operationType}&applyState=${parseInt(this.applyState)}`
			});
		},
		// 通过招聘状态筛选操作
		fillterStatus() {
			let num = parseInt(this.applyState);
			console.log(num);
			switch (num) {
				case 0:
					return recruitCtrl.status1; // 已投递
					break;
				case 2:
					return recruitCtrl.status2; // 已邀请
					break;
				case 3:
					return recruitCtrl.status3; // 待入职 待复试
					break;
				case 4:
					return recruitCtrl.status4; // 待入职 待复试
					break;
				case 1:
					return recruitCtrl.status5; // 已拒绝 已入库
					break;
				case 6:
					return recruitCtrl.status5; // 已拒绝 已入库
					break;
				default:
					break;
			}
		},
		// 缓存备选
		savePlanList() {
			storage.removeSync(RECURIT_APPLY);
			let applyList = [];
			this.list.forEach(el => {
				if (el.isSelect) {
					applyList.push(el);
				}
			});
			storage.set(RECURIT_APPLY, applyList);
		},
		// 筛选
		flitterCondition() {
			this.isNull = false
			this.showDawer = !this.showDawer;
		},
		getStatus(e) {
			this.showDawer = e;
		},
		getFillterData(e) {
			this.isNull = false
			this.page.current = 1
			this.list = []
			this.info.ageEnd = e.ageEnd;
			this.info.ageStart = e.ageStart;
			this.info.gender = e.gender == 3 ? '' : e.gender;
			this.getlist();
		},
		getWidHeiById(id) {
			let view = uni.createSelectorQuery().select(id);
			view.boundingClientRect(data => {
				this.animatePos =  data
			}).exec();
		},
	}
};
</script>

<style lang="scss" scoped>
.tab-header {
	/* #ifdef APP-PLUS */
	top: 0;
	/* #endif */
	/* #ifdef H5 */
	top: 88upx;
	/* #endif */
	z-index: 990;
	box-shadow: 0px 5upx 5upx 0px rgba(185, 185, 185, 0.48);
}
.swiper-box {
	width: 100%;
	flex: 1;
	padding-bottom: 100upx;
	box-sizing: border-box;
	.placeholder {
		opacity: 1;
		transition: opacity 0.4s linear;
	}
}
.uni-tab-bar-loading {
	position: fixed;
	top: 0;
	width: 100%;
	height: 100%;
	text-align: center;
	font-size: 28upx;
	color: #999;
}
.wh228 {
	width: 228upx;
	height: 228upx;
}
.card {
	width: 690upx;
	height: 291upx;
	background: rgba(255, 255, 255, 1);
	box-shadow: 0px 5px 18px 0px rgba(185, 185, 185, 0.5);
	border-radius: 20upx;
}
.wh50 {
	width: 50upx;
	height: 50upx;
}
.wh72 {
	width: 72upx;
	height: 72upx;
}
.wh116 {
	width: 116upx;
	height: 116upx;
}
.fixd-right {
	position: fixed;
	right: 20upx;
	bottom: 100upx;
}
.badge {
	width: 36upx;
	height: 36upx;
	left: 60%;
	top: 10upx;
}
.selectbox {
	z-index: 998;
	.mask {
		background: rgba(0, 0, 0, 0.5);
	}
	.select-content {
		width: 600upx;
	}
	
}
.angle {
	transition: 0.3s ease-in-out;
	transform: rotateZ(0);
}
.angleTransfrom {
	transition: 0.3s ease-in-out;
	transform: rotateZ(-180deg);
}

@keyframes animateImg {
	from {transform: rotateZ(0deg) scale(1); }
	to {top: 700px; left: 10px; transform: rotateZ(360deg) scale(.1)}
}

.animateToBottom {
	animation: animateImg 1.5s ease-in-out .2s;
}

@keyframes animateText {
	0% {transform: scale(1)}
	50% {transform: scale(1.5)}
	100% {transform: scale(1)}
}

.animateText {
	animation: animateText .6s ease-out 1.1s;
}

@keyframes animateBox {
	0% {transform: scale(0)}
	80% {transform: scale(1.2)}
	100% {transform: scale(1)}
}

.animateBox {
	animation: animateBox .5s ease-out;
}

</style>

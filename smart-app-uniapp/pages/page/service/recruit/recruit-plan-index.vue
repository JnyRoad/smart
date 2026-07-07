<template>
	<view class="mw">
		<view class="yt-title hcc border-bottom-D" :style="{ height: titleHeight + 'px' }">
			<view :class="['title hcc rel', isIOS ? 'ios' : 'android']">
				<view @click="back" class="arrowback w50 h60 hcc"><image class="w25 h40" src="/static/img/mine/next.png" mode=""></image></view>
				<text class="text">备选</text>
				<view @click="hasEdit = !hasEdit" class="abs btn-text">{{ hasEdit ? '完成' : '编辑' }}</view>
			</view>
		</view>
		<view class="pb120 mw pl30 pr30" :style="{ 'padding-top': titleHeight + 'px' }">
			<view class="button3 f1 pt20 pb20 ml30 mr30 mt40 mb40 rad8">共{{ list.length }}个备选</view>
			<view class="mw">
				<block v-for="(item,index) in list" :key="index">
					<view class="hlc card pl30 mb30 rel bgf" >
					    <view class="wh220 borderC">
					    	<image class="" :src="item.applicantPhoto" alt=""></image>
					    </view>
					    <view class="f1 pr40 pl20 ">
					        <view class="">
					            <view class="f28pc4 hlc">职位:<span class="ml26 f30pc1">{{item.jobName}}</span></view>
					            <view class="pl80 f30pc1">{{item.applicantName}}</view>
					            <view class="pl80 f30pc1">{{item.applicantGender}} | {{item.applicantEducation}} | {{item.applicantAge}}岁</view>
					        </view>
					        <view class="hbc mt10">
					            <view class="pc6 f1">{{applyStateDesc}}</view>
					        </view>
					    </view>
						<view @click="changeOne(index)" class="">
							<image class="wh60 card-check" :src="item.isSelect?'/static/img/serive/recruit/Invitation/success.png':'/static/img/serive/recruit/check.png'" alt=""></image>
						</view>
					</view>
				</block>
			</view>
			<!-- 操作 -->
			<view class="fb h100">
				<view class="hlc mh">
					<view v-if="!hasEdit" @click="doSelectAll" class="hcc w200 mh">
						<image class="wh50 mr20" :src="selectAll ? '/static/img/serive/recruit/Invitation/success.png' : '/static/img/serive/recruit/check.png'" alt=""></image>
						<text class="f32pc2">全选</text>
					</view>
					<view v-if="!hasEdit" class="hrc f1 mh">
						<block v-for="(item, index) in bottomList" :key="index">
							<view @click="dispose(item.operationType)" :class="['pr30 pl30 h100 hcc cf', bottomList.length == 2 ? 'f1' : '', item.bgclass]">{{ item.text }}</view>
						</block>
					</view>
					<view v-if="hasEdit" class="hrc f1 mh"><view @click="deletplanSelect" :class="['pr60 pl60 h100 hcc cf err-bg']">删除</view></view>
				</view>
			</view>
		</view>
		<yt-loading />
	</view>
</template>
<script>
import manageItem from '@/components/manage-item.vue';
import dimission from '@/api/api-dimission.js';
import { applyState } from '@/config/dictionaries.js';
import { RECURIT_APPLY, storage } from '@/tools/storage.js';
import { recruitCtrl } from '@/testjs/recruit/recruit.js';
import myMixin from '@/mixins/index.vue';
export default {
	mixins: [myMixin],
	components: {
		manageItem
	},
	data() {
		return {
			selectAll: false,
			applyState: -1,
			hasEdit: false,
			applicationId: [],
			list: [],
			bottomList: [],
			applyStateDesc: ''
		};
	},
	onLoad(e) {
		this.getTitleHeight();
		this.applyState = e.applyState;
		this.applyStateDesc = applyState[e.applyState]
		this.getLocalData();
	},
	onReady() {
		this.bottomList = this.fillterStatus();
	},
	methods: {
		// 从本地获取备选数据
		getLocalData() {
			try {
				this.list = storage.getSync(RECURIT_APPLY);
				this.list.forEach(el => {
					el.isSelect = false;
				});
			} catch (e) {
				console.log(e);
				throw e
			}
		},
		back() {
			uni.navigateBack({
				delta: 1
			});
		},
		deletplanSelect() {
			let arr = []
			for (let i = 0, len = this.list.length; i < len; i++) {
				if (this.list[i].isSelect) {
					arr.push(this.list[i].applicationId)
				}
			}
			if (arr.length === 0) {
				this.$ytSystem.vibrate({
					title: '至少选中一个备选员工'
				})
				return
			}
			this.$ytHint.modal({
				content: '确定要删除以下备选人员吗?',
				showCancel: false,
				confirm: () => {
					arr.forEach(el => {
						this.list.forEach((el1, inx1) => {
							if (el1.applicationId == el) {
								this.list.splice(inx1, 1)
								if (!this.list.length) {
									this.$ytHint.modal({
										content: '当钱没有备选员工,请返回招聘',
										showCancel: false,
										confirmText: '返回招聘',
										confirm: () => {
											uni.setStorageSync('isRefresh',true)
											uni.navigateBack({
												delta: 1
											})
										}
									})
								}
							}
						})
					})
				}
			})
		},
		changeOne(inx) {
			this.list.forEach((el, index) => {
				if (index === inx) {
					el.isSelect = !el.isSelect
				} 
			})
			let flag = true
			for (let i = 0, len = this.list.length; i < len; i++) {
				if (!this.list[i].isSelect) {
					flag = false
					break
				}
			}
			this.selectAll = flag
		},
		doSelectAll() {
			this.selectAll = !this.selectAll;
			this.list.forEach(el => {
				el.isSelect = this.selectAll 
			})
		},
		dispose(operationType) {
			let arr = []
			for (let i = 0, len = this.list.length; i < len; i++) {
				if (this.list[i].isSelect) {
					arr.push(this.list[i])
				}
			}
			if (arr.length === 0) {
				this.$ytSystem.vibrate({
					title: '至少选中一个备选员工'
				})
				return
			}
			storage.removeSync(RECURIT_APPLY)
			storage.setSync(RECURIT_APPLY,arr)
			uni.redirectTo({
				url: `./refuse-entry?operationType=${operationType}&applyState=${this.applyState}`
			});
		},
		fillterStatus() {
			const num = parseInt(this.applyState);
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
				case 6:
					return recruitCtrl.status5; // 已拒绝 已入库
				default:
					break;
			}
		}
	}
};
</script>

<style lang="scss" scoped>
.wh220 {
	width: 220upx;
	height: 220upx;
}	
.wh50 {
	width: 50upx;
	height: 50upx;
}
.arrowback {
	position: absolute;
	left: -130upx;
	transform: rotateZ(180deg);
}
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
.card {
        width:690upx;
        height:291upx;
        background:rgba(255,255,255,1);
        box-shadow:0px 5px 18px 0px rgba(185,185,185,0.5);
        border-radius:20upx;
    }
    .wh60{
        width: 60upx;
        height: 60upx;
	}
	.card-check {
		position: absolute;
		top: -20upx;
		left: -10upx;
		z-index: 2;
	}
</style>

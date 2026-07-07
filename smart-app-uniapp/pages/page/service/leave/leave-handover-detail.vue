<template>
	<view class="mw">
		<view class="table">
			<view class="mw h100 hbc border-bottom-D bgf">
				<view class="thead f32pc hcc">责任部门</view>
				<view class="thead f32pc hcc">交接项目</view>
				<view class="thead f32pc hcc">金额</view>
				<view class="thead f32pc hcc">说明</view>
				<view class="thead f32pc hcc">交接人</view>
			</view>
			<view class="mw bgf">
				<block v-for="(item1, index1) in list" :key="index1">
					<block v-for="(item2, index2) in item1.handItem" :key="index2">
						<view class="mw hbc h100 border-bottom-D">
							<view class="tbody">{{item1.deptName}}</view>
							<view class="tbody text2">{{item2.itemName}}</view>
							<view class="tbody">{{item2.itemAmt}}</view>
							<view @click="showModal(item2.itemDesc)" class="tbody text1">{{item2.itemDesc}}</view>
							<view class="tbody vcc">
								<text>{{item2.receiverName}}</text>
								<text>{{item2.receiverId}}</text>
							</view>
						</view>
					</block>
				</block>
			</view>
		</view>
		<view class="fb">
			<view class="mw pr30 pl30">
				<text class="f32pc">温馨提示</text>
				<view class="f30pc">
					离职当月工资（每月7日前离职者，含上月工资）将在一星期之内发放完毕
				</view>
			</view>
			<view v-if="approveStatus == 3" class="bgec hcc f1 f32pc pt20 pb20">工作交接中</view>
			<view @click="apply" v-if="approveStatus == 4" class="main-bg hcc f1 f32 cf pt20 pb20">提交离职交接书</view>
			<view v-if="approveStatus == 5" class="bgec hcc f1 f32pc4 pt20 pb20">已提交离职交接书</view>
		</view>
		<view v-if="modalShow" class="notice-modal">
			<view @click="modalShow = !modalShow" class="notice-mask"></view>
			<view class="notice-content vcc">
				<view class="hcc">
					{{noticeDesc}}
				</view>
				<view class="mw hcc mt80">
					<yt-button @click="modalShow = !modalShow" btnWidth="91" btnHeight="36" btnText="确定"></yt-button>
				</view>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue';
import ytlistitem from '@/components/yt-list-item/index.vue';
import ytbutton from '@/components/yt-button/index.vue'
import dimission from '@/api/api-dimission.js';
export default {
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'yt-button': ytbutton
	},
	data() {
		return {
			list: [],
			approveStatus: -1,
			processId: -1,
			noticeDesc: '' ,// 项目说明
			modalShow: false
		};
	},
	onLoad: function(options) {
		if (options) {
			this.processId = options.processId; // 流程id
			this.approveStatus = options.approveStatus
		}
		this.getWorkHand();
	},
	methods: {
		// 进度详情
		async getWorkHand () {
			try {
				const res = await dimission.dimissionRecordWorkhand(this.processId);
				this.list = res.data.data.handover;
				this.approveStatus = res.data.data.approveStatus
			} catch (e) {
				console.log(e);
				throw e;
				//TODO handle the exception
			}
		},
		// 提交离职交接书
		apply () {
			dimission.exchangeJobCommit(this.processId).then(res => {
				uni.redirectTo({
					url: './success'
				})
			})
		},
		showModal(desc) {
			this.modalShow = true
			this.noticeDesc = desc
		}
	}
};
</script>

<style lang="scss" scoped>
.table {
	width: 100%;
	padding-bottom: 280upx;
}
.thead {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	text-align: center;
	width: 24%;
	height: 100%;
	overflow: hidden;
	line-height: normal;
}

.tbody {
	text-align: center;
	width: 24%;
}

.line {
	height: 90%;
}
.f5 {
	flex: 5;
}
.f3 {
	flex: 2;
}
.notice-modal {
	position: fixed;
	width: 100%;
	height: 100%;
	left: 0;
	top: 0;
	.notice-mask {
		position: absolute;
		width: 100%;
		height: 100%;
		left: 0;
		top: 0;
		background-color: rgba(0,0,0, .6)
	}
	.notice-content {
		position: relative;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		border-radius: 20upx;
		width: 600upx;
		height: 400upx;
		padding: 30upx;
		background-color: #fff;
	}
}
</style>

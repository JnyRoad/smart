<template>
	<view v-if="leaveDetail.entryTime" class="mw rel bgf">
		<yt-list :ytClass="['pl30', 'pr30']">
			<yt-list-item showArrow="false" :title="employeeNameId" showImage="false" >
				<view class="f30pc bold">
					入职日期: {{leaveDetail.entryTime}}
				</view>
			</yt-list-item>
			<yt-list-item showArrow="false" :title="'公司'" showImage="false">
				<view class="f30pc bold">
					{{leaveDetail.buName}}
				</view>
			</yt-list-item>
			<yt-list-item showArrow="false" :title="leaveDetail.depName" showImage="false">
				<view class="f30pc bold">
					{{leaveDetail.jobName}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw bgec h20"></view>
		<yt-list :ytClass="['pl30', 'pr30']">
			<yt-list-item showArrow="false" title="申请离职日期" showImage="false" :tipContent="leaveDetail.dismissionDate"></yt-list-item>
			<yt-list-item showArrow="false" title="离职原因" showImage="false" :tipContent="leaveDetail.dismissionReasonDesc"></yt-list-item>
			<yt-list-item showArrow="false" title="离职类型" showImage="false" :tipContent="leaveDetail.dismissionTypeDesc"></yt-list-item>
			<yt-list-item showArrow="false" title="剩余年假天数" showImage="false" :tipContent="leaveDetail.restDatCount + '天'"></yt-list-item>
		</yt-list>
		<view class="mw bgec h20"></view>
		<!-- 审核流程 -->
		<yt-approval-process :flows="flows" />
		<view @click="exchangeJob" v-if="approveStatus == 1 && type == 0 && !showReadStatus" class="hcc mw mb40"><yt-button  btnText="开始交接工作" btnWidth="180" btnHeight="54"></yt-button></view>
		<show-read :showRead="showReadStatus"></show-read>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue';
import ytlistitem from '@/components/yt-list-item/index.vue';
import ytbutton from '@/components/yt-button/index.vue';
import ytApprovalProcess from '@/components/yt-approval-process.vue'
import showRead from '@/components/show-read.vue'
import dimission from '@/api/api-dimission.js';
export default {
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'yt-button': ytbutton,
		'yt-approval-process': ytApprovalProcess,
		'show-read': showRead
	},
	data() {
		return {
			type: '',
			approval: false, // 是否通过申请
			employeeNameId: '',
			leaveDetail: {},
			flows: [], // 审批流程
			processId: '' ,// 审批流程id
			approveStatus: -1 ,// 审批状态
			showReadStatus: false
		};
	},
	onLoad (e) {
		if (e && e.recordTitle) {
			this.type = e.type // 0 正常 1 异常
			this.processId = e.processId; // 审批流程id
			this.approveStatus = e.approveStatus
			uni.setNavigationBarTitle({
				// 设置title
				title: e.recordTitle
			});
		}
		if (e.readState != undefined && e.readState == 1) {
			this.showReadStatus = true // 消息模块过来的才有这个状态
		}
		this.getDetail()
	},
	methods: {
		// 离职详情
		async getDetail() {
			try{
				const res = await dimission.dimissionRecordDetail(this.processId);
				this.leaveDetail = res.data.data.employee;
				this.approveStatus = this.leaveDetail.approveStatus
				console.log(this.approveStatus);
				// 拼接名字和id
				this.employeeNameId = `${res.data.data.employee.employeeName}     ${res.data.data.employee.employeeId}`
				this.flows = res.data.data.flow;
			}catch(e){
				console.log(e);
				throw e
			}
		},
		// 开始离职交接
		exchangeJob() {
			dimission.exchangeJobStart(this.processId).then(res => {
				uni.redirectTo({
					url: `./leave-handover-detail?processId=${this.processId}`
				})
				if (!this.showReadStatus) {
					this.changePlusNewsStatus(this.processId) // 改变已读消息
				}
			})
		}
	}
};
</script>

<style scoped>
.line {
	left: 18upx;
	height: 70%;
}
</style>

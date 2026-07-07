<template>
	<view v-if="extraworkDetail.employeeName" class="yt-page pb200 bgf">
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false :title="extraworkDetail.employeeName">
				<view class="f30pc bold">
					{{extraworkDetail.employeeBadge}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="公司">
				<view class="f30pc bold">
					{{extraworkDetail.buName}}
				</view>
			</yt-list-item>
			<yt-list-item hideLastBorder=true showImage=false showArrow=false :title="extraworkDetail.deptName">
				<view class="f30pc bold">
					{{extraworkDetail.jobName}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false title="加班日期" :tipContent="extraworkDetail.extraworkDate"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="班别" :tipContent="extraworkDetail.extraworkClassDesc"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="加班类型" :tipContent="extraworkDetail.extraworkTypeDesc"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="二入" :tipContent="extraworkDetail.startDate2"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="二出" :tipContent="extraworkDetail.endDate2"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="四入" :tipContent="extraworkDetail.startDate4"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="四出" :tipContent="extraworkDetail.endDate4"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="五入" :tipContent="extraworkDetail.startDate5"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="五出" :tipContent="extraworkDetail.endDate5" hideLastBorder=true></yt-list-item>
			
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false title="时长">
				<view class="f28pc4">
					<text class="main-color f32">{{extraworkDetail.extraworkCount}}</text>小时
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="是否出差加班" :tipContent="extraworkDetail.isTravelExtrawork"></yt-list-item>
			<yt-list-item hideLastBorder=true showImage=false showArrow=false title="加班原因" :tipContent="extraworkDetail.extraworkDesc"></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<show-read :showRead="showReadStatus"></show-read>
		<yt-approval-process :flows="flows"></yt-approval-process>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytapprovalprocess from '@/components/yt-approval-process.vue'
	import ytapprovalsuccess from '@/components/yt-approval-success.vue'
	import extrawork from '@/api/api-extrawork.js'
	import showRead from '@/components/show-read.vue'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-approval-process':ytapprovalprocess,
			'yt-approval-success': ytapprovalsuccess,
			'show-read': showRead
		},
		data () {
			return {
				approval: false, // 是否通过申请
				extraworkDetail: {},
				flows:[], // 审批流程
				recordId: '', // 审批流程id
				showReadStatus: false
			}
		},
		onLoad (e) {
			this.recordId = e.recordId
			this.getextraworkDetail()
			if (e.readState && e.readState == 1) {
				this.showReadStatus = true // 消息模块过来的才有这个状态
			}
		},
		methods: {
			async getextraworkDetail () {
				const obj = {
					recordId: this.recordId
				}
				const res = await extrawork.extraworkRecordDetail(obj)
				this.extraworkDetail = res.data.data.employee
				this.flows = res.data.data.flow
			},
		},
	}
</script>

<style>
</style>

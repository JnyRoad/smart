<template>
	<view  v-if="restDetail.vacateTypeDesc" class="yt-page bgf">
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false title="调休类型" :tipContent="restDetail.vacateTypeDesc"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="可调休天数" :tipContent="restDetail.restAbleCount"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="出勤日期" :tipContent="restDetail.workDate"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="现在要用天数" :tipContent="restDetail.restCount"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="需调休日期" :tipContent="restDetail.restDate"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="备注" :tipContent="restDetail.restDesc"></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-approval-process :flows="flows"></yt-approval-process>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytapprovalprocess from '@/components/yt-approval-process.vue'
	import ytapprovalsuccess from '@/components/yt-approval-success.vue'
	import rest from '@/api/api-rest.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-approval-process':ytapprovalprocess,
			'yt-approval-success': ytapprovalsuccess
		},
		data () {
			return {
				approval: false, // 是否通过申请
				restDetail: {},
				flows:[
					
				], // 审批流程
				recordId: '', // 审批流程id
			}
		},
		onLoad (e) {
			this.recordId = e.recordId
			this.getRestDetail()
		},
		methods: {
			async getRestDetail () {
				const obj = {
					recordId: this.recordId
				}
				const res = await rest.restRecordDetail(obj)
				this.restDetail = res.data.data.employee
				this.flows = res.data.data.flow
			}
		},
	}
</script>

<style>
</style>

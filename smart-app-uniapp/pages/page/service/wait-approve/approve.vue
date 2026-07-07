<template>
	<view class="yt-page bgf">
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item title="王虎 D0001" tipContent="2019-02-21入职" showArrow=false showImage=false></yt-list-item>
			<yt-list-item title="运维管理部" tipContent="网络工程师" showArrow=false showImage=false></yt-list-item>
			<yt-list-item title="申请离职日期" tipContent="2019-02-02" showArrow=false showImage=false></yt-list-item>
			<yt-list-item hideLastBorder=true title="离职原因" tipContent="已填写" showArrow=false showImage=false></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-approval-process ></yt-approval-process>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytList from '@/components/yt-list/index.vue'
	import ytListItem from '@/components/yt-list-item/index.vue'
	import ytApprovalProcess from '@/components/yt-approval-process.vue';
	export default {
		components: {
			ytList,
			ytListItem,
			ytApprovalProcess
		},
		data() {
			return {
				
			}
		},
		
		methods: {
			name() {
				
			}
		},
	}
</script>

<style>
</style>

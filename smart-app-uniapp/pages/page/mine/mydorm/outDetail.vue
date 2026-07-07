<template>
	<view class="yt-page bgf">
		<view class="pr30 pl30 pt20 pb20 f34pc2">
			<view>外宿地址:</view>
			<view>{{detail.outAddress}}</view>
		</view>
		<view class="zhu">
			
		</view>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false title="补贴开始月份" :tipContent="detail.startTime"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="补贴结束月份" :tipContent="detail.endTime"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="补贴类型" :tipContent="detail.allowanceType"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="补贴金额" :tipContent="detail.amount"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="计算规则" :tipContent="detail.computaionRule"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="补贴说明" :tipContent="detail.explain" v-if="detail.explain"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="备注" :tipContent="detail.remark" v-if="detail.remark"></yt-list-item>
		</yt-list>
		<yt-approval-process :flows="flows"></yt-approval-process>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytapprovalprocess from '@/components/yt-approval-process.vue'
	import {employee} from '@/api/api-mine.js' // 请求接口的参数
export default {
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'yt-approval-process': ytapprovalprocess
	},
	data() {
		return {
			flows: [],
			detail:{}
		}
	},
	onLoad() {
		this.getDetail()
	},
	methods: {
		async getDetail() {
			try{
				const res = await employee.getOutDetail()
				this.detail = res.data.data
				this.flows = res.data.data.flow
				console.log(this.flows);
			}catch(e){
				//TODO handle the exception
				console.log(e);
				throw e
			}
		}
	},
}
</script>

<style>
	.zhu{
		width: 100%;
		height: 5upx;
		background-color: #ECECEC;
	}
</style>

<template>
	<view class="bgec w750">
		<leaveItem @click="toDetail" v-if="!isNull" :list="record" :type="type" :status="'leave'" />
		<yt-null v-if="isNull" notice="暂时没有离职办理记录"></yt-null>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import leaveItem from '@/components/leave-item';
import ytNull from '@/components/yt-null/yt-null.vue'
import dimission from '@/api/api-dimission.js';
import {flitterTime, formatDate} from '@/common/js/util.js'
export default {
	components: {
		leaveItem,
		ytNull
	},
	data() {
		return {
			record: [],
			type: '' ,//是否正常离职
			page: {
				current: 1,
				size: 10
			},
			isNull: false
		};
	},
	onLoad: function(options) {
		if (options.type) {
			let type = options.type =='0'? '正常离职记录' : '异常离职记录';
			this.type = options.type;
			uni.setNavigationBarTitle({
				// 设置title
				title: type
			});
		}
		this.getRecordList();
	},
	onPullDownRefresh() {
		this.record = []
		this.hasMore = true
		this.page.current = 1
		this.getRecordList()
	},
	onReachBottom() {
		if (!this.hasMore) {
			this.$ytHint.toast({
				title: '没有更多数据了！'
			})
			return
		}
		this.page.current++
		this.getRecordList()
	},
	methods: {
		// 区别跳转
		toDetail(e) {
			if (e.approveStatus == 3 || e.approveStatus == 4 || e.approveStatus == 5) {
				uni.navigateTo({
					url: `./leave-handover-detail?processId=${e.processId}&approveStatus=${e.approveStatus}`
				})
			} else {
				uni.navigateTo({
				    url: `./leave-detail?processId=${e.processId}&recordTitle=${e.recordTitle}&type=${e.type}&approveStatus=${e.approveStatus}`
				});
			}
		},
		// 离职记录
		async getRecordList() {
			try{
				const res = await dimission.dimissionRecordList(this.page,this.type);
				if (!res) return
				uni.stopPullDownRefresh()
				if (this.page.current === 1) {
					if (!res.data.data.records.length) {
						this.isNull = true
						return
					}
					res.data.data.records.forEach(el => {
						el.timeDesc = this.dayFormat(el.recordDate,'date')
					})
					this.record = res.data.data.records
				} else {
					if (!res.data.data.records.length) {
						this.hasMore = false
					} else {
						res.data.data.records.forEach(el => {
							el.timeDesc = this.dayFormat(el.recordDate,'date')
						})
						this.record.push(...res.data.data.records)
					}
				}
			}catch(e){
				console.log(e);
				throw e
			}
		},
		dayFormat (date) {
			const curTime = Date.now()
			const dateTime = new Date(flitterTime(date)).getTime()
			const day = Math.floor((curTime-dateTime)/86400000)
			switch (day){
				case 0:
					return '今天'
					break;
				case 1:
					return '昨天'
					break;
				case 2:
					return '前天'
					break;
				default:
					return date
					break;
			}
		}
	}
};
</script>

<style>
.gray {
	background: #ececec;
}
</style>

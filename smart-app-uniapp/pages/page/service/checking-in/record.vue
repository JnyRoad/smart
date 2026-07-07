<template>
	<view class="bgec w750">
		<yt-record-item v-if="list.length>0" :list="list" type="5" @click="onDetail"/>
		<yt-null v-if="isNull" notice="暂时没有补卡办理进度"> </yt-null>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytrecorditem from '@/components/yt-record-item.vue'
	import attendance from '@/api/api-attendance.js'
	import ytNull from '@/components/yt-null/yt-null.vue'
	import {storage} from '@/tools/storage.js'
	export default {
		components: {
			'yt-record-item': ytrecorditem,
			'yt-null': ytNull
		},
		data () {
			return {
				type: 5, // 自定义字段 可取config->dictionaries.js 查看规则
				list:[
					
				],
				page: {
					current: 1,
					size: 10
				},
				timeout: 0,
				hasMore: true,
				isNull: false
			}
		},
		onLoad() {
			this.getRecordList()
		},
		 // 返回退回到考勤列表
		onPullDownRefresh() {
			this.page.current = 1
			this.getRecordList()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多了'
				})
				return
			}
			this.page.current++
			this.getRecordList()
		},
		methods: {
			async getRecordList() {
				try{
					const res = await attendance.patchRecordList(this.page)
					uni.stopPullDownRefresh()
					if (this.page.current === 1) {
						if (!res.data.data.records.length) {
							this.isNull = true
							return
						}
						this.list = res.data.data.records;
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
						} else {
							this.list.push(...res.data.data.records)
						}
					}
				}catch(e){
					console.log(e);
					throw e
				}
			},
			onDetail (obj) {
				uni.navigateTo({
					url:`./detail?patchDate=${obj.patchDate}&recordId=${obj.recordId}`
				})
			}, // 补卡详情
		},
	}
</script>

<style>
</style>

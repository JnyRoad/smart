<template>
	<view class="bgec w750">
		<yt-record-item @click="toDetail" v-if="!isNull" :list="list" type="3" />
		<yt-null v-if="isNull" notice="暂时没有加班办理进度"> </yt-null>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytrecorditem from '@/components/yt-record-item.vue'
	import ytNull from '@/components/yt-null/yt-null.vue'
	import extrawork from '@/api/api-extrawork.js'
	export default {
	    components: {
	        'yt-record-item': ytrecorditem,
			'yt-null': ytNull
	    },
		data() {
		    return {
		        list:[],
				page: {
					current: 1,
					size: 10
				},
				hasMore: true,
				isNull: false
		    }
		},
		onLoad() {
			this.getExtraworkRecord()
		},
		onPullDownRefresh() {
			this.list = []
			this.hasMore = true
			this.page.current = 1
			this.getExtraworkRecord()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据'
				})
				return
			}
			this.page.current++
			this.getExtraworkRecord()
		},
		methods: {
			async getExtraworkRecord () {
				const res = await extrawork.extraworkRecordList(this.page)
				uni.stopPullDownRefresh()
				if (!res) return
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
			},
			toDetail (e) {
				uni.navigateTo({
					url: `./detail?recordId=${e}`
				})
			}
		}
	}
</script>

<style>
</style>

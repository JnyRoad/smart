<template>
	<view class="mw">
		<view class="title pl30">常见问题分类</view>
		<yt-list :ytClass="['pl30','pr30']">
			<scroll-view scroll-y @scrolltolower="loadMore">
				<block v-for="(item, index) in questionList" :key="index">
					<yt-list-item :navUrl="'./question-detail?questionId='+item.questionId" hideLastBorder=true showImage=false hasTip=true :title="item.questionTitle"></yt-list-item>
				</block>
			</scroll-view>
		</yt-list>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import {helpCenter} from '@/api/api-mine.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem
		},
		data () {
			return {
				questionList: [],
				page: {
					current: 1,
					size: 10
				},
				hasMore: true
			}
		},
		onPullDownRefresh() {
			this.hasMore = true
			this.page.current = 1
			this.getQuestionList()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据'
				})
				return
			}
			this.page.current++
			this.getQuestionList()
		},
		onLoad() {
			this.getQuestionList()
		},
		methods: {
			async getQuestionList() {
				const res = await helpCenter.requestList(this.page)
				uni.stopPullDownRefresh()
				if (this.page.current=== 1) {
					if (!res.data.data.records.length) {
						this.$ytHint.toast({
							title: '暂未数据'
						})
						return
					}
					this.questionList = res.data.data.records
				} else {
					if (!res.data.data.records.length) {
						this.hasMore = false
						return
					} 
					this.questionList.push(res.data.data.records)
				}
			}
		},
	}
</script>

<style scoped>

	.title {
		height: 110upx;
		line-height: 110upx;
		font-size: 32upx;
		color: #666;
	}

</style>

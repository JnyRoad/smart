<template>
	<view class="mw bgec pr30 pl30">
		<view class="vcc mw pt40">
			<block v-for="(item, index) in list" :key="index">
				<view @click="toDetail(item)" class="pr30 pl30 mw h140 mb20 hcc rad10 bgf">
					<image class="w90 h90 mr30" :src="item.noteImage" mode=""></image>
					<view class="title f32pc bold f1">{{item.noteName}}</view>
					<image class="w30 h50 " src="/static/img/mine/next.png" mode=""></image>
				</view>
			</block>
		</view>
		<yt-null v-if="isNull" notice="暂时没有数据"></yt-null>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import information from '@/api/api-new-employee.js'
	import ytNull from '@/components/yt-null/yt-null.vue';
	import { storage } from '@/tools/storage.js'
	export default {
		components: {
			ytNull
		},
		data() {
			return {
				list: [],
				page: {
					current: 1,
					size: 10,
				},
				parkId: -1,
				hasMore: true,
				isNull: false
			}
		},
		onLoad() {
			this.parkId = storage.getSync(storage.PARK_ID)
			this.getInformationList()
		},
		onShow() {
			uni.removeStorage({
				key: 'UPARSE'
			})
		},
		onPullDownRefresh() {
			this.isNull = false
			this.hasMore = true
			this.page.current = 1
			this.getInformationList()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint({
					title: '没有更多数据了'
				})
				return
			}
			this.page.current++
			this.getInformationList()
		},
		methods: {
			// 获取新员工须知
			async getInformationList() {
				try{
					const res = await information.informationList(this.page, this.parkId)
					uni.stopPullDownRefresh()
					if (this.page.current === 1) {
						if (!res.data.data.records.length) {
							this.isNull = true
							return
						}
						this.list = res.data.data.records
						
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
							return
						}
						this.list.push(...res.data.data.records)
					}
				}catch(e){
					//TODO handle the exception
					console.log(e);
				}
			},
			toDetail(item) {
				if (item.contentLinkType == 1) {
					const link = encodeURIComponent(item.noteUrl)
					uni.navigateTo({
						url: `./detail1?link=${link}`
					})
				} else 
					if (item.contentLinkType == 2) {
					uni.navigateTo({
						url: `./detail2?noteId=${item.noteId}&noteName=${item.noteName}`
					})
				} else
					if (item.contentLinkType == 4) {
						uni.navigateTo({
							url: `./detail?noteId=${item.noteId}&noteName=${item.noteName}`
						})
					}
			}
		},
	}
</script>

<style>
</style>

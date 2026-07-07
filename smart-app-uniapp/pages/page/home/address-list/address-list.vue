<template>
	<view class="page mw">
		<scroll-view class="scrollList" scroll-y >
			<view class="uni-list bgf">
				<view v-if="lists.length>0" class="uni-list-cell" hover-class="uni-list-cell-hover" v-for="(item,index) in lists" :key="index">
					<view @click="changePark(item)" class="uni-list-cell-navigate">
						{{item.parkName}}
					</view>
				</view>
			</view>
		</scroll-view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import airportDate from '@/common/js/airport.js'
	import location  from '@/api/api-location.js' //定位园区接口 location.getLocation, location.getParkList
	import { storage } from '@/tools/storage.js';
	export default {
		data() {
			return {
				lists: 	[],
				page: {
					current: 1,
					size: 30
				},
				hasMore: true
			}
		},
		onLoad() {
			this.getList()
		},
		// 下拉刷新
		onPullDownRefresh() {
			this.hasMore = true
			this.page.current = 1
			this.getList()
		},
		// 上拉加载
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据了！'
				})
				return
			}
			this.page.current++
			this.getList()
		},
		methods: {
			async getList() {
				try{
					const res = await location.getParkList(this.page)
					uni.stopPullDownRefresh()
					if (this.page.current === 1) {
						if(!res.data.data.records.length) {
							this.$ytHint.toast({
								title:'暂未数据'
							})
							return
						}
						this.lists = res.data.data.records
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
							this.$ytHint.toast({
								title: '没有更多数据了！'
							})
							return
						}
						this.lists.push(...res.data.data.records)
					}
					
				}catch(e){
					console.log(e);
				}
			},
			changePark(obj) {
				this.$ytHint.modal({
					content: `确定选择${obj.parkName}`,
					confirm: () => {
						uni.setStorage({key:'addressChange',data:true})
						this.$store.commit('setAddress',obj.parkName)
						storage.removeSync(storage.PARK_NAME)
						storage.removeSync(storage.PARK_ID)
						storage.set(storage.PARK_NAME, obj.parkName)
						storage.set(storage.PARK_ID, obj.parkId)
						storage.set(storage.USER_LAT, obj.parkLatitude)
						storage.set(storage.USER_LONG, obj.parkLongitude)
						uni.navigateBack({
							url: `/pages/page/tabbar/home/home`
						})
					}
				})
			}
		},
		
	}
</script>

<style>
	.page {
		display: flex;
		flex-direction: row;
	}

	.scrollList {
		flex: 1;
		height: 100vh;
	}

	.uni-indexed-list-bar {
		width: 46upx;
		height: 100vh;
		background-color: lightgrey;
		display: flex;
		flex-direction: column;
	}

	.uni-indexed-list-bar.active {
		background-color: rgb(200, 200, 200);
	}

	.uni-indexed-list-text {
		color: #aaa;
		font-size: 22upx;
		text-align: center;
	}

	.uni-indexed-list-bar.active .uni-indexed-list-text {
		color: #333;
	}

	.uni-indexed-list-text.active,
	.uni-indexed-list-bar.active .uni-indexed-list-text.active {
		color: #007AFF;
	}

	.uni-indexed-list-alert {
		position: absolute;
		z-index: 20;
		width: 160upx;
		height: 160upx;
		left: 50%;
		top: 50%;
		margin-left: -80upx;
		margin-top: -80upx;
		border-radius: 80upx;
		text-align: center;
		line-height: 160upx;
		font-size: 70upx;
		color: #fff;
		background-color: rgba(0, 0, 0, 0.5);
	}
</style>

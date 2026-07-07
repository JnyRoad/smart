<template>
	<view class="mw">
		<view class="hbc head-tab bgf">
			<block v-for="(item, index) in headTabbar" :key="index">
				<view @click="changeTabIndex(index)" class="item hcc f32pc" :class="[curTabIndex === index ? 'active main-bg cf': '']">
					<view v-if="index == 1">{{item}}</view>
					<view v-if="index == 0">{{item}}(<text class="main-color f32" :class="[curTabIndex === index?'cf': '']">{{approveNum}}</text>)</view>
				</view>
			</block>
		</view>
		<waitApproveItem @click="toDetail" v-if="!isNull" class="abs top100 bottom100 left0 right0" :list="approveList"></waitApproveItem>
		<yt-null v-if="isNull" :notice="curTabIndex == 0?'暂时没有待您审批的':'暂时没有您审批的记录'"></yt-null>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import waitApproveItem from '@/components/wait-approve-item.vue';
	import ytNull from '@/components/yt-null/yt-null.vue';
	import approve from '@/api/api-approve.js'
	import {dateUtils} from '@/common/js/util.js'
	export default {
		components: {
			waitApproveItem,
			ytNull
		},
		data() {
			return {
				approveNum: 0,
				curTabIndex: 0, // 头部选项卡的下标
				headTabbar: ['待我审批的', '我已审批的'],
				approveList: [],
				page: {
					current: 1,
					size: 10
				},
				hasMore: true,
				isNull: false
			}
		},
		onLoad() {
			this.waitApprove()
		},
		onShow() {
			this.isNull = false
			const hasRefresh = uni.getStorageSync('hasRefresh')
			if (hasRefresh) {
				uni.removeStorageSync('hasRefresh')
				this.waitApprove()
			}
		},
		onPullDownRefresh() {
			this.isNull = false
			this.hasMore = true
			this.page.current = 1
			this.waitApprove()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据了'
				})
				return
			}
			this.page.current++
			this.waitApprove()
		},
		methods: {
			// 获取待我审批的内容
			async waitApprove() {
				const obj = {
					recordType: this.curTabIndex,
					current: this.page.current,
					size: this.page.size
				}
				try{
					const res = await approve.approveRecord(obj)
					if (!res) return
					uni.stopPullDownRefresh()
					if (this.curTabIndex == 0) {
						this.approveNum = res.data.data.total
					}
					if (this.page.current === 1) {
						if (!res.data.data.records.length) {
							this.isNull = true
							return
						}
						res.data.data.records.forEach(el => {
							el.timeDesc = dateUtils.format(el.createTime)
						})
						this.approveList = res.data.data.records
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
							return
						}
						res.data.data.records.forEach(el => {
							el.timeDesc = dateUtils.format(el.createTime)
						})
						this.approveList.push(...res.data.data.records)
					}
					if (this.curTabIndex == 0) {
						console.log(111);
						console.log(res.data.data.total);
						this.approveNum = res.data.data.total
					}
					console.log(this.approveNum);
					
				}catch(e){
					//TODO handle the exception
					console.log(e);
				}
			},
			// 区别跳转
			toDetail (e) {
				
				if (e.approveType == 2) {
					if (this.curTabIndex == 0) {
						uni.navigateTo({
							url: `../visitor-appointment/visitor-detail?type=1&visitState=2&visitId=${e.approveId}`
						})
					} else if (this.curTabIndex == 1) {
						console.log(e);
						let status = e.approveState == '1'?'0':'1'
						uni.navigateTo({
							url: `../visitor-appointment/visitor-detail?type=1&visitState=${status}&visitId=${e.approveId}`
						})
					}
					
				} else if (e.approveType == 1) {
					if (this.curTabIndex == 0) {
						uni.navigateTo({
							url: `../leave/leave-node?approveId=${e.approveId}&approveName=${e.approveName}&type=0`
						})
					} else if (this.curTabIndex == 1) {
						uni.navigateTo({
							url: `../leave/leave-node?approveId=${e.approveId}&approveName=${e.approveName}&type=1`
						})
					}
					
				}
			},
			changeTabIndex(inx) {
				if (this.curTabIndex == inx) return
				this.isNull = false
				this.page.current = 1
				this.approveList = []
				this.curTabIndex = inx
				this.waitApprove()
			}
		},
	}
</script>

<style lang="scss" scoped>
.head-tab {
	position: fixed;
	top: 0;
	left: 0;
	/* #ifdef H5 */
	top: 88upx;
	/* #endif */
	height: 100upx;
	width: 100%;
	padding: 0 30upx;
	z-index: 999;
	box-shadow: 0px 2px 2px 0px rgba(185, 185, 185, 0.48);
	.item {
		width: 40%;
		height: 80upx;
		&.active {
			border-radius: 50upx;
		}
	}
}
</style>

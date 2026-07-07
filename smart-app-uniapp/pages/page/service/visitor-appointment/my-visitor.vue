<template>
	<view class="mw">
		<view class="vc mw rel">
			<view class="tab mw hbc bgf pr30 pl30 rel">
				<block v-for="(item, index) in topCate" :key="index">
					<view @click="tapTab(index)" :class="['item hcc f32', curTopIndex === index ? 'main-bg cf' : 'f32pc4']">
						<view v-if="index == 0">{{item}}</view>
						<view v-if="index == 1">{{item}}(<text class="main-color f32" :class="[curTopIndex === index?'cf': '']">{{waitAproveNum}}</text>)</view>
					</view>
				</block>
			</view>
			<view v-if="curTopIndex === 0" class="sroll-view mw">
				<yt-appointment-item v-if="appoinList.length>0" @click="toDetail" :appoinData="appoinList" />
			</view>
			<view v-if="curTopIndex === 1" class="sroll-view mw">
				<yt-appointment-item v-if="appoinList.length>0" @click="toDetail" :appoinData="appoinList" />
			</view>
			<yt-null v-if="isNull" :notice="curTopIndex == 0?'您还没有预约记录':'暂时没有需要您审批的'"></yt-null>
		</view>
		<navigator v-if="curTopIndex === 0" url="./add-visitor" class="w110 add-visiter h110 round fixed right30 bottom60 main-bg overflow">
			<text class="add cf hcc iconfont icon-jia">&#xe604;</text>
		</navigator>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytappointmentitem from '@/components/yt-appointment-item.vue';
import ytNull from '@/components/yt-null/yt-null.vue';
import visitor from '@/api/api-visitor.js';
import { visitState } from '@/config/dictionaries.js';
export default {
	components: {
		'yt-appointment-item': ytappointmentitem,
		'yt-null': ytNull
	},
	data() {
		return {
			topCate: ['我发起的预约', '待我审批的'],
			curTopIndex: 0,
			waitAproveNum: 0,
			visitListType: 1,
			appoinList: [],
			hasMore: true, // 是否还有数据
			approve: [],
			page: {
				current: 1,
				size: 10
			},
			isNull: false
		};
	},
	onLoad() {
		this.getVisitList()
		this.getApprovalNum()
	},
	onShow() {
		this.isNull = false
		// 处理数据后 读取本地存储的数据 判断页面是否要刷新
		const hasRefresh = uni.getStorageSync('hasRefresh')
		if (hasRefresh) {
			uni.removeStorageSync('hasRefresh')
			this.getVisitList()
			this.getApprovalNum()
		}
	},
	onPullDownRefresh() {
		this.isNull = false
		this.appoinList = []
		this.hasMore= true
		this.page.current = 1
		this.getVisitList()
		this.getApprovalNum()
	},
	onReachBottom() {
		if (!this.hasMore) {
			this.$ytHint.toast({
				title: '没有更多数据了'
			})
			return
		}
		this.page.current++
		this.getVisitList()
		this.getApprovalNum()
	},
	methods: {
		async getVisitList() {
			try{
				const res = await visitor.visitList(this.page, this.visitListType);
				uni.stopPullDownRefresh()
				res.data.data.records.forEach(el => {
					el.visitStatus = visitState[`${el.visitState}`]; // 根据状态值提取对应的状态
				});
				if (this.page.current === 1) {
					if (!res.data.data.records.length) {
						this.isNull = true
						return
					}
					this.appoinList = res.data.data.records
					return
				} else {
					if (!res.data.data.records.length) {
						this.hasMore = false
						return
					}
					this.appoinList.push(...res.data.data.records )
					
				}
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 获取未审批的数量
		async getApprovalNum() {
			try{
				const res = await visitor.approvalNum()
				this.waitAproveNum = res.data.data.toApprovalCount
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		toDetail(e) {
			// type 表示审批 还是我发起的
			uni.navigateTo({
				url: `./visitor-detail?type=${this.curTopIndex}&visitId=${e.visitId}&visitState=${e.visitState}`
			});
		},
		tapTab(index) {
			this.isNull = false
			if (this.curTopIndex === index) return
			this.appoinList = []
			this.hasMore = true
			this.page.current = 1
			this.curTopIndex = index;
			this.visitListType = this.curTopIndex + 1;
			this.getVisitList()
		}
	}
};
</script>

<style lang="scss" scoped>
.tab {
	position: fixed;
	left: 0;
	height: 100upx;
	top: 88upx;
	/* #ifdef APP-PLUS || MP-WEIXIN */
	top: 0;
	/* #endif */
	z-index: 4;
	box-shadow: 0px 5upx 5upx 0px rgba(185, 185, 185, 0.48);
	.item {
		width: 300upx;
		height: 80upx;
		border-radius: 40upx;
	}
}
.sroll-view {
	padding-top: 100upx;
	box-sizing: border-box;
}

.add-visiter {
	box-shadow: 5upx 0 5upx #ccc,
				0 5upx 5upx #ccc;
}

.add {
	font-size: 60upx;
	margin: 0;
}
</style>

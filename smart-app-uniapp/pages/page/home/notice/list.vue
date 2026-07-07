<template>
	<view class="mw pl30 pr30 pt30">
		<block v-for="(item, index) in list" :key="index">
			<view @click="toDetail(item)" class="hlc item mw pr30 pl30 pt20 pb20 bgf mb20 rad10">
				<view class="img mr30">
					<image class="mw mh" :src="item.bbsImg" mode=""></image>
				</view>
				<view class="f1 mh vlb">
					<view class="text3 f30pc">{{ item.bbsTitle }}</view>
					<view class="hrc mw f28pc4">{{item.dateDesc}}</view>
				</view>
			</view>
		</block>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import indexInfo from '@/api/api-index-info.js';
import { storage } from '@/tools/storage.js';
import { dateUtils } from '@/common/js/util.js'
export default {
	data() {
		return {
			list: [],
			page: {
				current: 1,
				size: 10
			},
			parkId: '',
			hasMore: true
		};
	},
	onLoad() {
		
		this.parkId = storage.getSync(storage.PARK_ID);
		this.getNoticeList();
	},
	onPullDownRefresh() {
		this.page.current = 1;
		this.hasMore = true;
		this.getNoticeList();
	},
	onReachBottom() {
		if (!this.hasMore) {
			this.$ytHint.toast({
				title: '没有更多数据了！'
			});
			return;
		}
		this.page.current++;
		this.getNoticeList();
	},
	methods: {
		async getNoticeList() {
			try {
				const res = await indexInfo.bbsList(this.page, this.parkId);
				uni.stopPullDownRefresh();
				if (this.page.current === 1) {
					if(!res.data.data.records.length) {
						this.$ytHint.toast({
							title:'暂未数据'
						})
						return
					}
					res.data.data.records.forEach(el => {
						if (el.date) {
							el.dateDesc = dateUtils.format(el.date)
						}
					})
					this.list = res.data.data.records;
				} else {
					if (!res.data.data.records) {
						this.hasMore = false;
						this.$ytHint.toast({
							title: '没有更多数据了！'
						});
						return;
					}
					res.data.data.records.forEach(el => {
						if (el.date) {
							el.dateDesc = dateUtils.format(el.date)
						}
					})
					this.list.push(...res.data.data.records);
				}
			} catch (e) {
				if (e.errMsg) {
					if (this.timeOut === 2) {
						this.$ytHint.toast({
							title: '网络异常，请检查您的网络！'
						});
						return;
					}
					this.getNoticeList();
				}
			}
		},
		toDetail(item) {
			if (item.contentLinkType == 1) {
				const link = encodeURIComponent(item.bbsUrl)
				uni.navigateTo({
					url: `./detail?link=${link}`
				})
			} else if (item.contentLinkType == 2) {
				uni.navigateTo({
					url: `./detail?bbsId=${item.bbsId}`
				})
			}
			else if (item.contentLinkType == 4) {
				uni.navigateTo({
					url: `./detail1?bbsId=${item.bbsId}`
				})
			}
		}
	}
};
</script>

<style scoped>
	.img {
		width: 240upx;
		height: 100%;
	}
	.item {
		height: 272upx;
		box-shadow: 0px 5upx 10upx 0px rgb(185, 185, 185, 0.9);
	}
</style>

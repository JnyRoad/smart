<template>
	<view class="mw">
		<view class="vlc mw bgf pr30 pl30 pt20">
			<view class="f30pc1 bold mb20 hlc">
				<image class="w50 h48 mr20" src="/static/img/serive/recruit/Invitation/keep.png" alt=""></image>
				应聘记录
			</view>
			<view v-if="records.length === 0" class="f32pc">暂无应聘记录</view>
			<view v-if="records.length > 0" class="">
				<block v-for="(item, index) in records" :key="index">
					<view class="mb20 item">
						<view class="hbc mw mb20">
							<view class="f32pc mr100">
								{{item.optName}}
							</view>
							<view class="f30pc4">
								{{item.optDate}}
							</view>
						</view>
						<view class="hlc">
							<view class="f32pc2 mr50">
								<text class="f32pc2 mr20">操作人:</text> {{item.optUser}}
							</view>
							<view class="">
								{{item.optDesc}}
							</view>
						</view>
					</view>
				</block>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import recruit from '@/api/api-recruitment-management.js'
export default {
	data() {
		return {
			applicationId: '',
			records: []
		};
	},
	onLoad(e) {
		this.getRecord(e)
	},
	methods: {
		async getRecord(obj) {
			recruit.applicationRecord(obj).then(res=>{
				this.records = res.data.data
			}) 
		}
	},
};
</script>

<style scoped>
	.item {
		width: 620upx;
		margin-left: 70upx;
	}
</style>

<template>
	<view class="pl30 pr30">
		<block v-for="(item, index) in list" :key="index">
			<view @click="onClick(item.visitId,item.visitState)" class="item rad10 mb20 mt20 pl20 pt20 pr20 pb20 bgf hbc rel">
				<view class="w200 h200 mr20 bge"><image lazy-load :src="item.visitorPhoto" mode=""></image></view>
				<view class="vlb f1 rel">
					<view class="hbc mw">
						<text class="f30pc">预约人:</text>
						<text class="f30pc4">{{ item.visitorName }}</text>
					</view>
					<view class="hbc mw">
						<text class="f30pc mr20">开始时间</text>
						<text class="f28pc4">{{ item.startDate }}</text>
					</view>
					<view class="hbc mw">
						<text class="f30pc mr20">结束时间</text>
						<text class="f28pc4">{{ item.endDate }}</text>
					</view>
					<view class="hbc mw">
						<text class="f30pc">预约事由</text>
						<text class="f30pc4">{{ item.visitReason }}</text>
					</view>
					<view class="hbc mw" v-if="item.processName">
						<text class="f30pc">当前审批节点</text>
						<text class="f30pc4">{{ item.processName }}</text>
					</view>
					<view class="status-icon">
						<image v-if="item.visitState == 0" src="/static/img/serive/pass.png" mode=""></image>
						<image v-if="item.visitState == 1" src="/static/img/serive/reject.png" mode=""></image>
						<image v-if="item.visitState == 2" src="/static/img/serive/wait-pass.png" mode=""></image>
						<image v-if="item.visitState == 3" src="/static/img/serive/arrive.png" mode=""></image>
						<image v-if="item.visitState == 4" src="/static/img/serive/pass-not.png" mode=""></image>
						<image v-if="item.visitState == 5" src="/static/yilikai.png" mode=""></image>
					</view>
				</view>
			</view>
		</block>
	</view>
</template>

<script>
// 访客预约组件模块
export default {
	name: 'yt-appointment-item',
	props: {
		// 数据
		appoinData: {
			type: Array
		},
		// 分 我发起的  待我审批的
		visitType: {
			type: [Number, String],
			default: 0
		}
	},
	computed: {
		list() {
			return this.appoinData; 
		}
	},
	methods: {
		onClick(visitId,visitState) {
			this.$emit('click', {
				visitId,
				visitState
			});
		}
	}
};
</script>

<style scoped>
.item {
	box-shadow: 0px 5upx 49upx 0px rgba(185, 185, 185, 0.48);
}
.status-icon {
	position: absolute;
	right: -15upx;
	top: -15upx;
	width: 125upx;
	height: 125upx;
}
</style>

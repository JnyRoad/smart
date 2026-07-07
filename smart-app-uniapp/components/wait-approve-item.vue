<template>
	<view class="pt20 pr30 pl30">
		<scroll-view scroll-y class="list">
			<block v-for="(item, index) in approveList" :key="index">
				<view @click="toDetail(item)" class="item vlc mb20">
					<view class="hbc mw">
						<text class="f30pc">{{ item.approveName }}</text>
						<text class="f28pc2">{{ item.timeDesc}}</text>
					</view>
					<block v-for="(item1, index1) in item.approveItem" :key="index1">
						<view class="hlc mw">
							<view class="f32pc mr20">{{ item1.itemName }}:</view>
							<text class="f30pc2">{{ item1.itemValue }}</text>
						</view>
					</block>
					<text class="main-color f32">{{ item.approveDesc }}</text>
				</view>
			</block>
		</scroll-view>
	</view>
</template>

<script>
// 待我审批的组件模块
export default {
	
	props: {
		// 数据列表
		list: {
			type: Array
		}
	},
	computed: {
		approveList () {
			return this.list
		}
	},
	methods: {
		toDetail(e) {
			this.$emit('click', e)
		}
	},
};
</script>

<style lang="scss" scoped>
	
	.item {
		background-color: #fff;
		width: 690upx;
		padding: 30upx;
		box-shadow: 0px 5upx 15upx 0px #ccc;
		border-radius: 20upx;
	}
</style>

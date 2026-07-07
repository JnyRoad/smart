<template>
	<view  class="pl30 pr30 pt30 vcc">
		<block  v-for="(item,index) in car" :key="index">
			<view @click="onClick(item.plateNumber)" class="deliver-item mw v mb30 bgf rad10">
				<view class="hbc mb10">
					<text class="f32pc">{{item.plateNumber}}</text>
					<text class="f28pc2">{{item.vehicleTypeDesc}}</text>
				</view>
				<view class="hbc mb10">
					<text class="f32pc">{{item.vehicleBrand}}</text>
					<text class="f28pc2">{{item.vehicleColorDesc}}</text>
				</view>
				<view class="hbc">
					<text @click.stop="remove(item.plateNumber)" class="main-color">移除</text>
					<text></text>
					<!-- <text v-if="item.statusType !== 2" class="f32pc">{{item.status}}</text> -->
					<text class="underline main-color f32pc">园区权限</text>
				</view>
			</view>
		</block>
	</view>
</template>

<script>
	// 我的车辆组件模块
	export default {
		name: 'yt-deliver-item',
		props: {
			car: {
				type: Array,
				require: true
			}
		},
		methods: {
			remove(plateNumber) {
				this.$emit('remove', plateNumber)
			},
			onClick (plateNumber) {
				this.$emit('click',plateNumber)
			} 
		}
	}
</script>

<style lang="scss" scoped>
	.deliver-item {
		padding: 30upx;
		box-shadow:0px 5upx 49upx 0px rgba(185,185,185,0.48);
	}
</style>

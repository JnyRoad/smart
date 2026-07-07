<template>
	<view v-if="carInfo.plateNumber" :class="['yt-page','bgec']">
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item showImage=false showArrow=false title="车牌号" :tipContent="carInfo.plateNumber"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="车类型" :tipContent="carInfo.vehicleTypeDesc"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="车品牌" :tipContent="carInfo.vehicleBrand"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="车颜色" :tipContent="carInfo.vehicleColorDesc"></yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pl30','pr30','mt20']">
			<yt-list-item @click="preview(carInfo.drivingLicence)"  showImage=false showArrow=false title="驾驶证" hasRightImage=true hasTip=false :rightImage="carInfo.drivingLicence" ></yt-list-item>
			<yt-list-item @click="preview(carInfo.carDrivingLicence)" showImage=false showArrow=false title="行驶证" hasRightImage=true hasTip=false :rightImage="carInfo.carDrivingLicence"></yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pl30','pr30','mt20']">
			<yt-list-item  showImage=false showArrow=false title="园区权限" :tipContent="parkName"></yt-list-item>
		</yt-list>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import ytpicker from '@/components/yt-picker.vue';
	import {vehicle} from '@/api/api-mine.js' // 请求接口的参数
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-button': ytbutton,
			'yt-picker': ytpicker
		},
		data () {
			return {
				parkName: '',
				carInfo: {}
			}
		},
		onLoad (e) {
			const { vehicleAuthkId } = e
			this.parkName = e.parkName
			this.myCar({vehicleAuthkId})
		},
		methods: {
			async myCar (obj) {
				console.log(obj);
				const res = await vehicle.authDetail(obj)
				console.log(res);
				this.carInfo = res.data.data
			},
			preview(img) {
				uni.previewImage({
					current: '0',
					urls: [img]
				})
			}
		},
	}
</script>

<style lang="scss" scoped>
	.mask {
		background-color: $uni-bg-color-mask;
	}
</style>

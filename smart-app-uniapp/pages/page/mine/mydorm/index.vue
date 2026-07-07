<template>
	<view class="mw">
		<yt-list :ytClass="['pl30','pr30','mb20']">
			<yt-list-item showArrow=false title="我的宿舍" image="/static/img/mine/dorm.png">
				<view class="f28pc2">
					{{myDormInfo.applyStateDes}}
				</view>
			</yt-list-item>
		</yt-list>
		<yt-list v-if="myDormInfo.buildingName" :ytClass="['pl30','pr30','mb20']">
			<yt-list-item showArrow=false showImage=false 
			:title="myDormInfo.buildingName+'栋-'+myDormInfo.floor+'楼-'+myDormInfo.room+'室'" :tipContent="myDormInfo.bedName"></yt-list-item>
			<yt-list-item showArrow=false showImage=false :title="myDormInfo.maxBed+'人间'" :tipContent="'已入住: '+myDormInfo.occupancy+'人'"></yt-list-item>
			<yt-list-item showArrow=false showImage=false title="宿舍类型" tipContent="内宿"></yt-list-item>
		</yt-list>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue';
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import {employee} from '@/api/api-mine.js' // 请求接口的参数
	import {bedType} from '@/config/dictionaries.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem
		},
		data () {
			return {
				myDormInfo: {}
			}
		},
		onLoad() {
			this.getMyDormInfo()
		},
		methods: {
			async getMyDormInfo () {
				try{
					const res = await employee.roomDetail()
					if (!res) return
					this.myDormInfo = res.data.data
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			toDrom() {
				uni.navigateTo({
					url: './select-dorm-type'
				})
			}
		},
	}
</script>

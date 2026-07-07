<template>
	<view class="yt-page">
		<view class="pl30 pr30 pt30 pb100 vcc">
			<block v-for="(item, index) in authList" :key="index">
				<view @click="toAuthDetail(item)" class="deliver-item mw v mb30 bgf rad10 pt30 pb30 pr30 pl30">
					<view class="hbc mb10">
						<text :class="['f32pc',item.authDesc === '已通过'?'suc-color': 'err-color']">{{item.parkName}}</text>
						<text class="f28pc2">{{item.authDesc}}</text>
					</view>
					<view class="hbc">
						<text></text>
						<text v-if="item.authDesc === '已通过'" @click.stop="remove(item.plateNumber)" class="main-color">移除</text>
					</view>
				</view>
			</block>
		</view>
		<view class="mw vcc mt100 rel">
			<yt-button btnText="申请园区权限" btnWidth="180" btnHeight="54"></yt-button>
			<yt-picker v-if="parkList.length>0" class="abs picker bottom0" @changePicker="getPark" idType="parkAuth" :arrData="parkList"></yt-picker>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytpicker from '@/components/yt-picker.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import {vehicle} from '@/api/api-mine.js' // 请求接口的参数
	import location  from '@/api/api-location.js'
	import { storage } from '@/tools/storage.js';
	export default {
		components: {
			'yt-button': ytbutton,
			'yt-picker': ytpicker
		},
		data () {
			return {
				showPick: false,
				authList: [],
				parkListData: [],
				parkList: [],
				parkId: '',
				page: {
					current: 1,
					size: 20
				}
			}
		},
		onNavigationBarButtonTap() {
			uni.navigateTo({
				url: './addcar'
			})
		},
		onLoad(e) {
			this.plateNumber = e.plateNumber
			const {plateNumber} = e
			this.getAuthList({plateNumber})
			this.getList()
			
		},
		methods: {
			async getAuthList (obj) {
				const res = await vehicle.authParkList(obj)
				this.authList = res.data.data
			},
			async getList() {
				const res = await location.getParkList(this.page)
				this.parkListData = res.data.data.records
				this.parkList = res.data.data.records.map(el=>{
					return el.parkName
				})
			},
			toAuthDetail(obj) {
				uni.navigateTo({
					url: `./detail?vehicleAuthkId=${obj.vehicleAuthkId}&parkName=${obj.parkName}`
				})
			},
			remove (carNum) {
				const obj = {
					plateNumber: carNum
				}
				this.$ytHint.modal({
					content: '确定要移出该车辆吗?',
					confirm: ()=>{
						vehicle.deleteCar(obj).then(res => {
							this.$ytHint.toast({
								title: '移出成功!'
							})
						})
					}
				})
				
			},
			// 获取选择值
			getPark( e ) {
				if (e.idType === 'parkAuth') {
					this.$ytHint.modal({
						content: `确定选择申请${e.value}通行权限吗`,
						confirm: () => {
							let job = storage.getSync('jobLevelflag')
							if (job == 'B2' || job == 'B1' || job == 'A') {
								this.$ytHint.toast({
									title: 'C级以下（含C级）不能申请入园!'
								})
								return
							}
							for (let el of this.parkListData) {
								if (el.parkName === e.value) {
									this.parkId = el.parkId
									break
								}
							}
							const obj = {
								plateNumber: this.plateNumber,
								parkId: this.parkId
							}
							vehicle.authApply(obj).then(res => {
								if (!res.data.data) {
									this.$ytHint.toast({
										title: res.data.msg
										
									})
								} else {
									this.$ytHint.toast({
										title: '提交成功，备案纸质资料请线下交到园区车管处'
										
									})
								}
								this.getAuthList({plateNumber: this.plateNumber})
							})
						}
					})
				}
			},
		},
	}
</script>

<style scoped>
.picker {
	width: 360upx;
	height: 108upx;
	opacity: 0;
}
</style>

<template>
	<view class="yt-page bgf">
		<yt-success :icon="icon" @click="toCancel" btnText="返回" :successText="text">
			<view class="sucess-info" v-if="dromStatus">
				<view class="item pt30 pr30 pb30 pl30 vlc border-bottom-D">
					<text class="f32pc4">您的宿舍信息为:</text>
					<text v-if="myDormInfo.buildingName" class="f32pc main-color bold">{{myDormInfo.buildingName}}-{{myDormInfo.floor}}-{{myDormInfo.room}}房间 {{myDormInfo.bedName}}</text>
				</view>
				<view class="item pt30 pr30 pb30 pl30 vlc">
					<text class="f32pc">请到宿管处领取房间钥匙，出示分配的宿舍信息</text>
				</view>
			</view>
		</yt-success>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import success from '@/components/Success.vue'
	import { employee } from '@/api/api-mine.js' // 请求接口的参数
	import { bedType } from '@/config/dictionaries.js'
	export default {
		components: {
			'yt-success': success	
		},
		data () {
			return {
				myDormInfo: {},
				bedTypeId: '', // 床位类型id
				bed: '' ,// 床位类型
				dromStatus: true,
				text : '宿舍自动分配成功',
				icon : '/static/img/login/set.png'
			}
		},
		onLoad(e) {
			this.bedTypeId = e.bedTypeId
			this.getMyDorm()
			this.bed = bedType[this.bedTypeId]
		},
		methods: {
			async getMyDorm () {
				const obj = {
					bedType: this.bedTypeId
				}
				try{
					const res = await employee.inRoomApply(obj)
					if (!res) {
						this.dromStatus = false
						this.text = '宿舍分配失败'
						this.icon = '/static/cha-tianchong.png'
						return
					}
					this.myDormInfo = res.data.data
					if (res.data.data == false) {
						this.dromStatus = false
						this.$ytHint.toast({
							title: res.data.msg
						})
						this.text = '宿舍分配失败'
						this.icon = '/static/cha-tianchong.png'
						return
					}
					this.$store.commit('WatchDorm',{
						dormStatus: '1',
						dormDesc: '已分配'
					})
				}catch(e){
					console.log(e);
				}
			},
			toCancel() {
				uni.switchTab({
					url: '../../tabbar/mine/mine'
				})
				// if (this.dromStatus) {
				// 	uni.redirectTo({
				// 		url: './index'
				// 	});
				// } else {
				// 	uni.switchTab({
				// 		url: '../../tabbar/mine/mine'
				// 	})
				// }
			}
		},
	}
</script>

<style lang="scss" scoped>
	.yt-page {
		.sucess-info {
			width: 690upx;
			box-shadow:0px 5upx 49upx 0px rgba(185,185,185,0.48);
			border-radius: 10upx;
		}
	}
</style>

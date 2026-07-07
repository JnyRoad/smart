<template>
	<view class="mw bgf">
		<view class="mw pl30 bgf pr30 hbc h90 border-bottom-D">
			<text class="f32pc2">考勤日期</text>
			<text class="f30pc2">{{curDate}}</text>
		</view>
		<view class=" pt30 pr30 pl30">
			<view  class="vlc">
				<view class="hlc mb20">
					<!-- <text class="icon mr10"
					 :style="list.secondEnter ? 'color:#508BFF;font-size:60upx' : 'color:#999999;font-size:60upx'"
					v-if="!checkingInDetail.secondEnter" @click="select('secondEnter')">&#xe673;</text> -->
					<view style="height: 50upx;width: 50upx;border-radius: 50%;border: 1px solid #E0E0E0;overflow: hidden;"
					class="mr20 hcc" v-if="!checkingInDetail.secondEnter" @click="select('secondEnter')">
						<image src="/static/gou.png" class="mw mh" v-if="list.secondEnter"></image>
					</view>
					<text style="width: 68upx;height: 10upx;" v-else></text>
					<text class="mr30 f32pc">二入</text>
					<view class="f32pc mr20">打卡时间 <text class="fs32pc2" v-if="checkingInDetail.secondEnter">({{checkingInDetail.secondEnter}})</text> 
					</view>
					<view class="hcc" v-if="!checkingInDetail.secondEnter" style="background-color: #FF453E;color: #FFFFFF;width: 80upx;height: 30upx;border-radius: 20upx;">
						<text>缺卡</text>
					</view>
				</view>
				<view class="hlc mb20">
				<!-- 	<text class="icon tc9 mr10" :style="list.secondOut ? 'color:#508BFF;font-size:60upx' : 'color:#999999;font-size:60upx'"
					 v-if="!checkingInDetail.secondOut" @click="select('secondOut')">&#xe673;</text> -->
					 <view style="height: 50upx;width: 50upx;border-radius: 50%;border: 1px solid #E0E0E0;overflow: hidden;"
					 class="mr20 hcc" v-if="!checkingInDetail.secondOut" @click="select('secondOut')">
					 	<image src="/static/gou.png" class="mw mh" v-if="list.secondOut"></image>
					 </view>
					<text style="width: 68upx;height: 10upx;" v-else></text>
					<view class="mr30 f32pc">二出</view>
					<view class="f32pc mr20">打卡时间  <text class="fs32pc2" v-if="checkingInDetail.secondOut">({{checkingInDetail.secondOut}})</text></view>
					<view class="hcc" v-if="!checkingInDetail.secondOut" style="background-color: #FF453E;color: #FFFFFF;width: 80upx;height: 30upx;border-radius: 20upx;">
						<text>缺卡</text>
					</view>
				</view>
				<view class="hlc mb20">
					 <view style="height: 50upx;width: 50upx;border-radius: 50%;border: 1px solid #E0E0E0;overflow: hidden;"
					 class="mr20 hcc" v-if="!checkingInDetail.fourthEnter" @click="select('fourthEnter')">
					 	<image src="/static/gou.png" class="mw mh" v-if="list.fourthEnter"></image>
					 </view>
					<text style="width: 68upx;height: 10upx;" v-else></text>
					<text class="mr30 f32pc">四入</text>
					<view class="f32pc mr20">打卡时间  <text class="fs32pc2" v-if="checkingInDetail.fourthEnter">({{checkingInDetail.fourthEnter}})</text></view>
					<view class="hcc" v-if="!checkingInDetail.fourthEnter" style="background-color: #FF453E;color: #FFFFFF;width: 80upx;height: 30upx;border-radius: 20upx;">
						<text>缺卡</text>
					</view>
				</view>
				<view class="hlc mb20">
					 <view style="height: 50upx;width: 50upx;border-radius: 50%;border: 1px solid #E0E0E0;overflow: hidden;"
					 class="mr20 hcc" v-if="!checkingInDetail.fourthOut" @click="select('fourthOut')">
						<image src="/static/gou.png" class="mw mh" v-if="list.fourthOut"></image>
					 </view>
					<text style="width: 68upx;height: 10upx;" v-else></text>
					<text class="mr30 f32pc">四出</text>
					<view class="f32pc mr20">打卡时间  <text class="fs32pc2" v-if="checkingInDetail.fourthOut">({{checkingInDetail.fourthOut}})</text></view>
					<view class="hcc" v-if="!checkingInDetail.fourthOut" style="background-color: #FF453E;color: #FFFFFF;width: 80upx;height: 30upx;border-radius: 20upx;">
						<text>缺卡</text>
					</view>
				</view>
				<view class="hlc mb20">
					<text style="width: 68upx;height: 10upx;"></text>
					<text class="mr30 f32pc">五入</text>
					<view class="f32pc">打卡时间 <text class="fs32pc2" v-if="checkingInDetail.fifthEnter">({{checkingInDetail.fifthEnter}})</text></view>
				</view>
				<view class="hlc mb20">
					<text style="width: 68upx;height: 10upx;"></text>
					<text class="mr30 f32pc">五出</text>
					<view class="f32pc">打卡时间<text class="fs32pc2" v-if="checkingInDetail.fifthOut">({{checkingInDetail.fifthOut}})</text></view>
				</view>
			</view>
			<!-- <view class="hcc mt40 mb40">
				<yt-button @click="applyClockin" btnText="去补卡" btnWidth="180" btnHeight="54"></yt-button>
			</view> -->
		</view>
		<view class="mw" style="height: 100upx;">
			
		</view>
		<view style="padding: 10upx;position: fixed;bottom: 110upx;color:rgb(80, 139, 255);"
		v-if="checkingInDetail.fourthOut == '' || checkingInDetail.fourthEnter == '' || checkingInDetail.secondOut == '' || checkingInDetail.secondEnter == ''">
			提示：5入5出为加班卡，不能进行系统补卡，请与部门文员确认后线下补卡
		</view>
		<view class="submit-buka hbc mw pl30" 
		v-if="checkingInDetail.fourthOut == '' || checkingInDetail.fourthEnter == '' || checkingInDetail.secondOut == '' || checkingInDetail.secondEnter == ''">
			<view class="hlc">
				<view style="height: 50upx;width: 50upx;border-radius: 50%;border: 1px solid #E0E0E0;overflow: hidden;"
				class="mr20 hcc" @click="allSelects">
					<image src="/static/gou.png" class="mw mh" v-if="allSelect"></image>
				</view>
				<!-- <text class="icon tc9 mr10" :style="allSelect ? 'color:#508BFF;font-size:60upx' : 'color:#999999;font-size:60upx'" @click="allSelects">&#xe673;</text> -->
				<text>全选</text>
			</view>
			<view class="mh hcc" style="width: 200upx;background-color: #508BFF;color: #FFFFFF;" @click="applyClockin">
				<text>去补卡</text>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import attendance from '@/api/api-attendance.js'
	import ytbutton from '@/components/yt-button/index.vue'
	export default {
		components: {
			'yt-button': ytbutton
		},
		data () {
			return {
				curDate: '',
				checkingInDetail: {},
				patchData: {},
				hasApply: false,
				list : {
					'secondOut' : '',
					'secondEnter' : '',
					'fourthOut' : '',
					'fourthEnter' : ''
					
				},
				allSelect : false
			}
		},
		onLoad(e) {
			this.curDate = e.date
			this.getErrorDetail(e.date)
			// this.getPatchQuery(e.date)
		},
		methods: {
			//全选
			allSelects () {
				let that = this
				that.allSelect = !that.allSelect
				if (that.allSelect) {
					for (let var1 in that.list) {
						that.list[var1] = true
					}
				} else {
					for (let var1 in that.list) {
						that.list[var1] = false
					}
				}
				
			},
			// 获取异常常的考勤信息
			async getErrorDetail (date) {
				const obj = {
					queryDay: date
				}
				const res = await attendance.errorDetail(obj)
				if (!res) return
				this.checkingInDetail = res.data.data
				let that = this
				let b = true
				for (let key in that.checkingInDetail) {
					if (key == 'secondOut' || key == 'secondEnter'|| key == 'fourthOut'|| key == 'fourthEnter'){
						if (that.checkingInDetail[key] == '') {
							that.list[key] = true
						} else {
							that.list[key] = false
							b = false
						}
					}
				}
				this.allSelect = b
				
			},
			// 如果当前日期缺考勤 申请补卡
			applyClockin() {
				let that = this
				let b = true
				for (let var1 in that.list) {
					if (that.list[var1]) {
						b = false
					}
				}
				if (b) {
					this.$ytHint.toast({
						title: '请选择需要补卡的考勤点'
					})
					return
				}
				uni.setStorageSync('applyClockin',this.list)
				uni.navigateTo({
					url: `./apply-clockin?patchDate=${this.curDate}`
				})
			},
			select (e) {
				let that = this
				this.list[e] = !this.list[e]
				let a = true
				for (let var1 in that.list) {
					if (!that.list[var1]) {
						a = false
					}
				}
				this.allSelect = a
			},
			// 获取补卡信息
			async getPatchQuery (date) {
				const obj = {
					patchDate: date
				}
				try{
					const res = await attendance.patchQuery(obj)
					if (!res) return
					let status = true // 定义一个状态是否进行过补卡申请
					for (let key in res.data.data) {
						if (key == 'fourthEnter' || key == 'fourthOut' || key == 'secondEnter' || key == 'secondOut') {
							if (!res.data.data[key]) {
								status = false
							}
						}
					}
					if (!status) {
						this.hasApply = false
						return
					}
					this.patchData = res.data.data
					Object.keys(this.checkingInDetail).forEach(el => {
						Object.keys(this.patchData).forEach(el1 => {
							if (el == el1) {
								this.checkingInDetail[el] =  this.patchData[el1]
							}
						})
					})
					this.hasApply = true
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
		},
	}
</script>

<style lang="scss">
	.submit-buka {
		position: fixed;
		left: 0;
		bottom: 0;
		height: 100upx;
		border-top: 1px solid #E0E0E0;
	}
	.black {
		color: black;
		font-size: 60upx;
	}
	.none {
		color: #CCCCCC;
		font-size: 60upx;
	}
</style>

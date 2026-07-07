<template>
	<view class="mw bgec">
		<custom-navigator titleText="消费记录" :showRightButton="true">
			<image @click.stop="showNoticeModel(1)" class="w30 h30" src="/static/img/serive/shuoming.png" mode=""></image>
			<view @click.stop="exchangeType" class="f1 hbc ml10">
				<text class="cf f28  mr10">{{curType==1?'公司账户':'个人账户'}}</text>
				<image class="w25 h25" src="/static/img/serive/icon_change.png" mode=""></image>
			</view>
		</custom-navigator>
		<view class="hbc mw h120 pl30 pr30 main-bg ">
			<view class="hlc">
				<yt-picker  @changePicker="getPickerData" idType="date" textColor="#ffffff" :value="curDate" :showValue="false" :arrData="dateList">
					<text class="f32 cf">{{curDate}}</text>
				</yt-picker>
				<view class="ml10 w30 h20 hcc">
					<image src="/static/img/serive/down-arrow.png" mode=""></image>
				</view>
			</view>
			<text class="f40 cf">￥{{totalAmount ? totalAmount : 0}}</text>
		</view>
		<block v-for="(item, index) in list" :key = 'index'>
			<view class="mw">
				<view class="mw vcc">
					<view class="f32pc2 mw border-bottom-D pl30">{{item.data}}</view>
					<block v-for="(item, index) in item.listItem" :key = 'index'>
						<view class="mw bgf">
							<view class="hbc mw pl30 pr30 pt20 pb20 h120 hlc border-bottom-D">
								<view class="w40 h40 mr30">
									<image class="mw mh" src="/static/img/serive/renminbi.png"></image>
								</view>
								<view class="f1 vlc">
									<text class="f32pc2 bold">刷卡消费</text>
									<text class="f28pc4">{{item.time}}</text>
								</view>
								<view class="f1 vrc">
									<text class="f32pc2 bold">-￥{{item.xfposMoney}}元</text>
									<text class="f28pc4">余额：￥{{item.xfcardMoney}}元</text>
								</view>
							</view>
						</view>
					</block >
				</view>
			</view>
		</block>
		<view class="mark-main" v-if="showNotice == 1">
			<view  @click="showNoticeModel(2)" :class="[showNotice==1?'animate-down':'',showNotice ==2?'animate-top':'']" class="mark-notice">
				<view class="hcc header">
					公司账号的介绍说明
				</view>
				<view class="mt20">
					裕同集团为员工提供福利,每月根据福利层自动发放饭补,存至公司账户,可用于食堂刷卡消费.
				</view>
				<view class="f36 main-color abs bottom30 right30">
					知道了
				</view>
			</view>
		</view>
		<yt-loading></yt-loading>
		<yt-null notice="暂无消费记录" v-if="showytNull"></yt-null>
	</view>
</template>

<script>
	import customNavigator from '@/components/custom-navigator.vue'
	import ytPicker from '@/components/yt-picker.vue'
	import consumeRecord  from '@/api/api-consume.js'
	import ytNull from '@/components/yt-null/yt-null.vue'
	import {flitterTime, formatDate} from '@/common/js/util.js'
	export default {
		components: {
			ytPicker,
			customNavigator,
			ytNull
		},
		data() {
			return {
				curDate: '', // 当前选中的时间
				dateList: [], // 日期列表  三个月
				curType: 1, // 当前账户
				curMonth: '', // 当前月份
				showNotice: '', // 展开说明
				current : 1 ,// 当前页数
				size : 10 ,//页数大小
				hasMore : false,
				showytNull : false, //无数据时显示
				totalAmount : '', //总金额
				consumeList : []
				
			}
		},
		computed: {
			list () {
				let that = this
				for (let i = 0; i < that.consumeList.length; i++) { // 截取年月日 时分秒
					that.consumeList[i].data = that.consumeList[i].xfposDay.slice(0,10)
					that.consumeList[i].data = this.dayFormat(that.consumeList[i].data) // 格式化时间
					that.consumeList[i].time = that.consumeList[i].xfposDay.slice(11)
				}
				let list =  this.mergerTime() //合并日期相同的消费记录
				return list
			},
		},
		onLoad() {
			
			const time = new Date()
			const year = time.getFullYear()
			const month = time.getMonth() +1
			this.curMonth = month
			this.dateList = this.formatStartDate(year,month)
			this.curDate = `${year}年${month>9?month:'0'+month}月消费记录`
			this.queryDate = `${year}-${month>9?month:'0'+month}`
			this.getConsumeList()
			this.getConsumeCount()
		},
		onPullDownRefresh() {
			this.init()
			this.getConsumeList()
		},
		onReachBottom() {
			if (this.hasMore) {
				this.$ytHint.toast({
					title: '没有数据了'
				})
				return
			}
			this.current ++
			this.getConsumeList()
		},
		methods: {
			init () {
				this.current = 1
				this.showytNull = false
				this.hasMore = false
			}, // 初始化状态
			// 获取消费总金额
			async getConsumeCount () {
				try{
					const res = await consumeRecord.consumeCount({
						queryDate : this.queryDate,
						acctType : this.curType
					})
					if ( res.data.success ) {
						this.totalAmount = res.data.data
					}
				}catch(e){
					//TODO handle the exception
					console.log(e);
					throw e
				}
			},
			// 获取消费记录列表
			async getConsumeList () {
				try{
					const res = await consumeRecord.consumeList({
						current : this.current,
						size : this.size,
						queryDate : this.queryDate,
						acctType : this.curType
					})
					uni.stopPullDownRefresh()
					if ( this.current == 1 ) {
						if (res.data.success) {
							this.consumeList =res.data.data.records
							this.showytNull = false
						}
						if ( this.consumeList.length == 0 ) this.showytNull = true
					}else {
						if ( res.data.data.records.length == 0 ) {
							this.hasMore = true
							return
						}
						this.consumeList.push(...res.data.data.records)
					}
				}catch(e){
					//TODO handle the exception
					console.log(e);
					throw e
				}
			},
			// 合并消费记录
			mergerTime () {
				let that = this
				let list = []
				for (let i = 0; i < that.consumeList.length; i++) { // 日期相同时 消费记录显示在一个日期里面
					if ( i === 0) {
						list.push({
							data : that.consumeList[i].data,
							listItem : [ that.consumeList[i] ] 
						})
						console.log(1);
					}
					if (i > 0) {
						if (that.consumeList[i].data == that.consumeList[i-1].data) {
							list[list.length - 1].listItem.push(that.consumeList[i])
						} else {
							list.push({
								data : that.consumeList[i].data,
								listItem : [ that.consumeList[i] ] 
							})
						}
					}
				}
				return list
			},
			// 格式化时间
			dayFormat (date) {
				const curTime = Date.now()
				const dateTime = new Date(flitterTime(date)).getTime()
				const day = Math.floor((curTime-dateTime)/86400000)
				switch (day){
					case 0:
						return '今天'
						break;
					case 1:
						return '昨天'
						break;
					case 2:
						return '前天'
						break;
					default:
						return date
						break;
				}
			},
			// 计算三个月的日期
			formatStartDate(year, month) {
				let arr = []
				if (month -3>=1) {
					for(let i=month;i>month-3;i--) {
						arr.push(`${year}-${i>9?i:'0'+i}`)
					}
				} else {
					for(let i=0;i<3;i++) {
						if (month<=0) {
							year--
							month = 12
						}
						arr.push(`${year}-${month>9?month:'0'+month}`)
						month--
					}
				}
				return arr
			},
			// 账户类型
			exchangeType() {
				if (this.curType == 1) this.curType = 2
				else if (this.curType == 2) this.curType = 1
				this.init()
				this.getConsumeList()
				this.getConsumeCount()
			},
			// 显示提示model
			showNoticeModel(type) {
				this.showNotice = type
			},
			getPickerData(e) {
				const arr = e.value.split('-')
				this.curDate = `${arr[0]}年${arr[1]}月消费记录`
				this.queryDate = `${arr[0]}-${arr[1]}`
				this.init()
				this.getConsumeList()
				this.getConsumeCount()
			}
		},
	}
</script>

<style lang="scss" scoped>
	.card {
		width: 690upx;
		height: 200upx;
		padding: 30upx;
		margin: 20upx auto;
		border: 2upx solid #dddddd;
		border-radius: 20upx;
		box-shadow: 0upx 5upx 5upx 2upx #ccc;
	}
	.mark-main {
		position: fixed;
		top:0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0,0,0,.5);
		z-index:1000;
		.mark-notice {
			position: fixed;
			top: 20%;
			left: 75upx;
			right: 30upx;
			width: 600upx;
			height: 50%;
			padding: 30upx;
			border-radius: 20upx;
			background-color: #fff;
			transform: translateY(-200%);
			box-shadow: 0upx 5upx 5upx 2upx #ccc;
			&.animate-top {
				transform: translateY(-200%);
				animation: transTop 0.6s ease-in-out;
			}
			&.animate-down {
				transform: translateY(0);
				animation: transDown 0.6s ease-in-out;
			}
		}
		.header{
				border-bottom:2upx solid #ccc;
				height: 100upx;
		}
	}
	@keyframes transDown {
		from {
			transform: translateY(-200%);
		}
		to{
			transform: translateY(0);
		}
	}
	@keyframes transTop {
		from {
			transform: translateY(0);
		}
		to{
			transform: translateY(-200%);
		}
	}
</style>

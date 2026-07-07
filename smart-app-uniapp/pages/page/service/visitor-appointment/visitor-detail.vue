<template>
	<view v-if="visitor.visitorName" class="view-list mw bgf">
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item  @click="showVisitorPhoto(visitor.visitorPhoto)" showImage=false showArrow=false title="访客照片" hasRightImage=true :rightImage="visitor.visitorPhoto" hasTip=false></yt-list-item>
			<yt-list-item  showImage=false showArrow=false title="访客姓名" :tipContent="visitor.visitorName"></yt-list-item>
			<yt-list-item  showImage=false showArrow=false title="访客手机" :tipContent="visitor.visitorMobile"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="访客单位" :tipContent="visitor.visitorCompany"></yt-list-item>
			<yt-list-item hideLastBorder=true  showImage=false showArrow=false title="事由" :tipContent="visitor.visitReason"></yt-list-item>
			<yt-list-item hideLastBorder=true  showImage=false showArrow=false title="驻厂说明" :tipContent="visitor.remark" v-if="visitor.remark"></yt-list-item>
			<yt-list-item
			showImage=false
			v-if = "visitor.visitReason == '园区驻厂' && visitState == 2"
			title="驻厂周期"
			>
				<yt-picker @changePicker="getPickerDataZq" idType="1" :value="value ? value : '请选择'" :arrData="extraworkClassType"></yt-picker>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bge"></view>
		<view class="hbc pt20 pb20">
			<text>身份证信息</text>
			<text v-if="visitor.visitorCertNo">{{visitor.visitorCertNo}}</text>
		</view>
		<view class="pl20 pt30 pb40 pr20 mw bge"  v-if="visitor.visitorBackPhoto && visitor.visitorFrontPhoto">
			<view class="pt20 pb20" style="margin: 0 auto;width: 60%;" v-if="visitor.visitorBackPhoto && visitor.visitorFrontPhoto">
				<view style="width: 100%;height: 300upx;" class="hcc">
					<image :src="visitor.visitorFrontPhoto" lazy-load="true" class="mw mh" @click="clooseImg(visitor.visitorFrontPhoto)"></image>
				</view>
				<view style="width: 100%;height: 300upx;" class="hcc mt30">
					<image :src="visitor.visitorBackPhoto" lazy-load="true" class="mw mh" @click="clooseImg(visitor.visitorBackPhoto)"></image>
				</view>
			</view>
		</view>
		<view class="mw h20 bge"></view>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false title="车牌号" :tipContent="visitor.plateNumber"></yt-list-item>
			<yt-list-item  showImage=false showArrow=false title="预约起始时间" :tipContent="visitor.startTime"></yt-list-item>
			<yt-list-item title="预约截止时间" :tipContent="visitor.endTime" @click="onShowDatePicker" 
			showImage="false" v-if="type == 1 && visitState == 2 && !showReadStatus"></yt-list-item>
			<yt-list-item title="预约截止时间" :tipContent="visitor.endTime" showArrow=false showImage=false v-else></yt-list-item>
			<yt-list-item :navUrl="'./accompany-people?type=2&visitId='+visitId" hideLastBorder=true showImage=false title="随行人员" :tipContent="accompanyPeopleNum+'名'">
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bge"></view>
		<yt-list :ytClass="['pr30 pl30']">
			<!-- <yt-list-item showImage=false showArrow=false title="员工号" :tipContent="visitor.employeeId"></yt-list-item> -->
			<yt-list-item showImage=false showArrow=false title="被访对象" :tipContent="visitor.employeeName"></yt-list-item>
			<yt-list-item hideLastBorder=true showImage=false showArrow=false title="被访手机号" :tipContent="visitor.employeeMobile"></yt-list-item>
		</yt-list>
		<view class="mw h20 bge" v-if="visitor.processList"></view>
		<view style="padding: 30upx;">
			<view class="mb20 fs30pc" style="font-weight: bold;font-size: 30upx;" v-if="visitor.processList">
				审批记录
			</view>
			<block v-for="(item, index) in visitor.processList" :key="index">
				<view class="hlc">
					<view class="vbc mr30" style="align-items: center;">
						<image src="../../../../static/gou1.png" style="height: 60upx;width: 60upx;" v-if="item.status == 0"></image>
						<image src="../../../../static/cha-tianchong.png" style="height: 60upx;width: 60upx;" v-if="item.status == 1"></image>
						<image src="../../../../static/daishenpi.png" style="height: 60upx;width: 60upx;" v-if="item.status == 2"></image>
						<text style="color: #16C4AF;" v-if="item.status == 0">审批通过</text>
						<text style="color: #16C4AF;" v-if="item.status == 1">审批拒绝</text>
						<text style="color: #16C4AF;" v-if="item.status == 2">等待审批</text>
					</view>
					<view class="vb">
						<text>{{item.staffName}} {{item.staffJche}}</text>
						<text class="tc9" style="color: #999999;" v-if="item.status == 2">{{item.createDate}}</text>
						<text class="tc9" style="color: #999999;" v-else>{{item.recordDate}}</text>
					</view>
				</view>
			</block>
		</view>
		<view v-if="type == 1 && visitState == 2 && !showReadStatus" style="height: 120upx;">
			
		</view>
		<view v-if="type == 1 && visitState == 2 && !showReadStatus && guo" class="fixed bottom0 left0 hcc mw h100">
			<view @click="refuse" class="err-bg cf f32 f1 mh hcc">拒绝</view>
			<view @click="pass" class="main-bg cf f32 f1 mh hcc">通过</view>
		</view>
		<view class="status-icon">
			<image v-if="visitState == 0" src="/static/img/serive/pass.png" mode=""></image>
			<image v-if="visitState == 1" src="/static/img/serive/reject.png" mode=""></image>
			<image v-if="visitState == 3" src="/static/img/serive/arrive.png" mode=""></image>
			<image v-if="visitState == 4" src="/static/img/serive/pass-not.png" mode=""></image>
			<image v-if="visitState == 5" src="/static/yilikai.png" mode=""></image>
		</view>
		<!-- <show-read :showRead="showReadStatus"></show-read> -->
		<yt-loading></yt-loading>
		<mx-date-picker format="yyyy-mm-dd hh:ii" :show="showPicker" :type="type1" color:="#5c92ff" :show-tips="true" @confirm="onSelected"/>
	</view>
</template>

<script>
	import {checkCarNum, checkPhone, dateMonment, flitterTime, formatDateTime,formatDate,formatDateTime2,formatTime2} from '@/common/js/util.js'
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import showRead from '@/components/show-read.vue'
	import {base64ToPath} from 'image-tools'
 	import visit from '@/api/api-visitor.js'
	import plusNews from '@/api/api-plusnews.js'
	import ytpicker from '@/components/yt-picker.vue';
	import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue';
	import { storage } from '@/tools/storage.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'mx-date-picker': MxDatePicker,
			'show-read': showRead,
			'yt-picker': ytpicker,
		},
		data () {
			return {
				visitor: {},
				value : '',
				type1: 'datetime',
				showPicker: false,
				accompanyPeopleNum: 0,
				extraworkClassType : ['一个月', '两个月','三个月', '六个月'],
				visitId: '',
				visitState: '',
				startTime : '',
				endTime : '',
				type: 0,
				showReadStatus: false,
				recordId: '' ,// 消息记录id
				guo : true
			}
		},
		onLoad(e) {
			console.log(e);
			this.type = e.type
			this.visitId = e.visitId
			this.visitState = e.visitState
			this.getVisitorDetail({visitId: e.visitId})
			if (e.recordId) this.recordId = e.recordId 
			if (e.readState != undefined && e.readState == 1) {
				this.showReadStatus = true
			}
			
		},
		methods: {
			onSelected(e) {
				this.showPicker = false;
				// console.log();
				let endTime = new Date(e.value).getTime()
				let startTime = new Date(this.visitor.startTime).getTime()
				if (endTime < startTime) {
					this.$ytHint.toast({
						title: '离开时间不能小于开始时间'
					})
					return
				}
				this.visitor.endTime = e.value + ':00'
			},
			onShowDatePicker () {
				this.showPicker = true;
			},
			getPickerDataZq (e) {
				let data = formatDateTime2(new Date())
				let time = formatTime2(new Date())
				let startMonth = data.month
				if (startMonth < 10) {
					startMonth = '0' + startMonth
				}
				let startTime = data.year + '-' + startMonth + '-' + data.day + ' ' + time
				this.startTime = startTime
				let year = data.year
				let month = data.month
				let day = data.day
				switch (e.value){
					case '一个月':
						month = month + 1
						if (month > 12) {
							year = year + 1
							month = month - 12
						}
						break;
					case '三个月':
						month = month + 3
						if (month > 12) {
							year = year + 1
							month = month - 12
						}
						break;
					case '两个月':
						month = month + 2
						if (month > 12) {
							year = year + 1
							month = month - 12
						}
						break;
					case '六个月':
						month = month + 6
						if (month > 12) {
							year = year + 1
							month = month - 12
						}
						break;
					default:
						break;
				}
				if (month < 10) {
					month = '0' + month
				}
				if (day < 10) {
					day = '0' + day
				}
				let endtime = year + '-' + month + '-' + day + ' ' + time
				this.endTime = endtime
			},
			// 预览详情
			async getVisitorDetail(obj) {
				try{
					const res = await visit.visitDetail(obj)
					if (!res) return
					this.visitor = res.data.data
					let that = this
					let badge = storage.getSync(storage.USER_NAME)
					console.log(badge);
					for (var i = 0; i < that.visitor.processList.length; i++) {
						if (that.visitor.processList[i].recordNode == 1 && that.visitor.processList[i].status == 0 && that.visitor.processList[i].staffBadge == badge) {
							this.guo = false 
						}
					}
					this.visitState = res.data.data.visitState
					if (this.visitState != 2) {
						this.changePlusNewsStatus(this.recordId)
					}
					if (this.visitor.member === null) {
						this.accompanyPeopleNum = 0
					} else {
						this.accompanyPeopleNum = this.visitor.member.length
					}
					if (this.visitor.visitReason == '园区驻厂') {
						this.endTime = this.visitor.endTime
						this.startTime = this.visitor.startTime
						let endTime = this.endTime.replace(/-/g,'/')
						let startTime = this.startTime.replace(/-/g,'/')
						let time = new Date(endTime).getTime() - new Date(startTime).getTime()
						let month = parseInt(time/86400000)
						if (month < 33) {
							this.value = '一个月'
						}
						if (month > 33 && month < 63) {
							this.value = '两个月'
						}
						if (month > 63 && month < 95) {
							this.value = '三个月'
						}
						if (month > 95 && month < 191) {
							this.value = '六个月'
						}
					}
				}catch(e){
					console.log(e);
					throw e
				}
			},
			showVisitorPhoto(img) {
				uni.previewImage({
					current: '0',
					urls: [img]
				})
			},
			clooseImg (img) {
				console.log(img);
				uni.previewImage({
					urls:[img]
				})
			},
			refuse() {
				const obj = {
					visitId: this.visitId,
					approveVisitState: 1,
					startTime : this.startTime,
					endTime : this.visitor.endTime
				}
				this.sendApproval(obj)
			},
			pass() {
				const obj = {
					visitId: this.visitId,
					approveVisitState: 0,
					startTime : this.startTime,
					endTime : this.visitor.endTime
				}
				this.sendApproval(obj)
			},
			// 点击消息为已读
			async changePlusNewsStatus (recordId) {
				try{
					console.log(recordId);
					const res = await plusNews.changePlusNews(recordId)
				}catch(e){
					//TODO handle the exception
					console.log(e);
					throw e
				}
			},
			sendApproval(obj) {
				visit.visitApprove(obj).then(res => {
					console.log(res);
					if (!res) return
					if (res.data.data) {
						this.$ytHint.toast({
							title: '操作成功',
							success: () => {
								uni.setStorageSync('hasRefresh',true)
								uni.navigateBack({
									delta: 1
								})
							}
						})
					}
					this.changePlusNewsStatus(this.recordId)
				})
			}
 		},
	}
</script>

<style scoped>
	.view-list {
		padding-bottom: 20upx;
	}
	.status-icon {
		position: absolute;
		left: 50%;
		top: 30%;
		transform: translateX(-50%);
		width: 250upx;
		height: 250upx;
		z-index: 998;
	}
</style>

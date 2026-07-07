<template>
	<view class="mw bgf">
		<view v-if="checkingInDetail.dateInfo" class="user hlc pr30 pl30">
			<view class="icon round w90 h90 hcc overflow main-bg cf mr30">
				{{employeeName}}
			</view>
			<view class="f1 vlc">
				<view class="mw hbc">
					<text class="f30 main-color classdesc">班次: {{checkingInDetail.classDesc}}</text>
					<text class="f28pc4">{{checkingInDetail.dateInfo}} {{checkingInDetail.weekInfo}}</text>
				</view>
				<view class="hbc mw">
					<view class="hbc w200">
						<view class="hcc">
							<view class="w20 h20 main-bg round mr10"></view>
							<text class="f30pc2">正常</text>
						</view>
						<view class="hcc">
							<view class="w20 h20 err-bg round mr10"></view>
							<text class="f30pc2">异常</text>
						</view>
					</view>
					<view v-if="showMonth" @click="changeTypeCalender('week')" class="borderC pl10 pr10 mr30">周</view>
				</view>
			</view>
		</view>
		<view v-if="showMonth" class="rel vcc calendar">
			<imt-calendar v-if="monthTime==0" @click="getCurDate" @dateChange="getChangeDate" :curMonth="curMonth" :selected="checkingInList"></imt-calendar>
		</view>
		<view v-if="showWeek" class="rel week">
			<view class="hbc">
				<view class="hcc w300 h120">
					<view class="f40pc month-week">
						{{curMonth}}月
					</view>
					<view class="vcc">
						<text class="">{{curWeek}}</text>
						<text class="">{{curYear}}年</text>
					</view>
				</view>
				<view @click="changeTypeCalender('month')" class="borderC pl10 pr10 mr30">月</view>
			</view>
			<scroll-view v-if="weekMonthList.length>0" id="tab-bar" class="uni-swiper-tab h150" scroll-with-animation="true"
			 scroll-x :scroll-left="weekScrollLeft">
				<view v-for="(item, index) in weekMonthList" :key="index" class="swiper-tab-list w100 rel" @click="changeDay(item, index)">
					<view class="week-desc">{{item.weekDesc}}</view>
					<view class="week-desc rel h100 vcc round" :class="[curDay == item.monthlyDay?'main-color-tint': '',selectDay == item.monthlyDay?'main-bg':'']">
						<view :class="[curDay < item.monthlyDay?'f30pc':'f30',curDay == item.monthlyDay || selectDay == item.monthlyDay?'cf': '']">{{item.monthlyDay}}</view>
						<view v-if="item.checkState=='0'" class="abs w10 dot bottom0 h10 main-bg round"></view>
						<view v-if="item.checkState=='1'" class="abs w10 dot bottom0 h10 err-bg round"></view>
					</view>
				</view>
			</scroll-view>
		</view>
		<view class="hlc pl30 h120 mw  border-bottom-D border-top-D pt30 pb30">
			<view class="w40 h40 round mr20 vc">
				<image src="/static/img/serive/time.png" mode=""></image>
			</view>
			<view class="f32pc2 mr30 bold">{{checkingInDetail.dateInfo}}</view>
			<view class="f32pc bold">打卡<text class="main-color">{{checkingInDetail.totalPunchCount}}</text>次</view>
		</view>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item @click="toDetail(2)" title="考勤异常" showImage=false>
				<view class="f30" :class="[curCheckStatus == '1'?'err-color':'main-color']">
					{{curCheckStatus == '1'?isPatch?'已补卡':'未补卡':'无'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="toDetail(1)" title="当日明细" showImage=false></yt-list-item>
			<yt-list-item @click="toDetail(4)" title="当月明细" showImage=false></yt-list-item>
			<yt-list-item @click="toDetail(3)" title="考勤汇总" showImage=false></yt-list-item>
		</yt-list>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import imtcalendar from '@/components/imt-calendar/imt-calendar.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import attendance from '@/api/api-attendance.js'
	import { employee } from '@/api/api-mine.js'; // 请求接口的参数
	import {
		formatDate,
		flitterTime,
		formatWeek,
		formatMonth
	} from '@/common/js/util.js'
	import {
		storage
	} from '@/tools/storage.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'imt-calendar': imtcalendar,
			'yt-button': ytbutton
		},
		data() {
			return {
				employeeName: '', // 员工名
				showMonth: true, // 默认月模式
				showWeek: false, // 周模式
				weekScrollLeft: 0, // 点击日期的位置
				curDateScrollLeft: 0, // 当前日期的位置
				checkingInList: [],
				checkingInDetail: {},
				curDate: '', // 当前的时间日期
				hasApply: false,
				isPatch: false, // 是否已经补卡
				patchData: {}, // 当前的考勤状态
				curMonth: 0, // 月
				curYear: 0, // 年
				curWeek: 0, // 星期
				curDay: 0, // 日
				selectDay: 0, // 选中的时间
				monthTime: 3, // 执行3次 查询近三个月的考勤记录 
				curCheckStatus: '', // 当前考情缺卡情况 0 缺卡 1不缺
				weekMonthList: [], // 周打卡
				weekList: ['周日', '周一', '周二', '周三', '周三', '周四', '周五', '周六']
			}
		},
		onLoad() {
			const time = new Date()
			let month = time.getMonth() + 1
			month = month > 9 ? month : `0${month}`
			this.curMonth = month
			const year = time.getFullYear()
			this.curYear = year
			this.curDay = time.getDate()
			this.curWeek = formatWeek(time.getDay())
			this.curDate = formatDate(time)
			this.getCheckingInList(year, month)
			this.getSuccessDetail(formatDate(time))
			this.getEmployeeInfo()
		},
		onShow() {
			const isRefresh = storage.getSync('isRefresh') || false
			if (isRefresh) {
				this.monthTime = 3
				storage.remove('isRefresh')
				this.getCheckingInList(this.curYear, this.curMonth)
				this.getSuccessDetail(this.curDate)
			}
		},
		onNavigationBarButtonTap() {
			uni.navigateTo({
				url: './record'
			})
		},
		methods: {
			//员工信息
			async getEmployeeInfo() {
				try {
					const res = await employee.baseInfo();
					if (!res) return
					
					this.employeeName = res.data.data.employeeName
				} catch (e) {
					console.log(e);
					throw e;
				}
			},
			// 获取考勤列表
			async getCheckingInList(year, month) {
				console.log(this.monthTime);
				if (this.monthTime == 0) return
				const obj = {
					queryDay: `${year}-${month}`
				}
				try {
					const res = await attendance.list(obj)
					if (!res) return
					if (this.monthTime == 3) { // 处理本月数据 周模式下的数据 处理三数据
						let data = []
						let curIndex = 0
						if (res.data.data.records.length) {
							data = res.data.data.records
						} else {
							for(let i = 1;i<=this.curDay;i++) {
								res.data.data.records.push({
									checkState: "0",
									fullDate: `${this.curYear}-${this.curMonth}-${'0'+i}`,
									isRecord: "1",
									monthlyDay: `${i}`
								})
							}
							data = res.data.data.records
						}
						let leng = data.length
						let startDay = parseInt(data[0].monthlyDay) // 开始记录考勤的日期
						let endDay = parseInt(data[leng - 1].monthlyDay) // 当天的考勤日期
						let hasList = JSON.parse(JSON.stringify(data)) // 深拷贝 有考勤的时间点
						let beforeList = [] // 考勤记录开始之前
						let afterList = [] // 考勤结束之后
						for (let i = 1; i < formatMonth(this.curYear, parseInt(this.curMonth)) + 1; i++) {
							if (this.curDay == i) curIndex = i // 记录当前位置
							if (i < startDay || i > endDay) { // 分别处理这个状态下的时间点
								if (i < startDay) {
									beforeList.push({
										monthlyDay: i,
										weekDesc: formatWeek(new Date(
											`${this.curYear}/${this.curMonth>9?this.curMonth:'0'+this.curMonth}/${i>9?i:'0'+i}`).getDay()),
										fullDate: `${this.curYear}-${this.curMonth>9?this.curMonth:'0'+this.curMonth}-${i>9?i:'0'+i}`,
										checkState: ''
									})
								} else if (i > endDay) {
									afterList.push({
										monthlyDay: i,
										weekDesc: formatWeek(new Date(
											`${this.curYear}/${this.curMonth>9?this.curMonth:'0'+this.curMonth}/${i>9?i:'0'+i}`).getDay()),
										fullDate: `${this.curYear}-${this.curMonth>9?this.curMonth:'0'+this.curMonth}-${i>9?i:'0'+i}`,
										checkState: ''
									})
								}
							} else {
								hasList.forEach((el, index) => {
									el.weekDesc = formatWeek(new Date(flitterTime(el.fullDate)).getDay())
								})
							}
						}
						this.weekMonthList.push(...beforeList, ...hasList, ...afterList)
						const windowWidth = uni.getSystemInfoSync().windowWidth;
						this.weekScrollLeft = ((curIndex - 5) * windowWidth) / 7;
						this.curDateScrollLeft = this.weekScrollLeft // 存储一份当前日期的位置
						this.monthTime--
						this.checkingInList = []
						this.checkingInList.unshift(data)
						console.log(this.checkingInList);
					} else {
						if (res.data.data.records.length) {
							this.monthTime--
							this.checkingInList.unshift(res.data.data.records)
							console.log(this.checkingInList);
						} else {
							this.monthTime = 0
							return
						}
					}
					month--
					month = month > 9 ? month : `0${month}`
					if (month == 0) {
						year = year - 1
						month = 12
					}
					this.getCheckingInList(year, month)
				} catch (e) {
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 获取正常的考勤信息
			async getSuccessDetail(date) {
				const obj = {
					queryDay: date
				}
				const res = await attendance.successDetail(obj)
				if (!res) return
				this.checkingInDetail = res.data.data
				this.isPatch = 0
			},
			// 获取异常常的考勤信息
			async getErrorDetail(date) {
				const obj = {
					queryDay: date
				}
				const res = await attendance.errorDetail(obj)
				if (!res) return
				this.checkingInDetail = res.data.data
			},
			// 获取考勤时间点
			getCurDate(e) {
				const currentDate = formatDate(new Date())
				if (e == currentDate) {
					this.curDate = e
					this.getSuccessDetail(e)
					this.isPatch = false
					this.curCheckStatus = 0
					return
				}
				this.curDate = e
				console.log(this.checkingInList);
				console.log(e);
				this.checkingInList.forEach(el => {
					el.forEach(el1 => {
						if (el1.fullDate === e) {
							if (el1.checkState == "1") {
								this.checkingInDetail = {}
								this.getSuccessDetail(this.curDate)
								this.isPatch = el1.isRecord == '0' ? true : false
							} else if (el1.checkState == "0") {
								this.getSuccessDetail(this.curDate)
								this.isPatch = false
							}
							this.curCheckStatus = el1.checkState
						}
					})
				})
				console.log(this.isPatch);
			},
			// 详情
			toDetail(type) {
				if (type == 1) {
					if (this.showMonth) {
						uni.navigateTo({
							url: `./success-detail?date=${this.curDate}&status=${this.curCheckStatus}`
						})
					} else if (this.showWeek) {
						uni.navigateTo({
							url: `./success-detail?date=${this.checkingInDetail.dateInfo}&status=${this.curCheckStatus}`
						})
					}

				} else if (type == 2) {
					if (this.curCheckStatus == 0 || this.curCheckStatus == '') return
					if (this.isPatch) { // 如果补过卡 就去查看记录
						uni.navigateTo({
							url: `./record`
						})
						return
					}
					if (this.showMonth) {
						uni.navigateTo({
							url: `./error-detail?date=${this.curDate}`
						})
					} else if (this.showWeek) {
						uni.navigateTo({
							url: `./error-detail?date=${this.checkingInDetail.dateInfo}`
						})
					}
				} else if (type == 3) {
					uni.navigateTo({
						url:`./summary?patchDate=${this.curDate}`
					})
				}
				else if (type == 4) {
					uni.navigateTo({
						url:`./dangyue?patchDate=${this.curDate}`
					})
				}
			},
			// 周模式
			changeDay(item, index) {
				if (item.checkState == '') {
					if (item.fullDate != this.curDate) {
						this.$ytHint.toast({
							title: '当前日期不支持查看考勤信息'
						})
					} else if (item.fullDate == this.curDate) {
						this.getSuccessDetail(item.fullDate)
						this.selectDay = ''
					}
					return
				}
				this.weekMonthList.forEach(el1 => {
					if (el1.fullDate === item.fullDate) {
						if (el1.checkState == "1") {
							this.checkingInDetail = {}
							this.getSuccessDetail(item.fullDate)
							this.isPatch = el1.isRecord == '0' ? true : false
						} else if (el1.checkState == "0") {
							this.getSuccessDetail(item.fullDate)
							this.isPatch = false
						}
						this.curCheckStatus = el1.checkState
					}
				})
				this.curCheckStatus = item.checkState
				this.selectDay = item.monthlyDay
				let curIndex = index
				const windowWidth = uni.getSystemInfoSync().windowWidth;
				this.weekScrollLeft = ((curIndex - 5) * windowWidth) / 7;
			},
			changeTypeCalender(type) {
				if (type === 'month') {
					this.showMonth = true
					this.showWeek = false
					this.selectDay = 0
					this.curCheckStatus = 0
					this.isPatch = true
				} else if (type === 'week') {
					this.showMonth = false
					this.showWeek = true
					this.isPatch = true
					this.curCheckStatus = 0
					this.weekScrollLeft = this.curDateScrollLeft // 切换复原
				}
				this.curDate = formatDate(new Date())
				this.getSuccessDetail(this.curDate)
			},
			getChangeDate(e) {

			}
		},
	}
</script>

<style lang="scss" scoped>
	.user {
		min-height: 108upx;
		z-index: 999;
	}

	.classdesc {
		width: 320upx;
	}

	.calendar {
		height: 800upx;
	}

	.calendar-bg {
		height: 1000upx;
		z-index: 0;
	}

	.weekitem {
		display: inline-block;
		text-align: center;
	}

	.dot {
		left: 50%;
		transform: translateX(-50%);
	}

	.week {
		padding: 20upx 25upx;
	}

	.month-week {
		font-size: 60upx;
	}

	.main-color-tint {
		background-color: rgba($main-color, 0.7);
	}
</style>

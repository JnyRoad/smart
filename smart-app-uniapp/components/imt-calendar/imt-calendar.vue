<template>
	<view class="imt-calendar">
		<view class="calendar-month-wrapper rel">
			<view class="calendar-icon" :style="{color:current&&color}" @click="changeDate(true)"><uni-icon type="arrowleft" size="26"></uni-icon></view>
			<view class="calendar-month">{{currentTime}}</view>
			<view class="calendar-icon" :style="{color:current == calendar.length-1 || color}" @click="changeDate(false)"><uni-icon type="arrowright" size="26"></uni-icon></view>
		</view>
		<view class="calendar-week-wrapper rel">
			<view class="calendar-week" v-for="(item,index) in week" :key="index">{{item}}</view>
		</view>
		<swiper class="calendar-date-swiper rel" :current="current" @change="current = $event.detail.current">
			<swiper-item class="calendar-date-wrapper" v-for="(item,index) in calendar" :key="index">
				<view  @click="taday(value.checkState,value.date,value.imrMonth,value.isPatch)" class="calendar-date rel" :class="[(value.date == curDate && value.imrMonth == curMonth)?'main-color-tint cf':'', value.date == curClickDate && value.imrMonth == curClickMonth?'main-bg': '']" v-for="(value, key) in item" :key="key">
					<text :class="[(value.date == curClickDate && value.imrMonth == curClickMonth)?'cf': '']">{{ value.date }}</text>
					<view v-if="value.checkState == 1" class="dot err-bg abs round"></view>
					<view v-if="value.checkState == 0" class="dot main-bg abs round"></view>
				</view>
			</swiper-item>
		</swiper>
	</view>
</template>

<script>
	import uniicon from '@/components/uni-icon/uni-icon.vue';
	export default {
		components: {
			'uni-icon': uniicon
		},
		data() {
			return {
				week: ['日', '一', '二', '三', '四', '五', '六'],
				current: '',
				startYear: '',
				startMonth: '',
				calendar: [],
				curClickDate: '',// 当亲点击的日期
				curClickMonth: '', // 当前点击的月份
				curDate: '' // 当前日期
			}
		},
		props: {
			selected: {
				type: Array,
				default () {
					return []
				}
			},
			curMonth: {
				type: [String, Number],
				default: ''
			},
			color: {
				type: String,
				default: '#e5373f'
			}
		},
		created() {
			this.curDate = new Date().getDate()
			let year = new Date().getFullYear()
			let month = new Date().getMonth()
			this.startYear = this.selected.length ? this.selected[0][0].fullDate.substr(0, 4) : `${year}`
			this.startMonth = this.selected.length ? this.selected[0][0].fullDate.substr(5, 2) -1 : `${month}`;
			this.calendar = [...Array((year - this.startYear) * 12 + month - this.startMonth+1).keys()].map(i => this.getDate(
				this.format(i)))
			this.current = this.calendar.length - 1
			this.calendar.forEach((el1,index) => {
				el1.forEach(el2 => {
					this.selected[index].forEach(el3 => {
						if (el2.date == el3.monthlyDay) {
							el2.checkState = el3.checkState
							el2.imrMonth = parseInt(el3.fullDate.split('-')[1])
						} else {
							el2.imrMonth = parseInt(el3.fullDate.split('-')[1])
						}
					})
				})
			})
		},
		methods: {
			getDate(e) {
				let time = e.split('-')
				return [...Array(new Date(`${time[0]}-${time[1]}`).getDay())].map(i => ({
					date: ''
				})).concat([...Array([4, 6, 9, 11].includes(time[1] * 1) ? 30 : (time[1] != 2 ? 31 : (time[0] % 4 == 0 ? 29 : 28)))
					.keys()
				].map(i => ({
					date: i + 1
				})))
			},
			format(e) {
				let time = new Date(new Date(this.startYear).setMonth(this.startMonth + e))
				return `${time.getFullYear()}-${(time.getMonth()<9 && '0')+(time.getMonth()+1)}`
			},
			changeDate(type) {
				if (type === true) {
					this.current--
					if (this.current<0) {
						this.current = 0
						return
					}
				} else if (type === false) {
					this.current == this.calendar.length - 1 || this.current++
				}
				this.$emit('dateChange', {
					date: this.format(this.current),
					status: type
				})
			},
			taday(checkState, date, month) {
				if (checkState == undefined || checkState == '' || checkState == null) {
					if (date != this.curDate) {
						this.$ytHint.toast({
							title: '当前日期不支持查看考勤信息'
						})
					} else {
						this.curClickDate = date
						this.curClickMonth = month
						const clickCurrent = `${this.format(this.current)}-${date < 10 ? '0' + date : date}`;
						this.$emit('click', clickCurrent)
					}
					return
				}
				this.curClickDate = date
				this.curClickMonth = month
				const clickCurrent = `${this.format(this.current)}-${date < 10 ? '0' + date : date}`;
				this.$emit('click', clickCurrent)		
			}
		},
		computed: {
			currentTime() {
				if (this.current >= 0) {
					return this.format(this.current)
				}
				
			}
		}
	}
</script>

<style lang="scss" scoped>
	.imt-calendar {
		width: 700upx;
		margin: auto;
		background: #fff;
		border-radius: 10upx;
		text-align: center;
		color: #333;
	}

	.calendar-month-wrapper {
		display: flex;
		justify-content: space-between;
	}

	.calendar-icon {
		font-family: 'imt-calendar';
		width: 100upx;
		font-size: 32upx;
		line-height: 100upx;
		color: #ccc;
	}

	.calendar-month {
		font-size: 32upx;
		line-height: 100upx;
	}

	.calendar-week-wrapper {
		display: flex;
	}

	.calendar-week {
		flex: 1;
		font-size: 32upx;
		line-height: 100upx;
	}

	.calendar-date-swiper {
		height: 600upx;
	}

	.calendar-date-wrapper {
		display: flex;
		flex-wrap: wrap;
	}

	.calendar-date {
		width: 60upx;
		height: 60upx;
		font-size: 32upx;
		line-height: 60upx;
		border-radius: 50%;
		margin: 20upx;
	}
	.dot {
		bottom: -16upx;
		width: 16upx;
		height: 16upx;
		left: 50%;
		transform: translateX(-50%);
	}
	.main-color-tint {
		background-color: rgba($main-color, 0.7)
	}
</style>






<!-- <template>
	<view class="imt-calendar">
		<view class="calendar-month-wrapper rel">
			<view class="calendar-icon calendar-icon-leftArrow" :style="current == 0 || { color: color }" @click="changeDate('true')">
				<uni-icon type="arrowleft" size="26"></uni-icon>
			</view>
			<view class="calendar-month">{{ currentTime }}</view>
			<view class="calendar-icon calendar-icon-rightArrow" :style="current == calendar.length - 1 || { color: color }"
			 @click="changeDate('false')">
				<uni-icon type="arrowright" size="26"></uni-icon>
			</view>
		</view>
		<view class="calendar-week-wrapper rel">
			<view class="calendar-week" v-for="(item, key) in week" :key="key">{{ item }}</view>
		</view>
		<swiper class="calendar-date-swiper rel" duration="500" :current="current" @change="current = $event.detail.current">
			<swiper-item class="calendar-date-wrapper" v-for="(item, index) in calendar" :key="index">
				<view  @click="taday(value.date)" class="calendar-date rel" :class="[value.date == curDate?'main-color-tint':'', value.date == curClickDate?'main-bg': '']" v-for="(value, key) in item" :key="key">
					<text :class="[value.date == curDate?'cf':'', value.date == curClickDate?'cf': '']">{{ value.date }}</text>
					<view v-if="value.checkState == 1" class="dot err-bg abs  round"></view>
				</view>
			</swiper-item>
		</swiper>
	</view>
</template>

<script>
	import uniicon from '@/components/uni-icon/uni-icon.vue';
	export default {
		components: {
			'uni-icon': uniicon
		},
		data() {
			return {
				week: ['日', '一', '二', '三', '四', '五', '六'],
				current: '',
				startYear: '',
				startMonth: '',
				curDate: '', // 当前日期
				calendar: [],
				curClickDate: '0' // 当亲点击的日期
			};
		},
		props: {
			selected: {
				type: Array,
				default () {
					return [];
				}
			},
			color: {
				type: String,
				default: '#5c92ff'
			},

		},
		created() {
			this.curDate = new Date().getDate()
			let year = new Date().getFullYear();
			let month = new Date().getMonth();
			this.startYear = this.selected.length ? this.selected[0].fullDate.substr(0, 4) : `${year}`;
			this.startMonth = this.selected.length ? this.selected[0].fullDate.substr(5, 2) - 1 : `${month}`;
			this.calendar = [...Array((year - this.startYear) * 12 + month - this.startMonth + 1).keys()].map(i => this.getDate(
				this.format(i)));
			this.current = this.calendar.length - 1;
			this.calendar[0].forEach(el1 => {
				this.selected.forEach(el2 => {
					if (el1.date == el2.monthlyDay) {
						el1.checkState = el2.checkState
					}
				})
			})
		},
		methods: {
			changeDate(type) {
				if (type === 'true') {
					this.current--
				} else if (type === 'false') {
					this.current == this.calendar.length - 1 || this.current++
				}
				this.$emit('dateChange', {
					date: this.format(this.current),
					status: type
				})
			},
			getDate(e) {
				let time = e.split('-');
				return [...Array(new Date(`${time[0]}-${time[1]}`).getDay())]
					.map(i => ({
						date: ''
					}))
					.concat(
						[...Array([4, 6, 9, 11].includes(time[1] * 1) ? 30 : time[1] != 2 ? 31 : time[0] % 4 == 0 ? 29 : 28).keys()].map(
							i => ({
								date: i + 1
							}))
					);
			},
			format(e) {
				let time = new Date(new Date(this.startYear).setMonth(this.startMonth + e));
				return `${time.getFullYear()}-${(time.getMonth() < 9 && '0') + (time.getMonth() + 1)}`;
			},
			taday(date) {
				if (date == '') return
				if (date > this.curDate) {
					this.$ytHint.toast({
						title: '无法查看未来的打卡情况'
					})
					return
				}
				this.curClickDate = date
				const clickCurrent = `${this.format(this.current)}-${date < 10 ? '0' + date : date}`;
				this.$emit('click', clickCurrent)
				this.selected.forEach(i => {
					if (clickCurrent === i.date) {
						let time = i.date.split('-');
						// this.calendar[time[1]-this.startMonth-1+(time[0]-this.startYear)*12][time[2]-1+new Date(`${time[0]}-${time[1]}`).getDay()].selected = true
					}
				})

			}
		},
		computed: {
			currentTime() {
				return this.format(this.current);
			}
		}
	};
</script>

<style lang="scss" scoped>
	@font-face {
		font-family: 'imt-calendar';
		src: url('//at.alicdn.com/t/font_1114123_r6yq558axt.ttf') format('truetype');
	}

	.imt-calendar {
		width: 700upx;
		background: #fff;
		border-bottom-right-radius: 40upx;
		border-bottom-left-radius: 40upx;
		text-align: center;
		color: #333;
	}

	.calendar-month-wrapper {
		display: flex;
		justify-content: space-between;
		border-bottom: 2upx solid #e5e5e5;
	}

	.calendar-icon {
		font-family: 'imt-calendar';
		width: 100upx;
		font-size: 32upx;
		line-height: 100upx;
		color: #ccc;
	}

	.calendar-month {
		font-size: 32upx;
		line-height: 100upx;
	}

	.calendar-week-wrapper {
		display: flex;
	}

	.calendar-week {
		flex: 1;
		font-size: 32upx;
		line-height: 100upx;
	}

	.calendar-date-swiper {
		height: 600upx;
	}

	.calendar-date-wrapper {
		display: flex;
		flex-wrap: wrap;
	}

	.calendar-date {
		width: 60upx;
		height: 60upx;
		font-size: 32upx;
		line-height: 60upx;
		border-radius: 50%;
		margin: 20upx;
	}

	.dot {
		bottom: -16upx;
		width: 8upx;
		height: 8upx;
		left: 50%;
		transform: translateX(-50%);
	}
	.main-color-tint {
		background-color: rgba($main-color, 0.7)
	}
</style>
 -->
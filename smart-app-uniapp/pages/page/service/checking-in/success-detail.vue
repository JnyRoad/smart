<template>
	<view class="mw bgf">
		<view class="mw pl30 pr30 hbc border-bottom-D h90">
			<text class="f32pc2">考勤日期</text>
			<text class="f30pc2">{{curDate}}</text>
		</view>
		<view class=" pt30 pr30 pl30">
			<view class="vcc mw">
				<text class="main-color f30">打卡点明细</text>
				<view v-if="kqTime.length<=0">无</view>
				<view v-if="kqTime.length>0" class="mw">
					<block v-for="(item, index) in kqTime" :key="index">
						<text v-if="item" class="f28pc2 mw h90 hcc border-bottom-D">{{item}}</text>
					</block>
				</view>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import attendance from '@/api/api-attendance.js'
	export default {
		data () {
			return {
				kqTime: [],
				curDate: ''
			}
		},
		onLoad (e) {
			this.curDate = e.date
			if (e.status == 0) {
				this.getSuccessDetail(e.date)
			} else if (e.status == 1) {
				this.getSuccessDetail(e.date)
			}
		},
		methods: {
			// 获取正常的考勤信息
			async getSuccessDetail(date) {
				const obj = {
					queryDay: date
				}
				const res = await attendance.successDetail(obj)
				if (!res) return
				this.kqTime = res.data.data.kqTime
			},
			// 获取异常常的考勤信息
			async getErrorDetail (date) {
				const obj = {
					queryDay: date
				}
				const res = await attendance.errorDetail(obj)
				if (!res) return
				const data = res.data.data
				this.kqTime = [data['fifthOut'],data['fifthEnter'],data['fourthOut'],data['fourthEnter'],data['secondOut'],data['secondEnter']]
			},
		},
	}
</script>

<style>
</style>

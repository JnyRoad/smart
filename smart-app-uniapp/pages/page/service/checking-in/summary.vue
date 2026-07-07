<template>
	<view class="mw bgf">
		<view class="mw pl30 pr30 hbc border-bottom-D h90">
			<text class="f32pc2">考勤月份</text>
			<view class="hlc">
				<picker mode="selector" :range="timeList" @change="changeTime" :value="index">
					<view class="f32pc2">{{timeList[index]}}</view>
				</picker>
				<view class="h30 w20 hcc ml20">
					<image src="/static/img/mine/next.png" class="mw mh"></image>
				</view>
			</view>
		</view>
		<view class="hlc pl20 header">
			<text>单位: H小时, M分钟</text>
			<text class="ml20" v-if="!sumMary">查询暂无数据</text>
		</view>
		<view class="list-body">
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					应出勤H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a6 ? sumMary.a6 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					实出勤H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a7 ? sumMary.a7 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					平时加班H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a10 ? sumMary.a10 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					周末加班H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a11 ? sumMary.a11 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					法定节日加班H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a12 ? sumMary.a12 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					事假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a15 ? sumMary.a15 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					病假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a16 ? sumMary.a16 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					待料H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a17 ? sumMary.a17 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					工伤假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a18 ? sumMary.a18 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					婚假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a19 ? sumMary.a19 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					丧假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a20 ? sumMary.a20 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					产假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a21 ? sumMary.a21 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					陪产假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a22 ? sumMary.a22 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					陪检假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a23 ? sumMary.a23 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					放行H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a24 ? sumMary.a24 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					年休假H
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a25 ? sumMary.a25 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					迟到累计M
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a27 ? sumMary.a27 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					迟到次数
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a28 ? sumMary.a28 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					早退累计M
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a29 ? sumMary.a29 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					早退次数
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a30 ? sumMary.a30 : 0}}
				</view>
			</view>
			<view class="item-body hlc">
				<view class="left-item hrc pr20 mh">
					夜班次数
				</view>
				<view class="right-item mh hlc pl20">
					{{sumMary.a36  ? sumMary.a36 : 0}}
				</view>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import attendance from '@/api/api-attendance.js'
	import { formatMonth } from '@/tools/index.js'
export default {
	data() {
		return {
			curYear: '' ,// 当前月份
			timeList : [],
			index : 0,
			sumMary : {}
		}
	},
	onLoad(e) {
		let data = formatMonth(new Date())
		let month = data.slice(5,7)
		let year = data.slice(0,4)
		month = month - 1
		if (month < 10) {
			month = '-0' + month
		} else {
			month = '-' + month
		}
		this.curYear = year + month + '-01'
		this.getTime()
		this.getpatchSkyPay()
	},
	methods: {
		async getpatchSkyPay () {
			try{
				const res = await attendance.patchSkyPay({ patchDate : this.curYear + " 00:00:00"})
				if (!res) return
				this.sumMary = res.data.data
				console.log(this.sumMary);
			}catch(e){
				//TODO handle the exception
				console.log(e);
				throw e
			}
		},
		getTime () {
			let data = formatMonth(new Date())
			// data = '2019-12'
			let year = data.slice(0,4)
			let month = data.slice(5,7)
			let item
			let monthRemv
			let lin
			for (var i = 1; i < 13; i++) {
				monthRemv = month - i
				if (monthRemv == 0) {
					monthRemv = 12
					year = year - 1
				}
				if (monthRemv < 0) {
					monthRemv = 12 + monthRemv
				}
				if (monthRemv > 9) {
					lin = '-'
				} else {
					lin = '-0'
				} 
				item = year + lin + monthRemv + '-01'
				this.timeList.push(item)
			}
			// month = month - 1
			// if (month < 10) {
			// 	month = '-0' + month
			// } else {
			// 	month = '-' + month
			// }
			// this.curYear = year +  month + '-01'
		},
		changeTime (e) {
			let index = e.detail.value
			this.index = index
			this.curYear = this.timeList[index]
			this.getpatchSkyPay()
		}
	},
}
</script>

<style lang="scss">
	.header {
		height: 60upx;
		width: 100%;
		background-color: #F2F3F3;
	}
	.item-body {
		height: 80upx;
		border-bottom: 1px solid #E6E6E4;
		.right-item {
			width: 70%;
			
		}
		.left-item {
			width: 30%;
			border-right: 2px solid #F2F3F3;
		}
	}
</style>

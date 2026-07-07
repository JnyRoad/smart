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
		<view class=" pt30 pr30 pl30">
			<view class="vcc mw">
				<text class="main-color f30">打卡点明细</text>
				<view class="">

				</view>
				<view v-if="list.length<=0">无</view>
				<view class="mw">
					<block v-for="(item, index) in list" :key="index">
						<view class="hbc border-bottom-D">
							<text class="radius hcc">{{index+1}}</text>
							<text class="f28pc2 h90 hcc" v-if="item.kqdate">{{item.kqdate}}</text>
							<text class="f28pc2 h90 hcc" v-if="item.kqtime">{{item.kqtime}}</text>
						</view>
					</block>
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
			list: [],
			index : 0
		}
	},
	onLoad() {
		this.getTime()
		this.getMonth()
	},
	onPullDownRefresh() {
		// this.getTime()
		this.getMonth()
	},
	methods: {
		async getMonth () {
			try{
				const res = await attendance.getMonth({ patchDate : this.curYear})
				uni.stopPullDownRefresh()
				if (!res) return
				this.list = res.data.data
				console.log(this.list);
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
			month = month - 1
			if (month < 10) {
				month = '-0' + month
			} else {
				month = '-' + month
			}
			this.curYear = year +  month + '-01'
		},
		changeTime (e) {
			let index = e.detail.value
			this.index = index
			this.curYear = this.timeList[index]
			this.getMonth()
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
	.radius {
		height: 40upx;
		width: 40upx;
		border-radius: 50%;
		border: 1px solid #ccc;
	}
</style>

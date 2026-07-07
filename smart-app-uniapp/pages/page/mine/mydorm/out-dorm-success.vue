<template>
	<view class="yt-page bgf">
		<view class="pr30 pl30">
			<view v-if="status" class="item rel hb bgf rad10 pt30 pb30 pl30 pr30 mt40" @click="onOutdetail">
				<view class="image mr60"><image src="/static/img/mine/out-dorm.png"></image></view>
				<view class="vlc f1">
					<view class="hbc mw">
						<text class="f32pc bold">宿舍信息</text>
						<view class="f30 pl20 pr20 rad10 main-bg cf">{{dorm.statusDes}}</view>
					</view>
					<view class="hbc mw">
						<text class="f32pc2">住宿类型：</text>
						<text class="f32 main-color">{{dorm.dormitoryType}}</text>
					</view>
					<view class="vlc">
						<text class="f32pc2">宿舍地址： </text>
						<text class="f30pc4 text2">{{dorm.outAddress}}</text>
					</view>
				</view>
			</view>
			<yt-success v-if="!status" icon="static/img/mine/wait.png"  @click="cancel" btnText="返回" successText="已提交申请,等待审批"></yt-success>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import success from '@/components/Success.vue';
import {employee} from '@/api/api-mine.js' // 请求接口的参数
export default {
	components: {
		'yt-success': success
	},
	data() {
		return {
			status: false,
			dorm: {}
		};
	},
	onLoad(e) {
		if (e) {
			if (e.status == 'true') {
				this.status = true
				this.getMyDormInfo()
			}
			return
		}
		const status = uni.getStorageSync('hasOutRoomApplay')
		if (status) {
			this.status = true
			this.getMyDormInfo()
		}
	},
	methods: {
		async getMyDormInfo () {
			try{
				const res = await employee.getOutRoomDetail()
				this.dorm = res.data.data
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		cancel () {
			uni.setStorage({
				key: 'isRefresh',
				data: true
			})
			uni.setStorage({
				key: 'hasOutRoomApplay',
				data: true
			})
			uni.switchTab({
				url: '../../tabbar/mine/mine'
			})
			
		},
		toDrom() {
			uni.navigateTo({
				url: './select-dorm-type'
			})
		},
		onOutdetail () {
			uni.navigateTo({
				url:'./outDetail'
			})
		}
	},
};
</script>

<style lang="scss" scoped>
.yt-page {
	.item {
		box-shadow: 0px 5upx 49upx 0px rgba(185, 185, 185, 0.48);
		.image {
			width: 86upx;
			height: 86upx;
			overflow: hidden;
		}
	}
}
</style>

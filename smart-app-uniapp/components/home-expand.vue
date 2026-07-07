<template>
	<view class="expand hbc">
		<view @click="toSalarySeach" class="rel item bg rad10">
			<view class="abs left10 top10 vlc">
				<text class="f26pc bold">工资明细查询</text>
				<text style="color: #f8bebe;" class="f24">每月中旬更新</text>
			</view>
			<view class="abs right0 bottom0 w200 h150">
				<image class="mw mh" src="/static/img/index/home-salary-icon.png" mode=""></image>
			</view>
		</view>
		<view class="rel item right">
			<navigator url="/pages/page/service/social-security/index" hover-class="none" class="item-top bg rel rad10">
				<view class="abs left10 top10 vlc">
					<text class="f26pc bold">社保公积金查询</text>
					<text style="color: #f8bebe;" class="f24">便捷查询通道</text>
				</view>
				<view class="abs right0 w130 h100">
					<image class="mw mh" src="/static/img/index/home-salary-icon.png" mode=""></image>
				</view>
			</navigator>
			<view class="item-bottom hbc">
				<navigator url="/pages/page/service/visitor-appointment/my-visitor"  hover-class="none" class="rel unit bg rad10">
					<view class="abs left10 top10 vlc">
						<text class="f26pc bold">访客邀请</text>
						<text style="color: #f8bebe;" class="f24">邀请客户</text>
					</view>
					<view class="abs right0 bottom0 w75 h70">
						<image class="mw mh" src="/static/img/index/home-visit-icon.png" mode=""></image>
					</view>
				</navigator>
				<navigator url="/pages/page/service/new-employees/index" hover-class="none" class="unit rel bg rad10">
					<view class="abs left10 top10 vlc">
						<text class="f26pc bold">员工须知</text>
						<text style="color: #f8bebe;" class="f24">园区制度</text>
					</view>
					<view class="abs right0 bottom0 w60 h60">
						<image class="mw mh" src="/static/img/index/home-must-know.png" mode=""></image>
					</view>
				</navigator>
			</view>
		</view>
	</view>
</template>

<script>
	// 环境配置：薪资查询外链地址统一来源（见 config/env.js）
	import env from '@/config/env.js'
	export default {
		data() {
			return {

			}
		},
		methods: {
			toSalarySeach() {
				let url = uni.getStorageSync('USER_NAME')
				console.log(url)
				// 薪资查询为 gz 独立系统的页面，工号按对方要求拼在 query 上
				let link = encodeURIComponent(`${env.SALARY_QUERY_URL}?GongHao=${url}`)
				uni.navigateTo({
					url: `../../home/news/web-link?title=工资查询&link=${link}&fuck=1`
				});
			}
		},
	}
</script>

<style lang="scss" scoped>
	.expand {
		width: 100%;
		height: 310upx;
		padding: 30upx;
		.item {
			width: 340upx;
			height: 250upx;
			.item-top {
				width: 100%;
				height: 100upx;
			}
			.item-bottom {
				width: 100%;
				height: 140upx;
				.unit {
					width: 164upx;
					height: 100%;
				}
			}
		}
		.right {
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			align-items: center;
		}
		.bg {
			background-color: #fff3f6;
		}
	}
</style>

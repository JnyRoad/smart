<template>
	<view class="mw bgf">
		<view class="pl30 pr30">
			<view class="hcc mw h200 border-bottom-D">
				<view class="w120 h120">
					<image class="mw mh" src="/static/img/serive/salary-icon.png"></image>
				</view>
			</view>
			<view class="mw hbc h120 border-bottom-D">
				<view class="f32pc w300 ">查询密码</view>
				<view class="f1">
					<input class="mw mh tr" v-model="password" password type="text" placeholder="请输入您的薪资查询密码" />
				</view>
			</view>
		</view>
		<view  class="mw hcc btn">
			<yt-button btnText="查询"></yt-button>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytButton from '@/components/yt-button/index.vue'
	import salary from '@/api/api-salary.js'
	export default {
		components: {
			'yt-button': ytButton
		},
		data() {
			return {
				postList: {
					facePhoto: '', // 人脸的采集查询
					offset: 1, // 查询起始值
					limit: 10 // 查询的条数
				},
				password: '',
				list: [
					{
						salaryId: '1',
						salaryTitle: '2019年3月工资条'
					},
					{
						salaryId: '2',
						salaryTitle: '2019年4月工资条'
					}
				], // 薪资列表
			}
		},
		onLoad() {
			// this.getSalaryList()
		},
		methods: {
			// 获取工资条列表
			async getSalaryList() {
				const res = await salary.list(this.postList)
				this.list = res.data.data.rows + encodeURIComponent(e)
			},
			query () {
				uni.navigateTo({
					url: './list'
				})
			}
		}
	}
</script>

<style scoped>
	.btn {
		height: 400upx;
	}
</style>

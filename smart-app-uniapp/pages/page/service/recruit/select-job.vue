<template>
	<view class="mw bgec">
		<view v-if="routerPage.length>0" class="mw h60 hlc border-bottom-D pl30 pr30">
			<block v-for="(item,index) in routerPage" :key="index">
				<text class="f32pc2">{{item}}</text>
				<text v-if="routerPage.length>1">》</text>
			</block>
		</view>
		<view class="mw">
			<block v-for="(item,index) in job[current]" :key="index">
				<view @click="getRouter(item.title)" class="mw hbc h90 pl30 pr30 border-bottom-D bgf">
					<text class="f32pc2">{{item.title}}</text>
					<view class="hbc">
						<text>{{item.jobNum}}</text>
						<view class="ml10 w20 h30 hcc">
							<image src="/static/img/mine/next.png" mode=""></image>
						</view>
					</view>
				</view>
			</block>
		</view>
	</view>
</template>

<script>
	import {storage} from '@/tools/storage.js'
	export default {
		data() {
			return {
				routerPage: [],
				current: 0,
				job: [
					[{
						id: 1,
						title:'东莞包装科技',
						jobNum: 3
					},
					{
						id: 2,
						title:'东莞IT',
						jobNum: 1
					},{
						id: 3,
						title:'东莞商标模切',
						jobNum: 2
					}],
					[{
						id: 1,
						title:'人力资源课',
						jobNum: 4
					},
					{
						id: 2,
						title:'品质部',
						jobNum: 1
					},{
						id: 3,
						title:'实验课',
						jobNum: 6
					},
					{
						id: 4,
						title:'综合服务中心',
						jobNum: 2
					},{
						id: 5,
						title:'安全课',
						jobNum: 6
					}],
					[
						{
							id: 1,
							title:'薪资核算员',
							jobNum: 6
						},
						{
							id: 2,
							title:'员工',
							jobNum: 6
						},
						{
							id: 3,
							title:'招聘专员',
							jobNum: 6
						},
						{
							id: 4,
							title:'人资科长',
							jobNum: 6
						},
						{
							id: 5,
							title:'人事专员',
							jobNum: 6
						},
					]
				]
			}
		},
		onLoad() {
			
		},
		methods: {
			getRouter(title) {
				console.log(this.job.length);
				if (this.current>=this.job.length-1) {
					uni.setStorage({
						key: 'selectJob',
						data: title
					})
					uni.navigateBack({
						delta: 1
					})
				}
				this.current++
				this.routerPage.push(title)
			}
		},
	}
</script>

<style>
</style>

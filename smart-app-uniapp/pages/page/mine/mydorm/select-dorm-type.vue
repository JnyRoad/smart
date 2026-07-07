<template>
	<view class="yt-page">
		<view class="pt40 pl30 pr30">
			<view class="item hbc bgf rad10 pt20 pb20 pl20 pr20 mb20" @click="getOuting">
				<view class="image mr60">
					<image src="/static/img/mine/out-dorm.png"></image>
				</view>
				<view class="vlc f1">
					<text class="f32pc bold">申请外宿</text>
					<text class="f28pc main-color">在厂区外租住,享受公司补贴</text>
				</view>
				<uni-icon color="#999" size="30" type="arrowright" ></uni-icon>
			</view>
			<view class="item hbc bgf rad10 pt20 pb20 pl20 pr20" @click="getInside">
				<view class="image mr60">
					<image src="/static/img/mine/in-dorm.png"></image>
				</view>
				<view class="vlc f1">
					<text class="f32pc bold">申请内宿</text>
					<text class="f28pc main-color">在厂区内宿舍入住</text>
				</view>
				<uni-icon color="#999" size="30" type="arrowright" ></uni-icon>
			</view>
		</view>
	</view>
</template>

<script>
	import uniicon from '@/components/uni-icon/uni-icon.vue';
	import {HASGUIDED, storage} from '@/tools/storage.js'
	import { mapState, mapMutations } from 'vuex';
	export default {
		components: {
			'uni-icon': uniicon
		},
		computed: {
			...mapState(['dormStatus'])
		},
		onLoad() {
			storage.set(HASGUIDED, true)
		},
		methods: {
			getOuting() {
				if (this.dormStatus == 1) {
					this.$ytHint.toast({
						title : '已申请了内宿补贴不能再申请外宿'
					})
					return
				}
				if (this.dormStatus == 2) {
					uni.navigateTo({
						url:'/pages/page/mine/mydorm/out-dorm-success?status=true',
					})
				} else {
					uni.navigateTo({
						url:'./apply-out-dorm'
					})
				}
			},
			getInside () {
				if (this.dormStatus == 2) {
					this.$ytHint.toast({
						title : '已申请了外宿补贴不能再申请内宿'
					})
					return
				}
				if (this.dormStatus == 1) {
					uni.navigateTo({
						url:'/pages/page/mine/mydorm/index'
					})
				} else {
					uni.navigateTo({
						url:'./apply-in-dorm'
					})
				}
			}
		},
	}
</script>

<style lang="scss" scoped>
	.yt-page {
		.item {
			.image {
				width: 86upx;
				height: 86upx;
				overflow: hidden;
			}
		}
	}
</style>

<template>
	<view class="mw">
		<yt-deliver-item @remove="removeCar" @click="applyAuth" v-if="hasCar" :car="car"></yt-deliver-item>
		<view v-if="!hasCar" class="abs mw mh left-top-50 vcc bgf">
			<view class="vcc mb80">
				<view class="w180 h150 mb40">
					<image src="/static/img/mine/car-wait.png" mode=""></image>
				</view>
				<text class="f28pc4">暂无车辆</text>
				<text class="f32pc mt80">请在右上角去添加</text>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytdeliveritem from '@/components/yt-deliver-item.vue'
	import { mapState } from 'vuex'
	import {vehicle} from '@/api/api-mine.js' // 请求接口的参数
	export default {
		components: {
			'yt-list': ytlist,
			'yt-deliver-item': ytdeliveritem
		},
		data () {
			return {
				car: [],
				page: {
					current: 1,
					size: 10
				},
				hasMore: true,
				hasCar: true
			}
		},
		onBackPress() {
			uni.switchTab({
				url: '/pages/page/tabbar/mine/mine'
			})
			return true
		},
		onPullDownRefresh() {
			this.hasMore = true
			this.page.current = 1
			this.getMyCar()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多了'
				})
				return 
			}
			this.page.current++
			this.getMyCar()
		},
		onNavigationBarButtonTap(e) {
			uni.navigateTo({
				url: './addcar'
			})
		},
		onLoad () {
			this.getMyCar()
		},
		methods: {
			async getMyCar () {
				try{
					const res = await vehicle.baseInfo(this.page)
					uni.stopPullDownRefresh()
					if (this.page.current === 1) {
						if (!res.data.data.records.length) {
							this.hasCar = false
							return
						}
						this.car = res.data.data.records
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
							return
						}
						this.car.push(...res.data.data.records)
					}
				}catch(e){
					//TODO handle the exception
				}
			},
			applyAuth (e) {
				uni.navigateTo({
					url: `./authlist?plateNumber=${e}`
				})
			},
			removeCar(e) {
				const obj = {
					plateNumber: e
				}
				this.$ytHint.modal({
					content: '确定要移出该车辆吗?',
					confirm: ()=>{
						vehicle.deleteCar(obj).then(res => {
							this.$ytHint.toast({
								title: '移除成功!'
							})
							this.getMyCar()
						})
					}
				})
			}
		}
	}
</script>
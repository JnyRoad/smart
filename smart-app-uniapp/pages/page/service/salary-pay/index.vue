<template>
	<view class="mw bgec">
		<block v-for="(item, index) in salaryList" :key="index">
			<navigator :url="'./detail?salaryId='+item.salaryId" hover-class="none" style="box-sizing: border-box;" class="mw h120 pl30 pr30 bgf border-bottom-D hbc">
				<view class="w40 h40 mr30">
					<image class="mw mh" src="/static/img/serive/renminbi.png"></image>
				</view>
				<text class="f32pc2 f1 hlc">{item.salaryTitle}</text>
				<view class="hrc">
					<text class="err-color f32">未签收</text>
					<view class="ml10 w20 h30 hcc">
						<image src="/static/img/mine/next.png" mode=""></image>
					</view>
				</view>
			</navigator>
		</block>
		<yt-null v-if="isNull" notice="暂时没有离职办理记录"></yt-null>
		<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="200" :maxWidth="1000" :ql="0.8" type="base64" :sourceType="['camera']"></cpimg>
		<yt-loading />
	</view>
</template>

<script>
	import salary from '@/api/api-salary.js'
	import cpimg from '@/components/cpimg.vue'
	export default {
		components: {
			cpimg
		},
		data() {
			return {
				page: {
					current: 1,
					size: 20
				},
				salaryList: [],
				facePhoto: '',
				hasMore: true,
				isNull: false
			}
		},
		onLoad() {
			this.photo()
		},
		onPullDownRefresh() {
			this.hasMore = true
			this.isNull = false
			this.page.current = 1
			this.getSalaryList()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据了'
				})
				return
			}
			this.page.current++
			this.getSalaryList()
		},
		methods: {
			async getSalaryList() {
				const obj = {
					facePhoto: this.facePhoto
				}
				try{
					const res = await salary.list(this.page, obj)
					uni.stopPullDownRefresh()
					if (!res) {
						this.isNull = true
						this.$loading(false)
						return
					} else {
						if (this.page.current === 1) {
							if (!res.data.data.records.length) {
								this.isNull = true
								return
							}
							this.salaryList = res.data.data.records
						} else {
							if (!res.data.data.records.length) {
								this.hasMore = false
							} else {
								this.salaryList.push(...res.data.data.records)
							}
						}
					}
					
				}catch(e){
					//TODO handle the exception
				}
			},
			photo () {
				this.list = []
				this.$refs.cpimg._changImg()
			},
			cpimgOk(file) {
				if (file) {
					uni.hideLoading()
					this.facePhoto = file.split(',')[1]
					this.getSalaryList()
				} else {
					uni.hideLoading()
					this.$ytHint.toast({
						title: '处理失败，请重试'
					})
				}
			},
			cpimgErr(e) {
				if (e) {
					uni.hideLoading()
					this.$ytHint.toast({
						title: '处理失败,请重试'
					})
					console.log(e);
					throw e
				}
			}
		}
	}
</script>

<style>
</style>

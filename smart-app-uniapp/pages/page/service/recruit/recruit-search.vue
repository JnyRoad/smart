<template>
	<view class="bcf mw">
		<view class="yt-title bgf" :style="{ height: titleHeight + 'px' }">
			<view :class="['title overflow', isIOS ? 'ios' : 'android']">
				<view class="hac mw mh">
					<image @click="back" class="arrowback w20 h30" src="/static/img/mine/next.png" mode=""></image>
					<view class="searchbar rel overflow h70 f1 mr30 ml30 bgec rad10 pr80 pl80">
						<input v-model="searchTxt" @confirm="search" class="mw h70 f30pc2" confirm-type="search" type="text" placeholder="请输入姓名或手机号" />
						<image @click="close" class="w40 h40 abs right10 top-50" src="/static/img/serive/X.png" mode=""></image>
					</view>
					<image @click="search" class="w40 h40" src="/static/img/serive/recruit/search.png" mode=""></image>
				</view>
			</view>
		</view>
		<view class="bcf mt40" :style="{ paddingTop: titleHeight + 'px' }">
			<manageItem @click="toDetail" v-if="list.length > 0" :list="list"></manageItem>
			<view v-if="isNull" class="abs top-50 left0 right0 vcc">
				<image class="w230 h200 mb30" src="/static/img/serive/recruit/null.png" mode=""></image>
				<view class="vcc">很遗憾，没找到符合条件的简历</view>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>
<script>
	import uniNavBar from '@/components/uni-nav-bar/uni-nav-bar.vue'
	import uniIcon from '@/components/uni-icon/uni-icon.vue'
	import manageItem from '@/components/manage-item.vue';
	import ytLoading from '@/components/yt-loading/index.vue';
	import recruit  from '@/api/api-recruitment-management.js';
	import myMixin from '@/mixins/index.vue'
	import { recruitCtrl, recruitList } from '@/testjs/recruit/recruit.js';
	export default {
		mixins: [myMixin],
		components: {
			uniNavBar,
			uniIcon,
			manageItem,
			ytLoading
		},
		data() {
			return {
				isIOS: true,
				titleHeight: 60,//设置title高度
				list: [],
				searchTxt: '',
				name: '',
				mobile: '',
				page: {
					current: 1,
					size: 10
				},
				isNull: false,
				hasMore: true,
				applyStatus: [] // 简历状态
			}
		},
		onLoad () {
			// #ifdef APP-PLUS || MP-WEIXIN
			this.getTitleHeight()
			// #endif
			this.getTabarList()
		},
		onPullDownRefresh() {
			this.isNull = false
			this.list = []
			this.hasMore = true
			this.page.current = 1
			this.getApplications()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据了'
				})
				return
			}
			this.page.current++
			this.getApplications()
		},
		methods: {
			// 获取头部状态筛选列表
			async getTabarList() {
				try {
					const res = await recruit.optoTypeList();
					if (!res) return
					this.applyStatus = res.data.data;
				} catch (e) {
					throw e;
				}
			},
			back() {
				uni.navigateBack({
					delta: 1
				})
			},
			search() {
				this.list = []
				this.isNull = false
				this.page.current = 1
				this.hasMore = true
				let name = ''
				let mobile = ''
				if (!parseInt(this.searchTxt)) {
					this.name = this.searchTxt
				} else {
					this.mobile = this.searchTxt
				}
				this.getApplications()
			},
			async getApplications () {
				const obj = {
					"jobId": "", 
				    "applyState": '',
					"ageStart": 18,
					"ageEnd": 50,
					"gender": '',
					"ageOrder": '',
					"deliverOrder": '1',
					"applicantName": this.name,
					"applicantMobile": this.mobile
				}
				try{
					const res = await recruit.applicationList(this.page, obj)
					if (!res) return
					if (this.page.current === 1) {
						uni.stopPullDownRefresh()
						if (!res.data.data.records.length) {
							this.isNull = true
							return
						}
						this.list = res.data.data.records
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
							return
						}
						this.list.push(...res.data.data.records)
					}
					this.list.forEach(el => {
						this.applyStatus.forEach(el1 => {
							if (el.status == el1.applyState) {
								el.applyStateDesc = el1.applyStateDesc
							}
						})
					})
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			close(){
				this.searchTxt = ''
			},
			toDetail(e) {
				uni.redirectTo({
					url: `./recruit-detail?applicationId=${e.applicationId}&applyState=${e.status}`
				})
			}
		}
	}
</script>

<style lang="scss">
.yt-title {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	z-index: 999;
	box-sizing: border-box;
	padding: 14upx 0;
	.title {
		position: absolute;
		left: 30upx;
		right: 30upx;
		bottom: 0;
		text-align: center;
		color: #fff;
		font-size: 32upx;
		z-index: 999;
		/* #ifdef APP-PLUS || H5 */
		font-weight: bold;
		/* #endif */
		&.ios {
			height: 88upx;
		}
		&.android {
			height: 96upx;
		}
	}
	.arrowback {
		transform: rotateZ(-180deg);
	}
	.yt-bg {
		position: absolute;
		bottom: -22upx;
		left: 50%;
		transform: translateX(-50%);
		width: 1000upx;
		height: 1000upx;
		background-color: $main-color;
		border-radius: 24%;
		z-index: -1;
	}
}
.input-view {
	width: 92%;
	display: flex;
	background-color: #e7e7e7;
	height: 30px;
	border-radius: 15px;
	padding: 0 4%;
	flex-wrap: nowrap;
	margin: 7px 0;
	line-height: 30px;
	/* #ifdef APP-PLUS */
	&.ios {
		height: 88upx;
	}
	&.android {
		height: 96upx;
	}
	/* #endif */
}

.input-view .uni-icon {
	line-height: 30px !important;
}

.input-view .input {
	height: 30px;
	line-height: 30px;
	width: 94%;
	padding: 0 3%;
}
.uni-navbar--shadow {
	box-shadow: 0;
}
</style>

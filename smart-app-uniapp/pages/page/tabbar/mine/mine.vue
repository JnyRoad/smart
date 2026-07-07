<template>
	<view class="page bgf">
		<view class="yt-content rel">
			<view class="bg-cir"></view>
			<view class="mine-info hlc rel">
				<view @click="updateAvatar" class="wh152 ml24 mr30 overflow">
					<image  class="mw mh" :src="baseInfo.employeePhoto||'/static/img/mine/person-icon.png'"></image>
				</view>
				<view v-if="baseInfo.employeeName" class="f1 mr120">
					<view class="f32pc bold">{{ baseInfo.employeeName }}</view>
					<view class="f26pc4 pt10 pb30">
						<view>{{ baseInfo.buName }} {{ baseInfo.deptName }}</view>
						<view>{{ fullInfo.jobName }} {{ fullInfo.empTypeDes }}</view>
						<view>
							入职日期: {{fullInfo.entryDate}}
						</view>
						<view v-if="!baseInfo.employeePhoto" @click="updateAvatar(2)" class="f26 main-color">完善信息</view>
						<view v-if="baseInfo.employeePhoto" @click="updateAvatar(1)" class="f26 main-color">修改头像</view>
					</view>
					<navigator url="../../mine/personnel-records/index" hover-class="none" class="mine-setting w80 h80">
						<image src="/static/img/mine/editor.png"></image>
					</navigator>
				</view>
			</view>
			<navigator url="../../home/address-list/address-list" hover-class="none" class="hcc f38pc bold pt30 pb30">
				{{address}}
				<image class="exchange" src="/static/img/mine/tab.png"></image>
			</navigator>
			<yt-list :ytClass="['pl30 pr30']">
				<yt-list-item navUrl="../../mine/ercode/index" title="园区通行码" image="/static/img/mine/ercode.png"></yt-list-item>
				<yt-list-item @click="toMyDorm" title="我的宿舍" image="/static/img/mine/dorm.png">
					<view class="f30pc4">
						{{dormDesc}}
					</view>
				</yt-list-item>
				<yt-list-item navUrl="../../mine/mycar/index" hideLastBorder="true" title="我的车辆" image="/static/img/mine/cart.png" >
					<view class="f30pc4">
						{{vehicleDesc}}
					</view>
				</yt-list-item>
			</yt-list>
			<view class="yt-help-setting">
				<yt-list :ytClass="['pl30 pr30']">
					<yt-list-item navUrl="../../mine/help/help-center" title="帮助中心" image="/static/img/mine/helpcenter.png"></yt-list-item>
					<yt-list-item navUrl="../../mine/shareit/shareit" hideLastBorder="true" title="我的分享" image="/static/img/mine/fenxian.png"></yt-list-item>
					<yt-list-item navUrl="../../mine/settings/index" hideLastBorder="true" title="设置" image="/static/img/mine/settings.png"></yt-list-item>
					
				</yt-list>
			</view>
		</view>
		<!-- <guide-mine v-if="!hasGiuded" @click="toApplyDrom" /> -->
		<yt-loading />
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue';
import ytlistitem from '@/components/yt-list-item/index.vue';
import guideMine from '@/components/guide-mine.vue';
import myMixin from '@/mixins/index.vue';
import exitApp from '@/mixins/exitApp.vue';
import { HASGUIDED,storage } from '@/tools/storage.js';
import { employee } from '@/api/api-mine.js'; // 请求接口的参数
import { mapState, mapMutations } from 'vuex';
export default {
	mixins: [exitApp, myMixin],
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'guide-mine': guideMine
	},

	data() {
		return {
			baseInfo: {},
			fullInfo: {},
			hasGiuded: true, // 是否完成引导操作
			address: '', // 定位地址
			parkId: '', // 当前的定位园区
			hasComplete: '' // 是否完成信息完善
		};
	},
	onLoad() {
		this.parkId = storage.getSync(storage.PARK_ID);
		this.getEmployeeInfo();
	},
	onShow() {
		this.hasGiuded = storage.getSync(HASGUIDED) || false
		this.hasComplete =  storage.getSync('isLoginFirst') // 信息完善会更新为false
		const isRefresh = uni.getStorageSync('isRefresh');  // 申请完宿舍或者车辆 跳转过来刷新
		this.address = this.$store.state.address
		if (isRefresh) {
			uni.removeStorageSync('isRefresh'); // 刷新后删除本地数据
			this.getEmployeeInfo();
			return
		}
		const avatarRefresh = storage.getSync('avatarRefresh') || false
		if (avatarRefresh) {
			uni.removeStorageSync('avatarRefresh'); // 刷新后删除本地数据
			this.getEmployeeInfo();
			return
		}
		const parkId = storage.getSync(storage.PARK_ID);
		if (parkId != this.parkId) {
			this.parkId = parkId
			this.getEmployeeInfo()
		}
		
	},
	computed: {
		...mapState(['dormStatus', 'dormDesc', 'vehicleStatus', 'vehicleDesc'])
	},
	methods: {
		...mapMutations(['WatchDorm', 'WatchVehicle']),
		async getEmployeefullInfo () {
			const res = await employee.fullInfo()
			storage.set('jobLevelflag',res.data.data.jobLevelflag) // 存储员工级别
			this.fullInfo = res.data.data
			this.fullInfo.entryDate = this.fullInfo.entryDate.slice(0,10)
		},
		async getEmployeeInfo() {
			try {
				const res = await employee.baseInfo();
				if (!res) return
				this.getEmployeefullInfo()
				this.baseInfo = res.data.data
				storage.set(storage.EMPLOYEE_NAME,this.baseInfo.employeeName) // 存储身份证
				storage.set('identification',this.baseInfo.employeeCardNo) // 存储员工名
				storage.set(storage.USER_TEL, this.baseInfo.mobile) // 存储员工电话
				storage.set(storage.USER_TYPE, this.baseInfo.statusDes) // 存储员工类型
				storage.set(storage.USER_STATUS, this.baseInfo.status) // 存储员工状态
				this.WatchDorm({
					dormStatus: this.baseInfo.dormitoryState,
					dormDesc: this.baseInfo.dormitoryStateDesc
				});
				this.WatchVehicle({
					vehicleStatus: this.baseInfo.vehicleState,
					vehicleDesc: this.baseInfo.vehicleStateDesc
				});
			} catch (e) {
				console.log(e);
				throw e;
			}
		},
		toMyDorm() {
			let url = '../../mine/mydorm/select-dorm-type'
			// if (this.dormStatus == '0') {
			// 	url =  `../../mine/mydorm/select-dorm-type`
			// } else if (this.dormStatus == '1') {
			// 	url =  `../../mine/mydorm/index`
			// } else if (this.dormStatus == '2') {
			// 	url = '../../mine/mydorm/out-dorm-success?status=true'
			// }
			uni.navigateTo({
				url: url
			})
		},
		toApplyDrom() {
			this.hasGiuded = true
			uni.navigateTo({
				url: '../../mine/mydorm/select-dorm-type'
			})
		},
		// 修改上传头像
		updateAvatar(type){
			if (type === 2) {
				this.$ytHint.modal({
					content:"确定完善照片?",
					confirm : () => {
						uni.navigateTo({
							url: '../../logins/Information/Information'
						})
					}
				})
			} else if (type === 1) {
				uni.navigateTo({
					url: '../../logins/face/face?type=4' // 头像修改
				})
			}
		}
	}
};
</script>

<style lang="scss" scoped>
.page {
	width: 100%;
}
.bg-cir {
	position: absolute;
	top: -90upx;
	left: 0;
	width: 100%;
	height: 160upx;
	background-color: $main-color;
	z-index: 0;
}

.wh152 {
	width: 152upx;
	height: 152upx;
	border-radius: 50%;
}
.mine-info {
	width: 690upx;
	height: 327upx;
	margin: 0 auto;
	background: rgba(255, 255, 255, 1);
	box-shadow: 0px 5upx 49upx 0px rgba(185, 185, 185, 0.48);
	border-radius: 20upx;
}

.mine-setting {
	position: absolute;
	top: 60upx;
	right: 40upx;
}

.progress-box {
	border-radius: 5upx;
	overflow: hidden;
}

.exchange {
	width: 34upx;
	height: 30upx;
	margin-left: 20upx;
}

.yt-help-setting {
	width: 690upx;
	margin: 0 auto;
	border-radius: 20upx;
	margin-top: 30upx;
	overflow: hidden;
	box-shadow: 0px 5upx 40upx 0px rgba(185, 185, 185, 0.18), 0px -5upx 40upx 0px rgba(185, 185, 185, 0.18), 5upx 0upx 40upx 0px rgba(185, 185, 185, 0.18),
		-5upx 0upx 40upx 0px rgba(185, 185, 185, 0.18);
}
.mask {
	background: rgba(0, 0, 0, 0.5);
}
</style>

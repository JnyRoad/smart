<template>
	<view class="mw bgf">
		<view class="mt30 mb30 hlc">
			<view style="width: 10upx;height: 50upx;background-color: #007AFF;margin-right: 30upx;">
				
			</view>
			<text style="font-size: 40upx;font-weight: 300;">上传证件</text>
		</view>
		<view v-if="true" class="pl30 pr30">
			<view class="f32pc2">1.上下班可刷脸进入园区</view>
			<view class="f32pc2">2.以后考勤露脸就完成打卡</view>
			<view v-if="name" @click="openCamera" class="idcard vc rel  mt30" style="height: 350upx;width: 90%;margin: 0 auto;margin-top: 30upx;">
				<image class="mw mh abs" :src="face" ></image>
				<view class="mw mh abs pl40 pr40 hcc" style="background-color: rgba(0,0,0,.3);">
					<view style="height: 150upx;width: 150upx;border: 1px solid #FFFFFF;border-radius: 50%;background-color: rgba(0,0,0,.5);" class="hcc">
						<image src="/static/img/serive/recruit/xiangji.png" style="height: 60upx;width: 70upx;"></image>
					</view>
				</view>
				<!-- <view class="hlc  mt40 rel">
					<image class="w200 h200" mode="aspectFill" :src="face"></image>
					<view class="pl30">
						<view class="f30pc4">姓名</view>
						<view class="f32pc bold">{{name}}</view>
						<view class="f30pc4 mt10">身份证号</view>
						<view class="f32pc bold">{{identification}}</view>
					</view>
				</view> -->
				<!-- <text class="abs right100 bottom15 f32pc4">重新上传</text> -->
			</view>
			<view v-if="!name" class="idcard vc rel pl58 pr58">
				<image class="mw mh" @click="openCamera" src="/static/img/serive/recruit/sfz.png" mode=""></image>
			</view>
			<view class="f32pc2 center">
				完善照片时,会进行实名认证,以确保是本人
			</view>
			<view class="mb20 mt20 hlc" v-if="name">
				<text class="mr10" style="color: #999999;">姓&nbsp;&nbsp;&nbsp;名:</text>
				<input type="text" v-model="names" class="pl10"/>
			</view>
			<view class="mb20 mt20 hlc" v-if="name">
				<text class="mr10" style="color: #999999;">证件号:</text>
				<!-- <text class="tc9">{{identification}}</text> -->
				<input type="text" v-model="identification" class="pl10"/>
			</view>
			<view class="hcc mw mt80 mb80" >
				<yt-button v-if="!name" @click="next" btnText="下一步" btnBgColor="#ececec" btnWidth="180" btnHeight="54"></yt-button>
				<yt-button v-if="name" @click="next" btnText="下一步" btnWidth="180" btnHeight="54"></yt-button>
			</view>
			<view class="hcc mw mt40 mb120" >
				<yt-button btnText="忽略" @click="ignore" btnWidth="180" btnHeight="54"></yt-button>
			</view>
			<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="200" :maxWidth="1000" :ql="0.8" type="base64"
			 :sourceType="['camera','album']"></cpimg>
		</view>
		<yt-loading></yt-loading>
		<view v-if="false" class="vc rel bgf rad10 notice">
			<!-- <image class="mh" src="/static/img/serive/recruit/idcard.png"></image> -->
			<view class="mw h90 f32pc2 hlc">
				<view class="w10 h60 main-bg mr30"></view>
				<view class="tip-fs">员工设置照片的好处</view>
			</view>
			<view class="mw vlc pl30 pr30 pt40">
				<text class="f30pc2">1.上下班可刷脸进出园区</text>
				<text class="f30pc2">2.每天考勤只需要露脸就可以完成打卡</text>
				<text class="mt40 f32pc4">(如果忽略将影响您的智慧出行)</text>
			</view>
			<view class="mw mt80 vc">
				<text class="f32pc4 mb20">拍照时，请正对手机</text>
				<image class="guide-img" src="/static/img/login/face-guide.png" mode=""></image>
				<view class="btn-group vcc mw">
					<view @click="toFaceImg" class="update hcc main-bg cf">上传</view>
					<view @click="ignore" class="ignore hcc main-color">忽略</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	// 注释代码暂时保留  后期可能会用到
	import ytButton from '@/components/yt-button/index.vue'
	import cpimg from '@/components/cpimg.vue'
	import information from '@/api/api-complete-information'
	import { storage } from '@/tools/storage.js'
	import { employee } from '@/api/api-mine.js'; // 请求接口的参数
	export default {
		components: {
			'yt-button': ytButton,
			'cpimg': cpimg
		},
		data() {
			return {
				idcardScan: '',
				identificationPhoto: '',
				identification: '',
				name: '',
				hasMessage: false,
				perfectId: 0,
				face: '',
				names: '',
				baseInfo : {}
			};
		},
		onLoad() {
			this.getEmployeeInfo();
		},
		methods: {
			async getEmployeeInfo() {
				try {
					const res = await employee.baseInfo();
					if (!res) return
					this.baseInfo = res.data.data
					storage.set(storage.EMPLOYEE_NAME,this.baseInfo.employeeName) // 存储员工名
					storage.set('identification',this.baseInfo.employeeCardNo) // 存储身份证
				} catch (e) {
					console.log(e);
					throw e;
				}
			},
			// 下一步
			next() {
				if (!this.hasMessage) {
					this.$ytHint.toast({
						title: '请上传身份证正面'
					})
					return
				}
				let name = storage.getSync(storage.EMPLOYEE_NAME)
				let identification = storage.getSync('identification')
				if (name != this.names || identification != this.identification) {
					this.$ytHint.toast({
						title: '照片身份证信息与账号身份证信息不一致，请重新上传身份证'
					})
					return
				}
				// this.$ytHint.modal({
				// 	content: '请检查自己的信息是否正确，如果有误，请重新进行身份证识别',
				// 	cancelText: '重新拍',
				// 	confirmText: '信息正确',
				// 	confirm: () => {
				// 		uni.redirectTo({
				// 			url: `../face/face?perfectId=${this.perfectId}&type=2`
				// 		})
				// 	},
				// 	cancel: () => {
				// 		this.openCamera()
				// 	}
				// })
				uni.redirectTo({
					url: `../face/face?perfectId=${this.perfectId}&type=2`
				})
			},
			// 身份证识别
			async idOcr() {
				try {
					const res = await information.identification({
						identificationPhoto: this.identificationPhoto
					})
					if (!res) return
					this.hasMessage = true
					this.perfectId = res.data.data.perfectId
					this.name = res.data.data.name
					this.names = res.data.data.name
					this.identification = res.data.data.identification
					information.getpre({
						identityCard : this.identification,
						name : this.name,
						perfectId : this.perfectId
					}).then((res) => {
						if (!res) return
					})
					this.$ytHint.toast({
						title: '识别成功'
					})
				} catch (e) {
					console.log(e);
					throw e
				}
			},
			openCamera() {
				if (!storage.getSync('renlianshie')) {
					uni.showModal({
						title: '温馨提示',
						content: '是否允许调用摄像头?',
						success: (res) => {
							if (res.confirm) {
								this.list = []
								this.$refs.cpimg._changImg()
								storage.setSync('renlianshie',true)
							}
						}
					})
				} else {
					this.list = []
					this.$refs.cpimg._changImg()
				}
				
			},
			cpimgOk(file) {
				if (file) {
					uni.hideLoading()
					this.hasImage = true
					this.face = file
					this.identificationPhoto = file.split(',')[1]
					this.idOcr()
				} else {
					uni.hideLoading()
					this.hasImage = true
					this.$ytHint.toast({
						title: '处理失败，请重试'
					})
				}

			},
			cpimgErr(e) {
				if (e) {
					uni.hideLoading()
					this.hasImage = true
					this.$ytHint.toast({
						title: '处理失败,请重试'
					})
					console.log(e);
					throw e
				}
			},
			// 忽略
			ignore() {
				uni.switchTab({
					url: '../../tabbar/home/home'
				})
			},
			// 拍照
			toFaceImg() {
				uni.navigateTo({
					url: '../face/face?type=2'
				})
			}
		}
	}
</script>

<style scope lang="scss">
	.guide-img {
		width: 370upx;
		height: 160upx;
	}

	.info-photo {
		background: #E1E0E3;

		img {
			width: 50upx;
		}
	}

	.card {
		background: url('../../../../static/img/serive/recruit/info.png') no-repeat center center;
		background-size: cover;
		// width:690upx;
		height: 400upx;
		box-shadow: 0px 5upx 6upx 0px rgba(204, 204, 204, 0.2);
		border-radius: 20upx;
	}

	.idcard {
		height: 360upx;
	}

	.notice {
		width: 100%;
		padding: 30upx;
	}

	.btn-group {
		>view {
			width: 320upx;
			height: 120upx;
			border-radius: 60upx;
			margin-top: 40upx;
		}

		.ignore {
			border: 2upx solid $main-color;
		}
	}

	.tip-fs {
		font-size: 48upx;
	}
</style>

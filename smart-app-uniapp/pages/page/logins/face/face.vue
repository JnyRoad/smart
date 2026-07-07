<template>
	<view class="yt-page bgf vca">
		<view class="f32pc2">1.上下班可刷脸进入园区</view>
		<view class="f32pc2">2.以后考勤露脸就完成打卡</view>
		<view class="face-window rel">
			<view v-if="noface">
				<image class="facebg abs left0 top0" src="/static/img/login/face-p.png" mode=""></image>
			</view>
			<image v-if="!noface" class="facebg abs left0 top0" :src="faceImg" mode=""></image>
			<image class="facebg abs left0 top0" src="/static/img/login/facebg.png" mode=""></image>
			<view class="line" :class="{topdown:!openAgain}"></view>
		</view>
		<view class="fs32pc2 mw hcc">温馨提示：为确保拍摄效果，请按照下面提示进行拍摄</view>
		<image class="guide-img" src="/static/img/login/face-guide.png" mode=""></image>
		<view class="_cpimg">
			<canvas id="_myCanvas" canvas-id="_myCanvas" :style="{width:cWidth+'px',height:cHeight+'px'}" />
		</view>
		<view class="hcc mw mt80 mb120" @click="openCameraAgain">
			<yt-button v-if="(faceType==1 || faceType == 3 || faceType == 4) && !isVerfying" btnText="人脸识别拍摄" btnWidth="240" btnHeight="54"></yt-button>	
			<yt-button v-if="faceType==2&&!isVerfying" :btnText="isSuccess?'人脸识别拍摄':'重新认证'" btnWidth="240" btnHeight="54"></yt-button>
			<yt-button v-if="isVerfying" btnText="识别中 请稍后..." btnWidth="240" btnHeight="54" btnBgColor="#ccc"></yt-button>
		</view>
	</view>
</template>

<script>
	import ytbutton from '@/components/yt-button/index.vue'
	import loginAccount from '@/api/api-account.js'
	import information from '@/api/api-complete-information'
	import apiPassword from '@/api/api-password.js'
	import {storage} from '@/tools/storage.js'
 	export default {
		components: {
			'yt-button': ytbutton
		},
		data() {
			return {
				back: false,
				faceImg: '',
				noface: true, // 没人脸
				maxWidth: 750,
				minWidth: 0,
				type: 'base64',
				ql: 0.9,
				size: 200,
				faceType: 1 ,// 1 为 人脸识别登录  2 为 人脸身份证比对 3 通过人脸修改 4 修改头像
				perfectId: 0 ,// 身份识别的验证
				cWidth: 0,
				cHeight: 0,
				openAgain: false, // 人脸验证失败 改为true 显示
				isSuccess: true ,// 人脸身份证对比
				isVerfying: false, // 是否处于识别中
				deviceNo: '', // 设备识别码
			};
		},
		onLoad(e) {
			this.faceType = e.type // 1 为 人脸识别登录  2 为 人脸身份证比对 3 通过人脸修改密码 4 修改头像
			if (e.perfectId) this.perfectId = e.perfectId // 身份识别的验证id
			this.deviceNo = plus.device.uuid.split(',')[0] // 设备号
		},
		methods: {
			// 人脸识别登录
			async postFace (img) {
				// 防止第一次拿不到设备号
				if (this.deviceNo == '') {
					this.deviceNo = plus.device.uuid.split(',')[0]
				}
				try{
					const res = await loginAccount.loginFace(img,this.deviceNo)
					this.isVerfying = false
					if (!res) {
						this.openAgain = true
						return
					}
					this.openAgain = true
					this.savaLoginData(res.data)
					this.$ytHint.toast({
						title: '登录成功',
						mask: true
					})
					this.$store.commit('restart',false)
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/page/tabbar/home/home'
						})
					},1500)
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 认证比对 
			async perfectFace (img) {
				// 防止第一次拿不到设备号
				if (this.deviceNo == '') {
					this.deviceNo = plus.device.uuid.split(',')[0]
				}
				const obj = {
					facePhoto: img,
					deviceNo: this.deviceNo
				}
				if (this.faceType == 2) obj.perfectId = this.perfectId
				try{
					let res = {}
					if (this.faceType == 2) {
						res = await information.verifyFace({
							perfectId: this.perfectId,
							facePhoto: img,
							deviceNo: this.deviceNo
						})
					} else if(this.faceType == 4) {
						res = await information.updateAvatar(obj)
					}
					this.isVerfying = false
					if (!res.data.data || !res) {
						this.$ytHint.toast({
							title: '识别失败，请重新拍照'
						})
						this.openAgain = true
						this.isSuccess = false
					} else {
						this.openAgain = true
						this.isSuccess = true
						this.$ytHint.toast({
							title: '人脸验证成功',
							mask: true
						})
						storage.set('isLoginFirst', 'false') // 只要通过了身份验证 就可以走人脸识别找回密码
						storage.set('avatarRefresh', true) // 刷新我的页面
						setTimeout(() => {
							uni.switchTab({
								url: '../../tabbar/home/home'
							})
						},1200)
					}
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 人脸 修改密码
			async faceToPassword (feceImg) {
				// 防止第一次拿不到设备号
				if (this.deviceNo == '') {
					this.deviceNo = plus.device.uuid.split(',')[0]
				}
				try{
					const res = await apiPassword.verifyFaceToPassword(feceImg,this.deviceNo)
					this.isVerfying = false
					if (!res) {
						this.openAgain = true
						return
					}
					this.$ytHint.toast({
						title: '人脸验证成功',
						mask: true
					})
					this.openAgain = true
					setTimeout(() => {
						uni.setStorage({
							key: 'verify',
							data: res.data.data.pwdUpdateAuthCode
						})
						uni.redirectTo({
							url: '../findPwdPhone/setPassword'
						})
					},1500)
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 更新头像
			// 登录成功将信息存进本地
			savaLoginData(data) {
				const token = data.access_token // 登录令牌
				const refToken = data.refresh_token // 刷新令牌
				const userID = data.user_id // 用户id 目前未使用
				const userName = data.username // 员工号
				const tokenTimeout = (new Date()).getTime() + parseInt(`${data.expires_in}000`) - 3600000 // 减去一个小时
				storage.set(storage.USER_TOKEN, token);
				console.log('退出登录');
				storage.set(storage.USER_ID, userID);
				storage.set(storage.USER_NAME, userName);
				storage.set(storage.EXPIRES_IN, tokenTimeout)
				storage.set(storage.REFRESH_TOKEN, refToken)
				this.getSalayTypes(userName)
			},
			getSalayTypes (badge) {
				loginAccount.getSalayType({badge : badge}).then(res => {	//查询员工薪资计算类型
					console.log(res);
					if (res.data.data.salaryTypeName) {
						storage.set(storage.SALARYTYPENAME, res.data.data.salaryTypeName);
					}
				}).catch((e) => {
					console.log(e);
					throw e
				})
			},
			// 
			openCameraAgain () {
				if (!storage.getSync('shoucishexiang')) {
					uni.showModal({
						title: '温馨提示',
						content: '是否允许调用摄像头?',
						success: (res) => {
							if (res.confirm) {
								if (this.isVerfying) return
								this.openAgain = false
								storage.setSync('shoucishexiang',true)
								this.isVerfying = true
								this.openCamera()
							}
						}
					})
				} else {
					if (this.isVerfying) return
					this.openAgain = false
					this.isVerfying = true
					this.openCamera()
				}
			},
			// #ifdef APP-PLUS
			openCamera() {
				const that = this
				const cmr = plus.camera.getCamera(2);
				const res = cmr.supportedImageResolutions[0];
				const fmt = cmr.supportedImageFormats[0];
				cmr.captureImage(function(path) {
						that._cpImg(path)
					},
					function(error) {
						that.isVerfying = false
						console.log("Capture image failed: " + error.message);
					}, {
						resolution: res,
						format: fmt
					}
				);
			},
			// 以下代码如需修改请参考 uni-app官网插件市场
			_cpImg(resPath) {
				let that = this
				let imgFile = resPath
				let sysInfo = uni.getSystemInfoSync()
				// 获取图片信息，
				uni.getImageInfo({
					src: imgFile,
					success: function (image) {
						// #ifdef APP-PLUS || MP-WEIXIN
						// 如果图片的大小大于设定值才压缩
						uni.getFileInfo({
							filePath: imgFile,
							success: function (res) {
								_img(image, res.size)
							},
							fail: function (e) {
								console.log(e);
							}
						})
						// #endif
					},
					fail: function (e) {
						console.log(e);
					}
				});
				// 处理图片
				function _img(image, size) {
					let oW = image.width, oH = image.height, scaleWidth = 1, scaleHeight = 1;
					if (size / 1024 >= that.size || image.width >= that.maxWidth) {
						// 控制上传图片的宽高
						if (image.width >= image.height && image.width >= that.maxWidth) {
							image.height = that.maxWidth * image.height / image.width;
							image.width = that.maxWidth;
						} else if (image.width < image.height && image.height >= that.maxWidth) {
							image.width = that.maxWidth * image.width / image.height;
							image.height = that.maxWidth;
						}
						scaleWidth = image.width / oW
						scaleHeight = image.height / oH
					}
					const ctx = uni.createCanvasContext('_myCanvas', that);
					that.cWidth = image.width;
					that.cHeight = image.height;
					ctx.drawImage(imgFile, 0, 0, oW * scaleWidth, oH * scaleHeight);
					ctx.draw(false, () => {
						let time = 0;
						// #ifdef MP-WEIXIN
						time = 0;
						// #endif
						// #ifdef MP-WEIXIN
						time = 500;
						// #endif
						setTimeout(() => {
							uni.canvasToTempFilePath({
								width: Number(that.cWidth),
								height: Number(that.cHeight),
								destWidth: Number(that.cWidth),
								destHeight: Number(that.cHeight),
								canvasId: '_myCanvas',
								fileType: 'jpg',
								quality: Number(that.ql),
								success: function (res) {
									if (that.type == 'base64') {
										let img = '';
										// let platform = uni.getSystemInfoSync().platform
										// if (platform == 'ios') {
										// 	// 兼容处理：ios获取的图片上下颠倒
										// 	img = that._reverseImgData(res)
										// }
										//#ifdef MP-WEIXIN
										img = 'data:image/jpeg;base64,' + wx.getFileSystemManager().readFileSync(res.tempFilePath, "base64")
										//#endif
										//#ifdef APP-PLUS
										// console.log(JSON.stringify(res))
										plus.io.resolveLocalFileSystemURL(res.tempFilePath, function (entry) {
											entry.file(function (file) {
												let fileReader = new plus.io.FileReader();
												// console.log("getFile:" + JSON.stringify(file));
												fileReader.readAsDataURL(file);
												fileReader.onloadend = function (evt) {
													if (evt.target.readyState == 2) {
														that.faceImg = evt.target.result
														that.noface = false
														if (that.faceType == 1) {
															that.postFace(evt.target.result.split(',')[1])
														} else if (that.faceType == 2 || that.faceType == 4) {
															that.perfectFace(evt.target.result.split(',')[1])
														} else if (that.faceType == 3) {
															that.faceToPassword(evt.target.result.split(',')[1])
														}
														
													} else {
														console.log(evt);
													}
												}
											});
										}, function (e) {
											console.log(e);
										});
										//#endif
									} else {
										console.log(res.tempFilePath);
									}
								},
								fail: function (e) {
									console.log(e);
								}
							}, that)
						}, time);
					});
				}
			},
			// #endif
		}
	};
</script>

<style lang="scss" scoped>
	.face-window {
		width: 400upx;
		height: 400upx;
		overflow: hidden;
	}
	.facebg {
		width: 100%;
		height: 100%;
	}
	.guide-img {
		width: 370upx;
		height: 160upx;
	}
	.line {
		top: 0;
		left: 0;
		z-index: 998;
		width: 400upx;
		height: 5upx;
		background-color: $main-color;
		transform: translateY(-5upx);
	}
	@keyframes topdown{
		0%{
			transform: translateY(-5upx);
		}
		50%{
			transform: translateY(405upx);
		}
		100% {
			transform: translateY(-5upx);
		}
	}
	.line.topdown {
		animation: topdown 5s linear infinite;
	}
	._cpimg {
		position: fixed;
		top: -99999upx;
		left: -99999upx;
		z-index: -99999;
	}
</style>

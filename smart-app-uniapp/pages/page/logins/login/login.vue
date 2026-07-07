<template>
	<view>
		<view class="fixed top0 left0 mw mh">
			<image class="abs mw mh top0 left0" src="/static/img/login/bg.jpg" mode=""></image>
			<view class="login abs mw">
				<view class="login-group">
					<view class="item hlc pr30 pl30">
						<text class="w150 f32pc">员工号</text>
						<input class="border-bottom-D" type="text" maxlength=20 @blur="checkUsername" placeholder="请输入员工号" v-model.trim="form.username" />
					</view>
					<view class="item hlc pr30 pl30">
						<text class="w150 f32pc">密&nbsp;&nbsp;&nbsp;码</text>
						<input class="border-bottom-D" maxlength=20 @blur="checkPassWord" placeholder="请输入密码" password type="text" v-model.trim="password" />
					</view>
				</view>
				<view class="action-row h60 mt40 mb40 pr30 pl30">
					<navigator url="../findPwd/findPwd" hover-class="none" class="hcc main-color">忘记密码</navigator>
				</view>
				<view class="hcc login-btn main-bg cf"  @click="bindLogins">账号密码登录</view>
				<navigator v-if="loginFirst" class="hcc face-img main-bg cf" hover-class="none" url="../face/face?type=1">人脸识别登录</navigator>
			</view>
			<!-- <view v-if="editPasswordMadol" class="madol">
				<view @click="editPasswordMadol = !editPasswordMadol" class="mask"></view>
				<view class="content">
					<text>首次登录修改密码</text>
					<view @click="toEditPassword" class="btn-row"><button type="primary" class="primary" >去修改</button></view>
					<view @click="editPasswordMadol = !editPasswordMadol" class="btn-row"><button type="primary" class="primary">暂不修改</button></view>
				</view>
			</view> -->
			<yt-loading />
		</view>
	</view>
</template>

<script>
import loginAccount from '@/api/api-account.js'
import {storage, ISCOMPLETEINFORMATION } from '@/tools/storage.js'
import CryptoJS from '@/common/js/Crypto.js'
import information from '@/api/api-complete-information.js'
export default {
	data() {
		return {
			form: {
				username: '',
				password: '',
				randomStr: `${new Date().getTime()}`
			},
			password: '',
			disabled: false,
			checkPass: false,
			userTel: '',
			editPasswordMadol: false,
			loginFirst: false // 是否第一次进入app登录 默认不是
		};
	},
	onLoad() {
		this.$loading(false)
		uni.removeStorageSync('isNetErrPage')
		this.$store.commit('restart',false)
		this.removeLoginData()
		const username = storage.getSync(storage.USER_NAME)
		const isfirst = storage.getSync('isLoginFirst')
		if (isfirst == 'false') { // 为空表示是第一次
			this.loginFirst = true
		}
		if (!username) {
			this.editPasswordMadol = true
		}
		this.userTel = storage.getSync(storage.USER_TEL)
		this.checkPass = storage.getSync(ISCOMPLETEINFORMATION)
		
	},
	methods: {
		// 检测是否完成信息注册
		async checkInformation() {
			try{
				const res = await information.checkFace()
				if (!res) return
				this.checkPass = res.data.data
				if (this.checkPass) {
					storage.set('isLoginFirst', 'false')
					uni.switchTab({
						url: '/pages/page/tabbar/home/home'
					})
					return
				} else {
					this.$ytHint.modal({
						content: '检测到您信息未完善，前去完善,请上传人脸照片,人脸是进入园区的唯一凭证!',
						cancelText: '跳过',
						confirmText: '去完善',
						confirm: () => {
							uni.redirectTo({
								url: '../Information/Information'
							})
						},
						cancel: () => {
							uni.switchTab({
								url: '/pages/page/tabbar/home/home'
							})
						}
					})
				}
			}catch(e){
				//TODO handle the exception
			}
		},
		// 登录
		bindLogins() {
			const status = this.checkPassWord()
			if (!status) return
			if (!this.form.username) {
				this.$ytHint.toast({
					title: '请输入员工号'
				})
				return
			} else if (!this.password) {
				this.$ytHint.toast({
					title: '请输入密码'
				})
				return
			}
			// console.log(this.form.password);
			this.form.password = this.passwordEncrypt()
			// if ( this.form.username.indexOf('cy') == -1 && this.form.username.indexOf('x') == -1) {
			// 	this.form.username = this.form.username.toUpperCase()
			// }
			loginAccount.login(this.form).then(res => {
				if (res.data.code == 1) {
					this.$ytHint.toast({
						title: res.data.msg
					})
					return
				}
				if (res.data.error) {
					this.$ytHint.toast({
						title: res.data.message
					})
					return
				}
				this.savaLoginData(res.data); // 登录成功保存信息
				loginAccount.getSalayType({badge : this.form.username}).then(res => {
					console.log(res);
					if (!res) return
					if (res.data.data.salaryTypeName) {
						storage.set(storage.SALARYTYPENAME, res.data.data.salaryTypeName);
					}
				}).catch((e) => {
					console.log(e);
					// throw e
				})
				this.$store.commit('restart',false)
				this.checkInformation ()
				// uni.switchTab({
				// 	url: '/pages/page/tabbar/home/home'
				// })
			})
			.catch(err => {
				console.log(err);
				throw err
			});
		},
		// 输入密码的时候判断是否驶入完
		hasPassword() {
			let status = false;
			for (let el in this.form) {
				if (this.form[el] === '') {
					status = true;
					break;
				}
			}
			this.disabled = status;
		},
		// 检查员工号
		checkUsername () {
			if (this.form.username.length < 5) {
				this.$ytHint.toast({
					title: '员工号长度值在5到20之间'
				})
			}
		},
		// 检查密码
		checkPassWord () {
			if (this.password.length < 6 || this.password.length >20) {
				this.$ytHint.toast({
					title: '密码长度在6到20之间'
				})
				return false
			} else {
				return true
			}
		},
		// 加密登录密码的算法ase
		passwordEncrypt() {
			return CryptoJS.encrypt(this.password);
		},
		// 登录成功将信息存进本地
		savaLoginData(data) {
			const token = data.access_token;
			const refToken = data.refresh_token
			const userID = data.user_id;
			const userName = data.username
			const tokenTimeout = (new Date()).getTime() + parseInt(`${data.expires_in}000`) -3600000 // 减去一个小时
			storage.set(storage.USER_TOKEN, token);
			console.log('退出登录');
			storage.set(storage.USER_ID, userID);
			storage.set(storage.USER_NAME, userName);
			storage.set(storage.EXPIRES_IN, tokenTimeout)
			storage.set(storage.REFRESH_TOKEN, refToken)
		},
		// 进入登录页将登录信息删除
		removeLoginData() {
			console.log('退出登录');
			storage.removeSync(storage.USER_TOKEN);
			storage.removeSync(storage.REFRESH_TOKEN);
			storage.removeSync(storage.EXPIRES_IN);
			storage.removeSync('verify') // 默认删除人脸修改密码的key
		},
		// 首次登录提示修改密码
		// toEditPassword() {
		// 	this.editPasswordMadol = false
		// 	uni.navigateTo({
		// 		url: '../findPwdPhone/findPwdPhone'
		// 	})
		// }
	}
};
</script>

<style lang="scss" scoped>
.login {
	top: 400upx;
}
.item {
	height: 166upx;
	margin: 0;
	.title {
		width: 200upx;
	}
	input {
		flex: 1;
	}
}

.action-row {
	display: flex;
	flex-direction: row;
	justify-content: flex-end;
	align-items: center;
}

.oauth-row {
	display: flex;
	flex-direction: row;
	justify-content: center;
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
}

.oauth-image {
	width: 100upx;
	height: 100upx;
	border: 1upx solid #dddddd;
	border-radius: 100upx;
	margin: 0 40upx;
	background-color: #ffffff;
}


.oauth-image image {
	width: 60upx;
	height: 60upx;
	margin: 20upx;
}

.face-img {
	width: 690upx;
	height: 92upx;
	margin: 40upx auto;
	border-radius: 10upx;
	color: #fff;
	font-size: 32upx;
	letter-spacing: 20upx;
}

.login-btn {
	width: 690upx;
	height: 92upx;
	margin: 40upx auto;
	color: #fff;
	font-size: 32upx;
	letter-spacing: 20upx;
	border-radius: 10upx;
}


.login-group {
	width: 648upx;
	height: 332upx;
	background: rgba(255, 255, 255, 1);
	box-shadow: 0px 5px 49px 0px rgba(185, 185, 185, 0.48);
	border-radius: 20upx;
	margin: 0 auto;
}
.btn-row {
	width: 690upx;
	margin: 0 auto;
}
.madol {
	position: fixed;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	z-index: 999;
	.mask {
		position: absolute;
		left: 0;
		top: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, .6)
	}
	.content {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: center;
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
		width: 640upx;
		height: 400upx;
		padding: 30upx;
		background-color: #ffffff;
		.btn-row {
			width: 100%;
		}
	}
}
</style>

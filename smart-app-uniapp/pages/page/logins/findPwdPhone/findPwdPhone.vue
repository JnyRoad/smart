<template>
    <view class="mw bgf">
        <view v-if="!finish" class="mw">
            <view class="hbc pl30 pr30 h110 border-bottom-D">
            	<text class="f30pc w200">工号</text>
				<input class="w300 tr" @blur="getMobile" v-model.trim="employeeId" placeholder="请输入工号" type="text" value="" />
            </view>
			<view class="hlc pl30 pr30 h110 border-bottom-D rel">
				<text class="f30pc w200">验证码</text>
				<input class="w300 tl" v-model.trim="smsCode" placeholder="请输入短信验证码" type="text" value="" />
				<view @click.stop="getCode" :class="{ 'smsbtn-disabled': isSendingSms }" class="abs f28pc2 smsbtn right30">{{loadingText}}</view>
			</view>
			<view class=" pr30 pl30 mt20">
				<text class="f30pc2">温馨提示</text>
				<view class="f30pc4 vlc">
					温馨提示：
					<text>1.请输入工号后，系统会在不展示预留手机号的前提下发起找回验证。</text>

					<text>2.系统会直接向预留手机号发送验证码，输入验证码后进行密码修改。</text>

					<text>3.如果有问题，请联系人资系统管理员。</text>
				</view>
			</view>
            <view class="pl30 pr30 mt100 mw">
                <button type="primary" class="primary" @click="nextStep">下一步</button>
            </view>
        </view>
		<yt-loading />
    </view>
</template>

<script>
    import settingPassword  from '@/api/api-password.js' 
	import { storage } from '@/tools/storage.js'
    export default {
        components: {
        },
		data() {
			return {
                finish:false, //是否设置成功
                employeeId:'',
                smsCode:'',
				challengeId: '', // 服务端一次性 challenge，不能用工号替代
				challengeEmployeeId: '', // 当前 challenge 所属员工号
				challengeRequest: null, // 当前员工号唯一的 challenge 创建请求
				challengeVersion: 0, // 工号变化时递增，避免旧响应覆盖当前状态
                smsVerifyToken:'', //短信校验通过临时授权码
                smsCodeCheckValidate:'' ,//验证码是否正确
				loadingText: '获取验证码',
				loadingTime: 60,
				timeID: null,
				isSendingSms: false, // challenge 创建或短信发送期间阻止重复点击
			}
        },
		watch: {
			// 编辑工号后立即作废旧 challenge，避免用错员工号的找回凭证。
			employeeId(newEmployeeId, oldEmployeeId) {
				if (newEmployeeId !== oldEmployeeId) {
					this.invalidateChallenge(newEmployeeId)
				}
			}
		},
        methods: {
			// 工号变化后清除本地 challenge；已发出的请求不能取消，但其响应会因版本不匹配被忽略。
			invalidateChallenge(employeeId) {
				this.challengeId = ''
				this.challengeEmployeeId = employeeId
				this.challengeRequest = null
				this.challengeVersion++
			},
			// 确保当前工号只有一个创建中的 challenge 请求，供 blur 与获取验证码共用。
			ensureChallenge() {
				if (!this.employeeId) {
					this.$ytHint.toast({
						title: '请输入员工号'
					})
					return Promise.resolve('')
				}
				if (this.challengeEmployeeId !== this.employeeId) {
					this.invalidateChallenge(this.employeeId)
				}
				if (this.challengeId) return Promise.resolve(this.challengeId)
				if (this.challengeRequest) return this.challengeRequest

				const employeeId = this.employeeId
				const challengeVersion = this.challengeVersion
				const request = settingPassword.mobileQuery(employeeId)
					.then(res => {
						const challengeId = res.data.data
						if (this.challengeEmployeeId !== employeeId || this.challengeVersion !== challengeVersion) {
							return ''
						}
						this.challengeId = challengeId
						uni.setStorageSync('USER_NAME', employeeId)
						return challengeId
					})
					.catch(() => {
						if (this.challengeEmployeeId === employeeId && this.challengeVersion === challengeVersion) {
							this.$ytHint.toast({
								title: '获取验证码失败，请重试'
							})
						}
						return ''
					})
					.finally(() => {
						if (this.challengeRequest === request) {
							this.challengeRequest = null
						}
					})
				this.challengeRequest = request
				return request
			},
			// 输入框失焦时预创建 challenge，后续点击获取验证码会复用同一请求。
			getMobile() {
				return this.ensureChallenge()
			},
            // 获取验证码
			async getCode(){
				if (this.loadingTime != 60 || this.isSendingSms) return
				if (!this.employeeId) {
					this.$ytHint.toast({
						title: '请输入工号'
					})
					return
				}
				this.isSendingSms = true
				try{
					const challengeId = await this.ensureChallenge()
					if (!challengeId || challengeId !== this.challengeId || this.employeeId !== this.challengeEmployeeId) return
					const res = await settingPassword.sendSms(challengeId)
					if (res.data.code == 0) {
						this.cutTime()
						this.$ytHint.toast({
							title: '请求验证码成功,请稍后!'
						})
					}
				}catch(e){
					this.$ytHint.toast({
						title: '获取验证码失败，请重试'
					})
				} finally {
					this.isSendingSms = false
				}
                
            },
			// 倒计时
			cutTime() {
				const that = this
				this.timeID = setTimeout(function() {
					that.loadingTime--
					that.loadingText = `${that.loadingTime}s后重试`
					if (that.loadingTime === 0) {
						that.loadingText = '获取验证码'
						that.loadingTime = 60
					} else {
						that.cutTime()
					}
				},1000)
			},
            // 下一步
            nextStep(){
				if (!this.employeeId) {
					this.$ytHint.toast({
						title: '请输入工号'
					})
					return
				} else if (!this.smsCode) {
					this.$ytHint.toast({
						title: '请输入验证码'
					})
					return
				}
                // 验证短信码是否正确
				settingPassword.verifySms({ smsCode:this.smsCode,challengeId: this.challengeId}).then(res => {
					// 短信码错误
					if(res.data.code == 1){
					    this.$ytHint.toast({
							title: '验证码错误'
						})
					    return
					}
					clearTimeout(this.timeID)
					this.loadingText = '获取验证码'
					uni.setStorage({
						key: 'verify',
						data: res.data.data
					})
					uni.navigateTo({
						url: './setPassword'
					})
				})
                
            }
        }
    }
</script>

<style lang="scss">

.smsbtn {
	width: auto;
	padding: 10upx;
	border: 1upx solid #ddd;
	border-radius: 20upx;
}

.smsbtn-disabled {
	opacity: 0.5;
	pointer-events: none;
}

button[type=primary] {
	background-color: $main-color;
}
button[disabled][type=primary] {
	background-color: $main-tint-color;
}
	
.phone {
    width:690upx;
    height:107upx;
    background:rgba(255,255,255,1);
    box-shadow:0upx 0upx 29upx 0upx rgba(204,204,204,0.52);
    border-radius:54upx;
}
.w38{
   width: 38upx;
}
.h62{
    height: 62upx;
}
.h52{
    height: 52upx;
}
.h36{
    height: 36upx;
}
.w40{
    width: 40upx;
}
.w140 {
    width: 140upx;
}
.wh162 {
	width: 162upx;
	height: 162upx;
}
.success {
	padding-top: 30upx;
	font-size: 30upx;
	color: #3f85bc;
}
.mt300 {
	margin-top: 300upx;
}
</style>

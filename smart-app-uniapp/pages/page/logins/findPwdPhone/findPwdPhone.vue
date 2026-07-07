<template>
    <view class="mw bgf">
        <view v-if="!finish" class="mw">
            <view class="hbc pl30 pr30 h110 border-bottom-D">
            	<text class="f30pc w200">工号</text>
				<input class="w300 tr" @blur="getMobile" v-model.trim="employeeId" placeholder="请输入工号" type="text" value="" />
            </view>
			<view class="hbc pl30 pr30 h110 border-bottom-D">
				<text class="f30pc w200">预留手机号</text>
				<text class="f30pc tr w300">{{yuMobile}}</text>
			</view>
			<view class="hbc pl30 pr30 h110 border-bottom-D">
				<text class="f30pc w200">确认手机号</text>
				<input class="w300 tr" v-model.trim="mobile" placeholder="请输入手机号码" type="text" value="" />
			</view>
			<view class="hlc pl30 pr30 h110 border-bottom-D rel">
				<text class="f30pc w200">验证码</text>
				<input class="w300 tl" v-model.trim="smsCode" placeholder="请输入短信验证码" type="text" value="" />
				<view @click.stop="getCode" class="abs f28pc2 smsbtn right30">{{loadingText}}</view>
			</view>
			<view class=" pr30 pl30 mt20">
				<text class="f30pc2">温馨提示</text>
				<view class="f30pc4 vlc">
					温馨提示：
					<text>1.请输入工号，显示您预留的手机号码，为您入职时登记的联系方式。</text>

					<text>2.在确认手机号里，请输入您预留的完整手机号码，系统会自动核对。</text>

					<text>3.核对正确后，系统会发送短信，输入验证码后，进行密码修改。</text>

					<text>4.如果有问题，请联系人资系统管理员。</text>
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
	import {checkPhone} from '@/common/js/util.js'
	import { storage } from '@/tools/storage.js'
    export default {
        components: {
        },
        data() {
            return {
                finish:false, //是否设置成功
                employeeId:'',
                smsCode:'',
                yuMobile:'', // 预留号码
				yuTel: '', // 预留号码数据
                mobile:'', // 当前用户输入
                smsVerifyToken:'', //短信校验通过临时授权码
                smsCodeCheckValidate:'' ,//验证码是否正确
				loadingText: '获取验证码',
				loadingTime: 60,
				timeID: null,
            }
        },
        methods: {
			// 获取预留手机号码
			async getMobile() {
				if (!this.employeeId) {
					this.$ytHint.toast({
						title: '请输入员工号'
					})
					return
				}
				try{
					const res = await settingPassword.mobileQuery(this.employeeId)
					this.yuMobile = res.data.data
					this.username = uni.setStorageSync('USER_NAME',this.employeeId)
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
            // 获取验证码
			async getCode(){
				if (this.loadingTime != 60) return
				if (!this.employeeId) {
					this.$ytHint.toast({
						title: '请输入工号'
					})
					return
				} else if (this.mobile === '') {
					this.$ytHint.toast({
						title: '请输入手机号码'
					})
					return
				}
				if (!checkPhone(this.mobile)) {
					this.$ytHint.toast({
						title: '手机号码不合法'
					})
					return
				}
				try{
					const res = await settingPassword.sendSms(this.employeeId, this.mobile)
					if (res.data.code == 0) {
						this.cutTime()
						this.$ytHint.toast({
							title: '请求验证码成功,请稍后!'
						})
					}
				}catch(e){
					console.log(e);
					throw e
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
				} else if (!this.mobile) {
					this.$ytHint.toast({
						title: '请输入手机号码'
					})
					return
				} else if (!this.smsCode) {
					this.$ytHint.toast({
						title: '请输入验证码'
					})
					return
				}
                // 验证短信码是否正确
                settingPassword.verifySms({ mobile:this.mobile,smsCode:this.smsCode,badge: this.employeeId}).then(res => {
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

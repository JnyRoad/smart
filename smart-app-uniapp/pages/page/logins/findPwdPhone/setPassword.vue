<template>
	<view class="mw bgf">
		<view class="hbc pl30 pr30 h110 border-bottom-D">
			<text class="f30pc w200">新密码</text>
			<input class="w300 tr" :maxlength="20" @blur="checkPassword(1)" password v-model.trim="password" placeholder="6位字母、数字组合" type="text" value="" />
		</view>
		<view class="hbc pl30 pr30 h110 border-bottom-D">
			<text class="f30pc w200">确认密码</text>
			<input class="w300 tr" :maxlength="20" password @blur="checkPassword(2)" v-model.trim="apassword" placeholder="6位字母、数字组合" type="text" value="" />
		</view>
		 <view class="pl30 pr30 mt100 mw">
		    <button type="primary" class="primary" @click="apply">确定</button>
		</view>
		<yt-loading />
	</view>	
</template>

<script>
	import settingPassword from '@/api/api-password.js'
	import CryptoJS from '@/common/js/Crypto.js'
	export default {
		data () {
			return {
				password: '',
				apassword: '',
				smsVerifyToken: '',
				username: ''
			}
		},
		onLoad() {
			this.smsVerifyToken = uni.getStorageSync('verify')
			this.username = uni.getStorageSync('USER_NAME')
		},
		methods: {
			// 检查密码数据
			checkPassword (type) {
				if (this.password.length<6 || this.password.length<6) {
					this.$ytHint.toast({
						title: '密码长度至少6位'
					})
					if (type === 1) {
						this.password = ''
					} else if (type === 2) {
						this.apassword = ''
					}
				}
			},
			// 确认修改
			apply () {
				if (!this.password || !this.apassword) {
					this.$ytHint.toast({
						title: '请输入密码'
					})
					return
				}
				if (this.password !== this.apassword) {
					this.$ytHint.toast({
						title: '两次输入不匹配，请重新输入'
					})
					this.password = ''
					this.apassword = ''
					return
				}
				this.cjPassword = CryptoJS.encrypt(this.password)
				settingPassword.updatePassword({username: this.username, password:this.cjPassword,updateAuthCode:this.smsVerifyToken }).then(res => {
					if(!res) return
					uni.removeStorage({
						key: 'verify'
					})
					this.$ytHint.toast({
						title: '密码设置成功'
					})
					uni.redirectTo({
						url: './success'
					})
				})
			}
		},
	}
</script>

<style lang="scss">
	button[type=primary] {
		background-color: $main-color;
	}
	button[disabled][type=primary] {
		background-color: $main-tint-color;
	}
</style>

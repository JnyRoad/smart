<template>
	<view class="mw bgf">
		<view class="mw">
			<view class="hbc pl30 pr30 h110 border-bottom-D">
				<text class="f30pc w200">新手机号码</text>
				<input class="w300 tr" @blur="checkTel" v-model.trim="tel" placeholder="请输入新手机号码" type="text" value="" />
			</view>
			<view class="hlc pl30 pr30 h110 border-bottom-D rel">
				<text class="f30pc w200">验证码</text>
				<input class="w300 tl" v-model.trim="sms" placeholder="请输入短信验证码" type="text" value="" />
				<view @click.stop="sendSms" class="abs h110 hcc f28pc2 smsbtn right30">{{btnTxt}}</view>
			</view>
		</view>
		<view class="hcc mt80">
			<yt-button @click="next" btnText="确定" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytbutton from '@/components/yt-button/index.vue'
	import {checkPhone} from '@/common/js/util.js'
	import {setting} from '@/api/api-mine.js'
	export default {
		components: {
			'yt-button': ytbutton
		},
		data() {
			return {
				tel: '',
				timerId: null,
				time: 60,
				btnTxt: '发送验证码',
				sms: ''
			}
		},
		onLoad() {
			
		},
		methods: {
			async sendSms() {
				if (this.time != 60) return
				if (!this.tel || !checkPhone(this.tel)) {
					this.$ytHint.toast({
						title: '手机号码不合法'
					})
					return
				}
				this.cutTime()
				try{
					const res = await setting.newPhoneSendCode(this.tel)
					if (!res) return
					this.$ytHint.toast({
						title: '验证码发送成功',
						icon: 'success'
					})
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 验证
			async next() {
				if (!this.tel) {
					this.$ytHint.toast({
						title: '请输入手机号码'
					})
					return
				}
				if (!this.sms) {
					this.$ytHint.toast({
						title: '请输入验证码'
					})
					return
				}
				const obj = {
					mobile: this.tel,
					smsCode: this.sms
				}
				try{
					const res = await setting.updataPhone(obj)
					if (!res) return
					this.$ytHint.toast({
						title: '验证成功'
					})
					setTimeout(() => {
						uni.redirectTo({
							url: '../../logins/findPwdPhone/success'
						})
					},1500)
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			checkTel () {
				if (!checkPhone(this.tel)) {
					this.$ytHint.toast({
						title: '手机号码不合法'
					})
				}
			},
			cutTime() {
				this.time--
				this.timerId = setTimeout(()=>{
					if (this.time <= 0) {
						clearTimeout(this.timerId)
						this.btnTxt = '发送验证码'
						this.time = 60
						return
					}
					this.btnTxt = `${this.time}s后重试`
					this.cutTime()
				},1000)
			}
		}
	}
</script>

<style lang="scss" scoped>
		.code {
			position: absolute;
			top: 50%;
			transform: translateY(-50%);
			right: 30upx;
			width: 160upx;
			height: 50upx;
			border: 1upx solid $main-color;
			border-radius: 12upx;
			color: $main-color;
		}
		.sms {
			top: 50%;
			transform: translateY(-50%);
			left: -150upx;
		}
</style>
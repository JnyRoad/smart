<template>
	<view class="bgf mw">
		<view class="mw">
			<view class="hbc pl30 pr30 h110 border-bottom-D">
				<text class="f30pc w200">工号</text>
				<input class="w300 tr" v-model.trim="badge" placeholder="请输入工号" type="text" value="" />
			</view>
			<view class="hbc pl30 pr30 h110 border-bottom-D">
				<text class="f30pc w200">原手机号码</text>
				<input class="w300 tr" @blur="checkTel" v-model.trim="tel" placeholder="请输入手机号码" type="text" value="" />
			</view>
			<view class="hlc pl30 pr30 h110 border-bottom-D rel">
				<text class="f30pc w200">验证码</text>
				<input class="w300 tl" v-model.trim="sms" placeholder="请输入短信验证码" type="text" value="" />
				<view @click.stop="sendSms" class="abs h110 hcc f28pc2 smsbtn right30">{{btnTxt}}</view>
			</view>
		</view>
		<view class="hcc mt80" @click="next">
			<yt-button  btnText="下一步" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<yt-loading></yt-loading>
	</view>	
</template>
<script>
	import ytbutton from '@/components/yt-button/index.vue'
	import {checkPhone} from '@/common/js/util.js'
	import apiPassword from '@/api/api-password.js'
	import {setting} from '@/api/api-mine.js'
	export default {
		components: {
			'yt-button': ytbutton
		},
		data() {
			return {
				tel: '',
				badge: '', // 员工号
				sms: '', // 短信验证码
				timerId: null,
				time: 60,
				btnTxt: '发送验证码'
			}
		},
		methods: {
			async sendSms() {
				if (this.time != 60) return
				if (!this.badge) {
					this.$ytHint.toast({
						title: '请输入员工号'
					})
					return
				}
				if (!this.tel || !checkPhone(this.tel)) {
					this.$ytHint.toast({
						title: '手机号码不合法'
					})
					return
				}
				this.cutTime()
				try{
					const res = await apiPassword.sendSms(this.badge)
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
				console.log(111);
				if (!this.tel || !checkPhone(this.tel)) {
					this.$ytHint.toast({
						title: '手机号码不合法'
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
					const res = await setting.verifyOldPhone(obj)
					if (!res) return
					this.$ytHint.toast({
						title: '验证成功'
					})
					setTimeout(() => {
						uni.redirectTo({
							url: './change-tel'
						})
					},1500)
				}catch(e){
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
		},
	}
</script>

<style lang="scss" scoped>
	.yt-page {
		.code {
			position: absolute;
			top: 148upx;
			right: 30upx;
			width: 160upx;
			height: 50upx;
			border: 1upx solid $main-color;
			border-radius: 12upx;
			color: $main-color;
		}
	}
</style>

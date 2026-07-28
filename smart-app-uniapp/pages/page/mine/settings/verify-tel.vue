<template>
	<view class="bgf mw">
		<view class="mw">
			<view class="hbc pl30 pr30 h110 border-bottom-D">
				<text class="f30pc">验证码将发送至当前账号绑定的原手机号码</text>
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
	import {setting} from '@/api/api-mine.js'
	export default {
		components: {
			'yt-button': ytbutton
		},
		data() {
			return {
				sms: '', // 短信验证码
				timerId: null,
				time: 60,
				btnTxt: '发送验证码'
			}
		},
		methods: {
			async sendSms() {
				if (this.time != 60) return
				this.cutTime()
				try{
					const res = await setting.sendOldPhoneCode()
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
				if (!this.sms) {
					this.$ytHint.toast({
						title: '请输入验证码'
					})
					return
				}
				try{
					const res = await setting.verifyOldPhone(this.sms)
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

<template>
	<view class="page vcc" v-if="show">
		<view class="yt-mask" :class="{opacity: show}"  @click="hideModel"></view>
		<view :class="{animateBox: show}"  class="yt-content vcc pb20 bgf rad10">
			<view class="title hcc f32pc">{{modelTitle}}</view>
			<view class="input mb30 hcc">
				<input type="text" class="inputText" maxlength="20" confirm-type="确定" clearable placeholder="请输入(20字以内)" v-model.trim="txt"></input>
				<view v-if="showClose" @click="clearTxt" class="close abs hcc right0">
					<image class="mw mh vc" src="/static/img/mine/X.png"></image>
				</view>
			</view>
			<view class="hcc">
				<yt-button @click="hideModel(2)" btnWidth="91" btnHeight="36" btnText="取消" btnTextColor="#5c92ff" btnBgColor="#fff" ></yt-button>
				<view class="w50 h70"></view>
				<yt-button @click="hideModel(1)" btnWidth="91" btnHeight="36" btnText="确定"></yt-button>
			</view>
		</view>
	</view>
</template>

<script>
	// 弹窗输入模块
	import ytbutton from '@/components/yt-button/index.vue'
	export default {
		name: 'yt-mine-model',
		components: {
			'yt-button': ytbutton
		},
		data () {
			return {
				txt: ''
			}
		},
		computed: {
			show() {
				return this.$store.state.modelStatus
			},
			showClose() {
				if (!this.txt) return false
				else return true
			}
		},
		props: {
			modelTitle: {
				type: String,
				default: ''
			},
			modelInput: {
				type: String,
				default: ''
			},
			idType: {
				type: String,
				default: ''
			}
		},
		created() {
			if (this.modelInput === '请输入') {
				this.txt = ''
				return
			}
			this.txt = this.modelInput
		},
		methods: {
			hideModel(type) {
				this.$store.commit('ytModelStatus',false)
				if (type === 1) { // 用户按下了确定按钮
					if (this.txt === this.modelInput) return
					const text = this.txt.replace(/\s*/g,"")
					this.$emit('sendInput',{
						value: text,
						idType: this.idType
					})
				} else if (type === 2){ // 用户按下取消按钮 就算用户做过修改 不要修改用户原来的数据
					// ..do something
				}
			},
			clearTxt() {
				this.txt = ''
			}
		},
	}
</script>

<style lang="scss" scoped>
	.common {
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
	}
	.page {
		position: fixed;
		@extend .common;
		z-index: 999;
		.yt-mask {
			position: absolute;
			@extend .common;
			background-color: rgba(0,0,0, .5);
		}
		.yt-content {
			position: relative;
			width: 600upx;
			
			&.animateBox {
				
			}
			.title {
				width: 100%;
				height: 100upx;
				font-weight: bold;
			}
			.input {
				position: relative;
				width: 400upx;
				height: 80upx;
				padding-right: 80upx;
				border-bottom: 1rpx solid $uni-border-color;
				input {
					width: 100%;
					height: 100%;
				}
				.close {
					width: 36upx;
					height: 36upx;
				}
			}
		}
	}
	
	@keyframes animateBox {
		0% {transform: scale(0)}
		80% {transform: scale(1.2)}
		100% {transform: scale(1)}
	}
	
	.animateBox {
		animation: animateBox .5s ease-out .00000001s;
	}
	
</style>

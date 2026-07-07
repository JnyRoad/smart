<template>
	<view class="mw bgf">
		<view class="top-head rel">
			<view class="vcc mh mw">
				<text class="salary-fs cf">12000.00</text>
				<text class="f28 cf">合计工资（元）</text>
			</view>
			<view class="abs bottom0 mw h90 user hbc pl30 pr30">
				<text class="f32 cf">曾小慧</text>
				<text class="f32 cf">050746</text>
			</view>
		</view>
		<view class="mw ">
			<view class="f32 mw hlc main-color border-bottom-D pl30 pr30">工资明细</view>
			<view class="mw pl30 pr30">
				<view class="hbc mw h60">
					<text class="f32pc2">正常上班工资</text>
					<text class="f32 main-color">4000.00</text>
				</view>
				<view class="hbc mw h60">
					<text class="f32pc2">技能津贴</text>
					<text class="f32 main-color">1000.00</text>
				</view>
				<view class="hbc mw h60">
					<text class="f32pc2">绩效工资</text>
					<text class="f32 main-color">3000.00</text>
				</view>
				<view class="hbc mw h60">
					<text class="f32pc2">计件工资</text>
					<text class="f32 main-color">3020.00</text>
				</view>
				<view class="hbc mw h60">
					<text class="f32pc2">餐补</text>
					<text class="f32 main-color">1000.00</text>
				</view>
				<view class="hbc mw h60">
					<text class="f32pc2">迟到扣除</text>
					<text class="f32 err-color">-300.00</text>
				</view>
			</view>
		</view>
		<view @click="applySigna" class="btn" :class="[isSigna?'sign':'']">{{isSigna?'已签收':'签收'}}</view>
		<view v-if="showSigna" class="wrapper">
			<view @click="showSigna = !showSigna" class="mask"></view>
			<view :class="[animateType==1?'top':'',animateType==2?'down':'']" class="content">
				<view class="buttons hbc pl30 pr30">
					<view @click="retDraw" class="f32pc2">取消</view>
					<text class="f32pc2 bold">电子签名</text>
					<view @click="subCanvas" class="main-color f32">确认签收</view>
				</view>
				<view class="handCenter">
					<canvas class="handWriting" disable-scroll="true" @touchstart="uploadScaleStart" @touchmove="uploadScaleMove"
					 @touchend="uploadScaleEnd" canvas-id="handWriting">
					</canvas>
				</view>
				<view class="mw f32pc2 hcc">请确认工资后，用手写方式完成电子签名</view>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import Handwriting from "@/common/js/signature.js"
	export default {
		data() {
			return {
				slideValue: 50,
				lineColor: '#333',
				handwriting: '',
				showSigna: false, // 显示签名控件
				hasSigna: false ,// 是否签名
				isSigna: false, // 是否签收
				animateType: 0, // 动画方式
			}
		},
		onLoad(e) {
			uni.setNavigationBarTitle({
				title: '2019年8月工资条'
			})
			this.handwriting = new Handwriting({
				lineColor: this.lineColor,
				slideValue: this.slideValue, // 0, 25, 50, 75, 100
				canvasName: 'handWriting',
			})
		},
		methods: {
			// 去签收
			applySigna() {
				if (this.isSigna) return
				this.showSigna = !this.showSigna
				this.animateType = 1
			},
			retDraw() {
				this.handwriting.retDraw()
				this.showSigna = !this.showSigna
				this.animateType = 2
				this.hasSigna = false
				this.isSigna = false
			},
			// 笔触开始
			uploadScaleStart(event) {
				this.handwriting.uploadScaleStart(event)
			},
			// 笔画移动
			uploadScaleMove(event) {
				this.handwriting.uploadScaleMove(event)
			},
			// 笔画结束
			uploadScaleEnd(event) {
				this.handwriting.uploadScaleEnd(event)
				this.hasSigna = true
			},
			subCanvas() {
				this.handwriting.saveCanvas().then(res => {
					if (!this.hasSigna) {
						this.$ytHint.toast({
							title: '请签名！！！'
						})
						return
					}
					this.hasSigna = true
					this.isSigna = true
					this.showSigna = !this.showSigna
				}).catch(err => {
					console.log(err);
				});
			},
		}
	}
</script>

<style lang="scss" scoped="true">
	.top-head {
		width: 100%;
		height: 400upx;
		background-color: $main-color;
		.salary-fs {
			font-size: 60upx;
		}
		.user {
			background-color: rgba(0,0,0, 0.2);
		}
	}
	.btn {
		width: 320upx;
		height: 90upx;
		text-align: center;
		line-height: 90upx;
		color: #fff;
		font-size: 32upx;
		background-color: $main-color;
		border-radius: 90upx;
		margin: 40upx auto;
		&.sign{
			background-color: #cccccc;
		}
	}
	.wrapper {
		position: fixed;
		bottom: 0;
		left: 0;
		width: 100%;
		height: 100%;
		.mask {
			position: fixed;
			width: 100%;
			height: 100%;
			top: 0;
			left: 0;
			background-color: rgba(0,0,0, 0.3);
		}
		.content {
			position: absolute;
			bottom: 0;
			width: 100%;
			padding: 0 0 30upx 0;
			border-top-left-radius: 20upx;
			border-top-right-radius: 20upx;
			overflow: hidden;
			display: flex;
			align-content: center;
			flex-direction: column;
			justify-content: center;
			font-size: 28upx;
			background-color: #fff;
			transform: translateY(0);
			&.down {
				animation: trans-down 0.4s ease-in-out;
			}
			&.top {
				animation: trans-top 0.4s ease-in-out;
				
			}
		}
	}
	.handWriting {
		width: 100%;
		height: 350upx;
	}

	.handRight {
		align-items: center;
	}

	.handCenter {
		border: 4upx dashed #e9e9e9;
		flex: 5;
		overflow: hidden;
		box-sizing: border-box;
		width: 100%;
		margin: 20upx auto;
		background: #eee;
	}

	.handTitle {
		flex: 1;
		color: #666;
		justify-content: center;
		font-size: 30upx;
	}

	.handBtn {
		flex-direction: column;
		padding: 40upx 20upx;
	}

	.buttons {
		width: 100%;
		margin-top: 20upx;
		justify-content: space-between;
	}

	.buttons>button {
		font-size: 30upx;
		height: 80upx;
		width: 120upx;
	}

	.delBtn {
		color: #666;
	}

	.color {
		align-items: center;
	}

	.color>text {
		margin-right: 20upx;
	}

	.subBtn {
		background: #008ef6;
		color: #fff;
		text-align: center;
		justify-content: center;
	}
	
	.black-bg {
		background-color: #333333;
	}

	.select {
		width: 40upx;
		height: 40upx;
	}

	.select.color_select {
		width: 60upx;
		height: 60upx;
	}

	.select {
		width: 40upx;
		height: 40upx;
	}

	.select.color_select {
		width: 60upx;
		height: 60upx;
	}

	.slide-wrapper {
		align-items: center;
		margin-bottom: 20upx;
	}

	.slider {
		width: 400upx;
		padding-left: 20upx;
	}

	.drop {
		width: 50upx;
		height: 50upx;
		border-radius: 50%;
		background: #FFF;
		position: absolute;
		left: 0upx;
		top: -10upx;
		box-shadow: 0px 1px 5px #888888;
	}

	.slide {
		width: 250upx;
		height: 30upx;
	}

	.showimg {
		border: 4upx solid #e9e9e9;
		overflow: hidden;
		width: 90%;
		margin: 0 auto;
		background: #eee;
		height: 350upx;
		margin-top: 40upx;
		align-items: center;
		justify-content: center;
	}

	.showimg>image {
		width: 100%;
		height: 100%;
	}

	.showimg>text {
		font-size: 40upx;
		color: #888;
	}
	@keyframes trans-top {
		from {
			transform: translateY(200%);
		}
		to {
			transform: translateY(0);
		}
	}
	@keyframes trans-dwon {
		from {
			transform: translateY(0);
		}
		to {
			transform: translateY(200%);
		}
	}
</style>

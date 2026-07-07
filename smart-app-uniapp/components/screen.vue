<template>
    <view class=" cu-modal" :class=" selection ? 'show' : 'hide' ">
			<view class="bcf screen">
				<view class="hcc screen-title pt20 pb20 f32 cf">
					<image @click="closeScreen" class="w42 h42 cursor" src="/static/img/serive/recruit/X.png" alt=""></image>
					筛选
				</view>
				<view class="pl60 pr60 mt40 mb40">
					<view class="hlc f32pc mb20 bold">性别</view>
					<view class="hac mt60">
						<view class="button0 borderC">全部</view>
						<view class="button1 borderC">男</view>
						<view class="button0 borderC">女</view>
					</view>
				</view>
				<view class="pl60 pr60 mt40 mb40">
					<view class="hlc f32pc bold">年龄（18-40）</view>
					<view class="hlc mt60">
						<view class="rel">
							<image class="w48 h48" src="/static/img/serive/recruit/check1.png"></image>
						</view>
						<view class="age-progress active"></view>
						<view class="rel">
							<view class="w58 h52 finish-num">50</view>
							<image class="w48 h48" src="/static/img/serive/recruit/check1.png"></image>
						</view>
						<view class="age-progress"></view>
					</view>
				</view>
				<view class="btn-row">
					<button type="primary" class="primary" @tap="bindLogins">确定</button>
				</view>
			</view>
			
			<p class="mb60 mb60"></p>
		</view>
</template>
<script>
	// 废弃
	export default {
		data() {
			return {
                
			}
        },
        props: {
		  selection:{
              type:Boolean,
              default:false
          }
        },
        methods: {
            closeScreen() {
                this.$emit('closeScreen')
            }
        }
	}	
</script>
<style lang="scss">
.screen {
		position: fixed;
		bottom: 0;
		left: 0;
		width: 100%;
		border-top-left-radius:50rpx;
		border-top-right-radius:50rpx;
		&-title {
			background:rgba(153,153,153,1);
			border-top-left-radius:50rpx;
			border-top-right-radius:50rpx;
			image {
				position: absolute;
				left: 40upx;
			}
		}
	}
	.age-progress{
		width:280upx;
        height:10upx;
		background:#C7C7C7;
		border-radius:40px;
		&.active{
          background:rgba(158,191,255,1);
		}
	}
	.finish-num {
		position: absolute;
		margin-top: 30upx;
		bottom: 64upx;
		left: 0;
		z-index: 2;
		width: 66upx;
		height: 60upx;
		background: url('~/static/img/serive/recruit/bgchart.png') no-repeat;
		background-size: contain;
		color: #ffffff;
	}
	.w42{
		width: 42upx;
	}
	.h42{
		height: 42upx;
	}
</style>
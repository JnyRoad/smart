<template>
    <view class="content mw mt40">
        <navigator v-if="isFirst != ''" url="../face/face?type=3" class="group pl30 pr30 mt60 hlc sizing next">
            <view class=" hlc f1">
                <view class="icon hcc">
                    <img class="icon-face" src="static/img/login/face.png" alt="">
                </view>
                <view class="hbc f1">
                    <view class="ml20 f32pc ml70">通过人脸识别找回</view>
                    <image class="w22 h40" src="/static/img/login/right.png"></image>
                </view>
            </view>
        </navigator>
        <navigator url="../findPwdPhone/findPwdPhone" class="group pl30 pr30 mt60 hlc sizing">
            <view class=" hlc f1">
                <view class="icon hcc">
                    <img class="icon-phone" src="static/img/login/phone.png" alt="">
                </view>
                <view class="hbc f1">
                    <view class="ml20 f32pc ml70">通过人手机号码找回</view>
                    <image class="w22 h40" src="/static/img/login/right.png"></image>
                </view>
            </view>
        </navigator>
    </view>
</template>

<script>
    export default {
        data() {
            return {
				isFirst: '',
            }
        },
		onLoad() {
			this.isFirst = uni.getStorageSync('isLoginFirst')
		}
    }
</script>

<style>
.group {
	width: 690upx;
    height:139upx;
    background:rgba(255,255,255,1);
    box-shadow:0px 5px 6px 0px rgba(204,204,204,0.2);
    border-radius:20upx;
	margin: 0 auto 20upx;
}
.icon{
    width:76upx;
}
.icon-face {
    width: 72upx;
    height: 72upx;
}
.icon-phone {
    width: 50upx;
    height: 78upx;
}
.ml70 {
    margin-left: 70upx;
}
.pl70 {
    padding-left: 70upx;
}
</style>

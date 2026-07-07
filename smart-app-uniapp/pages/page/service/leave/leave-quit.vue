<template>
    <view class="content border-top">
        <navigator  url="../leave/leave-apply?type=0" class="group bg-normal pl216 pr40 mt40 hlc sizing next">
            <view class=" hbc f1">
                <view class="ml20 f32pc ">正常离职办理</view>
                <image class="w22 h40" src="/static/img/login/right.png"></image>
            </view>
        </navigator>
        <navigator url="../leave/leave-apply?type=1" class="group bg-abnormal pl216 pr40 mt40 hlc sizing">
            <view class=" hbc f1">
                <view class="ml20 f32pc">异常离职办理</view>
                <image class="w22 h40" src="/static/img/login/right.png"></image>
            </view>
        </navigator>
    </view>
</template>

<script>
    export default {
    }
</script>

<style>
.content {
	width: 100%;
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
	align-items: center;
}
.group {
    width:700upx;
    height:139upx;
    background:rgba(255,255,255,2);
    box-shadow:0px 5px 6px 0px rgba(204,204,204,0.2);
    border-radius:20upx;
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
.pl216 {
    padding-left: 216upx;
}
.bg-normal {
    background: url('~static/img/serive/quit/zhengchang.png') no-repeat;
    background-size: cover;
}
.bg-abnormal {
    background: url('~static/img/serive/quit/yichang.png') no-repeat;
    background-size: cover;
}

</style>

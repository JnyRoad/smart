<template>
	<view class="yt-load-box" v-if="isLoading">
		<view class="mask"></view>
		<view class="loading-center-absolute">
			<view class="object object_one"></view>
			<view class="object object_two"></view>
			<view class="object object_three"></view>
			<view class="object object_four"></view>
			<view class="object object_five"></view>
			<view class="object object_six" ></view>
			<view class="object object_seven"></view>
			<view class="object object_eight"></view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'yt-loading',
	data() {
		return {};
	},
	computed: {
		isLoading() {
			return this.$store.state.showLoading;
		}
	}
};
</script>

<style lang="scss" scoped>
@mixin loading {
	top: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 100%;
	height: 100%;
}
	
.yt-load-box {
	position: fixed;
	z-index: 999;
	@include loading;
}

.mask {
	position: absolute;
	@include loading;
}

.loading-center-absolute {
	position: absolute;
	left: 50%;
	top: 50%;
	height: 150upx;
	width: 150upx;
	transform: translate(-50%, -50%);
	-moz-border-radius: 50% 50% 50% 50%;
	-webkit-border-radius: 50% 50% 50% 50%;
	border-radius: 50% 50% 50% 50%;
}
.object {
	width: 20upx;
	height: 20upx;
	background-color: $main-color;
	position: absolute;
	-moz-border-radius: 50% 50% 50% 50%;
	-webkit-border-radius: 50% 50% 50% 50%;
	border-radius: 50% 50% 50% 50%;
	-webkit-animation: animate 0.8s infinite;
	animation: animate 0.8s infinite;
}

.object_one {
	top: 19upx;
	left: 19upx;
}
.object_two {
	top: 0upx;
	left: 65upx;
	-webkit-animation-delay: 0.1s;
	animation-delay: 0.1s;
}
.object_three {
	top: 19upx;
	left: 111upx;
	-webkit-animation-delay: 0.2s;
	animation-delay: 0.2s;
}
.object_four {
	top: 65upx;
	left: 130upx;
	-webkit-animation-delay: 0.3s;
	animation-delay: 0.3s;
}
.object_five {
	top: 111upx;
	left: 111upx;
	-webkit-animation-delay: 0.4s;
	animation-delay: 0.4s;
}
.object_six {
	top: 130upx;
	left: 65upx;
	-webkit-animation-delay: 0.5s;
	animation-delay: 0.5s;
}
.object_seven {
	top: 111upx;
	left: 19upx;
	-webkit-animation-delay: 0.6s;
	animation-delay: 0.6s;
}
.object_eight {
	top: 65upx;
	left: 0upx;
	-webkit-animation-delay: 0.7s;
	animation-delay: 0.7s;
}

@-webkit-keyframes animate {
	25% {
		-ms-transform: scale(1.5);
		-webkit-transform: scale(1.5);
		transform: scale(1.5);
	}

	75% {
		-ms-transform: scale(0);
		-webkit-transform: scale(0);
		transform: scale(0);
	}
}

@keyframes animate {
	50% {
		-ms-transform: scale(1.5, 1.5);
		-webkit-transform: scale(1.5, 1.5);
		transform: scale(1.5, 1.5);
	}

	100% {
		-ms-transform: scale(1, 1);
		-webkit-transform: scale(1, 1);
		transform: scale(1, 1);
	}
}
</style>

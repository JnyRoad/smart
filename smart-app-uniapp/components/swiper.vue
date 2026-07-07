<template>
	<view>
		<swiper class="card-swiper" :autoplay="true"  :interval="5000" :indicator-dots="false" :circular="true"
			:duration="500" @change="cardSwiper">
			<swiper-item v-for="(item,index) in bannerList" :key="index" :class="cardCur==index?'cur':''">
				<view @click="onClick(item)" class="swiper-item">
					<image :src="item.pictureUrl" mode="aspectFill" ></image>
				</view>
			</swiper-item>
		</swiper>
		<view class="hcc mt20">
			<view v-for="(item,index) in bannerList" :key="index">
				<view :class="index == cardCur ? ' active' : 'dot'"></view>
			</view>
		</view>
	</view>
</template>
<script>
// 首页的swiper 可前往uni-app 的插件市场处理
export default {
	props: {
		list:{
			type: Array
		},
		isAutoPlay: {
			type: Boolean
		}
	},
	data() {
		return {
			cardCur: 0,
			dotStyle: false,
			towerStart: 0,
			direction: '',
			bannerList: []
		};
	},
	created() {
		this.bannerList = this.list
		this.towerSwiper('bannerList');
		// 初始化towerSwiper 传已有的数组名即可
	},
	methods: {
		DotStyle(e) {
			this.dotStyle = e.detail.value;
		},
		// cardSwiper
		cardSwiper(e) {
			this.cardCur = e.detail.current;
		},
		// cardSwiper
		// cardSwiper(e) {
		// 	this.cardCur = e.detail.current;
		// },
		// towerSwiper
		// 初始化towerSwiper
		towerSwiper(name) {
			let list = this[name];
			for (let i = 0; i < list.length; i++) {
				list[i].zIndex = parseInt(list.length / 2) + 1 - Math.abs(i - parseInt(list.length / 2));
				list[i].mLeft = i - parseInt(list.length / 2);
			}
			this.bannerList = list;
		},
		// towerSwiper触摸开始
		TowerStart(e) {
			this.towerStart = e.touches[0].pageX;
		},
		// towerSwiper计算方向
		TowerMove(e) {
			this.direction = e.touches[0].pageX - this.towerStart > 0 ? 'right' : 'left';
		},
		// towerSwiper计算滚动
		TowerEnd(e) {
			let direction = this.direction;
			let list = this.bannerList;
			if (direction == 'right') {
				let mLeft = list[0].mLeft;
				let zIndex = list[0].zIndex;
				for (let i = 1; i < this.bannerList.length; i++) {
					this.bannerList[i - 1].mLeft = this.bannerList[i].mLeft;
					this.bannerList[i - 1].zIndex = this.bannerList[i].zIndex;
				}
				this.bannerList[list.length - 1].mLeft = mLeft;
				this.bannerList[list.length - 1].zIndex = zIndex;
			} else {
				let mLeft = list[list.length - 1].mLeft;
				let zIndex = list[list.length - 1].zIndex;
				for (let i = this.bannerList.length - 1; i > 0; i--) {
					this.bannerList[i].mLeft = this.bannerList[i - 1].mLeft;
					this.bannerList[i].zIndex = this.bannerList[i - 1].zIndex;
				}
				this.bannerList[0].mLeft = mLeft;
				this.bannerList[0].zIndex = zIndex;
			}
			this.direction = '';
			this.bannerList = this.bannerList;
		},
		onClick(obj) {
			this.$emit('click',{
				pictureId: obj.pictureId,
				linkUrl: encodeURIComponent(obj.linkUrl)
			})
		}
	}
};
</script>
<style lang="scss" scoped>
/* ==================
         轮播
 ==================== */
 
.tower-swiper .tower-item {
	transform: scale(calc(0.5 + var(--index) / 10));
	margin-left: calc(var(--left) * 100upx - 150upx);
	z-index: var(--index);
}
swiper.square-dot .wx-swiper-dot {
	background-color: #fff;
	opacity: 0.4;
	width: 10upx;
	height: 10upx;
	border-radius: 20upx;
	transition: all 0.3s ease-in-out 0s;
}

swiper.square-dot .wx-swiper-dot.wx-swiper-dot-active {
	opacity: 1;
	width: 30upx;
}

swiper.round-dot .wx-swiper-dot {
	width: 10upx;
	height: 10upx;
	top: -4upx;
	transition: all 0.3s ease-in-out 0s;
	position: relative;
}

swiper.round-dot .wx-swiper-dot.wx-swiper-dot-active::after {
	content: '';
	position: absolute;
	width: 10upx;
	height: 10upx;
	top: 0upx;
	left: 0upx;
	right: 0;
	bottom: 0;
	margin: auto;
	background-color: #fff;
	border-radius: 20upx;
}

swiper.round-dot .wx-swiper-dot.wx-swiper-dot-active {
	width: 18upx;
	height: 18upx;
	top: 0upx;
}

.screen-swiper {
	min-height: 375upx;
}

.screen-swiper image,
.screen-swiper video,
.swiper-item image,
.swiper-item video {
	width: 100%;
	display: block;
	height: 100%;
	margin: 0;
	pointer-events: none;
}

.card-swiper {
	height: 340upx;
	width: 100%;
}

.card-swiper swiper-item {
  width: 610upx !important;
  left: 70upx;
  box-sizing: border-box;
  overflow: initial;
}

.card-swiper swiper-item .swiper-item {
	width: 100%;
	display: block;
	height: 100%;
	border-radius: 10upx;
	transform: scale(0.9);
	transition: all 0.2s ease-in 0s;
	overflow: hidden;
}

.card-swiper swiper-item.cur .swiper-item {
	transform: none;
	transition: all 0.2s ease-in 0s;
}

.tower-swiper {
	height: 420upx;
	position: relative;
	max-width: 750upx;
	overflow: hidden;
}

.tower-swiper .tower-item {
	position: absolute;
	width: 300upx;
	height: 380upx;
	top: 0;
	bottom: 0;
	left: 50%;
	margin: auto;
	transition: all 0.2s ease-in 0s;
	opacity: 1;
}

.tower-swiper .tower-item.none {
	opacity: 0;
}

.tower-swiper .tower-item .swiper-item {
	width: 100%;
	height: 100%;
	border-radius: 6upx;
	overflow: hidden;
}
 
/*未选中时的小圆点样式 */

.dot {
	width: 16upx;
	height: 16upx;
	border-radius: 14upx;
	margin-right: 26upx;
	background: rgba(212, 214, 231, 1);
	border-radius: 50%;
}

/*选中以后的小圆点样式  */

.active {
	width: 24px;
	height: 9px;
	background: rgba(80, 139, 255, 1);
	border-radius: 6px;
	margin-right: 26upx;
}
</style>

<template>
	<scroll-view scroll-x class="mb20"
	scroll-with-animation :scroll-left="scrollLeft" @scroll="scrollTolower">
		<view class="item-news bgf item rad10">
			<view class="mw mh inline-block left-news">
				<view @click="toDetail(item)" class="mw rel hbc mh rad10">
					<view class="w120 h120 mr30 hcc f28 round cf" :class="[item.readState == 1?'bgec':'main-bg']">{{item.businessTypeDesc}}</view>
					<view class="content vlb mh pr10">
						<text class="f32pc mw text1">{{item.msgTitle}}</text>
						<text class="f28pc4 mw text1">{{item.msgContent}}</text>
						<text class="hrc f30pc4 mw">{{item.createDate}} {{item.createTime}}</text>
					</view>
					<view v-if="item.readState == 0" class="abs top10 left10 w20 h20 main-bg round"></view>
				</view>
			</view>
			<view class="mh inline-block right-news" @click="deleteNews(item.recordId)">
				<view class="delete mh hcc">
					删除
				</view>
			</view>
		</view>
	</scroll-view>
</template>

<script>
	export default {
		data() {
			return {
				// scrollLeft : 0
			};
		},
		props: {
			item: {
				type: Object
			},
			scrollLeft : {
				type : Number,
				default : 0
			}
		},
		methods: {
			toDetail(item) {
				this.$emit('detail', item)
			},
			deleteNews (recordId) {
				this.$emit('deleteNews', recordId)
			},
			scrollTolower (e) {
				console.log(e);
				this.scrollLeft = e.mp.detail.scrollLeft
			}
		},
	}
</script>

<style lang="scss">
	scroll-view{
		width: 100%;
		height: 170upx;
		white-space: nowrap;
	}
	.item-news{
		height: 170upx;
		width: 100.2%;
	}
	.inline-block{
		display: inline-block;
	}
	.item {
		box-shadow: 0px 2px 24px 0px rgba(185, 185, 185, 0.48);
	}
	.content {
		width: 500upx;
	}
	.delete{
		width: 150upx;
		color: #FFF;
		background-color: red;
	}
</style>

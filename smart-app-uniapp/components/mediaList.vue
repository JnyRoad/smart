<template>
	<view>
		<view @click="onClick(item)" v-for="(item,index) in list" :key="index" class="list-cell mb60" >
			<view class="media-list" >
				<view class="hlc">
					<view class="image-section image-section-left">
						<image class="image-list1 image-list2" 
						 :src="item.titleImage"></image>
					</view>
					<view class="f1 image-section-right">
						<view class="text2 media-title f30pc">{{item.newsTitle}}</view>
						<view class="hrc f28pc4">
							{{item.date}}
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	// 媒体组件  可到uni-app官网去下载
	export default {
		props: {
            list:{
                type: Array
            },
        },
		methods: {
			// 将控制权交个调用者
			onClick(item) {
				this.$emit('click', item)
			}
		}
	}
</script>

<style scoped>

	.h272{
		height: 272upx;
	}

	.list-cell {
		width: 750upx;
		padding: 0 30upx;
		height: 242upx;
	}

	.media-list {
		flex: 1;
		flex-direction: column;
		padding: 30upx 26upx;
		background: rgba(255,255,255,1);
		box-shadow:0px 0px 18px 0px rgba(216,216,216,0.38);
		border-radius:10upx;
	}


	.media-title {
		flex: 1;
		line-height: 40upx;
	}

	.image-section {
		margin-top: 20upx;
		flex-direction: row;
		justify-content: space-between;
	}

	.image-section-right {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		margin-top: 0upx;
		margin-left: 10upx;
		width: 225upx;
		height: 180upx;
	}

	.image-section-left {
		margin-top: 0upx;
		margin-right: 30upx;
		width: 241upx;
		height: 213upx;
	}

	.image-list1 {
		width: 690upx;
		height: 481upx;
	}

	.image-list2 {
		width: 241upx;
		height: 213upx;
	}

	.image-list3 {
		width: 241upx;
		height: 213upx;
	}

	.media-info {
		flex-direction: row;
	}

	.info-text {
		margin-right: 20upx;
		margin-top:60upx;
		color: #999999;
		font-size: 24upx;
	}

	.media-foot {
		margin-top: 20upx;
		flex-direction: row;
		justify-content: space-between;
	}

	.max-close-view {
		align-items: center;
		justify-content: flex-end;
		flex-direction: row;
		height: 40upx;
		width: 80upx;
	}

	.close-view {
		border-style: solid;
		border-width: 1px;
		border-color: #999999;
		border-radius: 10upx;
		justify-content: center;
		height: 30upx;
		width: 40upx;
		line-height: 30upx;
	}

	.close {
		text-align: center;
		color: #999999;
		font-size: 28upx;
	}
</style>

<template>
	<view class="yt-page bgf">
		<view class="hbc pr30 pl30 pt36 pb36 border-bottom-D">
			<view class="w135 h135 round overflow borderC"><image :src="face" mode=""></image></view>
			<view @click="photo" class="cutimg">拍照</view>
		</view>
		<view class="">
			<view @click="toDetail" v-if="hasList&&list.length>0"><manage-item :list="list" checked="false" /></view>
			<yt-null v-if="!hasList" notice="很遗憾,没有找到符合条件的简历"></yt-null>
		</view>
		<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="200" :maxWidth="1000" :ql="0.8" type="base64" :sourceType="['camera']"></cpimg>
		<yt-loading />
	</view>
</template>

<script>
import ytButton from '@/components/yt-button/index.vue';
import manageItem from '@/components/manage-item.vue';
import ytNull from '@/components/yt-null/yt-null.vue';
import cpimg from '@/components/cpimg.vue'
import recruit from '@/api/api-recruitment-management.js';
export default {
	components: {
		'yt-button': ytButton,
		'manage-item': manageItem,
		'cpimg': cpimg,
		'yt-null': ytNull
	},
	data() {
		return {
			face: '',
			hasList: true,
			list: [],
			temList: [],
			facePhoto: ''
		};
	},
	onLoad() {
		
	},
	methods: {
		// 获取简历
		async getApplicationFace () {
			this.hasList = false
			this.list = []
			try{
				const res = await recruit.applicationFaceList({facePhoto: this.facePhoto})
				if (!res) return
				if (res.data.data === null || res.data.data === '') {
					this.hasList = false
				} else {
					this.hasList = true
					this.list.push(res.data.data)
				}
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		photo () {
			this.list = []
			this.$refs.cpimg._changImg()
		},
		cpimgOk(file) {
			if (file) {
				this.face = file
				this.facePhoto = file.split(',')[1]
				this.getApplicationFace()
			} else {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理失败，请重试'
				})
			}
		},
		cpimgErr(e) {
			if (e) {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理失败,请重试'
				})
				console.log(e);
				throw e
			}
		},
		toDetail() {
			uni.navigateTo({
				url: `../recruit/recruit-detail?applicationId=${this.list[0].applicationId}`
			});
		}
	}
};
</script>

<style scoped>
	.cutimg {
		width: 230upx;
		height: 88upx;
		line-height: 88upx;
		text-align: center;
		background-color: #fff;
		border: 2upx solid #508BFF;
		color: #508BFF;
		border-radius: 444upx;
	}
</style>

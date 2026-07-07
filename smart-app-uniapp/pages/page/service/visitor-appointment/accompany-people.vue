<template>
	<view class="yt-page bgec">
		<view v-if="peoples.length && peoples.length > 0" class="hlc fw pt30 pr30 pb30 pl30 bgf">
			<view @click="showBigImage(item.memberPhoto)" class="item vcc mb20 rel" v-for="(item, index) in peoples" :key="index">
				<view class="w150 h155"><image lazy-load :src="item.memberPhoto" mode=""></image></view>
				<text class="f30pc2">{{ item.memberName }}</text>
				<text v-if="type == 1 || type == -1" @click.stop="deleteImg(index)" class="delete err-bg w40 h40 abs round cf hcc">-</text>
			</view>
		</view>
		<view v-if="type == 1 || type == -1" class="hcc mw mt80 mb60">
			<yt-button @click="toAdd" btnText="添加随行人员" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<view v-if="type == 1 || type == -1" class="hcc mw mt80 mb60"><yt-button @click="confirmAccompany" btnText="确定" btnWidth="180" btnHeight="54"></yt-button></view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytbutton from '@/components/yt-button/index.vue';
import { ACCOMPANYPEOPLE, storage } from '@/tools/storage.js';
import {base64ToPath} from 'image-tools'
import visit from '@/api/api-visitor.js';
export default {
	components: {
		'yt-button': ytbutton
	},
	data() {
		return {
			peoples: [],
			type: -1,
			visitId: '',
		};
	},
	onLoad(e) {
		if (e.type) this.type = e.type;
		if (e.visitId) this.visitId = e.visitId;
		if (Number(this.type) === 1 || this.type === -1) {
			this.getLocalAccompanyPeople();
		} else if (Number(this.type) === 2) {
			this.getNetAccompanyPeople();
		}
	},
	methods: {
		// 删除随行人员
		deleteImg(index) {
			const visitorName = this.peoples.map((el, inx) => {
				if (index === inx) {
					return el.visitorName;
				}
			});
			if (!visitorName) {
				this.$ytHint.toast({
					title: '该随行人员不存在'
				});
				return;
			}
			this.peoples.splice(index, 1);
			storage.setSync(ACCOMPANYPEOPLE, this.peoples)
		},
		// 如果是本地读取数据
		getLocalAccompanyPeople() {
			this.peoples = storage.getSync(ACCOMPANYPEOPLE)
			this.peoples.forEach(el => {
				if (el.memberPhoto.indexOf('base64') === -1) {
					el.memberPhoto = `data:image/png;base64,${el.memberPhoto}`
				}
			})
		},
		async getNetAccompanyPeople() {
			try{
				const obj = {
					visitId: this.visitId
				}
				const res = await visit.memberList(obj);
				this.peoples = res.data.data.records;
				if (this.peoples === null) this.peoples = []
			}catch(e){
				console.log(e);
				throw e
			}
		},
		// 点击预览图片
		showBigImage(imgUrl) {
			uni.previewImage({
				current: '0',
				urls: [imgUrl]
			});
		},
		confirmAccompany() {
			this.peoples.forEach(el => {
				el.memberPhoto = el.memberPhoto.split(',')[1]
			})
			storage.setSync(ACCOMPANYPEOPLE, this.peoples)
			uni.redirectTo({
				url: './add-visitor'
			})
		},
		toAdd() {
			uni.redirectTo({
				url: './add-accompany-people'
			})
		}
	}
};
</script>

<style scoped>
.item {
	width: 25%;
}
.item:nth-child(4n) {
	margin-right: 0;
}
.delete {
	right: -10upx;
	top: -10upx;
	z-index: 999;
}
</style>

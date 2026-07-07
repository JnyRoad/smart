<template>
	<view class="yt-page bgf">
		<view class="yt-content  pt40 pl30 pr30">
			<text class="f32pc">离职主要原因 (单选)</text>
			<view class="hbc fw mw  mt40">
				<block v-for="(item, index) in reason" :key="index">
					<view @click="getCurIndex(index)" :class="['item hcc w270 h120 rad10  f32pc4   mb20', curIndex === index ? 'main-bg cf' : 'bgf borderB']">{{ item }}</view>
				</block>
			</view>
		</view>
		<view class="hcc abs bottom200 left-50"><yt-button navUrl="" btnText="确定" btnWidth="180" btnHeight="54"></yt-button></view>
	</view>
</template>

<script>
import ytbutton from '@/components/yt-button/index.vue';
export default {
	components: {
		'yt-button': ytbutton
	},
	data() {
		return {
			reason: ['自离', '辞退', '其他'],
			curIndex: 0
		};
	},
	methods: {
		getCurIndex(index) {
			console.log(index);
			this.curIndex = index;
		}
	}
};
</script>

<style lang="scss" scoped>
.yt-content {
	.item {
	}
}
</style>

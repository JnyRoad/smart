<template>
	<view class="yt-page bge">	
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item @inputTxt="getData" hasTip=false showImage=false showArrow=false showInput=true title="填入新密码"></yt-list-item>
			<yt-list-item @inputTxt="getData" hasTip=false showImage=false showArrow=false  showInput=true title="确认新密码"></yt-list-item>
		</yt-list>
		<view class="hcc mt80">
			<yt-button navUrl="./setting-success" btnText="下一步" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue';
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-button': ytbutton
		},
		methods: {
			getData(e) {
				console.log(e);
			}
		},
	}
</script>

<style lang="scss" scoped>
</style>

<template>
	<view class="mw pl30 pr30 pt20 bgf">
		<view @click="toDetail(1)" class="card hbc red-linear">
			<image class="img" src="/static/img/serive/zhufang-icon.png" mode=""></image>
			<view class="cf text f32">东莞住房公积金查询</view>
		</view>
		<view @click="toDetail(2)" class="card hbc orange-linear">
			<image class="img" src="/static/img/serive/shebao-icon.png" mode=""></image>
			<view class="cf f32">东莞社保查询</view>
		</view>
		<view @click="toDetail(3)" class="card hbc red-linear">
			<image class="img" src="/static/img/serive/zhufang-icon.png" mode=""></image>
			<view class="cf text f32">深圳住房公积金查询</view>
		</view>
		<view @click="toDetail(4)" class="card hbc orange-linear">
			<image class="img" src="/static/img/serive/shebao-icon.png" mode=""></image>
			<view class="cf f32">深圳社保查询</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				linkList: [
					{
						id: 1,
						title: '东莞住房公积金查询',
						link: 'http://wb.dggjj.dg.gov.cn/web_psn/websys/pages/psncollquery/psnCollDetail/psnInfoQryOnline.jsp'
					},
					{
						id: 2,
						title: '东莞社保查询',
						link: 'http://dghrss.dg.gov.cn/grcx/'
					},
					{
						id: 3,
						title: '深圳住房公积金查询',
						link: 'http://gjj.sz.gov.cn/fzgn/zfcq/'
					},
					{
						id: 4,
						title: '深圳社保查询',
						link: 'https://sipub.sz.gov.cn/hspms/'
					}
				]
			}
		},
		onLoad() {
			
		},
		methods: {
			toDetail(type) {
				let link = ''
				this.linkList.forEach(el => {
					if (el.id == type) {
						plus.runtime.openURL( el.link );
					}
				})
			}
		},
	}
</script>

<style lang="scss" scoped>
	.card {
		width: 690upx;
		height: 200upx;
		border-radius: 20upx;
		margin: 0 auto 20upx;
		padding: 30upx;
		&.red-linear {
			background-image: linear-gradient(to right, #ffd0bb,#ff887f);
		}
		&.orange-linear {
			background-image: linear-gradient(to right, #ffe0bb,#ffac7f);
		}
		.img {
			width: 218upx;
			height: 116upx;
		}
		.text {
			width: 160upx;
			text-align: center;
			word-break: break-all;
		}
	}
</style>

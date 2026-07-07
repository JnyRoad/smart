<template>
	<view>
		<swiper class="swiper" @change="changeIndicatorDots">
			<swiper-item class="swiper-item" v-for="(item, index) in guideList" :key="index">
				<view class="mw mh rel"><image class="item-image abs left0 top0" :src="item" mode=""></image></view>
			</swiper-item>
		</swiper>
	</view>
</template>

<script>
import { ISFIRSTENTER, storage } from '@/tools/storage.js';
import guide from '@/api/api-guide.js'
export default {
	data() {
		return {
			indicatorDots: false,
			guideList: ['/static/img/start/guide1.jpg', '/static/img/start/guide2.jpg', '/static/img/start/guide3.jpg']
		};
	},
	onLoad() {
		const isFirstEnter = storage.getSync(ISFIRSTENTER) // 不是第一次 为 'false'
		const USERTOKEN = storage.getSync(storage.USER_TOKEN) // 不是第一次 为 'false'
		if (isFirstEnter == 'false') {
			if (!USERTOKEN){
				uni.reLaunch({
					url: '../logins/login/login'
				})
			}else {
				return
			}
		}
	},
	methods: {
		async getWelcomeInfo() {
			const res = await guide.getWelcomeInfo()
			this.guideList = res.data.records.map(el => {
				return el.pictureUrl
			})
		},
		changeIndicatorDots(e) {
			if (e.detail.current === 2) {
				storage.setSync(ISFIRSTENTER, 'false'); // 刷过欢迎图 就不是第一次登陆了
				setTimeout(function () {
					// 如果第一次进入app 进行完引导操作完后 存储值 下次再进入无需过引导图
					uni.reLaunch({
						url: '../logins/login/login'
					});
				}, 1000);
			}
		},
		// 登录成功将信息存进本地
		savaLoginData(data) {
			const token = data.access_token;
			const refToken = data.refresh_token
			const userID = data.user_id;
			const userName = data.username
			const tokenTimeout = (new Date()).getTime() + parseInt(`${data.expires_in}000`)
			storage.set(storage.USER_TOKEN, token);
			console.log('退出登录');
			storage.set(storage.USER_ID, userID);
			storage.set(storage.USER_NAME, userName);
			storage.set(storage.EXPIRES_IN, tokenTimeout)
			storage.set(storage.REFRESH_TOKEN, refToken)
		},
	}
};
</script>

<style lang="scss">
.swiper,
.item-image .swiper-item {
	width: 100vw;
	height: 100vh;
}
</style>

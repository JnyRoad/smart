<script>
	import {
		ISFIRSTENTER,
		storage
	} from '@/tools/storage.js';
	import loginAccount from '@/api/api-account.js' // 刷新token
	import { IS_FORCED } from '@/config/env.js' // 环境强制覆盖标记（防呆提示用）
	export default {
		onLaunch: function() {
			console.log('App onLaunch');
			// 防呆：FORCE_ENV 强制指向测试环境却用「发行」打包时，启动即阻断式提醒，
			// 防止连测试环境的包被误发到生产（发版检查单见 config/env.js 头部注释）
			if (IS_FORCED && process.env.NODE_ENV === 'production') {
				uni.showModal({
					title: '测试环境包',
					content: '当前包强制连接测试环境（FORCE_ENV 已开启）。若这是生产发布，请立即停止分发并回收。',
					showCancel: false
				})
			}
			if (typeof plus !== 'undefined' && plus.screen) {
				plus.screen.lockOrientation("portrait-primary");
			}
			const that = this
			const isFirstEnter = storage.getSync(ISFIRSTENTER) // 不是第一次 为 'false'
			console.log(isFirstEnter);
			if (isFirstEnter == 'false') { // 不是第一次 直接去首页 // 在首页直接请求刷新token
				const refToken = storage.getSync(storage.REFRESH_TOKEN)
				const loginTimeOut = storage.getSync(storage.EXPIRES_IN) || 0
				console.log(refToken);
				console.log(loginTimeOut);
				if (!refToken || !loginTimeOut) {
					uni.reLaunch({
						url: '/pages/page/logins/login/login'
					})
					return
				}
				const curTime = new Date().getTime()
				if ((curTime > loginTimeOut)) {
					this.getRefreshToken(refToken)
				} else {
					// console.log('没有出问题了');
					uni.reLaunch({
						url: '/pages/page/tabbar/home/home'
					})
					return
				}
			}
			// #ifdef APP-PLUS  
			// 监听点击消息事件
			plus.push.addEventListener("click", function(msg) {
				uni.switchTab({
					url: '/pages/page/tabbar/news/news'
				})
			}, false);
			// #endif
		},
		onShow: function() {
			console.log('App Show');
			console.log('????');
		},
		onHide: function() {
			console.log('App Hide');
		},
		methods: {
			// 刷新token
			getRefreshToken(refToken) {
				const obj = {
					refresh_token: refToken
				}
				loginAccount.refreshToken(obj).then(res => {
					console.log(obj);
					console.log(res);
					if (!res) {
						uni.reLaunch({
							url: '/pages/page/logins/login/login'
						})
						return
					}
					if (res.status === 200) {
						this.savaLoginData(res.data)
						setTimeout(() => {
							uni.switchTab({
								url: '/pages/page/tabbar/home/home'
							})
						}, 200)
					}
				}).catch(err => {
					console.log(err);
					throw err
				})
			},
			// 刷新token信息存进本地
			savaLoginData(data) {
				const tokenTimeout = (new Date()).getTime() + parseInt(`${data.expires_in}000`)
				storage.set(storage.USER_TOKEN, data.access_token) // 登录令牌
				storage.set(storage.REFRESH_TOKEN, data.refresh_token) // 刷新令牌
				storage.set(storage.USER_ID, data.user_id) // 用户id 暂存
				storage.set(storage.USER_NAME, data.username) // 员工号
				storage.set(storage.EXPIRES_IN, tokenTimeout) // token失效时间
			},
		},
	};
</script>

<style lang="scss">
	@import './common/css/uni.css';
	@import './components/gaoyia-parse/parse.css';
	@import './common/css/icon.css';
	@font-face {
		font-family: 'iconfont';
		src: url('./static/img/iconfont/iconfont.ttf') format('truetype');
	}

	.iconfont {
		font-family: iconfont;
		margin-left: 20upx;
		display: flex;
		flex-direction: row;
		justify-content: flex-start;
	}

	/*每个页面公共css */
	/* uni-app默认全局使用flex布局。因为fle
* x布局有利于跨更多平台，尤其是采用原生渲染的平台。如不了解flex布局，请参考http://www.w3.org/TR/css3-flexbox/。如不使用flex布局，请删除或注释掉本行。*/
	body,
	page {
		min-height: 100%;
		display: flex;
		font-family: '微软雅黑';
		background-color: #ececec;
	}
	
	
	/* 一下为自定义导航栏的的样式  放在这里只为提高渲染速度 */
	.title-contents {
		height: calc(var(--status-bar-height) + 44px);
		.status {
			height: var(--status-bar-height);
		}
		.titles {
			height: 44px;
		}
		.top-view{  
		    width: 100%;  
		    position: fixed;  
		    top: 0;
		}
		.titleLeftButton {
			position: absolute;
			left: 30upx;
			width: 27upx;
			height: 44upx;
		}
		.titleRightButton {
			position: absolute;
			right: 30upx;
		}
		.titles{  
			position: relative;
			width: 100%;
		    top: var(--status-bar-height);  
		}
	}

	
</style>

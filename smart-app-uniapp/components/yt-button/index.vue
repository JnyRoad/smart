<template>
	<view @click="onClick" 
	class="yt-btn hcc cf f32" 
	:style="{width:btnWidth+'px',color:btnTextColor, height:btnHeight +'px',backgroundColor:btnBgColor,borderRadius:btnHeight/2 +'px'}"
	>{{btnText}}</view>
</template>

<script>
	export default {
		name: 'yt-botton',
		props: {
			btnText: { // 按钮的文本
				type: String
			},
			btnWidth: { // 按钮的宽度
				type: [String,Number],
				default: 180
			},
			btnHeight: { // 按钮的高度
				type: [String,Number],
				default: 54
			},
			btnBgColor: { // 按钮的背景色
				type: String,
				default: '#508BFF'
			},
			navUrl: { // 跳转的链接
				type: String,
				default: ''
			},
			btnTextColor: { // 按钮内文本的颜色
				type: String,
				default: '#fff'
			}
		},
		data () {
			return {
				url: this.navUrl
			}
		},
 		watch: {
			navUrl() {
				this.url = this.navUrl
			}
		},
		methods: {
			onClick() {
				// 如果navUrl为配置 将按钮的点击权限交个对应的逻辑处理
				if (this.url === '') {
					this.$emit('click')
				} else { // 区别跳转
					if (this.navUrl.indexOf('home') !== -1 || this.navUrl.indexOf('serive') !== -1 || this.navUrl.indexOf('news') !== -1 || this.navUrl.indexOf('mine') !== -1 ) {
						uni.switchTab({ // 路由跳转 关闭非tabbar页面
							url: this.navUrl
						})
					}else if (this.url.indexOf('login') !== -1) {
						uni.reLaunch({ // 关闭所有页面 打开对应页面
							url: this.navUrl
						})
					} else {
						uni.navigateTo({ // 打开页面 保留上一页
							url: this.navUrl
						})
					}
				}
			}
		},
	}
</script>

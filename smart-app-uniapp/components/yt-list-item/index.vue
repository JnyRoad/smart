<template>
	<view @click="onClick" :class="['list-item','hbc',hideLastBorder===true||hideLastBorder==='true'?'hide-border':'']">
		<view class="item-left h60 hlc">
			<text v-if="hasMust===true || hasMust === 'true'" class="require f36pc mr10">*</text>
			<view v-if="showImage === true || showImage === 'true'" class="image mr30">
				<image :src="image" mode=""></image>
			</view>
			<text v-if="hasTitle ===true || hasTitle === 'true'" class="title f30pc bold text1 w360">
				{{title}}
			</text>
		</view>
		<view class="item-right hrc h60 f1 rel">
			<view v-if="hasTip === true || hasTip === 'true'" class="tips tr mw">
				<text v-if="tipContent !== ''" class="f28pc2 tr">
					{{tipContent}}
				</text>
				<!-- 额外的扩展 -->
				<slot></slot> 
			</view>
			<input @blur="getData" class="input f28pc2 mh tr" confirm-type="done" placeholder="请输入" v-model="txt" v-if="showInput === true || showInput === 'true'"></input>
			<view class="ml10 w20 h30 hcc" v-if="showArrow === true || showArrow === 'true'">
				<image src="/static/img/mine/next.png" mode=""></image>
			</view>
			<view v-if="hasRightIcon === true || hasRightIcon === 'true'" class="right-icon">
				<image :src="rightIcon" mode=""></image>
			</view>
			<view v-if="hasRightImage === true || hasRightImage === 'true'" class="right-image w90 h60 overflow">
				<image v-if="rightImage" :src="rightImage" mode=""></image>
			</view>
		</view>
	</view>
</template>

<script>
	import uniIcon from '@/components/uni-icon/uni-icon.vue'
	export default {
		components: {
			'uni-icon': uniIcon
		},
		data() {
			return {
				txt: '' ,// 绑定的输入框中的内容
			}
		},
		props: {
			idType: { // 内容唯一标致符
				type: String,
				default: ''
			},
			showImage: { // 是否显示左侧图片
				type:[Boolean,String],
				default: true
			},
			image: { // 左侧图片
				type: String,
				default: ''
			},
			hasTitle: {
				type: [Boolean,String],
				default: true
			},
			title: { // 每一项的主标题
				type: String,
				default: ''
			},
			hasTip: { // 是否显示内容角标
				type: [Boolean,String],
				default: true
			},
			tipContent: { // 内容角标
				type: String,
				default: ''
			},
			showArrow: { // 是否显示next箭头
				type: [Boolean,String],
				default: true
			},
			navUrl: { // 跳转的路径地址
				type: String,
				default: ''
			},
			showInput: { // 是否显示输入框
				type: [Boolean,String],
				default: false
			},
			inputContent: { // 输入的内容
				type: String,
				default: ''
			},
			hideLastBorder: { // 是否隐藏底部border
				type: [Boolean,String],
				default: false
			},
			hasMust: { // 是否显示 *
				type: [Boolean,String],
				default: false
			},
			hasRightIcon: { // 右侧是否有图标
				type: [Boolean,String],
				default: false
			},
			rightIcon: { // 图标路径
				type: String,
				default: ''
			},
			hasRightImage: { // 右侧是否有图片
				type: [Boolean,String],
				default: false
			},
			rightImage: { // 右侧图片路径
				type: String,
				default: ''
			}
		},
		methods: {
			onClick() { // 判断执行跳转
				if (this.navUrl === '') { 
					this.$emit('click',{ // 没有跳转路径 将内容返回给父组件 绑定click事件
						title: this.title,
						tipContent: this.tipContent,
						type: this.idType
					})
				} else { // 执行跳转
					uni.navigateTo({
						url: this.navUrl
					})
				}
			},
			getData() { // 返回输入的内容给父组件 绑定的inputTxt事件
				this.$emit('inputTxt',{
					text: this.txt,
					idType: this.idType
				})
			}
		},
	}
</script>

<style lang="scss" scoped>
.list-item {
	flex: 1;
	padding: 30upx 0;
	height: 120upx;
	border-bottom: 2upx solid $border-color;
	.image {
		width: 56upx;
		height: 56upx;
		overflow: hidden;
	}
	.require {
		color: red;
	}
	.right-icon {
		width: 40upx;
		height: 40upx;
	}
	&.hide-border {
		&:last-child {
			border-bottom: 0 none;
		}
	}
}
</style>

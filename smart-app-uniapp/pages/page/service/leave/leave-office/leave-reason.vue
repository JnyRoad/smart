<template>
	<view class="yt-page bgf">
		<view class="yt-content vlc pt40 pl30 pr30">
			<text class="f32pc bold">离职主要原因 (可多选)</text>
			<view class="hbc fw mw  mt40">
				<block v-for="(item, index) in reason" :key="index">
					<view @click="getCurIndex(index,1)" :class="['item sizing hcc w270 h120 rad20  f32pc4 pt20 pr20 pb20 pl20 mb20', item.isSelect? 'main-bg cf' : 'bgf borderB']">{{ item.msg }}</view>
				</block>
			</view>
			<text class="f32pc bold mb40">其他</text>
			<view class="msg hcc h180 pt20 pr20 pb20 pl20 rad20 overflow rel auto">
				<textarea class="mw mh" maxlength="50" placeholder="请具体说明" />
				<text class="abs right30 bottom30 f28pc4">(不超过50字)</text>
			</view>
			<view class="line mw hei2 bgec mt40"></view>
			<text class="f32pc bold mt40">如果公司改善哪一方面，您会继续留在公司工作（可多选）</text>
			<view class="hbc fw mw  mt40">
				<block v-for="(item, index) in suggest" :key="index">
					<view @click="getCurIndex(index,2)" :class="['item sizing hcc w270 h120 rad20  f32pc4 pt20 pr20 pb20 pl20 mb20', item.isSelect? 'main-bg cf' : 'bgf borderB']">{{ item.msg }}</view>
				</block>
			</view>
		</view>
		<view class="hcc mt40 mb40" @click="backTap"><yt-button  btnText="确定" btnWidth="180" btnHeight="54"></yt-button></view>
	</view>
</template>

<script>
import ytbutton from '@/components/yt-button/index.vue';
import dimission  from '@/api/api-dimission.js';
export default {
	components: {
		'yt-button': ytbutton
	},
	data() {
		return {
			reason: [
				{
					msg:'不满意公司 制度 政策',
					isSelect: false
				},
				{
					msg:'不适应公司文化',
					isSelect: false
				},
				{
					msg:'不满意主管管理风格',
					isSelect: false
				},{
					msg:'办公司气氛不佳同事关系不和谐',
					isSelect: false
				},{
					msg:'个人原因',
					isSelect: false
				}
			],
			suggest: [
				{
					msg:'提高薪资/福利待遇',
					isSelect: false
				},
				{
					msg:'制定个人职业发展规划',
					isSelect: false
				},
				{
					msg:'提供升迁机会',
					isSelect: false
				},
				{
					msg:'调换部门/主管领导',
					isSelect: false
				},
				{
					msg:'提供更多培训和学习机会',
					isSelect: false
				},
				{
					msg:'改善工作坏境/食宿',
					isSelect: false
				}
			],
			curIndex: 0
		};
	},
	methods: {
		getCurIndex(index,type) {
			let list = []
			if (type === 1) {
				list = this.reason
			} else if (type === 2) {
				list = this.suggest
			}
			list.forEach((el,inx)=>{
				if (inx === index) {
					el.isSelect = !el.isSelect
				}
			})
		},
		// 返回上一级
		backTap(){
			uni.navigateBack({
				delta: 1
			})
		}
	}
};
</script>

<style lang="scss" scoped>
.yt-content {
	.msg {
		width: 630upx;
		box-shadow: 0px 5upx 49upx 0px rgba(185, 185, 185, 0.48);
	}
}
</style>

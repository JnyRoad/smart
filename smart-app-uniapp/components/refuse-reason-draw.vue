<template name="recruit-flitter">
	<view v-if="visibleSync" :class="{ 'drawer-visible': showDrawer }" class="drawer">
		<view @click="close" class="drawer-mask"></view>
		<view class="drawer-content pr30 pl30 pb30 rel">
			<view class="hcc rel h80">
				<image @click="close" class="abs left0 w30 h30" src="/static/img/login/close.png" mode=""></image>
				<text class="f32pc">拒绝理由</text>
			</view>
			<scroll-view scroll-y class="sroll-view">
				<view class="sex vlc mb40">
					<text class="f32pc mb20">应聘者原因</text>
					<view class="hbc mw fw">
						<block v-for="(item, index) in reason1" :key="index">
							<view @click="selectReason1(index)" :class="{ 'main-bg cf': item.isSelect }" class="hcc item f32pc2 rad10 borderC">{{ item.text }}</view>
						</block>
					</view>
				</view>
				<view class="sex vlc mb40">
					<text class="f32pc mb20">公司原因</text>
					<view class="hbc mw">
						<block v-for="(item, index) in reason2" :key="index">
							<view @click="selectReason2(index)" :class="{ 'main-bg cf': item.isSelect }" class="hcc item f32pc2 rad10 borderC">{{ item.text }}</view>
						</block>
					</view>
				</view>
				<view class="sex vlc mb40">
					<text class="f32pc mb20">其他原因</text>
					<view class="hbc mw h200 borderC pr20 pl20 pt20 pb20 overflow rel rad10">
						<textarea @focus="clearSelectReason" v-model="otherReason" placeholder="如有其他原因请填写" maxlength="50" class="mh mw"></textarea>
						<text class="abs right20 bottom20 f30pc4">不超过50个字</text>
					</view>
				</view>
			</scroll-view>
			<view @click="submitData" class="btn pr30 pl30 abs bottom0 h80 main-bg cf f32 hcc rad10">确定</view>
		</view>
	</view>
</template>

<script>
// 招聘简历模块拒绝理由组件
export default {
	data() {
		return {
			showDrawer: false,
			visibleSync: false,
			watchTimer: null,
			isClickReason1: false,
			isClickReason2: false,
			reason1: [
				{
					text: '笔试成绩不合格',
					isSelect: false
				},
				{
					text: '视力不合格',
					isSelect: false
				},
				{
					text: '存在色弱色盲',
					isSelect: false
				},
				{
					text: '手脚灵活性不合格',
					isSelect: false
				},
				{
					text: '不符合岗位要求',
					isSelect: false
				},
				{
					text: '不愿前来',
					isSelect: false
				}
			], // 应聘者原因 暂时固定
			reason2: [
				{
					text: '职位已满',
					isSelect: false
				},
				{
					text: '暂时不招人',
					isSelect: false
				}
			], // 公司原因
			otherReason: '' ,// 其他原因.
			reasonList: []
		};
	},
	created() {
		this.visibleSync = this.visible;
		setTimeout(() => {
			this.showDrawer = this.visible;
		}, 100);
	},
	props: {
		visible: {
			type: Boolean,
			default: true
		}
	},
	watch: {
		// 延时弹出
		visible(val) {
			clearTimeout(this.watchTimer);
			setTimeout(() => {
				this.showDrawer = val;
			}, 100);
			if (this.visibleSync) {
				clearTimeout(this.closeTimer);
			}
			if (val) {
				this.visibleSync = val;
			} else {
				this.watchTimer = setTimeout(() => {
					this.visibleSync = val;
				}, 300);
			}
		}
	},
	methods: {
		// 原因选择 互斥
		selectReason1(inx) {
			if (!this.isClickReason1) {
				this.reasonList = []
				this.isClickReason1 = true
				this.isClickReason2 = false	
			}
			this.reason1.forEach((el, index) => {
				if (index === inx) {
					el.isSelect = !el.isSelect
					this.reasonList.push(el.text)
				}
			})
			this.reason2.forEach(el => {
				el.isSelect = false
			})
		},		
		selectReason2(inx) {
			if (!this.isClickReason2) {
				this.reasonList = []
				this.isClickReason2 = true
				this.isClickReason1 = false	
			}
			this.reason2.forEach((el, index) => {
				if (index === inx) {
					el.isSelect = !el.isSelect
					this.reasonList.push(el.text)
				}
			})
			this.reason1.forEach(el => {
				el.isSelect = false
			})
		},
		close() {
			this.showDrawer = false;
			this.resetData()
			this.closeTimer = setTimeout(() => {
				this.visibleSync = false;
				this.$emit('close', this.visibleSync);
			}, 200);
		},
		// 重置数据
		resetData() {
			this.reason1.forEach(el => {
				el.isSelect = false
			})
			this.reason2.forEach(el => {
				el.isSelect = false
			})
		},
		// 如果填写其他原因 无条件清除原因选择
		clearSelectReason() {
			this.resetData()
			this.reasonList = []
		},
		// 提交数据到父组件
		submitData() {
			let arr =  Array.from(new Set(this.reasonList));
			let str = this.otherReason
			this.close();
			this.resetData()
			this.otherReason = ''
			this.reasonList = []
			if (arr.length > 0) {
				this.$emit('reasonData',arr);
			} else if (str.length !== 0) {
				this.$emit('reasonData',str);
			}
			
		}
	}
};
</script>

<style lang="scss" scoped>
.drawer-visible {
	.drawer-mask {
		display: block;
		opacity: 1;
	}
	.drawer-content {
		transform: translateY(0);
	}
}
.drawer {
	position: fixed;
	width: 100%;
	height: 100%;
	top: 0;
	left: 0;
	overflow: hidden;

	z-index: 999;
	&-mask {
		display: block;
		opacity: 0;
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: rgba(0, 0, 0, 0.4);
		transition: opacity 0.3s;
	}
	&-content {
		display: block;
		position: absolute;
		bottom: 0;
		left: 0;
		width: 100%;
		height: 70%;
		background: #fff;
		transition: all 0.3s ease-out;
		transform: translateY(100%);
	}
	.item {
		width: 330upx;
		height: 60upx;
		margin-bottom: 20upx;
	}
}
.sroll-view {
	height: 80%;
}
.btn {
	width: 690upx;
	bottom: 20upx;
}
</style>

<template name="recruit-flitter">
	<view v-if="visibleSync" :class="{'drawer-visible':showDrawer}" class="drawer">
		<view @click="close" class="drawer-mask"></view>
		<view class="drawer-content pr30 pl30 pb30">
			<view class="hcc rel h80">
				<image @click="close" class="abs left0 w30 h30" src="/static/img/login/close.png" mode=""></image>
				<text class="f32pc">筛选</text>
			</view>
			<view class="sex vlc mb40">
				<text class="f32pc mb20">性别</text>
				<view class="hbc mw">
					<block v-for="(item,index) in sex" :key="index">
						<view @click="changeSexSelect(item.id)" :class="{'main-bg cf': item.isSelect}" class="hcc w200 h60 f32pc2 rad10 borderC">
							{{item.title}}
						</view>
					</block>
				</view>
			</view>
			<view class="sex vlc mb40">
				<text class="f32pc mb20">年龄 ({{ageRange[0]}} - {{ageRange[1]}})</text>
				<range-slider @rangechange="getAgeRange" :width="690" active-color="#508BFF" :values="ageRange" />
			</view>
			<view class="sex hrc h60 mb40">
				<view @click="restData" class="f32pc mr20 hcc w100 h60 rad10 borderC">重置</view>
			</view>
			<view @click="submitData" class="mw h80 main-bg cf f32 hcc rad10">
				确定
			</view>
		</view>
	</view>
</template>

<script>
// 招聘模块的简历塞选组件
import RangeSlider from '@/components/range-slider/range-slider.vue';	// 滑块slider
export default {
	name: 'recruit-flitter',
	components: {
		RangeSlider
	},
	data() {
		return {
			showDrawer: false,
			visibleSync: false,
			watchTimer: null,
			sexSelected: false,
			ageRange: [18, 50],
			sex: [
				{
					id: 3,
					title: '全部',
					isSelect: true
				},
				{
					id: 0,
					title: '男',
					isSelect: false
				},
				{
					id: 1,
					title: '女',
					isSelect: false
				}
			],
			gender: 3,
		};
	},
	created() {
		// 初始化数据
		this.visibleSync = this.visible
		setTimeout(() => {
			this.showDrawer = this.visible
		}, 100)
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
		// 改变性别塞选的id
		changeSexSelect(id) {
			for (let el of this.sex) {
				if (el.id === id) {
					el.isSelect = true
					this.gender = el.id
				} else {
					el.isSelect = false
				}
			}
		},
		// 改变年龄范围
		getAgeRange(e) {
			const that = this
			this.ageRange = new Array(e.minValue, e.maxValue)
		},
		// 关闭该组件
		close() {
			this.showDrawer = false
			this.closeTimer = setTimeout(() => {
				this.visibleSync = false
				this.$emit('close',this.visibleSync)
			}, 200)
		},
		// 重置数据
		restData() {
			this.ageRange = [18, 50]
			for (let el of this.sex) {
				if (el.id === 3) {
					el.isSelect = true
					this.gender = 3
				} else {
					el.isSelect = false
				}
			}
		},
		// 提交数据到父组件
		submitData() {
			const obj = {
				ageStart: this.ageRange[0],
				ageEnd: this.ageRange[1],
				gender: this.gender
			}
			this.close()
			this.$emit('fillterData',obj)
		}
	},
};
</script>

<style lang="scss" scoped>
.drawer-visible {
	
	.drawer-mask {
		display: block;
		opacity: 1
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
		height: 60%;
		background: #fff;
		transition: all 0.3s ease-out;
		transform: translateY(100%);
	}
}
</style>

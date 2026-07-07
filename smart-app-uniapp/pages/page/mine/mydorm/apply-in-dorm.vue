<template>
	<view class="yt-page">
		<view class="f32pc2 bold pt30 pr30 pb30 pl30">
			温馨提示: 系统会根据职层/房间空余/床位偏好,自动帮您分配
		</view>
		<view class="textarea vc auto" v-if="jobLeve == '员工' || jobLeve == '技工层'">
			<text class="f32pc bold mb60">选择你喜欢的床位类型</text>
			<view class="select hcc">
				<block v-for="(item, index) in bed" :key="index">
					<view @click="changeSelect(item.id,index)" :class="['item','mr20','ml20','hcc','f28pc4',curSelect===index?'active':'']">{{item.title}}</view>
				</block>
			</view>
		</view>
		<view class="hcc mt80">
			<yt-button @click="next" btnText="下一步" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytbutton from '@/components/yt-button/index.vue'
	import {employee} from '@/api/api-mine.js' // 请求接口的参数
	import { storage } from '@/tools/storage.js'
	import { mapState, mapMutations } from 'vuex';
	
	export default {
		components: {
			'yt-button': ytbutton
		},
		data () {
			return {
				bed: [{id: 1, title:'上铺'},{id:2, title:'下铺'}],
				curSelect: -1, // 当前选中
				bedTypeId: -1, // 床位类型
				jobLeve : '' //员工职位
		
			}
		},
		onLoad() {
			this.getfullInfo()
		},
		computed: {
			...mapState(['dormStatus'])
		},
		methods: {
			async getfullInfo () {
				try{
					const res = await employee.fullInfo()
					this.jobLeve = res.data.data.jobLeve
					console.log(this.jobLeve);
				}catch(e){
					//TODO handle the exception
					console.log(e);
				}
			},
			changeSelect(id,index) {
				this.curSelect = index
				this.bedTypeId = id
			},
			next () {
				if (this.dormStatus == 2) {
					this.$ytHint.toast({
						title: '您已经申请了外宿'
					})
					return
				}
				if (this.jobLeve != '员工' || this.jobLeve != '技工层') {
					uni.navigateTo({
						url: `./in-dorm-success?bedTypeId=${this.bedTypeId}`
					})
					return
				}
				if (this.curSelect === -1) {
					this.$ytHint.toast({
						title: '请选择床位'
					})
				} else {
					this.$ytHint.modal({
						content: '确定选好床位了吗?有疑问请前往宿管处',
						confirm: () => {
							uni.navigateTo({
								url: `./in-dorm-success?bedTypeId=${this.bedTypeId}`
							})
						}
					})
				}
				
				
				
			}
		},
	}
</script>

<style lang="scss" scoped>
	.yt-page {
		background-color: #fff;
		.textarea {
			width: 630upx;
			height: 266upx;
			padding: 30upx;
			border-radius: 10upx;
			box-shadow:0px 5upx 49upx 0px rgba(185,185,185,0.48);
			overflow: hidden;
			textarea {
				width: 100%;
				height: 100%;
			}
			.select {
				.item {
					width: 138upx;
					height: 70upx;
					border: 1upx solid $uni-border-color;
					border-radius: 10upx;
					overflow: hidden;
					&.active {
						background-color: $main-color;
						color: #fff;
					}
				}
			}
		}
	}
</style>

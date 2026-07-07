<template>
	<view class="yt-page bgf">
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item @click="getInput" idType="travelCity" hasMust showArrow=false title="出差地点" showImage=false :tipContent="travel.travelCity"></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item @click="onShowDatePicker('rangetime')" hasMust title="开始时间" showImage=false :tipContent="travel.startDate"></yt-list-item>
			<yt-list-item hasMust title="结束时间" showImage=false  :tipContent="travel.endDate"></yt-list-item>
			<yt-list-item showArrow=false hasMust title="时间" showImage=false>
				<view class="hec f28pc2">
					<text class="main-color f36 mh">{{travel.travelCount}}</text>
					<text>天</text>
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item @click="getInput" idType="travelkDesc"  showArrow=false title="出差事由" showImage=false  :tipContent="travel.travelkDesc" ></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-approval-process ></yt-approval-process>
		<view class="hcc mt60">
			<yt-button @click="apply" btnText="提交" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<mx-date-picker format="yyyy-mm-dd hh:ii:ss" :show="showPicker" :type="type" color:="#5c92ff" :show-tips="true" :begin-text="'开始'" :end-text="'结束'" :show-seconds="true"
		@confirm="onSelected" @cancel="onSelected" />
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue'
	import ytminemodel from '@/components/yt-mine-model.vue'
	import ytapprovalprocess from '@/components/yt-approval-process.vue'
	import travel from '@/api/api-travel.js'
	import approve from '@/api/api-approve.js'
	import hideModel from '@/mixins/hideModel.vue'
	export default {
		mixins:[hideModel],
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'mx-date-picker': MxDatePicker,
			'yt-button': ytbutton,
			'yt-mine-model': ytminemodel,
			'yt-approval-process':ytapprovalprocess
		},
		data () {
			return {
				modelObj: {
					title: '',
					input: '',
					idType: ''
				},
				showPicker: false,
				value: '',
				type: 'rangetime',
				startTime: '', // 开始时间
				endTime: '' ,// 结束时间
				day: '',
				travel: {
					startDate: '',
					endDate: '',
					travelCount: '',
					travelCity: '请输入',
					travelkDesc: '请输入'
				},
				travelType: [],
				travelTypeData: [],
				flows: []
			}
		},
		onNavigationBarButtonTap(e) {
			uni.navigateTo({
				url: './record'
			})
		},
		computed: {
			showModel() {
				return this.$store.state.modelStatus
			}
		},
		onLoad() {
			// this.getApprove()
		},
		methods: {
			async getApprove () {
				const obj = {
					processType: '6'
				}
				const res = approve.processPreview(obj)
				this.flows = res.data.data.rows
			},
			onShowDatePicker(type) {
				//显示
				this.showPicker = true;
				this.value = this[type];
			},
			getData (e) { // 获取请假事由
				switch (e.idType){
					case 'travelCity':
						this.travel.travelCity = e.value
						break;
					case 'travelkDesc':
						this.travel.travelkDesc = e.value
						break;	
					default:
						break;
				}
			},
			getInput(e) {
				this.$store.commit('ytModelStatus', true); // 显示弹出模态框
				this.modelObj = {
					title: e.title,
					input: e.tipContent,
					idType: e.type
				};
			},
			onSelected(e) {
				//选择
				this.showPicker = false;
				if (e) {
					this[this.type] = e.value
					this.travel.travelCount = (Date.parse(e.date[1]) - Date.parse(e.date[0]))/86400000
					this.travel.startDate = e.value[0]
					this.travel.endDate = e.value[1]
				}
			},
			apply() {
				let flag = true
				Object.keys(this.travel).forEach(el=>{
					if (this.travel[el].length === 0 || this.travel[el] === '请输入') {
						flag = false
					}
				})
				if (!flag) {
					this.$ytSystem.vibrate({
						title: '请输入完整'
					})
					return
				}
				uni.redirectTo({
					url: './record',
					success: () => {
						// do delete data
						// this.travel = {}
					}
				})
				// travel.travelApply(this.travel).then(res => {
				// 	uni.redirectTo({
				// 		url: './record',
				// 		success: () => {
				// 			// do delete data
				// 			// this.travel = {}
				// 		}
				// 	})
				// })
				
			}
		}
	}
</script>

<style>
</style>

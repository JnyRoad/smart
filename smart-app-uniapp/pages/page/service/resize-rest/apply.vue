<template>
	<view class="yt-page bgf">
		<!-- <view class="mw h20 bgec"></view> -->
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item hasMust title="出勤日期" showImage=false>
				<yt-picker v-if="curWorkDateList.length>0" @changePicker="getPickerData" idType="2" value="请选择" :arrData="curWorkDateList" :showValue="false">
					<view :style="!workDate ? 'color:#666666' : ''">
						{{workDate || '请选择'}}
					</view>
				</yt-picker>
				<view v-if="curWorkDateList.length == 0" @click="nothasDatelist" class="f32pc2">无出勤日期</view>
			</yt-list-item>
			<yt-list-item hasMust title="可调休天数" showArrow=false showImage=false>
				<view class="f30 main-color">
					{{restData.restAbleCount}}
				</view>
			</yt-list-item>
			<yt-list-item showImage="false" hideLastBorder=true hasMust title="调休类型">
				<yt-picker v-if="restType.length>0" @changePicker="getPickerData" idType="1" value="请选择" :arrData="restType"></yt-picker>
			</yt-list-item>
			<yt-list-item  @click="onShowDatePicker(2)" hasMust title="调休日期" showImage=false>
				<view v-if="restData.restDate" class="f30 main-color">
					{{restData.restDate}}
				</view>
				<view v-if="!restData.restDate" class="f30pc2">
					请选择
				</view>
			</yt-list-item>
			<yt-list-item hasMust idType="restCount" title="现在要用天数" showImage=false showArrow=false>
				<view class="f30pc2">
					{{restData.restCount}}
				</view>
			</yt-list-item>
			<yt-list-item @click="getItem" idType="vacateDesc" hideLastBorder=true showArrow=false title="备注" showImage=false>
				<view class="f30pc4">
					{{restData.vacateDesc || '请输入(非必填)'}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<view class="hcc mt80">
			<yt-button @click="apply" btnText="提交" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<mx-date-picker :format="dateFormat" :show="showPicker" type="date" color="#5c92ff" @confirm="onSelected" @cancel="onSelected" />
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input"
		 v-if="showModel"></yt-mine-model>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue';
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import ytminemodel from '@/components/yt-mine-model.vue'
	import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue'
	import ytpicker from '@/components/yt-picker.vue'
	import ytapprovalprocess from '@/components/yt-approval-process.vue'
	import rest from '@/api/api-rest'
	import hideModel from '@/mixins/hideModel.vue'
	import {
		formatDate,
		formatDateTime,
		flitterTime
	} from "@/common/js/util.js"
	export default {
		mixins: [hideModel],
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'mx-date-picker': MxDatePicker,
			'yt-picker': ytpicker,
			'yt-button': ytbutton,
			'yt-approval-process': ytapprovalprocess,
			'yt-mine-model': ytminemodel
		},
		data() {
			return {
				modelObj: {
					title: '',
					input: '',
					idType: ''
				},
				showPicker: false,
				value: '',
				type: 'datetime',
				timeType: '',
				restType: [], // 调休类型的处理数据
				restDesc: '', // 调休类型
				restTypeData: [],
				curWorkDateDataList: [], // 出勤时间的原始数据
				curWorkDateList: [], // 出勤时间列表
				workDate : '',
				restData: {
					term: '', // 出勤日期
					termId: '', // 出勤时间id
					restType: '', // 调休类型
					restDate: '', // 调休日期
					// workDate: '', // 出勤日期
					restCount: '', // 现在要用天数
					restAbleCount: '', // 可调休天数
					vacateDesc: '',
					termCount:''
				},
				dateFormat: 'yyyy-mm-dd',
				curTime: 0, // 当前时间
			}
		},
		onNavigationBarButtonTap(e) {
			uni.navigateTo({
				url: './record'
			});
		},
		onShow() {
			this.getRestType()
			this.getRestDay(formatDate(new Date))
		},
		computed: {
			showModel() {
				return this.$store.state.modelStatus
			}
		},
		methods: {
			// 获取调休类型
			async getRestType() {
				const res = await rest.restType()
				this.restTypeData = res.data.data.records
				this.restType = this.restTypeData.map((el) => {
					return el.restName
				})
			},
			// 获取调休的天数
			async getRestDay(date) {
				const obj = {
					queryDay: date
				}
				const res = await rest.restDay(obj)
				if (!res) return
				if (!res.data.data.records.length){
					this.$ytHint.toast({
						title: '您的调休天数已用完，不能调休'
					})
					return
				}
				this.curWorkDateDataList = res.data.data.records
				this.restData.restAbleCount = res.data.data.dayCount.toString()
				this.curWorkDateList = this.curWorkDateDataList.map(el => {
					return el.workDate
				})
			},
			getItem(e) {
				this.$store.commit('ytModelStatus', true) // 显示弹出模态框
				this.modelObj = {
					title: e.title,
					input: e.tipContent,
					idType: e.type
				}
			},
			// 获取输入
			getData(e) {
				if (e.idType == 'vacateDesc') {
					this.restData.vacateDesc = e.value
				}
			},
			onShowDatePicker() {
				//显示
				if (!this.restData.restType) {
					this.$ytHint.toast({
						title: '请先选择调休类型'
					})
					return
				}
				if (this.restData)
				this.showPicker = true;
				this.value = this[this.type];
			},
			onSelected(e) {
				//选择
				this.showPicker = false;
				if (e) {
					this[this.type] = e.value;
					let restCount
					if (this.restDesc == '全天') {
						restCount = '1'
					} else {
						restCount = '0.5'
					}
					if (this.workDate) {
						let workDate = this.workDate.substr(-4,4)
						console.log(workDate);
						console.log(restCount);
						if (workDate < restCount) {
							this.$ytSystem.vibrate({
								title: '出勤日期为0.5天 调休类型不能为全天，请选择半天'
							})
							return
						}
					}
					this.restData.restDate = e.value
					if (this.restDesc == '全天') {
						this.restData.restCount = '1'
					} else {
						console.log(111);
						this.restData.restCount = '0.5'
					}
				}
			},
			// 获取调休类型
			getPickerData(e) {
				if (e.idType == 1) {
					this.restTypeData.forEach(el => {
						if (this.restDesc === e.value) return
						if (el.restName === e.value) {
							if (this.restData.restType) { // 如果修改调休类型  重置数据
								this.restData.restDate = ''
								this.restData.restCount = ''
							}
							this.restDesc = e.value // 保存描述内容
							this.restData.restType = el.restCode // 查出调休类型id
						}
					})
				} else if (e.idType == 2) {
					if(!this.curWorkDateDataList.length) {
						this.$ytHint.toast({
							title: '您的调休天数已用完，不能调休'
						})
						return
					}
					for (let i = 0; i < this.curWorkDateDataList.length; i++) {
						const eli = this.curWorkDateDataList[i]
						if (eli.workDate == e.value) {
							let term = new Date(this.restData.term).getTime()
							let restDate = new Date(this.restData.restDate).getTime()
							this.restData.term = eli.term.split(' ')[0]
							this.restData.termId = eli.termId
							this.restData.termCount = eli.termCount.toString()
							let workDate = eli.workDate.substr(-4,4)
							console.log(workDate);
							console.log(this.restData.restCount);
							if (workDate < this.restData.restCount && this.restData.restCount) {
								this.$ytSystem.vibrate({
									title: '出勤日期为0.5天 调休类型不能为全天，请选择半天'
								})
								return
							}
							this.workDate = eli.workDate
							console.log(this.workDate);
							break;
						}
					}
				}
			},
			// 没有出勤日期显示
			nothasDatelist() {
				this.$ytHint.toast({
					title: '您的调休天数已用完，不能调休'
				})
			},
			// 发起调休申请
			apply() {
				// let workDate = this.workDate.substr(-4,4)
				// if (workDate < this.restData.restCount) {
				// 	this.$ytSystem.vibrate({
				// 		title: '加班时间不能小于调休时间'
				// 	})
				// 	return
				// }
				let flag = true;
				Object.keys(this.restData).forEach(el => {
					if (this.restData[el] === ''&& el != 'vacateDesc') {
						flag = false
					}
				});
				if (!flag) {
					this.$ytSystem.vibrate({
						title: '带‘*’为必填项'
					});
					return
				}
				let term = new Date(this.restData.term).getTime()
				let restDate = new Date(this.restData.restDate).getTime()
				if (restDate < term) {
					this.$ytSystem.vibrate({
						title: '调休天数不能小于出勤天数'
					});
					return
				}
				console.log(this.restData.work);
				rest.restApply(this.restData).then(res => {
					if (res.data.data) {
						this.$ytHint.toast({
							title: '提交成功'
						})
						uni.redirectTo({
							url: './record'
						})
					}
				})
			}
		},
	}
</script>

<style scoped>
	.line {
		height: 90%;
	}
</style>

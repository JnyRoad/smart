<template>
	<view class="yt-page bgf">
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item 
			showImage=false
			hasMust
			hideLastBorder=true
			title="加班日期" 
			@click="onShowDatePicker('date','extraworkDate')"
			>
			<view class="f30pc2">
				{{extraworkData.extraworkDate}}
			</view>
			</yt-list-item>
			<yt-list-item
			showImage=false
			hasMust
			title="班别"
			>
				<yt-picker v-if="extraworkClassType.length>0" @changePicker="getPickerData" idType="1" value="白班" :arrData="extraworkClassType"></yt-picker>
			</yt-list-item>
			<yt-list-item
			showImage=false
			hasMust
			title="加班类型"
			
			>
				<yt-picker v-if="extraworkType.length>0" @changePicker="getPickerData" idType="2" :value="extraworkTimeType || '请选择'" :arrData="extraworkType"></yt-picker>
			</yt-list-item>
			<yt-list-item
			showImage=false
			hasMust
			title="时间类型"
			
			>
				<yt-picker v-if="extraworkTimeTypeList.length>0" @changePicker="getPickerData" idType="3" value="请选择" :arrData="extraworkTimeTypeList"></yt-picker>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pr30 pl30']" v-if="showPickerlist">
			<yt-list-item @click="onShowTimePickers(classInfo.startDate2,1)" title="二入" showImage=false :showArrow="true">
				<view class="f30pc2">
					{{classInfo.startDate2 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.endDate2,2)" title="二出" showImage=false :showArrow="true">
				<view class="f30pc2">
					{{classInfo.endDate2 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.startDate4,3)" title="四入" showImage=false :showArrow="true">
				<view class="f30pc2">
					{{classInfo.startDate4 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.endDate4,4)" title="四出" showImage=false :showArrow="true">
				<view class="f30pc2">
					{{classInfo.endDate4 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.startDate5,5)" title="五入" showImage=false :showArrow="true">
				<view class="f30pc2">
					{{classInfo.startDate5 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.endDate5,6)" title="五出" showImage=false :showArrow="true">
				<view class="f30pc2">
					{{classInfo.endDate5 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item 
			showImage=false
			hasMust
			showArrow=false
			title="加班时长(小时)" 
			idType="extraworkCount"
			>
			<view class="f30 main-color" v-if="hasClassInfo">
				{{extraworkData.extraworkCount}}
			</view>
			<input type="text" v-model="extraworkData.extraworkCount" class="main-color f30" v-if="!hasClassInfo"/>
			</yt-list-item>
		</yt-list>
		<!-- <yt-list :ytClass="['pr30 pl30']" v-if="showPickerlist && !hasClassInfo">
			<yt-list-item @click="onShowTimePickers(classInfo.startDate2,1)" title="二入" showImage=false :showArrow="!hasClassInfo">
				<view class="f30pc2">
					{{classInfo.startDate2 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.endDate2,2)" title="二出" showImage=false :showArrow="!hasClassInfo">
				<view class="f30pc2">
					{{classInfo.endDate2 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.startDate4,3)" title="四入" showImage=false :showArrow="!hasClassInfo">
				<view class="f30pc2">
					{{classInfo.startDate4 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.endDate4,4)" title="四出" showImage=false :showArrow="!hasClassInfo">
				<view class="f30pc2">
					{{classInfo.endDate4 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.startDate5,5)" title="五入" showImage=false :showArrow="!hasClassInfo">
				<view class="f30pc2">
					{{classInfo.startDate5 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowTimePickers(classInfo.endDate5,6)" title="五出" showImage=false :showArrow="!hasClassInfo">
				<view class="f30pc2">
					{{classInfo.endDate5 || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item 
			showImage=false
			hasMust
			showArrow=false
			title="加班时长(小时)" 
			idType="extraworkCount"
			>
			<view class="f30 main-color">
				{{extraworkData.extraworkCount}}
			</view>
			</yt-list-item>
		</yt-list> -->
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item 
			showImage=false
			hasMust
			title="是否出差加班" 
			showArrow=true
			idType="extraworkDesc"
			>
			<view class="f30pc2">
				<yt-picker v-if="extraworkTypeList.length>0" @changePicker="getPickerData" idType="4" value="否" :arrData="extraworkTypeList"></yt-picker>
			</view></yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item 
			showImage=false
			hideLastBorder=true
			showArrow=false
			title="加班原因" 
			@click="getInput"
			idType="extraworkDesc"
			>
			<view class="f30pc2">
				{{extraworkData.extraworkDesc||'请输入'}}
			</view></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<view class="hcc mw mt80 mb40"><yt-button @click="apply" btnText="提交" btnWidth="180" btnHeight="54"></yt-button></view>
		<mx-date-picker :format="dateFormat" :show="showPicker" :type="dateType" color:="#5c92ff" :show-tips="true"
		@confirm="onSelected" @cancel="onSelected" />
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue'
import ytlistitem from '@/components/yt-list-item/index.vue'
import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue'
import ytpicker from '@/components/yt-picker.vue'
import ytbutton from '@/components/yt-button/index.vue'
import ytminemodel from '@/components/yt-mine-model.vue'
import ytapprovalprocess from '@/components/yt-approval-process.vue'
import {formatDate, flitterTime} from '@/common/js/util.js'
import extrawork from '@/api/api-extrawork.js'
import approve from '@/api/api-approve.js'
import vacation from '@/api/api-vacate.js'
import hideModel from '@/mixins/hideModel.vue'
export default {
	mixins: [hideModel],
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'mx-date-picker': MxDatePicker,
		'yt-button': ytbutton,
		'yt-picker': ytpicker,
		'yt-mine-model': ytminemodel,
		'yt-approval-process': ytapprovalprocess
	},
	data () {
		return {
			modelObj: { // 弹窗模块
				title: '',
				input: '',
				idType: ''
			},
			dateType: 'date', // 当前选择时间的类型
			showPicker: false,
			showPickerlist : false,
			curDateFormat: 'yyyy-mm-dd', // 时间选择器的格式输出
			extraworkData: {
				extraworkDate: '',
				extraworkClassCode: '1',
				extraworkType: '',
				extraworkCount: 0,
				extraworkDesc: '',
				isTravelExtrawork: '0'
			},
			idType : 0,
			extraworkClassType: [], // 班别
			extraworkClassTypeData: [], // 班别 
			extraworkType: [], // 加班类型 
			extraworkTypeData: [], // 加班类型
			dateFormat : 'hh:ii',
			extraworkTimeTypeList: ['上午','下午','全天'], // 时间类型列表
			extraworkTimeType: '', // 时间类型
			classTypeInfo: {}, // 班别信息
			enm : '',
			classInfo: {
				startDate2: '',
				endDate2: '',
				startDate4: '',
				endDate4: '',
				startDate5: '',
				endDate5: '',
			}, // 带出来的班次信息
			extraworkTypeList: [
				"否", "是"
			],
			curTimeType: '',
			forenoonCount: 0, // 上午上班时间长度
			afternoonCount: 0, // 下午上班时间长度
			nightCount: 0 ,// 晚上上班长度
			classdesc: '', // 班别描述
			hasClassInfo: false // 是否配置了班次信息
		}
	},
	onLoad () {
		this.getExtraworkType()
		this.getExtraworkClassType()
		this.extraworkData.extraworkDate = formatDate(new Date())
		this.getClassInfo(this.extraworkData.extraworkDate)
	},
	// 导航栏按钮点击时间
	onNavigationBarButtonTap(e) {
		uni.redirectTo({
			url: './record'
		})
	},
	computed: {
		showModel() {
			return this.$store.state.modelStatus;
		}
	},
	methods: {
		// 获取加班类别
		async getExtraworkClassType () {
			try{
				const res = await extrawork.extraworkClassType()
				this.extraworkClassTypeData = res.data.data.records
				this.extraworkClassType = this.extraworkClassTypeData.map(el => {
					return el.restName
				})
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 获取加班类型
		async getExtraworkType () {
			try{
				const res = await extrawork.extraworkType()
				this.extraworkTypeData = res.data.data.records
				this.extraworkType = this.extraworkTypeData.map(el => {
					return el.extraworkTypeName
				})
				
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 查看班次信息类型
		async getClassInfo(curDate) {
			const obj = {
				queryDay: curDate
			}
			try{
				const res = await vacation.classQuery(obj)
				if (!res) return
				this.classTypeInfo = res.data.data
				console.log(this.classTypeInfo);
				if (!this.classTypeInfo.secondEnter || !this.classTypeInfo.secondEnter || !this.classTypeInfo.fourthEnter || !this.classTypeInfo.fourthOut) {
					this.hasClassInfo = false
					this.$ytHint.toast({
						title: '当前日期无班次信息，请手动添加加班时间'
					})
					return
				}
				this.hasClassInfo = true
				// 一下计算班次时长信息
				const curDateformat = flitterTime(curDate)
				console.log(curDateformat);
				const forenoonStart = new Date(`${curDateformat} ${this.classTypeInfo.secondEnter}`).getTime()
				const forenoonEnd = new Date(`${curDateformat} ${this.classTypeInfo.secondOut}`).getTime()
				const afternoonStart = new Date(`${curDateformat} ${this.classTypeInfo.fourthEnter}`).getTime()
				const afternoonEnd = new Date(`${curDateformat} ${this.classTypeInfo.fourthOut}`).getTime()
				this.forenoonCount = this.computClassCount(forenoonEnd,forenoonStart)
				this.afternoonCount = this.computClassCount(afternoonEnd,afternoonStart)
				if (this.classTypeInfo.fourthOut && this.classTypeInfo.fifthOut) {
					const nightStart = new Date(`${curDateformat} ${this.classTypeInfo.fifthEnter}`).getTime()
					const nightEnd = new Date(`${curDateformat} ${this.classTypeInfo.fifthOut}`).getTime()
					this.nightCount = this.computClassCount(nightEnd,nightStart)
				}
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// getPickerstata (e) {
		// 	console.log(e);
		// 	switch (e.idType){
		// 		case '1':
		// 			this.classInfo.startDate2 = e.value
		// 			break;
		// 			
		// 		case '2':
		// 			if (this.classInfo.startDate2 == ''){
		// 				this.$ytHint.toast({
		// 					title: '请先选择打卡时间一!'
		// 				})
		// 				return
		// 			}
		// 			this.classInfo.endDate2 = e.value
		// 			let f = this.afternoonCount
		// 			this.extraworkData.extraworkCount += 4
		// 			break;
		// 		case '3':
		// 			this.classInfo.startDate4 = e.value
		// 			
		// 			break;
		// 		case '4':
		// 			if (this.classInfo.startDate4 == ''){
		// 			this.$ytHint.toast({
		// 				title: '请先选择打卡时间三'
		// 			})
		// 			return
		// 		}
		// 			this.classInfo.endDate4 = e.value
		// 			let af = this.forenoonCount
		// 			this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) + 4
		// 			break;
		// 		case '5':
		// 			this.classInfo.startDate5 = e.value
		// 			break;
		// 		case '6':
		// 			if (this.classInfo.startDate5 == ''){
		// 				this.$ytHint.toast({
		// 					title: '请先选择打卡时间五'
		// 				})
		// 				return
		// 			}
		// 			this.classInfo.endDate5 = e.value
		// 			this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) + 3.5
		// 			break;
		// 		default:
		// 			break;
		// 	}
		// 	console.log(this.classInfo);
		// },
		// 获取用户选择的类型
		getPickerData(e) {
			if (e.idType == 1) { // 班别
				this.extraworkData.extraworkClassCode = ''
				this.extraworkClassTypeData.forEach(el => {
					if (el.restName === e.value) {
						this.extraworkData.extraworkClassCode = el.extraworkClassCode
						this.classDesc = e.value
						if (e.value == '夜班') {
							this.showPickerlist = true
							this.extraworkType = ['平时加班']
							this.extraworkTimeType = '平时加班'
							this.extraworkData.extraworkType = '1'
							this.classInfo.startDate5 = '20:00'
							this.classInfo.endDate5 = '23:30'
							this.classInfo.startDate4 = ''
							this.classInfo.endDate4 = ''
							this.classInfo.startDate2 = ''
							this.classInfo.endDate2 = ''
							this.extraworkData.extraworkCount = 3.5
						} else if (e.value == '白班') {
							this.extraworkType = this.extraworkTypeData.map(el => {
								return el.extraworkTypeName
							})
							console.log(this.extraworkType);
							this.classInfo.startDate2 = ''
							this.classInfo.endDate2 = ''
							this.classInfo.startDate4 = ''
							this.classInfo.endDate4 = ''
							this.classInfo.startDate5 = ''
							this.classInfo.endDate5 = ''
							this.extraworkData.extraworkCount = ''
						}
					}
				})
			} else if (e.idType == 2) { // 加班类型
				this.extraworkTypeData.forEach(el => {
					if (el.extraworkTypeName === e.value) {
						this.extraworkData.extraworkType = el.extraworkType
						console.log(this.extraworkData.extraworkType);
					}
				})
			} else if (e.idType == 4) { // 是否出差加班
				this.extraworkTypeList.forEach(el => {
					if (el === e.value) {
						this.extraworkData.isTravelExtrawork = e.value=='否'?'0':'1'
					}
				})
			}else if (e.idType == 3) {
				this.curTimeType = e.value
				this.extraworkTimeTypeList.forEach(el => {
					if (el == e.value) {
						this.showPickerlist = true
						if (el == '上午') {
							this.enm = '2'
							this.classInfo.startDate2 = this.classTypeInfo.secondEnter
							this.classInfo.endDate2 = this.classTypeInfo.secondOut
							this.classInfo.startDate4 = ''
							this.classInfo.endDate4 = ''
							this.classInfo.startDate5 = ''
							this.classInfo.endDate5 = ''
							this.extraworkData.extraworkCount = this.forenoonCount
						} else if (el == '下午') {
							this.enm = '3'
							this.classInfo.startDate2 = ''
							this.classInfo.endDate2 = ''
							this.classInfo.startDate4 = this.classTypeInfo.fourthEnter
							this.classInfo.endDate4 = this.classTypeInfo.fourthOut
							this.classInfo.startDate5 = ''
							this.classInfo.endDate5 = ''
							this.extraworkData.extraworkCount = this.afternoonCount
						} else if (el == '全天') {
							this.enm = '1'
							this.classInfo.startDate2 = this.classTypeInfo.secondEnter
							this.classInfo.endDate2 = this.classTypeInfo.secondOut
							this.classInfo.startDate4 = this.classTypeInfo.fourthEnter
							this.classInfo.endDate4 = this.classTypeInfo.fourthOut
							this.classInfo.startDate5 = this.classTypeInfo.fifthEnter
							this.classInfo.endDate5 = this.classTypeInfo.fifthOut
							this.extraworkData.extraworkCount = this.afternoonCount + this.forenoonCount
							// console.log(this.extraworkData.extraworkCount);
						}
					}
				})	
			}
		},
		// 显示模态框
		getInput(e) {
			this.$store.commit('ytModelStatus', true)  // 显示弹出模态框
			this.modelObj = {
				title: e.title,
				input: e.tipContent,
				idType: e.type
			}
		},
		// 获取模态框的输入内容
		getData(e) {
			// 请输入
			if (e.idType === 'extraworkDesc') {
				this.extraworkData.extraworkDesc = e.value
			}
		},
		// 显示 时间日期选择器
		onShowDatePicker(type,$type) {
			//显示
			this.showPicker = true
			this.dateType = type
			this.idType = 7
			if (type == 'date') {
				this.dateFormat = 'yyyy-mm-dd'
			} else if (type == 'time') {
				this.dateFormat = 'hh:ii'
			}
			this.curDateSelect = $type // 当前选择的时间
		},
		onShowTimePickers (showtime,idtype) {
			// console.log(showtime);
			// console.log(this.hasClassInfo);
			// if (showtime && this.hasClassInfo) {
			// 	return
			// }
			this.dateFormat = 'hh:ii'
			this.dateType = 'time'
			if (idtype ==3) {
				if (!this.classInfo.startDate2) {
					this.$ytHint.toast({
						title: '请填写打卡时间一!'
					})
					return
				}
				if (!this.classInfo.endDate2) {
					this.$ytHint.toast({
						title: '请填写打卡时间二!'
					})
					return
				}
			}
			if (idtype ==5) {
				if (!this.classInfo.startDate4) {
					this.$ytHint.toast({
						title: '请填写打卡时间三!'
					})
					return
				}
				if (!this.classInfo.endDate4) {
					this.$ytHint.toast({
						title: '请填写打卡时间四!'
					})
					return
				}
			}
			this.idType = idtype
			this.showPicker = true;
		},
		onSelected(e) {
			//选择
			this.showPicker = false
			if (!e) {
				return
			}
			if (this.idType == 1) {
				if (this.classInfo.endDate2) {
					const endDate1 = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate2}`))).getTime()
					const startDate1 = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate2}`))).getTime()
						let time = Math.floor((endDate1 - startDate1)/ 3600000)
						console.log(time);
						console.log(this.extraworkData.extraworkCount);
						this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) - Number(time)
					}
				this.classInfo.startDate2 = e.value	
				this.classInfo.endDate2 = ''
				if (!this.hasClassInfo) {
					this.extraworkData.extraworkCount = 0
				}
			}
			if (this.idType == 2) {
				if (!this.classInfo.startDate2) {
					this.$ytHint.toast({
						title: '请填写打卡时间一!'
					})
					return
				}
				this.classInfo.endDate2 = e.value
				const endDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate2}`))).getTime()
				const startDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate2}`))).getTime()
				if (endDate < startDate) {
					this.$ytHint.toast({
						title: '打卡时间二不能在打卡时间一之前'
					})
					this.classInfo.endDate2 = ''
					return
				}
				let time = Math.floor((endDate - startDate)/ 3600000)
				console.log(time);
				console.log(this.extraworkData.extraworkCount);
				this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) + Number(time)
			}
			if (this.idType == 3) {
				if (this.classInfo.endDate4) {
				const endDate1 = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate4}`))).getTime()
				const startDate1 = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate4}`))).getTime()
					let time = Math.floor((endDate1 - startDate1)/ 3600000)
					console.log(time);
					console.log(this.extraworkData.extraworkCount);
					this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) - Number(time)
				}
				this.classInfo.startDate4 = e.value
				this.classInfo.endDate4 = ''
				const endDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate4}`))).getTime()
				const startDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate2}`))).getTime()
				if (endDate < startDate) {
					this.$ytHint.toast({
						title: '打卡时间三不能在打卡时间二之前'
					})
					this.classInfo.startDate4 = ''
					return
				}
			}
			if (this.idType == 4) {
				if (!this.classInfo.startDate4) {
					this.$ytHint.toast({
						title: '请填写打卡时间三!'
					})
					return
				}
				this.classInfo.endDate4 = e.value
				const endDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate4}`))).getTime()
				const startDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate4}`))).getTime()
				if (endDate < startDate) {
					this.$ytHint.toast({
						title: '打卡时间四不能在打卡时间三之前'
					})
					this.classInfo.endDate4 = ''
					return
				}
				let time = Math.floor((endDate - startDate)/ 3600000)
				this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) + Number(time)
			}
			if (this.idType == 5) {
				this.classInfo.startDate5 = e.value
				if (this.classInfo.endDate5) {
					const endDate1 = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate5}`))).getTime()
					const startDate1 = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate5}`))).getTime()
					let time = Math.floor((endDate1 - startDate1)/ 3600000)
					this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) - Number(time)
				}
				this.classInfo.endDate5 = ''
				const endDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate5}`))).getTime()
				const startDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate4}`))).getTime()
				if (endDate < startDate) {
					this.$ytHint.toast({
						title: '打卡时间五不能在打卡时间四之前'
					})
					this.classInfo.startDate5 = ''
					return
				}
			}
			if (this.idType == 6) {
				if (!this.classInfo.startDate5) {
					this.$ytHint.toast({
						title: '请填写打卡时间五!'
					})
					return
				}
				this.classInfo.endDate5 = e.value
				const endDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.endDate5}`))).getTime()
				const startDate = (new Date(flitterTime(`${this.extraworkData.extraworkDate} ${this.classInfo.startDate5}`))).getTime()
				if (endDate < startDate) {
					this.$ytHint.toast({
						title: '打卡时间六不能在打卡时间五之前'
					})
					this.classInfo.endDate5 = ''
					return
				}
				let time = Math.floor((endDate - startDate)/ 3600000)
				this.extraworkData.extraworkCount = Number(this.extraworkData.extraworkCount) + Number(time)
			}
			if (this.idType == 7) {
				this.extraworkData.extraworkDate = e.value
			}
		},
		apply() {
			// if (!this.hasClassInfo) {
			// 	this.$ytHint.toast({
			// 		title: '当前日期无班次信息，请手动添加加班时间'
			// 	})
			// 	return
			// }
			if ( this.enm == '1') {
				if (this.classInfo.startDate2 == '' || this.classInfo.endDate2 == '' || this.classInfo.startDate4 == '' || this.classInfo.endDate4 == '')
					{	
						this.$ytSystem.vibrate({
							title: '需要填写打卡时间一二三四的加班时间'
						});
						return
					}
			}
			if ( this.enm == '2') {
				if (this.classInfo.startDate2 == '' || this.classInfo.endDate2 == '')
					{	
						this.$ytSystem.vibrate({
							title: '需要填写打卡时间一二的加班时间'
						});
						return
					}
			}
			if ( this.enm == '3') {
				if (this.classInfo.startDate4 == '' || this.classInfo.endDate4 == '')
					{	
						this.$ytSystem.vibrate({
							title: '需要填写打卡时间三四的加班时间'
						});
						return
					}
			}
			const obj = {
				...this.extraworkData,
				...this.classInfo
			}
			let flag = true;
			Object.keys(this.extraworkData).forEach(el => {
				if (!this.extraworkData[el] && el !== 'extraworkDesc') {
					console.log(el);
					flag = false
				}
			});
			if (!flag) {
				this.$ytSystem.vibrate({
					title: '带‘*’为必填项'
				});
				return;
			}
			extrawork.extraworkApply(obj).then(res => {
				if (!res) return
				this.$ytHint.toast({
					title: '提交成功'
				})
				uni.redirectTo({
					url: './record'
				})
			})
		},
		computClassCount (time1, time2) {
			return Math.abs( (time2-time1)/3600000)
		}
	}
}
</script>

<style></style>

<template>
	<view class="yt-page bgf">
		<yt-list :ytClass="['pl30', 'pr30']">
			<yt-list-item hideLastBorder=true hasMust title="请假类型" showImage="false">
				<yt-picker v-if="vacateType.length>0" @changePicker="getPickerData" idType="1" value="请选择" :arrData="vacateType"></yt-picker>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30', 'pr30']">
			<yt-list-item @click="onShowDatePicker('startDate','date')" hasMust title="开始日期" showImage="false">
				<view class="f30pc2">
					{{vacateData.startDate || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowDatePicker('endDate','date')" hasMust title="结束日期" showImage="false" >
				<view class="f30pc2">
					{{vacateData.endDate || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item v-if="vacateData.startDate && vacateData.endDate" hideLastBorder=true hasMust title="时间类型" showImage="false">
				<yt-picker v-if="vacateTimeTypeList.length>0" @changePicker="getPickerData" idType="2" :value="vacateTimeType||'请选择'" :arrData="vacateTimeTypeList"></yt-picker>
			</yt-list-item>
			<yt-list-item v-if="vacateData.startDate && vacateData.endDate && zidingyi" @click="onShowDatePicker('startTime','time')" hasMust title="开始时间" showImage="false">
				<view class="f30pc2">
					{{vacateData.startTime || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item v-if="vacateData.startDate && vacateData.endDate && zidingyi" @click="onShowDatePicker('endTime','time')" hasMust title="结束时间" showImage="false" >
				<view class="f30pc2">
					{{vacateData.endTime || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item showArrow="false" hasMust title="时长" showImage="false">
				<view class="hec f28pc2">
					<text class="main-color f36 mh">{{ vacateData.vacateCount }}</text>
					<text>{{vacateData.unit}}</text>
				</view>
			</yt-list-item>
			
			<yt-list-item @click="getInput" showArrow="false" title="请假原因" showImage="false">
				<view class="f30pc2">
					{{vacateData.vacateDesc || '请输入'}}
				</view>
			</yt-list-item>
			<yt-list-item v-if="vacateTypeDesc =='年假'" showArrow="false" hasMust title="享有年假(天)" showImage="false">
				<view class="hec f28pc2">
					<text class="main-color f36 mh">{{ yearHoliday }}</text>
				</view>
			</yt-list-item>
			<view class="mw pt30 pb30 f30pc bold" style="border-bottom: 1px solid #E0E0E0;">
				<text>假期说明</text>
				<view class="f30pc2 mw">
					{{jiaqishuoming || ''}}
				</view>
			</view>
		
			<yt-list-item  hideLastBorder=true showArrow="false" title="班次" showImage="false">
				<view class="f30pc2">
					{{classInfo.classDesc || ''}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30', 'pr30']">
			<yt-list-item hasTitle="false" showImage="false" showArrow="false">
				<view class="hbc">
					<view class="hbc f1 mr80">
						<text class="f30pc bold mr40">二入</text>
						<text class="f30pc">{{ classInfo.secondEnter }}</text>
					</view>
					<view class="hbc f1">
						<text class="f30pc bold mr40">二出</text>
						<text class="f30pc">{{ classInfo.secondOut }}</text>
					</view>
				</view>
			</yt-list-item>	
			<yt-list-item hasTitle="false" showImage="false" showArrow="false">
				<view class="hbc">
					<view class="hbc f1 mr80">
						<text class="f30pc bold mr40">四入</text>
						<text class="f30pc">{{ classInfo.fourthEnter }}</text>
					</view>
					<view class="hbc f1">
						<text class="f30pc bold mr40">四出</text>
						<text class="f30pc">{{ classInfo.fourthOut }}</text>
					</view>
				</view>
			</yt-list-item>
			<yt-list-item hasTitle="false" showImage="false" showArrow="false">
				<view class="hbc mw">
					<view class="hbc f1 mr80">
						<text class="f30pc bold mr40">五入</text>
						<text class="f30pc">{{ classInfo.fifthEnter }}</text>
					</view>
					<view class="hbc f1">
						<text class="f30pc bold mr40">五出</text>
						<text class="f30pc">{{ classInfo.fifthOut }}</text>
					</view>
				</view>
			</yt-list-item>
			<yt-list-item @click="update" hideLastBorder=true title="附件" showImage="false" showArrow="false" :hasRightImage="hasImage" :rightImage="image" :hasTip="!hasImage" tipContent="上传请假单据(非必传)"></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<view class="hcc mw mt80 mb120" @click="apply"><yt-button  btnText="提交" btnWidth="180" btnHeight="54"></yt-button></view>
		<mx-date-picker :format="dateFormat" :show="showPicker" :type="type" color:="#5c92ff" :show-tips="true" :showApply="true"
		@confirm="onSelected" @cancel="onSelected" />
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="500" :maxWidth="1000" :ql="0.8" type="base64"></cpimg>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue'
import ytlistitem from '@/components/yt-list-item/index.vue'
import ytbutton from '@/components/yt-button/index.vue'
import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue'
import ytpicker from '@/components/yt-picker.vue'
import ytminemodel from '@/components/yt-mine-model.vue'
import ytapprovalprocess from '@/components/yt-approval-process.vue'
import cpimg from '@/components/cpimg.vue'
import vacation from '@/api/api-vacate.js'
import dimission from '@/api/api-dimission.js';
import approve from '@/api/api-approve.js'
import hideModel from '@/mixins/hideModel.vue'
import { formatDateTime,formatDate, flitterTime} from '@/common/js/util.js'
export default {
	mixins: [hideModel],
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'yt-button': ytbutton,
		'mx-date-picker': MxDatePicker,
		'yt-picker': ytpicker,
		'yt-mine-model': ytminemodel,
		'yt-approval-process': ytapprovalprocess,
		'cpimg': cpimg
	},
	data() {
		return {
			zidingyi : false,
			hasImage: false, // 是否有附件
			image: '', // 附件
			jiaqishuoming : '',
			modelObj: {
				title: '',
				input: '',
				idType: ''
			},
			showPicker: false,
			value: '',
			type: 'date',
			dateFormat: 'yyyy-mm-dd',
			timeType: '',
			vacateTimeTypeList: ['上午', '下午', '全天', '自定义'],
			vacateTimeType: '', // 用户选择的时间类型
			vacateType: [], // 请假类型的处理数据
			vacateTypeData: [], // 获取的请假类型数据(元数据)
			vacateData: {
				vacateType: '',
				startDate: '',
				endDate: '',
				startTime: '',
				endTime: '',
				vacateCount: '',
				vacateDesc: '',
				photo: '',
				unit: ''
			},
			classInfo: {} ,// 当天的班次信息
			yearHoliday: 0 ,// 年假天数
			vacateTypeDesc: '' ,// 请假类型文本描述
			forenoonCount: 0, // 上午上班时长
			afternoonCount: 0, // 下午上班时长
			hasClassInfo: true
		};
	},
	// 导航栏按钮点击时间
	onNavigationBarButtonTap(e) {
		uni.navigateTo({
			url: './record'
		});
	},
	onLoad() {
		this.getVacateType()
		this.getRestTime()
	},
	computed: {
		showModel() {
			return this.$store.state.modelStatus;
		}
	},
	methods: {
		// 获取请假的类型
		async getVacateType() {
			try{
				const res = await vacation.vacateType();
				if (!res) return
				this.vacateTypeData = res.data.data.records;
				this.vacateType = this.vacateTypeData.map(el => {
					return el.vacateName
				})
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 通过请假的卡是时间去获取当天班次信息
		async getClassInfo(curDate) {
			const obj = {
				queryDay: curDate
			}
			try{
				const res = await vacation.classQuery(obj)
				if (!res) return
				res.data.data.className = res.data.data.classDesc
				this.classInfo = res.data.data
				if (!this.classInfo.secondEnter || !this.classInfo.secondEnter || !this.classInfo.fourthEnter || !this.classInfo.fourthOut) {
					this.hasClassInfo = false
					return
				}
				this.hasClassInfo = true
				// 一下代码计算班次时长  请假不记录晚班时间
				const curDateformat = flitterTime(curDate)
				const forenoonStart = new Date(`${curDateformat} ${this.classInfo.secondEnter}`).getTime()
				const forenoonEnd = new Date(`${curDateformat} ${this.classInfo.secondOut}`).getTime()
				const afternoonStart = new Date(`${curDateformat} ${this.classInfo.fourthEnter}`).getTime()
				const afternoonEnd = new Date(`${curDateformat} ${this.classInfo.fourthOut}`).getTime()
				this.forenoonCount = this.computClassCount(forenoonEnd,forenoonStart)
				this.afternoonCount = this.computClassCount(afternoonEnd,afternoonStart)
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 获取请假时长单位
		async getVacateUnit (vacateCode) {
			try{
				const res = await vacation.vacateUnit(vacateCode)
				if (res.data.data.unit === '天') {
					this.type = 'date'
					this.dateFormat = 'yyyy-mm-dd'
				}
				this.vacateData.unit = res.data.data.unit
			}catch(e){
				console.log(e);
				//TODO handle the exception
			}
		},
		// 请假类型为年假
		async getRestTime() {
			try{
				const res = await dimission.dimissionRestTime();
				if (!res) return
				this.yearHoliday = `${res.data.data.dayCount}`;
			}catch(e){
				//TODO handle the exception
			}
		},
		// 提交请假申请
		apply() {
			// if (!this.hasClassInfo) {
			// 	this.$ytHint.toast({
			// 		title: '当前日期无班次信息，请手动添加加班时间!'
			// 	})
			// 	return
			// }
			Object.keys(this.classInfo).forEach(el => {
				if (this.classInfo[el] == null) {
					this.classInfo[el] = ''
				}
			})
			let flag = true;
			Object.keys(this.vacateData).forEach(el => {
				if (!this.vacateData[el] && el != 'photo') { // 不验证附件
					if (!this.zidingyi) { // 不验证附件
						if (!this.vacateData[el] && el != 'startTime' && el != 'endTime' && el != 'vacateDesc') {
							console.log(el);
							flag = false
						}
					} else {
						flag = false
					}
					
				}
			})

			if (!flag) {
				this.$ytSystem.vibrate({
					title: '带‘*’为必填项'
				});
				return;
			}
			const applyData = {
				vacateType: this.vacateData.vacateType,
				startDate: `${this.vacateData.startDate} ${this.vacateData.startTime}`,
				endDate: `${this.vacateData.endDate} ${this.vacateData.endTime}`,
				vacateCount: this.vacateData.vacateCount,
				vacateDesc: this.vacateData.vacateDesc,
				photo: this.vacateData.photo,
				unit: this.vacateData.unit
			}
			const obj = {
				...applyData,
				...this.classInfo
			}
			delete obj.classDesc
			vacation.vacateApply(obj).then(res => {
				if (!res) return
				if (res.data.data) {
					this.$ytHint.toast({
						title: '提交成功'
					})
					uni.redirectTo({
						url: './record'
					})
				}
			}).catch(err => {
				console.log(err);
				throw err
			}) 
		},
		update () {
			this.$refs.cpimg._changImg()
		},
		// 上传附件
		cpimgOk(file) {
			if (file) {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理成功'
				})
				this.hasImage = true
				this.image = file
				console.log(file);
				this.vacateData.photo = file.split(',')[1]
			}
			
		},
		cpimgErr(e) {
			if (e) {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理失败,请重试'
				})
				this.hasImage = true
				throw e
			}
		},
		// 显示模态框
		getInput(e) {
			this.$store.commit('ytModelStatus', true); // 显示弹出模态框
			this.modelObj = {
				title: e.title,
				input: e.tipContent,
				idType: e.type
			};
		},
		// 显示时间选择器
		onShowDatePicker(timeType,type) {
			if (!this.vacateData.vacateType) {
				this.$ytHint.toast({
					title: '请先选择请假类型'
				})
				return
			}
			if ((!this.vacateData.startDate)&&(timeType != 'startDate')) {
				this.$ytHint.toast({
					title: '请先选择开始日期'
				})
				return
			}
			if (timeType == 'startTime' || timeType == 'endTime') {
				if (!this.vacateTimeType) {
					this.$ytHint.toast({
						title: '请先选择时间类型'
					})
					return
				}
			}
			if (timeType == 'endDate') {
				this.vacateData.endDate = ''
				this.zidingyi = false
			}
			if (timeType == 'endTime') {
				if (!this.vacateData.startTime) {
					this.$ytHint.toast({
						title: '请先选择开始时间'
					})
					return
				}
			}
			// if (this.vacateTimeType !== '自定义') {
			// 	if (timeType == 'startTime' || timeType == 'endTime') {
			// 		this.$ytHint.toast({
			// 			title: '此时间类型不支持修改开始时间和结束时间'
			// 		})
			// 		return
			// 	}
			// }
			//显示
			this.showPicker = true;
			this.value = this[this.type];
			if (type == 'time') {
				this.dateFormat = 'hh:ii'
			} else if (type == 'date') {
				this.dateFormat = 'yyyy-mm-dd'
			}
			this.type = type
			this.timeType = timeType
		},
		// 确认选获取数据
		onSelected(e) {
			//选择
			this.showPicker = false;
			if (e) {
				console.log(e);
				this[this.type] = e.value;
				if (this.timeType === 'startDate' || this.timeType === 'endDate' ) {
					if (this.timeType === 'startDate') { // 当前为开始日期
						this.vacateTimeTypeList = ['上午', '下午', '全天', '自定义'],
						this.vacateData.startDate = e.value
						this.getClassInfo(e.value)
						this.vacateData.endDate = '' // 一点重置开始日期 以下数据全部重置
						this.vacateData.vacateCount = ''
						this.vacateData.startTime = ''
						this.vacateData.endTime = ''
						this.vacateTimeType = ''
					}
					if (this.timeType === 'endDate') {
						
						this.vacateData.endDate = e.value
						const startDate = Date.parse(new Date(flitterTime(this.vacateData.startDate))) // 开始时间
						const endDate = Date.parse(new Date(flitterTime(this.vacateData.endDate))) // 结束时间
						if (startDate == endDate) {
							this.vacateTimeTypeList = ['上午', '下午', '全天', '自定义'],
							this.vacateData.endDate = e.value
							this.vacateTimeType = '全天'
							this.vacateData.startTime = this.classInfo.secondEnter
							this.vacateData.endTime = this.classInfo.fourthOut
							if (this.vacateData.unit == '天') {
								this.vacateData.vacateCount = 1
							} else {
								this.vacateData.vacateCount = 8*(Math.floor(((endDate - startDate)/ 86400000))?Math.floor(((endDate - startDate)/ 86400000))+1:1)
								console.log(this.vacateData.vacateCount);
							}
						} else if (endDate < startDate) {
							this.$ytHint.toast({
								title: '结束日期不能小于开始日期'
							})
							this.vacateData.endDate = ''
							this.vacateData.vacateCount = ''
						} else if (endDate > startDate) {
							this.vacateTimeTypeList = ['全天']
							this.vacateTimeType = '全天'
							this.vacateData.startTime = this.classInfo.secondEnter
							this.vacateData.endTime = this.classInfo.fourthOut
							if (this.vacateData.unit == '天') {
								this.vacateData.vacateCount = Math.floor(((endDate - startDate)/ 86400000))?Math.floor(((endDate - startDate)/ 86400000))+1:1
								console.log(this.vacateData.vacateCount);
							} else if (this.vacateData.unit == '小时' || this.vacateData.unit == '时') {
								this.vacateData.vacateCount = 8*(Math.floor(((endDate - startDate)/ 86400000))?Math.floor(((endDate - startDate)/ 86400000))+1:1)
								console.log(this.vacateData.vacateCount);
							}
						}
					}
				} else if (this.timeType === 'startTime' || this.timeType === 'endTime') {
					if (this.timeType === 'startTime') this.vacateData.startTime = e.value
					if (this.timeType === 'endTime') this.vacateData.endTime = e.value
					if (!this.vacateData.startTime || !this.vacateData.endTime) return
					const endDate = (new Date(flitterTime(`${this.vacateData.endDate} ${this.vacateData.endTime}`))).getTime()
					const startDate = (new Date(flitterTime(`${this.vacateData.startDate} ${this.vacateData.startTime}`))).getTime()
					console.log(this.vacateData.startDate);
					if (endDate < startDate) {
						this.$ytHint.toast({
							title: '结束时间不能在开始时间之前'
						})
						this.vacateData.endTime = ''
						this.vacateData.vacateCount = ''
						return
					}	
					if (this.vacateTimeType == '自定义') {
						if (this.vacateData.unit == '天') {
							this.vacateData.vacateCount = parseFloat(((endDate - startDate)/ 28800000).toFixed(2))
							console.log(this.vacateData.vacateCount);
						} else if (this.vacateData.unit == '小时' || this.vacateData.unit == '时') {
							let time = (endDate - startDate)/ 3600000
							let caotime = time.toFixed(1)
							if (caotime.toString().split('.')[1] < 5) {
								caotime = caotime.toString().split('.')[0]
							}
							if (caotime.toString().split('.')[1] > 5) {
								caotime = caotime.toString().split('.')[0] + '.' + '5'
							}
							this.vacateData.vacateCount = caotime
							console.log(this.vacateData.vacateCount);
						}	
					}
				}
			}
		},
		// 获取用户选择的类型
		getPickerData(e) {
			this.vacateTypeDesc = e.value
			if (e.idType == 1) { // 请假类型
				this.vacateTypeData.forEach(el => {
					if (el.vacateName === e.value) {
						this.vacateData.startDate = ''
						this.vacateData.endDate = ''
						this.vacateData.vacateCount = ''
						this.vacateData.startTime = ''
						this.vacateData.endTime = ''
						this.vacateTimeType = ''
						this.jiaqishuoming = el.vacateRemark
						this.classInfo = {}
						this.vacateData.vacateType = el.vacateCode;
						this.getVacateUnit(el.vacateCode)
						this.vacateTimeTypeList = ['上午', '下午', '全天', '自定义']
					}
				});
			} else if (e.idType == 2) { // 时间类型
				if (!this.vacateData.startDate) {
					this.$ytHint.toast({
						title: '请先选择开始日期'
					})
					return
				}
				this.vacateTimeType = e.value
				if (e.value == '上午') {
					this.zidingyi = false
					this.vacateData.startTime = this.classInfo.secondEnter
					this.vacateData.endTime = this.classInfo.secondOut
					if (this.vacateData.unit == '天') {
						this.vacateData.vacateCount = 0.5
					} else {
						this.vacateData.vacateCount = 4
					}
					
				} else if (e.value == '下午') {
					this.zidingyi = false
					this.vacateData.startTime = this.classInfo.fourthEnter
					this.vacateData.endTime = this.classInfo.fourthOut
					if (this.vacateData.unit == '天') {
						this.vacateData.vacateCount = 0.5
					} else {
						this.vacateData.vacateCount = 4
					}
				} else if (e.value == '全天') {
					this.zidingyi = false
					this.vacateData.startTime = this.classInfo.secondEnter
					this.vacateData.endTime = this.classInfo.fourthOut
					const startDate = Date.parse(new Date(flitterTime(this.vacateData.startDate))) // 开始时间
					const endDate = Date.parse(new Date(flitterTime(this.vacateData.endDate))) // 结束时间
					if (this.vacateData.unit == '天') {
						this.vacateData.vacateCount = Math.floor(((endDate - startDate)/ 86400000))?Math.floor(((endDate - startDate)/ 86400000))+1:1
						console.log(this.vacateData.vacateCount);
					} else if (this.vacateData.unit == '小时' || this.vacateData.unit == '时') {
						this.vacateData.vacateCount = 8*(Math.floor(((endDate - startDate)/ 86400000))?Math.floor(((endDate - startDate)/ 86400000))+1:1)
						console.log(this.vacateData.vacateCount);
					}
					
				} else if (e.value == '自定义') {
					this.zidingyi = true
					this.vacateData.startTime = ''
					this.vacateData.endTime = ''
					this.vacateData.vacateCount = ''
					this.dateFormat = 'hh:ii'
				}
			}
			
		},
		getData(e) {
			// 获取请假事由
			this.vacateData.vacateDesc = e.value;
		},
		// 计算时长
		computClassCount (time1, time2) {
			console.log(Math.abs( parseInt((time2-time1)/3600000)));
			return Math.abs( parseInt((time2-time1)/3600000))
		}
	}
};
</script>

<style scoped>
.line {
	height: 90%;
}
</style>

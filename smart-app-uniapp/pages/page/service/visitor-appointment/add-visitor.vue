<template>
	<view class="mw">
		<yt-list :ytClass="['pl30 pr30 mb20']">
			<yt-list-item showImage="false" hasMust title="访客入园方式">
				<yt-picker v-if="reasonList.length>0" @changePicker="getPickerData" value="请选择" :arrData="list1"></yt-picker>
			</yt-list-item>
			<yt-list-item
			v-if="tmindex==1||tmindex==3"
				@click="update"
				showImage="false"
				hasMust
				showArrow="false"
				title="访客照片"
				:hasRightIcon="hasRightIcon"
				:hasRightImage="hasRightImage"
				:rightImage="rightImage"
				rightIcon="/static/img/mine/camera.png"
			></yt-list-item>
			<yt-list-item @click="showPlateModal = !showPlateModal" idType="plateNumber" showImage="false" showArrow="false" hasMust title="车牌号"
			v-if="tmindex==2||tmindex==3">
				<view class="f30pc2">
					{{visitor.plateNumber||'请输入'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="getInput" idType="visitorName" showImage="false" hasMust showArrow="false" title="访客姓名" :tipContent="visitor.visitorName||'请输入'"></yt-list-item>
			<yt-list-item
				@click="getInput"
				idType="visitorMobile"
				showImage="false"
				hasMust
				showArrow="false"
				title="访客手机"
			>
				<view class="f30pc2">
					{{visitor.visitorMobile||'请输入'}}
				</view>
			</yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pl30 pr30 mb20']">
			<yt-list-item @click="getInput" idType="visitorCompany" showImage="false" showArrow="false" title="访客单位">
				<view class="f30pc2">
					{{visitor.visitorCompany||'请输入'}}
				</view>
			</yt-list-item>
			<yt-list-item showImage="false" hasMust title="事由"><yt-picker v-if="reasonList.length>0" @changePicker="getPickerData" value="请选择" :arrData="reasonList"></yt-picker></yt-list-item>
			<yt-list-item @click="getInput" idType="remark" showImage="false" showArrow="false" title="驻场说明" hasMust v-if="showPhone">
				<view class="f30pc2">
					{{visitor.remark||'请输入'}}
				</view>
			</yt-list-item>
			<yt-list-item
			showImage=false
			hasMust
			v-if="showPhone"
			title="驻厂周期"
			>
				<yt-picker @changePicker="getPickerDataZq" idType="1" value="请选择" :arrData="extraworkClassType"></yt-picker>
			</yt-list-item>
		</yt-list>
		<view class="pl20 pt30 pb40 pr20 mw mb10" style="background-color: #FFFFFF;" v-if="showPhone">
			<view class="hlc blod">
				<text style="color: red;" class="f36pc mr10">*</text>
				<text>上传身份证</text>
			</view>
			<view class="ml20">
				<text>提示: 园区非员工的服务人员需上传身份证</text>
			</view>
		</view>
		<view class="pt20 pb20" style="margin: 0 auto;width: 60%;" v-if="showPhone">
			<view style="width: 100%;height: 300upx;" class="hcc rel" @click="updates(1)">
				<image :src="visitorFrontPhoto" lazy-load="true" class="mw mh" v-if="visitorFrontPhoto"></image>
				<view class="hcc mw" style="height: 70upx;position: absolute;bottom: 0;left: 0;background-color: rgba(0,0,0,.5);" v-if="visitorFrontPhoto">
					<view class="hlc">
						<image src="/static/img/mine/camera.png" style="height: 40upx;width: 40upx;"></image>
						<text style="color: #FFFFFF" class="ml10">重新拍摄</text>
					</view>
				</view>
				<view class="mw mh vb" v-else style="background-color: #FFFFFF;">
					<image src="/static/img/serive/recruit/idcard.png" lazy-load="true" class="mw mh"></image>
					<text class="mw hcc">点击上传人像页面照片</text>
				</view>
			</view>
			<view style="width: 100%;height: 300upx;" class="hcc rel mt20" @click="updates(2)">
				<image :src="visitorBackPhoto" lazy-load="true" class="mw mh" v-if="visitorBackPhoto"></image>
				<view class="hcc mw" style="height: 70upx;position: absolute;bottom: 0;left: 0;background-color: rgba(0,0,0,.5);" v-if="visitorBackPhoto">
					<view class="hlc">
						<image src="/static/img/mine/camera.png" style="height: 40upx;width: 40upx;"></image>
						<text style="color: #FFFFFF" class="ml10">重新拍摄</text>
					</view>
				</view>
				<view class="mw mh vb" v-else style="background-color: #FFFFFF;">
					<image src="/static/img/serive/recruit/idcard.png" lazy-load="true" class="mw mh"></image>
					<text class="mw hcc">点击上传国徽页面照片</text>
				</view>
			</view>
			<!-- <view style="width: 100%;height: 300upx;" class="hcc mt30 rel" @click="updates(2)">
				<image :src="visitorBackPhoto" lazy-load="true" class="mw mh" v-if="visitorBackPhoto"></image>
				<view class="hcc mw" style="height: 70upx;position: absolute;bottom: 0;left: 0;background-color: rgba(0,0,0,.5);" v-if="visitorFrontPhoto">
					<view class="hlc">
						<image src="/static/img/mine/camera.png" style="height: 40upx;width: 40upx;"></image>
						<text style="color: #FFFFFF" class="ml10">重新拍摄</text>
					</view>
				</view>
				<view class="mw mh vb" v-else style="background-color: #FFFFFF;">
					<image src="/static/img/serive/recruit/idcard.png" lazy-load="true" class="mw mh"></image>
					<text class="mw hcc">点击上传国徽页面照片</text>
				</view>
			</view> -->
		</view>
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item @click="onShowDatePicker('datetime', 1)" hasMust title="来访时间" showImage="false" v-if="!showPhone">
				<view class="f30 main-color">
					{{visitor.startTime || '未填写'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="onShowDatePicker('datetime', 2)" hasMust title="离开时间" showImage="false" v-if="!showPhone">
				<view class="f30 main-color">
					{{visitor.endTime || '未填写'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="addAccompany" title="添加随行人员" showImage="false">
				<view class="f30pc2">
					{{'已添加' + addAccompanyPeopleNum + '人'}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="bottom rel bgf pb40">
			<yt-list :ytClass="['pl30 pr30 mb40']">
				<yt-list-item hasMust @click="getInput" idType="employeeId" showImage="false" showArrow="false" title="被访工号">
					<view class="f30pc2">
						{{visitor.employeeId || '请输入'}}
					</view>
				</yt-list-item>
				<yt-list-item hasMust idType="employeeName" showImage="false" showArrow="false" title="被访姓名">
					<view class="f30pc2">
						{{visitor.employeeName || '请输入'}}
					</view>
				</yt-list-item>
				<yt-list-item hasMust idType="employeeMobile" showImage="false" showArrow="false" title="被访手机">
					<view class="f30pc2">
						{{visitor.employeeMobile || '请输入'}}
					</view>
				</yt-list-item>
			</yt-list>
			<view class="hcc mw"><yt-button @click="apply" btnWidth="180" btnHeight="54" btnText="提交"></yt-button></view>
		</view>
		<mx-date-picker format="yyyy-mm-dd hh:ii" :show="showPicker" :type="type" color:="#5c92ff" :show-tips="true" @confirm="onSelected" @cancel="onSelected"/>
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="200" :maxWidth="1000" :ql="0.8" type="base64"></cpimg>
		<cpimg ref="cpimgs" @result="cpimgOks" @err="cpimgErs" :size="500" :maxWidth="1000" :ql="0.8" type="base64"></cpimg>
		<plate-modal :show="showPlateModal" @confirm="getPlateNum" @cancel="cancelPlateModal"></plate-modal>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue';
import ytlistitem from '@/components/yt-list-item/index.vue';
import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue';
import ytpicker from '@/components/yt-picker.vue';
import ytbutton from '@/components/yt-button/index.vue';
import ytminemodel from '@/components/yt-mine-model.vue';
import plateMoadl from '@/components/plate-modal/plate-modal.vue'
import visit from '@/api/api-visitor.js';
import cpimg from '@/components/cpimg.vue'
import { ACCOMPANYPEOPLE, TEMPORARYDATA, storage } from '@/tools/storage.js';
import { employee } from '@/api/api-mine.js'
import hideModel from '@/mixins/hideModel.vue';
import {checkCarNum, checkPhone, dateMonment, flitterTime, formatDateTime,formatDate,formatDateTime2,formatTime2} from '@/common/js/util.js'
export default {
	mixins: [hideModel],
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'mx-date-picker': MxDatePicker,
		'yt-picker': ytpicker,
		'yt-button': ytbutton,
		'yt-mine-model': ytminemodel,
		'cpimg': cpimg,
		'plate-modal': plateMoadl
	},
	data() {
		return {
			modelObj: {
				title: '',
				input: '',
				idType: ''
			},
			list1 : ['人脸识别','车辆识别','人车皆识别'],
			visitorFrontPhoto : '',
			visitorBackPhoto: '',
			updatese : '',
			showPicker: false,
			value: '',
			type: 'datetime',
			showPlateModal: false,
			hasRightIcon: true,
			hasRightImage: false,
			rightImage: '',
			addAccompanyPeopleNum: 0, // 随行人数
			reason: [],
			reasonData: [],
			showPhone : false,
			extraworkClassType : ['一个月', '两个月' ,'三个月', '六个月'],
			timeType: 1, // 开始时间 2 离开时间
			visitor: {
				visitorPhoto: '',
				visitorName: '',
				visitorMobile: '',
				visitorCompany: '',
				visitReasonCode: '',
				visitorFrontPhoto : '',
				visitorBackPhoto : '',
				plateNumber: '',
				startTime: '',
				endTime: '',
				employeeId: '',
				employeeName: '',
				remark: '',
				employeeMobile: ''
			},
			tmindex : 0,
			member: [], // 随行人员
			rules: [
				{
					remark : '',
					require: true
				},
				{
					visitorFrontPhoto : '',
					require: true
				},
				{
					visitorBackPhoto : '',
					require: true
				},
				{
					plateNumber : '',
					require: true
				},
				{
					visitorPhoto: '',
					require: true
				},
				{
					visitorName: '',
					require: true
				},
				{
					visitorMobile: '',
					require: true
				},
				{
					visitReasonCode: '',
					require: true
				},
				{
					startTime: '',
					require: true
				},
				{
					endTime: '',
					require: true
				},
				{
					employeeId: '',
					require: true
				},
				{
					employeeName: '',
					require: true
				},
				{
					employeeMobile: '',
					require: true
				}
			]
		};
	},
	onLoad() {
		this.visitor.employeeId = storage.getSync(storage.USER_NAME)
		this.getEmployeeInfo(this.visitor.employeeId)
	},
	onShow() {
		this.getVisitData();
		this.getAccompanyPeople();
		this.getReasonList()
	},
	computed: {
		showModel() {
			return this.$store.state.modelStatus;
		},
		reasonList() {
			let reason = []
			for (let i=0, len=this.reasonData.length; i<len; i++) {
				reason.push(this.reasonData[i].reasonTypeName)
			}
			return reason
		}
		
	},
	onBackPress() {
		 //  提删除本地随行人员数据
		storage.removeSync(TEMPORARYDATA);
		storage.removeSync(ACCOMPANYPEOPLE);
	},
	methods: {
		updates (id) {
			this.updatese = id
			this.$refs.cpimgs._changImg()
		},
		// 上传附件
		cpimgOks(file) {
			if (file) {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理成功'
				})
				console.log(file);
				if (this.updatese == 1) {
					this.visitorFrontPhoto = file
					this.visitor.visitorFrontPhoto = file.split(',')[1]
				}
				if (this.updatese == 2) {
					this.visitorBackPhoto = file
					this.visitor.visitorBackPhoto = file.split(',')[1]
				}
			}
			
		},
		cpimgErrs(e) {
			if (e) {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理失败,请重试'
				})
				this.hasImage = true
				throw e
			}
		},
		// 获取添加随行人员后页面临时存的数据
		getVisitData() {
			const res = storage.getSync(TEMPORARYDATA);
			if (res) {
				this.visitor = res;
				if (this.visitor.visitorPhoto) {
					this.hasRightIcon = false;
					this.hasRightImage = true;
					this.rightImage = `data:image/png;base64,${res.visitorPhoto}`
				}
			}
		},
		// 获取是由列表
		async getReasonList() {
			try{
				const res = await visit.visitReasonList()
				this.reasonData = res.data.data.records
				
			}catch(e){
				console.log(e);
				throw e
			}
		},
		// 获取随行人员数据
		getAccompanyPeople() {
			const res = storage.getSync(ACCOMPANYPEOPLE) || [];
			this.addAccompanyPeopleNum = res.length
			this.member = res
		},
		// 输入员工号获取信息
		async getEmployeeInfo(value) {
			try{
				const res = await employee.baseInfo(value)
				if (res.data.code === 1) {
					this.visitor.visitorName
					this.$ytHint.toast({
						title: res.data.msg
					})
					return
				}
				this.visitor.employeeName = res.data.data.employeeName
				this.visitor.employeeMobile = res.data.data.mobile
			}catch(e){
				console.log(e);
				throw e
			}
		},
		// 获取车牌号
		getPlateNum (e) {
			let carNum = ''
			for (var i = 0; i < e.length; i++) {
				carNum += e[i].val
			}
			this.showPlateModal = false
			this.visitor.plateNumber = carNum;
		},
		// 隐藏车牌号输入
		cancelPlateModal (e) {
			this.showPlateModal = e
		},
		onShowDatePicker(type, timeType) {
			if (timeType == 2) {
				if (!this.visitor.startTime) {
					this.$ytHint.toast({
						title: '请选择来访时间'
					})
					return
				}
			}
			//显示
			this.showPicker = true;
			this.value = this[type];
			this.timeType = timeType;
		},
		getPickerDataZq (e) {
			let data = formatDateTime2(new Date())
			let time = formatTime2(new Date())
			let startMonth = data.month
			if (startMonth < 10) {
				startMonth = '0' + startMonth
			}
			let startTime = data.year + '-' + startMonth + '-' + data.day + ' ' + time
			this.visitor.startTime = startTime
			let year = data.year
			let month = data.month
			let day = data.day
			switch (e.value){
				case '一个月':
					month = month + 1
					if (month > 12) {
						year = year + 1
						month = month - 12
					}
					break;
				case '两个月':
					month = month + 2
					if (month > 12) {
						year = year + 1
						month = month - 12
					}
					break;
				case '三个月':
					month = month + 3
					if (month > 12) {
						year = year + 1
						month = month - 12
					}
					break;
				case '六个月':
					month = month + 6
					if (month > 12) {
						year = year + 1
						month = month - 12
					}
					break;
				default:
					break;
			}
			if (month < 10) {
				month = '0' + month
			}
			if (day < 10) {
				day = '0' + day
			}
			let endtime = year + '-' + month + '-' + day + ' ' + time
			this.visitor.endTime = endtime
			console.log(this.visitor)
		},
		getPickerData(e) {
			if (e.value == '人脸识别') {
				this.tmindex = 1
				this.rules[3].require = false
				this.rules[4].require = true
				this.visitor.plateNumber = ''
				return
			}
			if (e.value == '车辆识别') {
				this.tmindex = 2
				this.rules[3].require = true
				this.rules[4].require = false
				this.visitor.visitorPhoto = ''
				this.rightImage = '/static/img/mine/camera.png'
				return
			}
			if (e.value == '人车皆识别') {
				this.tmindex = 3
				this.rules[3].require = true
				this.rules[4].require = true
				this.visitor.visitorPhoto = ''
				this.rightImage = '/static/img/mine/camera.png'
				this.visitor.plateNumber = ''
				return
			}
			if (e.value == '园区驻厂') {
				this.showPhone = true
				this.visitor.startTime = ''
				this.visitor.endTime = ''
				this.rules[0].require = true
				this.rules[1].require = true
				this.rules[2].require = true
			} else {
				this.showPhone = false
				this.visitor.startTime = ''
				this.visitor.endTime = ''
				this.rules[0].require = false
				this.rules[1].require = false
				this.rules[2].require = false
				this.visitor.visitorBackPhoto = ''
				this.visitor.visitorFrontPhoto = ''
			}
			this.reasonData.forEach(el => {
				if (el.reasonTypeName == e.value) {
					this.visitor.visitReasonCode = el.reasonTypeCode
				}
			})
		},
		onSelected(e) {
			this.showPicker = false;
			const curTime = Date.parse(new Date(formatDateTime(new Date(), 'm')))
			if (e) {
				let time = flitterTime(e.value)
				this[this.type] = e.value;
				if (this.timeType == 1) {
					console.log(e.value);
					let startTime = Date.parse(new Date(time))
					let endTime = startTime + 7200000
					console.log(startTime);
					console.log(curTime);
					if (startTime < curTime) {
						this.$ytHint.toast({
							title: '不能选择过去时间'
						})
					} else {
						this.visitor.startTime = e.value;
						this.visitor.endTime = dateMonment('Y-m-d H:i',endTime/1000)
					}
					
				} else if (this.timeType == 2) {
					
					let endTime = Date.parse(new Date(time))
					let startTime = Date.parse(new Date(flitterTime(this.visitor.startTime)))
					console.log(endTime);
					console.log(startTime);
					if (endTime <= startTime) {
						this.$ytHint.toast({
							title: '离开时间不能在来访时间之前'
						})
					} else {
						this.visitor.endTime = e.value;
					}
				}
			}
		},
		update () {
			this.$refs.cpimg._changImg()
		},
		cpimgOk(file) {
			if (file) {
				this.hasRightIcon = false
				this.hasRightImage = true
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理成功'
				})
				this.rightImage = file
				console.log(file);
				this.visitor.visitorPhoto = file.split(',')[1]
			} else {
				this.hasRightIcon = true
				this.hasRightImage = false
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理失败，请重试'
				})
			}
			
		},
		cpimgErr(e) {
			if (e) {
				this.hasRightIcon = true
				this.hasRightImage = false
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理失败,请重试'
				})
				console.log(e);
				throw e
			}
		},
		addOtherPeople() {
			uni.navigateTo({
				url: `./add-other-people`
			});
		},
		getInput(e) {
			this.$store.commit('ytModelStatus', true); // 显示弹出模态框
			this.modelObj = {
				title: e.title,
				input: e.tipContent,
				idType: e.type
			};
		},
		getData(e) {
			switch (e.idType) {
				case 'visitorName':
				
					this.visitor.visitorName = e.value;
					break;
				case 'visitorMobile':
					if (!checkPhone(e.value)) {
						this.$ytHint.toast({
							title: '手机号码格式错误'
						})
						return
					}
					this.visitor.visitorMobile = e.value;
					break;
				case 'visitorCompany':
					
					this.visitor.visitorCompany = e.value;
					break;
				case 'plateNumber':
					let carNum = e.value.toUpperCase()
					if (!checkCarNum(carNum)) {
						this.$ytHint.toast({
							title: '车牌号不合法'
						})
						return
					}
					this.visitor.plateNumber = carNum;
					break;
				case 'employeeId':
					this.visitor.employeeId = e.value;
					this.getEmployeeInfo(e.value)
					break;
				case 'employeeName':
					this.visitor.employeeName = e.value;
					break;
				case 'employeeMobile':
					this.visitor.employeeMobile = e.value;
					break;
				case 'remark':
					this.visitor.remark = e.value;
					break;
				default:
					break;
			}
		},
		addAccompany() {
			// 添加随行人员 现存储当前填好的数据
			storage.setSync(TEMPORARYDATA, this.visitor);
			uni.navigateTo({
				url: `./accompany-people?type=1`
			});
		},
		apply() {
			let flag = true;
			try{
				Object.keys(this.visitor).forEach(el => {
					this.rules.forEach(el1 => {
						if (el in el1 && el1.require) {
							if (!this.visitor[el]) {
								throw new Error('over loop')
							}
						}
					})
				});
			}catch(e){
				flag = false
			}
			if (!flag) {
				this.$ytSystem.vibrate({
					title: '*为必填项'
				});
				return;
			}
			visit.visitAdd(this.visitor).then(res => {
				if (res.data.code === 0) {
					const obj = {
						visitId: res.data.data.visitId,
						member: this.member
					}
					visit.memberAdd(obj).then(res1 => {
						//  提删除本地随行人员数据
						if (res1.data.code === 0) {
							storage.removeSync(TEMPORARYDATA);
							storage.removeSync(ACCOMPANYPEOPLE);
							uni.redirectTo({
								url: './my-visitor'
							})
						} else {
							this.$ytHint.toast({
								title: `错误信息: ${res.data.msg}`
							})
						}
					}).catch(e => {
						console.log(e);
						throw e
					}) 
				} else {
					this.$ytHint.toast({
						title: `错误信息: ${res.data.msg}`
					})
				}
			}).catch(e => {
				console.log(e);
				throw e
			})
		}
	}
};
</script>

<style scoped>
.bottom {
	box-shadow: 0upx -10upx 20upx 0px rgba(185, 185, 185, 0.48);
}
</style>

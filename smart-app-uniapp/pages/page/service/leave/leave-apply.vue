<template>
	<view class="yt-page bgf">
		<view style="padding: 5upx 10upx;color:rgb(80, 139, 255);">
			须知：工作日内每周二、周四为办离职日（当天上午9:00带上离职交接书到人资招聘室处办理手续），每个月1日-7日不办离职。
		</view>
		<view class="mw h20 bgec"></view>
		<yt-list v-if="applyType == 1" :ytClass="['pr30', 'pl30']">
			<yt-list-item  @click="getItem" hideLastBorder=true hasMust showImage=false showArrow=false title="员工号" >
				<text class="f28pc2">{{ employeeIdInput || '请输入'}}</text>
			</yt-list-item>
			<yt-list-item v-if="employeeInfo.employeeName" hasMust showImage=false showArrow=false :title="employeeInfo.employeeName">
				<view class="f32pc">
					入职日期 {{employeeInfo.entryDate}}
				</view>
			</yt-list-item>
			<yt-list-item v-if="employeeInfo.buName" hasMust showImage=false showArrow=false :title="employeeInfo.buName">
				<view class="f32pc">
					{{employeeInfo.deptName}}
				</view>
			</yt-list-item>
		</yt-list>
		<view v-if="applyType == 1" class="mw h20 bgec"></view>
		<yt-list :ytClass="['pr30', 'pl30']">
			<yt-list-item
				showImage="false"
				hasMust
				showArrow=false
				title="申请离职日期"
				
			>
				<view class="f30 main-color">
					{{applyDate}}
				</view>
			</yt-list-item>
			<yt-list-item
				showImage=false
				hasMust
				title="离职日期"
				@click="onShowDatePicker('date', 'dimissionDate')"
			>
				<view class="f30pc2">
					{{applyInfo.dimissionDate || '请选择'}}
				</view>
			</yt-list-item>
			<yt-list-item showImage="false" hasMust title="离职原因">
				<yt-picker v-if="reasonNameArr.length>0" @changePicker="getPickerData" idType="reasonType" value="请选择" :arrData="reasonNameArr"></yt-picker>
			</yt-list-item>
			<yt-list-item showImage="false" hasMust title="离职类型">
				<yt-picker v-if="typeNameArr.length>0" @changePicker="getPickerData" idType="leaveType" value="请选择" :arrData="typeNameArr"></yt-picker>
			</yt-list-item>
			<yt-list-item hideLastBorder=true hasMust showArrow="false" title="剩余年假天数" showImage="false">
				<view class="f32 main-color">
					{{applyInfo.yearHoliday + '天'}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<view class="hcc mw mt80 mb120" @click="submit"><yt-button btnText="提交" btnWidth="180" btnHeight="54"></yt-button></view>
		<mx-date-picker :format="getCurDateFormat" :show="showPicker" :type="dateType" color="#5c92ff" @confirm="onSelected" @cancel="onSelected" />
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue';
import ytlistitem from '@/components/yt-list-item/index.vue';
import ytpicker from '@/components/yt-picker.vue';
import ytbutton from '@/components/yt-button/index.vue';
import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue';
import ytminemodel from '@/components/yt-mine-model.vue'
import hideModel from '@/mixins/hideModel.vue'
import { formatDate, flitterTime, dateMonment, formatDateTime} from '@/common/js/util.js'
import { storage } from '@/tools/storage.js';
import dimission from '@/api/api-dimission.js';
import {employee} from '@/api/api-mine.js'
export default {
	mixins:[hideModel],
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'yt-button': ytbutton,
		'mx-date-picker': MxDatePicker,
		'yt-mine-model': ytminemodel,
		'yt-picker': ytpicker
	},
	data() {
		return {
			modelObj: {
				title: '',
				input: '',
				idType: '', 
			},
			showPicker: false, // 显示事件日期选择器 
			type: 'rangetime', // 时间选择器的选择类型
			employeeIdInput: '', // 异常离职的方式 输入员工号拿到该员工信息
			applyType: '', // 申请方式
			typeNameArr: [],
			reasonNameArr: [],
			leaveTypeArr: [],
			leaveReasonArr: [],
			dateType: 'date',
			employeeInfo: {},
			applyInfo: {
				employeeId: '',
				dimissionReason: '',
				dimissionType: '',
				dimissionApplyType: 0,
				dimissionDate: '',
				yearHoliday: ''
			},
			applyDate: ''
		};
	},
	computed: {
		getCurDateFormat() {
			if (this.dateType === 'date') {
				return `yyyy-mm-dd`;
			} else if (this.dateType === 'datetime') {
				return 'yyyy-mm-dd hh:ii';
			}
		},
		showModel() {
			return this.$store.state.modelStatus
		}
	
	},
	onLoad (e) {
		const employeeId = storage.getSync(storage.USER_NAME) // 获取员工的id
		this.applyInfo.employeeId = employeeId
		this.applyInfo.dimissionApplyType = e.type
		this.applyDate = formatDate(new Date())
		if (e.type ==0) {
			const lastAmonth = Date.parse(new Date(flitterTime(this.applyDate)))+2592000000
			this.applyInfo.dimissionDate = dateMonment('Y-m-d',lastAmonth/1000) 
		}
		if (e.type) {
			this.applyType = e.type
			let type = parseInt(e.type)? '异常离职办理': '正常离职申请';
			uni.setNavigationBarTitle({
				// 设置title
				title: type
			});
		}
		this.getReleaseList();
		this.getTypeList();
		this.getRestTime();
	},
	onNavigationBarButtonTap(e) {
		uni.navigateTo({
			url: `./leave-record?type=${this.applyInfo.dimissionApplyType}`
		});
	},
	
	methods: {
		// 离职原因列表
		async getReleaseList() {
			const res = await dimission.dimissionReason();
			this.leaveReasonArr = res.data.data.records;
			this.reasonNameArr = this.leaveReasonArr.map(function(val) {
				return val.reasonName;
			});
		},
		// 类型列表
		async getTypeList() {
			const res = await dimission.dimissionTypeList();
			this.leaveTypeArr = res.data.data.records;
			this.typeNameArr = this.leaveTypeArr.map(function(val) {
				return val.typeName;
			});
		},
		async getEmployeeInfo(id) {
			try{
				const res = await employee.baseInfo(id)
				this.employeeInfo = res.data.data
				this.employeeInfo.entryDate = this.employeeInfo.entryDate.split(' ')[0]
				this.applyInfo.employeeId = id
			}catch(e){
				console.log(e);
				throw e
			}
		},
		// 获取离职原因/类型
		getPickerData(e) {
			if (e.idType === 'leaveType') {
				for (let i of this.leaveTypeArr) {
					if (i.typeName === e.value) {
						this.applyInfo.dimissionType = i.typeCode;
					}
				}
			} else if (e.idType === 'reasonType') {
				for (let i of this.leaveReasonArr) {
					if (i.reasonName === e.value) {
						this.applyInfo.dimissionReason = i.reasonCode;
					}
				}
			}
		},
		getItem (e) {
			this.$store.commit('ytModelStatus',true) // 显示弹出模态框
			this.modelObj = {
				title: e.title,
				input: e.tipContent,
				idType: e.type
			}
		},
		// 获取输入
		getData(e) {
			this.employeeIdInput = e.value
			this.getEmployeeInfo(e.value)
		},
		// 获取年假天数
		async getRestTime() {
			const res = await dimission.dimissionRestTime();
			this.applyInfo.yearHoliday = `${res.data.data.dayCount}`;
		},
		// 提交
		async submit() {
			let flag = true
			Object.keys(this.applyInfo).forEach(el => {
				if (this.applyInfo[el] == '') {
					console.log(this.applyInfo[el]);
					flag = false
					return
				}
			})
			console.log(this.applyInfo);
			if (!flag) {
				this.$ytSystem.vibrate({
					title: '带‘*’为必填项'
				})
				return
			}
			try{
				const res = await dimission.applyDimission(this.applyInfo)
				if (!res) return
				this.$ytHint.toast({
					title: '提交成功'
				})
				uni.redirectTo({
					url: `./leave-record?type=${this.applyInfo.dimissionApplyType}`
				})
			}catch(e){
				console.log(e);
			}
		},
		// 显示 时间日期选择器
		onShowDatePicker(type, $type) {
			//显示
			this.showPicker = true;
			this.dateType = type;
			this.curDateSelect = $type; // 当前选择的时间
		},
		onSelected(e) {
			//选择
			this.showPicker = false;
			this[this.type] = e.value;
			this.applyInfo.dimissionDate = e.value;
		}
	}
};
</script>

<style scoped>
.line {
	left: 18upx;
	height: 70%;
}
</style>

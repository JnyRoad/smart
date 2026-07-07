<template>
	<view class="yt-page bgf">
		<view v-if="status != 2" @click="onShowDatePicker('datetime')" class="hbc mw h90 pr30 pl30 border-bottom-D">
			<text class="f32pc bold">{{ typeText }}时间</text>
			<view class="hcc">
				<text class="f32 main-color mr20">{{ time?time:'请选择' }}</text>
				<image class="arrow w25 h30" src="/static/img/serive/recruit/nave.png" mode=""></image>
			</view>
		</view>
		<view v-if="applyStatus != 1 && status == 2"  @click="showReasonDraw" class="vcc mw pr30 pl30 border-bottom-D">
			<view class="hbc mw  mb20">
				<text class="f32pc bold">{{ typeText }}理由</text>
				<view class="hlc">
					<text  class="f32pc4">{{hasReason?'已选择':'请选择'}}</text>
					<image class="arrow w25 h30 ml30" src="/static/img/serive/recruit/nave.png" mode=""></image>
				</view>
			</view>
			<view v-if="reasonList.length > 0" class="mw hbc fw" >
				<block v-for="(item, index) in reasonList" :key="index">
					<view class="f28pc4 pl10 pr10 mb20 item hcc borderC mr10 rad10">{{item}}</view>
				</block>
			</view>
			<view v-if="reasonList.length == 0" class="mw pt20 pb20">
				<view>{{otherReason}}</view>
			</view>
		</view>
		<!-- 此功能暂缓  确认入职技能选择 -->
		<view v-if="false">
			<view @click="toSelectJob" v-if="applyStatus == 3 && status == 7"  class="hbc mw border-bottom-D h90 pl30 pr30">
				<text class="f32pc bold">入职岗位</text>
				<view class="hbc">
					<text class="f32pc2">{{jobName|| '请选择'}}</text>
					<view class="ml10 w20 h30 hcc">
						<image src="/static/img/mine/next.png" mode=""></image>
					</view>
				</view>
			</view>
		</view>
		<view :class="['list', applyStatus == 1 ? '' : 'listtop']">
			<manage-item v-if="list.length>0" :applyStatus="applyStatusDesc" :list="list" checked="false" noclick="true" />
			</view>
		<view class="bottom hcc bgf">
			<view class="f1 hcc mh">
				共:
				<text class="err-color f32">{{ list.length }}</text>
				个人员
			</view>
			<view @click="dispose" :class="[disabled?'ec-bg':'main-bg']" class="w210 mh main-bg hcc cf">确认{{ typeText }}</view>
		</view>
		<mx-date-picker format="yyyy-mm-dd hh:ii" :show="showPicker" :type="type" color:="#5c92ff" :show-tips="true" @confirm="onSelected" @cancel="onSelected" />
		<refuse-reason-draw @reasonData="getReason" @close="getStatus" :visible="showDawer"></refuse-reason-draw>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import manageItem from '@/components/manage-item.vue';
import mxDatepicker from '@/components/mx-datepicker/mx-datepicker.vue';
import ytpicker from '@/components/yt-picker.vue';
import refuseReasonDraw from '@/components/refuse-reason-draw.vue'
import recruit from '@/api/api-recruitment-management.js'
import { applyState, operationType } from '@/config/dictionaries.js';
import { RECURIT_APPLY, storage } from '@/tools/storage.js';
import { formatDateTime, flitterTime} from '@/common/js/util.js'
export default {
	components: {
		'manage-item': manageItem,
		'mx-date-picker': mxDatepicker,
		'yt-picker': ytpicker,
		'refuse-reason-draw': refuseReasonDraw
	},
	data() {
		return {
			disabled: true, // 是否禁用
			showPicker: false,
			hasReason: false,
			value: '',
			type: 'datetime',
			time: '', // 时间
			typeText: '', // 操作状态文本
			status: '', // 操作状态值
			applyStatus: '', // 当前简历的状态值
			applyStatusDesc: '', // 招聘状态描述
			list: [], // 筛选的应聘者数据
			reasonList: [], // 拒绝理由数组
			otherReason: '', // 其他原因
			showDawer: false,
			info: {
				applicationId: '',
				operationType: '',
				operationDesc: '',
				appointTime: ''
			},
			jobName: '', // 选择的岗位
		};
	},
	// 加载页面获取参数对应做出相应页面数据的更改
	onLoad(e) {
		this.info.operationType = e.operationType
		this.list = storage.getSync(RECURIT_APPLY);
		this.applyStatusDesc= applyState[e.applyState];
		this.applyStatus = e.applyState
		console.log(this.applyStatus);
		this.status = e.operationType; // 状态
		this.typeText = operationType[e.operationType];
		// 招聘  应聘状态  操作状态值请前往 config->dictionaries.js
		uni.setNavigationBarTitle({
			title: `${applyState[e.applyState]}确认${operationType[e.operationType]}`
		});
	},
	onShow() {
		// 此功能暂缓
		// const jobName = storage.getSync('selectJob')
		// console.log(jobName);
		// if (jobName) {
		// 	this.jobName = jobName
		// 	uni.removeStorage({
		// 		key: 'selectJob'
		// 	})
		// }
	},
	methods: {
		// 显示时间选择器
		onShowDatePicker(type) {
			//显示
			this.showPicker = true;
			this.value = this[type];
		},
		// 确认选获取数据
		onSelected(e) {
			//选择
			this.showPicker = false;
			const curTime = Date.parse(formatDateTime(new Date(), 'm'))
			if (e) {
				this[this.type] = e.value;
				const selectTime = Date.parse(new Date(flitterTime(e.value)))
				console.log(selectTime);
				console.log(curTime);
				if (selectTime < curTime) {
					this.$ytHint.toast({
						title: '时间选择错误，请选择未来时间'
					})
					return
				}
				this.time = e.value
				this.info.appointTime = e.value
				this.disabled = false
			}
		},
		showReasonDraw() {
			this.disabled = true
			this.showDawer = !this.showDawer
			if (this.reasonList.length >0 || this.otherReason) {
				this.reasonList = []
				this.hasReason = false
				this.otherReason = ''
				this.info.operationDesc= ''
			}
		},
		// 处理现实状态
		getStatus(e) {
			this.showDawer = e;
		},
		// 获取拒绝元原因
		getReason(e) {
			if (typeof e === 'string') {
				this.otherReason = e
				this.hasReason = true
				this.info.operationDesc = e
				this.disabled = false
			} else {
				this.reasonList = e
				let str = ''
				this.reasonList.forEach(el => {
					str += `${el},`
				})
				this.hasReason = true
				this.info.operationDesc = str.substr(0, str.length-1)
				this.disabled = false
			}
		},
		// 跳转统一处理页面
		dispose(operationType) {
			if (this.disabled) {
				if (this.status == '2') {
					this.$ytHint.toast({
						title: '请选择拒绝理由'
					})
				} else {
					this.$ytHint.toast({
						title: '请选择时间'
					})
				}
				return
			}
			let ids = ''
			this.list.forEach(el => {
				ids += `${el.applicationId}|`
			})
			ids = ids.substr(0,ids.length-1)
			this.info.applicationId = ids
			recruit.applicationOperation(this.info).then(res => {
				storage.removeSync(RECURIT_APPLY)
				if (res.data.data) {
					uni.redirectTo({
						url: `./success?operationType=${this.typeText}&applyState=${applyState[this.applyStatus]}`
					});
				}
			}).catch(err => {
				console.log(err);
				throw err
			})
		},
		// 选择岗位 暂缓
		toSelectJob() {
			uni.navigateTo({
				url: './select-job'
			});
		}
	}
};
</script>

<style scoped>
.list {
	padding-top: 20upx;
	padding-bottom: 90upx;
}
.bottom {
	position: fixed;
	bottom: 0;
	height: 90upx;
	width: 100%;
}
.timeselect {
	position: fixed;
	top: 88upx;
	/* #ifdef APP-PLUS */
	top: 0;
	/* #endif */
	height: 90upx;
}
.arrow {
	transform: rotateZ(-180deg);
}
.w210 {
	width: 210upx;
}

</style>

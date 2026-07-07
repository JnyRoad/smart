<template>
	<view class="yt-page bgf">
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item title="考勤月份" showImage="false" showArrow="false">
				<view class="f30pc2">
					{{patchDetail.patchMonth}}
				</view>
			</yt-list-item>
			<yt-list-item title="补卡日期" showImage="false" hideLastBorder=true showArrow="false">
				<view class="f30pc2">
					{{patchDetail.patchDate}}
				</view>
			</yt-list-item>
		</yt-list>
		<view v-if="patchCount>=4" class="mw h90 pl30 pr30 bgec hlc bt bb">
			提示：本月已补卡过 <text class="f32 err-color">{{patchCount}}</text> 次
		</view>
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item v-if="classInfo.secondEnter !== ''" title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">二入</text>
					<text class="f30 err-color">{{classInfo.secondEnter}}</text>
				</view>
			</yt-list-item>
			<yt-list-item v-if="classInfo.secondOut !== ''" title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">二出</text>
					<text class="f30 err-color">{{classInfo.secondOut}}</text>
				</view>
			</yt-list-item>
			<yt-list-item v-if="classInfo.fourthEnter !== ''" title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">四入</text>
					<text class="f30 err-color">{{classInfo.fourthEnter}}</text>
				</view>
			</yt-list-item>
			<yt-list-item v-if="classInfo.fourthOut !== ''" title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">四出</text>
					<text class="f30 err-color">{{classInfo.fourthOut}}</text>
				</view>
			</yt-list-item>
			<yt-list-item  v-if="classInfo.fifthEnter !== ''" title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">五入</text>
					<text class="f30 err-color">{{classInfo.fifthEnter}}</text>
				</view>
			</yt-list-item>
			<yt-list-item v-if="classInfo.fifthOut !== ''" title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">五出</text>
					<text class="f30 err-color">{{classInfo.fifthOut}}</text>
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item hasMust="false" title="二段出是否跨天" showImage="false" showArrow="false" v-if="classInfo.secondOutCover">
				<view class="f30pc2">
					{{classInfo.secondOutCover > 0 ? '是' : '否'}}
				</view>
			</yt-list-item>
			<yt-list-item hasMust="false" title="四段进是否跨天" showImage="false" showArrow="false" v-if="classInfo.fourthEnterCover">
				<view class="f30pc2">
					{{classInfo.fourthEnterCover > 0 ? '是' : '否'}}
				</view>
			</yt-list-item>
			<yt-list-item hasMust="false" title="四段出是否跨天" showImage="false" showArrow="false" v-if="classInfo.fourthOutCover">
				<view class="f30pc2">
					{{classInfo.fourthOutCover > 0 ? '是' : '否'}}
				</view>
			</yt-list-item>
			<yt-list-item hasMust="false" title="五段进是否跨天" showImage="false" showArrow="false" v-if="classInfo.fifthEnterCover">
				<view class="f30pc2">
					{{classInfo.fifthEnterCover > 0 ? '是' : '否'}}
				</view>
			</yt-list-item>
			<yt-list-item hasMust="false" title="五段出是否跨天" showImage="false" showArrow="false" v-if="classInfo.fifthOutCover">
				<view class="f30pc2">
					{{classInfo.fifthOutCover > 0 ? '是' : '否'}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item hasMust="false" title="原因" showImage="false" showArrow="false" >
				<view class="f30pc2" v-if="classInfo.causeDesc">
					{{classInfo.causeDesc}}
				</view>
			</yt-list-item>
			<yt-list-item hasMust="false" title="备注" showImage="false" showArrow="false" v-if="classInfo.remark">
				<view class="f30pc2">
					{{classInfo.remark}}
				</view>
			</yt-list-item>
			<yt-list-item v-if="classInfo.photoUrl" title="附件" @click="preImage" hideLastBorder=true showImage="false" showArrow="false" :hasRightImage="true" :rightImage="classInfo.photoUrl" :hasTip="!hasImage"></yt-list-item>
			
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-approval-process :flows="flows"></yt-approval-process>
		<!-- <view class="hcc mt40 mb40"><yt-button @click="apply" btnText="提交" btnWidth="180" btnHeight="54"></yt-button></view> -->
		<mx-date-picker format="yyyy-mm-dd hh:ii:ss" :show="showPicker" :type="type" color:="#5c92ff" :show-tips="true" :show-seconds="true" @confirm="onSelected"
		@cancel="onSelected" />
		<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="500" :maxWidth="1000" :ql="0.8" type="base64"></cpimg>
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue';
import ytlistitem from '@/components/yt-list-item/index.vue';
import MxDatePicker from '@/components/mx-datepicker/mx-datepicker.vue';
import ytpicker from '@/components/yt-picker.vue';
import ytbutton from '@/components/yt-button/index.vue';
import ytminemodel from '@/components/yt-mine-model.vue'
import cpimg from '@/components/cpimg.vue'
import ytapprovalprocess from '@/components/yt-approval-process.vue'
import attendance from '@/api/api-attendance.js';
import vacation from '@/api/api-vacate.js'
import {flitterTime, formatDate} from '@/common/js/util.js'
import {storage} from '@/tools/storage.js'
export default {
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'mx-date-picker': MxDatePicker,
		'yt-button': ytbutton,
		'yt-picker': ytpicker,
		'yt-mine-model': ytminemodel,
		'cpimg': cpimg,
		'yt-approval-process' : ytapprovalprocess
	},
	data() {
		return {
			modelObj: {
				title: '',
				input: '',
				idType: ''
			},
			hasImage: false, // 是否有附件
			image: '', // 附件
			reason: [], // 处理过的数据
			reasonData: [
				
			], // 补卡原因原始数据
			showPicker: false,
			value: '',
			type: 'datatime',
			patchDetail: {
				patchMonth: '',
				patchDate: '',
				patchReason: '',
				remark: '',
				photo: ''
			},
			submitData: {
				
			},
			list: ['否', '是'],
			classInfo: {},
			checkingInDetail: {},
			patchCount: 0 ,// 当月缺卡次数
			flows : []
		};
	},
	onLoad(e) {
		this.patchDetail.patchMonth = e.patchDate.split('-')[0] + '-' + e.patchDate.split('-')[1] // 考勤月份
		this.patchDetail.patchDate = e.patchDate // 补卡日期
		this.getReason() // 补卡原因
		this.getinfoFlow(e.recordId) //补卡审核流程
		this.getPatchCount(this.patchDetail.patchDate) // 当月补卡次数
		this.getClassInfo(e.recordId) // 当前的班次信息
		// this.getCheckingInDetail(this.patchDetail.patchDate) // 当前的考勤详情
	},
	computed: {
		showModel() {
			return this.$store.state.modelStatus;
		}
	},
	onNavigationBarButtonTap() {
		uni.navigateTo({
			url: './record'
		})
	},
	methods: {
		// 补卡流程信息
		async getinfoFlow ( recordId ) {
			const obj = {
				recordId: recordId
			}
			try{
				const res = await attendance.infoFlow(obj)
				if (!res) return
				this.flows = res.data.data
				console.log(this.flows);
			}catch(e){
				//TODO handle the exception
				console.log(e);
				throw e
			}
		},
		// 获取当前日期的补卡时间
		async getClassInfo(recordId) {
			const obj = {
				recordId : recordId
			}
			try{
				const res = await attendance.patchDetail(obj)
				if(!res) return
				res.data.data.data.className = res.data.data.data.causeDesc
				// console.log(res.data.data);
				this.classInfo = res.data.data.data
				console.log(this.classInfo);
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 获取当天的当天的考勤详情
		async getCheckingInDetail(date) {
			const obj = {
				queryDay: date
			}
			try{
				const res = await attendance.errorDetail(obj)
				if(!res) return
				this.checkingInDetail = res.data.data
				console.log(this.checkingInDetail);
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 获取补卡原因
		async getReason() {
			try{
				const res = await attendance.patchReason();
				if (!res) return
				this.reasonData = res.data.data.records;
				this.reason = this.reasonData.map((el,inx) => {
					return el.reasonName
				})
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 获取当月补卡信息
		async getPatchCount(date) {
			const obj = {
				patchDate: date
			}
			try{
				const res = await attendance.patchCount(obj)
				if (!res) return
				this.patchCount = parseInt(res.data.data.patchCount)
			}catch(e){
				console.log(e);
				throw e
				//TODO handle the exception
			}
		},
		// 预览图片
		preImage () {
			let that = this
			uni.previewImage({
				urls:[that.classInfo.photoUrl]
			})
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
			this.patchDetail.remark = e.value
		},
		// 提交补卡申请
		apply() {
			const patchDate = Date.parse(new Date(flitterTime(this.patchDetail.patchDate)))
			const curTime = Date.parse(new Date(flitterTime(formatDate(new Date()))))
			const coverTime = (curTime-patchDate)/86400000 // 判断是否跨天
			const isCover = coverTime > 0 ? '0' : '1'
			this.submitData.secondEnterCover = isCover
			this.submitData.secondOutCover = isCover
			this.submitData.fourthEnterCover = isCover
			this.submitData.fourthOutCover = isCover
			this.submitData.fifthEnterCover = isCover
			this.submitData.fifthOutCover = isCover
			let {classDesc, className, ...classInfo} = this.classInfo
			const obj = {
				...this.patchDetail,
				...this.submitData,
				...classInfo
			}
			if (!this.patchDetail.patchReason) {
				this.$ytHint.toast({
					title: '请选择补卡原因'
				})
				return
			}
			attendance.patch(obj).then(res => {
				if (!res) return
				storage.set('isRefresh',true)
				this.$ytHint.toast({
					title: res.data.msg,
					success: function () {
						uni.redirectTo({
							url: './record'
						})
					}
				})
			})
		},
		onShowDatePicker(type) {
			//显示
			this.showPicker = true;
			this.value = this[type];
		},
		onSelected(e) {
			//选择
			this.showPicker = false;
			if (e) {
				this[this.type] = e.value;
				this.applyData = e.value;
			}
		},
		getPickerData(e) {
			switch (e.idType) {
				case 'reason':
					this.reasonData.forEach(el => {
						if (el.reasonName == e.value) {
							this.patchDetail.patchReason = el.reasonCode
							return
						}
					})
				default:
					break;
			}
		},
		// 处理数据
		getValue(value) {
			for (let i = 0, len = this.list.length; i < len; i++) {
				if (this.list[i] === value) {
					return i;
				}
			}
		},
		// 处理附件上传
		update () {
			this.$refs.cpimg._changImg()
		},
		cpimgOk(file) {
			if (file) {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理成功'
				})
				this.hasImage = true
				this.image = file
				this.patchDetail.photo = file.split(',')[1]
			}
			
		},
		cpimgErr(e) {
			if (e) {
				uni.hideLoading()
				this.$ytHint.toast({
					title: '处理失败,请重试'
				})
				this.hasRightIcon = true
				this.hasRightImage = false
				throw e
			}
		},
		// 获取输入内容
		getInputTxt(e) {
			this.submitData.remark = e.text
		}
	}
};
</script>

<style scoped>
.msg {
	width: 630upx;
	box-shadow: 0px 5upx 49upx 0px rgba(185, 185, 185, 0.48);
}
</style>

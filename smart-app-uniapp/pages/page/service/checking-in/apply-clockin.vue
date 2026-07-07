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
		<view v-if="patchCount>=4" class="mw h70 pl30 pr30 bgec hlc bt bb">
			提示：本月已补卡过 <text class="f32 err-color">{{patchCount}}</text> 次
		</view>
		<!-- <view class="mw pl30 pr30 bgec hlc bt bb">
			<text>5入5出为加班卡，不能进行系统补卡，请与部门文员确认后补卡</text>
		</view> -->
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
			<!-- <yt-list-item  v-if="classInfo.fifthEnter !== '' && checkingInDetail.fifthEnter == '' " title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">五入</text>
					<text class="f30 err-color">{{classInfo.fifthEnter}}</text>
					<text class="err-color" @click="deleteTime('fifthEnter')">移除</text>
				</view>
			</yt-list-item>
			<yt-list-item v-if="classInfo.fifthOut !== '' && checkingInDetail.fifthOut == ''" title="补卡时间点" showImage="false" showArrow="false">
				<view class="hbc">
					<text class="f30pc">五出</text>
					<text class="f30 err-color">{{classInfo.fifthOut}}</text>
					<text class="err-color" @click="deleteTime('fifthOut')">移除</text>
				</view>
			</yt-list-item> -->
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item title="二段出是否跨天" showImage="false" v-if="classInfo.secondOut !== ''">
				<yt-picker @changePicker="getPickerData" idType="kuatian1" value="请选择" :arrData="kuatian" v-if="!submitData.secondOutCover"></yt-picker>
				<yt-picker @changePicker="getPickerData" idType="kuatian1" :value="submitData.secondOutCover > 0 ? '是' : '否'" :arrData="kuatian" v-else></yt-picker>
			</yt-list-item>
			<yt-list-item title="四段进是否跨天" showImage="false" v-if="classInfo.fourthEnter !== ''">
				<yt-picker @changePicker="getPickerData" idType="kuatian2" value="请选择" :arrData="kuatian" v-if="!submitData.fourthEnterCover"></yt-picker>
				<yt-picker @changePicker="getPickerData" idType="kuatian2" :value="submitData.fourthEnterCover > 0? '是' : '否'" :arrData="kuatian" v-else></yt-picker>
			</yt-list-item>
			<yt-list-item title="四段出是否跨天" showImage="false" v-if="classInfo.fourthOut !== ''">
				<yt-picker @changePicker="getPickerData" idType="kuatian3" value="请选择" :arrData="kuatian" v-if="!submitData.fourthOutCover"></yt-picker>
				<yt-picker @changePicker="getPickerData" idType="kuatian3" :value="submitData.fourthOutCover > 0? '是' : '否'" :arrData="kuatian" v-else></yt-picker>
			</yt-list-item>
			<!-- <yt-list-item title="五段进是否跨天" showImage="false">
				<yt-picker @changePicker="getPickerData" idType="kuatian4" value="请选择" :arrData="kuatian" v-if="!submitData.fifthEnterCover"></yt-picker>
				<yt-picker @changePicker="getPickerData" idType="kuatian4" :value="submitData.fifthEnterCover > 0? '是' : '否'" :arrData="kuatian" v-else></yt-picker>
			</yt-list-item>
			<yt-list-item title="五段出是否跨天" showImage="false">
				<yt-picker @changePicker="getPickerData" idType="kuatian5" value="请选择" :arrData="kuatian" v-if="!submitData.fifthOutCover"></yt-picker>
				<yt-picker @changePicker="getPickerData" idType="kuatian5" :value="submitData.fifthOutCover > 0? '是' : '否'" :arrData="kuatian" v-else></yt-picker>
			</yt-list-item> -->
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item title="原因" hasMust showImage="false"><yt-picker v-if="reason.length>0" @changePicker="getPickerData" idType="reason" value="请选择" :arrData="reason"></yt-picker></yt-list-item>
			<yt-list-item @click="getInput" title="备注" showImage="false" showArrow="false" >
				<view class="f30pc2">
					{{patchDetail.remark || '请输入'}}
				</view>
			</yt-list-item>
			<yt-list-item @click="update" title="附件" hideLastBorder=true showImage="false" showArrow="false" :hasRightImage="hasImage" :rightImage="image" :hasTip="!hasImage" tipContent="请上传"></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<view class="hcc mt40 mb40"><yt-button @click="apply" btnText="提交" btnWidth="180" btnHeight="54"></yt-button></view>
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
		'cpimg': cpimg
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
			kuatian : ['是','否'],
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
			patchCount: 0 // 当月缺卡次数
		};
	},
	onLoad(e) {
		this.patchDetail.patchMonth = e.patchDate.split('-')[0] + '-' + e.patchDate.split('-')[1] // 考勤月份
		this.patchDetail.patchDate = e.patchDate // 补卡日期
		// const patchDate = Date.parse(new Date(flitterTime(this.patchDetail.patchDate)))
		// const curTime = Date.parse(new Date(flitterTime(formatDate(new Date()))))
		// const coverTime = (curTime-patchDate)/86400000 // 判断是否跨天
		// const isCover = coverTime > 0 ? '' : '1'
		this.submitData.secondOutCover = ''
		this.submitData.fourthEnterCover = ''
		this.submitData.fourthOutCover = ''
		this.submitData.fifthEnterCover = ''
		this.submitData.fifthOutCover = ''
		this.getReason() // 补卡原因
		this.getPatchCount(this.patchDetail.patchDate) // 当月补卡次数
		this.getClassInfo(this.patchDetail.patchDate) // 当前的班次信息
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
		// 获取当前日期的补卡时间
		async getClassInfo(curDate) {
			let username = storage.getSync(storage.USER_NAME)
			const obj = {
				patchDate: curDate
			}
			try{
				const res = await attendance.patchQuery(obj)
				if(!res) return
				res.data.data.className = res.data.data.classDesc
				this.classInfo = res.data.data
				this.classInfo.fifthEnter  = ''
				this.classInfo.fifthOut  = ''
				let data = uni.getStorageSync('applyClockin')
				if (!data.secondEnter) {
					this.classInfo.secondEnter = ''
				}
				if (!data.secondOut) {
					this.classInfo.secondOut = ''
				}
				if (!data.fourthEnter) {
					this.classInfo.fourthEnter = ''
				}
				if (!data.fourthOut) {
					this.classInfo.fourthOut = ''
				}
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
			// const patchDate = Date.parse(new Date(flitterTime(this.patchDetail.patchDate)))
			// const curTime = Date.parse(new Date(flitterTime(formatDate(new Date()))))
			// const coverTime = (curTime-patchDate)/86400000 // 判断是否跨天
			// const isCover = coverTime > 0 ? '0' : '1'
			// this.submitData.secondEnterCover = isCover
			// this.submitData.secondOutCover = isCover
			// this.submitData.fourthEnterCover = isCover
			// this.submitData.fourthOutCover = isCover
			// this.submitData.fifthEnterCover = isCover
			// this.submitData.fifthOutCover = isCover
			// let that = this
			// for(let key in that.classInfo){
			// 	if (key != 'classDesc' && key != 'className'){
			// 		if (that.classInfo[key] != '' && that.checkingInDetail[key] != '') {
			// 			that.classInfo[key] = ''
			// 		}
			// 	}
			// }
			if (this.classInfo.patchDate == '') {
				delete this.classInfo.patchDate
			}
			if (this.classInfo.patchMonth == '') {
				delete this.classInfo.patchMonth
			}
			let {classDesc, className, ...classInfo} = this.classInfo
			const obj = {
				...this.patchDetail,
				...this.submitData,
				...classInfo
			}
			console.log(obj);
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
					title: '申请成功',
					success: function () {
						uni.redirectTo({
							url: './record'
						})
					}
				})
			})
		},
		// 移除补卡时间的
		deleteTime (data) {
			this.$ytHint.modal({
				content : "确认移除?",
				confirm : () => {
					this.classInfo[data] = ""
				}
			})
			// this.classInfo[data] = ""
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
			console.log(e);
			switch (e.idType) {
				case 'reason':
					this.reasonData.forEach(el => {
						if (el.reasonName == e.value) {
							this.patchDetail.patchReason = el.reasonCode
							return
						}
					})
					break;
				case 'kuatian1':
					if (e.value == '是') {
						this.submitData.secondOutCover = '1'
					} else {
						this.submitData.secondOutCover = '0'
					}
					break;
				case 'kuatian2':
					if (e.value == '是') {
						this.submitData.fourthEnterCover = '1'
					} else {
						this.submitData.fourthEnterCover = '0'
					}
					break;
				case 'kuatian3':
					if (e.value == '是') {
						this.submitData.fourthOutCover = '1'
					} else {
						this.submitData.fourthOutCover = '0'
					}
					break;
				case 'kuatian4':
					if (e.value == '是') {
						this.submitData.fifthEnterCover = '1'
					} else {
						this.submitData.fifthEnterCover = '0'
					}
					break;
				case 'kuatian5':
					if (e.value == '是') {
						this.submitData.fifthOutCover = '1'
					} else {
						this.submitData.fifthOutCover = '0'
					}
					break;
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

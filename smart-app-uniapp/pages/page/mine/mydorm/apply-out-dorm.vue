<template>
	<view class="yt-page">
		<view class="f32pc2 bold pt30 pr30 pb30 pl30">温馨提示: 需要填写详细外宿地址信息,地址不详,有可能被拒</view>
		<view class="textarea rad20 overflow auto"><textarea class="mw mh" v-model="outAddress" placeholder="外宿地址" style="padding: 10upx;"/></view>
		<view class="ml30 mt40">
			<label class="hlc">
				<view class="" @click="changeStatus"><checkbox value="cb" :checked="check" color="#5c92ff"/></view>
				<view @click.stop="toAgreement" class="main-color f28 f1 ml30">请阅读并同意《员工外宿协议》</view>
			</label>
		</view>
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item  hasMust=true showImage=false   title="补贴开始日期">
				<rattenking-dtpicker
					v-if="startDate"
					fields="day"
					:start="startDate"
					:end="endDate"
					:value="startTime"
					@change="bindChange($event,'start')"
				></rattenking-dtpicker>
			</yt-list-item>
			<yt-list-item  hasMust=false showImage=false title="补贴结束日期">
				<rattenking-dtpicker
					v-if="startDate"
					fields="day"
					:start="startDate"
					:end="endDate"
					:value="endTime"
					:dada = "true"
					@change="bindChange($event,'end')"
				></rattenking-dtpicker>
			</yt-list-item>
			<yt-list-item hasMust=true showArrow=false showImage=false title="补贴类型">
				<text class="f28pc2">{{allowance.allowanceType}}</text>
			</yt-list-item>
			<yt-list-item hasMust=true  showArrow=false showImage=false title="计算规则">
				<text class="f28pc2">{{allowance.computaionRule}}</text>
			</yt-list-item>
			<yt-list-item hasMust=true  showArrow=false showImage=false title="补贴金额">
				<text class="f28pc2">{{allowance.amount}}</text>
			</yt-list-item>
			<yt-list-item @click="getItem" hasMust=false idType="explain" showArrow=false showImage=false title="补贴说明">
				<text class="f28pc2">{{explain || '请输入'}}</text>
			</yt-list-item>
			<yt-list-item @click="getItem"  idType="remark"  showArrow=false showImage=false title="备注">
				<text class="f28pc2">{{remark || '请输入'}} </text>
			</yt-list-item>
		</yt-list>
		<view class="hcc mt80 mb40" @click="applyOutRoom"><yt-button  btnText="下一步" btnWidth="180" btnHeight="54"></yt-button></view>
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<yt-loading></yt-loading>
		<view class="hcc mark" v-if="showW">
			<view class="wozhidaole" style="width: 85%;padding-bottom: 30upx;">
				<view class="hcc mw border-bottom" style="height: 100upx;border-bottom: 1px solid #E0E0E0;">
					外宿申请说明
				</view>
				<view style="padding: 20upx;">
					<view>
						1、临时工、实习期员工不得申请外宿补贴
					</view>
					<view>
						2、每月25日（含）之前完成申请，补贴次月1号生效。
					</view>
					<view>
						3、如在25日之后完成申请，下下月（第三月）1号生效。
					</view>
					<view>
						4、申请开始月份，必须是整月，填写当月1号即可，如2019-10-01。
					</view>
					<view>
						5、结束月份，不是必填，可不填
					</view>
				</view>
				<view class="mw hcc" @click="hideW">
					<view class="hcc" style="width: 50%;height: 60upx;background-color: #169BD5;border-radius: 10upx;color: #FFFFFF;">
						<text>我知道了</text>
					</view>
				</view>
			</view>
		</view>
		
	</view>
</template>

<script>
import ytlist from '@/components/yt-list/index.vue'
import ytlistitem from '@/components/yt-list-item/index.vue'	
import ytbutton from '@/components/yt-button/index.vue';
import ruiDatePicker from '@/components/rattenking-dtpicker/rattenking-dtpicker.vue';
import ytminemodel from '@/components/yt-mine-model.vue'
import { mapState, mapMutations } from 'vuex';
import { employee } from '@/api/api-mine.js'
import { storage } from '@/tools/storage.js'
export default {
	components: {
		'yt-list': ytlist,
		'yt-list-item': ytlistitem,
		'yt-button': ytbutton,
		'yt-mine-model': ytminemodel,
		'rattenking-dtpicker': ruiDatePicker
	},
	data() {
		return {
			modelObj: {
				title: '',
				input: '',
				idType: '', 
			},
			showW : true,
			outAddress: '', // 外宿地址
			allowance: {}, // 补贴信息
			value: '',
			check: false,
			startDate: '',
			startTime: '',
			endTime: '',
			endDate: '',
			date: '',
			explain: '', // 说明
			remark: '' ,// 备注
			isShow: false,
			type: '' // 开始结束时间
		};
	},
	onLoad() {
		this.getOutRoomAllowance()
		const time = new Date()
		let year = time.getFullYear()
		let month = time.getMonth() + 1
		let day = time.getDate()
		if (day > 25) {
			month += 1
		}
		let endMonth = month
		this.endTime = `${year+1}-${endMonth>9?endMonth:'0'+endMonth}-01`
		month = month>9?month:`0${month}`
		day = day>9?day:`0${day}`
		this.startDate = `${year}-${month}-01`
		this.startTime = this.startDate
		this.endDate = `${year+10}-${month}-01`
	},
	computed: {
		showModel() {
			return this.$store.state.modelStatus
		},
		...mapState(['dormStatus'])
		
	},
	methods: {
		// 申请外宿
		applyOutRoom () {
			let status = storage.getSync(storage.USER_STATUS)
			if (status != 1 ) {
				this.$ytHint.toast({
					title: '只有在职的正式工,才可申请'
				})
				return
			}
			if (this.dormStatus == 1) {
				this.$ytHint.toast({
					title: '您已经申请了内宿'
				})
				return
			}
			if (!this.outAddress) {
				this.$ytHint.toast({
					title: '请输入外宿地址'
				})
				return
			} else if (!this.check) {
				this.$ytHint.toast({
					title: '请同意《员工外宿协议》'
				})
				return
			} else if (!this.endTime) {
				this.$ytHint.toast({
					title: '请选择补贴结束月份'
				})
				return
			}  else if (this.startTime < this.startDate) {
				this.$ytHint.toast({
					title: '补贴开始月份不能小于当前月份'
				})
				return
			} else if (this.startTime > this.endTime) {
				this.$ytHint.toast({
					title: '补贴结束月份不能小于补贴开始月份'
				})
				return
			}
			const obj = {
				outAddress: this.outAddress,
				startTime: this.startTime,
				endTime: this.endTime,
				explain: this.explain,
				remark: this.remark,
				...this.allowance,
			}
			employee.applyOutRoom(obj).then(res => {
				if (!res) return
				uni.redirectTo({
					url: './out-dorm-success'
				})
			})
		},
		hideW () {
			this.showW = false
		},
		// 获取外宿补贴
		async getOutRoomAllowance() {
			try{
				const res = await employee.getOutRoomAllowance()
				this.allowance = res.data.data
			}catch(e){
				console.log(e);
				throw e
			}
		},
		changeStatus(e) {
			this.check = !this.check;
			
		},
		toAgreement() {
			uni.navigateTo({
				url: './agreement'
			});
		},
		getItem (e) {
			console.log(e);
			this.$store.commit('ytModelStatus',true) // 显示弹出模态框
			this.modelObj = {
				title: e.title,
				input: e.tipContent,
				idType: e.type
			}
		},
		// 获取输入
		getData(e) {
			console.log(e);
			if (e.idType === 'explain') {
				this.explain = e.value
			} else if (e.idType === 'remark'){
				this.remark = e.value
			}
		},
		bindChange(e,type) {
			if (type === 'start') {
				if (this.endTime) {
					if (e > this.endTime) {
						this.$ytHint.toast({
							title: '开始时间不能大于结束时间'
						})
					}
					
				} 
				if (e < this.startDate) {
					this.$ytHint.toast({
						title: '补贴开始月份不能小于于当前月份'
					})
				}
				this.startTime = e
			} else if (type === 'end') {
				if (e < this.startTime) {
					this.$ytHint.toast({
						title: '补贴结束月份不能小于补贴开始月份'
					})
				}
				const time = new Date()
				this.endTime = e
			}
			
		}
	}
};
</script>

<style lang="scss" scoped>
.yt-page {
	background-color: #fff;
	.textarea {
		width: 630upx;
		height: 266upx;
		padding: 20upx;
		box-shadow: 0px 5upx 49upx 0px rgba(185, 185, 185, 0.48);
	}
}
.mark {
	top: 0;
	left: 0;
	bottom: 0;
	right: 0;
	position: fixed;
	background-color: rgba(0,0,0,.5);
	.wozhidaole {
		background-color: #FFFFFF;
		border-radius: 10upx;
	}
}
</style>

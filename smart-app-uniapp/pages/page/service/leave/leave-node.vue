<template>
	<view v-if="info.employeeName" class="mw bgf">
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage="false" showArrow="false" :title="info.employeeName">
				<view class="f30pc">{{info.employeeId}}</view>
			</yt-list-item>
			<yt-list-item showImage="false" showArrow="false" title="入职日期">
				<view class="f30pc">{{info.entryTime}}</view>
			</yt-list-item>
			<yt-list-item showImage="false" showArrow="false" :title="info.buName">
				<view class="hbc">
					<view class="f30pc">{{info.deptName}}</view>
					<view class="f30pc">{{info.jobName}}</view>
				</view>
			</yt-list-item>
			<yt-list-item showImage="false" showArrow="false" title="申请离职日期" :tipContent="info.dismissionDate"></yt-list-item>
			<yt-list-item showImage="false" showArrow="false" title="离职原因" :tipContent="info.dismissionReasonDesc"></yt-list-item>
			<yt-list-item showImage="false" showArrow="false" title="离职类型" :tipContent="info.dismissionTypeDesc"></yt-list-item>
			<yt-list-item showImage="false" showArrow="false" hideLastBorder="true" title="审批状态">
				<view class="f30 main-color">
					已通过
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h80 hlc pr30 pl30 bgec">
			<view class="f32pc ">
				离职交接书-责任部门-{{handover.deptName}}
			</view>
		</view>
		<view class="mw bgf">
			<view class="mw h100 hbc border-bottom-D">
				<view class="f1 f32pc hcc">交接项目</view>
				<view class="f1 f32pc hcc">金额</view>
				<view class="f1 f32pc hcc">说明</view>
			</view>
			<view class="mw pl30 pr30">
				<block v-for="(item, index) in handover.handItem" :key="index">
					<view class="mw hbc h100 border-bottom-D">
						<view class="f1">{{item.itemName}}</view>
						<view class="f1 ml20 mr20 borderC">
							<input class="mw" type="text" v-model="item.itemAmt" />
						</view>
						<view class="f1 borderC">
							<input class="mw" type="text" v-model="item.itemDesc" />
						</view>
					</view>
				</block>
			</view>
			
		</view>
		<view class="mw h20 bgec"></view>
		<view v-if="type==0" class="hcc mw mt40 mb40" @click="applyHandover"><yt-button btnText="提交" btnWidth="180" btnHeight="54"></yt-button></view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
import ytList from '@/components/yt-list/index.vue';
import ytListItem from '@/components/yt-list-item/index.vue';
import ytbutton from '@/components/yt-button/index.vue';
import dimission from '@/api/api-dimission.js'
export default {
	components: {
		'yt-list': ytList,
		'yt-list-item': ytListItem,
		'yt-button': ytbutton
	},
	data() {
		return {
			info: {}, // 员工信息
			handover: {} ,// 交接项,
			approveId: '',
			type: '', // 0 为 交接  1 为查看
		};
	},
	onLoad(e) {
		const { approveName, approveId} = e
		this.approveId = approveId
		this.type = e.type
		uni.setNavigationBarTitle({
			title: approveName || '离职交接'
		})
		this.getLeaveNode(approveId)
	},
	methods: {
		async getLeaveNode(approveId) {
			try{
				const res = await dimission.workHnadGet(approveId)
				this.info = res.data.data.employee
				this.handover = res.data.data.handover
			}catch(e){
				//TODO handle the exception
			}
		},
		// 提交交接内容项目
		async applyHandover () {
			const handItem = []
			if (!this.handover) {
				return
			}
			this.handover.handItem.forEach(el => {
				let {itemId, itemAmt, itemDesc} = el
				itemAmt = parseInt(itemAmt)
				handItem.push({itemId, itemAmt, itemDesc})
			})
			const obj = {
				processId: this.approveId,
				handItem: handItem
			}
			try{
				const res = await dimission.workHandSubmit(obj)
				uni.setStorageSync('hasRefresh',true)
				uni.navigateBack({
					delta: 1
				})
			}catch(e){
				//TODO handle the exception
			}
		}
	}
};
</script>

<style></style>

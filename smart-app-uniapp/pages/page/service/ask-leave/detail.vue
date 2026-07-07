<template>
	<view v-if="askLeaveDetail.employeeId" class="pb200 bgf rel mw">
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false :title="askLeaveDetail.employeeName">
				<view class="f30pc bold">
					{{askLeaveDetail.employeeBadge}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="所属公司">
				<view class="f30pc bold">
					{{askLeaveDetail.buName}}
				</view>
			</yt-list-item>
			<yt-list-item hideLastBorder=true showImage=false showArrow=false :title="askLeaveDetail.deptName">
				<view class="f30pc bold">
					{{askLeaveDetail.jobName}}
				</view>
			</yt-list-item>
			
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item hideLastBorder=true showImage=false showArrow=false title="请假类型" :tipContent="askLeaveDetail.vacateTypeDesc"></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false title="开始时间" :tipContent="askLeaveDetail.startDate"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="结束时间" :tipContent="askLeaveDetail.endDate"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="时长">
				<view class="f30pc2">
					{{askLeaveDetail.vacateCount}}{{askLeaveDetail.unit?askLeaveDetail.unit:''}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="请假原因" :tipContent="askLeaveDetail.vacateDesc"></yt-list-item>
			<yt-list-item hideLastBorder=true showImage=false showArrow=false title="班次" :tipContent="askLeaveDetail.className"></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-list :ytClass="['pl30', 'pr30']">
			<yt-list-item hasTitle="false" showImage="false" showArrow="false">
				<view class="hbc">
					<view class="hbc">
						<text class="f30pc bold mr40">二入</text>
						<text class="f30pc">{{ askLeaveDetail.secondEnter }}</text>
					</view>
					<view class="hbc">
						<text class="f30pc bold mr40">二出</text>
						<text class="f30pc">{{ askLeaveDetail.secondOut }}</text>
					</view>
				</view>
			</yt-list-item>	
			<yt-list-item hasTitle="false" showImage="false" showArrow="false">
				<view class="hbc">
					<view class="hbc">
						<text class="f30pc bold mr40">四入</text>
						<text class="f30pc">{{ askLeaveDetail.fourthEnter }}</text>
					</view>
					<view class="hbc">
						<text class="f30pc bold mr40">四出</text>
						<text class="f30pc">{{ askLeaveDetail.fourthOut }}</text>
					</view>
				</view>
			</yt-list-item>
			<yt-list-item hasTitle="false" showImage="false" showArrow="false">
				<view class="hbc">
					<view class="hbc">
						<text class="f30pc bold mr40">五入</text>
						<text class="f30pc">{{ askLeaveDetail.fifthEnter }}</text>
					</view>
					<view class="hbc">
						<text class="f30pc bold mr40">五出</text>
						<text class="f30pc">{{ askLeaveDetail.fifthOut }}</text>
					</view>
				</view>
			</yt-list-item>
			<yt-list-item hideLastBorder=true @click="previewPhoto(askLeaveDetail.photo)" title="附件" showImage="false" showArrow="false" hasRightImage=true :rightImage="askLeaveDetail.photo" hasTip=false></yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<yt-approval-process :flows="flows"></yt-approval-process>
		<show-read :showRead="showReadStatus"></show-read>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytapprovalprocess from '@/components/yt-approval-process.vue'
	import ytapprovalsuccess from '@/components/yt-approval-success.vue'
	import showRead from '@/components/show-read.vue'
	import vacation from '@/api/api-vacate.js'
	import { nodeState } from '@/config/dictionaries.js'
	export default {
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-approval-process':ytapprovalprocess,
			'yt-approval-success': ytapprovalsuccess,
			'show-read': showRead
		},
		data () {
			return {
				approval: false, // 是否通过申请
				askLeaveDetail: {},
				flows:[], // 审批流程
				recoredId: '', // 审批流程id
				showReadStatus: false
			}
		},
		onLoad (e) {
			if (e.readState && e.readState == 1) {
				this.showReadStatus = true // 消息模块过来的才有这个状态
			}
			this.getAskLeaveDetail({recordId: e.recordId})
		},
		methods: {
			async getAskLeaveDetail (obj) {
				try{
					const res = await vacation.vacateRecordDetail(obj)
					if (!res) return
					this.askLeaveDetail = res.data.data.employee
					res.data.data.flow.forEach(el => {
						el.nodeStatus = nodeState[el.nodeState]
					})
					this.flows = res.data.data.flow
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 预览图片
			previewPhoto(url) {
				uni.previewImage({
					current: '0',
					urls: [url]
				})
			}
		},
	}
</script>



<template>
	<view class="mw pb200 bgf rel">
		<yt-list v-if="businessDetail.employeeName" :ytClass="['pr30 pl30']">
			<yt-list-item showImage=false showArrow=false :title="businessDetail.employeeName">
				<view class="f30pc bold">
					{{businessDetail.pedestrianBadge}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="公司">
				<view class="f30pc bold">
					{{businessDetail.compName}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false :title="businessDetail.deptName">
				<view class="f30pc bold">
					{{businessDetail.jobName}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="代办人">
				<view class="f30pc">
					{{businessDetail.agentName}}-{{businessDetail.agentBadge}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="申请部门编码">
				<view class="f30pc">
					{{businessDetail.depId}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="出行时间">
				<view class="f30pc">
					{{businessDetail.pedestrianTime}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="申请时间">
				<view class="f30pc">
					{{businessDetail.applicationTime}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="结束时间">
				<view class="f30pc">
					2019年9月
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="出差类型">
				<view class="f30pc">
					{{businessDetail.tripTypeDesc}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="出差时间">
				<view class="f30pc vrc">
					<text>{{businessDetail.tripBeginTime}}</text>
					<text>{{businessDetail.tripBeginTime}}</text>
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="出差达成效果确认人员">
				<view class="f30pc">
					{{businessDetail.confirmName}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="出差原因">
				<view class="f30pc">
					{{businessDetail.tripReason}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="实际返回时间">
				<view class="f30pc">
					{{businessDetail.actualReturnTime}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="是否需订机票">
				<view class="f30pc">
					{{businessDetail.isBookingDesc}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="CRM客户">
				<view class="f30pc">
					{{businessDetail.customerId}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="客户名称">
				<view class="f30pc">
					{{businessDetail.customerName}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="预算项余额">
				<view class="f30pc">
					{{businessDetail.budgetBalance}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="酒店单价">
				<view class="f30pc">
					{{businessDetail.hotelUnitPrice}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="酒店住宿天数">
				<view class="f30pc">
					{{businessDetail.inHotelDays}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="交通费用">
				<view class="f30pc">
					{{businessDetail.transCost}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="短途交通费用">
				<view class="f30pc">
					{{businessDetail.shortTranCost}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false showArrow=false title="餐补费用">
				<view class="f30pc">
					{{businessDetail.mealCost}}
				</view>
			</yt-list-item>
			<yt-list-item showImage=false hideLastBorder=true showArrow=false title="单据备注">
				<view class="f30pc">
					{{businessDetail.remark}}
				</view>
			</yt-list-item>
		</yt-list>
		<view v-if="infoDay.length>0" class="mw bge">
			<view @click="show1 = !show1" class="show-head mw hbc h90 pr30 pl30">
				<text class="f30pc">出差行程</text>
				<text class="f30pc">查看</text>
			</view>
			<block v-for="(item, index) in infoDay" :key="index">
				<view  class="hide-content mt10 mw bgf pr30 pl30" :class="{showContent: show1, mt0: index == 0}">
					<view class="hbc h60">
						<text class="f30pc">出发时间</text>
						<text class="f30pc">{{item.departureTime}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">到达时间</text>
						<text class="f30pc">{{item.arrivalTime}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">出发城市</text>
						<text class="f30pc">{{item.departureCity}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">到达城市</text>
						<text class="f30pc">{{item.arrivalCity}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">交通方式</text>
						<text class="f30pc">{{item.transportLargeClassDesc}}-{{item.transportSubClassDesc}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">平均机票价格</text>
						<text class="f30pc">{{item.averageTicketPrice}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">实际机票费用</text>
						<text class="f30pc">{{item.actualTicketPrize}}</text>
					</view>
				</view>
			</block>
		</view>
		<view v-if="infoReport.length>0" class="mw bge">
			<view @click="show2 = !show2" class="show-head mw hbc h90 pr30 pl30">
				<text class="f30pc">出差报告</text>
				<text class="f30pc">查看</text>
			</view>
			<block v-for="(item, index) in infoReport" :key="index">
				<view :class="{showContent: show2}" class="hide-content mw bgf  pr30 pl30">
					<view class="hbc h60">
						<text class="f30pc">日期</text>
						<text class="f30pc">{{item.tripTime}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">出差行程</text>
						<text class="f30pc">{{item.businessTrip}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">工作项目</text>
						<text class="f30pc">{{item.workItem}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">预计效果</text>
						<text class="f30pc">{{item.expectedEffect}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">达成部门效果确认</text>
						<text class="f30pc">{{item.confirmDep}}</text>
					</view>
					<view class="hbc h60">
						<text class="f30pc">建议事项</text>
						<text class="f30pc">{{item.recommendations}}</text>
					</view>
				</view>
			</block>
		</view>
		<yt-approval-process v-if="infoFlow.length>0" :flows="infoFlow"></yt-approval-process>
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
	import business from '@/api/api-travel.js'
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
				businessDetail: {}, // 详情
				show1: true,
				show2: true,
				recordId: '',
				infoDay: [], // 出差日程
				infoReport: [], // 出差报告
				infoFlow: [] ,// 出差审批流程
				showReadStatus: false
			}
		},
		onLoad (e) {
			this.recordId = e.recordId
			this.getBusinessDetail()
			this.getTravelInfoDay()
			this.getTravelInfoReport()
			this.getTravelInfoFlow()
			if (e.readState != undefined && e.readState == 1) {
				this.showReadStatus = true
			}
		},
		methods: {
			// 获取出差的详情
			async getBusinessDetail () {
				try{
					const res = await business.travelRecordDetail(this.recordId)
					if (!res) return
					this.businessDetail = res.data.data
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 获取出差日程
			async getTravelInfoDay () {
				try{
					const res = await business.travelInfoDay(this.recordId)
					if(!res) return
					this.infoDay = res.data.data
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 获取出差报告
			async getTravelInfoReport () {
				try{
					const res = await business.travelInfoReport(this.recordId)
					if(!res) return
					this.infoReport = res.data.data.data
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			},
			// 获取出差流程
			async getTravelInfoFlow () {
				try{
					const res = await business.travelInfoFlow(this.recordId)
					if(!res) return
					this.infoFlow = res.data.data
				}catch(e){
					console.log(e);
					throw e
					//TODO handle the exception
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.showContent {
		height: 0;
		overflow: hidden;
		transition: all 4s;
	}
</style>

<template>
	<view class="pt30 pr30 pl30 pb30">
		<view class="title hlc" v-if="flows.length > 0">
			<view class="image w55 h55 mr30"><image src="/static/img/serive/quit/liucheng.png" mode=""></image></view>
			<text class="f32pc bold">审批流程</text>
		</view>
		<view class="uni-timeline" style="padding: 30upx 20upx;background-color: #fff;">
			<block v-for="(item, index) in flows" :key="index">
				<view class="uni-timeline-item uni-timeline-first-item">
					<view class="uni-timeline-item-divider">
						<view class="before"></view>
						<image class="w40 h40 mr20"  src="/static/img/serive/next2.png" mode=""></image>
						<view class="after"></view>
					</view>
					<view class="uni-timeline-item-content">
						<view class="f32pc2">
							<text v-if="item.nodeName" class="mr10">{{ item.nodeName }}</text>
							<text v-if="item.createUser">{{ item.createUser }}</text>
							<text v-if="item.staffName">{{ item.staffName }}</text>
							<text v-if="item.processDesc && item.processDesc !== ''" class="f32pc2">({{ item.processDesc }})</text>
						</view>
						<view class="f32pc2" v-if="item.remark">
							<uParse :content="item.remark" />
						</view>
						<view class="datetime">
							{{ item.processDate }}
						</view>
					</view>
				</view>
			</block>
		</view>
	</view>
</template>

<script>
import uParse from '@/components/uParse/src/wxParse.vue'
// 审批流程页面
export default {
	name: 'yt-approval-process',
	components: {
		uParse
	},
	props: {
		flows: {
			type: Array,
			default: () => {
				return [];
			}
		}
	},
	computed: {
		list() {
			let that = this
			for (let i = 0; i < that.flows.length; i++) {
				let objRegExp = /\<span\s?\S+\<span\>/g
				let reg = objRegExp.exec(that.flows[i].remark)
				if (reg) {
					that.flows[i].remark = reg[0]
				}
			}
			return this.flows
		}
	},
};
</script>

<style lang="scss">
.uni-timeline {
	margin: 0;
	padding: 0;
}
.uni-timeline-item .uni-timeline-item-divider {
	width: 40upx;
	height: 40upx;
}

.uni-timeline-item:first-child .uni-timeline-item-divider .before {
	height: 0;
}

.uni-timeline-item-divider .before, .uni-timeline-item-divider::after {
	left: 20upx;
	position: absolute;
	width: 1upx;
	content: '';
}

.uni-timeline-item:last-child .uni-timeline-item-divider::after {
	height: 0;
}

.uni-timeline-item-divider .after {
	height: 80upx;
}
.uni-timeline-item-divider .before {
	position: absolute;
    width: 1upx;
    content: '';
	height: 20upx;
	top: -20upx;
	background-color: $main-color;
}

</style>

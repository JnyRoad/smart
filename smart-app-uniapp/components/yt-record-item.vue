<template>
    <view>
        <view @click="onClick(item.recordId, item.patchDate)" class="hlc rel bcf mb20 pl30 pr30 pt20 pb20" v-for="(item,index) in listData" :key="index">
            <view class="f1 vlc">
                <view class="hbc mw">
                    <text class="f32pc bold">{{item.recordTitle}}</text>
                    <text class="f26pc6 ">{{item.recordTime}}</text>
                </view>
				<view v-if="type === '4'" class="hbc mw">
					<text class="f32pc bold">出差地点</text>
					<text class="f26pc4">{{item.travelCity}}</text>
				</view>
                <view  class="hbc mw" >
                    <text v-if="type !== '2' && type !== '5' && type !== '3'" class="f32pc bold">开始时间</text>
                    <text v-if="type === '3'" class="f32pc bold">加班日期</text>
                    <text v-if="type === '2'" class="f32pc bold">调休类型</text>
					<text v-if="type === '5'" class="f32pc bold">原因</text>
                    <text v-if="type === '1'" class="f28pc4">{{item.startDate}}</text>
					<text v-if="type === '2'" class="f28pc4">{{item.restTypeDesc}}</text>
					<text v-if="type === '3'" class="f28pc4">{{item.extraworkDate}}</text>
					<text v-if="type === '4'" class="f28pc4">{{item.startDate}}</text>
					<text v-if="type === '5'" class="f30pc4">{{item.patchReasonDesc}}</text>
                </view>
                <view class="hbc mw" >
                    <text v-if="type !== '2' && type !== '5' && type !== '3'" class="f32pc bold">结束时间</text>
					 <text v-if="type === '2'" class="f32pc bold">需调休日期</text>
					 <text v-if="type === '3'" class="f32pc bold">加班类型</text>
					 <text v-if="type === '5'" class="f32pc bold">补卡时间</text>
                    <text v-if="type === '1'" class="f28pc4">{{item.endDate}}</text>
                    <text v-if="type === '2'" class="f28pc4">{{item.restDate}}</text>
                    <text v-if="type === '3'" class="f28pc4">{{item.extraworkTypeName}}</text>
                    <text v-if="type === '4'" class="f28pc4">{{item.endDate}}</text>
                    <text v-if="type === '5'" class="f28pc4">{{item.patchDate}}</text>
                </view>
                <view class="hbc mw">
                    <text class="main-color f32">{{item.recordDesc}}</text>
                    <view class="f28pc4">
						<view v-if="type === '1'" class="hcc">
							<text class="f28pc4 mr20">时长:</text><text class="main-color f32">{{item.vacateCount}}{{item.unit?item.unit:''}}</text>
						</view>
						<view v-if="type === '3'" class="hcc">
							<text class="f28pc4 mr20">时长:</text><text class="main-color f32">{{item.extraworkCount}}小时</text>
						</view>
						<!-- <view v-if="type === '5'" class="hcc">
							<text class="f28pc4 mr20">缺卡:</text><text class="main-color f32">{{item.missPatchCount}}次</text>
						</view> -->
					</view>
                </view>
            </view>
        </view>
    </view>
</template>
<script>
	// 请假 加班 调休 出差 补卡 的记录模块组件
	import {dateUtils} from '@/common/js/util.js'
	export default {
		data(){
			return {
                
			}
        },
        props: {
			list: {
				type: Array,
				require: true
            },
			// 这个值的定义前往config->dictionares.js
            type:{
                type:String,
                default:''
            }
        },
		computed: {
			listData() {
				this.list.forEach(el => {
					el.recordTime = dateUtils.format(el.recordDate)
				})
				return this.list
			}
		},
		methods: {
			onClick(recordId, patchDate) {
				if ( this.type == 5) {
					this.$emit('click',{recordId, patchDate})
					return
				}
				this.$emit('click',recordId)
			}
		},
	}	
</script>
<style lang="scss">
.wm93 {
    width: 93upx;
    height: 93upx;
    position: relative;
    top: -20upx;

}
</style>
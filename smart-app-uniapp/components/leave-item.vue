<template>
    <view v-if="listData.length>0">
        <view @click="detail(item.recordTitle,item.processId,item.approveStatus)" 
        class="hlc rel bcf mb20 pl30 pr30 pt20 pb20" 
        v-for="(item,index) in listData" :key="index">
            <view class="f1 vlc">
                <view class="hbc mw">
                    <view class="f32pc bold">{{item.recordTitle}}</view>
                    <view class="f26pc6 ">{{item.timeDesc}}</view>
                </view>
                <view class="hbc mw" v-if="status === 'leave'">
                    <view class="f32pc bold">申请离职日期:</view>
                    <view class="f26pc4">{{item.dismissionDate}}</view>
                </view>
                <view class="hbc mw">
                    <view :class="item.recordDesc === '已申请' ? 'f32pc7':'f32pc8'">{{item.recordDesc}}</view>
                 </view>
            </view>
        </view>
    </view>
</template>
<script>
	// 离职记录模块
	import {dateUtils} from '@/common/js/util.js'
	export default {
		data(){
			return {
                
			}
        },
        props: {
			// 离职数据
			list: {
				type: Array,
				require: true
            },
			// 正常 or 异常
            type:{
                type:String,
                default:''
            },
            status:{
                type:String,
                default:''
            }
        },
		computed: {
			listData() {
				return this.list
			}
		},
        methods: {
            detail(recordTitle,processId,approveStatus){
				this.$emit('click', {
					recordTitle,
					processId,
					approveStatus,
					type: this.type
				})
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
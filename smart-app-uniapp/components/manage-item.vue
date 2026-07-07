<template>
    <view class="pt20 vcc mw">
        <view @click="onClick(item.applicationId,item.status)" class="hlc card pl30 mb30 rel" v-for="(item,index) in planList" :key="index">
            <image class="wh228" :src="item.applicantPhoto" alt=""></image>
            <view class="f1 pr40 pl20 ">
                <view class="">
                    <view class="f28pc4 hlc">职位:<span class="ml26 f30pc1">{{item.jobName}}</span></view>
                    <view class="pl80 f30pc1">{{item.applicantName}}</view>
                    <view class="pl80 f30pc1">{{item.applicantGender}} | {{item.applicantEducation}} | {{item.applicantAge}}岁</view>
                </view>
                <view class="hbc mt10">
                    <view v-if="applyStatus" class="pc6 f1">{{applyStatus}}</view>
                    <view v-if="!applyStatus" class="pc6 f1">{{item.applyStateDesc}}</view>
                </view>
            </view>
			<view v-if="checked === true || checked === 'true'" @click="onClick(item.applicationId)" class="">
				<image class="wh60 card-check" :src="item.isSelect?'/static/img/serive/recruit/Invitation/success.png':'/static/img/serive/recruit/check.png'" alt=""></image>
			</view>
        </view>
    </view>
</template>
<script>
	// 招聘模块组件 应用于备选与简历塞选操作的页面
	export default {
		
		data(){
			return {
				listLength: 0,
				idArr: [],
				planList: []
			}
        },
        props: {
			// 简历数据
			list: {
				type: Array
            },
			// 选中状态
            checked:{
                type:[Boolean,String],
                default: false
            },
			// 扩展参数 未使用
			noclick: {
				type:[Boolean,String],
				default: false
			},
			// 当前招聘数据的状态
			applyStatus: {
				type: String,
				default: ''
			},
			// 是否选中所有 未使用 状态不好控制
			isSelectAll: {
				type: Boolean,
				default: false
			}
        },
		created() {
			this.listLength = this.list.length
			this.planList = this.list
		},
		watch: {
			isSelectAll(value) {
				console.log(value);
			}
		},
        methods: {
			onClick(applicationId,status){
				this.$emit('click',{
					applicationId,
					status
				})
			}
        },
	}	
</script>
<style lang="scss" scope>
 	
    .wh228{
        width: 228upx;
        height: 228upx;
    }
    .card {
        width:690upx;
        height:291upx;
        background:rgba(255,255,255,1);
        box-shadow:0px 5px 18px 0px rgba(185,185,185,0.5);
        border-radius:20upx;
    }
    .wh60{
        width: 60upx;
        height: 60upx;
	}
	.card-check {
		position: absolute;
		top: -20upx;
		left: -10upx;
		z-index: 2;
	}
</style>
<template>
	<view class="rpickerBox">
		<view :class="{'pickerMask':showPicker}" @click="maskClick" @touchmove.stop="touchmoveHandler">
			<view class="r-dtpicker" :class="{'r-dtpicker-show':showPicker}">
				<view class="rdtBtn" @touchmove.stop="touchmoveHandler" @tap.stop="returnHandle">
					<view @click="pickerCancel">取消</view>
					<view :style="{color:themeColor}" @click="pickerConfirm">确定</view>
				</view>
				<!-- <view class="rangeBox" catchtouchmove="true" @tap.stop="returnHandle">
					<input type="text" disabled placeholder="开始时间" :value="startDate" :style="{color:themeColor,'border-color':themeColor,opacity:dateType=='startDate'?1:.5}" @tap="changeDateType('startDate')">至<input type="text" disabled placeholder="结束时间" :style="{color:themeColor,'border-color':themeColor,opacity:dateType=='endDate'?1:.5}" :value="endDate" @tap="changeDateType('endDate')">
				</view> -->
				<picker-view indicator-style="height: 40px;" class="mpvue-picker-view"  @change="pickerChangeMul">
					<block>
						<picker-view-column>
							<view class="picker-item" v-for="(item,index) in yearArr" :key="index">{{item}}</view>
						</picker-view-column>
						<picker-view-column>
							<view class="picker-item" v-for="(item,index) in monthArr" :key="index">{{item}}</view>
						</picker-view-column>
					</block>
				</picker-view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'range-dtpicker',
		props: {
			/**
			 * picker允许选中的最小值
			 */
			start: {
			  type: String,
			  default: '1900-01'
			},
			/**
			 * picker允许选中的最大值
			 */
			end: {
			  type: String,
			  default: '2500-12'
			},
			/**
			 * picker默认展示的值
			 */
			value: {
                type: Array,
                default(){
					return [0,0]
				}
            },
			//是否显示
			show: {
				type: Boolean,
				default: false
			},
			/**
			主题色
			*/
			themeColor:{
			  type: String,
			  default: '#4C83D6'
			}
		},
		created() {
			if(!this.start||!this.end){
				console.error("时间不能为空");
			}else if(this.start>this.end){
				console.error("结束时间必须大等于开始时间");
			}
			if(this.value[0]){
				this.startDate=this.value[0];
				if(this.value[1]){
					this.endDate=this.value[1];
					this.dateType="endDate";
					this.pickerValue=this.getIndex(this.value[1]);
				}else{
					this.dateType="startDate";
					this.pickerValue=this.getIndex(this.value[0]);
				}
			}else{
				this.startDate=this.start;
			}
		},
		data() {
			return {
				showPicker: false,
				pickerValue:[0,0],
				dateType:"startDate",
				startDate:'',
				endDate:""
			};
		},
		watch: {
			show(isShow) {
				this.showPicker = isShow;
			}
		},
		computed: {
			yearArr(){
				var arr = [],start = parseInt(this.start.slice(0,4)),end=parseInt(this.end.slice(0,4));
				if (end-start == 0) {
					arr.push(start)
				}
				else {
					for(var i=0;i<=end-start;i++){
						arr.push(start+i)
					}
				}
				return arr;
			},
			monthArr(){
				let arr = [];
				const start = parseInt(this.start.slice(0,4)),end=parseInt(this.end.slice(0,4));
				const startMonth = parseInt(this.start.slice(5));
				const endMonth = parseInt(this.end.slice(5));
				if (end - start == 0){
					for(let i= startMonth; i<= endMonth; i++) {
						let v = i;
						if(v<10)v="0"+v;
						arr.push(v)
					}
				} else {
					for(let i=1;i<=12;i++){
						let v = i;
						if(v<10)v="0"+v;
						arr.push(v)
					}
				}
				return arr;
			}
		},
		methods:{
			returnHandle(){},
			maskClick(){
				this.$emit("showchange",false);
			},
			pickerConfirm(){
				// var data = {
				// 	detail:{
				// 		value:{
				// 			startDate:this.startDate,
				// 			endDate:this.endDate,
				// 			}
				// 		}
				// 	}
				// if(this.endDate<this.startDate){
				// 	uni.showToast({
				// 		title:"结束时间不得小于开始时间",
				// 		icon:"none",
				// 		mask:true
				// 	})
				// 	return;
				// }
				this.$emit("change",this.startDate);
				this.$emit("showchange",false);
			},
			pickerCancel(){
				
				this.$emit("cancel");
				this.$emit("showchange",false);
			},
			changeDateType(dateType){
				var that = this;
				this.dateType=dateType;
				if(this[dateType]){
					this.pickerValue=this.getIndex(this[dateType])
				}else{
					// if(dateType=="startDate"){
						var dateTxt=this.yearArr[0]+'-'+this.monthArr[0];
						this[dateType]=dateTxt;
						this.pickerValue=[0,0];
					// }else{
					// 	this.getNewArr(this["startDate"],(txt)=>{
					// 		that[that.dateType]=txt;
					// 		that.pickerValue=[0,0];
					// 	})
					// }
				}
			},
			pickerChangeMul(e){
				console.log(e);
				var val = e.detail.value,dateTxt="";
				dateTxt=this.yearArr[val[0]]+'-'+this.monthArr[val[1]];
				this[this.dateType]=dateTxt;
				this.pickerValue=this.getIndex(dateTxt)
			},
			getIndex(value){
				var year = value.slice(0,4),month=value.slice(5,7),y=0,m=0;
				for(let el of this.yearArr){
					console.log(el);
					if(year==el){
						y=el;break;
					}
				}
				for(var i in this.monthArr){
					if(month==this.monthArr[i]){
						m=i;break;
					}
				}
				return [y,m]
			},
			getNewArr(value){
				var year = value.slice(0,4),month=value.slice(5,7),y=0,m=0;
				for(var i in this.yearArr){
					if(year==this.yearArr[i]){
						y=i;break;
					}
				}
				for(var i in this.monthArr){
					if(month==this.monthArr[i]){
						m=i;break;
					}
				}
				return [y,m]
			},
			// 防止页面滚动穿透
			touchmoveHandler () { }
		}
	}
</script>

<style>
    .pickerMask {
        position: fixed;
        z-index: 998;
        top: 0;
        right: 0;
        left: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
    }

    .r-dtpicker {
        position: fixed;
        bottom: 0;
        left: 0;
        width: 100%;
        transition: all 0.3s ease;
        transform: translateY(100%);
        z-index: 998;
    }
	
    .r-dtpicker-show {
        transform: translateY(0);
    }
    
    .rdtBtn {
        display: flex;
        padding: 9px 15px;
        background-color: #fff;
        position: relative;
        text-align: center;
        font-size: 17px;
    }
    
    .rdtBtn:after {
        content: ' ';
        position: absolute;
        left: 0;
        bottom: 0;
        right: 0;
        height: 1px;
        border-bottom: 1px solid #e5e5e5;
        color: #e5e5e5;
        transform-origin: 0 100%;
        transform: scaleY(0.5);
    }
    
    .rdtBtn view{
        display: block;
        flex: 1;
        color: #1aad19;
    }
    
    .rdtBtn view:first-child {
        text-align: left;
        color: #888;
    }
    
    .rdtBtn view:last-child {
        text-align: right;
    }
    
    .picker-item {
        text-align: center;
        line-height: 40px;
        font-size: 16px;
    }
    
    .mpvue-picker-view {
        position: relative;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 238px;
        background-color: rgba(255, 255, 255, 1);
    }
	.rangeBox{
		background: #fff;
		display: flex;
		justify-content: center;
		padding: 15px 0;
		font-size: 16px;
		align-items: center;
	}
	.rangeBox input{
		width: 180upx;
		margin: 0 10px;
		text-align: center;
		align-items: center;
		display: flex;
		min-height: auto;
		border-bottom: 1px solid #000;
	}
</style>

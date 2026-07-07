<template>
    <view class=" hlc">
		<picker @change="getPickerData" :value="index" v-if="title != '投递时间'"
			 :range="array" >
			<view class="h50 hrc f28pc2"> {{ title }}</view>
		</picker>
		<picker v-if="title === '投递时间'"  mode="date"   @change="getPickerData" >
			<view class="h50 hrc f28pc2"> {{ title }}</view>
		</picker>
		<image class=" ml10 w22 h16" src="/static/img/serive/recruit/down1.png"></image>
	</view>
</template>
<script>
	// 已废弃
	function getDate(type) {
		const date = new Date();
	
		let year = date.getFullYear();
		let month = date.getMonth() + 1;
		let day = date.getDate();
	
		if (type === 'start') {
			year = year - 60;
		} else if (type === 'end') {
			year = year + 2;
		}
		month = month > 9 ? month : '0' + month;;
		day = day > 9 ? day : '0' + day;
	
		return `${year}-${month}-${day}`;
	}


	export default {
        props: {
            title:{
                type: String,
                default:''
			},
			array: {
				type: Array,
				default: ()=>[]
			},
			idType: {
				type: String,
				default:''
			},
        },
		data(){
			return {
				index: -1,
				date: getDate({
					format: true
				}),
				startDate:getDate('start'),
				endDate:getDate('end'),
			}
		},
		methods: {
			getPickerData: function(e) {
				console.log(e,'e',this.idType)
				const idType = this.idType
				this.$emit('changePicker', e.target.value)
				if (this.index === -1) {
					this.index = 0
					if(idType == 'timeType'){
						this.$emit('changePicker', {
							value: e.target.value,
						    idType: this.idType
						})
						return
					}
					this.$emit('changePicker', {
						value: this.array[this.index],
						idType: this.idType
					})
				} else {
					this.index = e.target.value
					if(idType == 'timeType'){
						this.$emit('changePicker', {
							value: e.target.value,
						    idType: this.idType
						})
						return
					}
					this.$emit('changePicker', {
						value: this.array[this.index],
						idType: this.idType
					})
					
				}
			},
		}
	}	
</script>
<style lang="scss">
	.w22{
		width: 22upx;
	}
	.h16{
		height: 16upx;
	}
</style>
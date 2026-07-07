<template>
	<picker @change="PickerChange" :value="index" :range="list">
		<view v-if="showValues" class="h50 hrc" :style="{color:textColor}">{{value}}</view>
		<view v-if="showValue" class="h50 hrc" :style="{color:textColor}">{{list[index]|| value}}</view>
		<slot v-else></slot>
	</picker>
</template>

<script>
	// 所有picker类处理
	export default {
		data () {
			return {
				index: -1
			}
		},
		props: {
			arrData: {
				type: Array,
				required: true
			},
			showValues: {
				type : Boolean,
				required: false
			},
			idType: {
				type: String
			},
			value: {
				type: String
			},
			textColor: {
				type: String,
				default: '#666666'
			},
			showValue: {
				type: Boolean,
				default: true
			}
		},
		computed: {
			list () {
				return this.arrData
			}
		},
		methods: {
			PickerChange (e) {
				const inx = e.target.value
				if (inx == -1) {
					this.index = 0
				} else {
					this.index = inx
				}
				this.$emit('changePicker', {
					value: this.arrData[this.index],
					idType: this.idType
				})
				
			}
		},
	}
</script>

<style>
</style>

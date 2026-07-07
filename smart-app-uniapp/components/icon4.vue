<template>
    <view class="fw hlc w750 h400"  style="flex-direction: row;">
        <view  @click="onClick(item.moduleId)" class="w187 vcc mt38" v-for="(item,index) in list" :key="index">
            <image class="icon81" :src="item.moduleIcon"></image>
            <view class="f26pc mt20">{{item.moduleName}}</view>
        </view>
    </view>
</template>
<script>
	// 模块类组件
	export default {
		props: {
			list: {
				type: Array,
				require: true
			}
		},
		methods: {
			onClick(id) {
				this.$emit('click', id)
			}
		},
	}	
</script>
<style lang="scss">
.w187{
    width: 187upx;
}
.icon81 {
    width: 100upx;
    height: 100upx;
}
</style>
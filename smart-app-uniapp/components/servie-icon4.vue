<template>
    <view  class="pb40 border-bottom-f2 rel">
        <view class="f32pc pl52 pt40 bold">{{title}}</view>
        <view class="fw hlc">
            <view @longpress="startMove(item.id, $event)" @click="onClick(item.moduleUrl)" class="w187 vcc mt38 rel" v-for="(item,index) in iconList" :key="index">
                <image class="icon81" :src="item.moduleIcon"></image>
                <view class="f26pc mt20">{{item.moduleName}}</view>
				<view v-show="hasMove" class="w50 h50 abs close">
					<image class="mw mh" src="/static/img/serive/X.png"></image>
				</view>
            </view>
        </view>
		<view v-if="curMove" @touchend="moveEnd" @longpress="startMove(currentIconObj.id, $event)" @touchmove.stop="moving(currentIconObj.id, $event)" class="w180 h140 vcc fixed move rel" :style="{left: left+'px', top: top+'px'}">
			<image class="icon81" :src="currentIconObj.moduleIcon"></image>
			<view class="f26pc mt20">{{currentIconObj.moduleName}}</view>
			<view @click.stop="closeCurrent" v-show="curClose" class="w50 h50 abs close">
				<image class="mw mh" src="/static/img/serive/X.png"></image>
			</view>
		</view>
    </view>
</template>
<script>
	// 按时未使用 模块的长按移动换位
	export default {
        props: {
            title:{
                type: String,
                default:''
            }, 
            list:{
                type: Array,
            },
        },
		data(){
			return {
				hasMove: false,
				curClose: false,
				curMove: false,
				currentIconObj: {},
				left: 0,
				top: 0,
				startPoint: {},
				iconList: []
			}
		},
		created() {
			this.iconList = this.list
		},
		methods: {
			onClick(url) {
				if (this.hasMove) return
				if (url == '') {
					console.log(url);
					this.$ytHint.toast({
						title: '暂未开放,尽情期待!'
					})
				} else {
					uni.navigateTo({
						url: url
					})
				}
			},
			startMove(id,e) {
				e.preventDefault()
				uni.vibrateShort({
					success:() => {
						console.log('整栋了呀');
					}
				})
				// this.startPoint = e.mp.touches[0];
				this.startPoint = e.currentTarget;
				this.left = this.startPoint.offsetLeft
				this.top = this.startPoint.offsetTop
				this.hasMove = this.curClose = this.curMove = true
				this.iconList.forEach((el, inx) => {
					if (el.id === id) {
						this.iconList.splice(inx, 1)
						this.currentIconObj = el
					}
				})
				
			},
			moving(id,e) {
				
				var endPoint = e.touches[0]
				this.left = endPoint.pageX -45
				this.top = endPoint.pageY -35
				
			},
			moveEnd(e) {
				this.currentIconObj.id = this.iconList.length + 1
				this.iconList.push(this.currentIconObj)
				console.log(this.iconList);
				this.curClose = false
				this.curMove = false
				this.hasMove = false
			},
			closeCurrent() {
				this.curClose = false
				this.curMove = false
			}
		},
	}	
</script>
<style lang="scss">
	.w187{
    width: 187upx;
}
.icon81 {
    width: 81upx;
    height: 81upx;
}
.move {
	z-index: 999;
}
.close {
	top: -20upx;
	right: 30upx;
}
</style>
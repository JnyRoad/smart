<template>
    <view class="fw hlc w750"  style="flex-direction: row;">
        <view class="w187 vcc mt38" v-for="(item,index) in parkList" :key="index">
            <image class="icon81" :src="'/static/img/index/park'+(index+1)+'.png'"></image>
            <view class="f26pc mt20">{{item.title}}</view>
        </view>
    </view>
</template>
<script>
	// 废弃未使用
	export default {
		data(){
			return {
				 parkList:[
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'园区广告',
                        link1:''
                    },
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'园区新闻',
                        link1:''
                    },
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'园区概况',
                        link1:''
                    },
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'园区活动',
                        link1:''
                    },
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'裕同简介',
                        link1:''
                    },
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'企业文化',
                        link1:''
                    },
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'园区导航',
                        link1:''
                    },
                    {
                        imgurl:'static/img/index/park1.jpg',
                        title:'园区VR',
                        link1:''
                    }
                ]
			}
		}
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
</style>
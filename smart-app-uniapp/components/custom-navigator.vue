<template>
	<view class="title-contents">  
        <view class="top-view status" :style="{background:statusColor}"></view>  
        <view class="_top titles pl30 pr30 hcc" :style="{background:statusColor}">  
            <view class="titleLeftButton" @click="backButton" v-if="showLeftButton">
				<image src="/static/img/serive/arrow-left.png" mode=""></image>
			</view>
            <view class="titleText hcc f32" >{{titleText}}</view>  
            <view class="titleRightButton hbc" v-if="showRightButton">
				<slot></slot>
			</view>  
        </view>  
    </view>
</template>

<script>
	export default {  
        props:{  
            titleText:{  
                type:String,  
                default:""  
            },  
            statusColor:{  
                type:String,  
                default:"#508BFF"
            },  
            showLeftButton:{  
                type:Boolean,  
                default:true  
            },  
            showRightButton:{  
                type:Boolean,  
                default:false  
            }  
        },  
        methods:{  
            backButton(){  
                uni.navigateBack({
					delta: 1
				})  
            }
        }  
    }  
</script>

<style lang="scss">


</style>

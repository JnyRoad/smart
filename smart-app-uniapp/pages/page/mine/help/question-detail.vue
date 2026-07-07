<template>
	<view class="yt-page bgf">
		<view class="yt-content vcc">
			<view class="title hlc f32pc bold mw h80  pt30 pr30 pb30 pl30">
				{{questionInfo.questionTitle}}
			</view>
			<view class="mw h10 border-top-D border-bottom-D"></view>
			<view class="mw pt20 pr30 pb30 pl30">
				<uParse :content="questionInfo.answerContent"></uParse>
			</view>
		</view>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import uParse from '@/components/uParse/src/wxParse.vue'
	import {helpCenter} from '@/api/api-mine.js'
	export default {
		components: {
			uParse
		},
		data () {
			return {
				questionInfo: {}
			}
		},
		onLoad(e) {
			const {questionId} = e
			this.getQuestionDetail(questionId)
		},
		methods: {
			async getQuestionDetail (questionId) {
				const res = await helpCenter.requestAnswer(questionId)
				this.questionInfo = res.data.data
			}
		},
	}
</script>

<style>
</style>

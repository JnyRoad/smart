<template>
	<view class="yt-page bgf">
		<view v-if="info" class="yt-content pt30 pr30 pb50 pl30">
			<wx-parse :content="info"></wx-parse>
		</view>
	</view>
</template>

<script>
	import wxParse from '@/components/uParse/src/wxParse.vue'
	import { employee } from '@/api/api-mine.js'
	import {storage} from '@/tools/storage.js'
	export default {
		components: {
			wxParse
		},
		data () {
			return {
				info: ''
			}
		},
		onLoad() {
			const parkId = storage.getSync(storage.PARK_ID)
			this.getAgrement(parkId)
		},
		methods: {
			async getAgrement (parkId) {
				try{
					const res = await employee.outRoomAgrement(parkId)
					this.info = res.data.data.agreeContent
				}catch(e){
					console.log(e);
					//TODO handle the exception
				}
			}
		},
	}
</script>

<style></style>

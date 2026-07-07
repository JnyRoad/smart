<template>
    <view class="bgec w750">
        <yt-record-item @click="toDetail" v-if="!isNull" :list="list" type="4" />
		<yt-null v-if="isNull" notice="暂时没有出差办理进度"> </yt-null>
		<yt-loading></yt-loading>
    </view>
</template>

<script>
    import ytrecorditem from '@/components/yt-record-item.vue'
	import travel from '@/api/api-travel.js'
	import ytNull from '@/components/yt-null/yt-null.vue'
    export default {
        components: {
            'yt-record-item': ytrecorditem,
			'yt-null': ytNull
        },
        data() {
            return {
                list:[],
                type:'' ,//是否正常离职
				page: {
					current: 1,
					size: 10
				},
				hasMore: true,
				isNull: false,
				recordId: 0
            }
        },
		onLoad (e) {
			this.recordId = e.recordId
			this.getBusinessRecord()
		},
		onPullDownRefresh() {
			this.list = []
			this.isNull = false
			this.hasMore = true
			this.page.current = 1
			this.getBusinessRecord()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据'
				})
				return
			}
			this.page.current++
			this.getBusinessRecord()
		},
        methods: {
			// 查看出差记录
			async getBusinessRecord () {
				try{
					const res = await travel.travelRecordList(this.page)
					uni.stopPullDownRefresh()
					if (this.page.current === 1) {
						if (!res.data.data.records.length ) {
							this.isNull = true
							return
						}
						this.list = res.data.data.records;
					} else {
						if (!res.data.data.records.length) {
							this.hasMore = false
						} else {
							this.list.push(...res.data.data.records)
						}
					}
				}catch(e){
					//TODO handle the exception
				}
			},
			
			toDetail (e) {
				uni.navigateTo({
					url: `./detail?recordId=${e}`
				})
			}
        }
    }
</script>

<style>
.gray{
    background: #ECECEC;
}

</style>

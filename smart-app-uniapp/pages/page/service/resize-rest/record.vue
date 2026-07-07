<template>
    <view class="w750 bgec">
        <yt-record-item @click="toDetail" :list="list" type="2" />
		<yt-null v-if="isNull" notice="暂时没有调休办理进度"> </yt-null>
		<yt-loading></yt-loading>
    </view>
</template>

<script>
    import ytrecorditem from '@/components/yt-record-item.vue'
	import rest from '@/api/api-rest.js'
		import ytNull from '@/components/yt-null/yt-null.vue'
    export default {
        components: {
            'yt-record-item': ytrecorditem,
			'yt-null': ytNull
        },
        data() {
            return {
                list:[],
                type:'',
				page: {
					current: 1,
					size: 10
				},
				hasMore: true,
				isNull: false
            }
        },
		onLoad(e) {
			this.getRestRecord()
		},
		onPullDownRefresh() {
			this.hasMore = true
			this.isNull = false
			this.page.current = 1
			this.hasMore = true
			this.getRestRecord()
		},
		onReachBottom() {
			if (!this.hasMore) {
				this.$ytHint.toast({
					title: '没有更多数据了'
				})
				return
			}
			this.page.current++
			this.getRestRecord()
		},
        methods: {
			async getRestRecord () {
				const res = await rest.restRecordList(this.page)
				uni.stopPullDownRefresh()
				if (!res) return
				if (this.page.current === 1) {
					if (!res.data.data.records.length) {
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

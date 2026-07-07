<template>
	<view class="yt-page bgf">
		<yt-list :ytClass="['pl30 pr30']">
			<yt-list-item @click="update" showImage=false hasMust showArrow=false title="访客照片" :hasRightIcon="hasRightIcon" :hasRightImage="hasRightImage" :rightImage="member.memberPhoto" rightIcon="/static/img/mine/camera.png"></yt-list-item>
			<yt-list-item @click="getInput" showImage=false hasMust showArrow=false title="访客姓名">
				<view class="f30pc2">
					{{member.memberName || '请输入'}}
				</view>
			</yt-list-item>
		</yt-list>
		<view class="mw h20 bgec"></view>
		<view class="hcc mw mt80 mb60"><yt-button @click="saveAccompanyPeople" btnText="保存" btnWidth="180" btnHeight="54"></yt-button></view>
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="500" :maxWidth="1000" :ql="0.8" type="base64"></cpimg>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import ytminemodel from '@/components/yt-mine-model.vue'
	import cpimg from '@/components/cpimg.vue'
	import {ACCOMPANYPEOPLE, storage} from '@/tools/storage.js'
	import hideModel from '@/mixins/hideModel.vue'
	export default {
		mixins: [hideModel],
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-button': ytbutton,
			'yt-mine-model': ytminemodel,
			'cpimg': cpimg
		},
		data () {
			return {
				modelObj: {
					title: '',
					input: '',
					idType: ''
				},
				hasRightIcon: true,
				hasRightImage: false,
				member: {
					memberPhoto: '',
					memberName: '请输入'
				}
			}
		},
		computed: {
			showModel() {
				return this.$store.state.modelStatus
			}
		},
		methods: {
			// 显示模态框 并给模态框初始值
			getInput(e) {
				this.$store.commit('ytModelStatus',true) // 显示弹出模态框
				this.modelObj = {
					title: e.title,
					input: e.tipContent,
					idType: e.type
				}
			},
			update () {
				this.$refs.cpimg._changImg()
			},
			cpimgOk(file) {
				if (file) {
					uni.hideLoading()
					this.$ytHint.toast({
						title: '处理成功'
					})
					this.hasRightIcon = false
					this.hasRightImage = true
					this.member.memberPhoto = file
				}
				
			},
			cpimgErr(e) {
				if (e) {
					uni.hideLoading()
					this.$ytHint.toast({
						title: '处理失败,请重试'
					})
					this.hasRightIcon = true
					this.hasRightImage = false
					throw e
				}
			},
			// 获取模态框的返回的值
			getData (e) {
				this.member.memberName = e.value
			},
			saveAccompanyPeople () {
				const {memberPhoto, memberName} = this.member
				if (memberPhoto && memberName !== '请输入') {
					let accompanyPeople = [] // 定义随行人员数组
					// 从本地获取随行人数据
					accompanyPeople = storage.getSync(ACCOMPANYPEOPLE) || []
					// 将随行人员push 进本地存储
					accompanyPeople.push(this.member)
					// 异步存储 当前的所有的随行人员的信息
					storage.setSync(ACCOMPANYPEOPLE, accompanyPeople)
					uni.redirectTo({
						url: './accompany-people'
					})
				} else {
					this.$ytSystem.vibrate({
						title: '请填写完整'
					})
				}
			}
		}
 	}
</script>

<style>
</style>

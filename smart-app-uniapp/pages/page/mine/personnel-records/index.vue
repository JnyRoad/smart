<template>
	<view v-if="fullInfo.employeeName" class="yt-page bge">	
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item showImage=false hideLastBorder=true showArrow=false title="姓名" :tipContent="fullInfo.employeeName"></yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pl30','pr30','mt20']">
			<yt-list-item showImage=false showArrow=false title="工号" :tipContent="fullInfo.employeeId"></yt-list-item>
			<yt-list-item showImage=false hideLastBorder=true showArrow=false title="身份证号" :tipContent="fullInfo.identification"></yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pl30','pr30','mt20']">
			<yt-list-item showImage=false showArrow=false title="BU" :tipContent="fullInfo.buName"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="部门" :tipContent="fullInfo.deptName"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="岗位" :tipContent="fullInfo.jobName"></yt-list-item>
			<yt-list-item showImage=false hideLastBorder=true showArrow=false title="职层" :tipContent="fullInfo.jobLeve"></yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pl30','pr30','mt20']">
			<yt-list-item showImage=false showArrow=false title="手机号码" :tipContent="fullInfo.mobile"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="邮箱" :tipContent="fullInfo.email"></yt-list-item>
			<yt-list-item showImage=false showArrow=false title="紧急联系人关系" >
				<view class="f30pc2">
					{{fullInfo.relation }}
				</view>
			</yt-list-item>
			<yt-list-item idType="name" showImage=false showArrow=false title="紧急联系人姓名">
				<view class="f30pc2">
					{{fullInfo.emergencyName }}
				</view>
			</yt-list-item>
			<yt-list-item idType="phone" hideLastBorder=true showImage=false showArrow=false title="紧急联系人电话" >
				<view class="f30pc2">
					{{fullInfo.emergencyPhone}}
				</view>
			</yt-list-item>
		</yt-list>
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import ytminemodel from '@/components/yt-mine-model.vue'
	import ytpicker from '@/components/yt-picker.vue'
	import hideModel from '@/mixins/hideModel.vue'
	import {employee} from '@/api/api-mine.js' // 请求接口的参数
	export default {
		mixins: [hideModel],
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-button': ytbutton,
			'yt-mine-model': ytminemodel,
			'yt-picker': ytpicker
		},
		data () {
			return {
				modelObj: {
					title: '',
					input: '',
					idType: ''
				},
				// linkRelation: ['兄妹','姐弟','父亲','母亲','配偶','兄弟','姐妹','子女','其他'],
				fullInfo: {}
			}
		},
		// // 导航栏的自定义按钮事件
		// onNavigationBarButtonTap(e) {
		// 	// 点击完成按钮 保存用户修改信息
		// 	const { relation,emergencyName,emergencyPhone } = this.fullInfo
		// 	employee.relationUpdate({ relation,emergencyName,emergencyPhone }).then(res => {
		// 		if (res.data.data) {
		// 			this.$ytHint.toast({
		// 				title: '修改成功',
		// 				icon: 'success',
		// 				success: () => {
		// 					uni.switchTab({
		// 						url: '/pages/page/tabbar/mine/mine'
		// 					})
		// 				}
		// 			})
		// 		}
		// 	}).catch(err => {
		// 		console.log(err);
		// 		throw err
		// 	})
		// },
		computed: {
			// 控制弹出模态框的显示状态
			showModel() {
				return this.$store.state.modelStatus
			}
		},
		onLoad () {
			this.getEmployeefullInfo()
		},
		methods: {
			// 获取员工的完整信息
			async getEmployeefullInfo () {
				const res = await employee.fullInfo()
				this.fullInfo = res.data.data
			},
			// 弹出输入框 输入并提交数据
			getItem (e) {
				this.$store.commit('ytModelStatus',true) // 显示弹出模态框
				this.modelObj = {
					title: e.title,
					input: e.tipContent,
					idType: e.type
				}
			},
			// 获取选择器里面的值
			getPickerData (e) { // 获取选择器里面返回的值
				this.fullInfo.relation = e.value
			},
			// 获取弹出框用户输入的数据 并保存
			getData(e) {
				if (e.idType === 'name') { // 表示紧急联系人的姓名
					this.fullInfo.emergencyName = e.value
				} else if (e.idType === 'phone') { // 表示紧急联系人的电话
					this.fullInfo.emergencyPhone = e.value
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
.yt-page {
	.yt-btn {
		margin: 200upx auto 0;
	}
}
</style>

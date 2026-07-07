<template>
	<view class="yt-page">
		<yt-list :ytClass="['pl30','pr30']">
			<yt-list-item @click="showPlateModal = !showPlateModal" idType="carNo" hasMust=true  showImage=false showArrow=false  title="车牌号">
				<text class="f28pc2">{{car.no || '请输入'}}</text>
			</yt-list-item>
			<yt-list-item showImage=false hasMust=true  title="车类型">
				<yt-picker v-if="carModel.length>0" @changePicker="getPickerData" idType="carType" value="请选择" :arrData="carModel"></yt-picker>
			</yt-list-item>
			<yt-list-item @click="getItem" hasMust=true  idType="carBrand" showImage=false showArrow=false title="车品牌">
				<text class="f28pc2">{{car.brand || '请输入'}}</text>
			</yt-list-item>
			<yt-list-item showImage=false hideLastBorder=true hasMust=true  title="车辆颜色">
				<yt-picker v-if="colorType.length>0" @changePicker="getPickerData" value="请选择" idType="carColor" :arrData="colorType"></yt-picker>
			</yt-list-item>
		</yt-list>
		<yt-list :ytClass="['pl30','pr30','mt20']">
			<yt-list-item @click="update('A')" hasMust=true  showImage=false showArrow=false title="上传驾驶证" :hasRightIcon="drivingA" :hasRightImage="!drivingA" :rightImage="drivingAImage" rightIcon="/static/img/mine/camera.png"></yt-list-item>
			<yt-list-item @click="update('B')" hasMust=true hideLastBorder=true  showImage=false showArrow=false title="上传行驶证" :hasRightIcon="drivingB" :hasRightImage="!drivingB" :rightImage="drivingBImage" rightIcon="/static/img/mine/camera.png"></yt-list-item>
		</yt-list>
		<view class="hcc mt80">
			<yt-button @click="addCar" :navUrl="navUrl" btnText="下一步" btnWidth="180" btnHeight="54"></yt-button>
		</view>
		<yt-mine-model @sendInput="getData" :idType="modelObj.idType" :modelTitle="modelObj.title" :modelInput="modelObj.input" v-if="showModel"></yt-mine-model>
		<cpimg ref="cpimg" @result="cpimgOk" @err="cpimgErr" :size="500" :maxWidth="1000" :ql="0.8" type="base64"></cpimg>
		<plate-modal :show="showPlateModal" @confirm="getPlateNum" @cancel="cancelPlateModal"></plate-modal>
		<yt-loading></yt-loading>
	</view>
</template>

<script>
	import ytlist from '@/components/yt-list/index.vue'
	import ytlistitem from '@/components/yt-list-item/index.vue'
	import ytbutton from '@/components/yt-button/index.vue'
	import ytpicker from '@/components/yt-picker.vue'
	import ytminemodel from '@/components/yt-mine-model.vue'
	import plateMoadl from '@/components/plate-modal/plate-modal.vue'
	import hideModel from '@/mixins/hideModel.vue'
	import cpimg from '@/components/cpimg.vue'
	import {pathToBase64} from 'image-tools'
	import {vehicle} from '@/api/api-mine.js' // 请求接口的参数
	import {checkCarNum} from '@/common/js/util.js'
	export default {
		mixins: [hideModel],
		components: {
			'yt-list': ytlist,
			'yt-list-item': ytlistitem,
			'yt-button': ytbutton,
			'yt-mine-model': ytminemodel,
			'yt-picker': ytpicker,
			'cpimg': cpimg,
			'plate-modal': plateMoadl
		},
		data () {
			return {
				modelObj: {
					title: '',
					input: '',
					idType: '', 
				},
				hasCar: false,
				car: {
					no: '',
					type: '',
					brand: '',
					color: ''
				},
				colorType: [],
				carModel: [],
				drivingA: true, // 驾驶证
				drivingB: true ,// 行驶证
				drivingAImage: '', // 驾驶证图片
				drivingBImage: '' ,// 行驶证图片
				navUrl: '',
				carColorType: [], // 存储后台返回车辆颜色数据
				carType: [] ,// 存储后台返回车辆类型数据
				carTypeCode: '', // 车辆类型编码
				carColorCode: '' ,// 车辆颜色编码
				upLoadType: '' ,// 上传文件标识
				showPlateModal: false
			}
		},
		computed: {
			showModel() {
				return this.$store.state.modelStatus
			}
		},
		onLoad () {
			this.getCarColor()
			this.getCarType()
		},
		methods: {
			// 获取车辆颜色类型数据
			async getCarColor () {
				const res = await vehicle.colorType()
				this.carColorType = res.data.data
				this.colorType = res.data.data.map(function(val, index) {
					return val.colorName
				})
			},
			// 获取车辆类型列表数据
			async getCarType () {
				const res = await vehicle.carType()
				this.carType = res.data.data
				this.carModel = res.data.data.map(function(val) {
					return val.typeName
				})
			},
			// 获取车牌号
			getPlateNum (e) {
				console.log(e);
				let carNum = ''
				for (var i = 0; i < e.length; i++) {
					carNum += e[i].val
				}
				this.showPlateModal = false
				this.car.no = carNum
			},
			// 隐藏车牌号输入
			cancelPlateModal (e) {
				console.log(e);
				this.showPlateModal = e
			},
			getItem (e) {
				this.$store.commit('ytModelStatus',true) // 显示弹出模态框
				this.modelObj = {
					title: e.title,
					input: e.tipContent,
					idType: e.type
				}
			},
			// 获取选择值
			getPickerData( e ) {
				if (e.idType === 'carType') {
					this.car.type = e.value
					for (let i of this.carType) {
						if (i.typeName === e.value) {
							this.carTypeCode = i.typeCode
						}
					}
				} else if (e.idType === 'carColor') {
					this.car.color = e.value
					for (let i of this.carColorType) {
						if (i.colorName === e.value) {
							this.carColorCode = i.colorCode
						}
					}
				}
			},
			// 获取输入
			getData(e) {
				if (e.idType === 'carNo') {
					let carNum = e.value.toUpperCase()
					if (!checkCarNum(carNum)) {
						this.$ytHint.toast({
							title: '车牌号格式错误'
						})
						return
					}
					this.car.no = carNum
				} else if (e.idType === 'carBrand') {
					this.car.brand = e.value
				}
			},
			update (type) {
				this.upLoadType = type
				this.$refs.cpimg._changImg()
			},
			cpimgOk(file) {
				if (file) {
					uni.hideLoading()
					this.$ytHint.toast({
						title: '处理成功'
					})
					if (this.upLoadType === 'A') {
						this.drivingA = false
						this.drivingAImage = file
					} else if (this.upLoadType === 'B') {
						this.drivingB = false
						this.drivingBImage = file
					}
				}
				
			},
			cpimgErr(e) {
				if (e) {
					uni.hideLoading()
					this.$ytHint.toast({
						title: '处理失败,请重试'
					})
					this.drivingA = true
					this.drivingB = true
					throw e
				}
			},
			addCar () {
				const obj = {
					plateNumber: this.car.no,
					vehicleType: this.carTypeCode,
					vehicleBrand: this.car.brand,
					vehicleColor: this.carColorCode,
					drivingLicence: this.drivingAImage.split(',')[1],
					carDrivingLicence: this.drivingBImage.split(',')[1]
				}
				// 效验请求对象有没有输入完全
				let flag = true // 定义一个状态值
				Object.keys(obj).forEach(key => {
					if (!obj[key]) {
						this.$ytHint.toast({
							title: `‘*’为必填项`
						})
						flag = false
					}
				})
				if (!flag) return
				vehicle.addCar(obj).then(res => {
					this.$store.commit('WatchVehicle',{
						vehicleStatus: '1',
						vehicleDesc: '已添加'
					})
					uni.navigateTo({
						url: './index'
					})
				})
				
			}
		},
	}
</script>

<style lang="scss" scoped>
	
	.credential {
		background-color: #fff;
		.item-left, .item-right {
			flex: 1;
			.image {
				width: 210upx;
				height: 210upx;
				background-color: $uni-border-color;
			}
		}
		.line {
			width: 2upx;
			height: 210upx;
			background-color: $uni-border-color;
		}
	}
	.not-car {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%,-90%);
		.image {
			width: 180upx;
			height: 150upx;
			background-color: #fff;
		}
	}
</style>

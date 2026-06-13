<!--车辆管理：员工车辆，添加车辆 -->
<template>
  <div class="my-basic-container vehicle_add">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-form ref="addform" :rules="addRules" :model="infoForm" label-width="80px" style="padding-top: 20px">
          <el-row :gutter="20">
            <el-col :span="7" class="minWidth" :offset="1">
              <el-form-item label="所属园区" prop="parkId">
                <el-select v-model="infoForm.parkId" placeholder="所属园区">
                  <el-option v-for="item in parkData" :key="item.id" :label="item.parkName" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="车牌号" prop="vehiclePlate">
                <el-input v-model="infoForm.vehiclePlate" clearable></el-input>
              </el-form-item>
              <el-form-item label="车牌类型" prop="ctId">
                <el-select v-model="infoForm.ctId" placeholder="请选择" clearable>
                  <el-option label="临时车牌" value="0"></el-option>
                  <el-option label="月租车牌" value="1"></el-option>
                  <el-option label="充值车牌" value="2"></el-option>
                  <el-option label="贵宾车牌" value="3"></el-option>
                  <el-option label="免费车牌" value="4"></el-option>
                  <el-option label="收费月租车牌" value="8"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="车类型" prop="vehicleType">
                <el-select v-model="infoForm.vehicleType" placeholder="请选择" clearable>
                  <el-option label="大型车" value="1"></el-option>
                  <el-option label="小型车" value="2"></el-option>
                  <el-option label="其他车" value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="车品牌" prop="vehicleBrand">
                <el-input v-model="infoForm.vehicleBrand" clearable></el-input>
              </el-form-item>
              <el-form-item label="车颜色" prop="vehicleColor">
                <el-select v-model="infoForm.vehicleColor" placeholder="请选择" clearable>
                  <el-option label="白" value="16777215"></el-option>
                  <el-option label="黑" value="0"></el-option>
                  <el-option label="红" value="255"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="infoForm.remark"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="7" class="minWidth" :offset="1">
              <el-form-item label="工号" prop="staffBadge">
                <el-input placeholder="请输入工号" v-model="infoForm.staffBadge">
                  <el-button type="info" slot="append" @click="selectStaffDetail('addform')" class="noPading" icon="el-icon-search"></el-button>
                </el-input>
              </el-form-item>
              <el-form-item label="联系人" prop="contactsUser">
                <el-input placeholder v-model="infoForm.contactsUser" disabled></el-input>
              </el-form-item>
              <el-form-item label="联系方式" prop="contactsPhone">
                <el-input v-model="infoForm.contactsPhone" disabled></el-input>
              </el-form-item>
              <el-form-item label="车辆状态" prop="cardState">
                <el-select v-model="infoForm.cardState" placeholder="车辆状态" clearable>
                  <el-option v-for="item in staffStatusiItem" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="有效期" prop="rangTimeIn">
                <el-date-picker
                  v-model="infoForm.rangTimeIn"
                  type="daterange"
                  range-separator="-"
                  value-format="yyyy-MM-dd"
                  start-placeholder="起始时间"
                  end-placeholder="截止时间"
                  clearable
                ></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          <div class="btns-bottom">
            <el-button type="primary" @click="saveInfor('addform')">提交</el-button>
            <el-button type="primary" @click="saveCancel('addform')" plain>取消</el-button>
          </div>
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>
<style lang="scss">
@use "@/styles/platform/vehicle/add" as *;
</style>
<script>
import { addObj, getStaffDetail, plate } from "./_service"
import { allPark } from '@/api/platform/_publicService'
import { mapGetters } from 'vuex'
import { isArrayFn } from '@/util/util'
export default {
  data() {
    var vehiclePlateValidator = (rule, value, callback) => {
      var temp = value.replace(/\s*/g, '')
      this.infoForm.vehiclePlate = temp.toLocaleUpperCase()
      var parten =
        /^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[A-Z])|([A-Z]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$/
      if (!parten.test(this.infoForm.vehiclePlate)) {
        callback(new Error('车牌号输入错误'))
      } else {
        const { parkId, vehiclePlate, vehicleAscription, source } = this.infoForm
        plate({ parkId, vehiclePlate, vehicleAscription, source }).then((response) => {
          if (response.data.data) {
            callback(new Error('车牌号已绑定，请勿重复绑定'))
            // this.$notify.error({
            //     title: '提示',
            //     message: '车牌号已绑定，请勿重复绑定！'
            //   });
          } else {
            callback()
          }
        })
      }
    }
    return {
      staffStatusiItem: [
        {
          label: '挂失',
          value: 0
        },
        {
          label: '过期',
          value: 1
        }
      ],
      infoForm: {
        vehiclePlate: '',
        vehicleType: '',
        vehicleBrand: '',
        vehicleColor: '',
        ctId: '',
        parkId: '',
        remark: '',
        staffBadge: '',
        contactsUser: '',
        contactsPhone: '',
        cardState: null,
        rangTimeIn: null,
        remark: ''
      },
      parkData: [],
      addRules: {
        vehiclePlate: [
          { required: true, message: '请输入车牌号', trigger: 'blur' },
          { validator: vehiclePlateValidator, trigger: 'blur' }
        ],
        vehicleType: [
          { required: true, message: "请选择车辆类型", trigger: "change" }
        ],
        ctId: [
          { required: true, message: "请选择车牌类型", trigger: "change" }
        ],
        vehicleBrand: [
          { required: true, message: '请输入车品牌', trigger: 'blur' }
        ],
        parkId: [{ required: true, message: '请选择所属园区', trigger: 'change' }],
        staffBadge: [
          { required: true, message: '请输入工号', trigger: 'blur' }
        ]
      },
      selectForm: {
        compId: ''
      },
      result: {
        code: '',
        message: ''
      }
    }
  },
  created() {
    this.getParks()
    this.doClearValidate('addform')
  },
  mounted() {},
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    // async selectChanged(parkId) {
    //   const res = await authorityList(parkId)
    //   this.authorityData = res.data.data
    //   this.infoForm.parkId = parkId
    // },
    async getParks() {
      const res = await allPark()
      this.parkData = res.data.data
    },
    saveInfor(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (this.validatenull(this.infoForm.contactsUser)) {
            this.$notify.error({
              title: '提示',
              message: '请根据工号查询员工信息！'
            })
          } else {
            if(this.infoForm.rangTimeIn && this.infoForm.rangTimeIn.length > 0){
              this.infoForm['startDate'] = this.infoForm.rangTimeIn[0]
              this.infoForm['endDate'] = this.infoForm.rangTimeIn[1]
            }
            const params = Object.assign({}, this.infoForm)
            if (isArrayFn(params.parkId)) {
              params.parkId = params.parkId.join(',')
            }
            addObj(params)
              .then((response) => {
                // this.$router.go(-1)
              })
              .catch(err => { console.error(err) })
          }
        } else {
          return false
        }
      })
    },
    saveCancel() {
      this.$router.go(-1)
    },
    //根据工号查询员工详细信息
    selectStaffDetail(formName) {
      let _this = this
      this.$refs[formName].validateField('staffBadge', (errorMessage) => {
        if (!errorMessage) {
          getStaffDetail(this.infoForm.staffBadge).then((response) => {
            let result = response.data.data
            if (!this.validatenull(result)) {
              let id = result.id
              if (!this.validatenull(id)) {
                this.infoForm.contactsUser = result.name
                this.infoForm.contactsPhone = result.phone
              } else {
                this.$notify.error({
                  title: '提示',
                  message: '暂无该工号对应员工信息，请检查工号是否正确！'
                })
              }
            }
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.noPading {
  padding: 12px 20px;
}
</style>

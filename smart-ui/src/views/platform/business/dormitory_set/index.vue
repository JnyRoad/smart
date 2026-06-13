<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="dormitoryForm" :rules="dormitoryRule" :inline="false" class="dot-form" :model="dormitoryForm" label-width="85px">
            <el-form-item label="所属园区" class="w1" prop="parkId">
              <parkSelect v-model="dormitoryForm.parkId" @getItem="parkChange"></parkSelect>
            </el-form-item>
            <el-row class>
              <el-col>
                <p class="tip">宿舍水电结算规则</p>
                <div class="radio-cont">
                  <el-radio v-model="type" label="1">按自然月</el-radio>
                  <div class="radio-input">
                    <span>每月按自然月天数计算，从1号开始到月底</span>
                    <!-- <el-input-number v-model="countDate1" :disabled="type !== '1'" controls-position="right" :min="1" :max="28" label="" class="w3"></el-input-number>
                    <span>日为结算日，范围1-28</span> -->
                  </div>
                </div>
                <div class="radio-cont">
                  <el-radio v-model="type" label="2">动态日期</el-radio>
                  <div class="radio-input">
                    <span>以上月</span>
                    <el-input-number v-model="countDate2" :disabled="type !== '2'" controls-position="right" :min="2" :max="28" label="" class="w3"></el-input-number>
                    <span>日为结算日，到下月的当日-1，为一个结算周期（输入2-28数字）</span>
                  </div>
                </div>
              </el-col>
              <!-- <el-col>
                <p class="tip">
                  启用宿舍水电智能抄表
                  <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="1" :inactive-value="0" v-model="switchModel"></el-switch>
                </p>
                <div class="radio-cont">
                  <div class="radio-input">
                    <span>当天每隔</span>
                    <el-input type="text" v-model="form1" class="w2"></el-input>
                    <span>小时，采集一次水电数据</span>
                  </div>
                </div>
                <div class="radio-cont">
                  <div class="radio-input">
                    <span>以</span>
                    <el-time-picker
                        v-model="time1"
                        placeholder=""
                        class="w3"
                        >
                    </el-time-picker>
                    <span>点后最近的一次数据，为当天抄表数据</span>
                  </div>
                </div>
              </el-col> -->
            </el-row>
            <el-form-item label>
              <el-button type="primary" @click="saveInfo()" :loading="addLoading">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { saveData, getDetails, allPark} from './_service'
export default {
  name: 'dormitory_set',
  data() {
    return {
      dormitoryForm: {
        parkId: null
      },
      dormitoryRule: {
        parkId: [{ required: true, message: '请选择关联园区', trigger: 'blur' }]
      },
      type: '',
      countDate1: null,
      countDate2: 2,
      addLoading: false,
    }
  },
  async created() {
    const res = await allPark();
    const d = res.data.data;
    this.dormitoryForm.parkId = d[0].id
    this.parkSet(d[0].id)
  },
  methods: {
    parkChange(val){
      this.parkSet(val.value)
    },
    async parkSet(id){
      const res = await getDetails(id)
      const data = res.data.data
      if(data !== null){
        this.dormitoryForm.parkId = data.parkId
        this.type = JSON.stringify(data.type)
        data.type === 1?this.countDate1 = data.countDate:this.countDate2 = data.countDate
      }else{
        this.type = ''
        this.countDate1 = null
        this.countDate2 = 2
      }
    },
    saveInfo() {
      this.$refs['dormitoryForm'].validate((valid) => {
        if (valid) {
          const obj = {
            parkId: this.dormitoryForm.parkId,
            type: parseInt(this.type),
            countDate: this.type === '1' ? this.countDate1 : this.countDate2
          }
          // const res = await saveData(obj)
          saveData(obj)
            .then((response) => {
              var dataResult = response.data.data
              if (dataResult === true) {
                this.$notify({
                  title: '成功',
                  message: '保存成功',
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.$notify({
                  title: '失败',
                  message: response.data.msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.addLoading = false
            }).catch((err) => {
              this.addLoading = false
            })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 900px;
  padding: 50px 0 0 60px;
  .el-checkbox + .el-checkbox {
    margin-left: 0;
  }
  .el-checkbox {
    margin: 0 20px 0 0;
  }
  .radio-cont {
    margin: 20px 0 20px 85px;
  }
  .radio-input {
    display: inline-block;
    margin-left: 20px;
  }
  .w1 {
    width: 500px;
  }
  .w2 {
    width: 60px;
    margin: 0 10px;
    .el-input__inner {
      height: 30px;
      line-height: 30px;
      padding: 0 10px;
      text-align: center;
    }
  }
  .w3 {
    width: 120px !important;
    margin: 0 10px;
    // .el-input__inner {
    //   height: 30px;
    //   line-height: 30px;
    //   padding: 0 10px;
    //   text-align: center;
    // }
  }

  .tip {
    color: #ed6d00;
    font-size: 16px;
    padding-bottom: 10px;
    margin-bottom: 20px;
  }

  .tip1 {
    color: #999;
    font-size: 14px;
    padding-bottom: 10px;
    margin-bottom: 20px;
  }

  .tip2 {
    color: #999;
    font-size: 12px;
    line-height: 25px;
  }

  .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label:before {
    content: '';
  }

  .tbl {
    width: 100%;
    margin-bottom: 50px;

    .el-input__inner {
      text-align: center;
      border: none;
      outline: none;
    }

    td {
      width: 50%;
      text-align: center;
      border: 1px solid #e0e0e0;
      position: relative;
    }

    .line-btn {
      position: absolute;
      right: -40px;
      top: 0;

      .el-button {
        font-size: 18px;
      }
    }
  }
}
</style>
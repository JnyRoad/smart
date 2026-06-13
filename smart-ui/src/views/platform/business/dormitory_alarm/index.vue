<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="addForm" :rules="addRule" :inline="false" class="dot-form" :model="addForm" label-width="150px">
            <el-form-item label="所属园区" class="w1" prop="parkId">
              <parkSelect v-model="addForm.parkId"></parkSelect>
            </el-form-item>
            <p class="tip">宿舍水电结算规则</p>
            <el-form-item label="">
              <div>
                <el-radio v-model="addForm.radio1" label="1">固定日期</el-radio>
                <span style="margin-left: 20px"
                  >每月
                  <el-input type="text" v-model="addForm.waterNum" style="width: 60px"></el-input>
                  日为结算日，范围1-28
                </span>
              </div>
              <div>
                <el-radio v-model="addForm.radio1" label="2">动态日期</el-radio>
                <span style="margin-left: 20px"
                  >每月最后倒数第
                  <el-input type="text" v-model="addForm.waterNum" style="width: 60px"></el-input>
                  天为结算日，每月自动推算到上月，为一个结算周期
                </span>
              </div>
            </el-form-item>
            <p class="tip">
              启用宿舍水电智能抄表
              <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="1" :inactive-value="0" v-model="addForm.switch"></el-switch>
            </p>
            <el-form-item label="" v-if="addForm.switch == 1">
              <p>
                当天每隔
                <el-input type="text" v-model="addForm.waterNum" style="width: 60px"></el-input>
                次小时，采集一次水电数据
              </p>
            </el-form-item>
            <el-form-item label="" v-if="addForm.switch == 1">
              <p>
                以
                <el-input type="text" v-model="addForm.waterNum" style="width: 60px"></el-input>
                点后最近的一次数据，为当天抄表数据
              </p>
            </el-form-item>
            <el-form-item label>
              <el-button type="primary" @click="saveInfo()">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
export default {
  name: 'alarm',
  data() {
    var validateIsNum = (rule, value, callback) => {
      let regName = /^-?\d+$/
      if (value === 0) {
        callback(new Error('请输入非0整数'))
      }
      if (!regName.test(value)) {
        callback(new Error('请输入整数'))
      } else {
        callback()
      }
    }
    return {
      addForm: {
        parkId: null,
        radio1: '1',
        waterNum: null,
        switch: 1
      },
      alarmListIndex: 1,
      listForm: {
        name: null,
        userName: null
      },
      addRule: {
        parkId: [{ required: true, message: '请选择关联园区', trigger: 'blur' }],
        waterNum: [{ validator: validateIsNum, trigger: 'blur' }]
      }
    }
  },
  created() {},
  methods: {
    saveInfo() {},
    addLine() {},
    delLine(i) {}
  },
  mounted: function () {}
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
  .w1 {
    width: 500px;
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
      width: 30%;
      text-align: center;
      border: 1px solid #e0e0e0;
      position: relative;
    }
    td:first-child {
      width: 10%;
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
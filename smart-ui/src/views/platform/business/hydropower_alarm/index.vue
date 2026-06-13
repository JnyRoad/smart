<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="addForm" :rules="addRule" :inline="false" class="dot-form" :model="addForm" label-width="150px">
            <el-form-item label="所属园区" class="w1" prop="parkId">
              <parkSelect v-model="addForm.parkId"></parkSelect>
            </el-form-item>
            <p class="tip">水表告警规则</p>
            <el-form-item label="">
                <p>当日每次采集用量，连续
                    <el-input type="text" v-model="addForm.waterNum" style="width: 60px"></el-input>
                    次，都超过用量
                    <el-input type="text" v-model="addForm.waterThreshold" style="width: 80px"></el-input>
                    时，将生成“水表超出每日用量”告警</p>
            </el-form-item>
            <p class="tip">电表告警规则</p>
            <el-form-item label="">
                <p>当日每次采集用量，连续
                    <el-input type="text" v-model="addForm.electricNum" style="width: 60px"></el-input>
                    次，都超过用量
                    <el-input type="text" v-model="addForm.electricThreshold" style="width: 80px"></el-input>
                    时，将生成“电表超出每日用量”告警</p>
            </el-form-item>
            <p class="tip">告警通知规则</p>
            <el-form-item label="">
              <table class="tbl">
                <tr>
                  <td>序号</td>
                  <td>账号名称</td>
                  <td>姓名</td>
                </tr>
                <tr v-for="(item, index) in addForm.alarmList" :key="index">
                    <td>
                      <el-input type="text" v-model="item.receiver" readonly></el-input>
                    </td>
                    <td>
                      <el-input type="text" v-model="item.email" readonly></el-input>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-delete" @click="delLine(index)"></el-button>
                      </div>
                    </td>
                  </tr>
                <tr>
                  <td>{{alarmListIndex}}</td>
                  <td>
                    <el-form-item label prop="receiver">
                      <el-input type="text" v-model="listForm.userName" placeholder="点击输入账号名称"></el-input>
                    </el-form-item>
                  </td>
                  <td>
                    <el-form-item label prop="email">
                      <el-input type="text" v-model="listForm.name" placeholder="点击输入姓名"></el-input>
                    </el-form-item>
                    <div class="line-btn">
                      <el-button type="text" icon="el-icon-plus" @click="addLine"></el-button>
                    </div>
                  </td>
                </tr>
              </table>
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
        waterNum:null,
        waterThreshold:null,
        electricNum:null,
        electricThreshold:null,
        alarmList: [],
      },
      alarmListIndex: 1,
      listForm: {
        name: null,
        userName: null
      },
      addRule: {
        parkId: [{ required: true, message: '请选择关联园区', trigger: 'blur' }],
        waterNum: [{ validator: validateIsNum, trigger: 'blur' }],
      }
    }
  },
  created() {},
  methods: {
      saveInfo(){

      },
      addLine(){

      },
      delLine(i){

      }
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
    td:first-child{
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
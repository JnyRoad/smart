<!--园区服务：调查表管理:添加/编辑 -->
<template>
  <div class="my-basic-container ques_add">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-form
          class="minWidth"
          ref="addform"
          :rules="addRules"
          :model="addform"
          label-width="100px"
          style="padding-top:20px;"
        >
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="addform.parkId" @doChange="getComp" :myDisabled="isEdit"></parkSelect>
          </el-form-item>
          <el-form-item label="调查表名称" prop="title">
            <el-input v-model="addform.title" clearable></el-input>
          </el-form-item>
          <el-form-item label="有效期" prop="timeRange">
            <el-date-picker
              v-model="addform.timeRange"
              type="datetimerange"
              range-separator="-"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd HH:mm:ss"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :default-time="['00:00:00', '23:59:59']"
              clearable
            ></el-date-picker>
          </el-form-item>
          <!-- <el-form-item label="状态" prop="status">
            <el-select v-model="addform.status" placeholder="状态" clearable>
              <template v-for="(item, index) in statusItem">
                <el-option :label="item" :value="index" :key="index"></el-option>
              </template>
            </el-select>
          </el-form-item>-->
          <el-form-item label="发布范围" prop="compIds">
            <el-select v-model="addform.compIds" placeholder="请选择" multiple collapse-tags clearable>
              <template v-for="(item, index) in buOptions">
                <el-option :label="item.compName" :value="item.compId" :key="index"></el-option>
              </template>
            </el-select>
          </el-form-item>
        </el-form>
        <el-form ref="quesform" :model="quesform" label-width="100px" class="minWidth2">
          <el-form-item label="问题列表" prop="questions">
            <div class="ques-block">
              <template v-for="(quesItem, quesIndex) in quesform.questions">
                <el-card class="ques-item" shadow="always" :key="quesIndex">
                  <div>
                    <table class="ques-table">
                      <tr>
                        <td>问题{{quesIndex+1}}</td>
                        <td>
                          <el-form-item
                            :prop="'questions.'+quesIndex+'.title'"
                            :rules="{ required: true, message: '请输入问题', trigger: 'blur' }"
                          >
                            <el-input v-model="quesItem.title" clearable placeholder="请输入问题"></el-input>
                          </el-form-item>
                        </td>
                        <td>
                          <el-form-item
                            :prop="'questions.'+quesIndex+'.type'"
                            :rules="{ required: true, message: '请选择问题类型', trigger: 'change' }"
                          >
                            <el-select
                              v-model="quesItem.type"
                              placeholder="请选择问题类型"
                              clearable
                              @change="quesTypeHandle(quesItem)"
                            >
                              <template v-for="(item, index) in quesTypeItem">
                                <el-option :label="item" :value="index" :key="index"></el-option>
                              </template>
                            </el-select>
                          </el-form-item>
                          <div class="quesbtn-outer">
                            <div>
                              <el-button
                                icon="el-icon-delete"
                                type="text"
                                @click="delQues(quesIndex)"
                              >删除问题</el-button>
                            </div>
                            <div v-if="quesItem.hasAnswer">
                              <el-button
                                icon="el-icon-plus"
                                type="text"
                                @click="addAnswer(quesItem)"
                              >添加答案</el-button>
                            </div>
                          </div>
                        </td>
                      </tr>
                      <template v-for="(answerItem, answerIndex) in quesItem.answers">
                        <tr :key="quesIndex+answerIndex+''">
                          <td>答案{{answerIndex+1}}</td>
                          <td>
                            <el-form-item
                              :prop="'questions.'+quesIndex+'.answers.'+answerIndex"
                              :rules="{ required: true, message: '请输入答案', trigger: 'blur' }"
                            >
                              <el-input
                                v-model="quesItem.answers[answerIndex]"
                                clearable
                                placeholder="请输入答案"
                              ></el-input>
                            </el-form-item>
                          </td>
                          <td>
                            <el-button
                              icon="el-icon-delete"
                              type="text"
                              @click="delAnswer(quesItem.answers, answerIndex)"
                            >删除答案</el-button>
                          </td>
                        </tr>
                      </template>
                    </table>
                  </div>
                </el-card>
              </template>
              <el-button icon="el-icon-plus" type="text" @click="addQues()">添加问题</el-button>
            </div>
          </el-form-item>
        </el-form>
        <div class="btns-bottom">
          <el-button type="primary" @click="saveInfor('addform')">{{isEdit?'确定修改':'提交'}}</el-button>
          <el-button type="primary" @click="saveCancel('addform')" plain>取消</el-button>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>
<style lang="scss">
@use "@/styles/platform/vehicle/add" as *;
</style>
<script>
import {
  getById,
  addObj,
  putObj,
  getCompsObj
} from "@/api/platform/park_service/paper";
import { mapGetters } from "vuex";
import { validatenull } from "@/util/validate";
const statusOption = ["未开始", "进行中", "已结束"];
const quesTypeOption = ["单选题", "多选题", "填空题"];
export default {
  data() {
    var driverLicenseIdValidator = (rule, value, callback) => {
      var driverLicenseIdImg = this.addform.driverLicenseId;
      if (
        driverLicenseIdImg == null ||
        driverLicenseIdImg == "" ||
        driverLicenseIdImg == undefined
      ) {
        callback(new Error("请选择驾驶证照片！"));
      } else {
        callback();
      }
    };
    return {
      isEdit: false,
      statusItem: statusOption, //状态
      quesTypeItem: quesTypeOption, //问题类型
      addform: {
        title: undefined,
        startTime: undefined,
        endTime: undefined,
        // status: undefined,
        parkId: undefined,
        compIds: [],
        questions: []
      },
      addRules: {
        title: [
          { required: true, message: "请输入调查表名称", trigger: "change" }
        ],
        parkId: [
          { required: true, message: "请选择所属园区", trigger: "change" }
        ],
        // status: [{ required: true, message: "请选择状态", trigger: "change" }],
        // compIds: [
        //   { required: true, message: "请选择发布范围", trigger: "change" }
        // ],
        timeRange: [
          { required: true, message: "请选择有效期", trigger: "change" }
        ],
        questions: [
          { required: true, message: "请填写问题", trigger: "change" }
        ]
      },
      quesform: {
        questions: []
      },
      buOptions: []
    };
  },
  created() {},
  mounted() {
    if (!validatenull(this.$route.query.id)) {
      this.isEdit = true;
      this.getDetail();
    }
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    async getDetail() {
      const res = await getById(this.$route.query.id);
      let data = res.data.data;
      this.getComp(data.parkId);
      this.addform = Object.assign(
        { timeRange: [data.startTime, data.endTime] },
        data
      );
      let quesArr = [];
      this.addform.questions.forEach(el => {
        let obj = { hasAnswer: false };
        if (el.type === 0 || el.type === 1) {
          obj.hasAnswer = true;
        }
        quesArr.push(Object.assign(obj, el));
      });
      this.quesform.questions = quesArr;
    },
    getComp(id) {
      //获取BU集合
      getCompsObj(id).then(response => {
        this.buOptions = response.data.data;
      });
    },
    //删除问题
    delQues(index) {
      this.quesform.questions.splice(index, 1);
    },
    //删除答案
    delAnswer(item, index) {
      item.splice(index, 1);
    },
    //添加问题
    addQues() {
      let obj = {
        title: "",
        type: "",
        hasAnswer: false,
        answers: []
      };
      this.quesform.questions.push(obj);
    },
    //添加问题
    addAnswer(item) {
      item.answers.push("");
    },
    quesTypeHandle(item) {
      item.answers = [];
      if (item.type === 0 || item.type === 1) {
        item.hasAnswer = true;
        item.answers.push("");
      } else {
        item.hasAnswer = false;
        item.answers = [];
      }
    },
    async saveInfor() {
      await this.$refs["addform"].validate();
      await this.$refs["quesform"].validate();
      this.addform.questions = this.quesform.questions;
      if (this.addform.questions.length == 0) {
        this.$message.error("请给调查表添加问题！");
        return;
      }
      let temp = [];
      this.addform.questions.forEach(el => {
        if (el.hasAnswer && el.answers.length === 0) {
          temp.push(el);
        }
      });
      if (temp.length > 0) {
        this.$message.error("有单选题或多选题没有添加答案，请检查！");
        return;
      }
      this.addform.startTime = this.addform.timeRange[0];
      this.addform.endTime = this.addform.timeRange[1];
      if (this.isEdit) {
        putObj(this.addform)
          .then(response => {
            this.$router.go(-1);
          })
          .catch(err => { console.error(err) });
      } else {
        addObj(this.addform)
          .then(response => {
            this.$router.go(-1);
          })
          .catch(err => { console.error(err) });
      }
    },
    saveCancel() {
      this.$router.go(-1);
    }
  }
};
</script>
<style lang="scss" scoped>
.ques_add {
  min-width: 1000px;
}
.noPading {
  padding: 12px 20px;
}
.minWidth {
  width: 500px;
}
.minWidth2 {
  width: 830px;
}
.ques-block ::v-deep {
  .el-card.is-always-shadow {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border: 1px solid #ebeef5;
  }
  .el-card__body {
    padding: 0 10px;
  }
}
.ques-item {
  margin-bottom: 20px;
  min-height: 130px;
  padding: 15px 0;
}
.ques-table ::v-deep {
  width: 600px;
  color: #606266;
  td {
    text-align: center;
    height: 55px;
  }
  td:nth-child(2) {
    padding: 0 20px;
  }
  td:nth-child(3) {
    position: relative;
    width: 150px;
  }
  .el-input__inner {
    text-align: center;
  }
  .quesbtn-outer {
    position: absolute;
    top: 8px;
    right: -100px;
    width: 100px;
    .el-button {
      margin-bottom: 18px;
    }
  }
}
</style>

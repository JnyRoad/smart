<!--园区管理，配置信息，组织关系  -->
<template>
  <div class="content">
    <el-form ref="form" :model="form" :rules="rules" class="form">
      <div class="left">
        <div class="block-bu">
          <p class="red-star p1">组织关联BU</p>
          <el-form-item label prop="workComp">
            <el-transfer
              filterable
              :titles="['可选择BU', '已选择BU']"
              filter-placeholder="请输入BU名称"
              v-model="form.workCompList"
              :data="workCompList"
            ></el-transfer>
          </el-form-item>
        </div>
      </div>
      <div class="right">
        <el-row class>
          <el-col :span="20" :offset="2">
            <p class="red-star p1">物联中心关联</p>
            <el-form-item label="物流中心ID" prop="logisticId" label-width="100px">
              <el-input v-model="form.logisticId" placeholder="请输入"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row class>
          <el-col :span="20" :offset="2">
            <p class="red-star p1">车辆入园申请设置</p>
            <div>请选择不允许车辆入园申请的职层</div>
            <el-form-item label prop="jcheList">
              <el-checkbox-group v-model="form.jcheList" class="zclist">
                <el-checkbox
                  v-for="(item, index) in jcheListOption"
                  :label="item.value"
                  :key="index"
                >{{item.label}}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <div class="btns">
              <el-button type="primary" @click="cancel" plain>取消</el-button>
              <el-button type="primary" @click="save('form')" :loading="loading">保存</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-form>
  </div>
</template>
<script>
import {
  getCompList,
  viewOrgInfo,
  saveOrgInfo
} from "@/api/platform/area/park-set";
import { getJchesObj } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      workCompList: [],
      jcheListOption: [],
      form: {
        logisticId: null,
        workCompList: [],
        jcheList: []
      },
      loading: false,
      rules: {
        logisticId: [
          { required: true, message: "请输入园区名称", trigger: "blur" }
        ]
      }
    };
  },
  props: {
    parkId: [Number, String]
  },
  created() {
    // 获取可签约Bu列表
    getCompList({ compTitle: "" }).then(response => {
      let self = this;
      // console.log(
      //   "setBu.getCompList.response====================>" +
      //     JSON.stringify(response.data.data)
      // );
      response.data.data.forEach((workComp, $index) => {
        self.workCompList.push({
          label: workComp.title,
          key: workComp.compid
        });
      });
    });

    // 获取职层字典
    getJchesObj().then(response => {
      let self = this;
      response.data.data.forEach((item, $index) => {
        self.jcheListOption.push({ label: item.typeName, value: item.typeCode });
      });
      //console.log("self.jcheLis====================>"+JSON.stringify(self.jcheListOption));
    });

    //查看园区组织关系
    viewOrgInfo(this.parkId).then(response => {
      let self = this;
      self.form = response.data.data;
      //Stirng转成Int
      if (self.form.workCompList) {
        let workCompIntList = [];
        self.form.workCompList.forEach(item => {
          workCompIntList.push(parseInt(item));
        });
        self.form.workCompList = workCompIntList;
      }else{
        self.form.workCompList = []
      }
      self.form.jcheList?'': self.form.jcheList=[]
    });
  },
  mounted() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    filterMethod(query, item) {
      return item.pinyin.indexOf(query) > -1;
    },
    selectBu() {},
    cancel() {
      const src = `/platform/area/park`;
      this.$router.push({
        path: src
      });
    },
    save(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          saveOrgInfo(this.form).then(response => {
            this.$router.push({
              path: `/platform/area/park`
            });
          });
        } else {
          return false;
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.content ::v-deep {
  .form {
    display: flex;
    flex-direction: row;
    .p1 {
      margin-bottom: 20px;
      font-size: 16px;
      font-weight: bold;
    }
    .left {
      width: 500px;
      margin-right: 50px;
      .el-transfer__buttons {
        .el-button + .el-button {
          margin: 0;
        }
      }
    }
    .right {
      flex: 1;
    }
    .bucklist {
      padding-top: 20px;
      .el-checkbox,
      .el-checkbox__input {
        display: block;
      }
      .el-checkbox + .el-checkbox {
        margin-left: 0;
      }
    }
    .zclist {
      padding-top: 20px;
      .el-checkbox{
        margin-right: 20px;
      }
      .el-checkbox+.el-checkbox{
        margin-left: 0;
      }
    }
  }
}
</style>

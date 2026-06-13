<!--招聘管理，消息模板  -->
<template>
  <div class="my-basic-container msnmodel">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <p class="box-orange">短信通知模板</p>
          <el-form ref="form" class="dot-form" :model="msnForm" label-width="110px">
            <el-form-item label="消息类型">
              <el-checkbox-group v-model="msnForm.type">
                <el-checkbox label="短信" name="type" disabled checked></el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="短信标题">
              <el-select v-model="form.id" placeholder="请选择" @change="getTemplateContent">
                <el-option
                  v-for="item in template"
                  :key="item.id"
                  :label="item.tempName"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="消息内容" class="push-item">
              <el-input type="textarea" :rows="8" placeholder disabled v-model="textarea"></el-input>
            </el-form-item>
            <el-form-item label>
              <!-- <el-button type="primary"  @click="saveInfo" >保存</el-button>
              <el-button type="primary"  @click="handleCancel" plain>取消</el-button>-->
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from "@/api/platform/recruit/msnmodel";
import { mapGetters } from "vuex";

export default {
  name: "msnmodel",
  data() {
    return {
      activeName: "面试通知",
      msnForm: {
        type: "发送短信",

        msg: ""
      },
      textarea: "",
      template: [],
      form: {
        id: 0
      }
    };
  },
  created() {
    this.getList();
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getList() {
      let _this = this;
      fetchList().then(response => {
        _this.template = response.data.data;
        if (_this.template.length > 0) _this.form.id = _this.template[0].id;
        _this.textarea = _this.template[0].tempContent;
      });
    },
    getTemplateContent: function(val) {
      var _this = this;
      this.template.forEach(function(element) {
        if (element.id == val) {
          _this.textarea = element.tempContent;
        }
      }, this);
    },
    saveInfo() {
      var _this = this;
      _this.$message({
        showClose: true,
        message: "成功",
        type: "success"
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.content {
  padding-top: 50px;
  padding-left: 40px;
}
.box-orange {
  font-size: 16px;
}
.dot-form {
  padding-top: 10px;
  padding-left: 30px;
  width: 80%;
  max-width: 700px;
}
.push-item {
  margin-top: 70px;
}
</style>

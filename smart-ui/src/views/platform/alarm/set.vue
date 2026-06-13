<!--安防中心，警报设置  -->
<template>
  <div class="my-basic-container alarm-set">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <p class="box-orange">警报设置</p>
          <el-form ref="form" class="dot-form" :model="alarmForm" label-width="110px">
            <el-form-item label="推送方式">
              <el-checkbox-group v-model="alarmForm.type">
                <el-checkbox label="发送短信" name="type" disabled checked></el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="推送模板">
              <el-select v-model="alarmForm.template" placeholder="请选择" clearable>
                <el-option label="模板一" value="shanghai"></el-option>
                <el-option label="模板二" value="beijing"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="推送人" class="push-item">
              <table class="push-list">
                <tr>
                  <td>推送人信息</td>
                  <td>
                    手机号码
                    <div class="circle-btns">
                      <el-button type="primary" icon="el-icon-plus" @click="addItem" plain></el-button>
                    </div>
                  </td>
                </tr>
                <template v-for="(item, index) in alarmForm.person">
                  <tr :key="index">
                    <td>
                      <el-input v-model="item.info"></el-input>
                    </td>
                    <td>
                      <el-input v-model="item.tel"></el-input>
                      <div class="circle-btns">
                        <el-button type="primary" icon="el-icon-minus" @click="delItem(item)" plain></el-button>
                      </div>
                    </td>
                  </tr>
                </template>
              </table>
            </el-form-item>
            <el-form-item label>
              <el-button type="primary" @click="saveInfo">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchDetail } from "@/api/platform/alarm/set";
import { mapGetters } from "vuex";

export default {
  name: "alarm",
  data() {
    return {
      alarmForm: {
        type: "发送短信",
        template: [],
        person: [
          {
            info: "",
            tel: ""
          }
        ]
      }
    };
  },
  created() {
    fetchDetail().then(response => {
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    saveInfo() {
      var _this = this;
      _this.$message({
        showClose: true,
        message: "警报设置成功",
        type: "success"
      });
    },
    addItem() {
      this.alarmForm.person.push({
        info: "",
        tel: ""
      });
    },
    delItem(item) {
      var index = this.alarmForm.person.indexOf(item);
      if (index !== -1) {
        this.alarmForm.person.splice(index, 1);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.content {
  padding-top: 50px;
  padding-left: 40px;
}
.push-list {
  width: 100%;
  text-align: center;
  tr:first-child {
    background: #f7f7f7;
  }
  ::v-deep .el-input__inner {
    border: none;
    text-align: center;
  }
  td {
    width: 50%;
    border: 1px solid #e0e0e0;
  }
  td:last-child {
    position: relative;
  }
  .circle-btns {
    position: absolute;
    top: 0;
    right: -40px;
  }
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

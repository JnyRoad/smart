<!--日志管理：ehr员工同步设置 -->
<template>
  <div class="my-basic-container staff-setting">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-form
          ref="searchForm"
          :rules="addRule"
          :inline="true"
          :model="searchForm"
          label-width="120px"
          class="topForm"
          size="mini"
        >
          <el-row>
            <el-col :span="24">
              <el-form-item label="EHR同步BU" prop="compIds" class="item1">
                <buAllSelect v-model="searchForm.compIds" :myMultiple="true"></buAllSelect>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="EHR同步频率" prop="time" class="item1">
                <el-input v-model="searchForm.time" clearable></el-input>
              </el-form-item>
              <el-form-item label prop="timeUnit" class="item2">
                <el-select v-model="searchForm.timeUnit" placeholder>
                  <el-option
                    v-for="item in timeStatus"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row class="btm-row">
            <el-col :span="24">
              <el-button type="primary" @click="onSubmit(searchForm)" :loading="saveLoading">保存</el-button>
              <el-button type="primary" @click="saveCancel()" plain>取消</el-button>
            </el-col>
          </el-row>
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>


<script>
import { fetchList, addList } from "@/api/platform/records/staff_setting";
import { fetchMenuTree } from "@/api/platform/limit/index";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
const timeStatusOption = [
  { label: "时", value: "时" },
  { label: "分", value: "分" },
  { label: "日", value: "日" },
  { label: "周", value: "周" },
  { label: "月", value: "月" }
];
export default {
  name: "staff_setting",
  data() {
    var checkTime = (rule, value, callback) => {
      if (!/^[1-9]\d*$/.test(value)) {
        callback(new Error("频率值应为正整数"));
      } else {
        callback();
      }
    };
    return {
      saveLoading: false,
      timeStatus: timeStatusOption,
      value1: [],
      value2: [],
      searchForm: {
        //搜索菜单表单
        compIds: [],
        time: undefined,
        timeUnit: "时"
      },
      addRule: {
        compIds: [
          { required: true, message: "请选择需要同步的BU", trigger: "change" }
        ],
        time: [
          { required: true, message: "请输入同步的频率", trigger: "blur" },
          { validator: checkTime, trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.fetchList();
    this.doClearValidate("searchForm");
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {},
  methods: {
    fetchList() {
      var _this = this;
      _this.value2 = [];
      fetchList().then(response => {
        var selected = response.data.data;
        if (selected.length > 0) {
          _this.searchForm.time = selected[0].time;
          _this.searchForm.timeUnit = selected[0].timeUnit;
        }
        selected.forEach(function(element) {
          _this.searchForm.compIds.push(Number(element.compId));
        }, this);
      });
    },
    saveCancel() {
      this.fetchList();
      this.$refs["searchForm"].clearValidate();
    },
    onSubmit(params) {
      this.$refs["searchForm"].validate(valid => {
        if (valid) {
          this.saveLoading = true;
          addList(params)
            .then(response => {
              this.this.saveLoading = false;
              if (response.data.data == true) {
                this.$notify({
                  title: "成功",
                  message: "成功",
                  type: "success",
                  duration: 2000
                });
              }
            })
            .catch(err => {
              this.saveLoading = false;
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
.topForm {
  padding: 20px;
}
.testimg {
  width: 100px;
  height: 100px;
  border: 1px solid red;
}
.item1 {
  // width: 400px;
  // display: inline-block;
  margin-bottom: 30px;
}
.item2 {
  width: 80px;
}
.btm-row {
  padding-left: 120px;
  margin-top: 100px;
  .el-button {
    margin-right: 50px;
  }
}
.upload-demo {
  display: inline-block;
  margin-right: 20px;
}
.tips {
  max-width: 500px;
  border: 1px solid red;
}
.num {
  padding: 0 5px;
  font-weight: bold;
}
.red {
  color: red;
}
.importBtn {
  margin-right: 20px;
}
.fileBtn {
  display: none;
}
.importCont {
  line-height: 25px;
  padding-bottom: 30px;
}
.fileTb {
  margin: 10px 0 0 0;
  width: 100%;
  .haveNo {
    color: red;
  }
  td {
    border: 1px solid #e0e0e0;
    text-align: center;
    padding: 0 10px;
    height: 35px;
  }
}
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
</style>

<!--招聘管理，批量入职管理 -->
<template>
  <div class="my-basic-container parking">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="stepOuter">
          <el-steps :active="active" align-center>
            <el-step title="STP 1" description="导入Excel数据"></el-step>
            <el-step title="STP 2" description="批量入职登记"></el-step>
          </el-steps>
        </div>
        <div class="step2">
          <el-form
            ref="addForm"
            :rules="addRule"
            :inline="true"
            :model="addForm"
            class="topForm"
            size="mini"
          >
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addForm.parkId" @doChange="parkChange"></parkSelect>
            </el-form-item>
            <el-form-item label="BU" prop="compId">
              <buSelect
                v-model="addForm.compId"
                :parkId="addForm.parkId"
                @getItem="getCompItem"
                @doChange="buChange"
              ></buSelect>
            </el-form-item>
            <el-form-item label="部门" prop="depId">
              <deptSelect
                v-model="addForm.depId"
                :compId="addForm.compId"
                @getItem="getDepItem"
                @doChange="deptChange"
              ></deptSelect>
            </el-form-item>
            <el-form-item label="岗位" prop="jobId">
              <jobSelect v-model="addForm.jobId" :depId="addForm.depId" @getItem="getJobItem"></jobSelect>
            </el-form-item>
          </el-form>

          <avue-crud
            ref="crud"
            :data="tableData"
            :table-loading="tableLoading"
            :option="tableOption"
            @selection-change="selectChange"
          >
            <template slot-scope="scope" slot="menu">
              <el-button
                type="text"
                icon="el-icon-view"
                @click="deleteItem(scope.row, scope.row.$index)"
              >删除</el-button>
            </template>
          </avue-crud>
          <div class="btns">
            <el-button
              type="primary"
              @click="submit('addForm')"
              icon="el-icon-check"
              :loading="loading"
            >批量入职登记</el-button>
          </div>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import { importStaff } from "@/api/platform/recruit/entry";
  import { tableOption } from "@/const/crud/platform/recruit/entry";
  import { mapGetters } from "vuex";
  import XLSX from "xlsx";

  export default {
    name: "parking",
    data() {
      return {
        loading: false,
        active: 1,
        addForm: {
          //菜单表单
          compId: "",
          compName: "",
          depId: "",
          depName: "",
          jobId: "",
          jobName: "",
          parkId:"",
          staffList: []
        },
        addRule: {
          parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
          compId: [{ required: true, message: "请选择BU", trigger: "change" }],
          depId: [{ required: true, message: "请选择部门", trigger: "change" }],
          jobId: [{ required: true, message: "请选择岗位", trigger: "change" }]
        },
        selectStaffs: [], //当前选中员工的集合
        tableLoading: false,
        tableData: [],
        tableOption: tableOption
      };
    },
    created: function() {
      let fileData = JSON.parse(localStorage.getItem("entryFileInfo"));
      if (!this.validatenull(fileData)) {
        this.tableData = fileData;
      }
    },
    mounted: function() {},
    watch: {
      selectStaffs: function(val) {
        val.length > 0 ? (this.hasSelect = true) : (this.hasSelect = false);
      }
    },
    computed: {},
    methods: {
      submit(formName) {
        let _this = this;
        this.$refs[formName].validate(valid => {
          if (valid) {
            let staffList = this.addForm.staffList;
            if (!this.validatenull(staffList)) {
              this.loading = true;
              importStaff(this.addForm)
                .then(response => {
                  if(!response.data.success){
                    this.loading = false;
                    this.$notify.error({
                      title: '失败',
                      message: '员工批量入职失败！'
                    });
                    return
                  }
                  if (response.data.data) {
                    //导入成功后
                    this.loading = false;
                    this.$notify({
                      title: "成功",
                      message: "员工批量入职成功！",
                      type: "success"
                    });
                    var indexs = [];
                    staffList.forEach(function (item) {
                      //要实际被删除的集合的索引
                      indexs.push(item.$index);
                    });
                    //先计算能被正常删除的索引：
                    var newIndexs = indexs.map(function (val, idx) {
                      return val - idx;
                    });
                    /**因为splice是从第二次的时候出现bug的，
                     *所以我们第一个索引的位置是正确的，
                     *从第二次开始 “n-1”，也就是刚好对应索引数组的索引。**/
                    newIndexs.forEach(function (index) {
                      _this.tableData.splice(index, 1);
                    });

                    localStorage.setItem(
                      "entryFileInfo",
                      JSON.stringify(_this.tableData)
                    );
                  }
                })
                .catch(err => {
                  this.loading = false;
                  // this.$notify.error({
                  //   title: '失败',
                  //   message: '员工批量入职失败！'
                  // });
                });
            } else {
              this.$message({
                message: "请选择要批量入职的员工信息！",
                type: "warning"
              });
              return false;
            }
          } else {
            return false;
          }
        });
      },
      deleteItem(row, index) {
        this.tableData.splice(index, 1);
        localStorage.setItem("entryFileInfo", JSON.stringify(this.tableData));
      },
      selectChange(val) {
        //序号那边选择事件
        this.addForm.staffList = val;
      },
      getCompItem(obj) {
        this.addForm.compName = obj.label;
      },
      getDepItem(obj) {
        this.addForm.depName = obj.label;
      },
      getJobItem(obj) {
        this.addForm.jobName = obj.label;
      },
      parkChange() {
        this.addForm.compId = undefined;
        this.addForm.depId = undefined;
        this.addForm.jobId = undefined;
      },
      buChange() {
        this.addForm.depId = undefined;
        this.addForm.jobId = undefined;
      },
      deptChange() {
        this.addForm.jobId = undefined;
      }
    }
  };
</script>

<style lang="scss" scoped>
  .stepOuter {
    color: #ed6d00;
    width: 400px;
    margin: 0 auto;
    padding: 50px 0 60px 0;
  }
  .btns {
    text-align: center;
    padding-top: 60px;
  }
  .step1 ::v-deep {
    width: 400px;
    margin: 0 auto;
    .step1Info {
      text-align: center;
      line-height: 30px;
    }
    .el-icon-document {
      font-size: 20px;
      margin: -2px 5px 0 0;
    }
    .fileInfo {
      width: 245px;
      margin: 20px auto;
      text-align: center;
      padding: 30px 10px;
      background: #fafafa;
    }
  }
</style>

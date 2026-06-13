<!--园区管理，配置信息，招聘设置  -->
<template>
  <div class="content">
    <el-form ref="form" :model="form" :inline="true" class="form">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>合同签约单位</span>
          <el-button
            type="primary"
            icon="el-icon-plus"
            style="float: right;"
            @click="addCompany('form')"
          >新增
          </el-button>
        </div>
        <el-form-item label="BU" prop="workCompId" label-width="30px">
          <el-select
            v-model="selectForm.workCompId"
            placeholder="请选择"
            @change="buChange(selectForm.workCompId)"
            clearable
          >
            <el-option
              v-for="item in workCompList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="签约单位ID" prop="workOrgId" label-width="90px">
          <el-select
            v-model="selectForm.workOrgId"
            placeholder="请选择"
            @change="idChange(selectForm.workOrgId)"
            clearable
          >
            <el-option
              v-for="item in workOrgIdList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="工作地点" prop="workBaseCode" label-width="90px">
          <el-select v-model="selectForm.workBaseCode" placeholder="请选择"
                     @change="workChange(selectForm.workBaseCode)"
                     clearable>
            <el-option
              v-for="item in workBaseList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <table class="infoTb">
          <thead>
          <tr>
            <th>BU</th>
            <th>签约单位</th>
            <th>工作地点</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(item,index) in form.compOrgList" :key="index">
            <td>{{item.workCompTitle}}</td>
            <td>{{item.workOrgName}}</td>
            <td>{{item.workBaseName}}</td>
            <td>
              <el-button type="text" icon="el-icon-delete" @click="handleDel(index)">删除</el-button>
            </td>
          </tr>
          <tr v-if="form.compOrgList.length ==0">
            <td colspan="3" class="noId">当前还没有添加任何“合同签约单位”</td>
          </tr>
          </tbody>
        </table>
      </el-card>
      <div class="btns">
        <el-button type="primary" @click="cancel" plain>取消</el-button>
        <el-button type="primary" @click="save('form')" :loading="loading">保存</el-button>
      </div>
    </el-form>
  </div>
</template>
<script>
  import {
    getRecruitSetInfo,
    getWorkBaseList,
    getCompList,
    getConComanyList,
    saveRecruitSetInfo
  } from "@/api/platform/area/park-set";
  import {mapGetters} from "vuex";

  export default {
    data() {
      return {
        form: {
          parkId: this.parkId,
          compOrgList: []
        },
        selectForm: {
          workBaseCode:"",
          workBaseName:"",
          workCompId: "",
          workCompTitle: "",
          workOrgId: "",
          workOrgName: ""
        },
        workCompList: [], //签约BU列表
        workOrgIdList: [], //签约单位列表
        workBaseList: [], //工作地点列表
        loading: false,

      };
    },
    props: {
      parkId: [Number, String]
    },
    created() {
      //获取招聘配置信息
      getRecruitSetInfo(this.parkId).then(response => {
        let self = this;
        if(response.data.data != null) {
          self.form = response.data.data;
        }
      });
      // 获取工作地点列表
      getWorkBaseList({workBaseName: ""}).then(response => {
        let self = this;
        response.data.data.forEach((item, $index) => {
          self.workBaseList.push({
            value: item.workBaseCode,
            label: item.workBaseName
          });
        });
      });

      // 获取可签约Bu列表
      getCompList({compTitle: ""}).then(response => {
        let self = this;
        response.data.data.forEach((item, $index) => {
          self.workCompList.push({value: item.compid, label: item.title});
        });
      });

      // 获取可签约单位列表
      getConComanyList({workOrgName: ""}).then(response => {
        let self = this;
        response.data.data.forEach((item, $index) => {
          self.workOrgIdList.push({
            value: item.workOrgId,
            label: item.workOrgName
          });
        });
      });
    },
    mounted() {
    },
    computed: {
      ...mapGetters(["permissions"])
    },
    methods: {
      handleDel(index) {
        this.form.compOrgList.splice(index, 1);
      },
      addCompany(formName) {
        let {
          workBaseCode,
          workBaseName,
          workCompId,
          workCompTitle,
          workOrgId,
          workOrgName
        } = this.selectForm;
        if (this.validatenull(workCompId)) {
          this.$message.error("请选择BU");
          return;
        }
        if (this.validatenull(workOrgId)) {
          this.$message.error("请选择签约单位ID");
          return;
        }
        if (this.validatenull(workBaseCode)) {
          this.$message.error("请选择签约单位ID");
          return;
        }
        this.form.compOrgList.push({
          workBaseCode,
          workBaseName,
          workCompId,
          workCompTitle,
          workOrgId,
          workOrgName
        });
      },
      cancel() {
        const src = `/platform/area/park`;
        this.$router.push({
          path: src
        });
      },
      buChange(val) {
        this.selectForm.workCompTitle = this.workCompList.filter(
          ele => ele.value === val
        )[0].label;
      },
      idChange(val) {
        this.selectForm.workOrgName = this.workOrgIdList.filter(
          ele => ele.value === val
        )[0].label;
      },
      workChange(val) {
        this.selectForm.workBaseName = this.workBaseList.filter(
          ele => ele.value === val
        )[0].label;
      },
      save(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            saveRecruitSetInfo(this.form).then(response => {
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
  .infoTb {
    width: 100%;
    text-align: center;
    margin-top: 20px;

    th,
    td {
      padding: 8px 0;
      border-bottom: 1px solid #e9ecf2;
      width: 40%;
    }
    th {
      padding: 12px 0;
      background: #f7f7f7;
    }
    th:nth-child(3),
    td:nth-child(3) {
      width: 100px;
    }
    th:nth-child(1),
    td:nth-child(1) {
      width: 200px;
    }
    th:nth-child(4),
    td:nth-child(4) {
      width: 80px;
    }
    .noId {
      padding: 20px 0;
      color: #666;
      font-size: 12px;
    }
  }
</style>

<!--招聘管理，批量入职管理 -->
<template>
  <section class="my-basic-inner">
    <div class="stepOuter">
      <el-steps :active="active" align-center>
        <el-step title="STP 1" description="导入Excel数据"></el-step>
        <el-step title="STP 2" description="批量导入"></el-step>
      </el-steps>
    </div>
    <div class="step2">
      <el-scrollbar :native="false" class="el-scrollbar-wrap">
        <div style="padding:0 10px">
          <avue-crud ref="crud" :data="fromList" :table-loading="tableLoading" :option="tableOption">
            <template slot-scope="scope" slot="menu">
              <el-button type="text" icon="el-icon-delete" @click="deleteItem(scope.row, scope.row.$index)">删除</el-button>
            </template>
          </avue-crud>
        </div>
      </el-scrollbar>
      <div class="btns">
        <el-button plain @click.stop="$emit('back')">上一步</el-button>
        <el-button type="primary" @click="submit" icon="el-icon-check" :loading="loading">批量导入</el-button>
      </div>
    </div>
  </section>
</template>

<script>
import { postImportStaff } from '@/api/platform/basic/personnel_manage'
const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  menuWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '姓名',
      prop: 'name'
    },
    {
      label: '岗位',
      prop: 'post'
    },
    {
      label: '工号',
      prop: 'jobNumber'
    },
    {
      label: '手机号',
      prop: 'phone'
    },
    {
      label: '职层',
      prop: 'rank'
    },
    {
      label: '身份证号',
      prop: 'identity'
    },
    {
      label: '部门',
      prop: 'department'
    },
    {
      label: '入职日期',
      prop: 'entryTime'
    },
    {
      label: '派遣单位',
      prop: 'dispatch'
    }
  ]
}
export default {
  data() {
    return {
      loading: false,
      active: 1,
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      fromList: []
    };
  },
  props: {
    dataList: Array
  },
  watch: {
    dataList() {
      this.fromList = [].concat([], this.dataList)
    }
  },
  mounted() {
    this.fromList = [].concat([], this.dataList)
  },
  methods: {
    /**
     * 提交
     */
    async submit() {
      // console.log(this.fromList)
      // return
      await this.$confirm('确认批量导入', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      this.loading = true
      try{
        const res = await postImportStaff(this.fromList)
        if (res.data.code === 0) {
          this.$message({
            showClose: true,
            message: '导入成功',
            type: 'success'
          })
          this.$emit('complete')
        } else {
          this.$message({
            message: res.data.message,
            type: 'error'
          })
        }
        this.loading = false
      }catch(err){
        this.loading = false
      }
    },
    /**
     * 移除指定项
     */
    async deleteItem(row, index) {
      await this.$confirm('是否移除此项', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      this.fromList.splice(index, 1);
    }
  }
};
</script>

<style lang="scss" scoped>
.el-scrollbar-wrap {
  height: 300px !important;
}
.stepOuter {
  color: #ed6d00;
  width: 400px;
  margin: 0 auto;
  padding: 0;
}
.btns {
  text-align: center;
  padding-top: 20px;
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

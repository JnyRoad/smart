<!--业务设置，考勤汇总提醒设置  -->
<template>
  <div class="my-basic-container parking_lot">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加配置</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,scope.$index)"
            >编辑</el-button>
          </template>
        </avue-crud>
        <el-dialog
          title="添加考勤汇总规则配置"
          class="dialog_form"
          @close="resetAddFrom('addform')"
          width="600px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addform" :model="addform" label-width="110px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addform.parkId"></parkSelect>
            </el-form-item>
           <el-form-item label="考勤确认提醒" prop="deadline">
              <div class="content1">
                <span>每月</span>
                <div>
                  <el-select v-model="addform.deadline" filterable clearable placeholder="请选择">
                    <el-option
                      v-for="(item, index) in dayOptions"
                      :key="index"
                      :label="item"
                      :value="item">
                    </el-option>
                  </el-select>
                </div>
                <span>日</span>
              </div>
            </el-form-item>
            <el-form-item label="">
              <div class="tip1">*避免出现确认提醒已发出时考勤汇总未结算，提醒日宜晚于考勤结算日。</div>
            </el-form-item>
            <el-form-item label="考勤确认截止至" prop="delayLine">
              <div class="content1">
                <div>
                  <el-select v-model="addform.delayLine" filterable clearable placeholder="请选择">
                    <el-option
                      v-for="(item, index) in dayOptions"
                      :key="index"
                      :label="item"
                      :value="item">
                    </el-option>
                  </el-select>
                </div>
                <span>天后，系统将自动确认签收</span>
              </div>
            </el-form-item>
            <el-form-item label="提醒频次">
              仅提醒一次
            </el-form-item>
            <el-form-item label="是否启用" prop="isMessage">
              <el-switch
                v-model="addform.isMessage"
                active-color="#13ce66"
                inactive-color="#ff4949"
                :active-value="1"
                :inactive-value="2" >
              </el-switch>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addform')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addform')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="编辑考勤汇总规则配置"
          class="dialog_form"
          @close="resetEditFrom('editform')"
          width="600px"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="rules" ref="editform" :model="editform" label-width="110px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="editform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="考勤确认提醒" prop="deadline">
              <div class="content1">
                <span>每月</span>
                <div>
                  <el-select v-model="editform.deadline" filterable clearable placeholder="请选择">
                    <el-option
                      v-for="(item, index) in dayOptions"
                      :key="index"
                      :label="item"
                      :value="item">
                    </el-option>
                  </el-select>
                </div>
                <span>日</span>
              </div>
            </el-form-item>
            <el-form-item label="">
              <div class="tip1">*避免出现确认提醒已发出时考勤汇总未结算，提醒日宜晚于考勤结算日。</div>
            </el-form-item>
            <el-form-item label="考勤确认截止至" prop="delayLine">
              <div class="content1">
                <div>
                  <el-select v-model="editform.delayLine" filterable clearable placeholder="请选择">
                    <el-option
                      v-for="(item, index) in dayOptions"
                      :key="index"
                      :label="item"
                      :value="item">
                    </el-option>
                  </el-select>
                </div>
                <span>天后，系统将自动确认签收</span>
              </div>
            </el-form-item>
            <el-form-item label="提醒频次">
              仅提醒一次
            </el-form-item>
            <el-form-item label="是否启用" prop="isMessage">
              <el-switch
                v-model="editform.isMessage"
                active-color="#13ce66"
                inactive-color="#ff4949"
                :active-value="1"
                :inactive-value="2">
              </el-switch>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editCancel('editform')" plain>取 消</el-button>
            <el-button type="primary" @click="editSubmit('editform')" :loading="editLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import { fetchList,editObj } from './_service'
  import { tableOption } from '@/const/crud/platform/business/attendance_config'
  import { mapGetters } from 'vuex'
  const DAY_OPTION = [
    1,2,3,4,5,
    6,7,8,9,10,
    11,12,13,14,15,
    16,17,18,19,20,
    21,22,23,24,25,
    26,27,28,
  ]
  export default {
    name: "badgeConfig",
    data() {
      return {
        dayOptions: DAY_OPTION,
        addLoading: false,
        editLoading: false,
        addFormVisible: false,
        editFormVisible: false,
        addform: {
          parkId: "",
          deadline: "",
          isMessage: "",
          delayLine:"",
          setType: 2
        },
        rules: {
          parkId: [{ required: true, message: "请选择园区", trigger: "blur" }]
        },
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 10 // 每页显示多少条
        },
        searchForm: {
          parkId: "",
          setType: 2
        },
        editform: {
          id:"",
          parkId: "",
          deadline: "",
          delayLine:"",
          isMessage: "",
          setType: 2
        },
        tableLoading: false,
        tableData: [],
        tableOption: tableOption
      };
    },
    watch: {
    },
    created() {
      this.getList(this.page, this.searchForm);
    },
    mounted: function() {},
    computed: {
      ...mapGetters(["permissions"])
    },
    methods: {
      getList(page, params) {
        this.tableLoading = true;
        fetchList(
            {
              current: page.currentPage,
              size: page.pageSize
            },params
        ).then(response => {
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
          this.tableLoading = false;
        });
        this.tableLoading = false;
      },
      handleEdit(row, index) {
        //点击编辑
        this.editFormVisible = true;
        this.editform =Object.assign({},row);
      },
      editSubmit(formName) {
        //编辑内容确定
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.editLoading = true;
            editObj(this.editform).then(dataResponse => {
              this.editFormVisible = false;
              this.editLoading = false;
              this.getList(this.page, this.searchForm);
            }).catch(() => {
                this.editLoading = false;
              });
          } else {
            return false;
          }
        });
      },
      addSubmit(formName) {
        //添加内容确定
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.addLoading = true;
            editObj(this.addform).then(dataResponse => {
              this.addFormVisible = false;
              this.addLoading = false;
              this.getList(this.page, this.searchForm);
            }).catch(() => {
              this.addLoading = false;
            });
          } else {
            this.addLoading = true;
            return false;
          }
        });
      },
      editCancel(formName) {
        this.editFormVisible = false;
        this.resetEditFrom(formName);
      },
      addCancel(formName) {
        this.addFormVisible = false;
        this.resetAddFrom(formName);
      },
      resetAddFrom(formName) {
        this.$refs[formName].resetFields();
        this.$refs[formName].clearValidate();
      },
      resetEditFrom(formName) {
        this.$refs[formName].resetFields();
        this.$refs[formName].clearValidate();
      },
      /**
       * 搜索回调
       */
      searchSubmit(form) {
        this.page.currentPage = 1;
        this.getList(this.page, form);
      },
      sizeChange(val) {
        this.page.currentPage = 1;
        this.page.pageSize = val;
        this.getList(this.page, this.searchForm);
      },
      currentChange(val) {
        this.page.currentPage = val;
        this.getList(this.page, this.searchForm);
      },
      /**
       * 清空搜索
       */
      resetFrom(formName) {
        if (this.$refs[formName] != undefined) {
          this.$refs[formName].resetFields();
          this.page.currentPage = 1;
          this.getList(this.page, this.searchForm);
        }
      }
    }
  };
</script>

<style lang="scss" scoped>
  .tip1{
    font-size: 12px;
    color: #666;
  }
  .content1{
    display: flex;
    >div{
      flex: 1;
      padding: 0 10px;
    }
  }
</style>

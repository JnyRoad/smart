<!--
- @name 组织管理
- @author yang.chuan <yang.chuan@bjtce.com>
- @date 2020-10-09
-->
<template>
  <div class="my-basic-container staff organization-manage">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <!-- 操作菜单 -->
        <div class="top-menu clear" style="min-height: 48px;">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom" plain>清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="dialogVisible = true">新增企业</el-button>
          </div>
        </div>

        <!-- 搜索条件 -->
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="组织名称" prop="buName">
            <el-input v-model="searchForm.buName" placeholder="组织名称" clearable></el-input>
          </el-form-item>
        </el-form>

        <!-- 列表 -->
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="compType">
            <span v-if="scope.row.compType==1">外包单位</span>
            <span v-if="scope.row.compType==2">派遣工</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" size="mini" @click="handleEdit(scope.row, scope.index)">编辑 </el-button>
            <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDel(scope.row, scope.index)">删除 </el-button>
          </template>
        </avue-crud>

        <!-- 新增组织 弹出框 -->
        <el-dialog :title="dialogFromData.id ? '编辑组织' : '新增组织'" :visible.sync="dialogVisible">
          <el-scrollbar style="height:400px;">
            <el-form ref="form" :rules="rules" :model="dialogFromData" label-width="120px" v-if="dialogVisible">
              <el-row>
                <el-col :span="12">
                  <el-form-item label="园区选择" prop="park">
                    <el-select v-model="dialogFromData.park" @change="changePark" placeholder="请选择园区">
                      <el-option v-for="item in parkSelectData" :key="item.id" :label="item.parkName" :value="item.id"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="组织名称" prop="orgName"> <el-input v-model="dialogFromData.orgName" placeholder="请输入组织名称"></el-input> </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="管理员用户名" prop="userName">
                    <el-input v-model="dialogFromData.userName" placeholder="请输入管理员用户名" :disabled="dialogFromData.id ? true : false"></el-input> </el-form-item>
                  <div class="user-line">
                    <input type="text" />
                    <input type="password" />
                  </div>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="管理员密码" prop="password">
                    <el-input v-model="dialogFromData.password" :placeholder="dialogFromData.id ? '留空则不修改密码' : '请输入管理员密码'" type="password"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="用户角色" prop="role">
                    <el-select v-model="dialogFromData.role" :disabled="true" placeholder="请选择角色"><el-option label="企业管理员" value="企业管理员"></el-option></el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="企业类型" prop="compType">
                    <el-select v-model="dialogFromData.compType" placeholder="请选择角色">
                      <el-option label="外包单位" :value="1"></el-option>
                      <el-option label="派遣工" :value="2"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="门禁权限" prop="deviceAuthId">
                    <el-select v-model="dialogFromData.deviceAuthId" :disabled="dialogFromData.park === ''" multiple collapse-tags placeholder="请选择门禁权限">
                      <el-option
                        v-for="item in entryList"
                        :key="item.id"
                        :label="item.authorityName"
                        :value="item.id"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-scrollbar>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" plain @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="save" :loading="orgSaveLoading">保 存</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { getDataList, delOrg, postSaveOrgXuCh, getUserParkAll, getDetails } from '@/api/platform/basic/organization_manage'
import { tableOption } from '@/const/crud/platform/basic/organization_manage'
import { mapGetters } from 'vuex'
import { validatePwd } from "@/util/password";
import { deviceAuthList } from "@/api/platform/basic/staff_info";
export default {
  data() {
    return {
      orgSaveLoading: false,
      entryList: [],
      dialogFromData: {
        id: '',
        park: '',
        orgName: '',
        userName: '',
        password: '',
        compType: '',
        role: '企业管理员',
        deviceAuthId: [],
      },
      parkSelectData: [],
      dialogVisible: false,
      searchForm: {//搜索菜单表单
        buName: ''
      },
      tableLoading: false,
      tableData: [{}, {}],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    }
  },
  mounted: function () {
    // deviceAuthList({
    //   type: 1, //1代表门禁，要和c++那边对应
    //   currentPage: 1,
    //   pageSize: 100
    // }).then(response => {
    //   if (response.data.data) {
    //     debugger
    //     this.entryList = response.data.data.records;
    //   }
    // });
    this.getList()
  },
  computed: {
    ...mapGetters(['permissions']),
    rules() {
      return {
        park: [
          { required: true, message: '请选择园区选择', trigger: 'change' }
        ],
        orgName: [
          { required: true, message: '请输入组织名称', trigger: 'blur' }
        ],
        userName: [
          { required: true, message: '请输入管理员用户名', trigger: 'blur' }
        ],
        compType: [
          { required: true, message: '请选择企业类型', trigger: 'change' }
        ],
        // 编辑时密码留空表示不修改密码，仅新增必填
        password: this.dialogFromData.id
          ? [{ validator: this.vPassword, trigger: 'blur' }]
          : [
              { required: true, message: '请输入管理员密码', trigger: 'blur' },
              { validator: this.vPassword, trigger: 'blur' }
            ],
        deviceAuthId: [
          { required: true, message: '请选择门禁权限', trigger: 'change' }
        ]
      }
    }
  },
  watch: {
    dialogVisible(value) {
      if (!value) {
        this.resetDialogFromData()
      }
      this.getParkAll()
    }
  },
  methods: {
    /**
     * 校验密码（留空由 required 规则处理，编辑留空表示不修改）
     */
    vPassword(rule, value, callback) {
      if (!value) {
        callback()
        return
      }
      let r = validatePwd(value)
      if (r[0]) {
        callback(new Error(r[1]))
      } else {
        callback()
      }
    },
    changePark() {
      deviceAuthList({
        parkId: this.dialogFromData.park,
        type: 1, //1代表门禁，要和c++那边对应
        currentPage: 1,
        size: 100
      }).then(response => {
        if (response.data.data) {
          this.entryList = response.data.data.records;
        }
      });
      this.dialogFromData.deviceAuthId = []
    },
    async getParkAll() {
      if (this.parkSelectData.length) {
        return
      }
      const res = await getUserParkAll()
      this.parkSelectData = res.data.data
    },
    resetDialogFromData() {
      this.dialogFromData = {
        id: '',
        park: '',
        orgName: '',
        userName: '',
        password: '',
        compType: '',
        role: '企业管理员',
        deviceAuthId: [],
      }
    },
    async handleEdit(row, index) {
      const res = await getDetails({ id: row.id })
      deviceAuthList({
        parkId: res.data.data.parkId,
        type: 1, //1代表门禁，要和c++那边对应
        currentPage: 1,
        size: 100
      }).then(response => {
        if (response.data.data) {
          this.entryList = response.data.data.records;
          this.dialogFromData = {
            id: res.data.data.id,
            park: res.data.data.parkId,
            orgName: res.data.data.compName,
            userName: res.data.data.userName,
            compType: res.data.data.compType,
            password: '',
            role: '企业管理员',
            deviceAuthId: res.data.data.deviceAuthId,
          }
          this.dialogVisible = true
        }
      });
    },
    async handleDel(row, index) {
      await this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res = await delOrg({
        id: row.id
      })
      if (res.data.code === 0) {
        this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
        this.getList()
      }
    },
    async save() {
      await this.$refs['form'].validate()
      this.orgSaveLoading = true
      const res = await postSaveOrgXuCh({
        compName: this.dialogFromData.orgName,
        id: this.dialogFromData.id,
        parkId: this.dialogFromData.park,
        // 编辑时密码留空表示不修改密码，不传给后端
        password: this.dialogFromData.password || undefined,
        source: '',
        userId: '',
        compType: this.dialogFromData.compType,
        userName: this.dialogFromData.userName,
        deviceAuthId: this.dialogFromData.deviceAuthId
      })
      this.orgSaveLoading = false
      if (res.data.code === 0) {
        this.$message({
          showClose: true,
          message: '保存成功',
          type: 'success'
        })
        this.dialogVisible = false
        this.getList()
      }

    },
    async getList() {
      this.tableLoading = true
      const response = await getDataList(this.searchForm, {
        current: this.page.currentPage,
        size: this.page.pageSize
      })
      this.tableData = response.data.data.records
      this.page.total = response.data.data.total
      this.tableLoading = false
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList();
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList();
    },
    resetEntry(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1;
      this.getList()
    },
    /**
     * 清空搜索
     */
    resetFrom() {
      this.$refs['searchForm'].resetFields();
      this.page.currentPage = 1;
      this.getList();
    }
  }
}
</script>

<style lang="scss" scoped>
.user-line {
  position: fixed;
  left: -9999px;
}
</style>

<template>
  <div class="my-basic-container user">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" v-if="sys_user_add" icon="el-icon-plus" @click="handleCreate">添加用户</el-button>
        </div>
        <el-row :span="24">
          <!-- <el-col :xs="24"
                  :sm="24"
                  :md="5"
                  class="user__tree">

            <avue-tree :option="treeOption"
                      :data="treeData"
                      @node-click="nodeClick"></avue-tree>
          </el-col> -->
          <el-col :span="24" class="user__main">
            <avue-crud
              :option="option"
              ref="crud"
              v-model="form"
              :page="page"
              :table-loading="listLoading"
              @search-change="handleFilter"
              @row-update="update"
              @row-save="create"
              @size-change="sizeChange"
              @current-change="currentChange"
              :before-open="handleOpenBefore"
              :data="list"
            >
              <template slot="username" slot-scope="scope">
                <span>{{ scope.row.username }}</span>
              </template>
              <template slot="role" slot-scope="scope">
                <span v-for="(role, index) in scope.row.roleList" :key="index">
                  <el-tag>{{ role.roleName }} </el-tag>&nbsp;&nbsp;
                </span>
              </template>
              <template slot="park" slot-scope="scope">
                <span v-for="(park, index) in scope.row.parkList" :key="index">
                  <el-tag>{{ park.parkName }} </el-tag>&nbsp;&nbsp;
                </span>
              </template>
              <!-- <template slot="deptId"
                        slot-scope="scope">
                {{scope.row.deptName}}
              </template> -->
              <template slot="lockFlag" slot-scope="scope">
                <el-tag>{{ scope.label }}</el-tag>
              </template>
              <template slot="menu" slot-scope="scope">
                <el-button v-if="sys_user_edit" size="small" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row, scope.index)">编辑 </el-button>
                <el-button v-if="sys_user_del" size="small" type="text" icon="el-icon-delete" @click="deletes(scope.row, scope.index)">删除 </el-button>
              </template>
              <!-- <template slot="deptIdForm"
                        slot-scope="scope">
                <avue-crud-input v-model="form.deptId"
                                type="tree"
                                placeholder="请选择所属部门"
                                :node-click="getNodeData"
                                :dic="treeDeptData"
                                :props="defaultProps"></avue-crud-input>
              </template> -->
              <template slot="passwordForm" slot-scope="scope">
                <el-input size="small"
                  :type="passwordType"
                  v-model="password"
                  auto-complete="off"
                  :placeholder="boxType === 'add' ? '请输入密码' : '留空则不修改密码'"
                >
                  <i class="el-icon-view el-input__icon" slot="suffix" @click="showPassword"></i>
                </el-input>
              </template>
              <template slot="parkForm" slot-scope="scope">
                <avue-crud-select v-model="park" multiple placeholder="请选择园区" :dic="parkOptions" :props="parkProps"></avue-crud-select>
              </template>
              <template slot="roleForm" slot-scope="scope">
                <avue-crud-select v-model="role" multiple placeholder="请选择角色" :dic="rolesOptions" :props="roleProps"></avue-crud-select>
              </template>
            </avue-crud>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { addObj, delObj, fetchList, getObj, putObj } from '@/api/admin/user'
import { deptRoleList } from '@/api/admin/role'
import { allPark } from '@/api/platform/area/park'
import { fetchDeptTree, fetchTree } from '@/api/admin/dept'
import { tableOption } from '@/const/crud/admin/user'
import { mapGetters } from 'vuex'
import { constants } from 'fs'
import { connect } from 'tls'

export default {
  name: 'table_user',
  data() {
    return {
      treeOption: {
        nodeKey: 'id',
        addBtn: false,
        menu: false,
        props: {
          label: 'name',
          value: 'id'
        }
      },
      // treeData: [],
      option: tableOption,
      // treeDeptData: [],
      checkedKeys: [],
      roleProps: {
        label: 'roleName',
        value: 'roleId'
      },
      parkProps: {
        label: 'parkName',
        value: 'id'
      },
      defaultProps: {
        label: 'name',
        value: 'id'
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条,
        isAsc: false //是否倒序
      },
      list: [],
      listLoading: true,
      role: [],
      park: [],
      form: {},
      password: '',
      parkOptions: [],
      rolesOptions: [],
      passwordType: "password",
      boxType: 'add'
    }
  },
  computed: {
    ...mapGetters(['permissions'])
  },
  watch: {
    role() {
      this.form.role = this.role
    },
    park() {
      this.form.park = this.park
    },
    password() {
      this.form.password = this.password
    }
  },
  created() {
    this.sys_user_add = this.permissions['sys_user_add']
    this.sys_user_edit = this.permissions['sys_user_edit']
    this.sys_user_del = this.permissions['sys_user_del']
    // this.init();
    this.getList(this.page)
  },
  methods: {
    // init() {
    //   fetchDeptTree().then(response => {
    //     this.treeData = response.data.data;
    //   });
    // },
    // nodeClick(data) {
    //   this.page.page = 1;
    //   this.getList(this.page, {deptId: data.id});
    // },
    showPassword() {
      this.passwordType == ''
        ? (this.passwordType = 'password')
        : (this.passwordType = '')
    },
    getList(page, params) {
      this.listLoading = true
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then((response) => {
        this.list = response.data.data.records
        this.page.total = response.data.data.total
        this.listLoading = false
      })
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.$refs['crud'].searchForm)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.$refs['crud'].searchForm)
    },
    // getNodeData(data) {
    //   deptRoleList().then(response => {
    //     this.rolesOptions = response.data.data;
    //   });
    // },
    // handleDept() {
    //   fetchTree().then(response => {
    //     this.treeDeptData = response.data.data;
    //   });
    // },
    handleFilter(param) {
      this.page.currentPage = 1
      this.getList(this.page, param)
    },
    handleCreate() {
      this.$refs.crud.rowAdd()
    },
    handleOpenBefore(show, type) {
      window.boxType = type
      this.boxType = type
      // 仅控制必填红星展示，实际校验在 validatePassWord 中按新增/编辑区分
      const passwordColumn = this.option.column.find(col => col.prop === 'password')
      if (passwordColumn) {
        passwordColumn.rules[0].required = type === 'add'
      }
      deptRoleList().then((response) => {
        this.rolesOptions = response.data.data
      })
      allPark().then((response) => {
        this.parkOptions = response.data.data
      })
      // this.handleDept();
      if (['edit', 'views'].includes(type)) {
        this.role = []
        for (var i = 0; i < this.form.roleList.length; i++) {
          this.role[i] = this.form.roleList[i].roleId
        }

        this.park = []
        for (var i = 0; i < this.form.parkList.length; i++) {
          this.park[i] = this.form.parkList[i].id
        }
        // deptRoleList().then(response => {
        //   this.rolesOptions = response.data.data;
        // });
      } else if (type === 'add') {
        this.role = []
        this.park = []
        this.password = ''
      }
      show()
    },
    handleUpdate(row, index) {
      this.$refs.crud.rowEdit(row, index)
      this.password = undefined
      this.form.password = undefined
    },
    create(row, done, loading) {
      addObj(this.form)
        .then(() => {
          this.getList(this.page)
          done()
          this.$notify({
            title: '成功',
            message: '创建成功',
            type: 'success',
            duration: 2000
          })
        })
        .catch(() => {
          loading()
        })
    },
    update(row, index, done, loading) {
      if (!this.form.password) {
        // 密码留空表示不修改密码，不传给后端
        this.form.password = undefined
      }
      putObj(this.form)
        .then(() => {
          this.getList(this.page)
          done()
          this.$notify({
            title: '成功',
            message: '修改成功',
            type: 'success',
            duration: 2000
          })
        })
        .catch(() => {
          loading()
        })
    },
    deletes(row, index) {
      this.$confirm('此操作将永久删除该用户(用户名:' + row.username + '), 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delObj(row.userId)
          .then(() => {
            this.list.splice(index, 1)
            this.$notify({
              title: '成功',
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          })
          .cache(() => {
            this.$notify({
              title: '失败',
              message: '删除失败',
              type: 'error',
              duration: 2000
            })
          })
      })
    }
  }
}
</script>
<style lang="scss">
.user {
  height: 100%;
  .user__main {
    margin-top: 20px;
  }
  &__tree {
    padding-top: 3px;
    padding-right: 20px;
  }

  &__main {
    .el-card__body {
      padding-top: 0;
    }
  }
}
</style>

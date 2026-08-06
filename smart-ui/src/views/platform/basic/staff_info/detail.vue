<!--基础信息：员工信息，详情 -->
<template>
  <div class="my-basic-container staff-info">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <!-- <div class="top-menu clear">
          <div class="top-right">
            <el-button type="primary" icon="" @click="demission">确认员工离职</el-button>
          </div>
        </div>-->
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <p class="box-orange">员工信息</p>
        <el-row>
          <el-col :span="12">
            <table class="dot-list">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="姓名"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.name}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="性别"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.sex | genderInit}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="员工号"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.badge}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="BU"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.companyName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="部门"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.departmentName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="岗位"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.jobName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="员工状态"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.status | staffStatusInit}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
        <p class="box-orange isc-card-title">ISC卡片</p>
        <div class="isc-card-toolbar">
          <el-button type="primary" size="mini" icon="el-icon-plus" @click="openCreateIscCard">新增卡片</el-button>
        </div>
        <el-table
          v-loading="iscCardLoading"
          :data="iscCards"
          border
          size="mini"
          empty-text="暂无ISC实体卡"
          class="isc-card-table">
          <el-table-column prop="cardNo" label="卡号" min-width="130"></el-table-column>
          <el-table-column prop="parkName" label="园区" min-width="120"></el-table-column>
          <el-table-column prop="dispatcherParkName" label="ISC平台" min-width="140"></el-table-column>
          <el-table-column label="同步状态" width="92">
            <template slot-scope="scope">
              <el-tag size="mini" :type="cardSyncStatusType(scope.row.syncStatus)" :title="scope.row.lastSyncRemark || ''">
                {{ cardSyncStatusText(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="维护时间" min-width="160"></el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="openEditIscCard(scope.row)">编辑</el-button>
              <el-button type="text" size="mini" @click="removeIscCard(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-scrollbar>
    <el-dialog
      :visible.sync="iscCardDialogVisible"
      :title="iscCardForm.id ? '编辑ISC卡片' : '新增ISC卡片'"
      width="420px"
      append-to-body>
      <el-form ref="iscCardForm" :model="iscCardForm" label-width="90px">
        <el-form-item label="园区">
          <el-select v-model="iscCardForm.parkId" placeholder="请选择已启用ISC卡片同步的园区" filterable clearable>
            <el-option
              v-for="item in iscParkOptions"
              :key="item.parkId"
              :label="item.parkName + ' -> ' + item.dispatcherParkName"
              :value="item.parkId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="卡号">
          <el-input v-model.trim="iscCardForm.cardNo" placeholder="请输入8-20位数字或大写字母实体卡号" maxlength="20"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="mini" @click="iscCardDialogVisible = false">取消</el-button>
        <el-button type="primary" size="mini" :loading="iscCardSaving" @click="saveIscCard">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { getAdminStaffDetail, fetchIscStaffCards, saveIscStaffCard, deleteIscStaffCard, fetchIscParkConfigs } from "@/api/platform/basic/staff_info_detail";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      iscCards: [],
      iscCardLoading: false,
      iscCardSaving: false,
      iscCardDialogVisible: false,
      iscCardForm: {
        id: null,
        staffId: null,
        parkId: null,
        cardNo: ''
      },
      iscParkOptions: [],
      personInfo: {}
    };
  },
  created() {
    this.loadStaffDetail()
  },
  computed: {
    ...mapGetters(["permissions"])
  },

  methods: {
    loadStaffDetail(){
      getAdminStaffDetail(this.$route.params.id).then(response => {
        if (!this.validatenull(response.data.data)) {
          this.personInfo = response.data.data;
          this.loadIscCards()
          this.loadIscParkOptions()
        }
      });
    },
    loadIscCards(){
      if (!this.personInfo.staffId) {
        this.iscCards = []
        return
      }
      this.iscCardLoading = true
      fetchIscStaffCards(this.personInfo.staffId).then(response => {
        this.iscCards = response.data.data || []
      }).finally(() => {
        this.iscCardLoading = false
      })
    },
    loadIscParkOptions(){
      fetchIscParkConfigs({
        current: 1,
        size: 1000
      }).then(response => {
        const records = response.data.data && response.data.data.records ? response.data.data.records : []
        this.iscParkOptions = records.filter(item => item.cardSyncEnabled === 1)
      })
    },
    openCreateIscCard(){
      this.iscCardForm = {
        id: null,
        staffId: this.personInfo.staffId,
        parkId: null,
        cardNo: ''
      }
      this.iscCardDialogVisible = true
      if (!this.iscParkOptions.length) {
        this.loadIscParkOptions()
      }
    },
    openEditIscCard(row){
      this.iscCardForm = {
        id: row.id,
        staffId: row.staffId,
        parkId: row.parkId,
        cardNo: row.cardNo
      }
      this.iscCardDialogVisible = true
      if (!this.iscParkOptions.length) {
        this.loadIscParkOptions()
      }
    },
    async saveIscCard(){
      const cardNo = (this.iscCardForm.cardNo || '').trim()
      if (!this.iscCardForm.parkId) {
        this.$message({
          message: '请选择园区',
          type: 'warning'
        })
        return
      }
      if (!cardNo) {
        this.$message({
          message: 'ISC卡号不能为空',
          type: 'warning'
        })
        return
      }
      if (cardNo && !/^[0-9A-Z]{8,20}$/.test(cardNo)) {
        this.$message({
          message: 'ISC卡号必须为8-20位数字或大写字母',
          type: 'warning'
        });
        return
      }
      if (cardNo.indexOf('999') === 0) {
        this.$message({
          message: '999开头为ISC虚拟卡号，不允许维护',
          type: 'warning'
        })
        return
      }
      this.iscCardSaving = true
      try {
        const res = await saveIscStaffCard({
          id: this.iscCardForm.id,
          staffId: this.personInfo.staffId,
          parkId: this.iscCardForm.parkId,
          cardNo
        })
        if(res.data.data){
          this.$notify({
            title: '成功',
            message: '保存ISC卡片成功',
            type: 'success'
          });
          this.iscCardDialogVisible = false
          this.loadIscCards()
        }
      } finally {
        this.iscCardSaving = false
      }
    },
    removeIscCard(row){
      this.$confirm(`确认删除ISC卡片 ${row.cardNo}？`, '提示', {
        type: 'warning'
      }).then(async () => {
        const res = await deleteIscStaffCard(row.id)
        if (res.data.data) {
          this.$notify({
            title: '成功',
            message: '删除ISC卡片成功',
            type: 'success'
          })
          this.loadIscCards()
        }
      }).catch(action => {
        if (action !== 'cancel' && action !== 'close') {
          this.$message({
            message: '删除ISC卡片失败',
            type: 'error'
          })
        }
      })
    },
    cardSyncStatusText(row) {
      if (row && row.syncStatusDesc) {
        return row.syncStatusDesc
      }
      const syncStatus = Number(row && row.syncStatus)
      if (syncStatus === 0) {
        return '待同步'
      }
      if (syncStatus === 1) {
        return '已同步'
      }
      if (syncStatus === 2) {
        return '同步失败'
      }
      if (syncStatus === 3) {
        return '本地取消'
      }
      return '未知'
    },
    cardSyncStatusType(syncStatus) {
      const normalizedStatus = Number(syncStatus)
      if (normalizedStatus === 1) {
        return 'success'
      }
      if (normalizedStatus === 2) {
        return 'danger'
      }
      if (normalizedStatus === 3) {
        return 'info'
      }
      if (normalizedStatus === 0) {
        return 'warning'
      }
      return 'info'
    },
    goBack() {
      this.$router.push({
        path: `/platform/basic/staff_info`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
    demission() {}
  }
};
</script>
<style lang="scss" scoped>
.staff-info{
  min-width: 1120px;
}
.dot-list {
  float: left;
  width: 80%;
  min-width: 320px;
  margin-right: 50px;
}
.isc-card-title {
  margin-top: 20px;
}
.isc-card-toolbar {
  margin-bottom: 10px;
  text-align: right;
}
.isc-card-table {
  width: 100%;
}
</style>

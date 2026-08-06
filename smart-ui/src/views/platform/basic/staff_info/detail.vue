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
          <el-col :span="6">
            <dl class="img-info">
              <dt class="img-outer">
                <i class="corner cn-top"></i>
                <i class="corner cn-rit"></i>
                <i class="corner cn-btm"></i>
                <i class="corner cn-lft"></i>
                <div class="img-inner">
                  <viewer>
                    <img :src="staffResult.facePic || errorImgPeaple()" :onerror="errorImgPeaple()" />
                  </viewer>
                </div>
              </dt>
              <dd class="desc-info"></dd>
            </dl>
          </el-col>
          <el-col :span="8">
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
                    <tce-label-justify label="手机号"></tce-label-justify>
                  </div>
                </td>
                <td>
                  <div class="phone-outer">
                    <template v-if="editPhone">
                      <el-input v-model="newPhone" placeholder="请输入手机号"></el-input>
                      <el-button type="text" size="mini" @click="doSave">保存 </el-button>
                      <el-button type="text" size="mini" @click="doCancel">取消 </el-button>
                    </template>
                    <template v-if="!editPhone && personInfo.phone">
                      <span class="phone">{{personInfo.phone}}</span>
                      <el-button type="text" size="mini" @click="doEdit">编辑 </el-button>
                    </template>
                  </div>
                </td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="BU"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.compName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="中心"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.depAbbr}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="部门"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.depName}}</td>
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
                    <tce-label-justify label="职层"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.jcheName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="所属园区"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.parkName}}</td>
              </tr>
            </table>
          </el-col>
          <el-col :span="10">
            <table class="dot-list dotlist2">
              <!-- <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="员工类型"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.name}}</td>
              </tr>-->
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="民族"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.nation}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="身份证地址"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.residentaddress}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="上级领导"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.reportTo}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="员工状态"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.status | staffStatusInit}}</td>
              </tr>
              <tr v-if="personInfo.status == 0">
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="离职类型"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.leaType}}</td>
              </tr>
              <tr v-if="personInfo.status == 0">
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="离职日期"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.leaDate}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="入职日期"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.createTime}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="福利层级"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.welfareLevel}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="工作邮箱"></tce-label-justify>
                  </div>
                </td>
                <td>{{personInfo.email}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="紧急联系人关系"></tce-label-justify>
                  </div>
                </td>
                <td>{{smtStaffEmergency.relation}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="紧急联系人姓名"></tce-label-justify>
                  </div>
                </td>
                <td>{{smtStaffEmergency.emergencyName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="紧急联系人手机"></tce-label-justify>
                  </div>
                </td>
                <td>{{smtStaffEmergency.telephont}}</td>
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
import { getById, editPhone, fetchIscStaffCards, saveIscStaffCard, deleteIscStaffCard, fetchIscParkConfigs } from "@/api/platform/basic/staff_info_detail";
import { mapGetters } from "vuex";
import { isMobile } from '@/util/validate'

export default {
  data() {
    return {
      newPhone: '',
      editPhone: false,
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
      personInfo: {},
      staffResult: {},
      smtStaffEmergency: {}
    };
  },
  created() {
    this.getById()
  },
  computed: {
    ...mapGetters(["permissions"])
  },

  methods: {
    getById(){
      getById(this.$route.params.id).then(response => {
        if (!this.validatenull(response.data.data)) {
          this.staffResult = response.data.data;
          this.personInfo = response.data.data.smtStaff;
          this.editPhone = false
          this.loadIscCards()
          this.loadIscParkOptions()
          if (!this.validatenull(response.data.data.smtStaffEmergency)) {
            this.smtStaffEmergency = response.data.data.smtStaffEmergency[0];
          }
        }
      });
    },
    doCancel(){
      this.editPhone = false
    },
    doEdit(){
      this.newPhone = this.personInfo.phone
      this.editPhone = true
    },
    async doSave(){
      let r = isMobile(this.newPhone)
      if (!r) {
        this.$message({
          message: `${this.newPhone} 手机号格式错误`,
          type: "warning"
        });
        return
      }
      const res = await editPhone({
        staffId: this.personInfo.id,
        newPhone: this.newPhone
      })
      if(res.data.data){
        this.$notify({
          title: '成功',
          message: '修改手机号成功',
          type: 'success'
        });
        this.getById()
      }
    },
    loadIscCards(){
      if (!this.personInfo.id) {
        this.iscCards = []
        return
      }
      this.iscCardLoading = true
      fetchIscStaffCards(this.personInfo.id).then(response => {
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
        staffId: this.personInfo.id,
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
          staffId: this.personInfo.id,
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
.phone-outer,
.card-outer {
  min-height: 20px;
}
.phone-outer ::v-deep,
.card-outer ::v-deep {
  .el-input{
    width: auto;
  }
  .el-input__inner{
    padding: 0 10px 0 0;
    height: 20px;
    line-height: 20px;
    width: 110px;
    border: none;
    border-bottom: 1px solid #eee;
    border-radius: 0;;
  }
  .el-button{
    padding: 0;
  }
  .phone,
  .card{
    width: 110px;
    padding: 0 10px 0 0;
    display: inline-block;
  }
}
.img-info {
  width: 60%;
  margin: 0 auto;
}
.dot-list {
  float: left;
  width: 80%;
  min-width: 320px;
  margin-right: 50px;
}
.dotlist2 {
  .my-dot-label{
    width: 160px;
  }
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

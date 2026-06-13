<template>
  <div class="my-basic-container">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner guard-apply-page">
        <div class="apply-toolbar">
          <el-button class="back-btn" type="primary" icon="el-icon-back" plain @click="goBack">返回列表</el-button>
          <div class="apply-status-strip">
            <div class="status-item">
              <span>已加入</span>
              <strong>{{ personSummary.totalCount }}</strong>
            </div>
            <div class="status-item">
              <span>已选</span>
              <strong>{{ personSummary.selectedCount }}</strong>
            </div>
            <div class="status-item" :class="{ warning: personSummary.blockedCount > 0 }">
              <span>需处理</span>
              <strong>{{ personSummary.blockedCount }}</strong>
            </div>
          </div>
        </div>
        <el-form
          ref="addform"
          :model="addform"
          size="mini"
          label-width="110px"
          label-position="top"
          :rules="rules"
          class="guard-apply-form"
        >
          <section class="apply-section apply-info-section">
            <div class="section-heading">
              <p class="box-orange">申请信息</p>
              <span>先确定园区、申请人和可进入区域，人员名单会基于这些条件校验权限。</span>
            </div>
            <div class="apply-info-grid">
              <el-form-item label="申请进入园区" prop="parkId">
                <parkSelect v-model="addform.parkId" :defaultSelected="true" class="ipt1"></parkSelect>
              </el-form-item>
              <el-form-item label="申请人工号" prop="applyBadge">
                <div class="staff-lookup">
                  <el-input
                    v-model="addform.applyBadge"
                    @keyup.enter.native="getStaffDetail('addform')"
                    @blur="handleApplyBadgeBlur"
                    placeholder="输入工号后自动查询"
                    clearable
                    class="ipt1"
                  >
                    <el-button
                      slot="append"
                      icon="el-icon-search"
                      :loading="staffLoading"
                      @click="getStaffDetail('addform')"
                    ></el-button>
                  </el-input>
                  <div v-if="staffInfo.id" class="staff-tags">
                    <el-tag type="warning">{{staffInfo.name}}</el-tag>
                    <el-tag type="warning">{{staffInfo.compName}}</el-tag>
                    <el-tag type="warning">{{staffInfo.depName}}</el-tag>
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="申请进入区域" prop="permitFactoryType">
                <zoneSelect v-model="addform.permitFactoryType" class="ipt1" @getItem="getZoneItem"></zoneSelect>
              </el-form-item>
            </div>
            <el-form-item label="可进区域列表" prop="areaType" class="area-form-item">
              <!-- <el-input v-model="zoneStr" :placeholder="zoneStrHolder" clearable readonly class="ipt1"></el-input> -->
              <div class="area-picker">
                <div class="area-row" v-if="addform.permitFactoryType && addform.permitFactoryType !== '22'">
                  <div class="area-label">新工厂</div>
                  <el-checkbox-group v-model="newAreaType" class="zclist">
                    <el-checkbox
                      v-for="(item, index) in newFactoryArea"
                      :label="item.code"
                      :key="index"
                      @change="areaTypeChange"
                    >{{item.desc}}</el-checkbox>
                  </el-checkbox-group>
                </div>
                <div class="area-row" v-if="addform.permitFactoryType && addform.permitFactoryType !== '21'">
                  <div class="area-label">老工厂</div>
                  <el-checkbox-group v-model="oldAreaType" class="zclist">
                    <el-checkbox
                      v-for="(item, index) in oldFactoryArea"
                      :label="item.code"
                      :key="index"
                      @change="areaTypeChange"
                    >{{item.desc}}</el-checkbox>
                  </el-checkbox-group>
                </div>
              </div>
            </el-form-item>
          </section>
          <authPersonList
            :key="newAreaType + oldAreaType + addform.parkId"
            :queryObj="addform"
            :newAreaType="newAreaType"
            :oldAreaType="oldAreaType"
            ref="authPersonList"
            @summaryChange="handlePersonSummaryChange"
          ></authPersonList>
          <div class="apply-footer">
            <div class="footer-status">
              <span v-if="personSummary.blockedCount > 0">当前有 {{ personSummary.blockedCount }} 人需要处理后再提交</span>
              <span v-else-if="personSummary.selectedCount > 0">已选 {{ personSummary.selectedCount }} 人，可继续提交申请</span>
              <span v-else>请先添加并选择申请人员</span>
            </div>
            <div class="footer-actions">
              <el-button type="primary" @click="goBack" plain>取消</el-button>
              <el-button type="primary" @click="saveSubmit(1)" :loading="saveLoading">提交申请</el-button>
              <el-button type="primary" @click="saveSubmit" plain :disabled="saveLoading">提交并继续添加</el-button>
            </div>
          </div>
        </el-form>
      </section>
    </el-scrollbar>

  </div>
</template>
<script>
import authPersonList from './components/authPersonList'
import zoneSelect from './components/zone-select'
import { getStaffDetail } from "@/api/platform/_publicService"
import { xcGuardApplyApi } from "./_service"

export default {
  components: {
    authPersonList,
    zoneSelect
  },
  data() {
    return {
      saveLoading: false,
      staffLoading: false,
      zoneStr: '',//区域列表
      zoneStrHolder: '请选择申请进入区域查询',
      personSummary: {
        totalCount: 0,
        selectedCount: 0,
        blockedCount: 0
      },
      queriedApplyBadge: '',
      staffRequestSeq: 0,
      addform: {
        permitFactoryType: '',
        permitFactoryTypeDesc: '新工厂+老工厂',
        parkId: null,
        areaType: []
      },
      staffInfo: {},
      rules: {
        parkId: [tce.helper.formRules.vempty()],
        applyBadge: [tce.helper.formRules.vempty()],
        permitFactoryType: [tce.helper.formRules.vempty()],
        areaType: [tce.helper.formRules.vempty()]
      },
      newFactoryArea: [],
      oldFactoryArea: [],
      newAreaType: [],
      oldAreaType: [],
    };
  },
  created() {
    this.getAuthForFactory()
  },
  computed: {},
  watch:{
    'addform.applyBadge'(val){
      if(!val){
        this.staffInfo = {}
        this.queriedApplyBadge = ''
      }else if(this.queriedApplyBadge && val !== this.queriedApplyBadge){
        this.staffInfo = {}
      }
    },
  },
  methods: {
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.addform) {
        return this.$refs.addform.validate()
      }
      return Promise.resolve()
    },
    /**
     * 获取可进入区域列表
     */
    // async getZoneList(){
    //   this.initPersonAuthList()
    //   let query = {
    //     parkId: this.addform.parkId,
    //     id: this.addform.areaId
    //   }
    //   const res = await xcGuardApplyApi.getZoneList(query)
    //   if(res.data.code===0){
    //     if(res.data.data){
    //       this.zoneStr = res.data.data
    //     }else{
    //       this.zoneStr = ''
    //       this.zoneStrHolder = "当前申请OA区域关联权限为空"
    //     }
    //   }
    // },
    areaTypeChange(){
      // console.log(this.addform, this.newAreaType, this.oldAreaType)
      this.addform.areaType = this.newAreaType.concat(this.oldAreaType)
    },
    handlePersonSummaryChange(summary){
      this.personSummary = Object.assign({}, this.personSummary, summary)
    },
    //获取新老厂区选项值
    async getAuthForFactory(){
      //老厂0
      const res1 = await xcGuardApplyApi.getAuthForFactory({flag: 0})
      this.oldFactoryArea = res1.data.data
      //新厂1
      const res2 = await xcGuardApplyApi.getAuthForFactory({flag: 1})
      this.newFactoryArea = res2.data.data
      // console.log('1111111111111',this.oldFactoryArea, this.newFactoryArea)
    },
    /**
     * 初始化列表数据
     */
    initPersonListAll(){
      this.$refs.authPersonList && this.$refs.authPersonList.initListAll()
    },
    /**
     * 初始化列表权限数据
     */
    initPersonAuthList(){
      this.$refs.authPersonList && this.$refs.authPersonList.initAuthList()
    },
    getZoneItem(obj){
      this.addform['permitFactoryTypeDesc'] = obj.label
      if(obj.value === '22'){ //老工厂
        this.newAreaType = []
      }else if(obj.value === '21'){ //新工厂
        this.oldAreaType = []
      }
    },
    /**
     * 提交
     */
    async saveSubmit(type){
      await this.validateForm()
      if(!(this.staffInfo && this.staffInfo.id)){
        this.$message.error('申请人信息为空，请在申请人工号输入框点击回车查询申请人信息');
        return
      }
      let list = this.$refs.authPersonList.getPersonList()
      if(!list || list.length===0){
        return
      }
      let personList = []
      let hasNoAuth = 0
      let hasErrorRemark = 0
      list.forEach(el=>{
        if(!el.authList || el.authList.length===0){
          hasNoAuth +=1
        }
        if(el.remark){
          hasErrorRemark +=1
        }
        personList.push({
          applyAuths: el.authList,
          areaName: this.addform.permitFactoryTypeDesc,
          badge: el.badge,
          id: el.id,
          staffDepId: el.staffDepId,
          staffJobId: el.staffJobId,
          staffName: el.name
        })
      })
      if(hasNoAuth>0){
        this.$message.error('所选人员信息存在权限为空的情况，请设置权限成功后再提交');
        return
      }
      if(hasErrorRemark>0){
        this.$message.error('所选人员信息存在权限设置失败的情况，请设置权限成功后再提交');
        return
      }
      let obj = {
        applyBadge: this.addform.applyBadge,
        applyDep: this.staffInfo.depId,
        areaId: this.addform.permitFactoryType,
        areaName: this.addform.permitFactoryTypeDesc,
        parkId: this.addform.parkId,
        areaType: this.addform.areaType,
        personList: personList
      }
      if(type===1){
        this.saveLoading = true
      }
      const res = await xcGuardApplyApi.doApply(obj)
      if(res.data.code===0){
        this.$message({
          message: '申请成功',
          type: 'success'
        });
        if(type===1){
          this.saveLoading = false
          this.goBack()
        }else{
          this.$router.go(0)
        }
      }
    },
    /**
     * 获取员工详情
     */
    getStaffDetail(formName) {
      this.$refs[formName].validateField("applyBadge", errMsg => {
        if (!errMsg) {
          const requestBadge = this.addform.applyBadge
          const requestSeq = ++this.staffRequestSeq
          this.staffLoading = true
          getStaffDetail(requestBadge).then(res => {
            if(requestSeq !== this.staffRequestSeq || this.addform.applyBadge !== requestBadge){
              return
            }
            let result = res.data.data
            if (!this.validatenull(result)) {
              let id = result.id
              if (!this.validatenull(id)) {
                this.queriedApplyBadge = requestBadge
                this.staffInfo = result
              } else {
                this.$notify.error({
                  title: "提示",
                  message: "暂无该工号对应员工信息，请检查工号是否正确！"
                });
              }
            }
          }).finally(() => {
            if(requestSeq === this.staffRequestSeq){
              this.staffLoading = false
            }
          });
        } else {
          return false;
        }
      });
    },
    handleApplyBadgeBlur(){
      if(this.addform.applyBadge && this.addform.applyBadge !== this.queriedApplyBadge){
        this.getStaffDetail('addform')
      }
    },
    goBack() {
      this.$router.push({
        path: `/platform/security_area/securityGuardApply`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.guard-apply-page{
  padding-bottom: 96px;
}
.apply-toolbar{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}
.back-btn{
  min-width: 112px;
}
.apply-status-strip{
  display: grid;
  grid-template-columns: repeat(3, minmax(92px, 1fr));
  gap: 1px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #ebeef5;
}
.status-item{
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
  min-height: 40px;
  padding: 0 14px;
  background: #fafbfc;
  color: #6b7280;
  transition: background-color .2s ease, color .2s ease;
  strong{
    color: #303133;
    font-size: 20px;
    font-variant-numeric: tabular-nums;
    line-height: 1;
  }
  &.warning{
    background: #fff7ed;
    color: #b45309;
    strong{
      color: #ea580c;
    }
  }
}
.guard-apply-form ::v-deep {
  .el-form-item__label{
    float: none;
    display: block;
    padding: 0 0 6px;
    color: #4b5563;
    font-weight: 600;
    line-height: 1.4;
  }
  .el-form-item__content{
    margin-left: 0 !important;
    line-height: 1.4;
  }
}
.apply-section{
  margin-bottom: 26px;
}
.section-heading{
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
  .box-orange{
    margin-bottom: 0;
    white-space: nowrap;
  }
  span{
    color: #8a9099;
    font-size: 13px;
  }
}
.apply-info-grid{
  display: grid;
  grid-template-columns: repeat(3, minmax(240px, 1fr));
  gap: 18px 24px;
  max-width: 1120px;
}
.staff-lookup{
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.staff-tags{
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  .el-tag{
    margin-right: 0;
  }
}
.area-form-item{
  max-width: 1180px;
}
.area-picker{
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #ffffff;
}
.area-row{
  display: grid;
  grid-template-columns: 112px 1fr;
  min-height: 46px;
  border-bottom: 1px solid #edf0f5;
  &:last-child{
    border-bottom: 0;
  }
}
.area-label{
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  color: #4b5563;
  font-weight: 600;
}
.el-checkbox-group{
  text-align: left;
  padding: 8px 12px 4px 14px;
}
.el-checkbox{
  margin-right: 20px;
  margin-bottom: 6px;
}
.el-checkbox+.el-checkbox{
  margin-left: 0;
}
.ipt1{
  width: 100% !important;
  max-width: 360px;
  margin-right: 0;
}
.apply-footer{
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-top: 42px;
  padding: 16px 18px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: rgba(255, 255, 255, .96);
  box-shadow: 0 18px 40px -24px rgba(31, 41, 55, .36);
}
.footer-status{
  color: #6b7280;
  font-size: 13px;
}
.footer-actions{
  display: flex;
  gap: 12px;
  white-space: nowrap;
}
.push-list {
  width: 100%;
  text-align: center;
  tr{
    .list-label {
      width: 120px;
      background: #f7f7f7;
    }
    .list-value{
      width: 300px;
    }
  }
  .label-required{
    content: '*';
    color: #f56c6c;
    margin-right: 4px;
  }
  ::v-deep .el-input__inner {
    border: none;
    text-align: center;
  }
  td {
    // width: 50%;
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
@media (max-width: 1200px){
  .apply-toolbar,
  .apply-footer,
  .staff-lookup{
    align-items: stretch;
    flex-direction: column;
  }
  .apply-info-grid{
    grid-template-columns: 1fr;
  }
  .ipt1{
    max-width: none;
  }
  .footer-actions{
    flex-wrap: wrap;
  }
}
</style>

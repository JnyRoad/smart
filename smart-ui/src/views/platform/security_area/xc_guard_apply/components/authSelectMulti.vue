<template>
  <el-dialog
    ref="dialog"
    :visible.sync="currVisible"
    width="960px"
    :show-close="true"
    :close-on-click-modal="!btnLoading"
    :close-on-press-escape="!btnLoading"
    :before-close="handleBeforeClose"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'auth-select-dialog'"
  >
    <div class="auth-dialog">
      <div class="auth-dialog__head">
        <div>
          <p class="auth-dialog__eyebrow">权限设置</p>
          <h3>选择本次申请权限</h3>
          <span>支持按权限名称或编号搜索，保存前会继续校验照片、保密区关联等规则。</span>
        </div>
        <div class="auth-dialog__count">
          <span>可选 {{ authList.length }}</span>
          <strong>已选 {{ authIds.length }}</strong>
        </div>
      </div>
      <el-transfer
        v-loading="authLoading"
        class="auth-transfer"
        filterable
        filter-placeholder="搜索权限名称或编号"
        :filter-method="filterAuth"
        :button-texts="['移除', '加入']"
        :format="transferFormat"
        :render-content="renderAuthItem"
        target-order="push"
        :titles="['区域权限列表', '已选中']"
        v-model="authIds"
        :data="authList"
      >
      </el-transfer>
    </div>
    <div slot="footer">
      <div class="auth-dialog__footer">
        <div class="auth-dialog__summary">
          <span>已选 {{ authIds.length }} 项权限</span>
          <span v-if="selectArr && selectArr.length">将应用到 {{ selectArr.length }} 人</span>
        </div>
        <div class="auth-dialog__actions">
          <el-button type="primary" plain @click="cancel" :disabled="btnLoading">取 消</el-button>
          <el-button type="primary" @click="formSumit" :loading="btnLoading">保 存</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { xcGuardApplyApi } from "../_service"
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      authLoading: false,
      currVisible: false,
      authIds: [],
      authList: [],
      areaId: '',
      transferFormat: {
        noChecked: '${total}',
        hasChecked: '${checked}/${total}'
      }
    }
  },
  props: {
    visible: Boolean,
    title: String,
    selectArr: Array,
    querObj: Object,
    showAuth: Array,
    newAreaType: Array,
    oldAreaType: Array
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        if(this.querObj.areaType.length > 0 && this.querObj.parkId){
          this.getAuthList()
        }
      }
    },
    selectArr:{
      handler(){},
      immediate: true
    },
    showAuth:{
      handler(val){
        if(val && val.length>0){
          this.authIds = val.slice()
        }else{
          this.authIds = []
        }
      },
      immediate: true
    }
  },
  methods: {
    /**
     * 提交
     */
    async formSumit() {
      // console.log(this.authIds, this.selectArr)
      if(!this.authIds || this.authIds.length===0){
        this.$message.error('请选择权限');
        return
      }
      this.btnLoading = true
      try {
        await this.getRemark()
      } finally {
        this.btnLoading = false
      }
    },
    /**
     * 获取权限列表
     */
    async getAuthList() {
      const areaType = this.newAreaType.concat(this.oldAreaType)
      if(areaType.length > 0){
        this.areaId = areaType.join(',')
      }
      let query = {
        parkId: this.querObj.parkId,
        areaId: this.areaId
      }
      this.authList = []
      this.authLoading = true
      try {
        const res = await xcGuardApplyApi.getAuthList(query)
        this.authList = (res.data.data || []).map(element => {
          return Object.assign({}, element, {
            label: element.authName,
            key: element.authId
          })
        }).sort((a, b) => String(a.label || '').localeCompare(String(b.label || ''), 'zh-CN'))
        if(this.authList.length === 0){
          this.$message.error('所选可进区域权限为空，请在业务设置中配置可进区域权限！');
        }
      } finally {
        this.authLoading = false
      }
    },
    /**
     * 获取备注
     */
    async getRemark() {
      let arr = []
      const authIds = this.authIds.slice()
      const staffs = (this.selectArr || []).slice()
      staffs.forEach(el=>{
        arr.push({
          areaId: this.areaId,
          authList: authIds,
          badge: el.badge,
          staffId: el.id
        })
      })
      const res = await xcGuardApplyApi.queryRemark(arr)
      if(res.data.code===0){
        let remarks = []
        if(res.data.data && res.data.data.length>0){
          remarks = res.data.data
        }
        let selectAuthes = []
        authIds.forEach(el=>{
          for(let i = 0; i < this.authList.length; i ++){
            if(this.authList[i].key===el){
              selectAuthes.push(this.authList[i])
              break
            }
          }
        })
        let obj = {
          staffs: staffs,
          remarks: remarks,
          authList: selectAuthes
        }
        this.$emit('refresh', obj)
        this.close(true)
      }
    },
    filterAuth(query, item) {
      const keyword = String(query || '').trim().toLowerCase()
      if(!keyword){
        return true
      }
      const label = String(item.label || '').toLowerCase()
      const key = String(item.key || '').toLowerCase()
      return label.indexOf(keyword) > -1 || key.indexOf(keyword) > -1
    },
    renderAuthItem(h, option) {
      return h('div', {
        class: 'auth-transfer-option',
        attrs: {
          title: option.label
        }
      }, [
        h('span', {
          class: 'auth-transfer-option__name'
        }, option.label),
        h('span', {
          class: 'auth-transfer-option__code'
        }, `ID ${option.key}`)
      ])
    },
    handleBeforeClose(done) {
      if(this.btnLoading){
        return
      }
      done()
    },
    cancel() {
      if(this.btnLoading){
        return
      }
      this.currVisible = false
      this.authIds = []
      this.authList = []
    },
    open() {
      this.currVisible = true
    },
    close(force) {
      if(!force && this.btnLoading){
        return
      }
      this.currVisible = false
      this.authIds = []
      this.authList = []
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
.auth-dialog{
  padding: 6px 20px 20px;
}
.auth-dialog__head{
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 18px;
  h3{
    margin: 4px 0 8px;
    color: #242933;
    font-size: 22px;
    font-weight: 700;
    line-height: 1.25;
  }
  span{
    color: #8a9099;
    font-size: 13px;
  }
}
.auth-dialog__eyebrow{
  margin: 0;
  color: #ea580c;
  font-size: 12px;
  font-weight: 700;
}
.auth-dialog__count{
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 168px;
  padding: 10px 14px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafbfc;
  color: #6b7280;
  strong{
    color: #242933;
    font-size: 18px;
    font-variant-numeric: tabular-nums;
  }
}
.auth-dialog__footer{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.auth-dialog__summary{
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #8a9099;
  font-size: 13px;
}
.auth-dialog__actions{
  display: flex;
  gap: 12px;
}
.auth-dialog ::v-deep {
  .auth-transfer{
    display: flex;
    justify-content: space-between;
    align-items: stretch;
    gap: 28px;
    min-height: 440px;
    .el-transfer-panel {
      width: 360px !important;
      border-color: #e5e7eb;
      border-radius: 6px;
      box-shadow: 0 18px 36px -30px rgba(31, 41, 55, .3);
    }
    .el-transfer-panel__header{
      height: 52px;
      background: #f8fafc;
      border-bottom-color: #e5e7eb;
      .el-checkbox{
        line-height: 52px;
      }
      .el-checkbox__label{
        color: #242933;
        font-size: 16px;
        font-weight: 700;
      }
      span{
        color: #8a9099;
        font-size: 13px;
      }
    }
    .el-transfer-panel__filter{
      margin: 12px;
      .el-input__inner{
        height: 34px;
        border-radius: 6px;
        background: #ffffff;
      }
    }
    .el-transfer-panel__body{
      height: 360px;
    }
    .el-transfer-panel__list{
      height: 302px;
      padding: 4px 0;
    }
    .el-transfer-panel__item{
      display: flex;
      align-items: center;
      height: 38px;
      padding-left: 16px;
      transition: background-color .2s ease;
      .el-checkbox__label{
        width: calc(100% - 28px);
        padding-left: 12px;
      }
      &:hover{
        background: #fff7ed;
      }
    }
    .el-transfer-panel__empty{
      color: #a0a7b2;
      line-height: 260px;
    }
    .el-transfer__buttons{
      display: flex;
      flex-direction: column-reverse;
      justify-content: center;
      gap: 12px;
      padding: 0;
    }
    .el-transfer__button{
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 72px;
      height: 36px;
      margin: 0;
      border-radius: 18px;
      transition: transform .2s cubic-bezier(.16, 1, .3, 1), background-color .2s ease;
      &:active{
        transform: translateY(1px) scale(.98);
      }
      span{
        display: inline-flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
  .auth-transfer-option{
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-width: 0;
    width: 100%;
    gap: 10px;
  }
  .auth-transfer-option__name{
    overflow: hidden;
    color: #4b5563;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .auth-transfer-option__code{
    flex: 0 0 auto;
    color: #a0a7b2;
    font-size: 12px;
    font-variant-numeric: tabular-nums;
  }
}
::v-deep .auth-select-dialog{
  border-radius: 6px;
  .el-dialog__header{
    padding: 0;
  }
  .el-dialog__headerbtn{
    top: 18px;
    right: 18px;
  }
  .el-dialog__body{
    padding: 26px 40px 16px;
  }
  .el-dialog__footer{
    padding: 18px 40px 24px;
    border-top: 1px solid #ebeef5;
  }
}
@media (max-width: 1100px){
  .auth-dialog{
    padding: 0;
  }
  .auth-dialog__head,
  .auth-dialog__footer{
    align-items: stretch;
    flex-direction: column;
  }
  .auth-dialog ::v-deep {
    .auth-transfer{
      flex-direction: column;
      gap: 14px;
      min-height: auto;
      .el-transfer-panel{
        width: 100% !important;
      }
      .el-transfer__buttons{
        flex-direction: row;
      }
    }
  }
}
</style>

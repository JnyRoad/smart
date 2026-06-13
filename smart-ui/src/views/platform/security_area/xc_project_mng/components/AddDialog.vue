<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="700px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <el-form :rules="rules" ref="form" class="form" :model="addform" label-width="120px">
      <!-- :defaultSelected="defaultSelected" -->
      <el-form-item label="园区" prop="parkId">
        <parkSelect v-model="addform.parkId"></parkSelect>
      </el-form-item>
      <el-form-item label="保密项目名称" prop="securityName">
        <el-input v-model="addform.securityName" placeholder="请输入" clearable></el-input>
      </el-form-item>
      <el-form-item label="保密项目代码" prop="securityCode">
        <el-input v-model="addform.securityCode" placeholder="请输入" clearable></el-input>
      </el-form-item>
      <el-form-item label="项目门禁授权" prop="authIds2">
        <div class="cks">
          <el-checkbox-group v-model="addform.authIds2">
            <el-checkbox v-for="(item, index) in authList" :label="item.id" :key="index">
              {{item.authorityName}}
              <el-button type="text" @click="ckAuth(item)" style="margin-left: 15px;">查看</el-button>
            </el-checkbox>
          </el-checkbox-group>
          <div class="noData" v-if="!authList || authList.length===0" style="height: 45px;">当前园区无权限策略内容</div>
        </div>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
    <AuthesDialog title="查看权限策略详情" :itemObj="curAuth" :parkId="addform.parkId" ref="AuthesDialog"/>

  </el-dialog>
</template>

<script>
import { xcProjectApi } from '../_service'
import { getAuthList } from "@/api/platform/area/park-set"
import AuthesDialog from './authes'

export default {
  mixins: [tce.mixins.executeOnce],
  components: {
    AuthesDialog
  },
  data() {
    return {
      defaultSelected: true,
      btnLoading: false,
      currVisible: false,
      addform: {
        parkId: '',
        securityName: '',
        securityCode: '',
        authIds2:[]
      },
      rules: {
        parkId: [tce.helper.formRules.vempty()],
        securityCode: [tce.helper.formRules.vempty()],
        securityName: [tce.helper.formRules.vempty()],
        authIds2: [
          tce.helper.formRules.vlimitArray({trigger: 'change',min:1,message:'请选择权限'})
        ]
      },
      authList: [],
      curAuth: {}
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object
  },
  created() {
  },
  watch: {

    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        if(this.itemObj && this.itemObj.id){
          this.defaultSelected = false

          this.addform = Object.assign({},this.itemObj)
          let arr = []
          this.$nextTick(()=>{
            if(this.addform.authLists && this.addform.authLists.length>0){
              this.addform.authLists.forEach(el=>{
                arr.push(el.authId)
              })
            }
            this.$set(this.addform, 'authIds2', arr)
          })
        }else{
          this.defaultSelected = true
          // this.addform = {}
        }
        this.doClearValidate('form')
      }
    },
    itemObj:{
      handler(){},
      immediate: true
    },
    'addform.parkId'(val) {
      if(val){
        this.getAuthList()
      }
    },
  },
  methods: {
    /**
     * 查看权限
     */
    ckAuth(item){
      this.curAuth = item
      this.$refs.AuthesDialog && this.$refs.AuthesDialog.open()
    },
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    /**
     * 提交
     */
    async formSumit() {
      if(this.itemObj && this.itemObj.id){
        this.editSubmit()
      }else{
        this.addSubmit()
      }
    },
    getAuthIds(){
      this.addform.authIds = []
      this.addform.authIds2.forEach(el=>{
        let arr = this.authList.filter(el2=>{
          return el2.id===el
        })
        if(arr && arr.length>0){
          let obj2 = {
            authId: arr[0].id,
            authName: arr[0].authorityName
          }
          this.addform.authIds.push(obj2)
        }
      })
    },
    /**
     * 添加
     */
    async addSubmit(){
      this.getAuthIds()
      await this.validateForm()
      await this.executeOnceSubmit({
        promise: xcProjectApi.addObjProject(this.addform)
      })
      this.refresh()
    },
    /**
     * 编辑
     */
    async editSubmit(){
      this.getAuthIds()
      await this.validateForm()
      await this.executeOnceSubmit({
        promise: xcProjectApi.editObjProject(this.addform)
      })
      this.refresh()
    },
    /**
     * 获取权限列表
     */
    async getAuthList() {
      const res = await getAuthList(1,this.addform.parkId)
      if(res.data.data && res.data.data.length>0){
        this.authList = res.data.data
      }else{
        this.authList = []
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {
        parkId: '',
        securityName: '',
        securityCode: '',
        authIds2:[]
      },
      this.authList = []
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {
        parkId: '',
        securityName: '',
        securityCode: '',
        authIds2:[]
      },
      this.authList = []
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .form{
    margin-bottom: 40px;
  }
  .cks ::v-deep {
    .el-checkbox+.el-checkbox{
      margin-left: 0;
    }
    .el-checkbox{
      margin-right: 20px;
    }
  }
</style>

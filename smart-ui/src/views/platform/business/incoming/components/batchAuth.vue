<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="800px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <div class="cks">
      <el-transfer
        filter-placeholder="请输入区域名称"
        :titles="['区域权限列表', '已选中']"
        v-model="myIds"
        :data="authLists"
      >
      </el-transfer>
      <!-- <el-checkbox-group v-model="myIds">
        <el-checkbox v-for="(item, index) in authLists" :label="item.id" :key="index">
          {{item.authorityName}}
        </el-checkbox>
      </el-checkbox-group> -->
      <!-- <div class="noData" v-if="!authLists || authLists.length===0" style="height: 45px;">当前园区无权限策略内容</div> -->
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { inComingApi } from '../_service'
export default {
  mixins: [tce.mixins.executeOnce],
  components: {
  },
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      myIds: [],
      authLists: [],
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object,
    parkId: [String, Number]
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
        if(this.parkId){
          this.getAuthList()
          this.initSelectIds()
        }
      }
    },
    itemObj:{
      handler(){},
      immediate: true
    }
  },
  methods: {
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
      this.addSubmit()
    },
    initSelectIds(){
      this.myIds = []
      if(this.itemObj.authLists && this.itemObj.authLists.length>0){
        this.itemObj.authLists.forEach(el=>{
          this.myIds.push(el.authId)
        })
      }
    },
    /**
     * 添加
     */
    async addSubmit(){
      if (this.myIds.length === 0) {
        this.itemObj.myIds = []
        this.itemObj.authList = []
        this.close()
        return
      }
      let arr = []
      this.myIds.forEach(el1=>{
        let temp = this.authLists.filter(el=>{
          return el1 === el.key
        })
        arr = arr.concat(temp)
      })
      let temp2 = []
      arr.forEach(el=>{
        temp2.push({
	authId: el.key,
				    authName: el.label,
				    authType: el.type,
        })
      })
      this.itemObj.myIds = this.myIds
      this.itemObj.authLists = temp2
      this.close()
    },
    /**
     * 获取权限列表
     */
    async getAuthList() {
      this.authLists = []
      let list = []
      const res = await inComingApi.getAllAuthes(this.parkId)
      if(res.data.data && res.data.data.length>0){
        list = res.data.data
        list.forEach(element => {
          this.authLists.push({
            label: element.authorityName,
            key: element.id,
            type: element.type
          })
        });
        // console.log(this.authLists)
      }else{
        this.authLists = []
      }
    },
    cancel() {
      this.myIds = []
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.myIds = []
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
    min-height: 300px;
    .el-transfer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .el-transfer__buttons{
      display: contents!important;
    }
    .el-transfer__button:first-child{
      margin-bottom: 0px;
    }
    .el-transfer-panel {
      width: 320px !important;
    }
  }
  }
</style>

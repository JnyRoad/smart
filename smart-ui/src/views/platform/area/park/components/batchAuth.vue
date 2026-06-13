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
        filter-placeholder="请输入"
        :titles="['BU权限列表', '已选中']"
        v-model="myIds"
        :data="authLists"
      >
      </el-transfer>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getAuthList } from "@/api/platform/area/park-set";
export default {
  // mixins: [tce.mixins.executeOnce],
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
      this.myIds = this.itemObj.ids
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
	id: el.key,
				    name: el.label
        })
      })
      this.itemObj.securityId = temp2
      this.itemObj.ids = this.myIds
      this.close()
    },
    /**
     * 获取权限列表
     */
    async getAuthList() {
      this.authLists = []
      let list = []
      const res = await getAuthList(1, this.parkId)
      if(res.data.data && res.data.data.length>0){
        list = res.data.data
        list.forEach(element => {
          this.authLists.push({
            label: element.authorityName,
            key: element.id,
            type: element.type
          })
        });
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

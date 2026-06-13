
<!--
- @name BU
-->
<template>
  <el-dialog ref="dialog" title="BU使用范围" :visible.sync="currVisible" width="650px" @open="open" @close="close" :close-on-click-modal="false" :append-to-body="true" :custom-class="'approve-detail-dialog'">
    <div class="lock_bind">
      <div class="sdcb_cont">
        <div class="sdcb_tp">
            <el-transfer
              filterable
              :titles="['可选择BU', '已选择BU']"
              filter-placeholder="请输入BU名称"
              v-model="form.workCompList"
              :data="workCompList"
            ></el-transfer>
        </div>
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">绑 定</el-button>
    </div>
  </el-dialog>
</template>


<script>
import {
  getCompList
} from "@/api/platform/area/park-set";
import {rangeEdit, rangeData} from '../_service.js'
export default {
  data() {
    return {
      currVisible: false,
      btnLoading: false,
      workCompList:[],
      form: {
        workCompList: []
      },
    }
  },
  props: {
    tempId: [Number, String],
    parkId: [Number, String]
  },
  created() {
      this.getCompList()
  },
  watch: {
  },
  mounted: function () {},
  computed: {},
  methods: {
    getCompList(){
      // 获取可签约Bu列表
      getCompList({ compTitle: "" }).then(response => {
        let self = this;
        response.data.data.forEach((workComp, $index) => {
          self.workCompList.push({
            label: workComp.title,
            key: workComp.compid
          });
        });
      });
    },
    async formSumit() {
      const checkedData = this.form.workCompList
      const arr = []
      checkedData.forEach(element => {
        arr.push({
          parkId: this.parkId,
          tempId: this.tempId,
          type: 2,
          value: element
        })
      });
      this.btnLoading = true
      const res = await rangeEdit(arr)
      this.btnLoading = false
      if(res.data.code === 0){
        this.$notify({
          title: '成功',
          message: '绑定成功',
          type: 'success'
        });
        this.close()
      }
    },
    async getRangeData(){
      if(this.tempId){
        const res = await rangeData(this.tempId, 2)
        const d = res.data.data
        const arr = []
        d.forEach(element => {
          arr.push(parseInt(element.value))
        });
        this.form.workCompList = arr
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.getRangeData()
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.top_dv {
  padding: 0 20px;
}
.my-lit-scrollbar {
  height: 100%;
}
.el-dialog__body {
  padding: 10px 0 0 0;
}
.sdcb_cont {
  display: flex;
  margin-bottom: 10px;
}
.sdcb_tp {
  width: 100%;
  padding: 0 20px;
}
.row1 {
  margin-bottom: 15px;
  .el-tooltip {
    display: inline-block;
    margin-left: 20px;
    font-weight: bold;
  }
}
</style>
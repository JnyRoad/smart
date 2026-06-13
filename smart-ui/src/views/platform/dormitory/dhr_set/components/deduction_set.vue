
<!--
- @name BU
-->
<template>
  <el-dialog
    ref="dialog"
    title="离职水电扣费标准"
    :visible.sync="currVisible"
    width="1000px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
    :close-on-click-modal="false"
  >
    <div class="listCont" v-loading="loading">
      <div class="top-right">
        <el-button type="primary" icon="el-icon-plus" @click="addList">添加</el-button>
      </div>
      <el-table :data="tableData" height="400" border style="width: 100%">
        <el-table-column label="职员层">
          <template slot-scope="scope">
            {{ scope.row.jcheName }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="240">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" icon="el-icon-delete" @click="delData(scope.row.itemId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 水电扣费金额 -->
      <DeductionAdd ref="deductionAdd" :parkId="parkId" :tempId="tempId" :jchesOptions="jchesOptions" :itemObj="itemObj" @initData="initData" @initJchesCheck="initJchesCheck"/>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>


<script>
// deduction_add
import { templateItem, templateItemDel, getJche } from '../_service.js'
import DeductionAdd from './deduction_add'
export default {
  components: {
    DeductionAdd
  },
  data() {
    return {
      currVisible: false,
      btnLoading: false,
      tableData: [],
      itemObj: null,
      jchesOptions: [],
      loading: false
    }
  },
  props: {
    tempId: [Number, String],
    parkId: [Number, String]
  },
  created() {
  },
  watch: {
    tempId(val) {
      this.initData()
    }
  },
  mounted: function () {},
  computed: {},
  methods: {
    addList() {
        this.itemObj = null
        this.$refs.deductionAdd.open('add')
    },
    handleEdit(item){
      this.itemObj = item
      this.$refs.deductionAdd.open('update')
    },
    async initData(){
      this.loading = true
      const res = await templateItem(this.tempId)
      if(res.data.code === 0){
        const d = res.data.data
        d.forEach(element => {
          const a = []
          element.jches.forEach(el => {
            a.push(el.jcheName)
          });
          element.jcheName = a.join(',')
        });
        this.tableData = d
        this.getJcheData()
      }
    },
    async getJcheData(){
      const res = await getJche()
      this.loading = false
      const d = res.data.data
      let a = []
      this.tableData.forEach(element => {
        a = a.concat(element.jches)
      });
      const e = d.filter(item => a.some(ele => ele.jcheId === item.typeCode))
      e.forEach(element => {
        element['disabled'] = true
      });
      this.jchesOptions = d
    },
    initJchesCheck(){
      this.itemObj = null
    },
    async delData(id){
      let _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该条数据？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return templateItemDel(id);
        })
        .then(dataResponse => {
          _this.initData();
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success",
            duration: 2000
          });
        })
        .catch(err => { console.error(err) });
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
.top-right {
  text-align: right;
  padding: 6px 0;
}
</style>
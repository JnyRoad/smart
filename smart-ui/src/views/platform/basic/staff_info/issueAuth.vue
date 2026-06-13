<!--下发权限 -->
<template>
  <el-dialog
    title="下发权限"
    ref="dialog"
    :visible.sync="currVisible"
    width="1000px"
    :show-close="false"
    @open="open"
    @close="close"
    :close-on-click-modal="false"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div>可在“
          <el-button type="text" icon="" @click="goRecord">日志管理--》ISC人脸下发记录</el-button>
          ”，查看下载详情或失败原因</div>
        <div>待下发{{dataNum.waitNum}}个，已下发{{dataNum.successNum}}个，下载失败{{dataNum.failNum}}个</div>
        <avue-crud
          ref="crud"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
        >
        </avue-crud>
      </section>
    </el-scrollbar>
    <div slot="footer" style="text-align: center">
      <el-button type="primary" plain @click="close">不想等了，去操作其他功能</el-button>
    </div>
  </el-dialog>
</template>
<script>
import { issueAuth } from '@/api/platform/basic/staff_info'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      currVisible: false,
      tableLoading: false,
      tableOption: tableOption,
      tableData: [],
      dataNum: {
        waitNum: 0,
        successNum: 0,
        failNum: 0
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      time: null,
      testNum: 0
    }
  },
  props: {
    visible: Boolean,
    dataItem: String
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    dataItem(val) {
      if (this.time) {
        clearInterval(this.time)
        this.time = null
      }
      if(val){
        this.getInfo()
        this.time = setInterval(() => {
          this.getInfo()
        },10000)
      }
    }
  },
  methods: {
    getInfo() {
      this.tableLoading = true
      if(this.dataItem !== ''){
        issueAuth({taskRecordStr: this.dataItem})
          .then((response) => {
            this.tableLoading = false
            this.tableData = response.data.data
            if(this.tableData.length > 0){
              // 0初始化 3处理中 6设备离线 -> 待下发；1 -> 已下发；2失败 4已取消 5已过期 -> 失败
              const a = this.tableData.filter(item => item.status === 0 || item.status === 3 || item.status === 6)
              const b = this.tableData.filter(item => item.status === 1)
              const c = this.tableData.filter(item => item.status === 2 || item.status === 4 || item.status === 5)
              this.dataNum.waitNum = a.length
              this.dataNum.successNum = b.length
              this.dataNum.failNum = c.length
            }
          })
          .catch((err) => {
            this.tableLoading = false
          })
      }
    },
    goRecord(){
      const src = `/platform/records/person_isc`
      this.$router.push({
        path: src
      })
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.currVisible = false
      if (this.time) {
        clearInterval(this.time)
        this.time = null
      }
      this.$emit('refresh')
    }
  },
  mounted() {},
  beforeDestroy() {
    if (this.time) {
      clearInterval(this.time)
      this.time = null
    }
  }
}
const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  align: 'center',
  menu: false,
  height: 400,
  column: [
    {
      label: '工号',
      prop: 'badge'
    },
    {
      label: '姓名',
      prop: 'name'
    },
    {
      label: '设备名称',
      prop: 'deviceName'
    },
    {
      label: '区域',
      prop: 'areaName'
    },
    {
      label: '下发结果',
      prop: 'statusDesc'
    },
    {
      label: '描述',
      prop: 'remark'
    }
  ]
}
</script>

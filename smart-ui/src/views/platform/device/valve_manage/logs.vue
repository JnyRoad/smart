<!--操作记录 -->
<template>
  <div class="my-basic-container device">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          <span style="font-size: 16px;margin-left: 10px;line-height: 30px;">当前设备名称：{{valveMeterName}}</span>
        </div>
        <avue-crud ref="crud"  :data="tableData" :table-loading="tableLoading"  :option="tableOption">
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { logList } from './_service.js'
import { tableOption } from '@/const/crud/platform/device/logs'

export default {
  name: 'device',
  data() {
    return {
      valveMeterId: null,
      valveMeterName: null,
      tableLoading: false,
      tableData: [],
      tableOption: tableOption
    }
  },
  created: function () {
    this.valveMeterId = this.$route.params.id
    this.valveMeterName = this.$route.query.name
    this.getList(this.page)
  },
  mounted: function () {},
  computed: {
  },
  methods: {
    goBack() {
      const path = `/platform/device/valveManage`
      this.$router.push({
        path: path,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      })
    },
    getList(page, params) {
      this.tableLoading = true
      logList(
        Object.assign(
          {
            targetId: this.valveMeterId,
            code: 1
          }
        )
      ).then((response) => {
        this.tableData = response.data.data
        // this.page.total = response.data.data.total
        this.tableLoading = false
      })

      this.tableLoading = false
    }
  }
}
</script>
<style lang="scss" scoped>
.img-outer {
  width: 80px;
  height: 80px;
  padding-top: 0;
  background: #f7f7f7;
  .img-inner {
    background: #f7f7f7;
    top: 2px;
    right: 2px;
    bottom: 2px;
    left: 2px;
  }
}
</style>

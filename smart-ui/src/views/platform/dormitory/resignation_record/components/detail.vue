<!--宿舍报修详情 -->
<template>
  <el-dialog ref="dialog" title="详情" :visible.sync="currVisible" width="650px" @open="open" @close="close" :append-to-body="true" :custom-class="'approve-detail-dialog'">
    <el-tabs v-model="activeName">
      <el-tab-pane label="详情" name="first">
        <el-row>
          <el-col :span="24">
            <table class="lit-table" border="false" v-if="detailInfo">
              <tr>
                <td>园区</td>
                <td>{{ detailInfo.parkName }}</td>
              </tr>
              <tr>
                <td>工号</td>
                <td>{{ detailInfo.badge }}</td>
              </tr>
              <tr>
                <td>姓名</td>
                <td>{{ detailInfo.name }}</td>
              </tr>
              <tr>
                <td>BU</td>
                <td>{{ detailInfo.bu }}</td>
              </tr>
              <tr>
                <td>部门</td>
                <td>{{ detailInfo.dept }}</td>
              </tr>
              <tr>
                <td>离职日期</td>
                <td>{{ detailInfo.leaveDate }}</td>
              </tr>
              <tr>
                <td>退宿日期</td>
                <td>{{ detailInfo.quitDate }}</td>
              </tr>
              <tr>
                <td>上月抄表时间</td>
                <td>{{ detailInfo.preCollect }}</td>
              </tr>
              <tr>
                <td>最终离职天数</td>
                <td>{{ detailInfo.leaveDays }}</td>
              </tr>
              <tr>
                <td>生成记录</td>
                <td>{{ detailInfo.createTime }}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="调用日志" name="second">
        <p class="box-orange">请求方信息</p>
        <el-row>
          <el-col :span="24">
            <table class="lit-table" border="false">
              <tr>
                <td>调用方名称</td>
                <td>{{ logData.requestName || "-" }}</td>
              </tr>
              <tr>
                <td>请求IP</td>
                <td>{{ logData.requestIp || "-"}}</td>
              </tr>
              <tr>
                <td>请求时间</td>
                <td>{{ logData.requestTime || "-"}}</td>
              </tr>
              <tr>
                <td>请求报文</td>
                <td>{{ logData.requestLog || "-"}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
        <p class="box-orange">响应方信息</p>
        <el-row>
          <el-col :span="24">
            <table class="lit-table" border="false">
              <tr>
                <td>响应状态</td>
                <td>{{ logData.responseStatus || "-"}}</td>
              </tr>
              <tr>
                <td>响应描述</td>
                <td>{{ logData.responseDesc || "-"}}</td>
              </tr>
              <tr>
                <td>响应时间</td>
                <td>{{ logData.responseTime || "-"}}</td>
              </tr>
              <tr>
                <td>响应报文</td>
                <td>{{ logData.responseLog || "-"}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>
<script>
import { getLog } from '../_service.js'
export default {
  data() {
    return {
      currVisible: false,
      activeName: 'first',
      logData: {
        requestName: null,
        requestIp: null,
        requestTime: null,
        requestLog: null,
        responseStatus: null,
        responseDesc: null,
        responseLog: null,
        responseTime: null
      }
    }
  },
  props: {
    detailInfo: Object
  },
  watch: {
    detailInfo(val) {
    }
  },
  created() {},
  computed: {},
  methods: {
    async getLog() {
      const res = await getLog(this.detailInfo.id)
      if(res.data.data){
        this.logData = res.data.data
      }
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      if(this.detailInfo.id){
        this.getLog()
      }
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
.noPading {
  padding: 12px 20px;
}
.img-info {
  width: 100px;
  margin: 0 20px 0 0;
  float: left;
  .img-inner {
    top: 5px;
    right: 5px;
    bottom: 5px;
    left: 5px;
  }
}
.lit-table {
  width: 550px;
  float: center;
}
.lit-table td:first-child {
  width: 140px;
}
.form-outer {
  max-width: 400px;
  margin-left: 30px;
}
.desc-info {
  color: #666;
  font-size: 12px;
}
.avatar-uploader-icon {
  width: 100%;
  height: 100%;
  font-size: 12px !important;
  font-style: normal;
  text-align: center;
  color: #999;
  // padding-top: 50%;
  display: inline-block;
  background-position: center;
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
</style>

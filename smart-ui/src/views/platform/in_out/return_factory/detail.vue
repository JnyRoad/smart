<template>
  <div class="my-basic-container visitor-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner" v-loading="loading" element-loading-text="正在查询，请稍等！">
        <el-col :lg="24" :md="24" class="box-outer">
          <div class="top-menu" style="margin-bottom:20px;">
            <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          </div>
          <div class="box-top-header">
            <span>申请时间：{{ visitorInfo.createTime }}</span>
            <span>确认状态：{{ visitorInfo.statusName }}</span>
          </div>
          <div v-if="visitorInfo.applyMain">
            <table class="my-table">
              <tr>
                <td class="label">申请人</td>
                <td>{{ visitorInfo.applyMain.sqr || '-' }}</td>
                <td class="label">放行去处</td>
                <td>{{ visitorInfo.applyMain.fxqcDesc || '-' }}</td>
                <td class="label">是否返厂</td>
                <td>{{ visitorInfo.applyMain.sffcDesc || '-' }}</td>
              </tr>
              <tr>
                <td class="label">申请部门</td>
                <td>{{ visitorInfo.applyMain.sqbm || '-' }}</td>
                <td class="label">出发地点</td>
                <td>{{ visitorInfo.applyMain.fxddDesc || '-' }}</td>
                <!-- <td class="label">是否拍照</td> -->
                <td colspan="3">{{ visitorInfo.applyMain.fxddxq || '-' }}</td>
              </tr>
              <tr>
                <td class="label">放行人级别</td>
                <td>{{ visitorInfo.applyMain.sqrjbDesc || '-' }}</td>
                <td class="label">到达地点</td>
                <td>{{ visitorInfo.applyMain.ddddDesc || '-' }}</td>
                <td colspan="3">{{ visitorInfo.applyMain.ddddxq || '-' }}</td>
              </tr>
              <tr>
                <td class="label">放行事项</td>
                <td>{{ visitorInfo.applyMain.fxsxDesc || '-' }}</td>
                <td class="label">物品放行类别</td>
                <td>{{ visitorInfo.applyMain.wpfxlbDesc || '-' }}</td>
                <td class="label">注解</td>
                <td>{{ visitorInfo.remark || '-' }}</td>
              </tr>
              <tr>
                <td class="label">附件</td>
                <td colspan="7">
                  <viewer v-if="visitorInfo.applyMain.fjsc"
                    ><img class="el-image" style="width: 50px; height: 50px; object-fit: contain" :src="visitorInfo.applyMain.fjsc"
                  /></viewer>
                </td>
              </tr>
            </table>
          </div>
          <div v-if="visitorInfo.personDetailList && visitorInfo.personDetailList.length > 0">
            <p class="box-orange">人员放行</p>
            <table class="my-table2">
              <tr class="header">
                <td>序号</td>
                <td>姓名</td>
                <td>工号</td>
                <td>离场事由</td>
                <td>离场日期</td>
                <td>离场时间</td>
                <td>返厂日期</td>
                <td>返厂时间</td>
              </tr>
              <tr v-for="(item, index) in visitorInfo.personDetailList" :key="index">
                <td>{{ index + 1 }}</td>
                <td>{{ item.xm || '-' }}</td>
                <td>{{ item.gh || '-' }}</td>
                <td>{{ item.lcsy || '-' }}</td>
                <td>{{ item.lcrq || '-' }}</td>
                <td>{{ item.lcsj || '-' }}</td>
                <td>{{ item.fcrq || '-' }}</td>
                <td>{{ item.fcsj || '-' }}</td>
              </tr>
            </table>
          </div>
          <div v-if="visitorInfo.thingDetailList && visitorInfo.thingDetailList.length > 0">
            <p class="box-orange">物品放行</p>
            <table class="my-table2">
              <tr class="header">
                <td>序号</td>
                <td>资产编号</td>
                <td>名称</td>
                <td>单位</td>
                <td>数量</td>
                <td>接收单位</td>
                <td>放行日期</td>
                <td>备注(原因)</td>
                <td>返厂日期</td>
                <td>返厂时间</td>
                <td>运输方式</td>
                <td>姓名</td>
                <td>车牌号</td>
              </tr>
              <template>
                <tr v-for="(item, index) in visitorInfo.thingDetailList" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ item.wpbm || '-' }}</td>
                  <td>{{ item.wpmc || '-' }}</td>
                  <td>{{ item.wpdw || '-' }}</td>
                  <td>{{ item.wpsl || '-' }}</td>
                  <td>{{ item.jsdw || '-' }}</td>
                  <td>{{ item.fxrq || '-' }}</td>
                  <td>{{ item.bz || '-' }}</td>
                  <td>{{ item.wpfcrq || '-' }}</td>
                  <td>{{ item.wpfcsj || '-' }}</td>
                  <td>{{ item.ysfsDesc || '-' }}</td>
                  <td>{{ item.xm || '-' }}</td>
                  <td>{{ item.cph || '-' }}</td>
                </tr>
              </template>
            </table>
          </div>
          <p class="box-orange">放行信息</p>
          <table class="lit-table">
            <tr>
              <td>状态</td>
              <td>{{ visitorInfo.statusName || '-' }}</td>
            </tr>
            <tr v-if="visitorInfo.status === 5">
              <td>拒绝原因</td>
              <td>{{ visitorInfo.remark }}</td>
            </tr>
            <tr>
              <td>放行人员</td>
              <td>{{ visitorInfo.securityStaff || '-' }}</td>
            </tr>
            <tr>
              <td>离厂时间</td>
              <td>{{ visitorInfo.departureTime || '-' }}</td>
            </tr>
          </table>
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>

<script>
import { returnApi } from './_service'
export default {
  data() {
    return {
      id: null,
      loading: false,
      btnLoading: false,
      visitorInfo: {},
    }
  },
  created() {
  },
  mounted: function () {
    this.loading = true
    returnApi.getDetail(this.$route.params.id).then((response) => {
      this.visitorInfo = response.data.data
      if(this.visitorInfo.applyMain.fjsc){
        this.visitorInfo.applyMain.fjsc = `platform/image/view/${this.visitorInfo.applyMain.fjsc}`
      }
      const thingDetailList = this.visitorInfo.thingDetailList
      if(thingDetailList !==null){
        this.visitorInfo.thingDetailList.forEach(element => {
          switch(element.ysfs){
            case '0':
              element.ysfsDesc = '人工'
              break;
            case '1':
              element.ysfsDesc = '货车'
              break;
            case '2':
              element.ysfsDesc = '叉车'
              break;
            case '3':
              element.ysfsDesc = '三轮'
              break;
          }
        });
      }
      // /platform/image/view
      this.loading = false
    })
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    // apply() {
    //   this.btnLoading = true
    //   returnApi.confirm({ releaseId: this.$route.params.id }).then((response) => {
    //     console.log(response)
    //     this.loading = false
    //     if (response.data.code === 0) {
    //       this.$notify({
    //         title: '成功',
    //         message: '确认返厂成功',
    //         type: 'success'
    //       })
    //       this.goBack()
    //     } else {
    //       this.$notify.error({
    //         title: '失败',
    //         message: response.data.msg
    //       })
    //     }
    //   })
    // }
  }
}
</script>

<style lang="scss">
@use "@/styles/platform/visitor/visitor_detail" as *;
</style>
<style lang="scss" scoped>
.box-top-header {
  margin-bottom: 20px;
  span {
    margin-right: 80px;
  }
}
.my-table {
  width: 100%;
  margin-bottom: 30px;
  td {
    padding: 10px 15px;
    border: 1px solid #e0e0e0;
  }
  .label {
    background: #e7f3fc;
  }
}
.my-table2 {
  width: 100%;
  margin-bottom: 30px;
  text-align: center;
  td {
    padding: 10px 15px;
    border: 1px solid #e0e0e0;
  }
  .header {
    background: #e7f3fc;
    text-align: center;
  }
}
.lit-table{
  width: 50%;
  line-height: 25px;
  border: 1px solid #e0e0e0;
  margin: 0
;
;
}
.btns {
  text-align: center;
}
</style>

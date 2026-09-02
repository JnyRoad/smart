<!--访客预约，预约记录详情  -->
<template>
  <div class="my-basic-container visitor-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner" v-loading="loading" element-loading-text="正在查询，请稍等！">
        <el-col :lg="17" :md="16" class="box-outer box-left">
          <div class="top-menu" style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
            <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
            <div>预约码：{{ visitorInfo.smsCode || '-'}}</div>
          </div>
          <div>
            <table class="my-table">
              <tr>
                <td class="label">申请人</td>
                <td>{{ visitorInfo.visitorName || '-' }}</td>
                <td class="label">申请部门</td>
                <td>{{ visitorInfo.receptionistDept || '-' }}</td>
                <td class="label">来访单位</td>
                <td colspan="3">{{ visitorInfo.company || '-' }}</td>
              </tr>
              <tr>
                <td class="label">申请时间</td>
                <td>{{ visitorInfo.createTime || '-' }}</td>
                <td class="label">来访事由</td>
                <td>{{ visitorInfo.causeDesc || '-' }}</td>
                <td class="label">来访类别</td>
                <td>{{ visitorInfo.visitCauseType || '-' }}</td>
                <td class="label">是否拍照</td>
                <td>{{ visitorInfo.isPhoto || '-' }}</td>
              </tr>
              <tr>
                <td class="label">携带物品</td>
                <td>{{ visitorInfo.thingDesc || '-' }}</td>
                <td class="label">来访种类</td>
                <td>{{ visitorInfo.personTypeDesc || '-' }}</td>
                <td class="label">区域接待人</td>
                <td>{{ visitorInfo.receptionistName || '-' }}</td>
                <td class="label">状态</td>
                <td>
                  <div class="status-bg" v-if="visitorInfo.status != undefined">
                    <span class="status-text">{{ visitorInfo.status | visitorStatusFormat }}</span>
                  </div>
                  <!-- {{ visitorInfo.status || '-'}} -->
                </td>
              </tr>
              <tr>
                <td class="label">授权进入区域类别</td>
                <td colspan="7">{{ visitorInfo.permitFactoryTypeDesc || '-' }}</td>
              </tr>
              <tr v-if="newAreaType !== ''">
                <td class="label">新工厂</td>
                <td colspan="7">{{ newAreaType || '-' }}</td>
              </tr>
              <tr v-if="oldAreaType !== ''">
                <td class="label">老工厂</td>
                <td colspan="7">{{ oldAreaType || '-' }}</td>
              </tr>
            </table>
          </div>
          <div v-if="visitorInfo.cause<13 || visitorInfo.cause>16">
            <p class="box-orange">短期来访</p>
            <table class="my-table2">
              <tr class="header">
                <td rowspan="2">姓名</td>
                <td rowspan="2">性别</td>
                <!-- <td rowspan="2">证件类型</td> -->
                <td rowspan="2">证件号码</td>
                <td colspan="4">进入时间段</td>
                <td rowspan="2">访客照片</td>
              </tr>
              <tr class="header">
                <td colspan="2">进入开始时间</td>
                <td colspan="2">进入结束时间</td>
              </tr>
              <tr v-for="(item, index) in visitorInfo.fellowVisitorList" :key="index">
                <td>{{ item.fellowName || '-' }}</td>
                <td>{{ item.gender || '-' }}</td>
                <!-- <td>{{ item.certTypeDesc || '-'}}</td> -->
                <td>{{ item.certNo || '-' }}</td>
                <td>{{ visitorInfo.startDays || '-' }}</td>
                <td>{{ visitorInfo.startTimes || '-' }}</td>
                <td>{{ visitorInfo.endDays || '-' }}</td>
                <td>{{ visitorInfo.endTimes || '-' }}</td>
                <td>
                  <viewer v-if="item.fellowPhotoIdUrl"
                    ><img class="el-image" style="width: 50px; height: 50px; object-fit: contain" :src="item.fellowPhotoIdUrl" :onerror="errorImgPeaple()"
                  /></viewer>
                </td>
              </tr>
            </table>
          </div>
          <div>
            <p class="box-orange">车辆通行证办理</p>
            <table class="my-table2">
              <tr class="header">
                <td>司机姓名</td>
                <td>司机籍贯</td>
                <td>驾驶证号</td>
                <td>紧急联络人</td>
                <td>紧急联络人方式</td>
                <td>车牌号</td>
                <td>车型</td>
                <td>颜色</td>
                <td>车辆类型</td>
                <td>证件类型</td>
                <td>相关证件</td>
              </tr>
              <template v-if="visitorInfo.vehicleList && visitorInfo.vehicleList.length > 0">
                <tr v-for="(item, index) in visitorInfo.vehicleList" :key="index">
                  <td>{{ item.name || '-' }}</td>
                  <td>{{ item.nativePlace || '-' }}</td>
                  <td>{{ item.licenseNo || '-' }}</td>
                  <td>{{ item.emergencyName || '-' }}</td>
                  <td>{{ item.emergencyPhone || '-' }}</td>
                  <td>{{ item.plate || '-' }}</td>
                  <td>{{ item.modle || '-' }}</td>
                  <td>{{ item.colourDesc || '-' }}</td>
                  <td>{{ item.vehicleTypeDesc || '-' }}</td>
                  <td>{{ item.certTypeDesc || '-' }}</td>
                  <td>
                    <viewer v-if="item.certImgUrl"
                      ><img class="el-image" style="width: 50px; height: 50px; object-fit: contain" :src="item.certImgUrl" :onerror="errorImgPeaple()"
                    /></viewer>
                  </td>
                </tr>
              </template>
            </table>
          </div>
        </el-col>
        <el-col :lg="7" :md="8">
          <div class="box-outer">
            <template v-if="visitorInfo.status == 3">
              <p class="box-orange">抓拍信息</p>
              <ul class="photo-list">
                <template v-for="(item, index) in visitorInfo.snapVisitorList">
                  <li :key="index">
                    <div class="imgl">
                      <viewer>
                        <img :src="item.snapPhoto || errorImgPeapleCamera()" :onerror="errorImgPeapleCamera()" />
                      </viewer>
                    </div>
                    <p>{{ item.snapTime }}</p>
                  </li>
                </template>
              </ul>
              <div class="gray-line"></div>
            </template>
            <div v-if="visitorInfo.processList != null">
              <p class="box-orange">审批记录</p>
              <div class="step-outer">
                <el-steps direction="vertical" :active="steps">
                  <template v-for="(item, index) in visitorInfo.processList">
                    <el-step :title="item | processTitle" :description="item | processDescription" :key="index"></el-step>
                    <!-- <el-step
                      :title="item.staffBadge+'-'+item.staffName+'  '+item.staffJche?item.staffJche:'--'"
                      :description="item.statusName+'  '+item.recordDate" :key="index"
                    ></el-step>-->
                  </template>
                  <el-step title="完成审批"></el-step>
                </el-steps>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/visitor/visitor_detail" as *;
</style>

<script>
import { addBlackObj } from '@/api/platform/visitor/visitor_record_detail'
import { mapGetters } from 'vuex'
import { xcIncomingRecordApi } from './_service'

export default {
  name: 'visitor',
  data() {
    return {
      id: 0,
      loading: true,
      visitorInfo: {},
      tableLoading: false,
      tableData: {},
      fellowList: [],
      addform: {
        //添加黑名单，表单
        cardNo: '',
        personName: '',
        parkId: null
      },
      fellowOption: {
        //陌生人swiper配置
        slidesPerView: 5,
        spaceBetween: 20,
        observer: true,
        observeParents: true,
        navigation: {
          nextEl: '.fellow-button-next',
          prevEl: '.fellow-button-prev'
        }
      },
      processList: [],
      factoryList: [],
      areaNewType: [],
      areaOldType: [],
      newAreaType: '',
      oldAreaType: ''
    }
  },
  created() {},
  mounted: function () {
    // xcIncomingRecordApi.getFactoryList().then((response) => {
    //   this.factoryList = response.data.data
    // })
    xcIncomingRecordApi.getAreaType({ flag: 1 }).then((response) => {
      this.areaNewType = response.data.data
    })
    xcIncomingRecordApi.getAreaType({ flag: 0 }).then((response) => {
      this.areaOldType = response.data.data
    })
    xcIncomingRecordApi.getDetail(this.$route.params.id).then((response) => {
      this.visitorInfo = response.data.data
      const areaType = this.visitorInfo.areaType
      const newAreaTypeList = [],
        oldAreaTypeList = []
      areaType.forEach((element) => {
        const ant = this.areaNewType.filter((item) => item.code == element)
        if (ant.length > 0) {
          newAreaTypeList.push(ant[0].desc)
        }
        const aot = this.areaOldType.filter((item) => item.code == element)
        if (aot.length > 0) {
          oldAreaTypeList.push(aot[0].desc)
        }
      })
      if (newAreaTypeList.length > 0) {
        this.newAreaType = newAreaTypeList.join(',')
      }
      if (oldAreaTypeList.length > 0) {
        this.oldAreaType = oldAreaTypeList.join(',')
      }
      // console.log(this.visitorInfo);
      this.fellowList = response.data.data.fellowVisitorList
      this.processList = response.data.data.processList
      this.loading = false
    })
  },
  filters: {
    processTitle: function (item) {
      let str = item.createUser + '-' + item.nodeName
      item.staffJche = item.staffJche ? item.staffJche : ''
      str += item.staffJche
      return str
    },
    processDescription: function (item) {
      item.processDesc + '  ' + item.processDate
      let str = item.processDesc
      if (item.status != 2) {
        str += '  ' + item.processDate
      }
      return str
    }
  },
  computed: {
    ...mapGetters(['permissions']),
    hasFellowVisitor: function () {
      if (this.fellowList.length > 0) {
        return true
      } else {
        return false
      }
    },
    steps: function () {
      // status: 0已通过，1已拒绝，2待审批
      let list = this.processList
      let len = this.processList.length
      let step
      if (len == 1) {
        if (list[0].status == 0 || list[0].status == 1) {
          step = 1
        }
      }
      if (len == 2) {
        if (list[0].status == 0 || list[0].status == 1) {
          //第一个已审批
          step = 0
          if (list[1].status == 0 || list[1].status == 1) {
            step = 2
          }
        }
      }
      return step
    }
  },
  methods: {
    inBlack() {
      this.addform.cardNo = this.visitorInfo.certNo
      this.addform.personName = this.visitorInfo.visitorName
      this.addform.parkId = this.visitorInfo.parkId
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '是否确认将该访客拉入黑名单？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return addBlackObj(_this.addform)
        })
        .then((dataResponse) => {
          var msg = dataResponse.data.msg
          var dataResult = dataResponse.data.data
          if (dataResult == true) {
            _this.$notify({
              title: '成功',
              message: '成功',
              type: 'success',
              duration: 2000
            })
          } else if (dataResult === false) {
            _this.$notify({
              title: '失败',
              message: msg,
              type: 'error',
              duration: 2000
            })
          }
        })
        .catch(aa => { console.error(aa) })
    },
    /**
     * 返回来源申请列表，并恢复进入详情前的查询条件和分页。
     * 根据受限的货车来源标记选择列表路由；替换当前详情历史，避免浏览器后退再次进入详情页。
     */
    goBack() {
      const isTruckRecord = this.$route.query.recordType === 'truck'
      this.$router.replace({
        path: isTruckRecord ? '/platform/visitor/incoming_record/truck' : '/platform/visitor/incoming_record',
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm,
          ...(isTruckRecord ? { recordType: 'truck' } : {})
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.my-table {
  width: 100%;
  margin-bottom: 30px;
  td {
    padding: 10px 15px;
    border: 1px solid #e0e0e0;
  }
  .label {
    background: #f6f7fb;
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
    background: #f6f7fb;
    text-align: center;
  }
}
.step-outer ::v-deep {
  padding-left: 20px;
}
.imgl {
  min-height: 60px;
  background: #e0e0e0;
}
</style>

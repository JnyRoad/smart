<!--访客预约，预约记录详情  -->
<template>
  <div class="my-basic-container visitor-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner" v-loading="loading" element-loading-text="正在查询，请稍等！">
        <el-col :lg="17" :md="16" class="box-outer box-left">
          <div class>
            <div class="top-menu" style="margin-bottom: 20px">
              <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
              <el-button type="primary" @click="inBlack">加入黑名单</el-button>
            </div>
            <p class="box-orange">访客信息</p>
            <el-row :gutter="20">
              <el-col :span="6">
                <dl class="img-info">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <viewer>
                        <img :src="visitorInfo.visitorPhoto || errorImgPeaple()" :onerror="errorImgPeaple()" />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info"></dd>
                </dl>
              </el-col>
              <el-col :span="13">
                <table class="dot-list">
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="访客姓名"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.visitorName }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="证件类型"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.certTypeDesc }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="证件号码"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.certNo }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="手机号"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.visitorPhone }}</td>
                  </tr>
                  <tr v-if="visitorInfo.vehiclePlate !== null && visitorInfo.vehiclePlate !== ''">
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="车牌号"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.vehiclePlate }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="来访单位"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.company }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="预约来访时间"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.startTime }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="预约离开时间"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.endTime }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="来访事由"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.causeDesc }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="来访园区"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.parkName }}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="创建时间"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.createTime }}</td>
                  </tr>
                  <tr v-if="visitorInfo.status === 1">
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="拒绝理由"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{ visitorInfo.remark }}</td>
                  </tr>
                  <tr v-if="visitorInfo.tripCode || visitorInfo.healthcode">
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="疫情管控信息"></tce-label-justify>
                      </div>
                    </td>
                    <td class="idtd">
                      <div class="idouter" style="float: left; margin: 0 3% 0 0" v-if="visitorInfo.tripCode">
                        <div class="idImg">
                          <viewer>
                            <img :src="visitorInfo.tripCode || errorImgIdentity()" :onerror="errorImgIdentity()" />
                          </viewer>
                        </div>
                        <p>行程码</p>
                      </div>
                      <div class="idouter" style="float: left" v-if="visitorInfo.healthcode">
                        <div class="idImg">
                          <viewer>
                            <img :src="visitorInfo.healthcode || errorImgIdentity()" :onerror="errorImgIdentity()" />
                          </viewer>
                        </div>
                        <p>健康码</p>
                      </div>
                    </td>
                  </tr>
                  <!-- 审批时有拒绝原因，短信通知也会有拒绝原因，详情没有，到时候可以问世平 -->
                  <!-- <template v-if="visitorInfo.status==1">
                    <tr>
                      <td>
                        <div class="my-dot-label">
                          <tce-label-justify label="拒绝原因"></tce-label-justify>
                        </div>
                      </td>
                      <td>{{visitorInfo.refuseDes}}</td>
                    </tr>
                  </template> -->
                  <!-- 来访事由为 园区驻厂时才显示身份证照片 -->
                  <template v-if="visitorInfo.cause == 5 || visitorInfo.cause == 7">
                    <tr>
                      <td>
                        <div class="my-dot-label">
                          <tce-label-justify label="驻厂说明"></tce-label-justify>
                        </div>
                      </td>
                      <td>{{ visitorInfo.remark }}</td>
                    </tr>
                    <tr>
                      <td>
                        <div class="my-dot-label">
                          <tce-label-justify label="身份证照片"></tce-label-justify>
                        </div>
                      </td>
                      <td class="idtd" style="float: left; margin: 0 3% 0 0" v-if="visitorInfo.visitorFrontPhoto">
                        <div class="idouter">
                          <div class="idImg">
                            <viewer>
                              <img :src="visitorInfo.visitorFrontPhoto || errorImgIdentity()" :onerror="errorImgIdentity()" />
                            </viewer>
                          </div>
                          <p>正面照</p>
                        </div>
                        <div class="idouter" style="float: left" v-if="visitorInfo.visitorBackPhoto">
                          <div class="idImg">
                            <viewer>
                              <img :src="visitorInfo.visitorBackPhoto || errorImgIdentity()" :onerror="errorImgIdentity()" />
                            </viewer>
                          </div>
                          <p>背面照</p>
                        </div>
                      </td>
                    </tr>
                  </template>
                </table>
              </el-col>
              <el-col :span="5">
                <div class="status-bg" v-if="visitorInfo.status != undefined">
                  <span class="status-text">{{ visitorInfo.status | visitorStatusFormat }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
          <div class="gray-line"></div>
          <div class>
            <p class="box-orange">随行人员</p>
            <!-- Swiper -->
            <section class="my-swiper" v-if="hasFellowVisitor">
              <swiper :options="fellowOption" ref="fellowSwiper" class="fellowSwiper">
                <swiper-slide v-for="(item, index) of fellowList" :key="index">
                  <dl class="img-info">
                    <dt class="img-outer">
                      <i class="corner cn-top"></i>
                      <i class="corner cn-rit"></i>
                      <i class="corner cn-btm"></i>
                      <i class="corner cn-lft"></i>
                      <div class="img-inner">
                        <viewer>
                          <img :src="item.fellowPhoto" :onerror="errorImgPeaple()" />
                        </viewer>
                      </div>
                    </dt>
                  </dl>
                  <p>{{ item.fellowName }}</p>
                </swiper-slide>
              </swiper>
              <div class="fellow-button-prev swiper-button-prev" slot="button-prev">
                <el-button type="primary" icon="el-icon-arrow-left" plain></el-button>
              </div>
              <div class="fellow-button-next swiper-button-next" slot="button-next">
                <el-button type="primary" icon="el-icon-arrow-right" plain></el-button>
              </div>
            </section>
            <div class="noData" v-else><i></i>无随行人员</div>
          </div>
        </el-col>
        <el-col :lg="7" :md="8">
          <div class="box-outer">
            <p class="box-orange">被访人信息</p>
            <table class="lit-table">
              <tr>
                <td>姓名</td>
                <td>{{ visitorInfo.receptionistName }}</td>
              </tr>
              <tr>
                <td>工号</td>
                <td>{{ visitorInfo.receptionistNumber }}</td>
              </tr>
              <tr>
                <td>BU</td>
                <td>{{ visitorInfo.receptionistBU }}</td>
              </tr>
              <tr>
                <td>部门</td>
                <td>{{ visitorInfo.receptionistDept }}</td>
              </tr>
              <tr>
                <td>岗位</td>
                <td>{{ visitorInfo.receptionistJob }}</td>
              </tr>
              <tr>
                <td>职层</td>
                <td>{{ visitorInfo.receptionistJcheName }}</td>
              </tr>
              <tr>
                <td>电话</td>
                <td>{{ visitorInfo.receptionistPhone }}</td>
              </tr>
            </table>
          </div>
          <div class="box-outer box-btm">
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
import { swiper, swiperSlide } from 'vue-awesome-swiper'
import 'swiper/dist/css/swiper.css'
import { getDetails, addBlackObj } from '@/api/platform/visitor/visitor_record_detail'
import { mapGetters } from 'vuex'

export default {
  name: 'visitor',
  components: {
    swiper,
    swiperSlide
  },
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
      processList: []
    }
  },
  created() {},
  mounted: function () {
    getDetails(this.$route.params.id).then((response) => {
      if (response.data) {
        this.visitorInfo = response.data.data
        if (response.data.data.fellowVisitorList !== null) {
          this.fellowList = response.data.data.fellowVisitorList
        }
        if (response.data.data.processList !== null) {
          this.processList = response.data.data.processList
        }
      }
      this.loading = false
    })
  },
  filters: {
    processTitle: function (item) {
      let str = item.staffBadge + '-' + item.staffName + '  '
      item.staffJche = item.staffJche ? item.staffJche : ''
      str += item.staffJche
      return str
    },
    processDescription: function (item) {
      item.statusName + '  ' + item.recordDate
      let str = item.statusName
      if (item.status != 2) {
        str += '  ' + item.recordDate
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
    goBack() {
      this.$router.push({
        path: `/platform/visitor/visitor_record`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.dot-list {
  .my-dot-label {
    width: 150px;
  }
}
.step-outer ::v-deep {
  padding-left: 20px;
}
.imgl {
  min-height: 60px;
  background: #e0e0e0;
}
@media (max-width: 1440px) {
  .idImg{
    width: 100px;
  }
}
@media (min-width: 1441px) {
  .idImg{
    width: 180px;
  }
}
</style>

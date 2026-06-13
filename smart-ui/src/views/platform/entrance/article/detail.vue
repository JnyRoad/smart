<!--出入记录，车辆出入，详情  -->
<template>
  <div class="my-basic-container vehicle-detail">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom: 20px">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-row>
          <el-col :lg="8" :md="8" class="box-outer box-left" v-if="articlesType < 5">
            <div class>
              <p class="box-orange">申请信息</p>
              <table class="lit-table">
                <tr>
                  <td>所属园区</td>
                  <td>{{ articleInfo.parkName }}</td>
                </tr>
                <tr>
                  <td>BU</td>
                  <td>{{ articleInfo.compName }}</td>
                </tr>
                <tr>
                  <td>部门</td>
                  <td>{{ articleInfo.deptName }}</td>
                </tr>
                <tr>
                  <td>工号</td>
                  <td>{{ articleInfo.badge }}</td>
                </tr>
                <tr>
                  <td>申请人名</td>
                  <td>{{ articleInfo.name }}</td>
                </tr>
                <tr v-if="articleInfo.articlesType === 3">
                  <td>房间信息</td>
                  <td>{{ articleInfo.dormitoryName }}{{ articleInfo.roomName }}</td>
                </tr>
                <tr>
                  <td>携带人名</td>
                  <td>{{ articleInfo.carrier }}</td>
                </tr>
                <tr>
                  <td>车牌号</td>
                  <td>{{ articleInfo.licensePlate }}</td>
                </tr>
                <tr>
                  <td>物品类型</td>
                  <td>{{ articleInfo.articlesTypeName }}</td>
                </tr>
                <tr>
                  <td>物品描述</td>
                  <td>{{ articleInfo.articlesDesc }}</td>
                </tr>
                <tr>
                  <td>备注</td>
                  <td>{{ articleInfo.remarks }}</td>
                </tr>
                <tr>
                  <td>计划离厂时间</td>
                  <td>{{ articleInfo.plannedDepartureTime }}</td>
                </tr>
                <tr>
                  <td>提交时间</td>
                  <td>{{ articleInfo.createTime }}</td>
                </tr>
              </table>
            </div>
          </el-col>
          <el-col :lg="8" :md="8" class="box-outer box-left" v-if="articlesType === 5">
            <div class>
              <p class="box-orange">申请信息</p>
              <table class="lit-table">
                <tr>
                  <td>所属园区</td>
                  <td>{{ articleInfo.parkName }}</td>
                </tr>
                <tr>
                  <td>BU</td>
                  <td>{{ articleInfo.compName }}</td>
                </tr>
                <tr>
                  <td>部门</td>
                  <td>{{ articleInfo.deptName }}</td>
                </tr>
                <tr>
                  <td>工号</td>
                  <td>{{ articleInfo.badge }}</td>
                </tr>
                <tr>
                  <td>申请人名</td>
                  <td>{{ articleInfo.name }}</td>
                </tr>
                <tr>
                  <td>携带人名</td>
                  <td>{{ articleInfo.carrier }}</td>
                </tr>
                <tr>
                  <td>放行去处</td>
                  <td>{{ articleInfo.applyMain.fxqcDesc }}</td>
                </tr>
                <tr>
                  <td>出发地点</td>
                  <td>{{ articleInfo.applyMain.fxddDesc }}</td>
                </tr>
                <tr>
                  <td>到达地点</td>
                  <td>{{ articleInfo.applyMain.ddddDesc }}</td>
                </tr>
                <tr>
                  <td>放行事项</td>
                  <td>{{ articleInfo.applyMain.fxsxDesc }}</td>
                </tr>
                <tr>
                  <td>放行类别</td>
                  <td>{{ articleInfo.applyMain.wpfxlbDesc }}</td>
                </tr>
                <tr>
                  <td>放行人级别</td>
                  <td>{{ articleInfo.applyMain.sqrjbDesc }}</td>
                </tr>
                <tr>
                  <td>计划离厂时间</td>
                  <td>{{ articleInfo.plannedDepartureTime }}</td>
                </tr>
                <tr>
                  <td>提交时间</td>
                  <td>{{ articleInfo.createTime }}</td>
                </tr>
              </table>
            </div>
          </el-col>
          <el-col :lg="16" :md="16" class="box-outer">
            <p class="box-orange" v-if="isPerson && articlesType === 4">放行人员</p>
            <avue-crud v-if="isPerson && articlesType === 4" :data="personList" :option="option2"></avue-crud>
            <p class="box-orange" v-if="!isPerson && articlesType === 4">放行物品</p>
            <avue-crud v-if="!isPerson && articlesType === 4" :data="goodsList" :option="option1"></avue-crud>
            <p class="box-orange">图片</p>
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="wPercent">
                  <dl class="img-info">
                    <dt class="img-outer">
                      <i class="corner cn-top"></i>
                      <i class="corner cn-rit"></i>
                      <i class="corner cn-btm"></i>
                      <i class="corner cn-lft"></i>
                      <div class="img-inner">
                        <viewer>
                          <img :src="articleInfo.oneImg || errorImgGoods()" :onerror="errorImgGoods()" />
                        </viewer>
                      </div>
                    </dt>
                  </dl>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="wPercent">
                  <dl class="img-info">
                    <dt class="img-outer">
                      <i class="corner cn-top"></i>
                      <i class="corner cn-rit"></i>
                      <i class="corner cn-btm"></i>
                      <i class="corner cn-lft"></i>
                      <div class="img-inner">
                        <viewer>
                          <img :src="articleInfo.twoImg || errorImgGoods()" :onerror="errorImgGoods()" />
                        </viewer>
                      </div>
                    </dt>
                  </dl>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="wPercent">
                  <dl class="img-info">
                    <dt class="img-outer">
                      <i class="corner cn-top"></i>
                      <i class="corner cn-rit"></i>
                      <i class="corner cn-btm"></i>
                      <i class="corner cn-lft"></i>
                      <div class="img-inner">
                        <viewer>
                          <img :src="articleInfo.threeImg || errorImgGoods()" :onerror="errorImgGoods()" />
                        </viewer>
                      </div>
                    </dt>
                  </dl>
                </div>
              </el-col>
            </el-row>
          </el-col>
        </el-row>
        <el-row>
          <el-col :lg="8" :md="8" class="box-outer box-left">
            <div class>
              <p class="box-orange">审批信息</p>
              <div class="record">
                <div class="record-inner">
                  <template v-for="(item, index) in articleInfo.approvalProcess">
                    <div class="record-item" :key="index">
                      <div class="record-item__left">
                        <div class="num"></div>
                        <i class="line1"></i>
                      </div>
                      <!-- 提交节点 -->
                      <div class="record-item__right" v-if="item.recordNode === 0">
                        <div>
                          <span style="font-weight: bold">{{ item.staffInfos[0].staffName }}-</span>
                          <span class="pc_c0">{{ item.staffInfos[0].resultDesc }}</span>
                        </div>
                        <div class="line2">
                          {{ item.staffInfos[0].createDate }}
                        </div>
                      </div>
                      <!-- 审批节点 -->
                      <div class="record-item__right" v-else>
                        <div>处理人</div>
                        <div v-for="(item2, index2) in item.staffInfos" :key="index2" style="margin-top: 10px">
                          <div style="font-size: 14px">
                            {{ item2.staffName }}-
                            <!-- result：0 待审批 1 通过 2 拒绝 3关闭 4 等待 -->
                            <span v-if="item2.result === 0" class="pc_c0">{{ item2.resultDesc }}</span>
                            <span v-if="item2.result === 1" class="pc_c1">{{ item2.resultDesc }}</span>
                            <span v-if="item2.result === 2 || item2.result === 3" class="pc_c2">{{ item2.resultDesc }}</span>
                            <span v-if="item2.result === 4" class="pc_c4">{{ item2.resultDesc }}</span>
                          </div>
                          <div class="line2">
                            {{ item2.recordDate || item2.createDate }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </template>
                </div>
              </div>

              <p class="box-orange">放行信息</p>
              <table class="lit-table">
                <tr>
                  <td>状态</td>
                  <td>{{ articleInfo.statusName || '-' }}</td>
                </tr>
                <tr v-if="articleInfo.status === 5">
                  <td>拒绝原因</td>
                  <td>{{ articleInfo.remark }}</td>
                </tr>
                <tr>
                  <td>放行人员</td>
                  <td>{{ articleInfo.securityStaff || '-' }}</td>
                </tr>
                <tr>
                  <td>离厂时间</td>
                  <td>{{ articleInfo.departureTime || '-' }}</td>
                </tr>
              </table>
              <!-- status 1、待审批  2、通过  3、拒绝（审批人）  4已出厂  5拒绝（保安）-->
              <!-- <table class="lit-table">
              <tr>
                <td>审批人名</td>
                <td>{{articleInfo.approver}}</td>
              </tr>
              <tr>
                <td>审批时间</td>
                <td>{{articleInfo.approveTime}}</td>
              </tr>
              <tr>
                <td>放行人员</td>
                <td>{{articleInfo.securityStaff}}</td>
              </tr>
              <tr>
                <td>离厂时间</td>
                <td>{{articleInfo.departureTime}}</td>
              </tr>
              <tr>
                <td>状态</td>
                <td>{{articleInfo.statusName}}</td>
              </tr>
              <tr v-if="articleInfo.status===5">
                <td>拒绝原因</td>
                <td>{{articleInfo.remark}}</td>
              </tr>
            </table> -->
            </div>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { getDetails } from '@/api/platform/entrance/article_detail'
import { mapGetters } from 'vuex'

export default {
  name: 'article',
  data() {
    return {
      articleInfo: {},
      articlesType: null, //物品类型 3生活区 4办公区
      isPerson: true,
      personList: [],
      goodsList: [],
      option1: {
        size: 'mini',
        menu: false,
        column: [
          {label: '资产编码',prop: 'wpbm'},
          {label: '名称',prop: 'wpmc'},
          {label: '单位',prop: 'wpdw'},
          {label: '数量',prop: 'wpsl'},
          {label: '接收单位',prop: 'jsdw'},
          {label: '放行日期',prop: 'fxrq'},
          {label: '运输方式',prop: 'ysfsDesc'},
          {label: '姓名',prop: 'xm'},
          {label: '车牌号',prop: 'cph'},
        ]
      },
      option2: {
        size: 'mini',
        menu: false,
        column: [
          {label: '工号',prop: 'gh'},
          {label: '姓名',prop: 'xm'},
          {label: '离厂事由',prop: 'lcsy'},
          {label: '离厂日期',prop: 'lcTime'}
        ]
      }
    }
  },
  created() {
    getDetails(this.$route.params.id).then((response) => {
      this.articleInfo = response.data.data
      this.articlesType = response.data.data.articlesType
      if (this.articleInfo.applyMain.fxsx === 0 || this.articleInfo.applyMain.fxsx === 7) {
        this.personList = this.articleInfo.personDetailList
        this.personList.forEach(element => {
          element['lcTime'] = element['lcrq'] + ' ' + element['lcsj']
        });
        this.isPerson = true
      } else {
        this.goodsList = this.articleInfo.thingDetailList
        this.isPerson = false
      }
    })
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/entrance/article`,
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
.record {
  position: relative;
  padding-left: 20px;
  .pc_c0 {
    color: #508bff;
  }
  .pc_c1 {
    color: #74c288;
  }
  .pc_c2 {
    color: #f25c19;
  }
  .pc_c4 {
    color: #999;
  }
  &-item {
    display: flex;
    &__left {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding-right: 8px;
      .num {
        width: 18px;
        height: 18px;
        text-align: center;
        line-height: 18px;
        background: url('/img/p_1.png');
        background-size: 100% 100%;
      }
      .line1 {
        flex: 1;
        width: 1px;
        border-left: 1px dashed #e0e0e0;
      }
    }
    &__right {
      .line2 {
        font-size: 12px;
        color: #666;
        margin: 5px 0 15px 0;
      }
    }
  }
  .record-inner {
    .record-item:last-child {
      .record-item__left {
        .line1 {
          display: none;
        }
      }
    }
  }
}
</style>

<!--宿舍报修详情 -->
<template>
  <div class="my-basic-container vehicle">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-form
          ref="editform"
          :model="detailInfo"
          size="mini"
          label-width="80px"
        >
          <p class="box-orange">申请信息</p>
          <el-row>
            <el-col :span="14">
              <table class="lit-table" border="false" v-if="!isEdit">
                <tr>
                  <td>申请单号</td>
                  <td>{{detailInfo.applyId}}</td>
                </tr>
                <tr>
                  <td>申请时间</td>
                  <td>{{detailInfo.applyTime}}</td>
                </tr>
                <tr>
                  <td>申请状态</td>
                  <td>
                    <el-row :gutter="20">
                      <el-col :span="4">{{detailInfo.statusDesc}}</el-col>
                      <el-col :span="20">{{detailInfo.reason}}</el-col>
                    </el-row>
                  </td>
                </tr>
                <tr>
                  <td>审批人</td>
                  <td>{{detailInfo.approver}}</td>
                </tr>
                <tr>
                  <td>审批时间</td>
                  <td>{{detailInfo.approverTime}}</td>
                </tr>
              </table>
            </el-col>
          </el-row>
          <el-row>
            <div class>
              <p class="box-orange">申请名单</p>
              <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
              </avue-crud>
            </div>
          </el-row>
<!--          <div class="gray-line"></div>-->
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
  import {
    checkDetail,
    checkPage
  } from "@/api/platform/outsourcing/apply";
  import { tableOption } from '@/const/crud/platform/outsourcing/detail'
  export default {
    data() {
      return {
        isEdit: false, //是否是编辑状态
        detailInfo: {},
        tableLoading: false,
        tableData: [],
        tableOption: tableOption,
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 20 // 每页显示多少条
        }
      };
    },
    created() {
      checkDetail(this.$route.params.id).then(response => {
        this.detailInfo = response.data.data;
        this.getList(this.page)
      })
    },
    computed: {
    },
    methods: {
      goBack() {
        this.$router.push({
          path: this.$route.query.isApprove ? `/platform/outsourcing/approve` : `/platform/outsourcing/apply`,
          // query: {
          //   queryPage: this.$route.query.queryPage,
          //   queryForm: this.$route.query.queryForm
          // }
        });
      },
      getList(page) {
        this.tableLoading = true

        checkPage(
          Object.assign(
            {
              asc: 'id',
              current: page.currentPage,
              size: page.pageSize
            }
          ), this.detailInfo.applyId
        ).then((response) => {
          this.tableData = response.data.data.records
          this.page.total = response.data.data.total
          this.tableLoading = false
        })

        this.tableLoading = false
      },
      sizeChange(val) {
        this.page.currentPage = 1
        this.page.pageSize = val
        this.getList(this.page)
      },
      currentChange(val) {
        this.page.currentPage = val
        this.getList(this.page)
      },
    }
  };
</script>
<style lang="scss" scoped>
  .record {
    position: relative;
    padding-left: 20px;
    .pc_c0{
      color: #508BFF;
    }
    .pc_c1{
      color: #74C288;
    }
    .pc_c2{
      color: #F25C19;
    }
    .pc_c4{
      color: #999
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
    .record-inner{
      .record-item:last-child{
        .record-item__left {
          .line1 {
            display: none;
          }
        }
      }
    }
  }
</style>
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
    float: left;
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

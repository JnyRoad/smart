<template>
  <div class="my-basic-container">
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
          <el-row>
            <el-col :span="18">
              <p class="box-orange">工单信息</p>
              <table class="lit-table" border="false">
                <!-- <tr>
                  <td>流水号</td>
                  <td>{{detailInfo.serialNum}}</td>
                </tr> -->
                <tr>
                  <td>OA单号</td>
                  <td>{{detailInfo.processId || '-'}}</td>
                </tr>
                <tr>
                  <td>申请时间</td>
                  <td>{{detailInfo.createTime || '-'}}</td>
                </tr>
                <tr>
                  <td>OA状态</td>
                  <td>{{detailInfo.oaStatusDesc || '-'}}</td>
                </tr>
                <tr>
                  <td>申请区域</td>
                  <td>{{detailInfo.areaName || '-'}}</td>
                </tr>
                <tr v-if="newAreaType">
                  <td>新工厂</td>
                  <td>{{newAreaType || '-'}}</td>
                </tr>
                <tr v-if="oldAreaType">
                  <td>老工厂</td>
                  <td>{{oldAreaType || '-'}}</td>
                </tr>
                <tr>
                  <td>下发状态</td>
                  <td>{{detailInfo.deviceStatusDesc || '-'}}</td>
                </tr>
                <tr>
                  <td>下发总数</td>
                  <td>{{detailInfo.totalNum}}</td>
                </tr>
                 <tr>
                  <td>下发成功数量</td>
                  <td>{{detailInfo.successNum}}</td>
                </tr>
                 <tr>
                  <td>下发失败数量</td>
                  <td>{{detailInfo.failNum}}</td>
                </tr>
              </table>
            </el-col>
            <el-col :span="6">
              <div class>
                <p class="box-orange">审批信息</p>
                <div class="record">
                  <div class="record-inner">
                    <template v-for="(item, index) in detailInfo.oaFlow">
                      <div class="record-item" :key="index">
                        <div class="record-item__left">
                          <div class="num" :class="item.processDesc|flt_getClass"></div>
                          <i class="line1"></i>
                        </div>
                        <!-- 提交节点 -->
                        <div class="record-item__right">
                          <div>
                            <span style="font-weight: bold;">{{ item.createUser }}-</span>
                            <span class="">{{ item.nodeName }}</span>
                            <span class="pc_c0" :class="item.processDesc|flt_getClass2">（{{ item.processDesc }}）</span>
                          </div>
                          <div v-if="item.remark" style="padding: 10px 0;">
                            <span style="color: #999;" v-html="item.remark"></span>
                          </div>
                          <div class="line2">
                            {{ item.processDate }}
                          </div>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          <div class="gray-line"></div>
          <div class="row2">
            <p class="box-orange">人员名单</p>
            <el-button type="primary" @click="handelExport" :loading="exportLoading" icon="icon-yutong-download" :disabled="!tableData || tableData.length===0">导出下发失败excel</el-button>
          </div>
          <div class="person-list-crud">
            <avue-crud
              ref="crud"
              :data="tableData"
              :option="listOption"
            >
              <!-- 只有下发失败的时候，才可以更换照片 -->
              <!-- :disabled="scope.row.status!==2" -->
              <template slot-scope="scope" slot="menu">
                <el-button
                  type="text"
                  :disabled="scope.row.status!==2"
                  @click="handleChangeImg(scope.row,scope.$index)"
                >更换照片</el-button>
              </template>
            </avue-crud>
          </div>
        </el-form>
      </section>
    </el-scrollbar>
    <UploadImg title="更换照片" :itemObj="curObj" ref="uploadImg" @refresh="getDetail"/>

  </div>
</template>
<script>
import UploadImg from './components/upload_img'
import { xcGuardApplyApi } from "./_service"

export default {
  components: {
    UploadImg
  },
  data() {
    return {
      exportLoading: false,
      detailInfo: {},
      tableData: [],
      listOption: listOption(),
      curObj: {},
      areaNewType: [],
      areaOldType: [],
      newAreaType: null,
      oldAreaType: null
    };
  },
  created() {
    xcGuardApplyApi.getAuthForFactory({ flag: 1 }).then((response) => {
      this.areaNewType = response.data.data
    })
    xcGuardApplyApi.getAuthForFactory({ flag: 0 }).then((response) => {
      this.areaOldType = response.data.data
    })
    this.getDetail()
  },
  computed: {
  },
  filters:{
    flt_getClass(val){
      if(val==='退回'){
        return 'deny'
      }else if(val==='当前审批人'){
        return 'ing'
      }else{
        return 'done'
      }
    },
    flt_getClass2(val){
      if(val==='退回'){
        return 'pc_c2'
      }else if(val==='当前审批人'){
        return 'pc_c4'
      }
    }
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/security_area/securityGuardApply`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
    /**
     * 详情
     */
    async getDetail(){
      Promise.all([
        xcGuardApplyApi.getApplyDetail(this.$route.params.id),
        xcGuardApplyApi.getApplyDetailPerson(this.$route.params.id)
      ]).then(arr=>{
        if(arr[0].data.code===0 && arr[0].data.data){
          this.detailInfo = arr[0].data.data
          const areaType = this.detailInfo.areaType
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
        }else{
          this.detailInfo = {}
        }
        if(arr[1].data.code===0){
          this.tableData = arr[1].data.data
        }else{
          this.tableData = []
        }
      })
    },
    /**
     * 更换照片
     */
    handleChangeImg(row){
      this.curObj = row
      this.$refs.uploadImg && this.$refs.uploadImg.open()
    },
    /**
     * 导出-下发失败
     */
    handelExport() {
      // status: 下发失败
      let arr = this.tableData.filter(el=>{
        return el.status === 2
      })
      if(arr.length===0){
        this.$message({
          message: '当前列表没有下发失败的数据',
          type: 'warning'
        });
        return
      }
      require.ensure([], () => {
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "工号",
          "姓名",
          "申请进入区域",
          "下发权限",
          "下发状态",
          "失败原因"
        ];
        const filterVal = [
          "staffBadge",
          "staffName",
          "areaName",
          "auth",
          "statusDesc",
          "remark"
        ];
        const data = this.formatJson(filterVal, arr)
        export_json_to_excel(tHeader, data, "下发失败信息")
      })
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    }
  }
};
const listOption = function () {
  return {
    index: true,
    indexLabel: '序号',
    addBtn: false,
    delBtn: false,
    editBtn: false,
    viewBtn: false,
    border: false,
    refreshBtn: false,
    columnBtn: false,
    stripe: false,
    page: true,
    align: 'center',
    menuAlign: 'center',
    menu: true,
    menuWidth: 120,
    selection: false,
    tip: false,
    column: [
      {
        label: '工号',
        prop: 'staffBadge'
      },
      {
        label: '姓名',
        prop: 'staffName'
      },
      {
        label: '申请进入区域',
        prop: 'areaName'
      },
      {
        label: '下发权限',
        prop: 'auth'
      },
      {
        label: '下发状态',
        prop: 'statusDesc'
      },
      {
        label: '失败原因',
        prop: 'remark'
      }
    ]
  }
}
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
      color: #E7282D;
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
          border-radius: 50%;
          text-align: center;
          // line-height: 18px;
          // background: url('/img/p_1.png');
          // background-size: 100% 100%;
        }
        .done{
          background: #10CC8E;
        }
        .done::after{
          content: '';
          width: 4px;
          height: 8px;
          border: 1px solid #fff;
          border-top: none;
          border-left: none;
          display: inline-block;
          transform: rotate(45deg);
          vertical-align: middle;
          margin: -8px 0 0 0;
        }
        .deny{
          background: #E7282D;
        }
        .deny::after{
          content: '×';
          color: #fff;
          line-height: 25px;
          display: inline-block;
          vertical-align: middle;
          margin: -5px 0 0 0;
        }
        .ing::after{
          content: '';
          width: 10px;
          height: 10px;
          border-radius: 50%;
          background: #cec9c9;
          display: inline-block;
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
.person-list-crud ::v-deep {
  .el-card__body{
    padding: 0 !important;
  }
}
.row2{
  display: flex;
  justify-content: space-between;
  align-items: center;
  .box-orange{
    margin-bottom: 0;
  }
}
.lit-table {
  float: left;
  td:first-child {
    width: 140px;
  }
}
.form-outer {
  max-width: 400px;
  margin-left: 30px;
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

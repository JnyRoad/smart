<!--系统信息  -->
<template>
  <div class="my-basic-container msnmodel">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="mixinSearchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="mixinResetFrom('searchForm')" plain>清空</el-button>
            <!-- <el-button type="primary" icon="icon-yutong-download" @click="exportHandle">导出</el-button> -->
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="设备名称" prop="deviceName">
            <el-input v-model="searchForm.deviceName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="剩余电量" prop="devicePower">
            <el-input v-model="searchForm.devicePower" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="设备状态" prop="connectStatus">
            <el-select v-model="searchForm.connectStatus" placeholder="请选择" clearable>
              <el-option label="断开" :value="0"> </el-option>
              <el-option label="正常" :value="1"> </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div class="tip_row">
          <div class="tip_row_item"><i class="i1"></i>正常</div>
          <div class="tip_row_item"><i class="i2"></i>断开</div>
          <div class="tip_row_item">
            <i class="i3"></i>低电量<span style="padding-left: 10px; font-size: 12px">(当前园区电量阈值：{{ power }})</span>
          </div>
        </div>
        <div class="content" v-if="dataList">
          <!-- connectStatus: 0 断开 1正常  ，正常的情况下再去判断是否是低电量 -->
          <div class="item" v-for="(item, index) in dataList" :key="index">
            <div class="item_dv1">
              <img v-if="item.connectStatusDesc === '连接' && (item.devicePower > item.electricThreshold || item.devicePower === item.electricThreshold)" class="el-image" style="width: 100%; height: 100%; object-fit: contain" src="/img/lock_1.png"/>
              <img v-if="item.connectStatusDesc === '连接' && item.devicePower < item.electricThreshold" class="el-image" style="width: 100%; height: 100%; object-fit: contain" src="/img/lock_2.png"/>
              <img v-if="item.connectStatusDesc === '断开'" class="el-image" style="width: 100%; height: 100%; object-fit: contain" src="/img/lock_3.png"/>
            </div>
            <div class="item_dv2">
              <div class="item_dv2_l1">{{ item.deviceName }}</div>
              <div class="item_dv2_l2">剩余电量：{{ item.devicePower }}%</div>
            </div>
            <!-- <div class="item_dv3">
              <el-button type="primary" @click.stop="openDoor(item)" :loading="item.loading===2" :disabled="item.connectStatus===0">
                {{item.loading===1?'开门':(item.loading===2?'正在开门':'开门完成')}}
              </el-button>
            </div> -->
          </div>
          <TceEmpty v-if="!dataList || dataList.length === 0" style="margin-top: 50px" />
        </div>
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pages.currentPage"
          :page-sizes="[10, 20, 30, 40, 50, 100]"
          :page-size="pages.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pages.total"
        >
        </el-pagination>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { deviceApi } from './_service'
import { allPark } from '@/api/platform/_publicService'
export default {
  mixins: [tce.mixins.list],
  data() {
    return {
      searchForm: {
        parkId: ''
      },
      pages: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 10 // 每页显示多少条
      },
      loading: false,
      power: undefined,
      dataList: []
    }
  },
  created() {
    let _this = this
    allPark().then((response) => {
      const d = response.data.data
      _this.searchForm.parkId = d[0].id
      this.getList(this.pages, this.searchForm)
    })
  },
  mounted() {},
  computed: {},
  watch: {
    'searchForm.parkId'(val) {
      if (val === undefined || val === null || val === '') {
        this.power = undefined
      }
    }
  },
  methods: {
    /**
     * 获取列表
     */
    async getList(page, params) {
      let query = Object.assign(
        {
          current: page.currentPage,
          size: page.pageSize
        },
        params
      )
      const res = await deviceApi.getList(query)
      this.dataList = []
      res.data.data.records.forEach((el) => {
        // loading: 1,初始化 2,正在开门 3,开门完成（成功/失败）
        this.dataList.push(Object.assign({ loading: 1 }, el))
      })
      if(this.pages.total > 0){
        this.power = res.data.data.records[0].electricThreshold
      }
      this.pages.total = res.data.data.total
    },
    /**
     * 清空搜索
     */
    mixinResetFrom(formName) {
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.pages.currentPage = 1
      this.getList(this.pages, this.searchForm)
    },
    handleSizeChange(val) {
      this.pages.currentPage = 1
      this.pages.pageSize = val
      this.getList(this.pages, this.searchForm)
    },
    handleCurrentChange(val) {
      this.pages.currentPage = val
      this.getList(this.pages, this.searchForm)
    }
  }
}
</script>

<style lang="scss" scoped>

$c1: #0fcc8e;
$c2: #c5ccd7;
$c3: #e72b30;
.el-pagination {
  text-align: right;
  margin-top: 30px;
}
.tip_row {
  display: flex;
  margin-bottom: 15px;
  &_item {
    margin-right: 23px;
    i {
      width: 12px;
      height: 13px;
      border-radius: 2px;
      display: inline-block;
      vertical-align: middle;
      margin: -2px 5px 0 0;
    }
    .i1 {
      background: $c1;
    }
    .i2 {
      background: $c2;
    }
    .i3 {
      background: $c3;
    }
  }
}
.content ::v-deep {
  display: flex;
  flex-wrap: wrap;
  .item {
    display: flex;
    align-items: center;
    width: 280px;
    height: 100px;
    margin: 0 10px 10px 0;
    padding: 20px 10px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    &_dv1 {
      width: 30px;
      height: 100%;
      margin-right: 10px;
      .elImage {
        width: 100%;
        height: 100%;
      }
    }
    &_dv2 {
      flex: 1;
      &_l1 {
        color: $blueFt;
        font-weight: bold;
        margin-bottom: 10px;
      }
    }
    &_dv3 {
      width: 100px;
      text-align: right;
      .el-button {
        padding: 5px 10px;
      }
    }
  }
}
.box-orange {
  font-size: 16px;
}
.dot-form {
  padding-top: 10px;
  padding-left: 30px;
  width: 80%;
  max-width: 700px;
}
</style>

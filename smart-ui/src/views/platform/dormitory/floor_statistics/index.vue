<!--宿舍管理，床位监控  -->
<template>
  <div class="my-basic-container bed_control">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
            <el-button type="primary" icon="icon-yutong-download" @click="exportExcel">导出</el-button>
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
          <el-form-item label="园区">
            <parkSelect v-model="searchform.parkId" :defaultSelected="true" @defaultHandle="defaultHandle"></parkSelect>
          </el-form-item>
          <el-form-item label="楼栋">
            <dormMultiSelect
              :parkId="searchform.parkId"
              v-model="searchform.dormitoryId"
            ></dormMultiSelect>
          </el-form-item>
          <el-form-item label="房间职层">
            <jcheSelect v-model="searchform.jcheId"></jcheSelect>
          </el-form-item>
          <el-form-item label="房间属性">
            <roomGenderSelect v-model="searchform.roomSex"></roomGenderSelect>
          </el-form-item>
        </el-form>
        <div class="table-outer">
          <div class="t_head">
            <div class="t_tr t_">
              <div class="t_td t_td_1">序号</div>
              <div class="t_td">宿舍名称</div>
              <div class="t_td">宿舍分类</div>
              <div class="t_td t_td_3">可用房间</div>
              <div class="t_td t_td_3">标配人数</div>
              <div class="t_td t_td_3">标配床位</div>
              <div class="t_td t_td_3">锁定床位</div>
              <div class="t_td t_td_3">可住人数</div>
              <div class="t_td t_td_3">已住人数</div>
              <div class="t_td t_td_3">空床位</div>
              <div class="t_td t_td_3">单独空房间</div>
            </div>
          </div>
          <el-scrollbar class="my-scrollbar" :native="false">
            <div class="t_body" v-for="(item, index) in tableData" :key="index">
              <div class="t_tr" v-for="(item2, index2) in item.sexList" :key="index2">
                <div class="t_td t_td_1">{{ index2+1 }}</div>
                <div class="t_td">{{ item2.dormitoryDesc || '-' }}</div>
                <div class="t_td">{{ item2.roomTypeDesc || '-'  }}</div>
                <div class="t_td t_td_3">{{ item2.roomNum }}</div>
                <div class="t_td t_td_3">{{ item2.typeBedTotal }}</div>
                <div class="t_td t_td_3">{{ item2.standardBedNum }}</div>
                <div class="t_td t_td_3">{{ item2.lockBedNum }}</div>
                <div class="t_td t_td_3">{{ item2.roomBedTotal }}</div>
                <div class="t_td t_td_3">{{ item2.alreadyUse }}</div>
                <div class="t_td t_td_3">{{ item2.freeBedNum }}</div>
                <div class="t_td t_td_3">{{ item2.freeRoomNum }}</div>
              </div>
              <div class="t_tr t_foot">
                <div class="t_td">宿舍-{{item.roomSexDesc}}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('roomNum') }}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('typeBedTotal') }}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('standardBedNum') }}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('lockBedNum') }}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('roomBedTotal') }}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('alreadyUse') }}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('freeBedNum') }}</div>
                <div class="t_td t_td_3">{{ item | fl_sum('freeRoomNum') }}</div>
              </div>
            </div>
            <div class="t_body" v-if="!tableData || tableData.length===0">
              <div class="t_tr">
                <div class="t_td">暂无数据</div>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </section>
  </div>
</template>
<style lang="scss" scoped>
@use "@/styles/platform/dormitory/bed_control" as *;
</style>

<script>
import { fetchList, exportApi } from "@/api/platform/dormitory/floor_statistics";
import dormMultiSelect from "@/views/platform/components/dorm-multi-select/index";
export default {
  name: "",
  components: {
    dormMultiSelect
  },
  data() {
    return {
      searchform: {
        parkId: '',
        dormitoryId: [],
        jcheId: '',
        roomSex: ''
      },
      tableData: []
    };
  },
  created() {},
  mounted: function() {},
  computed: {},
  filters: {
    fl_sum: function(item, label) {
      if(item.sexList.length>0){
        let sum = 0
        item.sexList.forEach(el => {
          if(el[label] !== null && el[label] !== undefined){
            sum += el[label]
          }
        });
        return sum
      }else{
        return 0
      }
    }
  },
  methods: {
    exportExcel() {
      exportApi(this.searchform).then(res => {
        var fileDownload = require("js-file-download");
        fileDownload(res.data, "楼层统计报表.xls");
      });
    },
    defaultHandle(e){
      this.searchform.parkId = e.value
      this.getList(this.searchform)
    },
    getList(params) {
      fetchList(params).then(response => {
        this.tableData = response.data.data
      });
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.getList(this.searchform)
    },
  }
};
</script>
<style lang="scss" scoped>
.my-basic-inner{
  height: 100%;
  display: flex;
  flex-direction: column;
}
.table-outer{
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  .t_tr{
    width: 100%;
    display: flex;
    justify-content: space-between;
    border-right: 1px solid #e0e0e0;
  }
  .t_td{
    text-align: center;
    padding: 10px 0;
    flex: 1;
    border: 1px solid #e0e0e0;
    border-bottom: none;
    border-right: none;
  }
}
.t_head{
  width: 100%;
  border-bottom: 1px solid #e0e0e0;
  .t_td{
    font-weight: bold;
    background: #eef1f6;
  }
}
.my-scrollbar{
  flex: 1;
}
.t_body{
  width: 100%;
}
.t_body:last-child{
  border-bottom: 1px solid #e0e0e0;
}
.t_foot{
  .t_td{
    color: #333;
    background: #FDF6EC;
  }
}
.t_td_1{
  flex: none !important;
  width: 70px !important;
}
.t_td_3{
  flex: none !important;
  width: 8% !important;
}
.info-table{
  width: 100%;
  border-collapse: collapse;
  th{
    background: #eef1f6;
  }
  th, td{
    line-height: 38px;
    text-align: center;
    border: 1px solid #e0e0e0;
  }
  tr:last-child td{
    border-bottom: none;
  }
  &:last-child tr:last-child td{
    border-bottom: 1px solid #e0e0e0;
  }
  .sum-row td{
    color: #333;
    background: #FDF6EC;
  }
}
</style>

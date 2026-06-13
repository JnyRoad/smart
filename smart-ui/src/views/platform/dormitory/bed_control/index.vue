<!--宿舍管理，床位监控  -->
<template>
  <div class="my-basic-container bed_control">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="onSubmit('searchform')">搜索</el-button>
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
          <el-form-item label="园区">
            <parkSelect v-model="searchform.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="楼栋">
            <dormMultiSelect
              :parkId="searchform.parkId"
              v-model="searchform.dormitoryIds"
            ></dormMultiSelect>
          </el-form-item>
        </el-form>
        <div class="tip-title">床位统计表</div>
        <el-row class="room-row">
          <el-col :span="6" v-for="item in dataType " :key="item.index">
            <dl class="clear roomInfo">
              <dt>
                <div class="bk1 pnum">{{item.total}}</div>
                <div class="m_left">
                  <p class="mnum">男 {{item.manNum}}</p>
                  <p class="fnum">女 {{item.womanNum}}</p>
                  <p class="otherNum">其他 {{item.otherNum}}</p>
                </div>
              </dt>
              <dd>
                <div class="bk1" style="padding-top: 5px;">
                  <p>{{item.welfareLevel}}{{item.welfareLevel?'-':''}}{{item.jcheName}}</p>
                  <p class="ptip">当前入住（人）</p>
                </div>
                <div class="m_right">
                  <p class="num-bar">
                    <i class="m-bar" :style="{width: getMaleRate(item.manNum, item.total)}"></i>
                  </p>
                  <p class="num-bar">
                    <i class="f-bar" :style="{width: getFemaleRate(item.womanNum, item.total)}"></i>
                  </p>
                  <p class="num-bar">
                    <i class="other-bar" :style="{width: getFemaleRate(item.otherNum, item.total)}"></i>
                  </p>
                </div>
              </dd>
            </dl>
          </el-col>
        </el-row>
        <div class="midMenu clear">
          <el-button
            type="primary"
            icon="icon-yutong-download"
            class="exportTable"
            @click="exportExcel"
          >导出表格</el-button>
          <template>
            <el-radio-group v-model="countType" class="radGroup" @change="typeChange">
              <el-radio :label="1">按楼层统计</el-radio>
              <el-radio :label="2">按房间类型统计</el-radio>
              <el-radio :label="3">按宿舍空位统计</el-radio>
            </el-radio-group>
          </template>
        </div>
        <template v-if="countType==1">
          <el-table id="excelFloor" :data="floorTable" empty-text="暂无数据" style="width: 100%">
            <el-table-column prop="dormitoryName" label="宿舍楼"></el-table-column>
            <el-table-column prop="floorName" label="楼层"></el-table-column>
            <el-table-column prop="sex" label="房间属性"></el-table-column>
            <el-table-column prop="total" label="应住"></el-table-column>
            <el-table-column prop="actualNumber" label="实住"></el-table-column>
            <el-table-column prop="surplus" label="剩余"></el-table-column>
          </el-table>
        </template>
        <template v-if="countType==2">
          <el-table id="excelGender" :data="sexTable" empty-text="暂无数据" style="width: 100%">
            <el-table-column prop="sex" label="房间属性"></el-table-column>
            <el-table-column prop="typeName" label="房间类型"></el-table-column>
            <el-table-column prop="total" label="应住"></el-table-column>
            <el-table-column prop="actualNumber" label="实住"></el-table-column>
            <el-table-column prop="surplus" label="剩余"></el-table-column>
          </el-table>
        </template>
        <template v-if="countType==3">
          <el-table id="excelFloor" :data="freeTable" empty-text="暂无数据" style="width: 100%">
            <el-table-column prop="dormitoryName" label="宿舍楼"></el-table-column>
            <el-table-column prop="floorName" label="楼层"></el-table-column>
            <el-table-column prop="sex" label="房间属性"></el-table-column>
            <el-table-column prop="total" label="应住"></el-table-column>
            <el-table-column prop="actualNumber" label="实住"></el-table-column>
            <el-table-column prop="surplus" label="剩余"></el-table-column>
          </el-table>
        </template>
      </section>
    </el-scrollbar>
  </div>
</template>
<style lang="scss" scoped>
@use "@/styles/platform/dormitory/bed_control" as *;
</style>

<script>
import {
  fetchType,
  fetchFloor,
  fetchSex,
  fetchFree
} from "@/api/platform/dormitory/bed_control";
import { allPark } from "@/api/platform/_publicService";
import dormMultiSelect from "@/views/platform/components/dorm-multi-select/index";

export default {
  name: "bed_control",
  components: {
    dormMultiSelect
  },
  data() {
    return {
      searchform: {
        parkId: '',
        dormitoryIds: []
      },
      countType: 1,
      parkData: [],
      floorTable: [], //按楼层统计表格
      sexTable: [], //按房间属性统计表格
      freeTable: [], //按宿舍空位统计表格
      dataType: [],
      asFloor: true //按照楼层统计
    };
  },
  created() {
    let _this = this;
    allPark().then(response => {
      _this.parkData = response.data.data;
      _this.searchform.parkId = _this.parkData[0].id;
      fetchType( _this.searchform).then(responseD => {
        _this.dataType = responseD.data.data;
        fetchFloor( _this.searchform).then(responseF => {
          _this.floorTable = responseF.data.data;
          fetchSex( _this.searchform).then(responseS => {
            _this.sexTable = responseS.data.data;
          });
          fetchFree( _this.searchform).then(responseS => {
            _this.freeTable = responseS.data.data;
          });
        });
      });
    });
  },
  mounted: function() {},
  computed: {},
  methods: {
    exportExcel() {
      //导出excel
      if (this.countType == 1) {
        //按楼层
        this.export2ExcelByFloor();
      } else if (this.countType == 2) {
        //按房间属性
        this.export2ExcelByGender();
      } else if (this.countType == 3) {
        //按宿舍空位
        this.export2ExcelByFree();
      }
    },
    //按楼层导出
    export2ExcelByFloor() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = ["宿舍楼", "楼层", "房间属性", "应住", "实住", "剩余"];
        const filterVal = [
          "dormitoryName",
          "floorName",
          "sex",
          "total",
          "actualNumber",
          "surplus"
        ];
        fetchFloor(this.searchform)
          .then(responseD => {
            const list = responseD.data.data;
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "床位监控按楼层统计表格");
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //按房间属性导出
    export2ExcelByGender() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = ["房间属性", "房间类型", "应住", "实住", "剩余"];
        const filterVal = [
          "sex",
          "typeName",
          "total",
          "actualNumber",
          "surplus"
        ];
        fetchSex(this.searchform)
          .then(responseD => {
            const list = responseD.data.data;
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "床位监控按房间属性统计表格");
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //按宿舍空位导出
    export2ExcelByFree() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = ["宿舍楼", "楼层", "房间属性", "应住", "实住", "剩余"];
        const filterVal = [
          "dormitoryName",
          "floorName",
          "sex",
          "total",
          "actualNumber",
          "surplus"
        ];
        fetchFree(this.searchform)
          .then(responseD => {
            const list = responseD.data.data;
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "床位监控按宿舍空位统计表格");
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    getMaleRate(male, total) {
      //获取男生数量进度条百分比值
      if (male == 0 || total == 0) {
        return "0";
      } else {
        return (male / total) * 100 + "%";
      }
    },
    getFemaleRate(female, total) {
      //获取女生数量进度条百分比值
      if (female == 0 || total == 0) {
        return "0";
      } else {
        return (female / total) * 100 + "%";
      }
    },
    // getMaleRate(male, female){ //获取男生数量进度条百分比值
    //   if(male==0){
    //     return '0';
    //   }
    //   if(male>female){
    //     return '100%';
    //   }else{
    //     return (male/female)*100+'%';
    //   }
    // },
    // getFemaleRate(female, male){ //获取女生数量进度条百分比值
    //   if(female==0){
    //     return '0';
    //   }
    //   if(female>male){
    //     return '100%';
    //   }else{
    //     return (female/male)*100+'%';
    //   }
    // },
    onSubmit() {
      var _this = this;
      fetchType(this.searchform).then(response => {
        _this.dataType = response.data.data;
        fetchFloor(this.searchform).then(responseD => {
          _this.floorTable = responseD.data.data;
          fetchSex(this.searchform).then(responseSex => {
            _this.sexTable = responseSex.data.data;
          });
          fetchFree(this.searchform).then(responseS => {
            _this.freeTable = responseS.data.data;
          });
        });
      });
    },
    resetFrom() {},
    typeChange() {
      // this.countType == 1 ? this.asFloor = true : this.asFloor = false
    }
  }
};
</script>

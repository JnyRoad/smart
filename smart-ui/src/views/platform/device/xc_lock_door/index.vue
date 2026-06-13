<!--设备管理，门禁管理  -->
<template>
  <div class="my-basic-container device guard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm',searchForm)"
              plain
            >清空</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
             <el-form-item label="选择园区" prop="areaIdArray">
              <areaCascader v-model="searchForm.areaIdArray" :changeOnSelect="true" placeholder="选择园区"></areaCascader>
            </el-form-item>
            <!-- <el-form-item label="所在区域" prop="areaIdArray">
              <areaCascader v-model="searchForm.areaIdArray" :changeOnSelect="true" placeholder="所在区域"></areaCascader>
            </el-form-item> -->
            <el-form-item label="接通状态" prop="isAvailable">
              <el-select v-model="searchForm.isAvailable" placeholder="请选择接通状态" clearable>
                <el-option label="全部" value=""></el-option>
                <el-option label="接通" value="1"></el-option>
                <el-option label="掉线" value="2"></el-option>
              </el-select>
            </el-form-item>
             <el-form-item label="电量" prop="devicePower">
               <el-input v-model="searchForm.devicePower" placeholder="0到100以内的数字" clearable></el-input>
            </el-form-item>
          </el-form>
        </div>
        <el-checkbox-group v-model="ckItem" class="list-group">
          <div class="device-list">
            <template v-for="(item, key, index) in datalist ">
              <div class="device-outer" :key="index">
                <dl class="device-item clear">
                  <div class="device-status" :class="item.connectStatus | deviceStatusClassFormat">
                    <span>{{item.connectStatus | deviceStatusFormat}}</span>
                  </div>
                  <dt></dt>
                  <dd>
                    <div class="info-tbl">
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">设备名称</div>
                        <div class="tbl_cell" :title="item.deviceName">{{item.deviceName || '-'}}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">绑定房间</div>
                        <div class="tbl_cell" :title="item.deviceArea">{{item.deviceArea || '-'}}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">启停状态</div>
                        <div class="tbl_cell">
                          <el-switch
                            v-model="item._isAvailable"
                            :disabled= "true"
                            :active-color="activeColor"
                            :inactive-color="inactiveColor"
                            :active-value="1"
                            :inactive-value="2"
                          ></el-switch>
                        </div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">电量</div>
                        <div class="tbl_cell" :title="item.tagNames">
                           <div class="num-bar">
                            <i class="m-bar" :style="{width: getMaleRate(item.devicePower)}"></i>
                          </div>
                          <div style="width:100%;">{{item.devicePower}}%</div>
                        </div>
                      </div>
                      <div class="tbl_row" style="float:right">
                        <div class="tbl_cell tbl_cell2">
                          <div>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >下发记录</el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </dd>
                </dl>
              </div>
            </template>
          </div>
        </el-checkbox-group>
        <div v-if="!hasData" class="noData">
          <i></i>
          暂无数据！
        </div>
        <div class="page-outer" v-show="hasData">
          <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page.currentPage"
            :page-sizes="[10, 20, 300, 40]"
            :page-size="page.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="page.total"
          ></el-pagination>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/device/camera" as *;
</style>

<script>
import { fetchList, putObj } from './_service'
import { mapGetters } from "vuex";

export default {
  mixins: [tce.mixins.list],
  name: "alarm",
  components: {
  },
  data() {
    return {
      addLoading: false,
      editLoading: false,
      activeColor: "#10CC8F",
      inactiveColor: "#C6CAD3",
      ckItem: [],
      page: {
        total: null, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        parkId: "",
        isAvailable: "",
        areaIdArray: [],
        devicePower: ""
      },
      datalist: [],
      recordId: null
    };
  },
  created() {
    this.getList(this.page, this.searchForm);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    hasData: function() {
      if (this.datalist) {
        if (this.datalist.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  },
  methods: {
    refresh(){
      this.ckItem = []
      this.getList(this.page, this.searchForm)
    },
    getList(page, params) {
      this.tableLoading = true;
      const obj = {
          current: page.currentPage,
          size: page.pageSize
      }
      fetchList(
        Object.assign(obj,params)
      ).then(response => {
        this.datalist = response.data.data.records;
        this.datalist.forEach(element => {
          switch(element.isAvailable){
            case 0:
              element['_isAvailable'] = 1
              break;
            case 1:
              element['_isAvailable'] = 0
              break;
          }
          // isAvailable
        });
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
    },
    // updateStatus(item) {
    //   putObj(item);
    // },
     getMaleRate(male) {
      //获取男生数量进度条百分比值
      if (male == 0) {
        return "0";
      } else {
        return (male / 100) * 100 + "%";
      }
    },
    /**
     * 顶部搜索
     */
    searchSubmit(form) {
      this.getList(this.page, form);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName, form) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
      this.getList(this.page, form);
    },
    /**
     * 通关人员
     */
    permittedList(item) {
      this.recordId = item.id
      //点击编辑
      const src = `/platform/device/lock_door/record/${item.id}`;
      this.$router.push({
        path: src
      });
    },
    /**
     * 搜索回调
     */
    searchChange(form) {
      this.getList(this.page, form);
    },
    handleSizeChange(val) {
    },
    handleCurrentChange(val) {
    }
  }
};
</script>

<style lang="scss" scoped>
.num-bar{
      height: 21px;
      border-radius: 10px;
      width: 70%;
      // margin-bottom: 13px;
      // margin-top: 7px;
      float:left;
      overflow: hidden;
      background: #EAEEF4;
      i{
        display: inline-block;
        float: left;
        height: 100%;
        border-radius: 10px;
      }
    }
    .m-bar{
      background: #10cc8f;
    }
    .f-bar{
      background: #FF698C;
    }
    .other-bar{
      background: #c1bdbd;
    }
</style>

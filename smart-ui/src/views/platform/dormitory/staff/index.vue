<!--宿舍管理，入住信息  -->
<template>
  <div class="my-basic-container mycard2 room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          <div class="cont-left">
            <span class="tiplbl">房间检索</span>
            <div style="margin: 10px 0">
              <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
                <el-button type="info" slot="append" icon="el-icon-search"></el-button>
              </el-input>
            </div>
            <el-tree
              class="filter-tree"
              :data="treedata"
              highlight-current
              :props="defaultProps"
              :filter-node-method="filterNode"
              @node-click="handleNodeClick"
              ref="roomtree"
            ></el-tree>
          </div>
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="tab-out">
          <avue-tabs :option="tabOption" @change="tabChange" class="tabs">
            <template slot="insider"></template>
            <template slot="outsider"></template>
          </avue-tabs>
        </div>
        <checkIn
          ref="checkIn"
          v-if="isCheckIn"
          :roomInfo="roomInfo"
        />
        <checkOut
          ref="checkOut"
          v-else
          :roomInfo="roomInfo"
        />
      </div>
    </el-scrollbar>
  </div>
</template>
<script>
import { allList } from "@/api/platform/dormitory/staff";
import checkIn from './check_in'
import checkOut from './check_out'

export default {
  name: "room",
  components: {
    checkIn,
    checkOut
  },
  data() {
    return {
      tabOption: {
        column: [
          {
            label: "入住记录",
            prop: "insider"
          },
          {
            label: "退宿记录",
            prop: "outsider"
          }
        ]
      },
      isCheckIn: true,
      filterText: "",
      roomInfo: {
        parkId: '',
        dormitoryId: '',
        floorId: '',
        roomId: ''
      },
      treedata: [],
      defaultProps: {
        children: "children",
        label: "label"
      }
    };
  },
  created() {
    this.getTree();
  },
  mounted: function() {},
  computed: {},
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val);
    }
  },
  methods: {
    tabChange(item) {
      if (item.prop == "outsider") {
        this.initOutsider();
      } else {
        this.initInsider();
      }
    },
    initInsider() {
      this.isCheckIn = true;
      this.$refs.checkIn&&this.$refs.checkIn.parentSearch()
    },
    initOutsider() {
      this.isCheckIn = false;
      this.$refs.checkOut&&this.$refs.checkOut.parentSearch()
    },
    getTree() {
      var _this = this;
      allList().then(response => {
        this.treedata = response.data.data;
      });
    },
    handleNodeClick(data, node) {
      var level = node.level;
      this.roomInfo = {
        parkId: '',
        dormitoryId: '',
        floorId: '',
        roomId: ''
      }
      if (level == 1) {
        this.roomInfo.parkId = data.id;
      } else if (level == 2) {
        this.roomInfo.dormitoryId = data.id;
      } else if (level == 3) {
        this.roomInfo.floorId = data.id;
      } else if (level == 4) {
        this.roomInfo.roomId = data.id;
      }
    },
    filterNode(value, data, node) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    }
  }
};
</script>
<style lang="scss" scoped>
.mycard2{
  .box-left{
    width: 225px;
    padding: 20px 10px;
  }
  .my-scrollbar{
    padding: 0 0 0 230px;
  }
}
.tab-out ::v-deep {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 20px 20px 0 20px;
  .el-tabs__item{
    height: 50px;
  }
}
</style>

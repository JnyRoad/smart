<!--宿舍管理，房间管理可视化  -->
<template>
  <div class="my-basic-container mycard home-visual">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner">
        <el-col :lg="5" :md="6" class="box-outer box-left">
          <div class="cont-left">
            <div class="lft-block">
              <p class="lft-title">选择园区</p>
              <parkSelect v-model="parkId" @doChange="getCountList"></parkSelect>
              <div class="cont-chart">
                <div
                  id="inRate"
                  ref="inRateChart"
                  style="width: 100%; height: 300px;"
                  :class="{hasNoHeight:!hasBuilding}"
                ></div>
                <ul class="info-ul clear" v-show="hasBuilding">
                  <li>
                    <p>{{countList.parkName}}</p>
                  </li>
                  <li>
                    <p>{{countList.actualNumber}}/{{countList.total==null?0:countList.total}}人</p>
                  </li>
                </ul>
              </div>
            </div>
            <div class="lft-block" v-show="hasBuilding">
              <p class="lft-title">选择楼栋</p>
              <el-radio-group v-model="dormitoryId">
                <div
                  class="dm-item"
                  v-for="item in countBuilding"
                  :key="item.dormitoryId"
                  @change="getDormitoryName(item.dormitoryName)"
                >
                  <el-radio :label="item.dormitoryId" @change="getFloor">{{item.dormitoryName}}</el-radio>
                  <div class="bar-outer">
                    <span class="bar"></span>
                    <span class="nums">{{item.manNumber+item.womanNumber}}/ {{item.total}}人</span>
                  </div>
                </div>
              </el-radio-group>
            </div>
            <div class="lft-block" v-show="hasBuilding">
              <p class="lft-title">
                <el-checkbox
                  :indeterminate="isIndeterminate"
                  v-model="checkAllfloors"
                  @change="handleCheckAllChange"
                  :disabled="!hansFloors"
                >全选</el-checkbox>选择楼层
              </p>
              <el-checkbox-group v-model="checkedFloors" @change="handleCheckedFloorChange">
                <el-checkbox
                  v-for="floor in floors"
                  :label="floor"
                  :key="floor.id"
                >{{floor.floorName}}层</el-checkbox>
              </el-checkbox-group>
            </div>
            <div v-if="!hasBuilding" class="noData">该园区下暂无楼栋信息</div>
          </div>
        </el-col>
        <el-col :lg="19" :md="18" class="box-outer">
          <div class="top-menu">
            房间列表
            <el-button
              type="primary"
              @click="goListPage()"
              icon="icon-yutong-calendar"
              class="iconBtn"
            ></el-button>
            <el-button type="info" icon="icon-yutong-picture" class="iconBtn" disabled></el-button>
          </div>
          <!-- 一栋楼的信息 -->
          <div class="bulding-outer" v-if="hasData">
            <div class="room-outer clear room-top-outer">
              <div class="r-left"></div>
              <div class="r-right">
                <p class="rt-top">
                  <span class="bulding">{{dormitoryName}}</span>
                  <span>{{countList.parkName}}生活园区</span>
                </p>
                <div class="tips-outer">
                  <span>
                    <i class="lit-dot remain"></i>未满
                  </span>
                  <span>
                    <i class="lit-dot full"></i>已满
                  </span>
                  <span>
                    <i class="lit-dot empty"></i>空房
                  </span>
                  <span>
                    <i class="room-type male"></i>男
                  </span>
                  <span>
                    <i class="room-type female"></i>女
                  </span>
                  <span>
                    <i class="room-type couple"></i>夫妻/家属
                  </span>
                  <span>
                    <i class="room-type other"></i>其他
                  </span>
                </div>
              </div>
            </div>
            <template v-for="(flitem, flindex) in building">
              <div class="room-outer clear" :key="flindex">
                <div class="r-left">
                  <span class="floor-id">{{flitem.floorName}} 层</span>
                </div>
                <div class="r-right">
                  <ul class="room-list clear">
                    <template v-for="(rmitem, rmindex) in flitem.roomInfoList">
                      <li
                        class="room-item"
                        :key="rmindex"
                        :class="rmitem.used | statusClass(rmitem.bedTotal)"
                      >
                        <i class="lit-dot" :class="rmitem.roomSex | genderClass"></i>
                        <p>
                          <span class="room-id">{{rmitem.roomName}}</span>
                        </p>
                        <div class="btm-p">
                          <p class="r-type">{{rmitem.typeName}}</p>
                          <p>{{rmitem.used==null?0:rmitem.used}}/{{rmitem.bedTotal}}</p>
                        </div>
                        <div class="more">
                          <el-dropdown placement="bottom">
                            <span class="el-dropdown-link">
                              <i class="el-icon-more"></i>
                            </span>
                            <el-dropdown-menu slot="dropdown">
                              <el-dropdown-item @click.native="handlebdDetail(rmitem)">详情</el-dropdown-item>
                              <el-dropdown-item @click.native="handleEdit(rmitem,rmindex)">编辑</el-dropdown-item>
                              <el-dropdown-item @click.native="handleDel(rmitem,rmindex)">删除</el-dropdown-item>
                            </el-dropdown-menu>
                          </el-dropdown>
                        </div>
                      </li>
                    </template>
                    <li class="noBed" v-if="flitem.roomInfoList.length==0">
                      <div class="noData">该楼层暂无床位信息！</div>
                    </li>
                  </ul>
                </div>
              </div>
            </template>
          </div>
          <div v-else class="noData">
            <i></i>
            请在左侧进行选择！
          </div>
        </el-col>
      </el-row>
      <el-dialog
        title="编辑"
        class="dialog_form"
        width="550px"
        @close="resetEditForm('editForm')"
        :visible.sync="editFormVisible"
      >
        <el-form :rules="editRules" ref="editForm" :model="editForm" label-width="100px">
          <el-form-item label="房间号" prop="roomName">
            <el-input v-model="editForm.roomName" disabled></el-input>
          </el-form-item>
          <el-form-item label="是否参与分配" prop="isDormitoryRoom">
            <el-select v-model="editForm.isDormitoryRoom" placeholder="请选择" @change="showBedNum">
              <el-option
                v-for="item in isDormitoryArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="宿舍分类" prop="typeId">
            <el-select v-model="editForm.typeId" placeholder="请选择" @change="getBedNum">
              <el-option
                v-for="item in dormitoryTypeList"
                :key="item.id"
                :label="item.typeName"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="床位数" prop="bedTotal">
            <el-input v-model="editForm.bedTotal" disabled></el-input>
          </el-form-item>
          <el-form-item label="性别分类" prop="roomSex">
            <el-select v-model="editForm.roomSex" placeholder="请选择">
              <el-option
                v-for="item in genderArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="resetEditForm('editForm')" plain>取 消</el-button>
          <el-button type="primary" @click="editSubmit('editForm')" :loading="editLoading">确 定</el-button>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>
<style lang="scss">
@use "@/styles/platform/dormitory/room_visual" as *;
</style>

<script>
import {
  countList,
  countBuilding,
  getFloor,
  roomVisual,
  getBedNum,
  allDormitoryType,
  putObj,
  delObj
} from "@/api/platform/dormitory/room";
import { allPark } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import echarts from "echarts";

//是否为寝室，默认0， 0-是，1-否
const isDormitoryOption = [
  { label: "是", value: 0 },
  { label: "否", value: 1 }
];
const genderOption = [
  { label: "男", value: 0 },
  { label: "女", value: 1 }
];

export default {
  name: "room",
  data() {
    return {
      editLoading: false,
      editFormVisible: false, //编辑房间是否显示
      editForm: {
        roomName: undefined, //房间号
        isDormitoryRoom: undefined, //是否为寝室
        typeId: undefined, //宿舍分类
        bedTotal: undefined, //床位数
        roomSex: undefined //性别分类
      },
      editRules: {
        roomName: [
          { required: true, message: "请输入房间号", trigger: "blur" }
        ],
        isDormitoryRoom: [
          { required: true, message: "请选择是否参与分配", trigger: "change" }
        ],
        typeId: [
          { required: true, message: "请选择宿舍分类", trigger: "change" }
        ],
        bedTotal: [
          { required: true, message: "请输入床位数", trigger: "blur" }
        ],
        roomSex: [
          { required: true, message: "请选择性别分类", trigger: "change" }
        ]
      },
      genderArr: genderOption,
      isDormitoryArr: isDormitoryOption,
      parkData: [],
      parkId: "", //所属园区
      hasBuilding: false, //园区选择后是否有楼栋的数据信息
      countList: {}, //园区信息
      dormitoryTypeList: [],
      countBuilding: [],
      dormitoryId: undefined, //所属楼栋
      checkedFloors: [], //所选楼层集合
      checkAllfloors: false, //是否全选
      isIndeterminate: false, //表示 checkbox 的不确定状态,只负责样式的控制
      floors: [], //所有楼层
      dormitoryName: null,
      building: [],
      editrules: {
        name: [
          { required: true, message: "请输入宿舍楼栋名称", trigger: "blur" }
        ],
        park: [{ required: true, message: "请选择所属园区", trigger: "change" }]
      }
    };
  },
  created() {
    this.getPark();
    this.allDormitoryType();
  },
  filters: {
    statusClass: function(used, total) {
      if (!used || used == 0) {
        return "empty";
      } else if (used == total) {
        return "full";
      } else if (total > used) {
        return "remain";
      }
    },
    genderClass: function(gender) {
      var colors = ["male", "female", "couple", "other"];
      return colors[gender];
    }
  },
  mounted: function() {
    this.initResize();
  },
  beforeUpdate: function() {
    this.initChart();
  },
  computed: {
    getInrate: function() {
      //计算图表入住率
      if (this.countList) {
        let t = this.countList.total;
        let u = this.countList.actualNumber;
        if (t) {
          return Number(((u / t) * 100).toFixed(1)) + "%";
        } else {
          return "0%";
        }
      } else {
        return "0%";
      }
    },
    hasData: function() {
      //是否有楼栋信息
      if (this.building) {
        if (this.building.length > 0) {
          return true;
        } else {
          return false;
        }
      }
    },
    hansFloors: function() {
      //是否有楼层信息
      if (this.floors) {
        if (this.floors.length > 0) {
          return true; //有楼层信息后，是可以选择的，
        }
      } else {
        return false;
      }
    }
  },
  methods: {
    getPark() {
      var page = this.page;
      var _this = this;
      allPark().then(response => {
        _this.parkData = response.data.data;
        if (_this.parkData.length > 0) _this.parkId = _this.parkData[0].id;
        _this.getCountList(_this.parkId);
      });
    },
    getDormitoryName(name) {
      this.dormitoryName = name;
    },
    getCountList(parkId) {
      var _this = this;
      _this.floors = [];
      _this.dormitoryId = null;
      _this.checkAllfloors = false;
      _this.building = [];
      countList({ parkId: parkId }).then(response => {
        if (response.data.data.length > 0) {
          _this.hasBuilding = true;
          _this.countList = response.data.data[0];
          countBuilding({ parkId: parkId }).then(dormResponse => {
            _this.countBuilding = dormResponse.data.data;
          });
        } else {
          _this.hasBuilding = false;
          _this.countList = [];
          _this.countBuilding = [];
        }
      });
    },
    getFloor(dormitoryId) {
      var query = {parkId:this.parkId, dormitoryId: dormitoryId };
      this.checkAllfloors = false;
      this.checkedFloors = [];
      this.building = [];
      getFloor(query).then(response => {
        this.floors = response.data.data;
      });
    },
    handleCheckAllChange(val) {
      //全选
      this.checkedFloors = val ? this.floors : [];
      this.isIndeterminate = false;
      var param = [];
      if (val) {
        param = this.floors;
      }
      var param = { floorList: JSON.stringify(param) };
      roomVisual(param).then(response => {
        this.building = response.data.data;
      });
    },
    handleCheckedFloorChange(value) {
      //楼层点击
      let checkedCount = value.length;
      this.checkAllfloors = checkedCount === this.floors.length;
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.floors.length;
      var param = { floorList: JSON.stringify(value) };
      roomVisual(param).then(response => {
        this.building = response.data.data;
      });
    },
    goListPage() {
      //切换列表页
      const src = `/platform/dormitory/room`;
      this.$router.push({
        path: src,
        query: {}
      });
    },
    allDormitoryType() {
      allDormitoryType().then(response => {
        this.dormitoryTypeList = response.data.data;
      });
    },
    showBedNum() {
      if (this.editForm.isDormitoryRoom == 1) {
        this.editForm.bedTotal = 0;
      }
    },
    getBedNum() {
      if (this.editForm.isDormitoryRoom == 1) {
        return;
      }
      var id = this.editForm.typeId;
      this.editForm.bedTotal = null;
      getBedNum(id).then(response => {
        this.editForm.bedTotal = response.data.data.bedTotal;
      });
    },
    editSubmit(formName) {
      //编辑，确定事件
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          putObj(this.editForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;

              this.editLoading = false;
              if (dataResult == true || dataResult == 1) {
                this.editFormVisible = false;
                this.handleCheckedFloorChange(this.checkedFloors);
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
              } else if (dataResult === false) {
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
            })
            .catch(() => {
              this.editLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    handleEdit(bitem) {
      this.editFormVisible = true;
      this.editForm = Object.assign({}, bitem);
      if (this.editForm.isDormitoryRoom == 0) {
        getBedNum(this.editForm.typeId).then(response => {
          this.editForm.bedTotal = response.data.data.bedTotal;
        });
      }
    },
    handlebdDetail(bitem) {
      //以房间ID为搜索条件的床位列表
      const src = `/platform/dormitory/bed_mng`;
      var params = { roomId: bitem.id };
      this.$router.push({
        path: src,
        query: params
      });
    },
    handleDel(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该房间信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(row.id);
        })
        .then(data => {
          if (data.data.data == false) {
            _this.$notify({
              title: "失败",
              message: data.data.msg,
              type: "error"
            });
          } else {
            _this.handleCheckedFloorChange(_this.checkedFloors);
            _this.$notify({
              title: "成功",
              message: "删除成功",
              type: "success"
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    initChart() {
      let _this = this;
      let obj = _this.countList;
      var myChart = echarts.init(document.getElementById("inRate"));
      // 指定图表的配置项和数据
      var option = {
        title: {
          text: this.getInrate,
          subtext: "入住率",
          x: "center",
          y: "center",
          textStyle: {
            color: "#ED6D00",
            fontSize: 28,
            fontWeight: "normal"
          },
          subtextStyle: {
            color: "#ED6D00",
            fontSize: 14,

            fontWeight: "normal"
          }
        },
        series: [
          {
            name: "inrate",
            type: "pie",
            radius: ["60%", "75%"],
            labelLine: {
              normal: {
                show: false
              }
            },
            data: [
              {
                value: obj.total, //已入住人数 total
                itemStyle: {
                  normal: {
                    color: "#ED6D00",
                    shadowColor: "#ED6D00",
                    shadowBlur: 10
                  }
                }
              },
              {
                value: obj.total - obj.actualNumber, //未入住人数 total-actualNumber
                itemStyle: {
                  normal: {
                    color: "#E6E9F0",
                    label: {
                      show: false
                    },
                    labelLine: {
                      show: false
                    }
                  },
                  emphasis: {
                    color: "#E6E9F0"
                  }
                }
              }
            ]
          }
        ]
      };

      if (_this.countList) {
        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
      }
    },
    initResize() {
      const self = this; //因为箭头函数会改变this指向，指向windows。所以先把this保存
      setTimeout(() => {
        window.onresize = function() {
          if (self.chart) {
            self.chart = echarts.init(self.$refs.inRateChart);
            self.chart.resize();
          }
        };
      }, 20);
    },
    resetEditForm(formName) {
      //重置编辑表单
      this.editFormVisible = false;
      this.editLoading = false;
    }
  }
};
</script>

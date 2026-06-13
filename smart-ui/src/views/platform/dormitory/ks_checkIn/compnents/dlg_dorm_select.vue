<!--选择宿舍-->
<template>
  <el-dialog
    :title="title"
    class="dialog_form plcd_form"
    width="830px"
    @close="resetSetForm('searchForm')"
    :visible.sync="setFormVisible"
  >
    <div>
      <div class="top_dv">
        <el-form ref="searchForm" class="searchForm" :model="searchForm" label-position="top">
          <el-form-item label="请选择空余床位" prop="freeBedNum">
            <el-radio-group v-model="searchForm.freeBedNum" @change="getRoomHandle" class="group1">
              <el-radio-button
                v-for="(item) in emptyArr"
                :key="item.value"
                :label="item.value"
              >{{item.label}}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="请选择房间属性" prop="roomSex">
            <el-radio-group v-model="searchForm.roomSex" @change="getRoomHandle" class="group1">
              <el-radio-button
                v-for="(item) in genderArr"
                :key="item.value"
                :label="item.value"
              >{{item.label}}
              </el-radio-button>
            </el-radio-group>
            <!-- <el-button
              style="margin-left: 30px;"
              @click="getRoomHandle"
              type="primary"
            >查询</el-button> -->
            <el-button
              style="margin-left: 30px;"
              type="primary"
              @click="resetSearch('searchForm')"
              plain
            >重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="sdcb_cont">
        <div class="sdcb_tp">
          <el-scrollbar class="my-lit-scrollbar" :native="false">
            <div class="cont-left">
              <template v-if="treedata.length>0">
                <span class="tiplbl">楼层检索</span>
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
                  node-key="id"
                  :expand-on-click-node="false"
                  :filter-node-method="filterNode"
                  @node-click="handleNodeClick"
                  ref="roomtree"
                  default-expand-all
                ></el-tree>
              </template>
              <template v-else>
                <div class="noFloor">没有符合条件的楼层信息</div>
              </template>
            </div>
          </el-scrollbar>
        </div>
        <div class="sdcb_btm">
          <el-scrollbar style="width: 100%; height: 100%" :native="false">
            <template v-if="roomData&&roomData.length>0">
              <div class="sdcbb_b">
                <template v-for="(item, index) in roomData">
                  <div class="room-outer" :key='item.roomId' @click="roomChange(item, index)" :class="{'active':curRoomIndex===index}">
                    <div class="bed-inner" >
                      <span class="bed-no">{{item.roomName}}房</span>
                      <div>{{item.roomSex | roomGenderInit}} {{item.freeBedNum}}/{{item.bedTotal}}</div>
                    </div>
                  </div>
                </template>
              </div>
            </template>
            <template v-else>
              <div class="noBed">没有符合条件的房间信息，请重新查询</div>
            </template>
          </el-scrollbar>
        </div>
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm('searchForm')" plain
        >取 消</el-button
      >
      <!-- <el-button
        type="primary"
        @click="editSubmit()"
        :loading="setLoading"
        >确 认</el-button
      > -->
    </div>
    <DlgDormBeds ref="dlgdormbeds" :row="curRoom" @done="done"/>
  </el-dialog>
</template>

<script>
import { getFloors, getRooms } from "../_service.js";
import DlgDormBeds from "./dlg_dorm_beds";

let emptyOption = []
for(let i = 8; i>0; i--){
  let obj = { label:`空床${i}位`, value: i }
  emptyOption.push(obj)
}
const genderOption = [
  { label: '男', value: 0 },
  { label: '女', value: 1 },
  { label: '夫妻/家属', value: 2 },
  { label: '其他', value: 3 }
]
export default {
  name: "",
  components: {
    DlgDormBeds
  },
  data() {
    return {
      genderArr: genderOption,
      emptyArr: emptyOption,
      searchForm: {
        freeBedNum: '',
        roomSex: '',
        floorId: ''
      },
      setFormVisible: false,
      setLoading: false, //是否正在设置
      showBeds: true,
      filterText: '',
      treedata: [],
      roomData: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      curRoom: {},
      curRoomIndex: undefined
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    title: {
      type: String,
      default: '选择房间'
    },
    row: undefined,
  },
  watch: {
    visible(newVal, oldVal) {
      if (newVal) {
        this.getTree(this.row)
      }
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    },
    filterText(val) {
      this.$refs.roomtree.filter(val);
    },
    row:{
			handler: function(val){
        // if(val&&val.roomId){
        //   this.getTree(val.roomId);
        // }
      },
			immediate: true
		},
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    done(room, bed){
      this.$emit('dlgdoSuccess',room, bed)
      this.resetSetForm()
    },
    roomChange(item, index){
      this.curRoom = item
      this.curRoomIndex = index
      this.$refs.dlgdormbeds && this.$refs.dlgdormbeds.open()
    },
    getRoomHandle(){
      this.getRooms()
    },
    initData() {
      this.setFormVisible = this.visible;
    },
    async handleNodeClick(data, node) {
      var level = node.level;
      this.roomData = []
      this.curRoom = {}
      this.curRoomIndex = undefined
      if(level === 3){
        this.searchForm.floorId = data.id
      }else{
        this.searchForm.floorId = ""
      }
      this.getRooms()
    },
    filterNode(value, data, node) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    getTree(obj) {
      getFloors(obj).then(response => {
        let arr = response.data.data
        if(arr && arr[0]){ //园区
          let arr2 = arr[0].children
          if(arr2[0]){ //楼栋
            let arr3 = arr2[0].children
            if(arr3){ //各楼层
              arr3.forEach(el => {
                el.children = null
              })
              this.treedata = arr
              this.getRoomHandle()
            }else{
              this.treedata = [];
            }
          }
        }
      });
    },
    getRooms() {
      this.curRoom = {}
      this.curRoomIndex = undefined
      let obj = Object.assign(this.row, this.searchForm)
      getRooms(obj).then(response => {
        this.roomData = response.data.data;
      });
    },
    resetSearch(formName){
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.getRoomHandle()
    },
    resetSetForm(formName) {
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.searchForm.floorId=""
      this.treedata = [];
      this.roomData = [];
      this.setFormVisible = false;
      this.setLoading = false;
    },
    async editSubmit() {
      // if(this.validatenull(this.curRoomId)){
      //   this.$message.error('请选择房间');
      //   return
      // }
      // let obj = this.roomData.find(item => {
      //   if (item.roomId === this.curRoomId) {
      //     return item;
      //   }
      // });
      // this.$emit("dlgdoSuccess", obj);
      // this.resetSetForm('searchForm')
    }
  }
};
</script>
<style lang="scss" scoped>
.plcd_form ::v-deep {
  $c1: #e5e8ec;
  $c2: #687893;
  $c3: #0dbc82;
  .searchForm{
    .el-form-item{
      margin-bottom: 0;
    }
    .el-form-item__label{
      padding: 0;
    }
    margin-bottom: 20px;
  }
  .noFloor{
    text-align: center;
    padding: 50px 0;
    color: #999;
  }
  .group1{
    .el-radio-button__inner{
      border: 1px solid #dcdfe6;
      padding: 8px 17px;
      margin: 0 5px 0 0;
    }
    .el-radio-button:first-child .el-radio-button__inner{
      border-radius: 4px;
    }
    .el-radio-button:last-child .el-radio-button__inner{
      border-radius: 4px;
    }
    .el-radio-button .el-radio-button__inner{
      border-radius: 4px;
    }
    .el-radio-button__inner:hover{
      color: #fff;
      background-color: #ed6d00;
    }
    .el-radio-button__orig-radio:checked+.el-radio-button__inner{
      color: #fff;
      background-color: #ed6d00;
      border-color: #ed6d00;
      box-shadow: -1px 0 0 0 #ed6d00;
    }
  }
  .top_dv{
    padding: 0 20px;
  }
  .my-lit-scrollbar{
    height: 100%;
  }
  .el-dialog__body {
    padding: 10px 0 0 0;
  }
  .sdcb_cont{
    display: flex;
    margin-bottom: 10px;
    min-height: 300px;
    max-height: 500px;

  }
  .sdcb_tp {
    width: 300px;
    padding: 0 20px;
    border-right: 1px solid #e0e0e0;
  }
  .row1{
    margin-bottom: 15px;
    .el-tooltip{
      display: inline-block;
      margin-left: 20px;
      font-weight: bold;
    }
  }
  .sdcb_btm {
    flex: 1;
    padding: 0 20px 20px;
    .noBed{
      padding: 30px 0;
      text-align: center;
      color: #999;
    }
    .sdcbb_t{
      >span{
        margin-right: 30px;
        i{
          width: 6px;
          height: 6px;
          display: inline-block;
          vertical-align: middle;
          margin-right: 4px;
          border-radius: 50%;
        }
      }
      .st1{
        color: #999;
        i{
          background: #999;
        }
      }
      .st2{
        color: $c2;
        i{
          background: $c2;
        }
      }
      .st3{
        color: $c3;
        i{
          background: $c3;
        }
      }
    }
    .sdcbb_b{
      display: flex;
      flex-wrap: wrap;
      .room-outer{
        border: none;
        background: $c1;
        font-weight: normal;
        width: 130px;
        height: 50px;
        padding: 0 10px;
        cursor: pointer;
        margin: 0 0 15px 10px;
        &:hover{
          color: #fff;
          border-color: $c3;
          box-shadow: -1px 0 0 0 $c3;
          background: $c3;
        }
        &.active{
          color: #fff;
          border-color: $c3;
          box-shadow: -1px 0 0 0 $c3;
          background: $c3;
        }
      }
      .bed-inner{
        line-height: 20px;
        padding: 5px;
        text-align: center;
        .bed-name{
          text-align: center;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
      }
      .el-radio-group{
        display: flex;
        justify-content: center;
        align-items: center;
        flex-wrap: wrap;
      }
      .el-radio-button{
        margin: 0 10px 20px;
      }
      .el-radio-button__inner{
        border: none;
        background: $c1;
        font-weight: normal;
        width: 130px;
        height: 50px;
        // line-height: 50px;
        padding: 0 10px;
      }
      .el-radio-button:last-child .el-radio-button__inner,
      .el-radio-button:first-child .el-radio-button__inner{
        border-radius: 0;;
      }
      .el-radio-button__orig-radio:checked+.el-radio-button__inner{
        color: #fff;
        background: $c3;
        box-shadow: -1px 0 0 0 $c3;
      }
      .el-radio-button__inner:hover{
        color: #fff;
        border-color: $c3;
        box-shadow: -1px 0 0 0 $c3;
        background: $c3;
      }
      .el-radio-button__orig-radio:disabled+.el-radio-button__inner{
        color: #fff;
        background: $c2;
      }
    }
  }
}
</style>
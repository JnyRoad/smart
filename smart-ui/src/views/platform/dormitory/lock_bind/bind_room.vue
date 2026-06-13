
<!--
- @name 门锁绑定 - 绑定房间
-->
<template>
  <el-dialog ref="dialog" title="绑定房间" :visible.sync="currVisible" width="1000px" @open="open" @close="close" :append-to-body="true" :custom-class="'approve-detail-dialog'">
    <div class="lock_bind">
      <div class="sdcb_cont">
        <div class="sdcb_tp">
          <div class="cont-left">
            <template v-if="treedata.length > 0">
              <span class="tiplbl">选择楼栋及楼层</span>
              <div style="margin: 10px 0">
                <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
                  <el-button type="info" slot="append" icon="el-icon-search"></el-button>
                </el-input>
              </div>
              <el-scrollbar class="my-lit-scrollbar" :native="false">
                <el-tree
                  style="height: 480px"
                  class="filter-tree"
                  :data="treedata"
                  highlight-current
                  :props="defaultProps"
                  node-key="id"
                  :expand-on-click-node="false"
                  :filter-node-method="filterNode"
                  @node-click="handleNodeClick"
                  ref="roomtree"
                ></el-tree>
              </el-scrollbar>
            </template>
            <template v-else>
              <div class="noFloor">没有符合条件的楼层信息</div>
            </template>
          </div>
        </div>
        <div class="sdcb_btm">
          <template v-if="roomData && roomData.length > 0">
            <div class="sdcbb_b">
              <el-scrollbar style="width: 100%; height: 100%" :native="false">
                <template v-for="(item, index) in roomData">
                  <div class="room-outer" :key="item.roomId" @click="roomChange(item, index)" :class="{ active: curRoomIndex === index }">
                    <div class="bed-inner">
                      <span class="bed-no">{{ item.roomName }}房</span>
                      <div>{{ item.roomSex | roomGenderInit }} {{ item.freeBedNum }}/{{ item.bedTotal }}</div>
                    </div>
                  </div>
                </template>
              </el-scrollbar>
            </div>
          </template>
          <template v-else>
            <div class="noBed">没有符合条件的房间信息，请选择楼层</div>
          </template>
        </div>
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">绑 定</el-button>
    </div>
  </el-dialog>
</template>


<script>
import { allList, fetchRoomList, bingRoom } from './_service.js'
export default {
  data() {
    return {
      currVisible: false,
      btnLoading: false,
      filterText: '',
      treedata: [],
      roomData: [],
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      searchForm: {
        parkId: '',
        dormitoryId: '',
        floorId: ''
      },
      curRoom: {},
      curRoomIndex: undefined
    }
  },
  props: {
    visible: {
      type: Boolean
    },
    title: {
      type: String,
      default: '选择床位'
    },
    row: undefined
  },
  created() {
    this.getTree()
  },
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val)
    },
    row: {
      handler: function (val) {},
      immediate: true
    }
  },
  mounted: function () {},
  computed: {},
  methods: {
    getTree() {
      var _this = this
      allList().then((response) => {
        // this.treedata = response.data.data
        let arr = response.data.data
        if (arr && arr[0]) {
          //园区
          arr.forEach(ele1 => {
            let arr2 = ele1.children
            //楼
            arr2.forEach(ele2 => {
              let arr3 = ele2.children
              arr3.forEach(ele3 => {
                ele3.children = null
              });
            });
          });
        }
        this.treedata = arr
      })
    },
    formSumit() {
      let _this = this
      const elm = this.$createElement
      if (this.curRoom.id) {
        const areaName = this.curRoom.dormitoryName + '/' + this.curRoom.roomName
        if (this.row.deviceArea !== null || this.row.deviceArea !== '') {
          this.$msgbox({
            message: elm('p', { attrs: { class: 'smallp' } }, [elm('span', null, '确认将' + _this.row.deviceArea + '修改为' + areaName + '?')]),
            showCancelButton: true,
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            customClass: 'small_dialog',
            center: true
          }).then(function () {
            _this.bingRoom(_this.curRoom.id, areaName)
          })
        } else {
          _this.bingRoom(_this.curRoom.id, areaName)
        }
      }
    },
    bingRoom(roomId, areaName) {
      const obj = {
        id: this.row.id,
        roomId: roomId,
        areaName: areaName
      }
      this.btnLoading = true
      bingRoom(Object.assign(obj))
        .then((response) => {
          this.$notify({
            title: '成功',
            message: '门锁绑定成功',
            type: 'success'
          })
          this.btnLoading = false
          this.cancel()
          this.$emit('done', this.row, this.curRoom)
        })
        .catch((err) => {
          this.btnLoading = false
        })
    },
    async handleNodeClick(data, node) {
      var level = node.level
      this.roomData = []
      this.curRoom = {}
      this.curRoomIndex = undefined
      if (level == 3) {
        //只有选择楼层的时候进行查询
        // console.log('楼层')
        this.searchForm.parkId = node.parent.parent.data.id
        this.searchForm.dormitoryId = node.parent.data.id
        this.searchForm.floorId = data.id
        // console.log('园区id--' + this.searchForm)
        this.getRooms()
      }
    },
    roomChange(item, index) {
      this.curRoom = item
      this.curRoomIndex = index
      // console.log(item, index)
      // this.$refs.dlgdormbeds && this.$refs.dlgdormbeds.open()
    },
    //查询房间列表
    getRooms(params) {
      fetchRoomList(
        Object.assign(
          {
            asc: 'room_name',
            parkId: this.searchForm.parkId,
            dormitoryId: this.searchForm.dormitoryId,
            floorId: this.searchForm.floorId
          },
          params
        )
      ).then((response) => {
        this.roomData = response.data.data
      })
    },
    filterNode(value, data, node) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
$c1: #e5e8ec;
$c2: #687893;
$c3: #0dbc82;
.noFloor {
  text-align: center;
  padding: 50px 0;
  color: #999;
}
.group1 {
  .el-radio-button__inner {
    border: 1px solid #dcdfe6;
    padding: 8px 17px;
    margin: 0 5px 0 0;
  }
  .el-radio-button:first-child .el-radio-button__inner {
    border-radius: 4px;
  }
  .el-radio-button:last-child .el-radio-button__inner {
    border-radius: 4px;
  }
  .el-radio-button .el-radio-button__inner {
    border-radius: 4px;
  }
  .el-radio-button__inner:hover {
    color: #fff;
    background-color: #ed6d00;
  }
  .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    color: #fff;
    background-color: #ed6d00;
    border-color: #ed6d00;
    box-shadow: -1px 0 0 0 #ed6d00;
  }
}
.top_dv {
  padding: 0 20px;
}
.my-lit-scrollbar {
  height: 100%;
}
.el-dialog__body {
  padding: 10px 0 0 0;
}
.sdcb_cont {
  display: flex;
  margin-bottom: 10px;
  min-height: 500px;
  max-height: 600px;
}
.sdcb_tp {
  width: 300px;
  padding: 0 20px;
  border-right: 1px solid #e0e0e0;
}
.row1 {
  margin-bottom: 15px;
  .el-tooltip {
    display: inline-block;
    margin-left: 20px;
    font-weight: bold;
  }
}
.sdcb_btm {
  flex: 1;
  padding: 0 20px 20px;
  .noBed {
    padding: 30px 0;
    text-align: center;
    color: #999;
  }
  .sdcbb_t {
    > span {
      margin-right: 30px;
      i {
        width: 6px;
        height: 6px;
        display: inline-block;
        vertical-align: middle;
        margin-right: 4px;
        border-radius: 50%;
      }
    }
    .st1 {
      color: #999;
      i {
        background: #999;
      }
    }
    .st2 {
      color: $c2;
      i {
        background: $c2;
      }
    }
    .st3 {
      color: $c3;
      i {
        background: $c3;
      }
    }
  }
  .sdcbb_b {
    display: flex;
    flex-wrap: wrap;
    .room-outer {
      border: none;
      background: $c1;
      font-weight: normal;
      width: 130px;
      height: 50px;
      padding: 0 10px;
      cursor: pointer;
      margin: 0 0 15px 10px;
      float: left;
      &:hover {
        color: #fff;
        border-color: $c3;
        box-shadow: -1px 0 0 0 $c3;
        background: $c3;
      }
      &.active {
        color: #fff;
        border-color: $c3;
        box-shadow: -1px 0 0 0 $c3;
        background: $c3;
      }
    }
    .bed-inner {
      line-height: 20px;
      padding: 5px;
      text-align: center;
      .bed-name {
        text-align: center;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
    }
    .el-radio-group {
      display: flex;
      justify-content: center;
      align-items: center;
      flex-wrap: wrap;
    }
    .el-radio-button {
      margin: 0 10px 20px;
    }
    .el-radio-button__inner {
      border: none;
      background: $c1;
      font-weight: normal;
      width: 130px;
      height: 50px;
      // line-height: 50px;
      padding: 0 10px;
    }
    .el-radio-button:last-child .el-radio-button__inner,
    .el-radio-button:first-child .el-radio-button__inner {
      border-radius: 0;
    }
    .el-radio-button__orig-radio:checked + .el-radio-button__inner {
      color: #fff;
      background: $c3;
      box-shadow: -1px 0 0 0 $c3;
    }
    .el-radio-button__inner:hover {
      color: #fff;
      border-color: $c3;
      box-shadow: -1px 0 0 0 $c3;
      background: $c3;
    }
    .el-radio-button__orig-radio:disabled + .el-radio-button__inner {
      color: #fff;
      background: $c2;
    }
  }
}
</style>

<!--
- @name 门锁绑定 - 绑定房间
-->
<template>
  <el-dialog ref="dialog" title="使用宿舍" :visible.sync="currVisible" width="500px" @open="open" @close="close" :close-on-click-modal="false" :append-to-body="true" :custom-class="'approve-detail-dialog'">
    <div class="lock_bind">
      <div class="sdcb_cont">
        <div class="sdcb_tp">
          <div class="cont-left">
            <template v-if="treedata.length > 0">
              <span class="tiplbl">选择房间</span>
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
                  show-checkbox
                  :expand-on-click-node="true"
                  :filter-node-method="filterNode"
                  :default-expanded-keys="checkedArr"
                  :default-checked-keys="checkedArr"
                  ref="roomtree"
                ></el-tree>
              </el-scrollbar>
            </template>
            <template v-else>
              <div class="noFloor">没有符合条件的楼层信息</div>
            </template>
          </div>
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
import { allList, rangeEdit, rangeData} from '../_service.js'
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
      checkedArr:[],
      curRoom: {},
      curRoomIndex: undefined
    }
  },
  props: {
    tempId: [Number, String],
    parkId: [Number, String],
    visible: {
      type: Boolean
    },
    title: {
      type: String,
      default: '选择床位'
    },
  },
  created() {
    // console.log()
  },
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val)
    }
  },
  mounted: function () {},
  computed: {},
  methods: {
    getTree() {
      allList({
        parkId: this.parkId,
        tempId: this.tempId,
        type: 1
      }).then((response) => {
        let arr = response.data.data
        if (arr && arr[0]) {
          arr.forEach(ele1 => {
            ele1['level'] = 1
            if(ele1.children){
              let arr2 = ele1.children
              arr2.forEach(ele2 => {
                ele2['level'] = 2
                if(ele2.children){
                  let arr3 = ele2.children
                  arr3.forEach(ele3 => {
                    ele3['level'] = 3
                    ele3.children = null
                  });
                }else{
                  ele2['disabled'] = true
                }
              });
            }else{
              ele1['disabled'] = true
            }
          });
        }
        this.treedata = arr
        this.getRangeData()
      })
    },
    async getRangeData(){
      const res = await rangeData(this.tempId, 1)
      const d = res.data.data
      const arr = []
      d.forEach(element => {
        arr.push(element.value)
      });
      this.checkedArr = arr
      // this.$refs.roomtree.setCheckedKeys(arr)
    },
    async formSumit() {
      const checkedRooms = this.$refs.roomtree.getCheckedNodes().filter(item=> item.level === 3)
      const arr = []
      checkedRooms.forEach(element => {
        arr.push({
          parkId: this.parkId,
          tempId: this.tempId,
          type: 1,
          value: element.id
        })
      });
      this.btnLoading = true
      const res = await rangeEdit(arr)
      this.btnLoading = false
      if(res.data.code === 0){
        this.$notify({
          title: '成功',
          message: '绑定成功',
          type: 'success'
        });
        this.close()
      }
    },
    handleCheckChange(data, checked){
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
      this.checkedArr = []
      this.currVisible = false
    },
    open() {
      if(this.tempId && this.parkId){
        this.getTree()
      }
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.checkedArr = []
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
  width: 100%;
  padding: 0 20px;
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
      line-height: 40px;
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
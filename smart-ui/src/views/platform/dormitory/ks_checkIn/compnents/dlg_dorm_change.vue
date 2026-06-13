<!--更换宿舍-->
<template>
  <el-dialog
    :title="title"
    class="dialog_form plcd_form"
    width="800px"
    @close="resetSetForm()"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :inline="true" size="mini">
      <div class="sdcb_cont">
        <div class="sdcb_tp">
          <el-scrollbar class="my-lit-scrollbar" :native="false">
            <div class="cont-left">
              <div class="row1">入住人员
                <el-tooltip placement="bottom">
                  <div slot="content" style="line-height: 25px">
                    姓名：{{row.staffName}}<br/>
                    工号：{{row.staffBadge}}<br/>
                    性别：{{row.sex | genderInit}}<br/>
                    部门：{{row.depName}}<br/>
                    职级：{{row.jobName}}<br/>
                  </div>
                  <div style="cursor: pointer; font-weight:bold"><i class="person_i"></i><span>{{row.staffName}}</span></div>
                </el-tooltip>
              </div>
              <span class="tiplbl">房间检索</span>
              <div style="margin: 10px 0">
                <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
                  <el-button type="info" slot="append" icon="el-icon-search"></el-button>
                </el-input>
              </div>
              <el-tree
                class="filter-tree"
                :data="treedata"
                :highlight-current="true"
                :props="defaultProps"
                node-key="id"
                :expand-on-click-node="false"
                :filter-node-method="filterNode"
                :current-node-key="row.roomId"
                :default-expanded-keys="[row.roomId]"
                @node-click="handleNodeClick"
                ref="roomtree"
              ></el-tree>
            </div>
          </el-scrollbar>
        </div>
        <div class="sdcb_btm">
          <el-scrollbar style="width: 100%; height: 100%" :native="false">
            <template v-if="bedArr&&bedArr.length>0">
              <div class="sdcbb_t">
                <span class="st1"><i></i>空床可选</span>
                <span class="st2"><i></i>不可选</span>
                <span class="st4"><i></i>已禁用</span>
                <span class="st3"><i></i>当前选择床位</span>
              </div>
              <div class="sdcbb_b">
                <el-radio-group v-model="curBed">
                  <template v-for="(item) in bedArr">
                    <el-radio-button :label="item.id" :disabled="(item.staffName===null?false:true)||item.delFlag===1" :class="{'delFlag':item.delFlag===1}" :key='item.id' checked>
                      <div class="bed-inner">
                        <span class="bed-no">{{item.bedNumber}}床</span>
                        <span class="bed-name" :title="item.staffName">{{item.staffName}}</span>
                      </div>
                    </el-radio-button>
                  </template>
                </el-radio-group>
              </div>
            </template>
            <template v-else>
              <div class="noBed">请先选择房间！</div>
            </template>
          </el-scrollbar>
        </div>
      </div>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm()" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        @click="editSubmit()"
        :loading="setLoading"
        >确认入住</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { bedDetail, changeBed, getDormTreeByRoomId } from "../_service.js";
import { mapGetters } from "vuex";
export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
      showBeds: true,
      filterText: '',
      treedata: [],
      curBed: '', //当前所选床位
      bedArr: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    title: {
      type: String,
      default: '更换宿舍'
    },
    row: undefined,
  },
  watch: {
    visible(newVal, oldVal) {
      if (newVal) {
        this.getBeds(this.row.roomId)
        this.$refs.roomtree && this.$refs.roomtree.setCurrentKey(this.row.roomId);
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
        if(val&&val.roomId){
          this.getTree(val.roomId);
        }
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
    initData() {
      this.setFormVisible = this.visible;
    },
    async handleNodeClick(data, node) {
      var level = node.level;
      this.bedArr = []
      this.curBed = ''
      if(level == 4){
        this.getBeds(data.id)
      }
    },
    filterNode(value, data, node) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    getTree(roomId) {
      getDormTreeByRoomId(roomId).then(response => {
        this.treedata = response.data.data;
        this.$nextTick(_ => {
          this.$refs.roomtree && this.$refs.roomtree.setCurrentKey(this.row.roomId);
        });
      });
    },
    resetSetForm() {
      this.setFormVisible = false;
      this.setLoading = false;
      this.bedArr = []
    },
    async getBeds(roomId) {
      this.bedArr = []
      this.curBed = ''
      let res = await bedDetail(roomId)
      if(res.data.code==0){
        this.bedArr = res.data.data
      }
    },
    async editSubmit() {
      if(this.validatenull(this.curBed)){
        this.$message.error('请选择要更换的新床位');
        return
      }
      const res =  await changeBed({
        bedId: this.curBed,
        staffBadge: this.row.staffBadge,
        oldBedId: this.row.bedId
      });
      if(res.data.code==0){
        this.$notify({
          title: '成功',
          message: '换宿成功',
          type: 'success'
        });
        this.$emit("dlgdoSuccess");
      }
      this.resetSetForm()
    },
  },
};
</script>
<style lang="scss" scoped>
.plcd_form ::v-deep {
  $c1: #e5e8ec;
  $c2: #687893;
  $c3: #0dbc82;
  $c4: #e8888b;
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
    padding: 20px;
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
      .st4{
        color: $c4;
        i{
          background: $c4;
        }
      }
    }
    .sdcbb_b{
      padding: 20px 20px 0;
      .bed-inner{
        display: flex;
        justify-content: space-between;
        .bed-no{
          width: 40px;
          text-align: left;
        }
        .bed-name{
          flex: 1;
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
        line-height: 50px;
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
      .delFlag  .el-radio-button__orig-radio:disabled+.el-radio-button__inner{
        color: #fff;
        background: $c4;
      }
    }
  }
}
</style>
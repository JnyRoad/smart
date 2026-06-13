<!--更换宿舍-->
<template>
  <el-dialog
    :title="title"
    class="dialog_form plcd_form"
    width="800px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :visible.sync="currVisible"
  >
    <el-form ref="dataform" :inline="true" size="mini">
      <div class="sdcb_cont">
        <div class="sdcb_btm">
          <template v-if="bedArr&&bedArr.length>0">
            <div class="sdcbb_t">
              <span class="st1"><i></i>空床可选</span>
              <span class="st2"><i></i>不可选</span>
              <span class="st4"><i></i>已禁用</span>
              <span class="st3"><i></i>当前选择床位</span>
            </div>
            <div class="sdcbb_b">
              <el-radio-group v-model="curBedId" @change="bedChange">
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
        </div>
      </div>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="cancel" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        @click="editSubmit()"
        >确 定</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { bedDetail} from "../_service.js";
export default {
  name: "",
  data() {
    return {
      currVisible: false,
      curBedId: '', //当前所选床位
      curBed: {},
      bedArr: []
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    title: {
      type: String,
      default: '选择床位'
    },
    row: undefined,
  },
  watch: {
    row:{
			handler: function(val){},
			immediate: true
		},
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        if(this.row && this.row.roomId){
          this.getBeds(this.row.roomId);
        }
      }
    }
  },
  created() {},
  mounted: function () {},
  computed: {},
  methods: {
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    async getBeds(roomId) {
      this.bedArr = []
      this.curBedId = ''
      let res = await bedDetail(roomId)
      if(res.data.code==0){
        this.bedArr = res.data.data
      }
    },
    bedChange(id){
      this.curBed = this.bedArr.find(item => {
        if (item.id === id) {
          return item;
        }
      });
    },
    async editSubmit() {
      if(this.validatenull(this.curBedId)){
        this.$message.error('请选择床位');
        return
      }
      this.$emit('done',this.row,this.curBed)
      this.close()
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
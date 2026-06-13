<!--公摊抄表-新增水电表-->
<template>
  <el-dialog
    :title="mTitle"
    class="dialog_form gtcb_form"
    width="800px"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :inline="true" :model="dataform" :rules="addRule" size="mini">
      <div class="sdcb_tp">
        <div>
          <el-form-item label="园区选择" prop="parkId" class="m_ipt">
            <parkSelect
              @doChange="doParkChange"
              v-model="dataform.parkId"
            ></parkSelect>
          </el-form-item>
          <el-form-item label="宿舍选择" prop="dormitoryId" class="m_ipt">
            <dormSelect
              @doChange="doDormChange"
              :parkId="dataform.parkId"
              v-model="dataform.dormitoryId"
            ></dormSelect>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="楼层选择" prop="floorIds" class="m_ipt">
            <floorMultiSelect
              @doChange="doFloorChange"
              :parkId="dataform.parkId"
              :dormitoryId="dataform.dormitoryId"
              v-model="dataform.floorIds"
            ></floorMultiSelect>
          </el-form-item>
          <el-form-item label="设备名称" prop="sdName" class="m_ipt">
            <el-input v-model="dataform.sdName"/>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="设备类型" prop="categoryId" class="m_ipt">
            <el-radio v-model="dataform.categoryId" :label="1">热水</el-radio>
            <el-radio v-model="dataform.categoryId" :label="2">冷水</el-radio>
            <el-radio v-model="dataform.categoryId" :label="3">电</el-radio>
          </el-form-item>
        </div>
      </div>
      <div class="sdcb_trans">
        <p>公摊房间选择 <span class="tip">公摊水电将自动均摊至选中的宿舍房间中，一个房间仅可公摊一次</span></p>
        <el-form-item prop="roomIds" class="trans_ipt">
          <el-transfer
            :titles="['所有房间', '公摊房间']"
            v-model="dataform.roomIds"
            :data="rooms"
          ></el-transfer>
        </el-form-item>
      </div>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm('dataform')" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        @click="editSubmit('dataform')"
        :loading="setLoading"
        >保 存</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { commonsdSave, getRoomByFloorIds } from "../_service.js";
import floorMultiSelect from "@/views/platform/components/floor-multi-select/index";

export default {
  name: "",
  components:{
    floorMultiSelect
  },
  data() {
    return {
      workCompList: [],
      setFormVisible: false,
      setLoading: false, //是否正在设置
      dataform: {
        parkId: '',
        dormitoryId: '',
        floorIds: [],
        roomIds: [],
        sdName: '',
        categoryId: ''
      },
      addRule: {
        parkId: [
          { required: true, message: '请选择园区', trigger: 'change' }
        ],
        dormitoryId: [
          { required: true, message: '请选择楼栋', trigger: 'change' },
        ],
        floorIds: [
          { required: true, message: '请选择楼层', trigger: 'change' },
        ],
        sdName: [
          { required: true, message: '请输入设备名称', trigger: 'blur' },
        ],
        categoryId: [
          { required: true, message: '请选择设备类型', trigger: 'change' },
        ],
      },
      rooms: []
    };
  },
  props: {
    mTitle: {
      type: String,
      default: function(){
        return '新增水电表'
      }
    },
    visible: {
      type: Boolean,
    },
    row: {
      type: Object,
      default: function(){
        return null
      }
    },
  },
  watch: {
    visible(newVal, oldVal) {
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    },
    row:{
			handler: function(val){
        if(val&&val.id){
          this.getDetail();
        }else{
          this.initForm()
        }
      },
			immediate: true
		},
    'dataform.floorIds'(newVal, oldVal) {
      if (newVal&&newVal.length>0) {
        this.getRoomList(this.dataform.floorIds)
        if(this.row&&this.row.id){
          this.dataform.roomIds = this.row.roomIds
        }
      }else{
        this.dataform.roomIds = []
        this.rooms = []
      }
    },
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    doParkChange(e){
      this.dataform.dormitoryId = undefined
      this.dataform.floorIds = []
    },
    initForm(){
      this.dataform= {
        parkId: '',
        dormitoryId: '',
        floorIds: [],
        roomIds: [],
        sdName: '',
        categoryId: ''
      }
    },
    doDormChange(e){
      this.dataform.floorIds = []
    },
    doFloorChange(e){
    },
    async getRoomList(floorIds) {
      let objArr = []
      this.rooms = []
      this.dataform.roomIds = []
      const res = await getRoomByFloorIds({
        floors: floorIds.toString()
      })
      if(res.data.code==0){
        res.data.data.forEach(el=>{
          el.roomList.forEach(el2=>{
            objArr.push(
              {
                key: el2.roomId,
                label: el2.roomName+''
              }
            )
          })
        })
        this.rooms = objArr
      }else{
        this.$message.error(res.data.msg);
      }
    },
    initData() {
      this.setFormVisible = this.visible;
    },
    async getDetail() {
      if(this.row&&this.row.id){
        this.dataform = Object.assign({},this.row)
      }
    },
    resetSetForm(formName) {
      this.setFormVisible = false;
      this.setLoading = false;
      this.initForm()
      // this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    editSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid){
          if(this.dataform.roomIds.length===0){
            this.$message.error('请选择公摊房间');
            return
          }
          commonsdSave(this.dataform).then(res => {
            if(res.data.code==0){
              this.resetSetForm(formName)
              this.$emit("dlgdoSuccess");
              this.$notify({
                title: '成功',
                message: this.mTitle,
                type: 'success'
              });
            }
          });
        } else {
          return false
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.gtcb_form ::v-deep {
  .el-dialog__body {
    padding: 10px 0 0 0;
  }
  .el-form--inline .el-form-item{
    margin-right: 0;
  }
  .el-form--inline .m_ipt .el-form-item__content{
    width: 250px;
  }
  .trans_ipt{
    width: 100%;
  }
  .el-form--inline .trans_ipt .el-form-item__content{
    width: 100%;
  }
  .sdcb_tp {
    border-bottom: 10px solid #f0f2f5;
    padding: 0 20px;
    >div{
      display: flex;
      justify-content: space-between;
    }
    .sdcbt_b{
      margin-bottom: 15px;
      .el-radio-button__orig-radio:checked+.el-radio-button__inner{
        color: #fff;
        background: #ed6d00;
        border-color: #ed6d00;
        box-shadow: -1px 0 0 0 #ed6d00;
      }
      .el-radio-button__inner:hover{
        color: #ed6d00;
        border-color: #ed6d00;
        box-shadow: -1px 0 0 0 #ed6d00;
      }
    }
  }
  .sdcb_btm {
    padding: 10px 20px 10px;
    margin-bottom: 15px;
    border-bottom: 1px solid #EEF0F2;
    .sdcb_tb{
      .sdcbt_row, .sdcbt_t{
        display: flex;
        height: 40px;
        div{
          line-height: 40px;
          text-align: center;
        }
        div:not(.c1){
          flex: 1;
        }
        .c1{
          width: 90px;
        }
      }
      .sdcbt_row{
        height: 50px;
        .row_ipt{
          width: 150px;
        }
        .el-input--mini .el-input__inner{
          text-align: center;
          background: #fafafa;
          border: 1px solid #dcdfe6;
          border-radius: 0;
        }
      }
    }
  }
  .sdcb_trans{
    padding: 20px;
    p{
      color: #333;
      margin-bottom: 15px;
      .tip{
        font-size: 14px;
        color: #999;
        padding-left: 30px;
      }
    }
    .el-transfer{
      display: flex;
      justify-content: space-between;
      align-items: center;
      .el-transfer-panel{
        width: 320px;
      }
      .el-button + .el-button {
        margin-left: 0;
      }
    }
  }
}
</style>
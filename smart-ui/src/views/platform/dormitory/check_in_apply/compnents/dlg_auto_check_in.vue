<!--退回-->
<template>
  <el-dialog
    title="自动分配"
    class="dialog_form auto_check_in"
    width="500px"
    :visible.sync="setFormVisible"
  >
    <div class="cont">
      <div style="margin-bottom: 10px;">系统自动分配信息如下：</div>
      <div>
        <div class="row">
          <span class="label">姓名</span>
          <span class="val">{{row.staffName}}</span>
        </div>
        <div class="row">
          <span class="label">楼栋</span>
          <span class="val">{{row.dormitoryName}}</span>
        </div>
        <div class="row">
          <span class="label">楼层</span>
          <span class="val">{{row.floorName}}</span>
        </div>
        <div class="row">
          <span class="label">房间号</span>
          <span class="val">{{row.roomName}}</span>
        </div>
        <div class="row">
          <span class="label">床位</span>
          <span class="val">{{row.bedName}}</span>
        </div>
        <div class="row">
          <span class="label">房间类型</span>
          <span class="val">{{row.roomTypeName}}</span>
        </div>
      </div>
      <div class="tip2">确认无误后，员工将分配到此房间</div>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm()" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        @click="editSubmit()"
        :loading="setLoading"
        >确 定</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { recommend, applyManual } from "../_service.js";
export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    row: undefined,
  },
  watch: {
    visible(newVal, oldVal) {
      this.setFormVisible = newVal;
      // this.recommend()
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
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
    async editSubmit() {
      const res =  await applyManual({
        bedId: this.row.bedId,
        applyId: this.row.applyId
      });
      if(res.data.code==0){
        this.$notify({
          title: '成功',
          message: '自动分配成功',
          type: 'success'
        });
        this.$emit("dlgdoSuccess");
      }
    },
    async recommend() {
      const res = recommend({applyId: this.row.id})
    },
    resetSetForm() {
      this.setFormVisible = false;
    }
  },
};
</script>
<style lang="scss" scoped>
.auto_check_in ::v-deep {
  .el-dialog__body{
    text-align: center;
  }
  .cont{
    text-align: left;
    display: inline-block;
    font-size: 14px;
  }
  .tip2{
    margin: 30px 0 20px 0;
    font-size: 12px;
    color: #ffa271;
  }
  .row{
    display: flex;
    margin-bottom: 10px;
    .label{
      width: 70px;
      color: #666;
    }
    .val{
      color: #333;
      padding-left: 15px;
    }
  }
}
</style>
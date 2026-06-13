<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="900px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <el-form :rules="rules" ref="form" :model="addform" label-width="130px">
      <el-form-item label="授权人员" prop="badges" v-if="!isEdit">
        <el-input v-model="personNum" placeholder="输入姓名或工号,然后查询" clearable @keyup.enter.native="selectPerson">
          <el-button slot="append" class="append-button" @click="selectPerson" icon="el-icon-search"></el-button>
        </el-input>
        <div>
          <div v-if="personInfos && personInfos.length>0 " class="tip2">人员卡号、指纹、密码均为空者 不可勾选</div>
          <el-checkbox-group v-model="addform.badges">
            <el-checkbox
              v-for="(item, index) in personInfos"
              :key="index"
              :label="item.personNum"
              :disabled="!item.cardNo && !item.fingerCodeList && !item.password"
            >{{item.personNum}}-{{item.personName}}</el-checkbox>
          </el-checkbox-group>
        </div>
      </el-form-item>
      <el-form-item label="授权设备" prop="deviceIds" v-if="!isEdit">
          <!-- :filter-method="filterMethod" -->
          <!-- filterable -->
        <el-transfer
          filter-placeholder="请输入设备名称"
          :titles="['设备列表', '已选中']"
          :props="{
            key: 'id',
            label: 'deviceName'
          }"
          v-model="addform.deviceIds"
          :data="deviceList">
        </el-transfer>
      </el-form-item>
      <el-form-item label="授权有效期设置" prop="validTimeStart">
        <div>
          <el-radio-group v-model="isLong">
            <el-radio :label="1">指定时间</el-radio>
            <el-radio :label="2">长期有效</el-radio>
          </el-radio-group>
          <el-date-picker
            v-if="isLong===1"
            v-model="times"
            type="datetimerange"
            range-separator="-"
            :default-time="['00:00:00', '23:59:59']"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="pickerOptions"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            clearable>
          </el-date-picker>
        </div>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { authApi } from '../_service'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    var vPeriodTime = (rule, value, callback) => {
      if (this.isLong ===1 ) {
        if(!value){
          callback(new Error('请选择时间'))
        }else{

          callback()
        }
      } else {
        callback()
      }
    }
    return {
      isEdit: false,
      btnLoading: false,
      currVisible: false,
      isLong: 2, // 2 长期有效，1 指定时间 （只有开始时间和结束时间同时有值才说指定时间，其他是长期有效）
      isLongInit: false, //编辑时，如果原来就是长期有效，这不可以再选择长期有效进行保存
      times: [],
      personNum: undefined,
      personInfos: [],
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7
        }
      },
      addform: {
        badges: [],
        deviceIds: [],
        validTimeStart: undefined,
        validTimeEnd: undefined
      },
      rules: {
        badges: [
          tce.helper.formRules.vempty({
            message: '请查询并勾选授权人员'
          }),
          tce.helper.formRules.vlimitArray({
            min: 1,
            message: '请查询并勾选授权人员'
          })
        ],
        deviceIds: [
          tce.helper.formRules.vempty({
            message: '请勾选授权设备'
          }),
          tce.helper.formRules.vlimitArray({
            min: 1,
            message: '请勾选授权设备'
          })
        ],
        validTimeStart: [{ validator: vPeriodTime, trigger: 'change' }]
      },
      deviceList: [],
    }
    // filterMethod(query, item) {
    //     return item.pinyin.indexOf(query) > -1;
    //   }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    },
    isLong(val){
      if(val ===2){
        this.times = []
      }
    },
    times(val){
      if(val && val.length>0){
        this.addform.validTimeStart = val[0]
        this.addform.validTimeEnd = val[1]
      }else{
        this.addform.validTimeStart = undefined
        this.addform.validTimeEnd = undefined
      }
    },
    itemObj:{
      handler(newV){
        if( newV && newV.id ){
          this.isEdit = true
          this.addform.id = newV.id
          if(newV.validTimeStart && newV.validTimeEnd){
            this.times = [newV.validTimeStart, newV.validTimeEnd]
            this.isLong = 1
            this.isLongInit = false
          }else{
            this.isLong = 2
            this.isLongInit = true
          }
        }else{
          this.isEdit = false
        }
      },
      immediate: true
    }
  },
  created(){
    this.getDevices()
  },
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
    /**
     * 提交
     */
    async formSumit() {
      if(this.isEdit){
        if(this.isLongInit && this.isLong===2){
          this.$message.error('重复授权，请重新选择时效或者直接取消编辑');
          return
        }
        this.editSubmit()
      }else{
        this.addSubmit()
      }
    },
    /**
     * 添加
     */
    async addSubmit(){
      await this.validateForm()
      this.btnLoading = true
      const res =  await authApi.addObjBatch(this.addform)
      if(res.data.code === 200){
        this.$message.success('授权成功！');
        this.refresh()
        this.btnLoading = false
      }else{
        this.btnLoading = false
        this.$message.error(res.data.message);
      }
    },
    /**
     * 编辑
     */
    async editSubmit(){
      await this.validateForm()
      this.btnLoading = true
      const res =  await authApi.putObj(this.addform)
      if(res.data.code === 200){
        this.$message.success('编辑成功！');
        this.refresh()
        this.btnLoading = false
      }else{
        this.btnLoading = false
        this.$message.error(res.data.message);
      }
    },
    /**
     * 查询
     */
    async selectPerson(){
      this.personInfos = []
      if(!this.personNum){
        this.$message.error('请输入工号或者姓名');
        return
      }
      const res = await authApi.checkPerson(this.personNum)
      if(res.data.code===200){
        this.personInfos = res.data.data
        if(!(this.personInfos && this.personInfos.length>0)){
          this.$message.error('没有查到对应信息，请核对后重新输入');
        }
      }
    },
    /**
     * 获取设备列表
     */
    async getDevices(){
      this.deviceList = []
      const res = await authApi.getLockData(
        {
          current: 1,
          size: 1000
        }
      )
      this.deviceList = res.data.data.records
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.personInfos = []
      this.personNum = undefined
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.personInfos = []
      this.personNum = undefined
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .theme-yutong .append-button:active {
      background-color: transparent;
      border-color: transparent;
      color: inherit;
  }
  .theme-yutong .append-button:hover {
      background-color: transparent;
      border-color: transparent;
      color: inherit;
  }
  .tip2{
    font-size: 12px;
    font-weight: bold;
    color: red;
  }
</style>

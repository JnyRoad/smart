<template>
  <el-dialog
    ref="dialog"
    title="导入抄表数据"
    :visible.sync="currVisible"
    width="700px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <section class="my-basic-inner">
      <div class="step1">
        <div class="step1Info">
          <p class="box-orange">下载模板</p>
          <el-form ref="form" :inline="false" :model="searchform" :rules="rules" size="mini" label-width="80px">
            <el-form-item label="园区" prop="parkId">
              <parkSelect v-model="searchform.parkId" :defaultSelected="true" @doChange="parkChange" @defaultHandle="defaultHandle"></parkSelect>
            </el-form-item>
            <el-form-item label="楼栋" prop="dormitoryIds">
              <dormMultiSelect
                :parkId="searchform.parkId"
                v-model="searchform.dormitoryIds"
                :placeholder="'可多选'"
              ></dormMultiSelect>
            </el-form-item>
            <el-form-item label="抄表月份" prop="meterMonth">
              <el-date-picker v-model="searchform.meterMonth" type="month" value-format="yyyy-MM" placeholder="选择月份" :clearable="false"></el-date-picker>
            </el-form-item>
            <div class="tips" style="margin-bottom: 10px;"> 请选择您负责的楼栋后下载模板，系统会自动生成房间号，您只需填写当月的水电后，把文件上传即可 </div>
            <el-button type="primary" @click="download" icon="icon-yutong-download" :loading="downloadLoading"> 下载模板 </el-button>
          </el-form>
        </div>
        <p class="box-orange">导入水电数据</p>
        <el-form ref="form2" :inline="false" :model="importform" :rules="rules2" size="mini" label-width="80px">
          <el-form-item label="抄表月份" prop="meterMonth">
            <el-date-picker v-model="importform.meterMonth" type="month" value-format="yyyy-MM" placeholder="选择月份" :clearable="false"></el-date-picker>
          </el-form-item>
          <div v-if="hasFile" class="fileInfo">
            <i class="el-icon-document"></i>
            {{ myFile.name }}
          </div>
          <div class="btns">
            <input type="file" ref="file" @change="fileChange" accept=".xls,.xlsx" style="display: none;" />
            <el-button type="primary" @click="upload" :plain="hasFile" icon="icon-yutong-upload" v-if="!loading">
              <template v-if="!hasFile">上传Excel</template>
              <template v-else>重新上传</template>
            </el-button>
            <el-button type="primary" @click.stop="formSumit" v-if="hasFile" :loading="loading">下一步</el-button>
            <span class="tips" style="padding-left: 20px;" v-if="!hasFile">请选择要上传的水电模板</span>
          </div>
        </el-form>
      </div>
    </section>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import dormMultiSelect from "@/views/platform/components/dorm-multi-select/index";
import  {getTemplate, importMeterData } from '../../_service'
import { getDateMonth } from "@/util/util";
export default {
  components: {
    dormMultiSelect
  },
  data() {
    return {
      searchform: {
        parkId: '',
        dormitoryIds: [],
        meterMonth: '',
      },
      importform: {
        meterMonth: '',
      },
      rules: {
        parkId: { required: true, message: '请选择园区', trigger: 'change' },
        dormitoryIds: { required: true, message: '请选择楼栋', trigger: 'change' },
        meterMonth: { required: true, message: '请选择抄表月份', trigger: 'change' }
      },
      rules2: {
        meterMonth: { required: true, message: '请选择抄表月份', trigger: 'change' }
      },
      downloadLoading: false,
      loading: false,
      currVisible: false,
      hasFile: false,
      myFile: {}
    }
  },
  props: {
    visible: Boolean
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        this.doClearValidate("form");
      }
    }
  },
  methods: {
    parkChange(){
      this.searchform.dormitoryIds = []
    },
    async download(){
      await this.validateForm('form')
        //   + '-01'
      this.downloadLoading = true
      let obj = {
        dormitoryIds: this.searchform.dormitoryIds.toString(),
        meterMonth: this.searchform.meterMonth + '-01'
      }
      const res = await getTemplate(obj)
      // 由于返回结果是arraybuffer类型，获取不到提示的message信息  转换一下
      // arraybuffer→json
      let resBlob = new Blob([res.data])
      let reader = new FileReader()
      reader.readAsText(resBlob, 'utf-8')
      reader.onload = () => {
        try{
          this.downloadLoading = false
          let res2 = JSON.parse(reader.result)
          if(res2.code!==0){
            this.$message.error(res2.message)
          }
        }catch(e){
          var fileDownload = require("js-file-download")
          fileDownload(res.data, obj.meterMonth+'水电模板.xls')
          this.downloadLoading = false
        }
      }
    },
    defaultHandle(e){
      this.searchform.parkId = e.value
    },
    upload() {
      this.$refs.file.click();
    },
    fileChange(e) {
      if (e.target.files.length > 0) {
        let file = e.target.files[0];

        var suffixPosition = file.name.lastIndexOf(".") + 1;
        var nums = file.name.length - suffixPosition;
        var suffix = file.name.substr(suffixPosition, nums); //文件后缀

        //1、判断所选文件格式
        const isExcel = suffix == "xlsx" || suffix == "xls";
        if (!isExcel) {
          //
          this.$message({
            message: '请上传excel文件，文件后缀为 "xlsx" 或 "xls"！',
            type: "warning"
          });
          return;
        }
        this.myFile = file
        this.hasFile = true;
      }
    },
    /**
     * 提交
     */
    async formSumit() {
      await this.validateForm('form2')
      if(!this.hasFile){
        this.$message.error(`请上传文件`)
      }
      try {
        this.loading = true
        const formData = new FormData()
        formData.append('multipartFile', this.myFile)
        formData.append('meterMonth', this.importform.meterMonth + '-01')

        const res = await importMeterData(formData)

        //由于返回结果是arraybuffer类型，获取不到提示的message信息  转换一下
        // arraybuffer→json
        let resBlob = new Blob([res.data])
        let reader = new FileReader()
        reader.readAsText(resBlob, 'utf-8')
        reader.onload = () => {
          try{
            let res2 = JSON.parse(reader.result)
            if(res2.code!==0){
              this.$message.error(res2.message)
              this.loading = false
            }else{
              this.$message({
                message: '导入成功',
                type: 'success'
              })
              this.loading = false
              this.refresh()
            }
          }catch(e){
            // 导入接口成功后，有可能返回失败名单
            this.$message.error('水电数据导入存在失败信息!')

            var fileDownload = require("js-file-download");

            fileDownload(res.data, '水电数据导入失败清单.xls');

            this.loading = false
            this.refresh()
          }
        }
      } catch (error) {
        this.loading = false
      }
    },
    /**
     * 验证表单
     */
    validateForm(formName) {
      if (this.$refs[formName]) {
        return this.$refs[formName].validate()
      }
      return Promise.resolve()
    },
    refresh() {
      this.$emit('refresh')
      this.close()
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
      this.hasFile = false
      this.myFile = {}
      this.$refs.file.value =''
      this.currVisible = false
    }
  },
  mounted() {
    this.searchform.meterMonth = getDateMonth()
    this.importform.meterMonth = getDateMonth()
  }
}
</script>

<style lang="scss" scoped>
  .step1Info{
    margin-bottom: 20px;
  }
  .box-orange{
    margin-bottom: 20px;
  }
  .fileInfo{
    padding-bottom: 20px;
  }
  .tips{
    color: #999;
    font-size: 12px;
  }
</style>

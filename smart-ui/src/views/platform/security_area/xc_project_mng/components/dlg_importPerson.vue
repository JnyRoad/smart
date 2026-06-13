<template>
  <el-dialog
    ref="dialog"
    title="导入人员"
    :visible.sync="currVisible"
    width="600px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div>
      <section class="my-basic-inner">
        <div class="step1">
          <div class="step1Info">
            下载导入模板，填写人员
            <a href="/resource/person_to_security_project.xlsx" target="__blank">下载模板</a>
          </div>
          <div class="tips">
            <p class="tipTitle">导入须知：</p>
            <p>导入的文件格式支持 'xlsx' 和 'xls' 两种格式</p>
            <!-- <p>2、导入会覆盖原有员工的信息，如需更新已存在的员工，请先导出员工，在导出表格里进行修改</p> -->
          </div>
          <div class="btns">
            <input type="file" ref="file" @change="fileChange" accept=".xls,.xlsx" style="display: none;" />
            <el-button type="primary" @click="upload" :plain="hasFile" icon="icon-yutong-upload">
              <template v-if="!hasFile">上传Excel</template>
              <template v-else>重新上传</template>
            </el-button>
          </div>
          <div v-if="hasFile" class="fileInfo">
            <i class="el-icon-document"></i>
            {{ myFile.name }}
          </div>
        </div>
      </section>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading" :disabled="!dataList||dataList.length===0">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { xcProjectApi } from '../_service'
import XLSX from "xlsx"
export default {
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      hasFile: false,
      myFile: {},
      dataList: []
    }
  },
  props: {
    visible: Boolean,
    securityId: [String, Number],
    parkId: [String, Number]
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
    }
  },
  methods: {
    upload() {
      this.$refs.file.click();
    },
    fileChange(e) {
      let _this = this;
      if (e.target.files.length > 0) {
        let file = e.target.files[0];

        var suffixPosition = file.name.lastIndexOf(".") + 1;
        var nums = file.name.length - suffixPosition;
        var suffix = file.name.substr(suffixPosition, nums); //文件后缀

        //1、判断所选文件格式
        const isExcel = suffix == "xlsx" || suffix == "xls";
        if (!isExcel) {
          //
          _this.$message({
            message: '请上传excel文件，文件后缀为 "xlsx" 或 "xls"！',
            type: "warning"
          });
          return;
        }

        var fileReader = new FileReader();
        fileReader.onload = function (ev) {
          try {
            var data = ev.target.result,
              workbook = XLSX.read(data, {
                type: "binary"
              }), // 以二进制流方式读取得到整份excel表格对象
              persons = []; // 存储获取到的数据
          } catch (e) {
            return;
          }
          // 表格的表格范围，可用于判断表头数量是否正确
          var fromTo = "";

          //2、确保上传文件内只有一张工作表
          if (workbook.Props.Worksheets > 1) {
            _this.$message({
              message: "请确保上传文件内只有一张工作表！",
              type: "warning"
            });
            return;
          }

          // 遍历每张表读取
          for (var sheet in workbook.Sheets) {
            if (workbook.Sheets.hasOwnProperty(sheet)) {
              fromTo = workbook.Sheets[sheet]["!ref"];
              persons = persons.concat(
                XLSX.utils.sheet_to_json(workbook.Sheets[sheet])
              );

              break; // 如果只取第一张表，就取消注释这行
            }
          }

          // 3、确保上传文件内存在有效数据
          if (_this.validatenull(persons)) {
            _this.$message({
              message: "请确保上传文件内存在有效数据！",
              type: "warning"
            });
            return;
          }

          // 对数据进行清洗
          for (var {
            ["工号"]: staffBadge,
            ["姓名"]: staffName
          } of persons) {
            if (!staffBadge || !staffName ) {
              _this.$message({
                message: "文件内数据存在空值项！",
                type: "warning"
              });
              throw new Error('NOT VERIFICATION')
            }
            _this.dataList.push({
              staffBadge: staffBadge,
              staffName: staffName,
              securityId: _this.securityId,
              parkId: _this.parkId
            })
          }
          _this.hasFile = true;
          _this.myFile = file;
          _this.$emit('input', _this.dataList)
        };

        // 以二进制方式打开文件
        fileReader.readAsBinaryString(file);
      }
    },
     /**
     * 提交
     */
    async formSumit() {
      await this.$confirm('确认批量导入？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      this.btnLoading = true
      try{
        const res = await xcProjectApi.importPersonToProject(this.dataList)
        if (res.data.code === 0) {
          this.$message({
            showClose: true,
            message: '导入成功',
            type: 'success'
          })
          this.refresh()
        } else {
          this.$message({
            message: res.data.message,
            type: 'error'
          })
        }
        this.btnLoading = false
      }catch(err){
        this.btnLoading = false
      }
    },
    initData(){
      this.hasFile = false
      this.myFile = {}
      this.dataList = []
      if(this.$refs.file){
        this.$refs.file.value = ""
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.initData()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.initData()
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
.stepOuter {
  color: #ed6d00;
  margin: 0 auto;
  padding: 20px 0 20px 0;
}
.tips {
  .tipTitle {
    color: red;
    font-size: 14px;
  }
  color: #666;
  font-size: 12px;
  line-height: 25px;
}
.btns {
  text-align: center;
  padding-top: 10px;
}
.step1 ::v-deep {
  margin: 0 auto;
  .step1Info {
    text-align: center;
    line-height: 30px;
    a {
      color: #ed6d00;
      text-decoration: underline;
    }
  }
  .el-icon-document {
    font-size: 20px;
    margin: -2px 5px 0 0;
  }
  .fileInfo {
    width: 245px;
    margin: 20px auto;
    text-align: center;
    padding: 30px 10px;
    background: #fafafa;
  }
}
</style>

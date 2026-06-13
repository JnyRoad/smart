<!--入住信息 批量导入 -->
<template>
  <el-scrollbar :native="false">
    <section class="my-basic-inner">
      <!-- <div class="stepOuter">
        <el-steps :active="active" align-center>
          <el-step title="STP 1" description="导入Excel数据"></el-step>
          <el-step title="STP 2" description="批量导入"></el-step>
        </el-steps>
      </div> -->
      <div class="step1">
        <div class="step1Info">
          下载导入模板，填写入住信息
          <a href="/resource/dormIn_template.xlsx" target="__blank">下载模板</a>
        </div>
        <div class="tips">
          <p class="tipTitle">导入须知：</p>
          <p>①、请确保excel文件内只存在一张有效工作表</p>
          <p>②、导入的文件格式支持 'xlsx' 和 'xls' 两种格式</p>
          <!-- <p>2、导入会覆盖原有员工的信息，如需更新已存在的员工，请先导出员工，在导出表格里进行修改</p> -->
        </div>
        <div class="btns">
          <input type="file" ref="file" @change="fileChange" accept=".xls,.xlsx" style="display: none;" />
          <el-button type="primary" @click="upload" :plain="hasFile" icon="icon-yutong-upload" v-if="!loading">
            <template v-if="!hasFile">上传Excel</template>
            <template v-else>重新上传</template>
          </el-button>
          <el-button type="primary" @click.stop="next" v-if="hasFile" :loading="loading">下一步</el-button>
        </div>
        <div v-if="hasFile" class="fileInfo">
          <i class="el-icon-document"></i>
          {{ myFile.name }}
        </div>
      </div>
    </section>
  </el-scrollbar>
</template>

<script>
import { dateFormat2 } from "@/util/date";
import { importBatchExcel } from "@/api/platform/dormitory/bed_mng";
export default {
  name: "parking",
  data() {
    return {
      loading: false,
      active: 0,
      hasFile: false,
      myFile: {}
    };
  },
  props: {
    dormitoryId: [String, Number]
  },
  methods: {
    formatDate(numb, format) {
      const time = new Date((numb - 1) * 24 * 3600000 + 1)
      time.setYear(time.getFullYear() - 70)
      const year = time.getFullYear() + ''
      const month = time.getMonth() + 1 + ''
      const date = time.getDate() - 1 + ''
      if (format && format.length === 1) {
        return year + format + month + format + date
      }
      return year + (month < 10 ? '0' + month : month) + (date < 10 ? '0' + date : date)
    },
    dateFormat2(val) {
      if (!this.validatenull(val)) {
        return dateFormat2(new Date(val));
      }
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
      if(!this.hasFile){
        this.$message.error(`请上传文件`)
      }
      try {
        this.loading = true
        const formData = new FormData()
        formData.append('filename', this.myFile)
        formData.append('dormId', this.dormitoryId)

        const res = await importBatchExcel(formData)

        // 报500时 由于返回结果是arraybuffer类型，获取不到提示的message信息  转换一下
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
              this.$emit('complete')
            }
          }catch(e){
            // 导入接口成功后，有可能返回失败名单
            this.$message.error('存在导入失败的信息!')
            // let contentDisposition = res.headers['content-disposition']
            // console.log('contentDisposition---',contentDisposition)
            // let fileName = contentDisposition.substring(contentDisposition.indexOf('filename=') + 9)
            // console.log('filename---',fileName)
            // console.log('decodeURIComponent(filename)---',decodeURIComponent(fileName))

            var fileDownload = require("js-file-download");

            // fileDownload(res.data, decodeURIComponent(fileName));
            fileDownload(res.data, '入住信息导入失败清单.xls');

            this.loading = false
            this.$emit('complete')
          }
        }
      } catch (error) {
        this.loading = false
      }
    },
    //导出
    export2Excel(dList) {
      require.ensure([], () => {
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "园区名称",
          "楼栋名称",
          "楼层",
          "房间号",
          "房间分类",
          "性别",
          "床位编号",
          "工号",
          "姓名",
          "入住时间",
          "备注",
        ];
        const filterVal = [
          "parkName",
          "dormitoryName",
          "floorName",
          "roomName",
          "roomType",
          "staffSex",
          "bedNumber",
          "staffBadge",
          "name",
          "createTime",
          "mark"
        ];
        dList.forEach(el=>{
          el.createTime = this.dateFormat2(el.createTime)
        })
        const data = this.formatJson(filterVal, dList);
        export_json_to_excel(tHeader, data, "导入失败信息");
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    next() {
      this.formSumit()
    }
  }
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

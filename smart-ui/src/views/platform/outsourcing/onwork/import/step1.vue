<!--招聘管理，批量入职管理 -->
<template>
  <el-scrollbar :native="false">
    <section class="my-basic-inner">
      <div class="stepOuter">
        <el-steps :active="active" align-center>
          <el-step title="STP 1" description="导入Excel数据"></el-step>
          <el-step title="STP 2" description="批量导入"></el-step>
        </el-steps>
      </div>
      <div class="step1">
        <div class="step1Info">
          下载导入模板，填写人员
          <a href="/resource/staff-temp.xlsx" target="__blank">下载模板</a>
        </div>
        <div class="tips">
          <p class="tipTitle">导入须知：</p>
          <p>1、导入的文件格式支持 'xlsx' 和 'xls' 两种格式</p>
          <p>2、导入会覆盖原有员工的信息，如需更新已存在的员工，请先导出员工，在导出表格里进行修改</p>
        </div>
        <div class="btns">
          <input type="file" ref="file" @change="fileChange" accept=".xls,.xlsx" style="display: none;" />
          <el-button type="primary" @click="upload" :plain="hasFile" icon="icon-yutong-upload">
            <template v-if="!hasFile">上传Excel</template>
            <template v-else>重新上传</template>
          </el-button>
          <el-button type="primary" @click.stop="next" v-if="dataList.length">下一步</el-button>
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
import XLSX from "xlsx"
import { isMobile, cardid } from '@/util/validate'
export default {
  name: "parking",
  data() {
    return {
      active: 0,
      hasFile: false,
      myFile: {},
      dataList: []
    };
  },
  methods: {
    formatDate(numb, format) {
      const time = new Date((numb - 1) * 24 * 3600000 + 1)
      time.setYear(time.getFullYear() - 70)
      const year = time.getFullYear() + ''
      const month = time.getMonth() + 1 + ''
      const date = time.getDate() + ''
      if (format && format.length === 1) {
        return year + format + month + format + date
      }
      return year + (month < 10 ? '0' + month : month) + (date < 10 ? '0' + date : date)
    },
    vCardId(value) {
      if(value.includes(' ')){
        this.$message({
          message: "证件号码不能包含空格！",
          type: "warning"
        });
        return false
      }
      let codeReg = new RegExp("[\u4E00-\u9FA5]+") //正则 不能输入汉字
      let len = value.length,
      str='';
      for(var i=0;i<len;i++){
        if(codeReg.test(value[i])){
          str+=value[i];
        }
      }
      if(str.length>0){
        this.$message({
          message: "证件号码不能包含汉字！",
          type: "warning"
        });
        return false
      }else{
        if(len>7 && len<20){
          return true
        }else{
          this.$message({
            message: "证件号码需在8~20位之间",
            type: "warning"
          });
          return false
        }
      }
    },
    upload() {
      this.$refs.file.value = '';
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
            ["姓名"]: name,
            ["岗位"]: post,
            ["工号"]: jobNumber,
            ["手机号"]: phone,
            ["职层"]: rank,
            ["身份证号"]: identity,
            ["部门"]: department,
            ["入职日期"]: entryTime,
            ["派遣单位"]: dispatch
          } of persons) {
            if (!name || !post || !jobNumber || !phone || !rank || !identity || !department || !entryTime) {
              _this.$message({
                message: "文件内数据存在空值项！",
                type: "warning"
              });
              throw new Error('NOT VERIFICATION')
            }
            // 校验工号
            let codeReg = new RegExp("[A-Za-z0-9]+") //正则 英文+数字；
            let len = jobNumber.length,
            str='';
            for(var i=0;i<len;i++){
              if(!codeReg.test(jobNumber[i])){
                str+=jobNumber[i];
              }
            }
            if(str.length>0){
              _this.$message({
                message: `${jobNumber} 工号只能输入字母和数字`,
                type: "warning"
              });
              throw new Error('NOT VERIFICATION JOB NUMBER')
            }else{
              if(len<6 || len >12){
                _this.$message({
                  message: `${jobNumber} 工号长度在6~12位之间`,
                  type: "warning"
                });
                throw new Error('NOT VERIFICATION JOB NUMBER')
              }
            }
            // 校验手机号
            if (!isMobile(Number(phone))) {
              _this.$message({
                message: `${phone} 手机号格式错误`,
                type: "warning"
              });
              throw new Error('NOT VERIFICATION PHONE')
            }
            if(typeof(entryTime)!=='string'){
              entryTime = _this.formatDate(entryTime,'-')
            }else{
              if(entryTime.indexOf('/')>-1){
                entryTime = entryTime.replace(/\//g,'-')
              }
            }
            entryTime = entryTime + ' 00:00:00'
            if(!_this.vCardId(identity)){
              return
            }
            // let r = cardid(identity)
            // if (r[0]) {
            //   _this.$message({
            //     message: `${identity} 身份证格式错误`,
            //     type: "warning"
            //   });
            //   throw new Error('NOT VERIFICATION IDENTITY')
            // }
            // return
            _this.dataList.push({
              name: name,
              post: post,
              jobNumber: jobNumber,
              phone: phone,
              rank: rank,
              identity: identity,
              department: department,
              entryTime: entryTime,
              dispatch: dispatch
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
    next() {
      if (this.dataList.length) {
        this.$emit('next')
      }
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

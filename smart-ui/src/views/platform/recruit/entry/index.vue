<!--招聘管理，批量入职管理 -->
<template>
  <div class="my-basic-container parking">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="stepOuter">
          <el-steps :active="active" align-center>
            <el-step title="STP 1" description="导入Excel数据"></el-step>
            <el-step title="STP 2" description="批量入职登记"></el-step>
          </el-steps>
        </div>
        <div class="step1">
          <div class="step1Info">
            上传新员工登记信息excel，核实信息无误后，进入第二步 如果没有导入模板，请下载
            <a
              ref="downloadTemp"
              href="/resource/template.xlsx"
            ></a>
            <el-button type="text" @click="download">《新员工登记信息》</el-button>
          </div>
          <div class="tips">
            <p class="tipTitle">导入须知：</p>
            <p>1、导入的文件格式支持 'xlsx' 和 'xls' 两种格式</p>
            <p>2、请确保导入时使用的模板跟下载的模板一致，不要更改模板内标题字段的名称</p>
            <p>3、请确保导入时使用的模板内只有一张工作表</p>
            <p>4、请确保日期相关数据值使用 'yyyy-mm-dd' 格式</p>
            <p>5、请确保上传文件内证件号唯一（工作表内不能同时出现多个相同证件号的数据）</p>
            <p>6、为保证上传数据的完整性，请确保每条数据不存在空值</p>
          </div>
          <div class="btns">
            <input type="file" ref="file" @change="fileChange" style="display: none;" />
            <el-button type="primary" @click="upload" :plain="hasFile" icon="icon-yutong-upload">
              <template v-if="!hasFile">上传Excel</template>
              <template v-else>重新上传</template>
            </el-button>
            <el-button type="primary" @click.stop="next" v-if="hasFile">下一步</el-button>
          </div>
          <div v-if="hasFile" class="fileInfo">
            <i class="el-icon-document"></i>
            {{myFile.name}}
          </div>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { tableOption } from "@/const/crud/platform/recruit/entry";
import { mapGetters } from "vuex";
import XLSX from "xlsx";
// import templateOption from '@/resource/template.xlsx'

export default {
  name: "parking",
  data() {
    return {
      active: 0,
      // template: templateOption,
      hasFile: false,
      myFile: {}
    };
  },
  created: function() {},
  mounted: function() {},
  watch: {
    selectStaffs: function(val) {
      val.length > 0 ? (this.hasSelect = true) : (this.hasSelect = false);
    }
  },
  computed: {
    ...mapGetters(["permissions"])
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
        fileReader.onload = function(ev) {
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
          let arr = [];
          let certnoArr = [];
          let certnoSameArr = [];
          for (var {
            ["姓名"]: name,
            ["性别"]: sex,
            ["民族"]: nation,
            ["出生日期"]: birth,
            ["证件号"]: certno,
            ["户籍地址"]: homeAddress,
            ["签发机关"]: police,
            ["证件有效期（起）"]: validDateFm,
            ["证件有效期（止）"]: validDate
          } of persons) {
            if (_this.validatenull(name)) {
              _this.$message({
                message: '"姓名"列的标题应为 "姓名"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            if (_this.validatenull(sex)) {
              _this.$message({
                message:
                  '"性别"列的标题应为 "性别" 两个字，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            } else {
              if (sex == "男") {
                sex = 0;
              } else if (sex == "女") {
                sex = 1;
              } else {
                _this.$message({
                  message:
                    '请检查"性别"一列的数据值是否为"男"或"女"，且前后不要有空格！',
                  type: "warning"
                });
                return;
              }
            }

            if (_this.validatenull(nation)) {
              _this.$message({
                message: '"民族"列的标题应为 "民族"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            if (_this.validatenull(birth)) {
              _this.$message({
                message:
                  '"出生日期"列的标题应为 "出生日期"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            if (_this.validatenull(certno)) {
              _this.$message({
                message:
                  '"证件号"列的标题应为 "证件号"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            if (_this.validatenull(homeAddress)) {
              _this.$message({
                message:
                  '"户籍地址"列的标题应为 "户籍地址"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            if (_this.validatenull(police)) {
              _this.$message({
                message:
                  '"签发机关"列的标题应为 "签发机关"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            if (_this.validatenull(validDateFm)) {
              _this.$message({
                message:
                  '"证件有效期（起）"列的标题应为 "证件有效期（起）"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            if (_this.validatenull(validDate)) {
              _this.$message({
                message:
                  '"证件有效期（止）"列的标题应为 "证件有效期（止）"，且前后和中间不要有空格！',
                type: "warning"
              });
              return;
            }

            certno = certno.toUpperCase().replace(/(\s+)/g, ""); //转大写、去除全部空格

            if (certnoArr.indexOf(certno) == -1) {
              certnoArr.push(certno);
            } else {
              certnoSameArr.push(certno);
            }

            arr.push({
              name: name,
              sex: sex,
              nation: nation,
              birth: birth,
              certno: certno,
              homeAddress: homeAddress,
              police: police,
              validDateFm: validDateFm,
              validDate: validDate
            });
          }
          // 4、如果存在相同证件号，则需删除重复项后重新上传
          if (certnoSameArr.length > 0) {
            _this.$message({
              message: "检测到有相同证件号的数据，请删除重复项再重新上传！",
              type: "warning"
            });
            return;
          }

          _this.hasFile = true;
          _this.myFile = file;

          localStorage.setItem("entryFileInfo", JSON.stringify(arr));
        };

        // 以二进制方式打开文件
        fileReader.readAsBinaryString(file);
      }
    },
    next(e) {
      this.active = 1;
      const src = `/platform/recruit/entry/step2`;
      this.$router.push({
        path: src
      });
    },
    download() {
      this.$refs.downloadTemp.click();
    }
  }
}
</script>

<style lang="scss" scoped>
.stepOuter {
  color: #ed6d00;
  width: 450px;
  margin: 0 auto;
  padding: 50px 0 60px 0;
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
  padding-top: 60px;
}
.step1 ::v-deep {
  width: 450px;
  margin: 0 auto;
  .step1Info {
    text-align: center;
    line-height: 30px;
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

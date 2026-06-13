<template>
  <el-dialog
    ref="dialog"
    title="批量导入照片"
    :visible.sync="currVisible"
    width="700px"
    @open="open"
    @close="close"
    :close-on-click-modal="false"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <section class="my-basic-inner">
      <div class="step1">
        <div class="step1Info">
          <el-form ref="form" :inline="false" :model="searchform" :rules="rules" size="mini" label-width="110px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="searchform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="searchform.taskName" placeholder="请输入" clearable></el-input>
            </el-form-item>
            <el-form-item label="批量选择照片">
              <el-button type="primary" class="importBtn" :loading="selectLoading" @click="importImg" >选择员工照片</el-button>
              <input class="fileBtn" ref="uploadBtn" type="file" multiple @change="myChange($event)" />
            </el-form-item>
            <div class="importCont">
              <p>操作提示：</p>
              <p>1.照片名称以工号为命名。</p>
              <p>2.请核对一下要导入的照片，避免重复覆盖。</p>
              <p>3.一次性导入数量，不超过100张，避免造成系统压力。</p>
              <template v-if="totalImgNum && totalImgNum > 0">
                <div>
                  本次共选择
                  <span class="num">{{totalImgNum}}</span>个员工照片，
                  其中
                  <span class="num red">{{noneStaffs.length}}</span>个照片的工号不存在
                  <template v-if="noneStaffs.length>0">
                    ，请检查以下工号是否正确！
                    <div class="num red">
                      (
                      <template v-for="(item, index) in noneStaffs">
                        {{item.staffBadge}}
                        <template v-if="(index+1) !=noneStaffs.length">，</template>
                      </template>
                      )
                    </div>
                  </template>
                </div>
                <el-scrollbar style="height:400px;">
                  <table class="fileTb">
                    <tr>
                      <td>工号</td>
                      <td>姓名</td>
                      <td>是否存在</td>
                      <td>是否有照片</td>
                      <td>操作</td>
                    </tr>
                    <template v-for="(item, index) in listInfo">
                      <tr :key="index" :class="{'haveNo':item.status==2}">
                        <td>{{ item.staffBadge }}</td>
                        <td>{{ item.staffName || '-' }}</td>
                        <td>{{ item.status==0?'是':'否' }}</td>
                        <td>{{ item.status==0?'是':'否' }}</td>
                        <td>
                          <el-button type="text" @click="delImg(item.staffBadge)">移除</el-button>
                        </td>
                      </tr>
                    </template>
                    <template v-if="listInfo.length == 0">
                      <tr>
                        <td colspan="5">还未选择图片文件</td>
                      </tr>
                    </template>
                  </table>
                </el-scrollbar>
              </template>
          </div>
          </el-form>
        </div>
      </div>
    </section>
    <div slot="footer">
      <el-button type="primary" @click="cancel" plain>取 消</el-button>
      <el-button
        type="primary"
        @click="myUpload()"
        :loading="importLoading"
        :disabled="listInfo.length == 0"
      >确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import  { xcBatchImgApi } from '../_service'
import { getStaffImgInfo } from "@/api/platform/basic/staff_info";
export default {
  components: {
  },
  data() {
    return {
      searchform: {
        parkId: '',
        meterMonth: '',
      },
      rules: {
        parkId: [tce.helper.formRules.vempty()],
        taskName: [tce.helper.formRules.vempty()]
      },
      myFiles: null,
      totalImgNum: 0, //本次共选择了几项
      noneStaffs: [], //不存在的工号集合
      fileNames: [], //名称集合
      listInfo: [], //根据名称集合查询的图片信息
      uploadInfo: [], //要上传的文件列表

      selectLoading: false,
      importLoading: false,

      currVisible: false,
    }
  },
  props: {
    visible: Boolean,
    meterMonth: String
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
    },
    meterMonth(val){
      this.searchform.meterMonth = val
    }
  },
  methods: {
    readAndPreview(file) {
      //将对象转为base64和文件名称的组合
      let _this = this;
      let noneStaffs = this.noneStaffs;
      var namePosition = file.name.lastIndexOf(".");
      var badge = file.name.substr(0, namePosition);

      if (noneStaffs.length > 0) {
        for (var i = 0; i < noneStaffs.length; i++) {
          if (noneStaffs[i].staffBadge == badge) {
            return;
          }
        }
      }

      var reader = new FileReader();
      reader.addEventListener(
        "load",
        function() {
          let originalImgCode = this.result; //含有 base64 头的
          let baseIndex = originalImgCode.indexOf(",") + 1;
          let baseImg = originalImgCode.slice(baseIndex);

          _this.uploadInfo.push({ staffBadge: badge, facePic: baseImg });
        },
        false
      );
      reader.readAsDataURL(file);
    },
    myChange(e) {
      this.$message.closeAll();
      let files = e.target.files;
      this.myFiles = files;
      let errorSuffix = []; //所选文件非指定规范格式(jpg)的图片 的集合 ,用来提示
      let errorSize = []; //所选文件非指定规范大小(60~200k)的图片 的集合 ,用来提示
      let arrName = []; //图片名称集合，用来查询是否存在图片信息
      this.totalImgNum = files.length;

      if (files.length > 0) {
        if (files.length < 2000) {
          //单次导入，数量不能超过200张
          this.selectLoading = true;
          for (var i = 0; i < files.length; i++) {
            var suffixPosition = files[i].name.lastIndexOf(".") + 1;
            var nums = files[i].name.length - suffixPosition;
            var suffix = files[i].name.substr(suffixPosition, nums); //文件后缀

            var namePosition = files[i].name.lastIndexOf(".");
            var badge = files[i].name.substr(0, namePosition); //文件名字

            //找出格式不规范的文件集合
            const isImg =
              suffix == "jpg" ||
              suffix == "JPG" ||
              suffix == "jpeg" ||
              suffix == "JPEG";
            if (!isImg) {
              //判断所选文件格式
              errorSuffix.push(badge);
            }

            //找出大小不规范的文件集合
            const fileSize = files[i].size / 1024;
            if (fileSize > 200) {
              errorSize.push(badge);
            }

            arrName.push({ staffBadge: badge });
          }
          if (errorSuffix.length > 0) {
            this.selectLoading = false;
            let str =
              '<div class="tips" style="max-width: 500px; word-break:break-all;line-height:20px;""><p>共选择了 ' +
              this.totalImgNum +
              " 个文件，有 " +
              errorSuffix.length +
              " 个文件格式不符合规范</p>";
            str +=
              '<p>照片仅支持 jpg 一种格式，请检查以下文件格式再重新导入！</p><p style="color:red">';

            for (var i = 0; i < errorSuffix.length; i++) {
              str += errorSuffix[i] + "，";
            }

            var dotPosition = str.lastIndexOf("，");
            str = str.substr(0, dotPosition);
            str += "</p></div>";

            this.$message({
              dangerouslyUseHTMLString: true,
              showClose: true,
              message: str,
              type: "warning",
              duration: 0
            });
          } else if (errorSize.length > 0) {
            this.selectLoading = false;
            let str =
              '<div class="tips" style="max-width: 500px; word-break:break-all;line-height:20px;""><p>共选择了 ' +
              this.totalImgNum +
              " 个文件，有 " +
              errorSize.length +
              " 个文件大小不符合规范</p>";
            str +=
              '<p>照片大小应在小于200KB，请检查以下文件大小再重新导入！</p><p style="color:red">';

            for (var i = 0; i < errorSize.length; i++) {
              str += errorSize[i] + "，";
            }

            var dotPosition = str.lastIndexOf("，");
            str = str.substr(0, dotPosition);
            str += "</p></div>";

            this.$message({
              dangerouslyUseHTMLString: true,
              showClose: true,
              message: str,
              type: "warning",
              duration: 0
            });
          } else {
            getStaffImgInfo({ facePicUpLoad: arrName })
              .then(response => {
                if (!this.validatenull(response.data.data)) {
                  var result = response.data.data;
                  for (var i = 0; i < result.length; i++) {
                    if (result[i].status == 2) {
                      this.noneStaffs.push(result[i]);
                    } else {
                      this.listInfo.push(result[i]);
                    }
                  }
                  [].forEach.call(files, this.readAndPreview);
                  this.selectLoading = false;
                }
              })
              .catch(err => {
                this.selectLoading = false;
              });
          }
        } else {
          this.$message.warning(
            "为避免造成系统压力,一次性导入数量，不能超过2000张!"
          );
          return;
        }
      }
    },
    delImg(badge) {
      this.$refs.uploadBtn.value = "";
      //根据工号，移除上传列表中的照片文件

      let listInfo = this.listInfo; //列表展示的集合
      let uploadInfo = this.uploadInfo; //上传的集合

      //移除的同时，列表展示的集合和上传的集合都要移除
      for (var i = 0; i < listInfo.length; i++) {
        if (listInfo[i].staffBadge == badge) {
          this.listInfo.splice(i, 1);
        }
      }

      //移除的同时，列表展示的集合和上传的集合都要移除
      for (var i = 0; i < uploadInfo.length; i++) {
        if (uploadInfo[i].staffBadge == badge) {
          this.uploadInfo.splice(i, 1);
        }
      }
    },
    async myUpload() {
      await this.validateForm('form')
      if (this.uploadInfo.length > 0) {
        this.importLoading = true;
        let obj = {
          parkId: this.searchform.parkId,
          taskName: this.searchform.taskName,
          facePicUpLoad: this.uploadInfo
        }
        xcBatchImgApi.importImg(obj)
          .then(response => {
            this.importLoading = false;
            if (response.data.data) {
              this.$notify({
                title: "操作成功",
                message: "添加导入任务成功",
                type: "success"
              });
              this.refresh()
            } else {
              this.$notify.error({
                title: "失败",
                message: "员工照片导入失败！"
              });
            }
          })
          .catch(err => {
            //不要在此处提示失败
            // this.$notify.error({
            //   title: '失败',
            //   message: '员工照片导入失败！'
            // });
            this.importLoading = false;
          });
      }
    },
    importImg() {
      this.$refs.uploadBtn.click();
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
    initInfo(){
      this.myFiles = null
      this.totalImgNum = 0
      this.noneStaffs = []
      this.fileNames = []
      this.listInfo = []
      this.uploadInfo = []
      this.$refs.uploadBtn.value =''
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.initInfo()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.initInfo()
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .my-basic-inner{
    padding: 10px 0 30px;
  }
  .importCont {
    line-height: 25px;
  }
  .tips {
    max-width: 500px;
    border: 1px solid red;
  }
  .num {
    padding: 0 5px;
    font-weight: bold;
  }
  .red {
    color: red;
  }
  .fileBtn {
    display: none;
  }
  .fileTb {
    margin: 10px 0 0 0;
    width: 100%;
    .haveNo {
      color: red;
    }
    td {
      border: 1px solid #e0e0e0;
      text-align: center;
      padding: 0 10px;
      height: 35px;
    }
  }
  ::v-deep .el-scrollbar__wrap {
    overflow-x: auto;
  }
</style>

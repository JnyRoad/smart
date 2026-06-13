<!--基础信息：员工信息 -->
<template>
  <div></div>
</template>

<script>
import {
  getStaffImgInfo,
  importImgs
} from "@/api/platform/basic/staff_info";
import { mapGetters } from "vuex";

export default {
  name: "",
  data() {
    return {
      totalImgNum: 0, //本次共选择了几项
      noneStaffs: [], //不存在的工号集合
      fileNames: [], //名称集合
      listInfo: [], //根据名称集合查询的图片信息
      uploadInfo: [], //要上传的文件列表
      importVisible: false,
      importLoading: false,
    };
  },
  created() {},
  mounted: function() {},
  computed: {},
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
    delImg(badge) {
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
    importCancel() {
      this.importVisible = false;
      this.noneStaffs = []; //不存在的工号集合
      this.fileNames = []; //名称集合
      this.listInfo = []; //根据名称集合查询的图片信息
      this.uploadInfo = []; //要上传的文件列表
      this.$refs.uploadBtn.value = "";
    },
    myUpload() {
      if (this.uploadInfo.length > 0) {
        this.importLoading = true;

        importImgs({ facePicUpLoad: this.uploadInfo })
          .then(response => {
            this.importLoading = false;
            if (response.data.data) {
              this.importVisible = false;
              this.$notify({
                title: "完成",
                message: "成功导入" + response.data.data + "张照片！",
                type: "success"
              });
            } else {
              this.$notify.error({
                title: "失败",
                message: "员工照片导入失败！"
              });
            }
          })
          .catch(() => {
            //不要在此处提示失败
            // this.$notify.error({
            //   title: '失败',
            //   message: '员工照片导入失败！'
            // });
            this.importLoading = false;
          });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 130px;
  }
}
.upload-demo {
  display: inline-block;
  margin-right: 20px;
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
.importBtn {
  margin-right: 20px;
}
.fileBtn {
  display: none;
}
.importCont {
  line-height: 25px;
  padding-bottom: 30px;
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

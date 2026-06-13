<!--社保公积金 -->
<template>
  <div class="my-basic-container park">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加社保</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="标题" prop="title">
            <el-input v-model="searchForm.title" placeholder="标题" clearable></el-input>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @row-del="rowDel"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleUpdate(scope.row,scope.$index)"
            >编辑</el-button>
          </template>
        </avue-crud>

        <el-dialog
          :title="dialogTitle"
          class="dialog_form"
          @close="resetAddFrom('addForm')"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addForm" :model="addform" label-width="120px">
            <el-form-item label="标题名称" prop="title">
              <el-input v-model="addform.title" placeholder="请输入标题名称" clearable></el-input>
            </el-form-item>
            <el-form-item label="配图" prop="image">
              <el-col :span="12" style="padding-right:10px;">
                <div class="img-info car-img">
                  <div class="img-outer">
                    <div class="img-inner">
                      <el-upload
                        class="avatar-uploader"
                        action
                        :before-upload="imgrUpload"
                        :show-file-list="false"
                      >
                        <img
                          v-if="addform.image"
                          :src="addform.image"
                          :onerror="errorImgPeaple()"
                          class="avatar"
                        />
                        <i v-else class></i>
                      </el-upload>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-form-item>
            <el-form-item label="标题链接" prop="url">
              <el-input v-model="addform.url" placeholder="请输入标题链接" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addForm')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import EXIF from "exif-js";
import {
  fetchList,
  updateObj,
  addObj,
  delObj,
  fetchHrList
} from "@/api/platform/basic/social_security";
import { tableOption } from "@/const/crud/platform/basic/social_security";
import { mapGetters } from "vuex";
import { cardid } from "@/util/validate";

export default {
  name: "blacklist",
  data() {
    let _this = this;
    var validateTitle = (rule, value, callback) => {
      if (/^[0-9a-zA-Z\u4e00-\u9fa5_\s]{2,30}$/.test(value) == false) {
        this.$message.closeAll();
        this.$message({
          message:
            "标题名称应包含2-30个字符，可为汉字、数字、字母（大小写）、下划线!",
          type: "warning"
        });
      } else {
        callback();
      }
    };
    var validateUrl = (rule, value, callback) => {
      let url = this.addform.url;
      if (this.validatenull(url)) {
        callback(new Error("请输入标题链接"));
      } else {
        callback();
      }
    };
    return {
      dialogTitle: "添加社保",
      addLoading: false,
      addFormVisible: false, //添加园区，显示切换
      searchForm: {
        //搜索菜单表单
        title: undefined
      },
      blackType: 0,
      addform: {
        //添加园区，表单
        title: "",
        image: "",
        url: null
      },
      rules: {
        title: [
          { required: true, message: "请输入标题名称", trigger: "blur" },
          { validator: validateTitle, trigger: "blur" }
        ],
        url: [
          { required: true, message: "请输入标题链接", trigger: "blur" },
          { validator: validateUrl, trigger: "blur" }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },

      tableLoading: false,
      tableData: [],
      tableHrData: [],
      imgDirection: "",
      tableOption: tableOption
    };
  },
  created: function() {
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },

    handleUpdate(row) {
      this.dialogTitle = "修改社保";
      this.addFormVisible = true;
      this.addform = Object.assign({}, row);
      if (row.image.indexOf("data:image/jpeg;base64,") <= -1) {
        this.addform.image = "data:image/jpeg;base64," + row.image;
      }
    },

    imgrUpload(file) {
      let _this = this;
      let fileSize;
      let fileName;
      let maxSize = 2 * 1024 * 1024; //2MB
      EXIF.getData(file, function() {
        let exifdata = file.exifdata;
        _this.imgDirection = file.exifdata.Orientation;
      });
      let reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = function(e) {
        //onload
        fileSize = file.size;
        fileName = file.name;
        //这是图片没压缩以前的 代码
        let image = new Image();
        image.src = reader.result;
        image.onload = function() {
          let canvas = document.createElement("canvas");
          let context = canvas.getContext("2d");
          let imageWidth;
          let imageHeight;

          if (fileSize > maxSize) {
            imageWidth = image.width / 5;
            imageHeight = image.height / 5;
          } else {
            imageWidth = image.width;
            imageHeight = image.height;
          }

          let data = "";
          if (imageWidth > imageHeight) {
            canvas.width = imageWidth;
            canvas.height = imageWidth;
          } else {
            canvas.width = imageHeight;
            canvas.height = imageHeight;
          }

          if (_this.imgDirection && _this.imgDirection != 1) {
            switch (_this.imgDirection) {
              case 6: // 旋转90度
                var x = 0;
                var y = -(imageWidth - 50);
                if (imageWidth > imageHeight) {
                  y = -imageWidth - (imageHeight - imageWidth) / 2;
                } else {
                  // y = ;
                }
                context.rotate((90 * Math.PI) / 180);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
              case 3: // 旋转180度
                var x = -imageWidth;
                var y = -imageHeight;
                if (imageWidth > imageHeight) {
                  x = -imageWidth;
                  y = -imageHeight - (imageWidth - imageHeight) / 2;
                } else {
                  // y = ;
                }
                context.rotate(Math.PI);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
              case 8: // 旋转-90度  //270
                var x = -imageWidth;
                var y = 70;
                if (imageHeight > imageWidth) {
                  x = -imageWidth;
                  y = -(imageHeight - imageWidth) / 2;
                } else {
                  // y = ;
                }
                context.rotate((3 * Math.PI) / 2);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
            }
          } else {
            var x = 0;
            var y = 0;
            if (imageHeight > imageWidth) {
              x = (imageHeight - imageWidth) / 2;
            } else {
              y = (imageWidth - imageHeight) / 2;
            }
            context.drawImage(image, x, y, imageWidth, imageHeight);
          }

          // 将canvas的透明背景设置成白色
          var imageData = context.getImageData(
            0,
            0,
            canvas.width,
            canvas.height
          );
          for (var i = 0; i < imageData.data.length; i += 4) {
            // 当该像素是透明的，则设置成白色
            if (imageData.data[i + 3] == 0) {
              imageData.data[i] = 255;
              imageData.data[i + 1] = 255;
              imageData.data[i + 2] = 255;
              imageData.data[i + 3] = 255;
            }
          }

          context.putImageData(imageData, 0, 0);
          data = canvas.toDataURL("image/jpeg");

          //压缩完毕
          _this.addform.image = data; //含有 base64 头的
        };
      };
    },

    changeBlackType() {
      this.page.currentPage = 1;
      this.getList(this.page);
    },

    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page);
    },
    addSubmit(formName) {
      //添加内容确定
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          if (this.addform.id != null && this.addform.id != "") {
            updateObj(this.addform)
              .then(response => {
                var msg = response.data.msg;
                var dataResult = response.data.data;
                if (dataResult === true) {
                  this.getList(this.page);
                  this.addFormVisible = false;
                  this.$notify({
                    title: "成功",
                    message: msg,
                    type: "success",
                    duration: 2000
                  });
                } else if (dataResult === false) {
                  this.$notify({
                    title: "失败",
                    message: msg,
                    type: "error",
                    duration: 2000
                  });
                }
                this.addLoading = false;
              })
              .catch(err => {
                this.addLoading = false;
              });
          } else {
            addObj(this.addform)
              .then(response => {
                var msg = response.data.msg;
                var dataResult = response.data.data;
                if (dataResult === true) {
                  this.getList(this.page);
                  this.addFormVisible = false;
                  this.$notify({
                    title: "成功",
                    message: msg,
                    type: "success",
                    duration: 2000
                  });
                } else if (dataResult === false) {
                  this.$notify({
                    title: "失败",
                    message: msg,
                    type: "error",
                    duration: 2000
                  });
                }
                this.addLoading = false;
              })
              .catch(err => {
                this.addLoading = false;
              });
          }
        } else {
          return false;
        }
      });
    },
    addCancel(formName) {
      this.addFormVisible = false;
      //this.resetAddFrom(formName);
    },
    resetAddFrom(formName) {
      this.$refs[formName].resetFields();
    },
    handleDel(row, index) {
      //删除
      this.$refs.crud.rowDel(row, index);
    },
    rowDel: function(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该社保信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(row.id);
        })
        .then(dataResponse => {
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.data;
          if (dataResult == true) {
            _this.getList(this.page);
            _this.$notify({
              title: "删除成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          } else if (dataResult === false) {
            _this.$notify({
              title: "删除失败",
              message: msg,
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.getList(this.page);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>

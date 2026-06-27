<template>
  <div class="my-basic-container">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom: 20px">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-form ref="addform" :model="addform" size="mini" label-width="110px" :rules="rules">
          <div style="padding: 0 0 10px 10px">
            <p class="box-orange">添加资源信息</p>
            <el-form-item label="资源名称" prop="name">
              <el-input v-model="addform.name" class="ipt1" placeholder="资源名称"></el-input>
            </el-form-item>
            <el-form-item label="资源类型" prop="">
              <template>
                <el-tabs v-model="activeName" style="border: 1px solid #dcdfe6; min-height: 400px; width: 800px" type="border-card">
                  <el-tab-pane label="视频" name="0">
                    <div class="picture-box">
                      <el-button type="primary" class="importBtn" :loading="selectLoading" @click="importVideo">上传视频</el-button>
                      <div class="tab-tips">只能上传mp4文件,建议大小在50M以内</div>
                      <input class="fileBtn" ref="uploadVideoBtn" type="file" @change="myVideoChange($event)" />
                      <div v-if="file.name && file.type === 'video'">
                        {{ file.name }}<i @click="deleteFile()" class="el-icon-delete" style="margin-left: 20px; cursor: pointer"></i>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="图片" name="1">
                    <div class="picture-box">
                      <el-button type="primary" class="importBtn" :loading="selectLoading" @click="importImg">上传图片</el-button>
                      <div class="tab-tips">只能上传jpg/png文件，且不超过500kb</div>
                      <input class="fileBtn" ref="uploadBtn" type="file" @change="myChange($event)" />
                      <div v-for="(item, index) in fileList" :key="index">
                        <el-row>
                          <el-col :span="8">
                            <img v-if="item.url" :src="item.url" width="90px" height="90px" />
                            <i v-else style="font-size: 90px" class="el-icon-picture-outline"></i>
                          </el-col>
                          <el-col :span="10" style="height: 90px; line-height: 90px">
                            <span style="float: left">播放顺序：</span>
                            <div style="width: 80px; margin-left: 10px; float: left">
                              <el-input-number v-model="item.sort" :min="1"></el-input-number>
                            </div>
                          </el-col>
                          <el-col :span="6" style="height: 90px; line-height: 90px; text-align: center">
                            <el-button type="primary" plain icon="el-icon-delete" @click="deletePicture(index)">删除图片</el-button>
                          </el-col>
                        </el-row>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="PDF" name="2">
                    <div class="picture-box">
                      <el-button type="primary" class="importBtn" :loading="selectLoading" @click="importPPT">上传文件</el-button>
                      <div class="tab-tips">仅支持上传PDF文件</div>
                      <input class="fileBtn" ref="uploadPPTBtn" type="file" @change="myPPTChange($event)" />
                      <div v-if="file.name && file.type === 'ppt'">
                        {{ file.name }}<i @click="deleteFile()" class="el-icon-delete" style="margin-left: 20px; cursor: pointer"></i>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="文本" name="3" style="padding: 20px">
                    <el-form ref="textForm" :model="textForm" size="mini" label-width="110px" :rules="textRules">
                      <el-form-item label="文本内容" prop="content">
                        <el-input type="textarea" :rows="6" placeholder="请输入内容" v-model="textForm.content"> </el-input>
                      </el-form-item>
                      <el-form-item label="轮播展示" prop="textMoveType">
                        <template>
                          <el-radio v-model="textForm.textMoveType" label="1">左右方向轮播</el-radio>
                          <el-radio v-model="textForm.textMoveType" label="2">上下方向轮播</el-radio>
                        </template>
                      </el-form-item>
                      <el-form-item label="背景颜色" prop="backgroundColor">
                        <el-color-picker v-model="textForm.backgroundColor"></el-color-picker>
                      </el-form-item>
                      <el-form-item label="字体颜色" prop="fontColor">
                        <el-color-picker v-model="textForm.fontColor"></el-color-picker>
                      </el-form-item>
                    </el-form>
                  </el-tab-pane>
                  <el-tab-pane label="网页" name="4" style="padding: 30px 16%">
                    <el-form-item label="网页URL" prop="">
                      <el-input v-model="urlContent" placeholder="资源名称"></el-input>
                    </el-form-item>
                    <p class="box-remark">网页尺寸建议 1366*768</p>
                  </el-tab-pane>
                </el-tabs>
              </template>
            </el-form-item>
          </div>
          <div class="btns2" style="text-align: center">
            <el-button type="primary" @click="goBack" plain>取消</el-button>
            <el-button type="primary" @click="saveSubmit" :disabled="saveLoading">保存</el-button>
          </div>
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { returnApi } from './_service'
export default {
  data() {
    return {
      saveLoading: false,
      activeName: '0',
      addform: {
        name: null
      },
      urlContent: null,
      rules: {
        name: [tce.helper.formRules.vempty()]
      },
      textForm: {
        content: '',
        textMoveType: '1',
        backgroundColor: '#f0f0f0',
        fontColor: '#333333'
      },
      textRules: {
        content: [tce.helper.formRules.vempty()],
        textMoveType: [tce.helper.formRules.vempty()],
        backgroundColor: [tce.helper.formRules.vempty()],
        fontColor: [tce.helper.formRules.vempty()]
      },
      fileList: [{ url: '', imageId: '', sort: 1 }],
      file: {
        name: null,
        url: null,
        type: null
      },
      selectLoading: false,
      detailsInfo: {}
    }
  },
  created() {
    this.getDetails()
  },
  mounted: function () {},
  methods: {
    async getDetails() {
      const res = await returnApi.getDetail(this.$route.params.id)
      this.detailsInfo = res.data.data
      this.activeName = JSON.stringify(this.detailsInfo.type)
      this.addform.name = this.detailsInfo.infoName
      if (this.activeName == 1) {
        this.fileList = this.detailsInfo.images
        this.fileList.forEach((element) => {
          element['url'] = element['imageUrl']
        })
        // imageUrl
      } else if (this.activeName == 3) {
        this.textForm.content = this.detailsInfo.content
        this.textForm.textMoveType = JSON.stringify(this.detailsInfo.textMoveType)
        const textStyle = JSON.parse(this.detailsInfo.textStyle)
        this.textForm.backgroundColor = textStyle.backgroundColor
        this.textForm.fontColor = textStyle.fontColor
        // console.log(textStyle)
      } else if (this.activeName == 4) {
        this.urlContent = this.detailsInfo.content
      } else if (this.activeName == 2) {
        this.file = {
          name: this.detailsInfo.file.fileName,
          url: this.detailsInfo.file.id,
          type: 'ppt'
        }
      } else if (this.activeName == 0) {
        this.file = {
          name: this.detailsInfo.file.fileName,
          url: this.detailsInfo.file.id,
          type: 'video'
        }
      }
    },
    async saveSubmit() {
      await this.validateForm()
      const obj = {
        type: this.activeName,
        infoName: this.addform.name,
        id: this.detailsInfo.id
      }
      if (this.activeName == 1) {
        //图片
        obj['imageReqDTOS'] = this.fileList
        if (this.fileList[0].imageId === '') {
          this.$message({
            message: '上传图片不能为空',
            type: 'error'
          })
          return
        }
        this.addData(obj)
      } else if (this.activeName == 3) {
        //文本
        await this.validateForm('textForm')
        obj['content'] = this.textForm.content
        obj['textMoveType'] = this.textForm.textMoveType
        const textStyle = {
          backgroundColor: this.textForm.backgroundColor,
          fontColor: this.textForm.fontColor
        }
        obj['textStyle'] = JSON.stringify(textStyle)
        this.addData(obj)
      } else if (this.activeName == 4) {
        //网页
        obj['content'] = this.urlContent
        if (this.urlContent === '') {
          this.$message({
            message: '网页不能为空',
            type: 'error'
          })
          return
        }
        this.addData(obj)
      } else if (this.activeName == 0 || this.activeName == 2) {
        // 视频或者ppt
        obj['content'] = this.file.url
        if (!this.file.url) {
          this.$message({
            message: '文件不能为空',
            type: 'error'
          })
          return
        }
        this.addData(obj)
      }
    },
    addData(obj) {
      this.saveLoading = true
      returnApi
        .saveSubmit(obj)
        .then((response) => {
          this.saveLoading = false
          if (response.data.code === 0) {
            this.$message({
              showClose: true,
              message: '修改成功',
              type: 'success'
            })
            this.goBack()
          } else {
            this.$notify({
              title: '失败',
              message: response.data.msg,
              type: 'error',
              duration: 2000
            })
          }
        })
        .catch((err) => {
          this.saveLoading = false
        })
    },
    importImg() {
      this.$refs.uploadBtn.click()
    },
    importVideo() {
      this.$refs.uploadVideoBtn.click()
    },
    importPPT() {
      this.$refs.uploadPPTBtn.click()
    },
    myChange(e) {
      this.$message.closeAll()
      let files = e.target.files[0]
      var suffixPosition = files.name.lastIndexOf('.') + 1
      var nums = files.name.length - suffixPosition
      var suffix = files.name.substr(suffixPosition, nums) //文件后缀
      //找出格式不规范的文件集合
      const isImg = suffix == 'jpg' || suffix == 'JPG' || suffix == 'jpeg' || suffix == 'JPEG' || suffix == 'png'
      if (!isImg) {
        //判断所选文件格式
        this.$message({
          showClose: true,
          message: '请上传正确格式的文件',
          type: 'warning',
          duration: 0
        })
        return
      }
      //找出大小不规范的文件集合
      const fileSize = files.size / 1024
      if (fileSize > 500) {
        this.$message({
          showClose: true,
          message: '图片大小不超过500kb',
          type: 'warning',
          duration: 0
        })
        return
      }
      // base64
      var reader = new FileReader()
      reader.readAsDataURL(files)
      reader.onload = (e) => {
        // e.target.result
        this.selectLoading = true
        returnApi.importImgs({ base64String: e.target.result, imageType: 0 }).then((response) => {
          this.selectLoading = false
          if (this.fileList.length === 1 && this.fileList[0].imageId === '') {
            this.fileList[0].imageId = response.data.data
            this.fileList[0].url = e.target.result
          } else {
            const l = this.fileList.length
            this.fileList.push({
              imageId: response.data.data,
              url: e.target.result,
              sort: l + 1
            })
          }
        })
      }
    },
    myVideoChange(e) {
      this.$message.closeAll()
      let files = e.target.files[0]
      var suffixPosition = files.name.lastIndexOf('.') + 1
      var nums = files.name.length - suffixPosition
      var suffix = files.name.substr(suffixPosition, nums) //文件后缀
      //找出格式不规范的文件集合
      const isMp4 = suffix == 'mp4' || suffix == 'MP4'
      if (!isMp4) {
        //判断所选文件格式
        this.$message({
          showClose: true,
          message: '请上传mp4格式的视频',
          type: 'warning',
          duration: 0
        })
        return
      }
      //找出大小不规范的文件集合
      const fileSize = files.size / 1024 / 1024
      if (fileSize > 50) {
        this.$message({
          showClose: true,
          message: '视频大小不超过50mb',
          type: 'warning',
          duration: 0
        })
        return
      }
      this.fileUpload(files, suffix, 'video')
    },
    myPPTChange(e) {
      this.$message.closeAll()
      let files = e.target.files[0]
      var suffixPosition = files.name.lastIndexOf('.') + 1
      var nums = files.name.length - suffixPosition
      var suffix = files.name.substr(suffixPosition, nums) //文件后缀
      //找出格式不规范的文件集合
      const isImg = suffix == 'pdf'
      if (!isImg) {
        //判断所选文件格式
        this.$message({
          showClose: true,
          message: '请上传正确的pdf文件',
          type: 'warning',
          duration: 0
        })
        return
      }
      //找出大小不规范的文件集合
      const fileSize = files.size / 1024 / 1024
      if (fileSize > 20) {
        this.$message({
          showClose: true,
          message: '文件大小不超过20mb',
          type: 'warning',
          duration: 0
        })
        return
      }
      this.fileUpload(files, suffix, 'ppt')
    },
    async fileUpload(files, suffix, type) {
      const formdata = new FormData()
      formdata.append('data', files)
      formdata.append('fileName', files.name)
      formdata.append('fileSize', files.size)
      formdata.append('fileSuffix', suffix)
      const res = await returnApi.fileUpload(formdata)
      let resBlob = new Blob([res.data])
      let reader = new FileReader()
      reader.readAsText(resBlob, 'utf-8')
      reader.onload = () => {
        try {
          let res2 = JSON.parse(reader.result)
          if (res2.code !== 0) {
            this.$message.error(res2.message)
          } else {
            this.file.name = files.name
            this.file.url = res2.data
            this.file.type = type
            this.$message({
              message: '导入成功',
              type: 'success'
            })
          }
        } catch (e) {
          // 导入接口成功后，有可能返回失败名单
          this.$message.error('上传失败!')
        }
      }
    },
    deleteFile() {
      this.file.name = null
      this.file.url = null
      this.file.type = null
    },
    deletePicture(i) {
      if (this.fileList.length > 1) {
        this.fileList.splice(i, 1)
      } else {
        this.fileList[0].url = ''
        this.fileList[0].sort = 1
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
    goBack() {
      this.$router.push({
        path: `/platform/info_delivery/info_mng`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.ipt1 {
  width: 300px !important;
  margin-right: 30px;
}
.box-remark {
  width: 100%;
  text-align: center;
}
.fileBtn {
  display: none;
}
.tab-tips {
  font-size: 12px;
  color: #606266;
  margin: 10px 0;
}
.picture-box {
  position: relative;
  padding: 10px 100px;
  .cont1 {
    float: left;
    width: calc(100% - 60px);
  }
  .cont2 {
    float: left;
    width: 60px;
    padding-top: 68px;
    text-align: center;
    .icon-cont {
      margin-top: 10px;
      height: 92px;
      i {
        width: 100%;
        cursor: pointer;
      }
    }
  }
}
</style>
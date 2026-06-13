<template>
  <div class="mncont" style="padding: 50px 100px;">
    <el-row style="text-align: center">
      <el-col :span="5">
        <el-form ref="form" :inline="true" label-position="right" label-width="80px">
          <el-form-item label="算法选择">
            <el-select v-model="algorithmType">
              <el-option
                v-for="item in algorithms"
                :key="item.algorithmType"
                :label="item.algorithmName"
                :value="item.algorithmType">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="5">
        <el-form ref="form1" :inline="true" label-position="right" label-width="80px">
          <el-form-item label="检测类型">
            <el-select v-model="faceDetectType" @change="faceDetectChange">
              <el-option
                v-for="item in faceDetectTypes"
                :key="item.type"
                :label="item.name"
                :value="item.type">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="10">
        <div style="font-size: 20px;height: 40px;line-height: 40px;">检测结果</div>
      </el-col>
    </el-row>
    <el-row style="text-align: center">
      <el-col :span="5">
        <el-upload
          :class="[uploadClass]"
          action="#"
          list-type="picture-card"
          accept="image/*"
          :auto-upload="false"
          :limit="fileLimit"
          :multiple="false"
          :on-change="fileChange"
          :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove"
          :on-exceed="handleFileLimit"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
        <el-dialog :visible.sync="dialogVisible">
          <img class="max500" :src="dialogImageUrl" alt="">
        </el-dialog>
      </el-col>
      <el-col :span="5">
        <el-button @click.native="action" type="primary" style="margin-top:60px;">开始识别<i
          class="el-icon-arrow-right"></i></el-button>
      </el-col>
      <el-col :span="10">
        <el-form ref="form1" label-position="right" label-width="80px">
          <el-form-item v-if="faceDetectType === 1" label="特征值">
            <el-input type="textarea" size="medium" :show-word-limit="true" class="textarea1"
                      resize="none" :readonly="true" v-model="faceData"></el-input>
          </el-form-item>
          <el-form-item v-if="faceDetectType === 2" label="裁剪图片">
            <template>
              <div class="best-images">
                <img @click="showBigImg(faceData)" v-if="faceData" :src="'data:image/jpg;base64,' + faceData" title="查看大图">
                <img v-else src="../assets/img/default.png">
              </div>
            </template>
          </el-form-item>
        </el-form>
        <el-dialog :visible.sync="bigImgVisible">
          <img class="max500" :src="bigImgBase64" alt="">
        </el-dialog>
      </el-col>
    </el-row>
    <h1 style="margin-top: 30px;margin-bottom: 10px;font-size: 24px;">响应结果：</h1>
    <el-row>
      <el-col :span="24">
        <el-input class="textarea2" type="textarea" :readonly="true" v-model="result"></el-input>
      </el-col>
    </el-row>
  </div>
</template>

<script>

  export default {
    name: "FaceDetect",
    data: function () {
      return {
        dialogImageUrl: '',
        dialogVisible: false,
        fileLimit: 1,
        uploadClass: 'uploadShow',
        algorithms: [],
        algorithmType: 'facedetect_seeta',
        faceDetectTypes: [],
        faceDetectType: 1,
        imageBase64: '',
        faceData: '',
        result: '',
        bigImgVisible: false,
        bigImgBase64: ''
      };
    },
    methods: {
      faceDetectChange () {
          this.faceData = '';
          this.result = '';
      },
      showBigImg (base64) {
          this.bigImgBase64 = 'data:image/jpg;base64,' + base64;
          this.bigImgVisible = true;
      },
      algorithmList(algorithmType) {
        const that = this;
        this.get('/test/algorithms?type=' + algorithmType, function (r) {
          that.algorithms = r.data;
        });
      },
      faceDetectTypeList() {
          const that = this;
          this.get('/test/face/detect/type', function (r) {
              that.faceDetectTypes = r.data;
          });
      },
      action() {
        const that = this;
        if (!this.imageBase64) {
          this.$message.warning('请上传图片');
          return;
        }
        this.post('/test/face/detect/' + this.algorithmType + '/' + this.faceDetectType + '/test', this.imageBase64, function (r) {
          that.faceData = r.data;
          that.result = JSON.stringify(r);
          that.$message.success("识别完成");
        }, function (e) {
          that.faceData = '';
          that.result = e.responseText;
          that.$message.error("识别失败：" + e.responseJSON.message);
        });
      },
      fileChange(file, fileList) {
        if (this.fileLimit === fileList.length) {
          this.uploadClass = 'uploadHide';
        }
        let reader = new FileReader();
        reader.readAsDataURL(file.raw);
        let that = this;
        reader.onload = function (evt) {
          //读取完文件之后会回来这里
          that.imageBase64 = evt.target.result.substring(
            reader.result.indexOf(",") + 1
          );
        };
      },
      handleRemove(file, fileList) {
        this.imageBase64 = '';
        this.faceData = '';
        this.result = '';
        this.uploadClass = 'uploadShow';
      },
      handlePictureCardPreview(file) {
        this.dialogImageUrl = file.url;
        this.dialogVisible = true;
      },
      handleFileLimit() {
        this.$message.warning("已达到最大图片数量：" + this.fileLimit)
      },
    },
    created() {
      //实例创建完成后,页面渲染前执行
      this.algorithmList('facedetect');
      this.faceDetectTypeList();
    },

    mounted() {
    }
  };
</script>

<style scoped>
  .mncont {
    display: flex;
    flex-direction: column;
    background: transparent;
    box-shadow: none;
    padding: 0;
  }

</style>

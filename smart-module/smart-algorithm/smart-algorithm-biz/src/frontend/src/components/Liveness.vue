<template>
  <div class="mncont" style="padding: 50px 100px;">
    <el-row style="text-align: center">
      <el-col :span="10">
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
      <el-col :span="4">
      </el-col>
      <el-col :span="10">
        <div style="font-size: 20px;height: 40px;line-height: 40px;">检测结果</div>
      </el-col>
    </el-row>
    <el-row style="text-align: center">
      <el-col :span="10">
        <el-upload
          :class="[uploadClass]"
          action="#"
          list-type="picture-card"
          accept="image/*"
          :auto-upload="false"
          :limit="fileLimit"
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
      <el-col :span="4">
        <el-button @click.native="action" type="primary" style="margin-top:60px;">开始检测<i class="el-icon-arrow-right"></i></el-button>
      </el-col>
      <el-col :span="10">
        <el-form ref="form1" label-position="right" label-width="80px">
          <el-form-item label="置信度">
            <el-input v-model="livenessDTO.livevnessConfidence" style="border:0;" readonly></el-input>
          </el-form-item>
          <el-form-item label="最优图片">
            <template>
              <div class="best-images">
                <img @click="showBigImg" v-if="livenessDTO.bestImageBase64" :src="'data:image/jpg;base64,' + livenessDTO.bestImageBase64" title="查看大图">
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
        <el-input class="textarea2" type="textarea" :rows="4" :readonly="true" v-model="result"></el-input>
      </el-col>
    </el-row>
  </div>
</template>

<script>

  export default {
    name: "Liveness",
    data: function () {
      return {
        dialogImageUrl: '',
        dialogVisible: false,
        fileLimit: 8,
        uploadClass: 'uploadShow',
        algorithms: [],
        algorithmType: 'liveness_static_seeta',
        fileList: [],
        livenessDTO: {},
        result:'',
        bigImgVisible: false,
        bigImgBase64: ''
      };
    },
    methods: {
      algorithmList(algorithmType) {
        const that = this;
        this.get('/test/algorithms?type=' + algorithmType, function (r) {
          that.algorithms = r.data;
        });
      },
      action () {
        const that = this;
        const imageBase64List = [];
        this.fileList.forEach(function (f, index) {
          let reader = new FileReader();
          reader.readAsDataURL(f.raw);
          reader.onload = function (evt) {
            //读取完文件之后会回来这里
            imageBase64List.push(evt.target.result.substring(
              reader.result.indexOf(",") + 1
            ));
            if (that.fileList.length === (index+1)) {
              that.post('/test/liveness/static/' + that.algorithmType + '/test', imageBase64List, function (r) {
                that.livenessDTO = r.data;
                that.result = JSON.stringify(r);
                that.$message.success("检测完成");
              }, function (e) {
                that.livenessDTO = e.responseJSON;
                that.result = e.responseText;
                that.$message.error("检测失败：" + e.responseJSON.message);
              });
            }
          };
        });
      },
      showBigImg () {
        this.bigImgBase64 = 'data:image/jpg;base64,' + this.livenessDTO.bestImageBase64;
        this.bigImgVisible = true;
      },
      fileChange(file, fileList) {
        if (this.fileLimit === fileList.length) {
          this.uploadClass = 'uploadHide';
        }
        this.fileList = fileList;
      },
      handleRemove(file, fileList) {
        this.livenessDTO = {};
        this.result = '';
        this.uploadClass = 'uploadShow';
        this.fileList = fileList;
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
      this.algorithmList('liveness_static');
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

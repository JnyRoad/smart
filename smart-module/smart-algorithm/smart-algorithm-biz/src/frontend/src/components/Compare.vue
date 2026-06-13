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
        <div style="font-size: 20px;height: 40px;line-height: 40px;">比对结果</div>
      </el-col>
    </el-row>
    <el-row style="text-align: center">
      <el-col :span="5">
        <el-upload
          :class="uploadClassA"
          action="#"
          list-type="picture-card"
          accept="image/*"
          :auto-upload="false"
          :limit="fileLimit"
          :multiple="false"
          :on-change="fileChangeA"
          :on-preview="handlePictureCardPreviewA"
          :on-remove="handleRemoveA"
          :on-exceed="handleFileLimit"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
        <el-dialog :visible.sync="dialogVisibleA">
          <img class="max500" :src="dialogImageUrlA" alt="">
        </el-dialog>
      </el-col>

      <el-col :span="5">
        <el-upload
          :class="uploadClassB"
          action="#"
          list-type="picture-card"
          accept="image/*"
          :auto-upload="false"
          :limit="fileLimit"
          :multiple="false"
          :on-change="fileChangeB"
          :on-preview="handlePictureCardPreviewB"
          :on-remove="handleRemoveB"
          :on-exceed="handleFileLimit"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
        <el-dialog :visible.sync="dialogVisibleB">
          <img class="max500" :src="dialogImageUrlB" alt="">
        </el-dialog>
      </el-col>
      <el-col :span="4">
        <el-button @click.native="action" type="primary" style="margin-top:60px;">开始比对<i
          class="el-icon-arrow-right"></i></el-button>
      </el-col>
      <el-col :span="10">
        <el-form ref="form1" label-position="right" label-width="80px">
          <el-form-item label="相似度">
            <el-input type="text" size="medium" :show-word-limit="true"
                      resize="none" :readonly="true" v-model="similarity"></el-input>
          </el-form-item>
        </el-form>
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
    name: "Compare",
    data: function () {
      return {
        algorithms: [],
        algorithmType: 'compare_seeta',
        fileLimit: 1,
        dialogImageUrlA: '',
        dialogVisibleA: false,
        uploadClassA: 'uploadShow',
        dialogImageUrlB: '',
        dialogVisibleB: false,
        uploadClassB: 'uploadShow',
        compareDTO: {
          compareImageA: {
            imageBase64:'',
            faceType:'LIVE'
          },
          compareImageB: {
            imageBase64:'',
            faceType:'LIVE'
          }
        },
        similarity: '',
        result: ''
      };
    },
    methods: {
      algorithmList(algorithmType) {
        const that = this;
        this.get('/test/algorithms?type=' + algorithmType, function (r) {
          that.algorithms = r.data;
        });
      },
      action() {
        const that = this;
        if (!this.compareDTO.compareImageA.imageBase64 || !this.compareDTO.compareImageB.imageBase64) {
          this.$message.warning('请上传2张图片');
          return;
        }
        this.post('/test/compare/' + this.algorithmType + '/test', this.compareDTO, function (r) {
          that.similarity = r.data.similarity;
          that.result = JSON.stringify(r);
          that.$message.success("比对完成");
        }, function (e) {
          that.similarity = null;
          that.result = e.responseText;
          that.$message.error("比对失败：" + e.responseJSON.message);
        });
      },
      fileChangeA(file, fileList) {
        if (this.fileLimit === fileList.length) {
          this.uploadClassA = 'uploadHide';
        }
        let reader = new FileReader();
        reader.readAsDataURL(file.raw);
        let that = this;
        reader.onload = function (evt) {
          //读取完文件之后会回来这里
          that.compareDTO.compareImageA.imageBase64 = evt.target.result.substring(
            reader.result.indexOf(",") + 1
          );
        };
      },
      handleRemoveA(file, fileList) {
        this.compareDTO.compareImageA.imageBase64 = '';
        this.similarity = '';
        this.result = '';
        this.uploadClassA = 'uploadShow';
      },
      handlePictureCardPreviewA(file) {
        this.dialogImageUrlA = file.url;
        this.dialogVisibleA = true;
      },

      fileChangeB(file, fileList) {
        if (this.fileLimit === fileList.length) {
          this.uploadClassB = 'uploadHide';
        }
        let reader = new FileReader();
        reader.readAsDataURL(file.raw);
        let that = this;
        reader.onload = function (evt) {
          //读取完文件之后会回来这里
          that.compareDTO.compareImageB.imageBase64 = evt.target.result.substring(
            reader.result.indexOf(",") + 1
          );
        };
      },
      handleRemoveB(file, fileList) {
        this.compareDTO.compareImageB.imageBase64 = '';
        this.similarity = '';
        this.result = '';
        this.uploadClassB = 'uploadShow';
      },
      handlePictureCardPreviewB(file) {
        this.dialogImageUrlB = file.url;
        this.dialogVisibleB = true;
      },
      handleFileLimit() {
        this.$message.warning("已达到最大图片数量：" + this.fileLimit)
      },
    },
    created() {
      //实例创建完成后,页面渲染前执行
      this.algorithmList('compare');
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

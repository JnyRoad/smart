<template>
  <div class="mncont" style="padding: 50px 100px;">
    <el-row style="text-align: center">
      <el-col :span="6">
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
      <el-col :span="6">
        <el-form ref="form" :inline="true" label-position="right" label-width="80px">
          <el-form-item label="证件选择">
            <el-select v-model="cardType">
              <el-option label="身份证正面" value="2"></el-option>
              <template v-if="algorithmType === 'ocr_wentong'">
                <el-option label="身份证背面" value="3"></el-option>
                <el-option label="临时身份证" value="4"></el-option>
                <el-option label="驾照" value="5"></el-option>
                <el-option label="护照" value="13"></el-option>
                <el-option label="港澳内陆通行证" value="22"></el-option>
                <el-option label="台湾往来大陆通行证" value="25"></el-option>
                <el-option label="企业营业执照" value="2008"></el-option>
              </template>
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="12">
        <div style="font-size: 20px;height: 40px;line-height: 40px;">识别结果</div>
      </el-col>
    </el-row>
    <el-row style="text-align: center">
      <el-col :span="6">
        <el-upload
          :class="['w300', uploadClass]"
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
      <el-col :span="6">
        <el-button @click.native="action" type="primary" style="margin-top:60px;">开始识别<i
          class="el-icon-arrow-right"></i></el-button>
      </el-col>
      <el-col :span="12">
        <el-form ref="form1" label-position="right" label-width="130px" style="display: flex; justify-content:space-between; ">
          <template v-if="cardType === '2'">
            <item2 :data="data"></item2>
          </template>
          <template v-if="cardType === '3'">
            <item3 :data="data"></item3>
          </template>
          <template v-if="cardType === '4'">
            <item4 :data="data"></item4>
          </template>
          <template v-if="cardType === '5'">
            <item5 :data="data"></item5>
          </template>
          <template v-if="cardType === '13'">
            <item13 :data="data"></item13>
          </template>
          <template v-if="cardType === '22'">
            <item22 :data="data"></item22>
          </template>
          <template v-if="cardType === '25'">
            <item25 :data="data"></item25>
          </template>
          <template v-if="cardType === '2008'">
            <item2008 :data="data"></item2008>
          </template>
          <div >


          <el-form-item v-if="algorithmType === 'ocr_wentong' && cardType !== '3'" label="头像">
            <template>
              <div class="best-images">
                <img @click="showBigImg(data.headImg)" v-if="data.headImg" :src="'data:image/jpg;base64,' + data.headImg" title="查看大图">
                <img v-else src="../assets/img/default.png">
              </div>
            </template>
          </el-form-item>
          <el-form-item v-if="algorithmType === 'ocr_wentong'" label="处理图">
            <template>
              <div class="best-images">
                <img @click="showBigImg(data.handleImg)" v-if="data.handleImg" :src="'data:image/jpg;base64,' + data.handleImg" title="查看大图">
                <img v-else src="../assets/img/idnone.png">
              </div>
            </template>
          </el-form-item>
          </div>
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
  import item2 from './ocr-item/item2'
  import item3 from './ocr-item/item3'
  import item4 from './ocr-item/item4'
  import item5 from './ocr-item/item5'
  import item13 from './ocr-item/item13'
  import item22 from './ocr-item/item22'
  import item25 from './ocr-item/item25'
  import item2008 from './ocr-item/item2008'
  export default {
    name: "Ocr",
    data: function () {
      return {
        dialogImageUrl: '',
        dialogVisible: false,
        fileLimit: 1,
        uploadClass: 'uploadShow',
        algorithms: [],
        algorithmType: 'ocr_wentong',
        cardType: '2',
        imageBase64: '',
        data: '',
        result: '',
        bigImgVisible: false,
        bigImgBase64: ''
      };
    },
    components:{
      item2,
      item3,
      item4,
      item5,
      item13,
      item22,
      item25,
      item2008
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
        if (!this.imageBase64) {
          this.$message.warning('请上传图片');
          return;
        }
        this.post('/test/ocr/' + this.algorithmType + '/' + this.cardType + '/test', this.imageBase64, function (r) {
          that.data = JSON.parse(r.data);
          that.result = JSON.stringify(r);
          that.$message.success("识别完成");
        }, function (e) {
          that.data = e.responseJSON;
          that.result = e.responseText;
          that.$message.error("识别失败：" + e.responseJSON.message);
        });
      },
      showBigImg (base64) {
        this.bigImgBase64 = 'data:image/jpg;base64,' + base64;
        this.bigImgVisible = true;
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
        this.data = {};
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
      this.algorithmList('ocr');
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

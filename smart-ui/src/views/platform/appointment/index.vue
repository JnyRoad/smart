<!--访客预约，从裕同官网跳转过来的  -->
<template>
  <div class="appointment">
    <basic-container>
      <div class="top">
        <div class="top-logo">
          <img src="img/logo.png" />
        </div>
      </div>
      <div class="ap-cont">
        <el-row class="flex" :gutter="20" justify="center">
          <el-col :span="15">
            <el-form ref="form" :model="visitor_form" :rules="visitor_rules" label-width="80px">
              <div class="apt-info">
                <p class="apt-p">来访者信息</p>
                <el-row class="row2" :gutter="80">
                  <el-col :span="12">
                    <el-form-item label="姓名" prop="name">
                      <el-input v-model="visitor_form.name"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号" prop="tel">
                      <el-input v-model="visitor_form.tel"></el-input>
                    </el-form-item>
                    <el-form-item label="验证码" prop="verification" class="verification-item">
                      <el-input v-model="visitor_form.verification"></el-input>
                      <el-button
                        type="primary"
                        @click="getVerifyCode"
                        :disabled="!verify_code"
                        plain
                      >获取验证码</el-button>
                    </el-form-item>
                    <el-form-item label="来访时间" prop="time1">
                      <el-date-picker v-model="visitor_form.time1" type="datetime" placeholder></el-date-picker>
                    </el-form-item>
                    <el-form-item label="离开时间" prop="time2">
                      <el-date-picker v-model="visitor_form.time2" type="datetime" placeholder></el-date-picker>
                    </el-form-item>
                    <el-form-item label="来访事由" prop="reason">
                      <el-select v-model="visitor_form.reason" placeholder>
                        <el-option label="区域一" value="shanghai"></el-option>
                        <el-option label="区域二" value="beijing"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="车牌号">
                      <el-input v-model="visitor_form.carid"></el-input>
                    </el-form-item>
                    <el-form-item label="所属单位">
                      <el-input v-model="visitor_form.firm"></el-input>
                    </el-form-item>
                    <el-upload
                      class="avatar-uploader"
                      action="https://jsonplaceholder.typicode.com/posts/"
                      :show-file-list="false"
                      :on-success="visitorImgSuccess"
                      :before-upload="beforeVisitorUpload"
                    >
                      <img v-if="visitor_form.photo" :src="visitor_form.photo" class="avatar" />
                      <i v-else class="avatar-uploader-icon"></i>
                      <div class="el-upload__text">点击上传</div>
                      <div class="el-upload__tip" slot="tip">
                        请上传您的人脸正面照，
                        <br />格式为png、jpg、jpeg、bmp，大小不超过3M
                      </div>
                    </el-upload>
                  </el-col>
                </el-row>
              </div>
              <div class="apt-info interviewee-info">
                <p class="apt-p">被访者信息</p>
                <el-row class="row2" :gutter="80">
                  <el-col :span="12">
                    <el-form-item label="姓名" prop="interviewee_name">
                      <el-input v-model="visitor_form.interviewee_name"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号" prop="interviewee_tel">
                      <el-input v-model="visitor_form.interviewee_tel"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>

              <div class="btndv">
                <el-button type="primary" @click="onVisitorSubmit" :disabled="!valid">确认预约</el-button>
              </div>
            </el-form>
          </el-col>
          <el-col :span="9">
            <div class="apt-info entourage-info">
              <p class="apt-p">
                <el-button type="text" class="addbtn" @click="entourageFormVisible = true">+ 添加随行人员</el-button>随行人员信息
              </p>
              <el-table :data="entourage_data" style="width: 100%">
                <el-table-column type="index" width="80px" label="序号"></el-table-column>
                <el-table-column prop="name" label="姓名"></el-table-column>
                <el-table-column prop="photo" label="人员照片">
                  <template slot-scope="scope">
                    <img class="sximg" :src="scope.row.photo" />
                  </template>
                </el-table-column>
                <el-table-column prop="address" width="80px" label="管理">
                  <template slot-scope="scope">
                    <el-button
                      class="delbtn"
                      @click="handleDel(scope.row,scope.$index)"
                      icon="el-icon-delete"
                      type="text"
                    ></el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="page-outer">
                <el-pagination
                  layout="prev, pager, next"
                  :page-size="entourage_page.pageSize"
                  :total="entourage_page.total"
                ></el-pagination>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-dialog
        title="添加随行人员"
        class="entourage-dialog"
        :visible.sync="entourageFormVisible"
        width="550px"
      >
        <el-form :model="entourage_form" :rules="entourage_rules">
          <el-form-item label="姓名" label-width="80px" prop="name">
            <el-input v-model="entourage_form.name" auto-complete="off"></el-input>
          </el-form-item>
          <el-upload
            class="avatar-uploader"
            action="https://jsonplaceholder.typicode.com/posts/"
            :show-file-list="false"
            :on-success="entourageImgSuccess"
            :before-upload="beforeEntourageUpload"
          >
            <img v-if="entourage_form.photo" :src="entourage_form.photo" class="avatar" />
            <i v-else class="avatar-uploader-icon"></i>
            <div class="el-upload__text">点击上传</div>
            <div class="el-upload__tip" slot="tip">
              请上传您的人脸正面照，
              <br />格式为png、jpg、jpeg、bmp，大小不超过3M
            </div>
          </el-upload>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="entourageFormVisible = false" plain>取 消</el-button>
          <el-button type="primary" @click="addEntourage()">确 定</el-button>
        </div>
      </el-dialog>
    </basic-container>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/appointment/index" as *;
</style>

<script>
import { fetchList } from "@/api/platform/appointment/index";
import { mapGetters } from "vuex";

export default {
  name: "appointment",
  data() {
    return {
      verify_code: true, //是否能发送手机验证码
      valid: false, //表单是否验证通过，false:没通过
      entourageFormVisible: false,
      entourage_page: {
        pageSize: 4,
        total: 0
      },
      visitor_form: {
        name: "",
        tel: "",
        verification: "",
        time1: "",
        time2: "",
        reason: [],
        interviewee_name: "",
        interviewee_tel: "",
        carid: "",
        firm: "",
        photo: ""
      },
      entourage_form: {
        //添加随行人员
        name: "",
        photo: ""
      },
      entourage_data: [
        {
          name: "张三",
          photo: ""
        },
        {
          name: "李四1",
          photo: "img/placeholder_people.png"
        },
        {
          name: "李四2",
          photo: "img/placeholder_people.png"
        },
        {
          name: "李四3",
          photo: "img/placeholder_people.png"
        },
        {
          name: "李四4",
          photo: "img/placeholder_people.png"
        }
      ],
      entourage_rules: {
        name: [
          { required: true, message: "请输入随行人员姓名", trigger: "blur" }
        ]
      },
      visitor_rules: {
        name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        tel: [{ required: true, message: "请输入手机号", trigger: "blur" }],
        verification: [
          { required: true, message: "请输入验证码", trigger: "blur" }
        ],
        time1: [
          {
            type: "date",
            required: true,
            message: "请选择来访时间",
            trigger: "change"
          }
        ],
        time2: [
          {
            type: "date",
            required: true,
            message: "请选择离开时间",
            trigger: "change"
          }
        ],
        reason: [
          { required: true, message: "请选择来访事由", trigger: "change" }
        ],
        interviewee_name: [
          { required: true, message: "请输入被访者姓名", trigger: "blur" }
        ],
        interviewee_tel: [
          { required: true, message: "请输入被访者手机号", trigger: "blur" }
        ]
      }
    };
  },
  created() {},
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    handleDel(row, index) {
      //删除随行人员
      this.entourage_data.splice(index, 1);
      this.$message({
        message: "删除随行人员成功",
        type: "success"
      });
    },
    addEntourage(row) {
      //添加随行人员，确认
      this.entourageFormVisible = false;
      this.$message({
        message: "添加随行人员成功",
        type: "success"
      });
    },
    getVerifyCode() {
      //获取验证码
    },
    onVisitorSubmit() {
      //确认预约
      this.$message({
        message: "访客预约成功",
        type: "success"
      });
    },
    visitorImgSuccess(res, file) {
      //访客图片上传
      this.visitor_form.photo = URL.createObjectURL(file.raw);
    },
    beforeVisitorUpload(file) {
      //访客图片上传之前（里面具体判断内容要修改）
      const isJPG = file.type === "image/jpeg";
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error("上传头像图片只能是 JPG 格式!来访者");
      }
      if (!isLt2M) {
        this.$message.error("上传头像图片大小不能超过 2MB!");
      }
      return isJPG && isLt2M;
    },
    entourageImgSuccess(res, file) {
      //随行人员图片上传
      this.entourage_form.photo = URL.createObjectURL(file.raw);
    },
    beforeEntourageUpload(file) {
      //随行人员图片上传之前（里面具体判断内容要修改）
      const isJPG = file.type === "image/jpeg";
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error("上传头像图片只能是 JPG 格式!随行人员");
      }
      if (!isLt2M) {
        this.$message.error("上传头像图片大小不能超过 2MB!");
      }
      return isJPG && isLt2M;
    }
  }
};
</script>

<style lang="scss" scoped>
::v-deep .basic-container {
  padding: 0;
}
::v-deep .el-card {
  background-color: #f0f2f5;
}
::v-deep .el-card__body {
  padding: 0;
}
.avatar-uploader {
  text-align: center;
}
::v-deep .avatar-uploader .el-upload {
  background-color: #eeeeee;
  border-radius: 3px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 155px;
  height: 155px;
  line-height: 155px;
}
::v-deep .avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
::v-deep .avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  text-align: center;
  background: #eeeeee url("/img/placeholder_web.png") no-repeat;
  background-position: center bottom;
  display: inline-block;
  float: left;
}
::v-deep .el-upload__text {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 30px;
  line-height: 30px;
  color: #fff;
  background: rgba(0, 0, 0, 0.3);
}
::v-deep .el-upload__tip {
  color: #999;
  margin-top: 15px;
}
::v-deep .avatar {
  max-width: 100%;
  height: auto;
  max-height: 100%;
  width: auto;
  display: inline-block;
}
</style>

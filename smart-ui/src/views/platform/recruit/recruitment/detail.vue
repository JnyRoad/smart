<!--招聘管理，招聘岗位，添加招聘岗位  -->
<template>
  <div class="my-basic-container center-card recruit_add">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-form
          ref="form"
          :model="editForm"
          :rules="edit_rules"
          label-width="90px"
          :disabled="!isEdit"
        >
          <div class="center-conent">
            <p class="box-orange">岗位信息</p>
            <el-row class="info-row">
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="园区" prop="parkId">
                    <parkSelect v-model="editForm.parkId" @doChange="parkChange"></parkSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="BU" prop="compId">
                    <buSelect
                      v-model="editForm.compId"
                      :parkId="editForm.parkId"
                      @getItem="getCompItem"
                      @doChange="buChange"
                    ></buSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="部门" prop="depId">
                    <deptSelect
                      v-model="editForm.depId"
                      :compId="editForm.compId"
                      @getItem="getDepItem"
                      @doChange="deptChange"
                    ></deptSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="岗位名称" prop="jobId">
                    <jobSelect
                      v-model="editForm.jobId"
                      :depId="editForm.depId"
                      @getItem="getJobItem"
                    ></jobSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="职层" prop="jcheId">
                    <jcheSelect
                      v-model="editForm.jcheId"
                      @getItem="getJcheItem"
                      @doChange="jcheChange"
                    ></jcheSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="福利层次" prop="welfareLevel">
                    <el-input v-model="editForm.welfareLevel" disabled></el-input>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24" class="lit-col">
                <el-form-item label="工资" prop="salaryType">
                  <el-radio-group v-model="editForm.salaryType">
                    <el-radio :label="2">薪资面议</el-radio>
                    <el-radio :label="1">具体数字</el-radio>
                  </el-radio-group>
                </el-form-item>
                <template v-if="salaryIsNum">
                  <el-form-item label prop="salaryStart" style="display:inline-block">
                    <el-input v-model="editForm.salaryStart">
                      <template slot="prepend">最低</template>
                      <template slot="append">元</template>
                    </el-input>
                    <span class="tip-span">至</span>
                  </el-form-item>
                  <el-form-item
                    label
                    prop="salaryEnd"
                    class="inline-item"
                    label-width="0"
                    style="display:inline-block"
                  >
                    <el-input v-model="editForm.salaryEnd">
                      <template slot="prepend">最高</template>
                      <template slot="append">元</template>
                    </el-input>
                    <span class="tip-span">请输入大于0的整数</span>
                  </el-form-item>
                </template>
              </el-col>
              <!-- <el-col :span="24" class="lit-col">
                <el-form-item label="年龄" prop="ageStart" style="display:inline-block">
                  <el-input v-model="editForm.ageStart">
                    <template slot="prepend">最低</template>
                    <template slot="append">岁</template>
                  </el-input>
                  <span class="tip-span">至</span>
                </el-form-item>
                <el-form-item label="" prop="ageEnd" class="inline-item" label-width="0" style="display:inline-block">
                  <el-input v-model="editForm.ageEnd" >
                    <template slot="prepend">最高</template>
                    <template slot="append">岁</template>
                  </el-input>
                  <span class="tip-span" v-if="isEdit">不能小于18岁</span>
                </el-form-item>
              </el-col>-->
              <el-col :span="24">
                <el-form-item label="专业" class="congent-tags">
                  <el-tag
                    :key="tag"
                    v-for="tag in profession"
                    :closable=" isEdit "
                    :disable-transitions="false"
                    @close="profDel(tag)"
                  >{{tag}}</el-tag>
                  <el-input
                    class="input-new-tag"
                    v-if="tagAddVisible"
                    v-model="tagAddValue"
                    ref="saveTagInput"
                    size="small"
                    @keyup.enter.native="tagAddfirm"
                    @blur="tagAddfirm"
                  ></el-input>
                  <el-button
                    class="button-new-tag"
                    v-if=" isEdit && !tagAddVisible"
                    size="small"
                    @click="showTagInput"
                    icon="el-icon-plus"
                  >添加专业</el-button>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="语言要求">
                  <el-radio-group v-model="editForm.reqLanguage" class="select-block">
                    <el-radio v-for="item in languages" :label="item" :key="item" border>{{item}}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="电脑要求">
                  <el-radio-group v-model="editForm.compRequire" class="select-block">
                    <el-radio v-for="item in computers" :label="item" :key="item" border>{{item}}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="学历要求">
                    <!-- <el-input v-model="editForm.education" placeholder="请输入最低学历"></el-input> -->
                    <el-select v-model="editForm.education" placeholder="请输入最低学历" clearable>
                      <el-option label="研究生" value="研究生"></el-option>
                      <el-option label="本科" value="本科"></el-option>
                      <el-option label="大专" value="大专"></el-option>
                      <el-option label="高中" value="高中"></el-option>
                      <el-option label="中专" value="中专"></el-option>
                      <el-option label="无" value="无"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="工作经验">
                    <el-input v-model="editForm.workYear" placeholder="请输入工作经验最低年限">
                      <template slot="append">年</template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-form-item label="岗位职责" prop="jobCotent">
                  <el-input
                    type="textarea"
                    style="display:none"
                    rows="6"
                    v-model="editForm.jobCotent"
                  ></el-input>
                  <div id="wangeditor">
                    <div ref="editorElem"></div>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="联系方式">
                    <el-input
                      type="textarea"
                      v-model="editForm.relation"
                      placeholder="请输入联系信息"
                      autosize
                    ></el-input>
                  </el-form-item>
                </el-col>
              </el-col>
            </el-row>
          </div>
          <div class="center-conent">
            <p class="box-orange">岗位设置</p>
            <el-row class="info-row">
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="招聘人数" prop="recruitNum">
                    <el-input v-model="editForm.recruitNum">
                      <template slot="append">人</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="1">
                  <span>&nbsp;</span>
                </el-col>
                <el-col :span="11">
                  <el-form-item label="招聘状态" prop="status">
                    <el-select v-model="editForm.status" placeholder="请选择">
                      <!-- <el-option label="状态一" value="1"></el-option>
                      <el-option label="状态二" value="2"></el-option>-->
                      <el-option
                        v-for="item in statusArray"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="有效期" prop="rangTime">
                    <el-date-picker
                      v-model="editForm.rangTime"
                      type="daterange"
                      :picker-options="pickerOptions"
                      @change="timeChanged"
                      range-separator="至"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      align="left"
                    ></el-date-picker>
                  </el-form-item>
                </el-col>
                <el-col :span="1">
                  <span>&nbsp;</span>
                </el-col>
              </el-col>
            </el-row>
          </div>
        </el-form>
        <div class="btns-bottom">
          <template v-if="!isEdit">
            <el-button type="primary" @click="isEdit = true">编辑</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="addSubmit()">保存</el-button>
          </template>
          <el-button type="primary" @click="editCancle()" plain>取消</el-button>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/recruit/add" as *;
</style>

<script>
import {
  fetchList,
  getById,
  putObj
} from "@/api/platform/recruit/recruitment_detail";
import { mapGetters } from "vuex";
import E from "wangeditor";
import { dateFormat } from "@/util/date";
const languageOptions = ["中文", "英语", "日语", "法语"];
const computerOptions = ["精通", "熟练", "一般"];
export default {
  name: "recruit",
  data() {
    // var validatStartAge = (rule, value, callback) => {
    //   if(Number(value)<18)
    //   {
    //     callback(new Error('最低年龄不能小于18岁'));

    //   }
    //   if(Number(value) > Number(this.editForm.ageEnd))
    //   {
    //     callback(new Error('最低年龄必须小于或等于最高年龄'));
    //   } else {
    //     callback();
    //   }
    // };
    // var validatEndAge = (rule, value, callback) => {
    //   if(Number(value)>50)
    //   {
    //     callback(new Error('最高年龄不能大于50岁'));

    //   }
    //   if(Number(value) < Number(this.editForm.ageStart))
    //   {
    //     callback(new Error('最高年龄必须大于或等于最低年龄'));
    //   } else {
    //     callback();
    //   }
    // };
    var validateStartSalary = (rule, value, callback) => {
      if (this.salaryIsNum) {
        if (Number(value) == 0) {
          callback(new Error("薪资必须大于0"));
        } else {
          callback();
        }
        if (Number(value) > Number(this.editForm.salaryEnd)) {
          callback(new Error("最低薪资必须小于或等于最高薪资"));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };
    var validateEndSalary = (rule, value, callback) => {
      if (this.salaryIsNum) {
        if (Number(value) == 0) {
          callback(new Error("薪资必须大于0"));
        } else {
          callback();
        }
        if (Number(value) < Number(this.editForm.salaryStart)) {
          callback(new Error("最高薪资必须大于或等于最低薪资"));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };
    var validateStartTime = (rule, value, callback) => {
      if (value >= this.editForm.endTime) {
        callback(new Error("开始时间应早于结束时间"));
      } else {
        callback();
      }
    };
    var validateEndTime = (rule, value, callback) => {
      if (value <= this.editForm.startTime) {
        callback(new Error("结束时间应晚于开始时间"));
      } else {
        callback();
      }
    };
    return {
      editor: null,
      isEdit: false, //true 编辑状态，false 非编辑状态
      tagAddVisible: false,
      tagAddValue: "",
      languages: languageOptions,
      computers: computerOptions,
      salaryIsNum: null, //表示薪资已选类型，true: 具体数字， false: 薪资面议 默认为具体数字
      profession: [], //专业
      editForm: {
        salaryType: 1,
        rangTime: []
      },
      statusArray: [
        {
          label: "招聘中",
          value: 1
        },
        {
          label: "招聘暂停",
          value: 2
        },
        {
          label: "招聘结束",
          value: 0
        }
      ],
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7; //8.64e7=1000*60*60*24一天
        },
        shortcuts: [
          {
            text: "一个月",
            onClick(picker) {
              const start = new Date();
              const end = new Date();
              end.setDate(start.getDate() + 30 * 1);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "两个月",
            onClick(picker) {
              const start = new Date();
              const end = new Date();
              end.setDate(start.getDate() + 30 * 2);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "三个月",
            onClick(picker) {
              const start = new Date();
              const end = new Date();
              end.setDate(start.getDate() + 30 * 3);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "半年",
            onClick(picker) {
              const start = new Date();
              const end = new Date();
              end.setDate(start.getDate() + 30 * 6);
              picker.$emit("pick", [start, end]);
            }
          }
        ]
      },
      edit_rules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        jobId: [
          { required: true, message: "请选择招聘岗位", trigger: "change" }
        ],
        compId: [{ required: true, message: "请选择BU", trigger: "change" }],
        depId: [{ required: true, message: "请选择部门", trigger: "change" }],
        jcheId: [{ required: true, message: "请选择职层", trigger: "change" }],
        welfareLevel: [
          { required: true, message: "请选择福利层次", trigger: "change" }
        ],
        salaryStart: [
          { required: true, message: "请输入最低薪资", trigger: "blur" },
          { validator: validateStartSalary, trigger: "blur" }
        ],
        salaryEnd: [
          { required: true, message: "请输入最高薪资", trigger: "blur" },
          { validator: validateEndSalary, trigger: "blur" }
        ],
        salaryType: [
          { required: true, message: "请选择工资类型", trigger: "change" }
        ],
        // ageStart: [
        //   { required: true, message: '请输入最小年龄', trigger: 'blur' },
        //   { validator: validatStartAge, trigger: 'blur' }
        // ],
        // ageEnd: [
        //   { required: true, message: '请输入最大年龄', trigger: 'blur' },
        //   { validator: validatEndAge, trigger: 'blur' }
        // ],
        recruitNum: [
          { required: true, message: "请输入招聘人数", trigger: "blur" }
        ],
        jobCotent: [
          // { required: true, message: '请输入岗位职责', trigger: 'blur' }
        ],
        status: [
          { required: true, message: "请选择招聘状态", trigger: "change" }
        ],
        startTime: [
          { required: true, message: "请输入开始时间", trigger: "blur" },
          { validator: validateStartTime, trigger: "blur" }
        ],
        endTime: [
          { required: true, message: "请输入结束时间", trigger: "blur" },
          { validator: validateEndTime, trigger: "blur" }
        ],
        rangTime: [
          { required: true, message: "请选择岗位有效期", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    var id = this.$route.params.id;
    var params = { id };
    var _this = this;
    getById(id).then(response => {
      var compId = parseInt(response.data.data.compId);
      var depId = parseInt(response.data.data.depId);
      var jobId = parseInt(response.data.data.jobId);
      this.editForm = response.data.data;
      if (response.data.data.jobCotent == null) {
        this.editForm.jobCotent = "";
      }
      // 如果最低薪资和最高薪资都为0 ，则显示为 '薪资面议'
      if (this.editForm.salaryStart == 0 && this.editForm.salaryEnd == 0) {
        this.salaryIsNum = false;
        this.$set(this.editForm, "salaryType", 2);
      } else {
        this.salaryIsNum = true;
        this.$set(this.editForm, "salaryType", 1);
      }

      this.$set(this.editForm, "rangTime", [
        this.editForm.startTime,
        this.editForm.endTime
      ]);

      var txt = "<div>" + this.editForm.jobCotent + "</div>";
      this.editor.txt.clear();
      this.editor.txt.html(txt);

      this.editForm.compId = compId;
      this.editForm.depId = depId;
      this.editForm.jobId = jobId;
      if (response.data.data.major != null && response.data.data.major != "")
        this.profession = response.data.data.major.split(",");
      else this.profession = [];
    });
  },
  mounted: function() {
    this.initEditor();
  },
  watch: {
    isEdit(val) {
      this.editor.$textElem.attr("contenteditable", val);
    },
    "editForm.salaryType": {
      handler: function(newVal) {
        if (newVal == 1) {
          this.salaryIsNum = true;
          //编辑页面不能这样写，首次加载会将原来的值清空
          // this.editForm.salaryStart = undefined;
          // this.editForm.salaryEnd = undefined;
        } else {
          this.salaryIsNum = false;
          this.editForm.salaryStart = 0;
          this.editForm.salaryEnd = 0;
        }
      },
      deep: true
    }
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/recruit/recruitment`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
    initEditor() {
      this.editor = new E(this.$refs.editorElem);
      // 编辑器的事件，每次改变会获取其html内容
      this.editor.customConfig.onchange = html => {
        this.editForm.jobCotent = html;
      };
      this.editor.customConfig.menus = [
        // 菜单配置
        "head", // 标题
        "bold", // 粗体
        "fontSize", // 字号
        "fontName", // 字体
        "italic", // 斜体
        "underline", // 下划线
        "strikeThrough", // 删除线
        "foreColor", // 文字颜色
        "backColor", // 背景颜色
        "link", // 插入链接
        "list", // 列表
        "justify", // 对齐方式
        "quote", // 引用
        "emoticon", // 表情
        "image", // 插入图片
        "table", // 表格
        //'code', // 插入代码
        "undo", // 撤销
        "redo" // 重复
      ];
      this.editor.create(); // 创建富文本实例
      this.editor.$textElem.attr("contenteditable", false);
    },
    addSubmit() {
      var major = "";
      for (var i = 0; i < this.profession.length; i++) {
        major += this.profession[i] + ",";
      }
      major = major.substring(0, major.lastIndexOf(","));
      this.editForm.major = major;
      this.$refs["form"].validate(valid => {
        if (valid) {
          putObj(this.editForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult == true) {
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
                this.isEdit = false;
                this.$router.go(-1);
              } else if (dataResult === false) {
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
            })
            .catch(() => {
              this.$notify({
                title: "失败",
                message: "修改失败",
                type: "error",
                duration: 2000
              });
            });
        } else {
          return false;
        }
      });
    },
    timeChanged() {
      this.editForm.startTime = dateFormat(this.editForm.rangTime[0]);
      this.editForm.endTime = dateFormat(this.editForm.rangTime[1]);
    },
    getCompItem(obj) {
      this.editForm.compName = obj.label;
    },
    getDepItem(obj) {
      this.editForm.depName = obj.label;
    },
    getJobItem(obj) {
      this.editForm.jobName = obj.label;
    },
    parkChange() {
      this.editForm.compId = undefined;
      this.editForm.depId = undefined;
      this.editForm.jobId = undefined;
    },
    buChange() {
      this.editForm.depId = undefined;
      this.editForm.jobId = undefined;
    },
    deptChange() {
      this.editForm.jobId = undefined;
    },
    getJcheItem(obj) {
      this.editForm.jcheName = obj.label;
    },
    // 获取职层名称
    jcheChange: function(val) {
      if (val == 2) {
        this.editForm.welfareLevel = "H";
      } else if (val == 3) {
        this.editForm.welfareLevel = "G";
      } else if (val == 12 || val == 4) {
        this.editForm.welfareLevel = "F";
      } else if (val == 5) {
        this.editForm.welfareLevel = "E";
      } else if (val == 6) {
        this.editForm.welfareLevel = "D";
      } else if (val == 7) {
        this.editForm.welfareLevel = "C";
      } else if (val == 11) {
        this.editForm.welfareLevel = "B2";
      } else if (val == 8) {
        this.editForm.welfareLevel = "B1";
      } else if (val == 9) {
        this.editForm.welfareLevel = "A";
      }
    },
    profDel(tag) {
      //删除'专业'标签
      this.profession.splice(this.profession.indexOf(tag), 1);
    },
    showTagInput() {
      this.tagAddVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },
    editCancle() {
      this.$router.go(-1);
    },
    tagAddfirm() {
      //添加'专业'标签
      let tagAddValue = this.tagAddValue;
      if (tagAddValue) {
        this.profession.push(tagAddValue);
      }
      this.tagAddVisible = false;
      this.tagAddValue = "";
    }
  }
};
</script>

<style lang="scss" scoped>
</style>

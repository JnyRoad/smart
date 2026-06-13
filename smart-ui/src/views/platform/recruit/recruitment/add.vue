<!--招聘管理，招聘岗位，添加招聘岗位  -->
<template>
  <div class="my-basic-container center-card recruit_add">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-form ref="form" :model="addForm" :rules="add_rules" label-width="90px">
          <div class="center-conent">
            <p class="box-orange">岗位信息</p>
            <el-row class="info-row">
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="园区" prop="parkId">
                    <parkSelect v-model="addForm.parkId" @doChange="parkChange"></parkSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="BU" prop="compId">
                    <buSelect
                      v-model="addForm.compId"
                      :parkId="addForm.parkId"
                      @getItem="getCompItem"
                      @doChange="buChange"
                    ></buSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="地区" prop="local">
                    <el-select v-model="addForm.local">
                      <template v-for="(item, index) in locals">
                        <el-option :label="item.workBaseName" :value="item.workBaseCode" :key="index"></el-option>
                      </template>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="部门" prop="depId">
                    <deptSelect
                      v-model="addForm.depId"
                      :compId="addForm.compId"
                      @getItem="getDepItem"
                      @doChange="deptChange"
                    ></deptSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="岗位名称" prop="jobId">
                    <jobSelect v-model="addForm.jobId" :depId="addForm.depId" @getItem="getJobItem"></jobSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="职层" prop="jcheId">
                    <jcheSelect
                      v-model="addForm.jcheId"
                      @getItem="getJcheItem"
                      @doChange="jcheChange"
                    ></jcheSelect>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="福利层次" prop="welfareLevel">
                    <el-input v-model="addForm.welfareLevel" disabled></el-input>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24" class="lit-col">
                <el-form-item label="工资" prop="salaryType">
                  <el-radio-group v-model="addForm.salaryType">
                    <el-radio :label="1">具体数字</el-radio>
                    <el-radio :label="2">薪资面议</el-radio>
                  </el-radio-group>
                </el-form-item>
                <template v-if="salaryIsNum">
                  <el-form-item label prop="salaryStart" style="display:inline-block">
                    <el-input
                      v-model="addForm.salaryStart"
                      onkeyup="value=value.replace(/^(0+)|[^\d]+/g,'')"
                    >
                      <template slot="prepend">最低</template>
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
                    <el-input
                      v-model="addForm.salaryEnd"
                      onkeyup="value=value.replace(/^(0+)|[^\d]+/g,'')"
                    >
                      <template slot="prepend">最高</template>
                    </el-input>
                    <span class="tip-span">请输入大于0的整数</span>
                  </el-form-item>
                </template>
              </el-col>
              <el-col :span="24">
                <el-form-item label="专业" class="congent-tags">
                  <el-tag
                    :key="tag"
                    v-for="tag in profession"
                    closable
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
                    v-else
                    class="button-new-tag"
                    size="small"
                    @click="showTagInput"
                    icon="el-icon-plus"
                  >添加专业</el-button>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="语言要求" prop="reqLanguage">
                  <el-radio-group v-model="addForm.reqLanguage" class="select-block">
                    <el-radio v-for="item in languages" :label="item" :key="item" border>{{item}}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="电脑要求" prop="compRequire">
                  <el-radio-group v-model="addForm.compRequire" class="select-block">
                    <el-radio v-for="item in computers" :label="item" :key="item" border>{{item}}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="学历要求" prop="education">
                    <!-- <el-input v-model="addForm.education" placeholder="请输入最低学历"></el-input> -->
                    <el-select v-model="addForm.education" placeholder="请输入最低学历" clearable>
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
                  <el-form-item label="工作经验" prop="workYear">
                    <el-input
                      v-model="addForm.workYear"
                      onkeyup="value=value.replace(/^(0+)|[^\d]+/g,'')"
                      placeholder="请输入工作经验最低年限"
                    >
                      <template slot="append">年</template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-form-item label="岗位职责" prop="jobCotent">
                  <div id="wangeditor">
                    <div ref="editorElem"></div>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="联系方式" prop="relation">
                    <el-input
                      type="textarea"
                      v-model="addForm.relation"
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
                    <el-input
                      v-model="addForm.recruitNum"
                      onkeyup="value=value.replace(/^(0+)|[^\d]+/g,'')"
                    >
                      <template slot="append">人</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="1">
                  <span>&nbsp;</span>
                </el-col>
                <el-col :span="11">
                  <el-form-item label="招聘状态" prop="status">
                    <el-select v-model="addForm.status" placeholder="请选择">
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
                      v-model="addForm.rangTime"
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
          <div class="btns-bottom">
            <el-button type="primary" @click="addSubmit()">保存</el-button>
            <el-button type="primary" @click="addCancel()" plain>取消</el-button>
          </div>
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/recruit/add" as *;
</style>

<script>
import { addObj, localList } from "@/api/platform/recruit/recruitment_add";
import { mapGetters } from "vuex";
import E from "wangeditor";
import { dateFormat } from "@/util/date";
const languageOptions = ["中文", "英语", "日语", "法语"];
const computerOptions = ["精通", "熟练", "一般"];
export default {
  name: "recruit_add",
  data() {
    var validateStartSalary = (rule, value, callback) => {
      if (this.salaryIsNum) {
        if (this.validatenull(value) || Number(value) == 0) {
          callback(new Error("薪资必须大于0"));
        } else {
          callback();
        }
        if (Number(value) > Number(this.addForm.salaryEnd)) {
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
        if (this.validatenull(value) || Number(value) == 0) {
          callback(new Error("薪资必须大于0"));
        } else {
          callback();
        }
        if (Number(value) < Number(this.addForm.salaryStart)) {
          callback(new Error("最高薪资必须大于或等于最低薪资"));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };
    var validateStartTime = (rule, value, callback) => {
      if (value >= this.addForm.endTime) {
        callback(new Error("开始时间应早于结束时间"));
      } else {
        callback();
      }
    };
    var validateEndTime = (rule, value, callback) => {
      if (value <= this.addForm.startTime) {
        callback(new Error("结束时间应晚于开始时间"));
      } else {
        callback();
      }
    };
    return {
      editor: null,
      tagAddVisible: false,
      tagAddValue: "",
      languages: languageOptions,
      computers: computerOptions,
      salaryIsNum: true, //表示薪资已选类型，true: 具体数字， false: 薪资面议 默认为具体数字
      locals: [],
      addForm: {
        parkId: undefined, //园区id
        jobId: undefined, //岗位id
        compId: undefined, //BU id
        compName: undefined, //BU 名称
        depId: undefined, //部门 id
        depName: undefined, //部门 名称
        jcheId: undefined, //职层 id
        jcheName: undefined, //职层 名称
        welfareLevel: undefined, //福利
        salaryType: 1, //薪资类型，1：具体数字，2：薪资面议, 默认为具体数字
        salaryStart: undefined, //最低薪资
        salaryEnd: undefined, //最高薪资
        // ageStart: undefined,//最低年龄
        // ageEnd: undefined,//最高年龄
        major: undefined, //专业
        reqLanguage: undefined, //语言
        compRequire: undefined, //电脑
        education: undefined, //学历
        workYear: undefined, //工作经验
        recruitNum: undefined, //招聘人数
        status: undefined, //招聘状态
        startTime: undefined, //开始时间
        endTime: undefined, //结束时间
        rangTime: undefined,
        local:undefined,//地区
      },
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
      profession: [], //专业
      statusArray: [
        //招聘状态集合
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
      add_rules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        compId: [{ required: true, message: "请选择BU", trigger: "change" }],
        local: [{ required: true, message: "请选择地区", trigger: "change" }],
        depId: [{ required: true, message: "请选择部门", trigger: "change" }],
        jobId: [
          { required: true, message: "请选择招聘岗位", trigger: "change" }
        ],
        jcheId: [{ required: true, message: "请选择职层", trigger: "change" }],
        welfareLevel: [
          { required: true, message: "请选择福利层次", trigger: "change" }
        ],
        salaryStart: [
          // { required: true, message: '请输入最低薪资', trigger: 'blur' },
          { validator: validateStartSalary, trigger: "blur" }
        ],
        salaryEnd: [
          // { required: true, message: '请输入最高薪资', trigger: 'blur' },
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
  created() {},
  mounted: function() {
    this.initEditor();
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    "addForm.salaryType": {
      handler: function(newVal) {
        if (newVal == 1) {
          this.salaryIsNum = true;
          this.addForm.salaryStart = undefined;
          this.addForm.salaryEnd = undefined;
        } else {
          this.salaryIsNum = false;
          this.addForm.salaryStart = 0;
          this.addForm.salaryEnd = 0;
        }
      },
      deep: true
    }
  },
  methods: {
    initEditor() {
      this.editor = new E(this.$refs.editorElem);
      // 编辑器的事件，每次改变会获取其html内容
      this.editor.customConfig.onchange = html => {
        this.addForm.jobCotent = html;
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
    },
    addSubmit() {
      var major = "";
      for (var i = 0; i < this.profession.length; i++) {
        major += this.profession[i] + ",";
      }
      major = major.substring(0, major.lastIndexOf(","));
      this.addForm.major = major;
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.addForm.local = parseInt(this.addForm.local);
          addObj(this.addForm).then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult == true) {
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
                this.clearFomr();
                this.editor.txt.clear();
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
                message: "添加失败",
                type: "error",
                duration: 2000
              });
            });
        } else {
          return false;
        }
      });
    },
    clearFomr() {
      if (this.$refs["form"] != undefined) {
        this.$refs["form"].resetFields();
      }
      this.profession = [];
    },
    addCancel() {
      this.$router.go(-1);
    },
    timeChanged() {
      this.addForm.startTime = dateFormat(this.addForm.rangTime[0]);
      this.addForm.endTime = dateFormat(this.addForm.rangTime[1]);
    },
    getCompItem(obj) {
      this.addForm.compName = obj.label;
      this.getLocal(obj);
    },
    getDepItem(obj) {
      this.addForm.depName = obj.label;
    },
    getJobItem(obj) {
      this.addForm.jobName = obj.label;
    },
    parkChange() {
      this.addForm.compId = undefined;
      this.addForm.depId = undefined;
      this.addForm.jobId = undefined;
      this.addForm.local = undefined;
    },
    buChange(obj) {
      this.addForm.depId = undefined;
      this.addForm.jobId = undefined;
      this.addForm.local = undefined;
    },
    deptChange() {
      this.addForm.jobId = undefined;
    },
    getJcheItem(obj) {
      this.addForm.jcheName = obj.label;
    },
    getLocal(obj) {
        localList(Object.assign(
          {
            parkId: this.addForm.parkId,
            workCompId: obj.value
          }
        )).then(response => {
          this.locals = response.data.data;
        });
    },
    // 获取职层名称
    jcheChange: function(val) {
      if (val == 2) {
        this.addForm.welfareLevel = "H";
      } else if (val == 3) {
        this.addForm.welfareLevel = "G";
      } else if (val == 12 || val == 4) {
        this.addForm.welfareLevel = "F";
      } else if (val == 5) {
        this.addForm.welfareLevel = "E";
      } else if (val == 6) {
        this.addForm.welfareLevel = "D";
      } else if (val == 7) {
        this.addForm.welfareLevel = "C";
      } else if (val == 11) {
        this.addForm.welfareLevel = "B2";
      } else if (val == 8) {
        this.addForm.welfareLevel = "B1";
      } else if (val == 9) {
        this.addForm.welfareLevel = "A";
      }
    },
    profDel(tag) {
      this.profession.splice(this.profession.indexOf(tag), 1);
    },
    showTagInput() {
      this.tagAddVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },
    tagAddfirm() {
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

<!--访客预约，预约记录  -->
<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="emailForm" :rules="emailRule" :inline="false" class="dot-form" :model="emailForm" label-width="150px">
            <el-form-item label="所属园区" class="w1" prop="parkId">
              <parkSelect v-model="parkId" @doChange="getPushEmail"></parkSelect>
            </el-form-item>
            <el-row class>
              <el-col>
                <p class="tip">被访限制规则</p>
                <div class="tip1">使用说明：员工属于以下选中的职层，外来人员无法设置其为被访人</div>
                <el-form-item label prop="jcheList">
                  <el-checkbox-group v-model="jcheForm.jcheList" class="zclist">
                    <el-checkbox v-for="(item, index) in jcheListOption" :label="item.value" :key="index">{{ item.label }} </el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
            </el-row>
            <!--            <p class="tip">访客通行设置</p>-->
            <!--            <div class="tip1">使用说明：访客预约审批通过后将自动获得以下配置的通行权限，于【区域管理】-【通关权限策略】可进行设置</div>-->
            <!--            <el-form-item class="w1" label="访客人员通行权限">-->
            <!--              <el-select v-model="authForm.personAuth" placeholder="请选择通行权限">-->
            <!--                <template v-for="(item, index) in personAuthList">-->
            <!--                  <el-option :label="item.authorityName" :value="item.id" :key="index"></el-option>-->
            <!--                </template>-->
            <!--              </el-select>-->
            <!--            </el-form-item>-->
            <!--            <el-form-item class="w1" label="访客车辆通行权限">-->
            <!--              <el-select v-model="authForm.vehicleAuth" placeholder="请选择通行权限">-->
            <!--                <template v-for="(item, index) in vehicleAuthList">-->
            <!--                  <el-option :label="item.authorityName" :value="item.id" :key="index"></el-option>-->
            <!--                </template>-->
            <!--              </el-select>-->
            <!--            </el-form-item>-->
            <p class="tip">访客记录推送设置</p>
            <div class="tip1">使用说明：设置访客记录邮件推送，系统会定时生成excel文件，发送到推送人邮箱里</div>
            <el-form-item label="设置记录统计周期" class="w1" prop="type">
              <el-select v-model="emailForm.type" placeholder="请选择统计周期">
                <el-option v-for="item in periods" :key="item.value" :label="item.label" :value="item.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="设置记录推送人">
              <div>
                <p>
                  已添加
                  <span style="color: #ed6d00">{{ emailForm.emails.length }}</span> 位推送人信息
                </p>
                <table class="tbl">
                  <tr>
                    <td>推送人姓名</td>
                    <td>推送人邮箱</td>
                  </tr>
                  <tr v-for="(item, index) in emailForm.emails" :key="index">
                    <td>
                      <el-input type="text" v-model="item.receiver" readonly></el-input>
                    </td>
                    <td>
                      <el-input type="text" v-model="item.email" readonly></el-input>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-delete" @click="delLine(index)"></el-button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <el-form-item label prop="receiver">
                        <el-input type="text" v-model="emailForm.receiver" placeholder="点击输入姓名"></el-input>
                      </el-form-item>
                    </td>
                    <td>
                      <el-form-item label prop="email">
                        <el-input type="text" v-model="emailForm.email" placeholder="点击输入邮箱"></el-input>
                      </el-form-item>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-plus" @click="addLine"></el-button>
                      </div>
                    </td>
                  </tr>
                </table>
                <div class="tip2">
                  <p>1、输入姓名、邮箱。（输入后要点击右侧的 " + " 才视为完成一条推送人信息的添加 ）</p>
                  <p>2、推送的邮件名为：{园区名称}-访客记录报表-统计起止时间</p>
                  <p>如“大岭山园区-访客记录报表-20190901-20190902”</p>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="访客邀约">
              <div class="row">
                <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="1" :inactive-value="0" v-model="needApprovalObj.needApproval"></el-switch>
                <span style="padding-left: 15px;">是否需要上级领导审批</span>
              </div>
            </el-form-item>
            <el-form-item label="疫情信息管控" v-if="parkId === 20381 || parkId === 10001">
              <div class="row">
                <table class="tbl">
                  <tr>
                    <td>字段名称</td>
                    <td>是否显示</td>
                  </tr>
                  <tr>
                    <td>
                      行程码
                    </td>
                    <td>
                      <el-checkbox v-model="isEpideObj.isTripCode"></el-checkbox>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      健康码
                    </td>
                    <td>
                     <el-checkbox v-model="isEpideObj.isHealthCode"></el-checkbox>
                    </td>
                  </tr>
                </table>
                <div class="tip2">
                  <p>访客信息页面，将显示行程码/当地健康码输入项</p>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="访客提示">
              <div class="row">
                <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="1" :inactive-value="0" v-model="isNeedNoticeObj.isNeedNotice"></el-switch>
                <span style="padding-left: 15px;">开启后，在访客首页弹框提示</span>
              </div>
            </el-form-item>
            <el-form-item label>
              <div id="wangeditor">
                <div ref="noticeContent"></div>
              </div>
            </el-form-item>
            <el-form-item label>
              <el-button type="primary" @click="saveInfo()">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchListEmail, editJche, editAuth, editObj, getJcheId, fetchAuthList, fetchAuthId, saveBatchConfig, getBatchConfig, saveEpide} from './visitor_service'
import { mapGetters } from 'vuex'
import { getJchesObj } from '@/api/platform/_publicService'
import E from "wangeditor";
const periodOption = [
  {
    label: '每一天',
    value: 0
  },
  {
    label: '每一周',
    value: 1
  },
  {
    label: '每一月',
    value: 2
  }
]
export default {
  name: 'visitor',
  data() {
    var nameValid = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error('请输入推送人姓名'))
      } else {
        if (/^[0-9a-zA-Z\u4e00-\u9fa5_\s]{2,20}$/.test(value) == false) {
          callback(new Error('姓名应包含2-20个字符，可为汉字、数字、字母（大小写）、下划线!'))
        } else {
          callback()
        }
      }
    }
    var emailvalid = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error('请输入推送人邮箱'))
      } else {
        if (!/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((.[a-zA-Z0-9_-]{2,3}){1,2})$/.test(value.replace(/(^\s*)|(\s*$)/g, ''))) {
          callback(new Error('请输入正确的邮箱地址'))
        } else {
          callback()
        }
      }
    }
    return {
      disable: true,
      jcheListOption: [],
      emailForm: {
        parkId: undefined,
        type: '',
        emails: []
      },
      authForm: {
        personAuth: '',
        vehicleAuth: '',
        parkId: '',
        businessCode: '',
        type: '',
        authId: ''
      },
      personAddForm: {
        businessCode: 1,
        type: 1
      },
      vehicleAddForm: {
        businessCode: 1,
        type: 2
      },
      personAuthList: [],
      vehicleAuthList: [],
      jcheForm: {
        jcheList: [],
        parkId: ''
      },
      parkId: '',
      periods: periodOption,
      emailRule: {
        parkId: [{ required: true, message: '请选择关联园区', trigger: 'blur' }],
        receiver: [{ validator: nameValid, trigger: 'blur' }],
        email: [{ validator: emailvalid, trigger: 'blur' }]
      },
      needApprovalObj: {
        needApproval: 0
      },
      isNeedNoticeObj: {
        isNeedNotice: 0,
        content: ''
      },
      editor: null,
      isEpideObj:{
        isTripCode: 0, //行程码
        isHealthCode: 0, //健康码
      },
      isEpideId: ''
    }
  },
  created() {
    if (this.$route.params.parkId != 0) {
      this.parkId = parseInt(this.$route.params.parkId)
      this.getJcheList(this.parkId)
      this.fetchList()
      this.getAuthList(this.parkId)
      this.personAddForm.parkId = this.parkId
      this.vehicleAddForm.parkId = this.parkId
      this.getBatchConfig()
    }
  },
  mounted: function () {
    this.initEditor()
  },
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    getPushEmail(val) {
      this.parkId = val
      this.fetchList()
      this.getJcheList(this.parkId)
      this.getAuthList(this.parkId)
      this.personAddForm.parkId = this.parkId
      this.vehicleAddForm.parkId = this.parkId
      this.getBatchConfig()
    },
    fetchList() {
      fetchListEmail(
        Object.assign({
          parkId: this.parkId
        })
      )
        .then((response) => {
          var data = response.data.data
          if (data.length > 0) {
            this.emailForm.emails = data
            this.emailForm.type = data[0].type
          } else {
            this.emailForm.emails = []
            this.emailForm.type = null
          }
        })
        .catch(err => { console.error(err) })
    },
    getJcheList(parkId) {
      getJchesObj().then((response) => {
        let self = this
        ;(self.jcheListOption = []),
          response.data.data.forEach((item, $index) => {
            self.jcheListOption.push({ label: item.typeName, value: item.typeCode })
          })
      })

      if (parkId != null) {
        getJcheId(
          Object.assign({
            parkId: parkId
          })
        ).then((response) => {
          let self = this
          self.jcheForm.jcheList = response.data.data
        })
      }
    },
    getAuthList(parkId) {
      let personParams = Object.assign({ parkId: parkId, type: 2 })
      let vehicleParams = Object.assign({ parkId: parkId, type: 3 })
      fetchAuthList(
        Object.assign(
          {
            current: 1,
            size: 999
          },
          personParams
        )
      ).then((response) => {
        let self = this
        self.personAuthList = response.data.data.records
      })

      fetchAuthList(
        Object.assign(
          {
            current: 1,
            size: 999
          },
          vehicleParams
        )
      ).then((response) => {
        let self = this
        self.vehicleAuthList = response.data.data.records
      })

      fetchAuthId(
        Object.assign({
          parkId: parkId,
          businessCode: 1,
          type: 1
        })
      ).then((response) => {
        let self = this
        self.authForm.personAuth = response.data.data
      })

      fetchAuthId(
        Object.assign({
          parkId: parkId,
          businessCode: 2,
          type: 2
        })
      ).then((response) => {
        let self = this
        self.authForm.vehicleAuth = response.data.data
      })
    },
    saveInfo() {
      Promise.all([
        this.saveEmail(),
        // this.saveVehiceAuth(this.authForm.vehicleAuth),
        // this.saveAuthInfo(this.authForm.personAuth),
        this.saveJcheInfo(this.jcheForm),
        this.saveBatchConfig(),
        this.saveEpide()
      ]).then((arr) => {
        this.$router.go(-1)
      })
    },
    async saveEpide(){ //疫情管控配置
      if(this.parkId == 10001 || this.parkId == 20381){
        const obj = {
          isHealthCode: 0,
          isTripCode: 0
        }
        // this.isEpideObj
        switch(this.isEpideObj.isHealthCode){
          case true: obj.isHealthCode = 1
          break
          case false: obj.isHealthCode = 0
          break
        }
        switch(this.isEpideObj.isTripCode){
          case true: obj.isTripCode = 1
          break
          case false: obj.isTripCode = 0
          break
        }
        const params = {
          businessType: 1,
          configType: 4,
          parkId: this.parkId,
          value: JSON.stringify(obj),
          id: this.isEpideId
        }
        const res = await saveEpide(params)
      }
    },
    async saveBatchConfig() {
      let obj1 = {
        needApproval: this.needApprovalObj.needApproval
      }
      let obj2 = {
        isNeedNotice: this.isNeedNoticeObj.isNeedNotice,
        content: this.isNeedNoticeObj.content
      }
      let configObj1 = {}
      let configObj2 = {}
      if(this.needApprovalObj.id){
        configObj1 = this.needApprovalObj
        configObj1.value = JSON.stringify(obj1)
      }else{
        configObj1 = {
          businessType: 1, //固定1 访客预约
          value: JSON.stringify(obj1),
          configType: 1, //访客邀约
          parkId: this.parkId,
        }
      }
      if(this.isNeedNoticeObj.id){
        configObj2 = this.isNeedNoticeObj
        configObj2.value = JSON.stringify(obj2)
      }else{
        configObj2 = {
          businessType: 1, //固定1 访客预约
          value: JSON.stringify(obj2),
          configType: 2, //访客提示
          parkId: this.parkId,
        }
      }
      let arr = [configObj1, configObj2]
      const res = await saveBatchConfig(arr)

    },
    async getBatchConfig() {
      this.initBatchConfig()
      let obj = {
        businessTypes: [1],
        configTypes: [1,2,4],
        parkId: this.parkId
      }
      const res = await getBatchConfig(obj)
      if(res.data.code===0 && res.data.data.length>0){
        res.data.data.forEach(el=>{
          if(el.configType === 1){
            this.needApprovalObj = el
          }else if(el.configType === 2){
            this.isNeedNoticeObj = el
          }else if(el.configType === 4){
            this.isEpideObj = JSON.parse(el.value)
            switch(this.isEpideObj.isHealthCode){
              case 0: this.isEpideObj.isHealthCode = false
              break
              case 1: this.isEpideObj.isHealthCode = true
              break
            }
            switch(this.isEpideObj.isTripCode){
              case 0: this.isEpideObj.isTripCode = false
              break
              case 1: this.isEpideObj.isTripCode = true
              break
            }
            this.isEpideId = el.id
          }
        })
        if(this.needApprovalObj.value){
          let obj1 = JSON.parse(this.needApprovalObj.value)
          this.$set(this.needApprovalObj, 'needApproval', obj1.needApproval)
        }
        if(this.isNeedNoticeObj.value){
          let obj2 = JSON.parse(this.isNeedNoticeObj.value)
          this.$set(this.isNeedNoticeObj, 'isNeedNotice', obj2.isNeedNotice)
          this.$set(this.isNeedNoticeObj, 'content', obj2.content)
          let txt = "<div>" + obj2.content + "</div>";
          this.editor.txt.clear()
          this.editor.txt.html(txt)
        }
      }
    },
    initBatchConfig(){
      this.needApprovalObj = {
        needApproval: 0
      }
      this.isNeedNoticeObj = {
        isNeedNotice: 0,
        content: ''
      }
      this.editor && this.editor.txt.clear()
    },
    initEditor() {
      this.editor = new E(this.$refs.noticeContent);
      // 编辑器的事件，每次改变会获取其html内容
      this.editor.customConfig.onchange = html => {
        // this.emailForm.tempContent = html;
        this.isNeedNoticeObj.content = html
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
    async saveEmail() {
      this.emailForm.parkId = this.parkId
      const response = await editObj(this.emailForm)
      if (!response.data.data) {
        this.$message.error(response.data.msg)
      }
    },
    async saveAuthInfo(formName) {
      if (formName != '' && formName != null) {
        this.personAddForm.authId = formName
        const response = await editAuth(this.personAddForm)
        if (!response.data.data) {
          this.$message.error(response.data.msg)
        }
      }
    },
    async saveVehiceAuth(formName) {
      if (formName != '' && formName != null) {
        this.vehicleAddForm.authId = formName
        const response = await editAuth(this.vehicleAddForm)
        if (!response.data.data) {
          this.$message.error(response.data.msg)
        }
      }
    },
    async saveJcheInfo(formName) {
      formName.parkId = this.parkId
      const response = await editJche(formName)
      if (!response.data.data) {
        this.$message.error(response.data.msg)
      }
    },
    delLine(index) {
      this.emailForm.emails.splice(index, 1)
    },
    addLine() {
      let nameIsOk = false
      let emailIsOk = false
      this.$refs.emailForm.validateField('receiver', (msg) => {
        if (this.validatenull(msg)) {
          nameIsOk = true
        } else {
          nameIsOk = false
        }
      })
      this.$refs.emailForm.validateField('email', (msg) => {
        if (this.validatenull(msg)) {
          emailIsOk = true
        } else {
          emailIsOk = false
        }
      })
      if (nameIsOk && emailIsOk) {
        this.emailForm.emails.push({
          receiver: this.emailForm.receiver,
          email: this.emailForm.email
        })
        this.emailForm.receiver = ''
        this.emailForm.email = ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 900px;
  padding: 50px 0 0 60px;
  .el-checkbox+.el-checkbox {
    margin-left: 0;
  }
  .el-checkbox{
    margin: 0 20px 0 0;
  }
  .w1 {
    width: 500px;
  }

  .tip {
    color: #ed6d00;
    font-size: 16px;
    padding-bottom: 10px;
    margin-bottom: 20px;
  }

  .tip1 {
    color: #999;
    font-size: 14px;
    padding-bottom: 10px;
    margin-bottom: 20px;
  }

  .tip2 {
    color: #999;
    font-size: 12px;
    line-height: 25px;
  }

  .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label:before {
    content: '';
  }

  .tbl {
    width: 100%;
    margin-bottom: 50px;

    .el-input__inner {
      text-align: center;
      border: none;
      outline: none;
    }

    td {
      width: 50%;
      text-align: center;
      border: 1px solid #e0e0e0;
      position: relative;
    }

    .line-btn {
      position: absolute;
      right: -40px;
      top: 0;

      .el-button {
        font-size: 18px;
      }
    }
  }
}
</style>

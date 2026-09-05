<!--保密区设置  -->
<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <div class="content">
          <!-- <div class="top-menu" style="margin-bottom:20px;"> -->
        <!-- </div> -->
          <el-form ref="areaForm" :rules="whiteListRule" :inline="false" class="dot-form" :model="areaForm" label-width="85px">
            <el-form-item label="所属园区" class="w1" prop="parkId">
              <!-- @doChange="getPushEmail" -->
              <parkSelect v-model="areaForm.parkId" @doChange="getPushEmail"></parkSelect>
            </el-form-item>
            <el-row class>
              <el-col>
                <div class="row row1">
                  <p class="tip">保密区OA审批信息设置</p>
                  <el-button type="text" icon="el-icon-refresh" @click="doSyncTask">手动实时刷新</el-button>
                </div>
                <!-- <el-form-item label prop="">
                  <div>
                    <span>同步OA保密权限表单中区域字典,每</span>
                    <el-input type="text" v-model="" class="w2"></el-input>
                    <span>分钟 从OA定时同步</span>
                  </div>
                </el-form-item> -->
                <el-form-item label prop="">
                  <div class="row">
                    <div class="dv1"> 申请进入区域类别 </div>
                    <div class="dv1"> OA申请进入区域 </div>
                    <div class="dv2"></div>
                    <div class="dv3"> 关联平台区域权限（可多选） </div>
                  </div>
                  <div class="row" v-for="(item, index) in areaList" :key="index">
                    <div class="dv1">
                      <el-tooltip placement="bottom">
                        <div slot="content" style="line-height: 25px">
                          {{item.factoryTypeDesc}}
                        </div>
                        <el-tag class="-ellipsis" type="warning">{{item.factoryTypeDesc}}</el-tag>
                      </el-tooltip>
                    </div>
                    <div class="dv1">
                      <el-tooltip placement="bottom">
                        <div slot="content" style="line-height: 25px">
                          {{item.oaAreaName}}
                        </div>
                        <el-tag class="-ellipsis" type="warning">{{item.oaAreaName}}</el-tag>
                      </el-tooltip>
                    </div>
                    <div class="dv2" v-if="item.authList && item.authList.length>0"> 已设置 </div>
                    <div class="dv2 ft-danger" v-else> 未设置 </div>
                    <div class="dv3">
                      <el-tooltip placement="bottom" v-if="item.authList && item.authList.length>0">
                        <div slot="content" style="line-height: 25px">
                          <div v-for="(item2, index2) in item.authList" :key="index2">
                            {{item2.authName}}
                          </div>
                        </div>
                        <el-tag class="-ellipsis">{{item.authList[0].authName}}<span class="num">+{{item.authList.length}}</span></el-tag>
                      </el-tooltip>
                    </div>
                    <div class="dv4"> <el-button type="text" icon="el-icon-edit" @click="addAuth(item)" round></el-button> </div>
                    <!-- <div class="dv4"> <el-button type="text" icon="el-icon-delete" @click="delAuth(item)" round :disabled="!item.authList || item.authList.length===0"></el-button> </div> -->
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-form ref="whiteListForm" :rules="whiteListRule" :inline="false" class="dot-form" :model="whiteListForm" label-width="85px">
            <p class="tip">保密区权限自动删除配置</p>
            <el-form-item label prop="deleteDay">
              <div>
                <span>系统检测员工保密区设备上，</span>
                <el-input type="text" v-model="whiteListForm.deleteDay" class="w2"></el-input>
                <span>天没有进出记录，则删除员工权限。</span>
              </div>
            </el-form-item>
            <el-form-item label>
              <div class="row">
                <span>其中过滤掉以下场景的天数：</span>
                <div class="zclist">
                  <el-checkbox :true-label="1" :false-label="0" v-model="whiteListForm.isHoliday">节假日</el-checkbox>
                  <el-checkbox :true-label="1" :false-label="0" v-model="whiteListForm.isBusiness">出差</el-checkbox>
                  <el-checkbox :true-label="1" :false-label="0" v-model="whiteListForm.isLeave">请假</el-checkbox>
                  <el-checkbox :true-label="1" :false-label="0" v-model="whiteListForm.isCompensatory">调休</el-checkbox>
                </div>
              </div>
            </el-form-item>
            <el-form-item label prop="isWhiteList">
              <div class="row">
                <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="1" :inactive-value="0" v-model="whiteListForm.isWhiteList"></el-switch>
                <span style="padding-left: 15px;">启动白名单后，白名单里的人员将不受检测规则影响</span>
              </div>
            </el-form-item>
            <el-form-item label="" v-if="whiteListForm.isWhiteList===1">
              <div>
                <p>
                  已添加
                  <span style="color: #ed6d00">{{ whiteListForm.whiteList.length }}</span> 个白名单信息
                </p>
                <table class="tbl">
                  <tr>
                    <td>工号</td>
                    <td>姓名</td>
                  </tr>
                  <tr v-for="(item, index) in whiteListForm.whiteList" :key="index">
                    <td>
                      <el-input type="text" v-model="item.staffBadge" readonly></el-input>
                    </td>
                    <td>
                      <el-input type="text" v-model="item.staffName" readonly></el-input>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-delete" @click="delLine(index)"></el-button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <el-form-item label prop="staffBadge">
                        <el-input type="text" v-model="whiteListForm.staffBadge" @keyup.enter.native="getStaffDetail('whiteListForm')" placeholder="点击输入工号，按回车查询姓名"></el-input>
                      </el-form-item>
                    </td>
                    <td>
                      <el-form-item label prop="staffName">
                        <el-input type="text" v-model="whiteListForm.staffName" placeholder="姓名自动获取" readonly></el-input>
                      </el-form-item>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-plus" @click="addLine"></el-button>
                      </div>
                    </td>
                  </tr>
                </table>
                <div class="tip2">
                  <p>输入工号后，按回车键查询姓名。（输入后要点击右侧的 " + " 才视为完成一条白名单信息的添加 ）</p>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="">
              <div class="row dry-run-setting">
                <el-switch
                  active-color="#10CC8F"
                  inactive-color="#909399"
                  :active-value="1"
                  :inactive-value="0"
                  v-model="whiteListForm.dryRun"
                ></el-switch>
                <span style="padding-left: 15px;">演练模式：只记录命中结果，不删除权限、不生成设备任务</span>
              </div>
            </el-form-item>
            <el-form-item label>
              <el-button type="primary" @click="saveInfo()">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
    <AuthDialog title="关联平台区域权限" :itemObj="oaAreaObj" ref="authDialog" :parkId="areaForm.parkId"/>
    <DelAuthDialog title="删除区域权限" :itemObj="oaAreaObj" ref="delAuthDialog"/>
  </div>
</template>

<script>
import { bsSecurityAreaApi } from './_service'
import { getStaffDetail } from "@/api/platform/_publicService"
import AuthDialog from './components/batchAuth'
import DelAuthDialog from './components/delAuth'
import { mapGetters } from 'vuex'

export default {
  name: 'visitor',
  components: {
    AuthDialog,
    DelAuthDialog
  },
  data() {
    var badgeValid = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error('请输入工号'))
      } else {
        callback()
      }
    }
    var nameValid = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error('请在工号输入框按回车查询姓名'))
      } else {
        callback()
      }
    }
    return {
      disable: true,
      oaAreaObj: {},
      areaForm: {
        parkId: '',
      },
      areaList: [],
      whiteListForm: {
        parkId: '',
        isBusiness: '',
        isCompensatory: '',
        isHoliday: '',
        isLeave: '',
        isWhiteList: 0,
        dryRun: 0,
        whiteList: []
      },
      whiteListRule: {
        staffBadge: [{ validator: badgeValid, trigger: 'blur' }],
        staffName: [{ validator: nameValid, trigger: 'blur' }]
      }
    }
  },
  created() {
    if (this.$route.params.parkId != 0) {
      this.areaForm.parkId = parseInt(this.$route.params.parkId)
    }
    this.initData()
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  watch: {
    'areaForm.parkId'(val){
      this.initData()
    }
  },
  methods: {
    getPushEmail(val){
      this.areaForm.parkId = val;
      this.initData()
    },
    refresh(){},
    async saveInfo() {
      // await this.validateForm('whiteListForm')
      //开启白名单时，切名单为空时进行验证
      if(this.whiteListForm.isWhiteList===1){
        if(!this.whiteListForm.whiteList || this.whiteListForm.whiteList.length===0){
          await this.validateForm('whiteListForm')
        }
      }

      // 演练字段存量可能为空，保存前统一为后端接受的 0/1。
      this.whiteListForm.dryRun = this.normalizeDryRun(this.whiteListForm.dryRun)

      //保密区权限参数
      let areaArr = []
      this.areaList.forEach(el=>{
        areaArr.push(
          {
            authIds: el.authList?el.authList:[],
            oaAreaId: el.oaAreaId,
            oaAreaName: el.oaAreaName,
            parkId: el.parkId
          }
        )
      })

      //白名单参数
      if(this.whiteListForm.whiteList.length>0){
        this.whiteListForm.whiteList.forEach(el=>{
          el.parkId = this.areaForm.parkId
        })
      }
      Promise.all([
        bsSecurityAreaApi.editAreaListObj(areaArr),
        bsSecurityAreaApi.editWhiteListObj(this.whiteListForm)
      ]).then(arr=>{
         if(arr.length===2 && arr[0] && arr[1]){
          if(arr[0].data.code===0 && arr[0].data.data && arr[1].data.code===0 && arr[1].data.data){
            this.$message({
              message: '修改成功',
              type: 'success'
            });
            this.initData()
          }
        }
      });
    },
    initData(){
      Promise.all([
        this.getOaArea(),
        this.getWhiteListConfig()
      ]).then(arr=>{
        // this.loading&&this.loading.close();
      });
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
    /**
     * 保存OA区域设置
     */
    async saveOaArea(data){
      const res = await bsSecurityAreaApi.editAreaListObj(data)
    },
    /**
     *保存白名单设置
     */
    async saveWhiteListConfig(data){
      const res = await bsSecurityAreaApi.editWhiteListObj(data)
    },
    /**
     * 获取OA区域设置
     */
    async getOaArea(){
      const res = await bsSecurityAreaApi.getOaArea({
        parkId : this.areaForm.parkId
      })
      if( res.data.code===0){
        this.areaList = res.data.data
        this.areaList.forEach(element => {
          switch(element.factoryType){
            case 1:
            element.factoryTypeDesc = '新工厂'
            break
            case 2:
            element.factoryTypeDesc = '老工厂'
            break
          }
        });
      }
    },
    /**
     * 获取白名单设置
     */
    async getWhiteListConfig(){
      const res = await bsSecurityAreaApi.getWhiteListObj(
        { parkId: this.areaForm.parkId }
      )
      if( res.data.code===0&& res.data.data){
        // 先补齐响应式字段，再整体替换表单对象，避免 Vue2 对后加属性不建立 getter/setter。
        const normalizedConfig = Object.assign({
          parkId: '',
          isBusiness: '',
          isCompensatory: '',
          isHoliday: '',
          isLeave: '',
          isWhiteList: 0,
          dryRun: 0,
          whiteList: []
        }, res.data.data)
        normalizedConfig.dryRun = this.normalizeDryRun(normalizedConfig.dryRun)
        normalizedConfig.whiteList = Array.isArray(normalizedConfig.whiteList) ? normalizedConfig.whiteList : []
        this.whiteListForm = normalizedConfig
      }
    },

    /**
     * 将演练配置兼容为后端约定的 0/1，历史空值表示关闭演练。
     * @param {number|string|null} value 配置接口返回或表单中的演练值。
     * @returns {number} 只返回 0 或 1，避免向后端透传非法状态。
     */
    normalizeDryRun(value) {
      return value === 1 || value === '1' ? 1 : 0
    },
    /**
     * 获取员工详情
     */
    getStaffDetail(formName) {
      this.$refs[formName].validateField("staffBadge", errMsg => {
        if (!errMsg) {
          getStaffDetail(this.whiteListForm.staffBadge).then(res => {
            let result = res.data.data
            if (!this.validatenull(result)) {
              let id = result.id
              if (!this.validatenull(id)) {
                this.whiteListForm.staffId = result.id
                this.$set(this.whiteListForm, 'staffName', result.name)
              } else {
                this.$notify.error({
                  title: "提示",
                  message: "暂无该工号对应员工信息，请检查工号是否正确！"
                });
              }
            }
          });
        } else {
          return false;
        }
      });
    },
    /**
     * 实时手动刷新
     */
    async doSyncTask(){
      const res = await bsSecurityAreaApi.doSyncTask()
      if(res.data.code==0 && res.data.data==true){
        this.$message({
          message: '同步成功',
          type: 'success'
        });
      }
    },
    /**
     * 添加权限
     */
    addAuth(item){
      this.oaAreaObj = item
      this.$refs.authDialog && this.$refs.authDialog.open()
    },
    /**
     * 删除权限
     */
    delAuth(item){
      this.oaAreaObj = item
      this.$refs.delAuthDialog && this.$refs.delAuthDialog.open()
    },
    /**
     * 移除白名单
     */
    delLine(index) {
      this.whiteListForm.whiteList.splice(index, 1)
    },
    /**
     * 添加白名单
     */
    addLine() {
      let staffBadgeIsOk = false
      let staffNameIsOk = false
      this.$refs.whiteListForm.validateField('staffBadge', (msg) => {
        if (this.validatenull(msg)) {
          staffBadgeIsOk = true
        } else {
          staffBadgeIsOk = false
        }
      })
      this.$refs.whiteListForm.validateField('staffName', (msg) => {
        if (this.validatenull(msg)) {
          staffNameIsOk = true
        } else {
          staffNameIsOk = false
        }
      })
      if (staffBadgeIsOk && staffNameIsOk) {
        let arr = this.whiteListForm.whiteList.filter(el=>{
          return el.staffId === this.whiteListForm.staffId
        })
        if(arr && arr.length>0){
          this.$message.error('该工号已存在白名单内');
          return
        }
        this.whiteListForm.whiteList.push({
          staffId: this.whiteListForm.staffId,
          staffBadge: this.whiteListForm.staffBadge,
          staffName: this.whiteListForm.staffName
        })
        this.whiteListForm.staffId = ''
        this.whiteListForm.staffBadge = ''
        this.whiteListForm.staffName = ''
      }
    },
    goBack(){
      this.$router.push({
        path: `/platform/business/business_security_area`,
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 800px;
  padding: 50px 0 0 60px;
  .row{
    display: flex;
    align-items: center;
    line-height: 30px;
    margin-bottom: 10px;
    .dv1{
      margin-right: 10px;
      .el-tag{
        width: 105px;
        float: left;
        text-align: center;
      }
    }
    .dv2{
      width: 80px;
      text-align: center;
    }
    .dv3{
      margin-right: 20px;
      .el-tag{
        position: relative;
        width: 130px;
        float: left;
        text-align: left;
        padding-right: 30px;
        .num{
          position: absolute;
          top: 0;
          right: 5px;
          background: #ECF5FF;
          padding-left: 3px;
        }
      }
    }
  }
  .row1{
    .tip{
      margin-bottom: 0;
      padding-bottom: 5px;
    }
    .el-button{
      margin-left: 105px;
    }
  }
  .w2{
    width: 60px;
    margin: 0 10px;
    .el-input__inner{
      height: 30px;
      line-height: 30px;
      padding: 0 10px;
      text-align: center;
    }
  }
  .zclist{
    padding-left: 10px;
    padding-top: 5px;
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

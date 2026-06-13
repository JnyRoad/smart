<!--访客预约，预约记录  -->
<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="areaForm" :inline="false" class="dot-form" label-width="85px">
            <el-row class>
              <el-col>
                <div class="row row1">
                  <p class="tip">入厂申请区域设置</p>
                  <el-button type="text" icon="el-icon-refresh" @click="doSyncTask">手动实时刷新</el-button>
                </div>
                <el-form-item label prop="">
                  <div class="row">
                    <div class="dv1"> 授权进入区域类别 </div>
                    <div class="dv1"> 授权进入区域 </div>
                    <div class="dv2"></div>
                    <div class="dv3"> 通关权限（可多选） </div>
                  </div>
                  <div class="row" v-for="(item, index) in inAreaList" :key="index">
                    <div class="dv1" style="width: 110px">
                      <el-tooltip placement="bottom">
                        <div slot="content" style="line-height: 25px">
                          {{item.factoryTypeName}}
                        </div>
                        <el-tag class="-ellipsis" type="warning">{{item.factoryTypeName}}</el-tag>
                      </el-tooltip>
                    </div>
                    <div class="dv1">
                      <el-tooltip placement="bottom">
                        <div slot="content" style="line-height: 25px">
                          {{item.typeName}}
                        </div>
                        <el-tag class="-ellipsis" type="warning">{{item.typeName}}</el-tag>
                      </el-tooltip>
                    </div>
                    <div class="dv2" v-if="item.authLists && item.authLists.length>0"> 已设置 </div>
                    <div class="dv2 ft-danger" v-else> 未设置 </div>
                    <div class="dv3">
                      <el-tooltip placement="bottom" v-if="item.authLists && item.authLists.length>0">
                        <div slot="content" style="line-height: 25px">
                          <div v-for="(item2, index2) in item.authLists" :key="index2">
                            {{item2.authName}}
                          </div>
                        </div>
                        <el-tag class="-ellipsis">{{item.authLists[0].authName}}<span class="num">+{{item.authLists.length}}</span></el-tag>
                      </el-tooltip>
                    </div>
                    <div class="dv4"> <el-button type="text" icon="el-icon-edit" @click="addAuth(item)" round></el-button> </div>
                    <!-- <div class="dv4"> <el-button type="text" icon="el-icon-delete" @click="delAuth(item)" round :disabled="!item.authLists || item.authLists.length===0"></el-button> </div> -->
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-form ref="jcheForm" :inline="false" class="dot-form" :model="jcheForm" label-width="80px">
            <p class="tip">被访限制规则</p>
            <div class="tip1">使用说明：员工属于以下选中的职层，被访人无法被外来人员预约</div>
            <el-form-item label prop="jcheList">
              <el-checkbox-group v-model="jcheForm.jcheList" class="zclist">
                <el-checkbox v-for="(item, index) in jcheListOption" :label="item.value" :key="index">{{ item.label }} </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
          <el-form ref="commonAreaForm" :inline="false" class="dot-form common-area-form" :model="commonAreaForm" label-width="100px">
            <p class="tip">H5 常用区域展示配置</p>
            <el-form-item label="展示数量">
              <el-input-number v-model="commonAreaForm.inlineAreaLimit" :min="1" :max="20" :step="1" size="small"></el-input-number>
            </el-form-item>
            <div class="common-factory" v-for="factory in commonAreaForm.factories" :key="factory.factoryType">
              <div class="common-factory-title">{{ factory.factoryName }}</div>
              <el-table :data="factory.areas" border size="mini" class="common-area-table">
                <el-table-column label="常用" width="70" align="center">
                  <template slot-scope="scope">
                    <el-checkbox v-model="scope.row.isCommon"></el-checkbox>
                  </template>
                </el-table-column>
                <el-table-column prop="areaName" label="区域名称"></el-table-column>
                <el-table-column label="排序" width="120" align="center">
                  <template slot-scope="scope">
                    <el-input-number v-model="scope.row.sort" :min="1" :max="999" :controls="false" size="mini" class="sort-input"></el-input-number>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-form>
          <el-form ref="tipForm" :inline="false" class="dot-form" :model="tipForm" label-width="85px">
            <p class="tip">前端提示配置</p>
            <el-form-item label prop="isOpen">
              <div class="row">
                <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="1" :inactive-value="0" v-model="isNeedNoticeObj.isNeedNotice"></el-switch>
                <span style="padding-left: 15px;">开启后，在许昌访客页面显示提示</span>
              </div>
            </el-form-item>
            <el-form-item label prop="isOpen">
              <div id="wangeditor">
                <div ref="noticeContent"></div>
              </div>
            </el-form-item>
            <el-form-item label style="margin-top: 50px;">
              <el-button type="primary" @click="saveInfo()">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
    <AuthDialog title="关联通关权限" :itemObj="inAreaObj" ref="authDialog" :parkId="parkId"/>
    <DelAuthDialog title="删除权限" :itemObj="inAreaObj" ref="delAuthDialog"/>

  </div>
</template>

<script>
import { inComingApi } from './_service'
import AuthDialog from './components/batchAuth'
import DelAuthDialog from './components/delAuth'
import { mapGetters } from 'vuex'
import { saveBatchConfig, getBatchConfig } from '../visitor/visitor_service'
import E from "wangeditor";
import { getJchesObj } from '@/api/platform/_publicService'
import { normalizeCommonAreaConfig } from './common-area-config'
export default {
  name: 'visitor',
  components: {
    AuthDialog,
    DelAuthDialog
  },
  data() {
    return {
      disable: true,
      inAreaObj: {},
      parkId: 5000021, //固定许昌园区id 线上5000021 测试10322
      inAreaList: [],
      tipForm: {},
      jcheListOption: [],
      jcheForm: {
        jcheList: [],
        parkId: ''
      },
      isNeedNoticeObj: {
        isNeedNotice: 0,
        content: ''
      },
      commonAreaConfigObj: {},
      commonAreaStandardAreas: {
        newAreas: [],
        oldAreas: []
      },
      commonAreaForm: normalizeCommonAreaConfig(null, {
        newAreas: [],
        oldAreas: []
      }),
      editor: null
    }
  },
  created() {
    this.initData()
  },
  mounted: function () {
    this.initEditor()
  },
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    async saveJcheInfo(formName) {
      formName.parkId = this.parkId
      return await inComingApi.editJche(formName)
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
        inComingApi.getJcheId(
          Object.assign({
            parkId: parkId
          })
        ).then((response) => {
          let self = this
          self.jcheForm.jcheList = response.data.data
        })
      }
    },
    refresh(){},
    async saveInfo() {
      //入厂区域参数
      let inArr = []
      this.inAreaList.forEach(el=>{
        inArr.push(
          {
            id: '',
            areaTypeId: el.areaTypeId,
            parkId: el.parkId,
            authLists: el.authLists?el.authLists:[]
          }
        )
      })
      Promise.all([
        inComingApi.saveAuthConfig(inArr),
        this.saveJcheInfo(this.jcheForm),
        this.saveBatchConfig()
      ]).then(arr=>{
         if(arr.length===3 && arr[0] && arr[1] && arr[2]){
          if(arr[0].data.code===0 && arr[0].data.data && arr[1].data.code===0 && arr[1].data.data && arr[2].data.code===0 && arr[2].data.data){
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
        this.getJcheList(this.parkId),
        this.getInArea(),
        this.getBatchConfig()
      ]).then(arr=>{
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
     * 保存入厂区域设置
     */
    async saveOaArea(data){
      const res = await inComingApi.saveAuthConfig(data)
    },
    /**
     * 获取入厂区域设置
     */
    async getInArea(){
      const res = await inComingApi.getAuthConfig(
        { parkId: this.parkId }
      )
      if( res.data.code===0){
        this.inAreaList = res.data.data
        this.inAreaList.forEach(element => {
          switch(element.factoryType){
            case 1: element.factoryTypeName = '新工厂'
            break
            case 2: element.factoryTypeName = '老工厂'
            break
          }
        });
      }
    },
    /**
     * 实时手动刷新
     */
    async doSyncTask(){
      const res = await inComingApi.doSyncTask()
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
      this.inAreaObj = item
      this.$refs.authDialog && this.$refs.authDialog.open()
    },
    /**
     * 删除权限
     */
    delAuth(item){
      this.inAreaObj = item
      this.$refs.delAuthDialog && this.$refs.delAuthDialog.open()
    },
     /**
     * 批量编辑配置
     */
    async saveBatchConfig() {
      let obj2 = {
        isNeedNotice: this.isNeedNoticeObj.isNeedNotice,
        content: this.isNeedNoticeObj.content
      }
      let configObj2 = {}
      if(this.isNeedNoticeObj.id){
        configObj2 = Object.assign({}, this.isNeedNoticeObj, {
          businessType: 2,
          value: JSON.stringify(obj2),
          configType: 2,
          parkId: this.parkId,
        })
      }else{
        configObj2 = {
          businessType: 2, //固定2 入厂申请
          value: JSON.stringify(obj2),
          configType: 2, //访客提示
          parkId: this.parkId,
        }
      }
      const commonAreaValue = normalizeCommonAreaConfig(this.commonAreaForm, this.commonAreaStandardAreas)
      let configObj7 = {}
      if(this.commonAreaConfigObj.id){
        configObj7 = Object.assign({}, this.commonAreaConfigObj, {
          businessType: 2,
          value: JSON.stringify(commonAreaValue),
          configType: 7,
          parkId: this.parkId,
        })
      }else{
        configObj7 = {
          businessType: 2, //固定2 入厂申请
          value: JSON.stringify(commonAreaValue),
          configType: 7, //H5 常用区域展示配置
          parkId: this.parkId,
        }
      }
      let arr = [configObj2, configObj7]
      return await saveBatchConfig(arr)
    },
    /**
     * 清除批量配置的数据
     */
    initBatchConfig(){
      this.isNeedNoticeObj = {
          isNeedNotice: 0,
          content: ''
        }
      this.commonAreaConfigObj = {}
      this.commonAreaStandardAreas = {
        newAreas: [],
        oldAreas: []
      }
      this.commonAreaForm = normalizeCommonAreaConfig(null, this.commonAreaStandardAreas)
      this.editor && this.editor.txt.clear()
    },
    async getCommonAreaStandardAreas(){
      try {
        const areaOptionsResponse = await inComingApi.getAreaOptions({ parkId: this.parkId })
        if(areaOptionsResponse.data.code === 0 && areaOptionsResponse.data.data && Array.isArray(areaOptionsResponse.data.data.factories) && areaOptionsResponse.data.data.factories.length > 0){
          this.commonAreaStandardAreas = areaOptionsResponse.data.data
          return this.commonAreaStandardAreas
        }
      } catch (error) {
        if(window.console){
          window.console.warn(error)
        }
      }
      const responses = await Promise.all([
        inComingApi.getAreaType({ flag: 1 }),
        inComingApi.getAreaType({ flag: 0 })
      ])
      const newAreaResponse = responses[0]
      const oldAreaResponse = responses[1]
      if(newAreaResponse.data.code !== 0 || oldAreaResponse.data.code !== 0){
        throw new Error('获取H5常用区域标准区域失败')
      }
      this.commonAreaStandardAreas = {
        newAreas: Array.isArray(newAreaResponse.data.data) ? newAreaResponse.data.data : [],
        oldAreas: Array.isArray(oldAreaResponse.data.data) ? oldAreaResponse.data.data : []
      }
      return this.commonAreaStandardAreas
    },
    /**
     * 根据类型获取配置
     */
    async getBatchConfig() {
      this.initBatchConfig()
      let obj = {
        businessTypes: [2],
        configTypes: [1, 2, 7],
        parkId: this.parkId
      }
      const responses = await Promise.all([
        getBatchConfig(obj),
        this.getCommonAreaStandardAreas()
      ])
      const res = responses[0]
      const commonAreaStandardAreas = responses[1]
      let commonAreaConfigValue = null
      if(res.data.code===0 && Array.isArray(res.data.data) && res.data.data.length>0){
        res.data.data.forEach(el=>{
          if(Number(el.configType) === 2){
            this.isNeedNoticeObj = el
          }
          if(Number(el.configType) === 7){
            this.commonAreaConfigObj = el
            commonAreaConfigValue = el.value
          }
        })
        if(this.isNeedNoticeObj.value){
          let obj2 = JSON.parse(this.isNeedNoticeObj.value)
          this.$set(this.isNeedNoticeObj, 'isNeedNotice', obj2.isNeedNotice)
          this.$set(this.isNeedNoticeObj, 'content', obj2.content)
          // let txt = "<div>" + obj2.content + "</div>";
          let txt = obj2.content;
          if(this.editor){
            this.editor.txt.clear()
            this.editor.txt.html(txt)
          }
        }
      }
      this.commonAreaForm = normalizeCommonAreaConfig(commonAreaConfigValue, commonAreaStandardAreas)
    },
    /**初始化编辑器
     */
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
        //"image", // 插入图片
        "table", // 表格
        //'code', // 插入代码
        "undo", // 撤销
        "redo" // 重复
      ];
      this.editor.create(); // 创建富文本实例
      if(this.isNeedNoticeObj.content){
        this.editor.txt.html(this.isNeedNoticeObj.content)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 800px;
  padding: 50px 0 0 60px;
  .el-checkbox+.el-checkbox {
    margin-left: 0;
  }
  .el-checkbox{
    margin: 0 20px 0 0;
  }
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
      width: 100px;
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
      margin-left: 147px;
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
  .common-area-form {
    width: 760px;
  }
  .common-factory {
    margin: 0 0 24px 10px;
  }
  .common-factory-title {
    color: #333;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 10px;
  }
  .common-area-table {
    width: 690px;
  }
  .sort-input {
    width: 80px;
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

<!--
- @name 供应商管理
-->
<template>
  <el-scrollbar class="my-lit-scrollbar" :native="false">
    <div style="margin:10px 0;">
      <el-input placeholder="请输入" clearable v-model="filterText" size="mini">
        <el-button slot="append" icon="el-icon-search" @click="filterTreeHandle(filterText)"></el-button>
      </el-input>
    </div>
    <el-tree
      class="my-menu-tree"
      :data="treeData"
      highlight-current
      :props="defaultProps"
      :expand-on-click-node="false"
      @node-click="handleNodeClick"
      :filter-node-method="filterNodeMethod"
      ref="roomtree"
      default-expand-all
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span v-if="node.level===3">
          <i class="el-icon-edit" @click.stop="treeNodeOption(data, 'EDI', node)"></i>
          <i class="el-icon-delete" @click.stop="treeNodeOption(data, 'DEL', node)"></i>
        </span>
        <span v-if="node.level===2">
          <i class="el-icon-circle-plus-outline" @click.stop="treeNodeOption(data, 'APP', node)"></i>
        </span>
      </span>
    </el-tree>
    <!-- 弹出框 -->
    <el-dialog :title="mTitle" :visible.sync="departmentDialog.visible">
      <!-- <el-scrollbar> -->
        <el-form ref="departmentForm" :rules="departmentDialog.rules" :model="departmentDialog.formData" label-width="120px" >
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="departmentDialog.formData.parkId" :myDisabled="true"></parkSelect>
          </el-form-item>
          <el-form-item label="分类" prop="supplierType">
            <el-select
              v-model="departmentDialog.formData.supplierType"
              placeholder="请选择"
              disabled
            >
              <el-option
                v-for="item in supplierTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="协议签订日期" prop="beginEffectTime">
            <el-date-picker
              v-model="departmentDialog.formData.beginEffectTime"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              type="date"
              placeholder="选择日期">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="协议到期日期" prop="endEffectTime">
            <el-date-picker
              v-model="departmentDialog.formData.endEffectTime"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              type="date"
              :picker-options="endTimeOption"
              placeholder="选择日期">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="供应商名称" prop="companyName">
            <el-input v-model="departmentDialog.formData.companyName" placeholder="请输入"></el-input>
          </el-form-item>
          <el-form-item label="授权项目" prop="authorList" v-if="editing">
            <el-input type="textarea" v-model="departmentDialog.formData.authorList" placeholder="授权项目以'/'分割，例如：Y12020/Y12024/Y12026"></el-input>
          </el-form-item>
          <el-form-item label="授权区域" prop="authorizedArea">
            <el-input v-model="departmentDialog.formData.authorizedArea" placeholder="请输入"></el-input>
          </el-form-item>
          <el-form-item label="授权人数" prop="authPersonNum">
            <el-input v-model="departmentDialog.formData.authPersonNum" placeholder="请输入"></el-input>
          </el-form-item>
          <!-- <el-form-item label="身份证号" prop="certNo">
            <el-input v-model="departmentDialog.formData.certNo" placeholder="请输入"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="personName">
            <el-input v-model="departmentDialog.formData.personName" placeholder="请输入"></el-input>
          </el-form-item> -->
          <el-form-item label="申请理由" prop="remark">
            <el-input v-model="departmentDialog.formData.remark" placeholder="请输入"></el-input>
          </el-form-item>
          <el-form-item label="联系人" prop="contactPerson">
            <el-input v-model="departmentDialog.formData.contactPerson" placeholder="请输入"></el-input>
          </el-form-item>
          <el-form-item label="上传保密协议" prop="protocolType">
            <div>
              <el-radio-group v-model="departmentDialog.formData.protocolType" size="mini" class="group1">
                <el-radio-button :label="1">上传图片</el-radio-button>
                <el-radio-button :label="2">上传pdf</el-radio-button>
              </el-radio-group>
              <div class="imgs" v-if="departmentDialog.formData.protocolType===1">
                <template v-for="(item, index) in newImgs">
                  <div class="img" :key="index">
                    <img :src="item" />
                    <i @click="delImg(index)">×</i>
                  </div>
                </template>
                <!-- 最多可以添加10张图 -->
                <tceImgUpload @uploadAfter="uploadAfter" v-if="newImgs.length<10"/>
              </div>
              <div v-if="departmentDialog.formData.protocolType===2">
                <input type="file" accept="application/pdf" ref="fileSupplier" @change="articleUpLoadFile($event)" style="display: none;" />
                <el-button type="text" @click="uploadSupplier" icon="icon-yutong-upload" size="mini">选择文件</el-button>
                <span class="fileName" v-if="pdfs.length>0 && pdfs[0].indexOf('http')>-1"><a :href="pdfs[0]">{{pdfName}}</a></span>
                <span class="fileName" v-else>{{pdfName}}</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="携带物品项目" prop="itemList">
            <el-checkbox-group v-model="departmentDialog.formData.itemList">
              <el-checkbox label="相机"></el-checkbox>
              <el-checkbox label="笔记本电脑"></el-checkbox>
              <el-checkbox label="移动存储设备"></el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      <!-- </el-scrollbar> -->
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" plain @click="departmentDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="depSave" :loading="addLoading">保 存</el-button>
      </div>
    </el-dialog>
  </el-scrollbar>
</template>

<script>
import { supplierList } from "./_service";
import { delRecord, addRecord, recordDetail } from "@/api/platform/security_area/security_supplier";
import { getSearchStaff } from "@/api/platform/basic/personnel_manage";
import { mapGetters } from 'vuex'
const supplierTypeList = [
  { label: 'A类', value: 1},
  { label: '非A类', value: 2}
]
export default {
  data() {
    const isNum = (rule, value, callback) => {
      const age= /^[0-9]*$/
    if (value !== null && !age.test(value)) {
        callback(new Error('只能输入数字'))
      }else{
        callback()
      }
    }
    /**
     * 校验身份证号
     */
    var vCardId = (rule, value, callback) => {
      let codeReg = new RegExp("[\u4E00-\u9FA5]+") //正则 不能输入汉字  ；
      let len = value.length,
      str='';
      for(var i=0;i<len;i++){
        if(codeReg.test(value[i])){
          str+=value[i];
        }
      }
      if(str.length>0){
        callback(new Error('证件号码不能包含汉字'))
      }else{
        if(len>7 && len<20){
          callback()
        }else{
          callback(new Error('证件号码需在8~20位之间'))
        }
      }
    }
    return {
      mTitle: '添加供应商',
      editing: false,
      addLoading: false,
      treeData: [],
      newImgs: [],
      pdfs: [],
      pdfName: '',
      supplierTypeList: supplierTypeList,
      departmentDialog: {
        directorLoading: false,
        directorDataList: [],
        formData: {
          authorList: null,
          beginEffectTime: null,
          authorList: null,
          companyName: null,
          contactPerson: null,
          endEffectTime: null,
          parkId: null,
          proContents: null,
          protocolName: null,
          protocolType: null,
          remark: null,
          supplierType: null,
          carryItem: null,
          itemList: [],
          authorizedArea: null,
          authPersonNum: null,
          // certNo: null,
          // personName: null
        },
        rules: {
          beginEffectTime: [
            { required: true, message: '请选择协议签订日期', trigger: 'change' }
          ],
          endEffectTime: [
            { required: true, message: '请选择协议到期日期', trigger: 'change' }
          ],
          companyName: [
            { required: true, message: '请输入供应商名称', trigger: 'blur' }
          ],
          contactPerson: [
            { required: true, message: '请输入联系人', trigger: 'blur' }
          ],
          remark: [
            { required: true, message: '请输入申请理由', trigger: 'blur' }
          ],
          authPersonNum:[
            { validator: isNum, trigger: "blur" }
          ],
          // certNo:[
          //   { validator: vCardId, trigger: "blur" }
          // ],
          // protocolType: [
          //   {  message: '请选择协议类型', trigger: 'change' }
          // ]
        },
        visible: false
      },
      filterText: '',
      defaultProps: {
        children: 'children',
        label: 'companyName'
      },
      base64:"data:image/jpeg;base64,",
      endTimeOption: this.endPickerDate()
    }
  },
  mounted: function () {
    this.getSupplierList()
  },
  computed: {
    ...mapGetters(['permissions']),
  },
  watch: {
    'departmentDialog.formData.protocolType':function(val) {
      if(val===1){ //图片
        this.pdfs = []
        this.pdfName = ''
      }
      if(val===2){ //pdf
        this.newImgs = []
      }
    },
    'departmentDialog.formData.beginEffectTime':function(val) {
      if(!this.editing){
        this.departmentDialog.formData.endEffectTime = ''
      }
    }
  },
  methods: {
    delImg(index){
      this.newImgs.splice(index, 1)
    },
    endPickerDate() {
      var _this = this
      return {
        disabledDate (date) {
          return date.getTime() < new Date(_this.departmentDialog.formData.beginEffectTime).getTime()
        }
      }
    },
    articleUpLoadFile(source){
      this.pdfs = []
      let _this = this
      let file = source.target.files[0];
      let fileSize = file.size;
      let fileType = file.name.substring(file.name.length - 3);
      if( fileSize > 0 && fileType.toLowerCase()  == "pdf"){
        let reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = function (e) {
          _this.pdfs.push(reader.result)
          _this.pdfName = file.name
        }
      }else{
        _this.$message.error("文件格式不正确，需为pdf格式");
        // _this.articleData.upLoadStatus2 =  false;
      }
    },
    uploadSupplier() {
      this.$refs.fileSupplier.click();
    },
    uploadAfter(files) {
      this.newImgs.push(files[0].url)
    },
    /**
     * 远程搜索主管列表
     */
    async remoteMethod(query) {
      if (query) {
        this.departmentDialog.directorLoading = true
        const res = await getSearchStaff({
          badge: query
        })
        this.departmentDialog.directorDataList = res.data.data
        this.departmentDialog.directorLoading = false
      } else {
        this.departmentDialog.directorDataList = []
        this.departmentDialog.directorLoading = false
      }
    },
    async getSupplierList() {
      const res = await supplierList()
      this.supplierList = res.data.data
      let arr1 = []
      let arr2 = []
      this.supplierList.forEach(el=>{
        if(arr1.indexOf(el.parkId)==-1){
          let obj = {
            id: -1,
            parkId: el.parkId,
            companyName: el.parkName,
            companyCode:'',
            supplierType: '',
            children: el.children
          }
          arr1.push(el.parkId)
          arr2.push(obj)
        }
      })

      let arr3 = []
      arr2.forEach(el=>{
        let o2 = {
          a: {
            id: -2,
            parkId: el.parkId,
            companyName: 'A类',
            companyCode:'',
            supplierType: 1,
            children: []
          },
          na:  {
            id: -2,
            parkId: el.parkId,
            companyName: '非A类',
            companyCode:'',
            supplierType: 2,
            children: []
          },
        }
        let tmpA = []
        let tmpNa = []
        if(el.children !== null){
          tmpA = el.children.filter((item) => {
            return item.parkId === el.parkId && item.supplierType === 1
          })
          tmpNa = el.children.filter((item) => {
            return item.parkId === el.parkId && item.supplierType === 2
          })
        }
        let tmpA2 = []
        tmpA.forEach(el=>{
          el.companyName = "["+el.companyCode+"]"+el.companyName
          tmpA2.push(el)
        })
        let tmpNa2 = []
        tmpNa.forEach(el=>{
          el.companyName = "["+el.companyCode+"]"+el.companyName
          tmpNa2.push(el)
        })
        o2.a.children = tmpA2
        o2.na.children = tmpNa2
        arr3.push(o2)
      })
      arr2.forEach((el, index)=>{
        el.children = [arr3[index].a, arr3[index].na]
      })
      this.treeData = arr2
    },
    /**
     * 添加供应商，保存
     */
    async depSave() {
      await this.$refs.departmentForm.validate()
      this.addLoading = true
      try{
        if (this.departmentDialog.formData.protocolType===1 ){
          if(this.newImgs.length===0){
            // this.$message.error('请上传图片');
            // return
            this.departmentDialog.formData.proContents = null
          }else{
            this.departmentDialog.formData.proContents = this.newImgs
          }
        }else if (this.departmentDialog.formData.protocolType===2) {
          if(this.pdfs.length===0){
            // this.$message.error('请上传pdf');
            // return
            this.departmentDialog.formData.proContents = null
            this.departmentDialog.formData.protocolName = null
          }else{
            this.departmentDialog.formData.proContents = this.pdfs
            this.departmentDialog.formData.protocolName = this.pdfName
          }
        }
        let obj = Object.assign({},this.departmentDialog.formData)
        if(obj.itemList && obj.itemList.length>0){
          obj.carryItem = obj.itemList.toString()
          obj.itemList = undefined
        }else{
          obj.carryItem = ''
        }
        await addRecord(obj)
        this.getSupplierList()
        this.departmentDialog.visible = false
        this.$message({
          showClose: true,
          message: '成功',
          type: 'success'
        })
      }catch(err){ /* 忽略异常：失败时仅复位 loading */ }finally{
        this.addLoading = false
      }
    },
    /**
     * tree节点操作
     */
    async treeNodeOption(data, opt, node) {
      switch (opt) {
        case 'EDI':
          this.editing = true
          this.mTitle = '编辑供应商'
          const res2 = await recordDetail( data.id )
          let resData = res2.data.data
          this.departmentDialog.formData = {
            id: resData.id,
            beginEffectTime: resData.beginEffectTime,
            companyName: resData.companyName,
            authorList: resData.authorList,
            contactPerson: resData.contactPerson,
            endEffectTime: resData.endEffectTime,
            parkId: resData.parkId,
            proContents: resData.contents,
            protocolName: resData.protocolName,
            protocolType: resData.protocolType,
            remark: resData.remark,
            supplierType: resData.supplierType,
            itemList: resData.itemList ? resData.itemList : [],
            authorizedArea: resData.authorizedArea,
            authPersonNum: resData.authPersonNum,
            // certNo: resData.certNo,
            // personName: resData.personName
          }
          if(resData.protocolType===1){
            this.newImgs = resData.contents
          }else if(resData.protocolType===2){
            this.pdfName = resData.protocolName
            this.pdfs = resData.contents
          }
          this.departmentDialog.visible = true
          break
        case 'APP':
          this.editing = false
          this.mTitle = '添加供应商'
          this.$refs.departmentForm && this.$refs.departmentForm.resetFields()
          this.departmentDialog.formData.supplierType = data.supplierType
          this.departmentDialog.formData.parkId = data.parkId
          this.departmentDialog.visible = true
          break
        case 'DEL':
          await this.$confirm('是否确认移除此节点', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          const res = await delRecord(data.id)
          if (res.data.code === 0) {
            this.$message({
              showClose: true,
              message: '删除成功',
              type: 'success'
            })
          }
          this.getSupplierList()
          break
      }
    },
    /**
     * 节点点击事件
     */
    handleNodeClick(data, node) {
      if(node.level===3){
        this.$emit('doSearch', data.id)
      }
    },
    /**
     * 搜索tree
     */
    filterTreeHandle(value) {
      this.$refs.roomtree.filter(value)
    },
    /**
     * 筛选tree
     */
    filterNodeMethod(value, data, node) {
      if (!value) return true;
      return data.companyName.indexOf(value) !== -1;
    }
  }
}
</script>

<style lang="scss" scoped>
.personnel-dialog-from-item {
  padding: 0 20px;
  ::v-deep {
    .el-form-item {
      margin-bottom: 20px;
    }
  }
}
.imgs{
  padding: 10px 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  .img{
    position: relative;
    width: 85px;
    height: 85px;
    margin: 0 10px 10px 0;
    border: 1px solid #e0e0e0;
    img{
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      max-width: 100%;
      max-height: 100%;
    }
    i{
      position: absolute;
      top: -10px;
      right: -10px;
      width: 18px;
      height: 18px;
      font-size: 20px;
      display: inline-block;
      line-height: 18px;
      text-align: center;
      color: #fff;
      background: red;
      border-radius: 50%;
      cursor: pointer;
    }
  }
  .tce-upload-img{
    margin: 0 10px 10px 0;
  }
}
.group1 ::v-deep {
  .el-radio-button__inner{
    // border: 1px solid #dcdfe6;
    padding: 8px 17px;
  }
  .el-radio-button__inner:hover{
    color: #fff;
    background-color: #ed6d00;
  }
  .el-radio-button__orig-radio:checked+.el-radio-button__inner{
    color: #fff;
    background-color: #ed6d00;
    border-color: #ed6d00;
    box-shadow: -1px 0 0 0 #ed6d00;
  }
}
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  i[class^='el-icon-'] {
    margin: 0 2px;
  }
  span:nth-of-type(1) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: break-all;
    width: 235px;
  }
  span:nth-of-type(2) {
    display: none;
  }
  &:hover {
    span:nth-of-type(2) {
      display: block;
    }
  }
}
.fileName{
  font-size: 12px;
  padding: 0 0 0 20px;
  a{
    color: #ed6d00;
  }
}
</style>

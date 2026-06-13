<!--快速入住  -->
<template>
  <div class="my-basic-container mycard2 ks_check_in">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          <div class="box-left_inner">
            <div class="lft_top">
              <div class="cont-left">
                <div class="top-til">录入入住信息 </div>
              </div>
              <div class="nav_t">
                <div class="st1" @click="isStep2 = false">第一步，基础设置</div>
                <div class="st2" :class="{'active': isStep2}">第二步，匹配宿舍</div>
              </div>
            </div>
            <div class="gray-line"></div>
            <el-form ref="searchform" :rules="searchformRule" :inline="true" :model="searchform" size="mini" label-width="80px" label-position="left">
              <div class="lft_btm step1" v-show="!isStep2">
                <p class="t1">预更换宿舍及房间类型</p>
                <!-- <p class="tip">自动匹配将基于预设房源进行关联匹配</p> -->
                <el-form-item label="分配规则" prop="">
                  <el-radio-group v-model="type" class="rd1">
                    <el-radio-button :label="1">自动分配</el-radio-button>
                    <el-radio-button :label="2">手动分配</el-radio-button>
                  </el-radio-group>
                  <div v-if="type==1" class="tip2">自动匹配将基于预设房源进行关联匹配</div>
                  <div v-if="type==2" class="tip2">自定义选择分配到具体房间</div>
                </el-form-item>
                <el-form-item label="园区选择" prop="parkId">
                  <parkSelect
                    :defaultSelected="defaultSelectedPark"
                    @defaultHandle="defaultHandle"
                    @getItem="getParkItem"
                    @doChange="parkChange"
                    v-model="searchform.parkId"
                  ></parkSelect>
                </el-form-item>
                <el-form-item label="楼栋选择" prop="dormitoryId">
                  <el-cascader
                    v-model="dormIds"
                    :options="dormList"
                    change-on-select
                    @change="dormChagne"
                  >
                  </el-cascader>
                </el-form-item>
                <el-form-item label="房间类型" prop="roomType">
                  <roomTypeSelect2
                    :defaultSelected="defaultSelectedPark"
                    @getItem="getRoomTypeItem"
                    :parkId="searchform.parkId"
                    :dormitoryId="searchform.dormitoryId"
                    v-model="searchform.roomType"
                  ></roomTypeSelect2>
                </el-form-item>
                <el-form-item label="房间号" prop="roomName" v-if="type==2" >
                  <el-input v-model="searchform.roomName" @focus="selectRoom" placeholder="请点击选择房间号"></el-input>
                </el-form-item>
                <el-form-item label="床位号" prop="bedNumber" v-if="type==2">
                  <el-input v-model="searchform.bedNumber" placeholder="由房间号带出" readonly></el-input>
                </el-form-item>
              </div>
            </el-form>
            <el-form ref="infoform" :rules="infoformRule" :inline="true" :model="infoform" size="mini" label-width="100px" label-position="left">
              <div class="lft_btm step2" v-show="isStep2">
                <p class="t1">预更换宿舍及房间类型</p>
                <p class="tip">自动匹配将基于预设房源进行关联匹配</p>
                <el-form-item label="园区选择:" class="item1">
                  <div><span class="val-item">{{parkLabel}}</span></div>
                </el-form-item>
                <br/>
                <el-form-item label="宿舍选择:" class="item1">
                  <div><span class="val-item">{{dormLabel}}</span></div>
                </el-form-item>
                <br/>
                <el-form-item label="房间类型:" class="item1">
                  <div><span class="val-item">{{roomTypeLabel}}</span></div>
                </el-form-item>
                <br/>
                <el-form-item label="房间号:" class="item1" v-if="type==2">
                  <div @click="bedChange"><span class="val-item">{{curRoom.roomName}}</span><span style="font-size: 12px; color: #ed6d00;padding-left: 10px;">(点击可重新选择床位号)</span></div>
                </el-form-item>
                <br/>
                <el-form-item label="床位号:" class="item1" v-if="type==2">
                  <div><span class="val-item">{{curBed.bedNumber}}</span></div>
                </el-form-item>
                <p class="t1" style="margin-top: 20px">入住信息录入</p>
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="infoform.name" size="mini" placeholder="请输入姓名"/>
                </el-form-item>
                <div class="item2">
                  <el-form-item label="性别" prop="sex">
                    <el-input v-model="infoform.sex" size="mini" placeholder="请输入性别"/>
                  </el-form-item>
                  <el-form-item label="民族" prop="nation">
                    <el-input v-model="infoform.nation" size="mini" placeholder="请输入民族"/>
                  </el-form-item>
                </div>
                <el-form-item label="身份证号" prop="certno">
                  <el-input v-model="infoform.certno" size="mini" placeholder="请输入身份证号"/>
                </el-form-item>
                <el-form-item label="出生日期" prop="birthday">
                  <el-date-picker
                    v-model="infoform.birthday"
                    type="date"
                    :picker-options="pickerOptions1"
                    value-format="yyyy-MM-dd"
                    placeholder="请输入出生日期">
                  </el-date-picker>
                  <!-- <el-input v-model="infoform.birthday" size="mini" placeholder="请输入出生日期"/> -->
                </el-form-item>
                <el-form-item label="住址" prop="address">
                  <el-input v-model="infoform.address" size="mini" placeholder="请输入住址"/>
                </el-form-item>
                <el-form-item label="签发机关" prop="signOrg">
                  <el-input v-model="infoform.signOrg" size="mini" placeholder="请输入签发机关"/>
                </el-form-item>
                 <el-form-item label="有效期开始" prop="validDateStart">
                  <el-date-picker
                    v-model="infoform.validDateStart"
                    type="date"
                    :picker-options="pickerOptions1"
                    value-format="yyyy-MM-dd"
                    placeholder="选择有效期开始日期">
                  </el-date-picker>
                  <!-- <el-input v-model="infoform.validDateStart" size="mini" /> -->
                </el-form-item>
                 <el-form-item label="有效期截止" prop="validDateEnd">
                  <el-date-picker
                    v-model="infoform.validDateEnd"
                    type="date"
                    value-format="yyyy-MM-dd"
                    placeholder="选择有效期截止日期">
                  </el-date-picker>
                  <!-- <el-input v-model="infoform.validDateEnd" size="mini" /> -->
                </el-form-item>
              </div>
            </el-form>
          </div>
          <div class="lft_btn">
            <div class="gray-line" style="margin-top: 0"></div>
            <el-button type="primary" v-if="!isStep2" @click="nextStep('searchform')">→ 下一步</el-button>
            <template v-else>
              <el-button type="primary" @click="readIDCard()" plain>读取身份证</el-button>
              <!-- <el-button type="primary" icon="" @click="checkIn('infoform')" :loading="autoLoading"><i class="pp_dorm_i"></i>自动匹配宿舍</el-button> -->
              <el-button type="primary" icon="" @click="checkIn('infoform')" :loading="autoLoading">确定</el-button>

            </template>
          </div>
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="box-outer">
          <div class="top-til">床位信息</div>
          <avue-crud
            ref="staffTable"
            :data="tableData"
            :option="tableOption"
            :row-class-name="tableRowClassName"
          >
            <template slot-scope="scope" slot="name">
              <el-tooltip placement="bottom" v-if="scope.row.staffBadge">
                <div slot="content" style="line-height: 25px">
                  姓名：{{scope.row.name}}<br/>
                  工号：{{scope.row.staffBadge}}<br/>
                  性别：{{scope.row.sex | genderInit}}<br/>
                  部门：{{scope.row.depName}}<br/>
                  职级：{{scope.row.jobName}}<br/>
                </div>
                <div style="cursor: pointer;"><i class="person_i"></i><span>{{scope.row.name}}</span></div>
              </el-tooltip>
            </template>
            <template slot-scope="scope" slot="sex">
              <template v-if="scope.row.sex===1">
                <span class="girl_c">女</span>
              </template>
              <template v-if="scope.row.sex===0">
                <span class="boy_c">男</span>
              </template>
            </template>
            <template slot-scope="scope" slot="dormitoryName">
              {{scope.row.dormitoryName}}-{{scope.row.roomName}}室-{{scope.row.bedNumber}}床
            </template>
            <template slot-scope="scope" slot="menu">
              <template v-if="scope.row.staffBadge">
                <el-button
                  type="text"
                  class="print2_btn"
                  @click="handlePrintPre(scope.row,scope.$index)"
                ><i class="print2_i"></i>补打凭条</el-button>
                <el-button
                  type="text"
                  @click="handleChange(scope.row,scope.$index)"
                ><i class="change_dorm_i"></i>换宿</el-button>
                <el-button
                  type="text"
                  icon="el-icon-delete"
                  @click="checkOut(scope.row,scope.$index)"
                >退宿</el-button>
              </template>
            </template>
          </avue-crud>
          <div class="note-outer" :class="{'printStyle':rowPrint && rowPrint.id}">
            <div class="note-inner" ref="print" id="printDom" >
              <div class="n_logo">
                裕同科技
                <!-- <img src="/img/dorm/logo_d.png"/> -->
              </div>
              <p class="p1">欢迎您加入裕同科技</p>
              <div class="gray-line"></div>
              <p class="p2">{{rowPrint.name}}（{{rowPrint.sex|genderInit}}）</p>
              <p class="p3">{{rowPrint.roomName}}室{{rowPrint.bedNumber}}床</p>
              <div class="gray-line"></div>
              <p class="p4">{{rowPrint.dormitoryName}}</p>
              <p class="p5">{{rowPrint.createTime}}</p>
              <p class="p6">*入住前请向宿管出示入住凭条</p>
            </div>
          </div>
        </div>
        <dlgDormChange :visible="dlg1Visible" :row="curDlg1Obj" @dlgdo="dlg1do" @dlgdoSuccess="dlg1doSuccess"/>
        <dlgDormSelect :visible="dlg3Visible" :row="curDlg3Obj" @dlgdo="dlg3do" @dlgdoSuccess="dlg3doSuccess"/>
        <dlgNotePre :visible="dlg2Visible" :row="curDlg2Obj" @dlgdo="dlg2do" />
      </div>
      <!-- 退宿 -->
      <checkOut ref="checkout" :dataItem="curCheckIn" @refresh="refreshList"/>
      <!-- 选择床位 -->
      <DlgDormBeds ref="dlgdormbeds" :row="curRoom" @done="selectBed"/>
    </el-scrollbar>
  </div>
</template>

<script>
import { autoallot, bedDetail, checkDormInInfo } from "./_service.js";
import { tableOption } from "@/const/crud/platform/dormitory/ks_check_in";
import dlgDormChange from "./compnents/dlg_dorm_change";
import dlgDormSelect from "./compnents/dlg_dorm_select";
import roomTypeSelect2 from "./compnents/room-type-select/index";
import dlgNotePre from "./compnents/dlg_note_pre";
import checkOut from '../bed_mng/components/_check_out'
import DlgDormBeds from "./compnents/dlg_dorm_beds";

import {  fetchList } from "@/api/platform/dormitory/bed_mng";
import {  queryDormitory } from "@/api/platform/dormitory/floor";

let socket;
export default {
  name: "",
  components:{
    dlgDormChange,
    dlgDormSelect,
    roomTypeSelect2,
    dlgNotePre,
    checkOut,
    DlgDormBeds
  },
  data() {
    let vlSex = (rule, value, callback) => {
      if (value === '男' || value === '女') {
        callback();
      } else {
        callback(new Error('请输入单字 男 或 女'))
      }
    };
    return {
      rowPrint: {}, //打印的对象
      type: 1,
      defaultSelectedPark: false, //默认选中园区
      autoLoading: false,
      dlg1Visible: false, //更换宿舍
      curDlg1Obj: {},
      dlg3Visible: false, //选择宿舍
      curDlg3Obj: {},
      dlg2Visible: false, //凭条预览
      curDlg2Obj: {},
      isStep2: false,
      infoform: {
        name:'',
        sex:'',
        nation:'',
        birthday:'',
        certno:'',
        address:'',
        signOrg:'',
        validDateStart: '',
        validDateEnd: ''
      },
      searchform: {
        parkId: '',
        dormitoryId: '',
        roomType: ''
      },
      searchformRule:{
        parkId: [
          { required: true, message: "请选择所属园区", trigger: "change" }
        ],
        dormitoryId: [
          { required: true, message: "请选择宿舍", trigger: "change" }
        ],
        roomType: [
          { required: true, message: "请选择房间类型", trigger: "change" }
        ]
      },
      infoformRule:{
        name: [
          { required: true, message: "请输入姓名", trigger: "blur" }
        ],
        certno: [
          { required: true, message: "请输入身份证号", trigger: "blur" }
        ],
        sex: [
          { required: true, message: "请输入性别", trigger: "blur" },
          { validator: vlSex, trigger: "blur" }
        ],
        nation: [
          { required: true, message: "请输入民族", trigger: "blur" }
        ],
        birthday: [
          { required: true, message: "请输入出生日期", trigger: "blur" }
        ],
        signOrg: [
          { required: true, message: "请输入签发机关", trigger: "blur" }
        ],
        address: [
          { required: true, message: "请输入地址", trigger: "blur" }
        ],
        validDateStart: [
          { required: true, message: "请输入有效期开始日期", trigger: "blur" }
        ],
        validDateEnd: [
          { required: true, message: "请输入有效期截止日期", trigger: "blur" }
        ],
      },
      pickerOptions1: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      tableOption: tableOption,
      tableData: [],
      parkLabel: '',
      dormLabel: '',
      roomTypeLabel: '',
      curRoom:{},
      curBed:{},
      curCheckIn: {},
      dormList: [],
      dormIds: []
    };
  },
  created() {
    this.doClearValidate("searchform");
    this.openReader()
    this.queryLocalStorage()
  },
  mounted: function() {},
  computed: {},
  watch: {
    type(val){
      if(val===1){
        this.initCurRoom()
      }
    },
    'searchform.parkId'(val){
      this.initCurRoom()
      if(this.defaultSelectedPark){
        this.dormIds = []
        this.searchform.roomType = ''
      }
      if(val){
        this.queryDormitory(val)
      }
    },
    'searchform.dormitoryId'(val){
      this.initCurRoom()
    },
    'searchform.roomType'(val){
      this.initCurRoom()
    },
    dormIds(val){
      if(val && val.length>0){
        this.searchform.dormitoryId = val[0]
        if(val.length>1){
          this.searchform.floorId = val[1]
        }else{
          this.searchform.floorId = undefined
        }
      }else{
        this.searchform.dormitoryId = undefined
        this.searchform.floorId = undefined
      }
      if(this.defaultSelectedPark){
        this.getLabel()
      }
    }
  },
  methods: {
    tableRowClassName({row, index}){
      if(row.isFlag===1){
        return 'curObj'
      }
    },
    queryLocalStorage(){
      if(localStorage.getItem("ksCheckInDormInfo")){
        let obj =JSON.parse(localStorage.getItem("ksCheckInDormInfo"))
        if(obj.parkId){
          this.defaultSelectedPark = false
          this.searchform.parkId = obj.parkId
          this.parkLabel = obj.parkLabel
          this.searchform.roomType = obj.roomType
          this.dormLabel = obj.dormLabel
          this.roomTypeLabel = obj.roomTypeLabel
          if(obj.floorId){
            this.dormIds = [obj.dormitoryId, obj.floorId]
          }else{
            this.dormIds = [obj.dormitoryId]
          }
        }else{
          this.defaultSelectedPark = true
        }
      }
      if(localStorage.getItem("personInfo")){
        let obj2 =JSON.parse(localStorage.getItem("personInfo"))
        this.infoform = obj2
      }
    },
    initLocalStorage(){
      let obj = {
        parkId: this.searchform.parkId,
        parkLabel: this.parkLabel,
        dormitoryId: this.searchform.dormitoryId,
        dormLabel: this.dormLabel,
        floorId: this.searchform.floorId,
        roomType: this.searchform.roomType,
        roomTypeLabel: this.roomTypeLabel
      }
      localStorage.setItem("ksCheckInDormInfo",JSON.stringify(obj))
      localStorage.setItem("personInfo",JSON.stringify(this.infoform))

    },
    async queryDormitory(parkId){
      const res = await queryDormitory({parkId})
      let tempList1 = res.data.data
      let tempList2= []
      tempList1.forEach(el=>{
        let childArr = []
        if(el.floors && el.floors.length>0){
          el.floors.forEach(el2=>{
            childArr.push({
              value: el2.floorId,
              label: el2.floorName
            })
          })
        }
        tempList2.push({
          value: el.id,
          label: el.dormitoryName,
          children: childArr
        })
      })
      this.dormList = tempList2
      if(this.defaultSelectedPark){
        if(this.dormList.length>0){
          if(this.dormList[0].children.length>0){
            this.dormIds = [this.dormList[0].value, this.dormList[0].children[0].value]
          }else{
            this.dormIds = [this.dormList[0].value]
          }
        }
      }
    },
    parkChange(){
      this.defaultSelectedPark = true
    },
    dormChagne(){
      this.defaultSelectedPark = true
    },
    getLabel(){
      this.dormLabel = ''
      if(this.dormIds && this.dormIds.length>0){
        let dormitoryId = undefined
        let floorId = undefined
        dormitoryId = this.dormIds[0]
        if(this.dormIds.length>1){
          floorId = this.dormIds[1]
        }
        if(dormitoryId){
          this.dormList.forEach(el=>{
            if(dormitoryId===el.value){
              this.dormLabel = el.label
            }
          })
        }
        if(floorId){
          this.dormList.forEach(el=>{
            if(dormitoryId===el.value){
              el.children.forEach(el2=>{
                if(floorId===el2.value){
                  this.dormLabel = el.label + '/' + el2.label

                }
              })
            }
          })
        }
      }
    },
    refreshList(){
      if(this.type===2){
        this.getList()
      }else{
        this.tableData = []
        this.rowPrint = {}
      }
    },
    //退宿
    checkOut(row, index) {
      this.curCheckIn = row
      this.$refs.checkout && this.$refs.checkout.open()
    },
    initCurRoom(){
      this.curRoom = {}
      this.searchform.roomId = ""
      this.searchform.roomName = ""

      this.curBed = {}
      this.searchform.bedId = ""
      this.searchform.bedNumber = ""
      this.tableData = []
    },
    //点击 选择房间号
    selectRoom(row, index){
      this.dlg3Visible = true
      // inStatus:, 1 未满 2已满 3空房
      // isNormal: 1,
      this.curDlg3Obj = {
        parkId: this.searchform.parkId,
        dormitoryId: this.searchform.dormitoryId,
        roomType: this.searchform.roomType
      }
    },
    //读取身份证
    readIDCard() {
      try {
        // readyState：值为0，表示正在连接。
        // readyState：值为1，表示连接成功，可以通信了。
        // readyState：值为2，表示连接正在关闭。
        // readyState：值为3，表示连接已经关闭，或者打开连接失败。
        if (socket.readyState == 1) {
          socket.send("EST_Reader_ReadIDCard#");
        }
        else {
          this.resultMsgError("未找到控件，请检查控件是否安装.");
        }
      }
      catch (ex) {
        this.resultMsgError("连接异常,请检查是否成功安装控件.");
      }
    },
    openReader() {
      let _this = this;
      let host = "ws://127.0.0.1:33666"; //客户端电脑本地IP，非服务器IP，无需修改
      if(socket == null){
        socket = new WebSocket(host);
      }else{
        this.resultMsgSuccess("已初始化.");
      }
      try {
        socket.onopen = function () {
          _this.resultMsgSuccess("初始化成功."); //可以注释
          //_this.autoReadIDCard(); //自动读卡可以放这里，自动读卡放身份证的间隔要>300ms，否则会不读卡
        };
        socket.onclose = function () {
          _this.resultMsgError("读卡服务已经断开.");
        };
        socket.onerror = function(){
          _this.resultMsgError("请检查控件是否正常安装.");
        };
        socket.onmessage = function (msg) {
          if (typeof msg.data == "string") {
            let msgM=msg.data+"";
            let msgJson = JSON.parse(msgM);
            switch(msgJson.fun) {
              case "EST_Reader_ReadIDCard#":
                if (msgJson.rCode == 0) {
                  _this.infoform.name = msgJson.name;  //姓名
                  _this.infoform.sex = msgJson.sex;  //性别
                  _this.infoform.nation = msgJson.nation;  //民族
                  _this.infoform.certno = msgJson.certNo;  //身份证号码
                  _this.infoform.address = msgJson.address;  //身份证号码
                  _this.infoform.signOrg = msgJson.department;  //签发机关

                  let d1 = '',d2 = '', d3 = ''
                  if(msgJson.birth.length==8){
                    d1 = msgJson.birth.substr(0, 4)+'-'
                    d1 += msgJson.birth.substr(4, 2)+'-'
                    d1 += msgJson.birth.substr(6, 2)
                  }
                  if(msgJson.effectData.length==8){
                    d2 = msgJson.effectData.substr(0, 4)+'-'
                    d2 += msgJson.effectData.substr(4, 2)+'-'
                    d2 += msgJson.effectData.substr(6, 2)
                  }
                  if(msgJson.expire.length==8){
                    d3 = msgJson.expire.substr(0, 4)+'-'
                    d3 += msgJson.expire.substr(4, 2)+'-'
                    d3 += msgJson.expire.substr(6, 2)
                  }
                  _this.infoform.birthday = d1;  //出生日期
                  _this.infoform.validDateStart = d2;  //有效期开始
                  if(d3 === '长期--' || d3.includes('长期')){
                    d3 = '2099-12-31'
                  }
                  _this.infoform.validDateEnd = d3;  //有效期结束
                  _this.checkIn('infoform')
                  // _this.resultMsgError(msgJson.errMsg);
                }
                else if(msgJson.rCode == "1"){
                  _this.resultMsgError("已停止自动读卡");
                }
                else if(msgJson.rCode == "-2"){
                  _this.resultMsgError("请放身份证");
                }
                else {
                  _this.resultMsgError(msgJson.errMsg);
                }
                break;
              default:
                break;
            }
          }
          else{
            this.resultMsgError("连接异常,请检查是否成功安装控件.");
          }
        };
      }
      catch (ex) {
        this.resultMsgError("连接异常,请检查是否成功安装控件.");
      }
    },
    //自动读取身份证信息
    autoReadIDCard() {
    // readyState：值为0，表示正在连接。
    // readyState：值为1，表示连接成功，可以通信了。
    // readyState：值为2，表示连接正在关闭。
    // readyState：值为3，表示连接已经关闭，或者打开连接失败。
      try {
        if (socket.readyState == 1) {
          socket.send("EST_Reader_ReadIDCard#|1");
          //this.resultMsgWarning("自动读卡，请放身份证...");
        }
        else {
          this.resultMsgError("未找到控件，请检查控件是否安装.");
        }
      }
      catch (ex) {
        this.resultMsgError("连接异常,请检查是否成功安装控件.");
      }
    },
    resultMsgSuccess(msg){
      this.$message({
        message: msg,
        type: 'success'
      });
    },
    resultMsgWarning(msg){
      this.$message({
        message: msg,
        type: 'warning'
      });
    },
    resultMsgError(msg){
      this.$message.error(msg);
    },
    dlg1do(val){
      this.dlg1Visible = val
    },
    dlg2do(val){
      this.dlg2Visible = val
    },
    dlg1doSuccess(){
      this.getList();
    },
    dlg3do(val){
      this.dlg3Visible = val
    },
    dlg3doSuccess(room, bed){
      this.curRoom = room
      this.curBed = bed
      this.searchform.roomId = room.roomId
      this.searchform.roomName = room.roomName
      this.searchform.bedId = bed.id
      this.searchform.bedNumber = bed.bedNumber
      this.getList()
    },
    //重新选择床位号之后
    selectBed(room, bed){
      this.curBed = bed
      this.searchform.bedId = bed.id
      this.searchform.bedNumber = bed.bedNumber
    },
    //点击房间号，重新选择床位号
    bedChange(){
      this.$refs.dlgdormbeds && this.$refs.dlgdormbeds.open()
    },
    //手动分配成功后，查询下一个可住空床位
    async getBeds(roomId) {
      let bedArr = []
      let res = await bedDetail(roomId)
      if(res.data.code==0){
        bedArr = res.data.data.filter(el=>{
          //空床且没有被锁定，且要床位号要大于上次分配的床位号
          if(!el.staffBadge && el.delFlag!==1 && el.bedNumber > this.rowPrint.bedNumber){
            return el
          }
        })
        if(bedArr && bedArr.length>0){
          this.selectBed(null, bedArr[0])
        }else{
          this.curBed = { bedNumber: '已到最后床位号' }
          this.searchform.bedId = ''
          this.searchform.bedNumber = ''
        }
      }else{
        this.curBed = { bedNumber: '没有查到当前房间的床位信息' }
        this.searchform.bedId = ''
        this.searchform.bedNumber = ''
      }
    },
    //下一步
    nextStep(formName){
      this.$refs[formName].validate(valid => {
        if (valid) {
          if(this.type===2){
            if(!this.searchform.roomName || !this.searchform.bedNumber){
              this.$message.error('请选择房间号和床位号');
              return
            }
          }
          this.isStep2 = true
        } else {
          return false;
        }
      });
    },
    defaultHandle(e){
      this.searchform.parkId = e.value
    },
    getParkItem(e){
      this.parkLabel = e.label
    },
    getRoomTypeItem(e){
      this.roomTypeLabel = e.label
    },
    //确认分配
    async checkIn(formName){
      // this.initLocalStorage()
      // return
      const res1 = await this.$refs[formName].validate()
      if(res1){
        if(this.type===2){
          if(!this.searchform.bedId || !this.searchform.bedNumber){
            this.$message.error('请先选择可用床位！');
            return
          }
        }
        let obj = Object.assign(this.searchform,this.infoform)
        if(obj.sex==='男'){
          obj.sex = 0
        }else{
          obj.sex = 1
        }
        //手动分配的时候 比较入住人员性别和所选房间性别
        if(this.type===2){
          //房间性别为 “男”或“女”时才比较，为“夫妻”或“其他”时不比较
          if(this.curRoom.roomSex === 0 || this.curRoom.roomSex === 0){
            if(this.curRoom.roomSex !== obj.sex){
              this.$message.error('入住人员性别和所选房间性别不匹配！');
              return
            }
          }
        }
        try{
          this.initLocalStorage()
          const res1 = await checkDormInInfo({parkId: this.searchform.parkId, certno: obj.certno})
          if(res1.data.code===0){
            if(res1.data.data){
              this.$message.error(res1.data.data);
              return
            }
          }else{
            this.$message.error('入住检查失败！');
            return
          }
          this.autoLoading = true
          const res = await autoallot(obj)
          if(res.data.code===0 && res.data.data){
            this.$notify({
              title: '成功',
              message: '宿舍分配成功',
              type: 'success'
            });
            this.tableData = res.data.data
            this.tableData.forEach(el=>{
              if(el.isFlag===1){
                this.rowPrint = el
                this.$nextTick(()=>{
                  document.getElementById("printDomBtn") && document.getElementById("printDomBtn").click()
                })
              }
            })
            //手动分配成功后，自动获取下一个可用空床位
            if(this.type===2){
              this.getBeds(this.searchform.roomId)
            }
          }else{
            this.$message.error(res.data.msg);
          }
          this.autoLoading = false
        }catch(err){
          this.autoLoading = false
        }
      }
    },
    //凭条预览
    handlePrintPre(row){
      this.dlg2Visible = true
      this.curDlg2Obj = row
    },
    //重新分配
    handleChange(row, index){
      this.dlg1Visible = true
      this.curDlg1Obj = row
    },
    //手动分配列表
    async getList() {
      this.tableLoading = true;
      const res = await fetchList({current: 1,size: 1000, roomId: this.searchform.roomId})
      this.tableData = res.data.data.records
    }
  }
};
</script>
<style lang="scss" scoped>

.ks_check_in ::v-deep {
  .curObj{
    background: yellow;
  }
  .el-table--striped .el-table__body tr.el-table__row--striped.curObj td{
    background: yellow;
  }
  .el-table--striped .el-table__body tr.el-table__row--striped.curObj.hover-row td{
    background: #ecf5ff;
  }
  .rd1{
    .el-radio-button__inner:hover{
      color: #ed6d00;
    }
    .el-radio-button__orig-radio:checked+.el-radio-button__inner{
      color: #fff;
      background-color: #ed6d00;
      border-color: #ed6d00;
      box-shadow: -1px 0 0 0 #ed6d00;
    }
  }
  .box-left{
    width: 500px;
    &_inner{
      padding-bottom: 105px;
    }
    .el-form-item__label{
      text-align: right;
    }
    .item2{
      display: flex;
      // justify-content: space-between;
      .el-form-item__content{
        width: 100px;
      }
    }
  }
  .my-scrollbar{
    padding: 0 0 0 505px;
  }
  .person_i{
    width: 14px;
    height: 14px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -4px;
    background-image: url('/img/dorm/person_i.png');
    background-size: 100% 100%;
  }
  .boy_c{
    color: #2a7ffe;
    padding: 4px 15px;
    background: rgba(42, 127, 254, .1);
  }
  .girl_c{
    color: #ff698c;
    padding: 4px 15px;
    background: rgba(255, 105, 140, .1);
  }
  .pp_dorm_i{
    width: 13px;
    height: 13px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -3px;
    background-image: url('/img/dorm/zdppss.png');
    background-size: 100% 100%;
  }
  .print2_btn{
    color: #0dbc82;
  }
  .print2_i{
    width: 12px;
    height: 14px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -2px;
    background-image: url('/img/dorm/print_green.png');
    background-size: 100% 100%;
  }
  .change_dorm_i{
    width: 13px;
    height: 13px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -2px;
    background-image: url('/img/dorm/hs_i.png');
    background-size: 100% 100%;
  }
  .top-til{
    font-weight: bold;
    font-size: 18px;
  }
  .lft_top,
  .lft_btm{
    padding: 0 10px;
  }
  .lft_btm{
    .el-form-item__content{
      width: 310px;
    }
  }
  .lft_btn{
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    text-align: center;
    padding-bottom: 10px;
    background: #fff;
  }
  .nav_t{
    display: flex;
    padding-top: 25px;
    div{
      width: 201px;
      height: 36px;
      line-height: 36px;
      background-repeat: no-repeat;
      padding-left: 30px;
      color: #fff;
    }
    .st1{
      background-image: url('/img/dorm/nr1.png');
      cursor: pointer;
    }
    .st2{
      background-image: url('/img/dorm/nr2.png');
    }
    .st2.active{
      background-image: url('/img/dorm/nr2_2.png');
    }
  }
  .t1{
    font-weight: bold;
    margin-bottom: 10px;
  }
  .tip{
    color: #999;
    margin-bottom: 10px;
  }
  .tip2{
    margin-top: 10px;
    font-size: 12px;
    color: #ffa271;
  }
  .item1{
    // display: flex;
    .el-form-item__content{
      width: auto;
    }
    .val-item{
      background: #d2d4d8;
      padding: 4px 15px
    }
  }
}
</style>

<style lang="scss" scoped>
  .note-outer{
    width: 300px;
    margin: 0 auto 15px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    opacity: 0;
  }
  .printStyle{
    opacity: 1;
  }
  .note-inner ::v-deep {
    width: 300px;
    height: 400px;
    background: #fff;
    margin: 0 auto;
    padding: 10px;
    text-align: center;
    .n_logo {
      height: 26px;
      font-size: 14px;
      // background-image: url("/img/dorm/logo_d.png");
      // background-repeat: no-repeat;
      // background-size: auto 100%;
    }
    .p1 {
      color: #000;
      font-size: 18px;
      margin-top: 30px;
      font-weight: bold;
    }
    .p2 {
      font-size: 22px;
      font-weight: bold;
      color: #333;
      margin-bottom: 10px;
    }
    .p3,
    .p4 {
      font-size: 18px;
    }
    .p5 {
      margin-top: 10px;
      margin-bottom: 30px;
    }
    .p6 {
      color: #999;
      font-size: 12px;
    }
  }
</style>
<style lang="scss" scoped media="print">
@page {
  size: 48mm portrait; /* auto is the initial value */
  margin: 3mm; /* this affects the margin in the printer settings */
}

html {
  width: 100%;
  // border: 1px solid black;
  background-color: #ffffff;
  margin: 0px; /* this affects the margin on the html before sending to printer */
}

body {
  width: 100%;
  //margin: 10mm 15mm 10mm 15mm; /* margin you want for the content */
}
//打印区样式
.note-inner {
  width: 100%;
  // border: 1px solid black;
  // height: 400px;
  background: #fff;
  margin: 0 auto;
  // padding: 10px;
  font-size: 12pt;
  text-align: center;
  .n_logo {
    height: 26px;
    font-size: 16pt;
    img{
      width: 100%;
      height: auto;
    }
    // background-image: url("/img/dorm/logo_d.png");
    // background-repeat: no-repeat;
    // background-size: auto 100%;
  }
  .p1 {
    color: #333;
    font-size: 12pt;
    margin-top: 30px;
    font-weight: bold;
  }
  .p2 {
    font-size: 16pt;
    font-weight: bold;
    color: #333;
    margin-bottom: 10px;
  }
  .p3,
  .p4 {
    font-size: 14pt;
  }
  .p5 {
    margin-top: 10px;
    margin-bottom: 30px;
  }
  .p6 {
    color: #999;
    font-size: 12pt;
  }
}
</style>

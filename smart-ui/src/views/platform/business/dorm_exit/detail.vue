<!--业务设置，退宿申请  -->
<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="dataform" :rules="emailRule" :inline="false" class="dot-form" :model="dataform" label-width="140px">
            <div class="node1" v-for="(item, index) in dataform.editApprovalNode" :key="index">
              <div class="row1">
                <div class="btn">
                  <el-button type="text" @click="delNode(index)" v-if="dataform.editApprovalNode.length !== 1">删除节点</el-button>
                </div>
                <div class="row_cell row_label_1">审批顺序</div>
                <div class="row_cell row_value_1">
                  <el-input v-model="item.sort" placeholder="请输入顺序" disabled></el-input>
                </div>
                <div class="row_cell row_label_2">审批节点名称</div>
                <div class="row_cell row_value_2">
                  <el-input v-model="item.name" placeholder="请输入节点名称"></el-input>
                </div>
              </div>
              <div class="tip1">审批配置</div>
              <!-- v-for="(item_c, index_c) in item.conditions" :key="'c'+index_c" -->
              <section class="sec1">
                <!-- <div class="sec1_title">
                  <el-button class="toggleBtn" type="primary" icon="el-icon-arrow-down" @click="toggle()" round plain></el-button>
                  <span>条件1</span>
                  <el-button type="text" @click="delCondition2(index_c)" v-if="item.conditions.length!==1">删除条件</el-button>
                </div> -->
                <div class="sec1_cont">
                  <el-form-item label="触发条件" prop="type">
                    <div>
                      <div class="row2" v-for="(itme_t, index_t) in item.conditions" :key="'t' + index_t">
                        <!-- 添加、删除 触发条件 v-if="index_t < item.conditions.length-1"-->
                        <div class="btns">
                          <el-button
                            type="text"
                            icon="el-icon-plus"
                            @click="addCondition(item.conditions)"
                            v-if="item.conditions.length === 0 || index_t === item.conditions.length - 1"
                          ></el-button>
                          <el-button type="text" icon="el-icon-delete" @click="delCondition(item.conditions, index_t)"></el-button>
                        </div>
                        <el-select v-model="itme_t.conditionType" @change="conditionTypeChange(itme_t)">
                          <el-option label="退宿原因" :value="6"></el-option>
                          <el-option label="楼栋名称" :value="7"></el-option>
                        </el-select>
                        <template>
                          <el-select v-model="itme_t.comparator" class="sel2">
                            <el-option label="等于" :value="1"></el-option>
                            <el-option label="不等于" :value="2"></el-option>
                          </el-select>
                          <!-- 物品类型 -->
                          <el-select v-model="itme_t.compareValue" v-if="itme_t.conditionType === 6">
                            <el-option v-for="(good_item, good_index) in goodArr" :label="good_item.label" :value="good_item.value" :key="good_index"></el-option>
                          </el-select>
                          <!-- 楼栋 @doChange="doDormChange"-->
                          <dormSelect v-if="itme_t.conditionType === 7" :parkId="parkId" v-model="itme_t.compareValue"></dormSelect>
                          <el-select v-model="itme_t.connector" class="sel2">
                            <el-option label="且" :value="1"></el-option>
                            <el-option label="或" :value="2"></el-option>
                          </el-select>
                        </template>
                      </div>
                      <div class="row2" v-if="!item.conditions || item.conditions.length === 0">
                        <!-- 添加、删除 触发条件 -->
                        <div class="btns" style="left: 10px; right: auto">
                          <el-button type="text" icon="el-icon-plus" @click="addCondition(item.conditions)"></el-button>
                        </div>
                      </div>
                    </div>
                  </el-form-item>
                  <el-form-item label="审批人设置">
                    <el-radio-group v-model="item.isExistApprover" @change="approverChange(item)">
                      <el-radio :label="0">无指定审批人</el-radio>
                      <el-radio :label="1">指定审批人</el-radio>
                      <el-radio :label="2">同室友</el-radio>
                      <el-radio :label="3">上级领导</el-radio>
                    </el-radio-group>
                    <div v-if="item.isExistApprover === 1">
                      <!-- <p>
                        已添加
                        <span style="color: #ed6d00">{{ item.approvalPersons.length }}</span> 位推送人信息
                      </p> -->
                      <table class="tbl">
                        <tr>
                          <td class="td1">序号</td>
                          <td class="td2">审批人编号</td>
                          <td class="td2">
                            姓名
                            <div class="line-btn">
                              <el-button type="text" icon="el-icon-plus" @click="addPerson(item.approvalPersons)"></el-button>
                            </div>
                          </td>
                        </tr>
                        <tr v-for="(person_item, person_index) in item.approvalPersons" :key="'person' + person_index">
                          <td>
                            {{ person_index + 1 }}
                          </td>
                          <td>
                            <el-form-item label prop="">
                              <el-input
                                type="text"
                                @keypress.native.enter="getStaffDetail(person_item, person_index, item.approvalPersons)"
                                v-model="person_item.approverBadge"
                                placeholder="输入后回车，查询姓名"
                              ></el-input>
                            </el-form-item>
                          </td>
                          <td>
                            <el-form-item label prop="">
                              <el-input type="text" v-model="person_item.approverName" placeholder="-" readonly></el-input>
                            </el-form-item>
                            <div class="line-btn">
                              <el-button type="text" icon="el-icon-delete" @click="delPerson(item.approvalPersons, person_index)"></el-button>
                              <!-- <el-button type="text" icon="el-icon-delete" @click="delPerson(item.approvalPersons, person_index)" v-if="person_index < item.approvalPersons.length-1"></el-button> -->
                              <!-- <el-button type="text" icon="el-icon-plus" @click="addPerson(item.approvalPersons, person_item)" v-if="person_index === item.approvalPersons.length-1"></el-button> -->
                            </div>
                          </td>
                        </tr>
                      </table>
                      <!-- <div class="tip2">
                        <p>输入人员编号后，点击回车，查询姓名</p>
                      </div> -->
                    </div>
                    <!-- <div>审批人通过规则</div>
                    <div>
                      <el-radio-group v-model="item.passRule">
                        <el-radio :label="1">审批人全部通过，进入下个流程</el-radio>
                        <el-radio :label="2">审批人任其一通过/拒绝，进入下个流程</el-radio>
                      </el-radio-group>
                    </div> -->
                  </el-form-item>
                  <el-form-item label="审批人通过规则">
                     <el-radio-group v-model="item.passRule">
                        <el-radio :label="1">审批人全部通过，进入下个流程</el-radio>
                        <el-radio :label="2">审批人任其一通过/拒绝，进入下个流程</el-radio>
                      </el-radio-group>
                  </el-form-item>
                  <el-form-item label="通知方式" class="w1" prop="type">
                    <el-checkbox v-model="item.isAppPush" :true-label="1" :false-label="0">APP PUSH消息/APP站内消息</el-checkbox>
                    <el-checkbox v-model="item.isMsg" @change="msgChange(item)">短信通知</el-checkbox>
                    <el-select v-if="item.isMsg" v-model="item.msgTemplate" style="width: 200px !important; margin: 0 20px 0 5px">
                      <el-option v-for="(msg_item, msg_index) in msgArr" :label="msg_item.tempName" :value="msg_item.id" :key="msg_index"></el-option>
                    </el-select>
                    <el-checkbox v-if="parkName === '裕同科技许昌园区'" v-model="item.isWeChatPush" :true-label="1" :false-label="0">微信推送</el-checkbox>
                    <!-- isWeChatPush -->
                  </el-form-item>
                </div>
              </section>
              <el-button type="text" @click="addNode" v-if="index === dataform.editApprovalNode.length - 1">添加审批节点</el-button>
              <!-- <el-button type="text" @click="addCondition2()">添加条件</el-button> -->
            </div>
            <div class="btns">
              <el-button type="primary" @click="saveInfo()" :loading="loading">保存</el-button>
              <el-button type="primary" @click="goBack()" plain>返回</el-button>
            </div>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { getStaffDetail, editObj, getObj, getMsg, getGoodType, triggerCondition } from './_service'
export default {
  data() {
    return {
      loading: false,
      dataform: {
        editApprovalNode: []
      },
      parkId: null,
      parkName: '',
      id: '',
      emailRule: {},
      msgArr: [], //短信模板集合
      goodArr: [], //物品类型
      conditionAryy: []
    }
  },
  created() {
    this.dataform.editApprovalNode.push(this.getNode())
    this.dataform.editApprovalNode[0].approvalPersons.push(this.getPerson())

    this.getMsg()
    this.getGoodType()

    if (this.$route.query.id) {
      this.id = parseInt(this.$route.query.id)
      this.getDetail(this.id)
    }
    if (this.$route.query.parkId) {
      this.parkId = parseInt(this.$route.query.parkId)
    }
    if (this.$route.query.parkName) {
      this.parkName = this.$route.query.parkName
    }
  },
  mounted: function () {},
  computed: {},
  methods: {
    getPerson() {
      return {
        approverBadge: undefined,
        approverName: undefined,
        nodeId: undefined,
        result: undefined,
        sort: 1
      }
    },
    getNode() {
      return {
        approvalId: 0,
        approvalPersons: [],
        conditions: [],
        isAppPush: undefined,
        isExistApprover: 0,
        msgTemplate: undefined,
        name: undefined,
        passRule: 1,
        sort: 1
      }
    },
    getCondition() {
      return {
        comparator: undefined,
        compareValue: undefined,
        conditionType: 6,
        connector: undefined,
        nodeId: undefined,
        sort: 1
      }
    },
    async getDetail(id) {
      let res = {}
      try {
        res = await getObj(id)
        if (res.data.code === 0 && res.data.data.length > 0) {
          this.dataform.editApprovalNode = res.data.data
          this.dataform.editApprovalNode.forEach((el) => {
            if (el.msgTemplate) {
              this.$set(el, 'isMsg', true)
            }
            if (el.conditions) {
              el.conditions.forEach((elc) => {
                if (elc.compareValue !== null && elc.compareValue !== undefined) {
                  elc.compareValue = Number(elc.compareValue)
                }
              })
            } else {
              el.conditions = []
            }
          })
        }
      } catch (err) { /* 忽略异常：详情加载失败保持页面现状 */ }
    },
    async saveInfo() {
      this.loading = true
      let obj = JSON.parse(JSON.stringify(this.dataform))
      obj.editApprovalNode.forEach((el) => {
        el.approvalId = this.id
        if (el.approvalPersons && el.approvalPersons.length > 0) {
          el.approvalPersons = el.approvalPersons.filter((el) => {
            if (el.approverBadge && el.approverName) {
              return el.approverBadge
            }
          })
        }
        if (el.approvalPersons && el.approvalPersons.length == 0 && el.isExistApprover != 2 && el.isExistApprover != 3) {
          el.isExistApprover = 0
        }
      })

      let queryData = {
        approvalId: this.id,
      }
      const res = await editObj(obj.editApprovalNode, queryData)
      if (res.data.code === 0) {
        this.$notify({
          title: '成功',
          message: '修改成功',
          type: 'success'
        })
        // this.getDetail(this.id)
        this.goBack()
      } else {
        this.$notify.error({
          title: '失败',
          message: res.data.msg
        })
      }
      this.loading = false
    },
    // 触发条件，切换
    conditionTypeChange(item) {
      item.comparator = undefined
      item.compareValue = undefined
      item.connector = undefined
    },
    //是否有指定人，切换
    approverChange(item) {
      if (item.isExistApprover === 1) {
        item.approvalPersons = [this.getPerson()]
      } else {
        item.approvalPersons = []
      }
    },
    //是否短信通知， 切换
    msgChange(item) {
      if (!item.isMsg) {
        item.msgTemplate = null
      }
    },
    // 短信模板
    async getMsg() {
      const res = await getMsg()
      this.msgArr = res.data.data
    },
    // 退宿原因
    async getGoodType() {
      // const res = await getGoodType()
      this.goodArr = [
        { label: '离职', value: 3 },
        { label: '自离', value: 5 },
        { label: '外宿', value: 2 }
      ]
    },
    // 触发条件
    async triggerCondition() {
      const res = await triggerCondition()
      this.conditionAryy = res.data.data
    },
    //根据工号获取员工姓名
    async getStaffDetail(item, index, list) {
      let isExist = false
      list.forEach((el, index2) => {
        if (el.approverBadge === item.approverBadge && index2 !== index) {
          this.$message.error('当前节点已存在该工号信息，请勿重复添加')
          isExist = true
          return
        }
      })
      if (isExist) return
      let res = {}
      try {
        res = await getStaffDetail(item.approverBadge)
        if (res.data.code === 0) {
          item.approverName = res.data.data.name
        }
      } catch (err) {
        item.approverName = ''
        this.$message.error('没有查到相关信息，请核对工号')
      }
    },
    goBack() {
      this.$router.go(-1)
    },
    //添加审批节点
    addNode() {
      let obj = this.getNode()
      let node = Object.assign(obj, { sort: this.dataform.editApprovalNode.length + 1 })
      node.approvalPersons.push(this.getPerson())
      this.dataform.editApprovalNode.push(node)
    },
    //删除审批节点
    delNode(index) {
      this.dataform.editApprovalNode.splice(index, 1)
    },
    //添加触发条件
    addCondition(item) {
      let obj = Object.assign(this.getCondition(), { sort: item.length + 1 })
      item.push(obj)
    },
    //删除触发条件
    delCondition(item, index) {
      item.splice(index, 1)
    },
    //删除审批人
    delPerson(arr, index) {
      arr.splice(index, 1)
    },
    //添加审批人
    addPerson(arr) {
      let obj = Object.assign(this.getPerson(), { sort: arr.length + 1 })
      arr.push(obj)

      // if(item.approverBadge && item.approverName){
      //   let obj = Object.assign(this.getPerson(),{sort: arr.length+1})
      //   arr.push(obj)
      //   console.log('persons---', arr)
      // }else{
      //   this.$message({
      //     message: '请先完善当前信息',
      //     type: 'warning'
      //   });
      // }
    },
    //条件展开，收起
    toggle() {}
  }
}
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 800px;
  padding: 30px 30px 30px 60px;
  .node1 {
    margin-bottom: 50px;
  }
  .row1 {
    position: relative;
    display: flex;
    align-items: center;
    margin-bottom: 5px;
    .btn {
      position: absolute;
      right: -75px;
      top: 5px;
    }
    .row_cell {
      border: 1px solid #e0e0e0;
      border-right: none;
      line-height: 40px;
      .el-input__inner {
        border: none;
      }
    }
    .row_cell:last-child {
      border-right: 1px solid #e0e0e0;
    }
    .row_label_1,
    .row_label_2 {
      text-align: center;
    }
    .row_label_1 {
      width: 80px;
    }
    .row_label_2 {
      width: 120px;
    }
    .row_value_1,
    .row_value_2 {
      flex: 1;
    }
  }
  .row2 {
    position: relative;
    display: flex;
    padding-right: 50px;
    margin-bottom: 10px;
    .el-select {
      margin-right: 10px;
    }
    .el-select:last-child {
      margin-right: 0;
    }
    .btns {
      position: absolute;
      right: 0px;
      top: 5px;
      .el-button {
        font-size: 18px;
      }
      .el-button + .el-button {
        margin-left: 3px;
      }
    }
  }
  .sec1 {
    border: 1px solid #e0e0e0;
    margin-bottom: 15px;
  }
  .sec1_title {
    position: relative;
    display: flex;
    justify-content: space-between;
    padding: 0 10px;
    line-height: 40px;
    border-bottom: 1px solid #e0e0e0;
    .toggleBtn {
      position: absolute;
      left: -35px;
      top: 5px;
      padding: 5px;
    }
  }
  .sec1_cont {
    padding: 20px 10px;
  }
  .tip {
    color: #ed6d00;
    font-size: 14px;
    line-height: 20px;
    margin: 5px 0 5px 0;
  }
  .tip1 {
    color: #999;
    font-size: 14px;
    padding: 10px 0;
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
    width: 92%;
    margin-bottom: 20px;
    .el-input__inner {
      text-align: center;
      border: none;
      outline: none;
    }
    td {
      text-align: center;
      border: 1px solid #e0e0e0;
      position: relative;
    }
    .td1 {
      width: 12%;
    }
    .td2 {
      width: 44%;
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

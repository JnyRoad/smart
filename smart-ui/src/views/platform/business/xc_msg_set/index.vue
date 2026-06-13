<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="dateForm" :rules="dateFormRule" :inline="false" class="dot-form" :model="dateForm" label-width="85px">
            <el-form-item label="模板名称" class="w1" prop="name">
              <el-input v-model="dateForm.name" placeholder="请输入模板名称"></el-input>
            </el-form-item>
            <el-form-item label="接收人" prop="personList">
              <div>
                <p>
                  已添加
                  <span style="color: #ed6d00">{{ dateForm.personList.length }}</span> 个接收人信息
                </p>
                <table class="tbl">
                  <tr>
                    <td>工号</td>
                    <td>姓名</td>
                  </tr>
                  <tr v-for="(item, index) in dateForm.personList" :key="index">
                    <td>
                      <el-input type="text" v-model="item.badge" readonly></el-input>
                    </td>
                    <td>
                      <el-input type="text" v-model="item.name" readonly></el-input>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-delete" @click="delLine(index)"></el-button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <el-form-item label prop="badge">
                        <el-input type="text" v-model="dateForm.badge" @keyup.enter.native="getStaffDetail('dateForm')" placeholder="点击输入工号，按回车查询姓名"></el-input>
                      </el-form-item>
                    </td>
                    <td>
                      <el-form-item label prop="staffName">
                        <el-input type="text" v-model="dateForm.staffName" placeholder="姓名自动获取" readonly></el-input>
                      </el-form-item>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-plus" @click="addLine"></el-button>
                      </div>
                    </td>
                  </tr>
                </table>
                <div class="tip2">
                  <p>输入工号后，按回车键查询姓名。（输入后要点击右侧的 " + " 才视为完成一条接收人信息的添加 ）</p>
                </div>
              </div>
            </el-form-item>
            <el-form-item label>
              <el-button type="primary" @click="saveInfo()" :loading="addLoading">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { saveData, getList} from './_service'
import { getStaffDetail } from "@/api/platform/_publicService"
export default {
  name: 'dormitory_set',
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
      dateForm: {
        name: '',
        badge: '',
        staffName: '',
        personList: []
      },
      dateFormRule: {
        name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
        personList: [{ required: true, message: '请添加接收人', trigger: 'blur' }],
        // badge: [{ validator: badgeValid, trigger: 'blur' }],
        // staffName: [{ validator: nameValid, trigger: 'blur' }]
      },
      addLoading: false,
    }
  },
  async created() {
    this.getList()
  },
  methods: {
    saveInfo() {
      this.$refs['dateForm'].validate((valid) => {
        if (valid) {
          saveData(this.dateForm)
            .then((response) => {
              var dataResult = response.data.data
              if (dataResult === true) {
                this.$notify({
                  title: '成功',
                  message: '保存成功',
                  type: 'success',
                  duration: 2000
                })
                this.getList()
              } else if (dataResult === false) {
                this.$notify({
                  title: '失败',
                  message: response.data.msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.addLoading = false
            }).catch((err) => {
              this.addLoading = false
            })
        }
      })
    },
    /**
     * 移除接收人
     */
    delLine(index) {
      this.dateForm.personList.splice(index, 1)
    },
    /**
     * 添加接收人
     */
    addLine() {
      let badgeIsOk = false
      let nameIsOk = false
      this.$refs.dateForm.validateField('badge', (msg) => {
        if (this.validatenull(msg)) {
          badgeIsOk = true
        } else {
          badgeIsOk = false
        }
      })
      this.$refs.dateForm.validateField('staffName', (msg) => {
        if (this.validatenull(msg)) {
          nameIsOk = true
        } else {
          nameIsOk = false
        }
      })
      if (badgeIsOk && nameIsOk) {
        let arr = this.dateForm.personList.filter(el=>{
          return el.staffId === this.dateForm.staffId
        })
        if(arr && arr.length>0){
          this.$message.error('该工号已存在接收人列表内');
          return
        }
        this.dateForm.personList.push({
          badge: this.dateForm.badge,
          name: this.dateForm.staffName
        })
        this.dateForm.badge = ''
        this.dateForm.staffName = ''
      }
    },
     /**
     * 获取员工详情
     */
    getStaffDetail(formName) {
      this.$refs[formName].validateField("badge", errMsg => {
        if (!errMsg) {
          getStaffDetail(this.dateForm.badge).then(res => {
            let result = res.data.data
            if (!this.validatenull(result)) {
              let id = result.id
              if (!this.validatenull(id)) {
                this.dateForm.staffId = result.id
                this.$set(this.dateForm, 'staffName', result.name)
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
    async getList(){
      const res = await getList()
      if(res.data.data && res.data.data.length>0){
        let redData = res.data.data[0]
        if(!redData.personList){
          redData.personList = []
        }
        this.dateForm = redData
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 900px;
  padding: 50px 0 0 60px;
  .el-checkbox + .el-checkbox {
    margin-left: 0;
  }
  .el-checkbox {
    margin: 0 20px 0 0;
  }
  .radio-cont {
    margin: 20px 0 20px 85px;
  }
  .radio-input {
    display: inline-block;
    margin-left: 20px;
  }
  .w1 {
    width: 500px;
  }
  .w2 {
    width: 60px;
    margin: 0 10px;
    .el-input__inner {
      height: 30px;
      line-height: 30px;
      padding: 0 10px;
      text-align: center;
    }
  }
  .w3 {
    width: 120px !important;
    margin: 0 10px;
    // .el-input__inner {
    //   height: 30px;
    //   line-height: 30px;
    //   padding: 0 10px;
    //   text-align: center;
    // }
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
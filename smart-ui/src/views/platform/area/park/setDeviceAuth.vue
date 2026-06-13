<!--园区管理，配置信息，权限配置  -->
<template>
  <div class="content">
    <el-form ref="form" :rules="rulesRefuse" :model="form" class="form" label-position="left" label-width="170px">
      <!-- red-star -->
      <p class="p1" style="margin-top: 0">员工通行权限设置</p>
      <el-form-item label="员工刷脸通行权限" :prop="'auth.2.authId'">
        <cpersonAuth v-model="form.auth[2].authId" :dataObj="personAuthList"></cpersonAuth>
      </el-form-item>
      <el-form-item label="员工车辆通行权限" :prop="'auth.3.authId'">
        <cvehicleAuth v-model="form.auth[3].authId" :dataObj="vehicleAuthList"></cvehicleAuth>
      </el-form-item>
      <el-form-item label="特殊职层车辆通行权限" prop="jcheIds">
        <cjcheList v-model="form.jcheIds"></cjcheList>
      </el-form-item>
      <el-form-item label="" :prop="'auth.7.authId'">
        <cvehicleAuth v-model="form.auth[7].authId" :dataObj="vehicleAuthList"></cvehicleAuth>
      </el-form-item>
      <p class="p1">访客通行权限设置</p>
      <el-form-item label="访客人员通行权限" :prop="'auth.0.authId'">
        <cpersonAuth v-model="form.auth[0].authId" :dataObj="personAuthList"></cpersonAuth>
      </el-form-item>
      <el-form-item label="访客车辆通行权限" :prop="'auth.1.authId'">
        <cvehicleAuth v-model="form.auth[1].authId" :dataObj="vehicleAuthList"></cvehicleAuth>
      </el-form-item>
      <p class="p1">更多通行权限设置</p>
      <el-form-item label="物流车辆通行权限" :prop="'auth.4.authId'">
        <cvehicleAuth v-model="form.auth[4].authId" :dataObj="vehicleAuthList"></cvehicleAuth>
      </el-form-item>
      <el-form-item label="公司车辆通行权限" :prop="'auth.6.authId'">
        <cvehicleAuth v-model="form.auth[6].authId" :dataObj="vehicleAuthList"></cvehicleAuth>
      </el-form-item>
      <el-form-item label="非员工车辆通行权限" :prop="'auth.5.authId'">
        <cvehicleAuth v-model="form.auth[5].authId" :dataObj="vehicleAuthList"></cvehicleAuth>
      </el-form-item>
      <div class="btns">
        <el-button type="primary" @click="cancel" plain>取消</el-button>
        <el-button type="primary" @click="save('form')" :loading="setLoading">保存</el-button>
      </div>
    </el-form>
  </div>
</template>
<script>
  import {editAuth, fetchAuthList, fetchAuthId} from "@/api/platform/area/park-set";
  import {mapGetters} from "vuex";
  import {validatenull} from "@/util/validate";
  import cpersonAuth from "./components/personAuth";
  import cvehicleAuth from "./components/vehicleAuth";
  import cjcheList from "./components/jcheList";

  const codeOption = {
    1: '访客人员通行权限',
    2: '访客车辆通行权限',
    4: '员工刷脸通行权限',
    5: '员工车辆通行权限',
    7: '物流车通行权限',
    8: '非员工车辆通行权限',
    9: '公司车辆通行权限',
    10: '非员工车辆通行权限'
  }
  // 10: '特殊职层通行权限'
  export default {
    data() {
      return {
        setLoading: false,
        form: {
          auth: [],
          jcheIds: []
        },
        personAuthList: [],
        vehicleAuthList: [],
        rulesRefuse: {
          jcheIds: [
            {required: true, message: "请选择职层", trigger: "change"}
          ],
          'auth.0.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ],
          'auth.1.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ],
          'auth.2.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ],
          'auth.3.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ],
          'auth.4.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ],
          'auth.5.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ],
          'auth.6.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ],
          'auth.7.authId': [
            {required: true, message: "不可为空", trigger: "blur"}
          ]
        },
      };
    },
    components: {
      cpersonAuth,
      cvehicleAuth,
      cjcheList
    },
    props: {
      parkId: [Number, String]
    },
    created() {
      this.initAuthArr()
      this.getAuthList(this.parkId)
      this.getData(this.parkId)
    },
    mounted() {
    },
    computed: {
      ...mapGetters(["permissions"])
    },
    methods: {
      getAuthList(parkId) {
        let personParams = Object.assign({parkId: parkId, type: 1});
        let vehicleParams = Object.assign({parkId: parkId, type: 3});
        fetchAuthList(
          Object.assign(
            {
              current: 1,
              size: 999
            }, personParams
          )).then(response => {
          let self = this;
          self.personAuthList = response.data.data.records;
        });
        fetchAuthList(
          Object.assign(
            {
              current: 1,
              size: 999
            }, vehicleParams
          )).then(response => {
          let self = this;
          self.vehicleAuthList = response.data.data.records;
        });
      },
      initAuthArr() {
        let authArr = []
        for (let i = 1; i < 11; i++) {
          if (i !== 3 && i !== 6) {
            authArr.push({
              authId: null,
              businessCode: i,
              parkId: this.parkId
            })
          }
        }
        this.form.auth = authArr
      },
      getData(parkId) {
        fetchAuthId({
          parkId: parkId
        }).then(res => {
          if (validatenull(res.data.data)) {
            this.initAuthArr()
          } else {
            this.form = res.data.data
            let jcheIds = res.data.data.jcheIds
            if (jcheIds.length > 0) {
              let arr = []
              jcheIds.forEach(el => {
                arr.push(el + '')
              })
              this.form.jcheIds = arr
            }
          }
        });
      },
      cancel() {
        const src = `/platform/area/park`;
        this.$router.push({
          path: src
        });
      },
      save(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.setLoading = true
            let obj = {
              auth: this.form.auth,
              jcheIds: this.form.jcheIds,
              parkId: this.parkId
            }
            editAuth(obj).then(response => {
              this.setLoading = false
              this.$router.push({
                path: `/platform/area/park`
              });
            }).catch(err=>{
              this.setLoading = false
            });
          } else {
            return false;
          }
        });
      }
    }
  };
</script>

<style lang="scss" scoped>
  .content ::v-deep {
    padding: 0 10%;

    .form {
      width: 600px;
      margin: 0 auto;

      .p1 {
        font-weight: bold;
        margin: 30px 0 20px 0;
      }
    }
  }
</style>

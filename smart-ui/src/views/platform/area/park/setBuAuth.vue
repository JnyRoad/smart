<!--园区管理，配置信息，访客设置  -->
<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <el-form ref="areaForm" :inline="false" class="dot-form" label-width="85px">
              <el-form-item label prop="">
                <div class="row">
                  <div class="dv1"> BU列表 </div>
                  <div class="dv2"></div>
                  <div class="dv3"> 配置权限（可多选） </div>
                </div>
                <div class="row" v-for="(item, index) in buList" :key="index">
                  <div class="dv1">
                    <el-tooltip placement="bottom">
                      <div slot="content" style="line-height: 25px">
                        {{item.compName}}
                      </div>
                      <el-tag class="-ellipsis" type="warning">{{item.compName}}</el-tag>
                    </el-tooltip>
                  </div>
                  <div class="dv2" v-if="item.securityId && item.securityId!==null && item.securityId.length>0"> 已设置 </div>
                  <div class="dv2 ft-danger" v-else> 未设置 </div>
                  <div class="dv3">
                    <el-tooltip placement="bottom" v-if="item.securityId && item.securityId.length>0">
                      <div slot="content" style="line-height: 25px">
                        <div v-for="(item2, index2) in item.securityId" :key="index2">
                          {{item2.name}}
                        </div>
                      </div>
                      <el-tag class="-ellipsis">{{item.securityId[0].name}}<span class="num">+{{item.securityId.length}}</span></el-tag>
                    </el-tooltip>
                  </div>
                  <div class="dv4"> <el-button type="text" icon="el-icon-edit" @click="addAuth(item)" round></el-button> </div>
                </div>
              </el-form-item>
              <div class="btns">
                <el-button type="primary" @click="cancel" plain>取消</el-button>
                <el-button type="primary" @click="save()" :loading="loading">保存</el-button>
              </div>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
    <AuthDialog title="关联BU权限" :itemObj="inAreaObj" ref="authDialog" :parkId="parkId"/>
  </div>
</template>
<script>
import { getBuList, saveBuEdit } from "@/api/platform/area/park-set";
import { mapGetters } from "vuex";
import AuthDialog from './components/batchAuth'

export default {
  components: {
    AuthDialog
  },
  data() {
    return {
      loading: false,
      buList:[],
      inAreaObj: {},
    };
  },
  props: {
    parkId: [Number, String]
  },
  created() {
    this.getDetails()
  },
  mounted() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    async save(){
      let arr = []
      this.buList.forEach(element => {
        const obj = {
          compId: element.compId,
          parkId: element.parkId,
          securityId: element.ids,
        }
        arr.push(obj)
      });
      const res = await saveBuEdit(arr)
      if(res.data.code === 0){
        this.$message({
          message: '保存成功',
          type: 'success'
        });
        this.getDetails()
      }
    },
    async getDetails(){
      getBuList(this.parkId).then(response => {
      this.buList = response.data.data
      this.buList.forEach(element => {
        element.ids = []
        if(element.securityId !== null && element.securityId.length > 0){
          element.securityId.forEach(el => {
            element.ids.push(el.id)
          });
        }
      });
    });
    },
    addAuth(item){
      this.inAreaObj = item
      this.$refs.authDialog && this.$refs.authDialog.open()
    },
    cancel() {
      const src = `/platform/area/park`;
      this.$router.push({
        path: src
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 800px;
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
        width: 120px;
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

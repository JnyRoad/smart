<!--园区管理，配置信息，访客设置  -->
<template>
  <div class="content">
    <el-form ref="form" :model="form" class="form">
      <div v-for="(item,index) in form.parkSwitchArray" :key="index" class="item">
        <div>{{item.switchName}}</div>
        <div v-if="item.switchCode ==='visitor_arrive'">
          到访前
          <el-input v-model="item.beforeTime" class="ipt1"></el-input>分钟通知
        </div>
        <div v-else></div>
        <div>
          <el-switch
            v-model="item.isOn"
            active-color="#13ce66"
            inactive-color="#B3B3B3"
            :active-value="1"
            :inactive-value="0"
          ></el-switch>
        </div>
      </div>
      <div class="btns">
        <el-button type="primary" @click="cancel" plain>取消</el-button>
        <el-button type="primary" @click="save('form')" :loading="loading">保存</el-button>
      </div>
    </el-form>
  </div>
</template>
<script>
import { getSwitcheList, saveSwitch } from "@/api/platform/area/park-set";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      loading: false,
      form: { parkSwitchArray: [] }
    };
  },
  props: {
    parkId: [Number, String]
  },
  created() {
    getSwitcheList(this.parkId).then(response => {
      let self = this;
      //console.log("response.data.data.json=========>"+JSON.stringify(response.data.data))
      self.form.parkSwitchArray = response.data.data;
    });
  },
  mounted() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    cancel() {
      const src = `/platform/area/park`;
      this.$router.push({
        path: src
      });
    },
    save() {
      let self = this;
      saveSwitch(self.parkId, self.form.parkSwitchArray).then(response => {
        this.$router.push({
          path: `/platform/area/park`
        });
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.content ::v-deep {
  padding: 0 10%;
  .form {
    width: 900px;
    margin: 0 auto;
  }
  .ipt1 {
    padding: 0 5px;
    width: 50px;
    .el-input__inner {
      padding: 0;
      height: 30px;
      line-height: 30px;
      text-align: center;
    }
  }
  .item {
    padding: 18px 0 18px 20%;
    > div {
      display: inline-block;
      width: 33.33%;
    }
    > div:last-child {
      padding-left: 30px;
    }
  }
  .item:nth-child(2n) {
    background: #f7f7f7;
  }
}
</style>

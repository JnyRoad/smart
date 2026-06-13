<!--园区管理，园区设置  -->
<template>
  <div class="my-basic-container setting">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-tabs v-model="activeName">
          <el-tab-pane label="组织关系" name="setbu">
            <setBu :parkId="parkId"></setBu>
          </el-tab-pane>
          <el-tab-pane label="访客设置" name="setvisitor">
            <setVisitor :parkId="parkId"></setVisitor>
          </el-tab-pane>
          <el-tab-pane label="招聘设置" name="setrecruit">
            <setRecruit :parkId="parkId"></setRecruit>
          </el-tab-pane>
          <el-tab-pane label="通行权限" name="setDeviceAuth">
            <setDeviceAuth :parkId="parkId"></setDeviceAuth>
          </el-tab-pane>
          <el-tab-pane label="BU权限配置" name="setBUAuth">
            <setBuAuth :parkId="parkId"></setBuAuth>
          </el-tab-pane>
        </el-tabs>
        <router-view></router-view>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { fetchList } from "@/api/platform/recruit/emailmodel";
import setBu from "./setBu";
import setVisitor from "./setVisitor";
import setRecruit from "./setRecruit";
import setDeviceAuth from "./setDeviceAuth";
import setBuAuth from "./setBuAuth";
import { mapGetters } from "vuex";
export default {
  name: "emailmodel",
  data() {
    return {
      parkId: "",
      activeName: "setbu",
      tabOption: {
        column: [
          {
            label: "组织关系",
            prop: "setbu"
          },
          {
            label: "访客设置",
            prop: "setvisitor"
          },
          {
            label: "招聘设置",
            prop: "setrecruit"
          },
          {
            label: "通行权限",
            prop: "setDeviceAuth"
          }
        ]
      }
    };
  },
  components: {
    setBu,
    setVisitor,
    setRecruit,
    setDeviceAuth,
    setBuAuth
  },
  created: function() {
    this.parkId = this.$route.query.parkId;
  },
  watch: {},
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {}
};
</script>
<style lang="scss" scoped>
.setting ::v-deep {
  .el-tabs__content {
    padding-top: 20px;
  }
  .btns {
    padding-top: 30px;
    text-align: center;
    .el-button {
      padding: 10px 25px;
      margin: 0 20px;
    }
  }
}
</style>

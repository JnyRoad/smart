<!--招聘管理，面试通知模板 主页  -->
<template>
  <div class="my-basic-container emailmodel">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="面试通知" name="msinform"></el-tab-pane>
          <el-tab-pane label="入职准备通知" name="rzzbinform"></el-tab-pane>
          <el-tab-pane label="入职通知" name="rzinform"></el-tab-pane>
        </el-tabs>
        <router-view></router-view>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { fetchList } from "@/api/platform/recruit/emailmodel";
import { mapGetters } from "vuex";
export default {
  name: "emailmodel",
  data() {
    return {
      activeName: "msinform",
      tabOption: {
        column: [
          {
            label: "面试通知",
            prop: "msinform"
          },
          {
            label: "入职准备通知",
            prop: "rzzbinform"
          },
          {
            label: "入职通知",
            prop: "rzinform"
          }
        ]
      }
    };
  },
  created: function() {
    const src = `/platform/recruit/emailmodel/model/msinform`;
    this.$router.push({
      path: src
    });
  },
  watch: {
    $route() {
      if (this.$route.path == "/platform/recruit/emailmodel") {
        this.activeName = "msinform";
        const src = `/platform/recruit/emailmodel/model/msinform`;
        this.$router.push({
          path: src
        });
      }
    }
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    handleClick(tab, event) {
      if (tab.name == "msinform") {
        const src = `/platform/recruit/emailmodel/model/msinform`;
        this.$router.push({
          path: src,
          query: {}
        });
      } else if (tab.name == "rzzbinform") {
        const src = `/platform/recruit/emailmodel/model/rzzbinform`;
        this.$router.push({
          path: src,
          query: {}
        });
      } else if (tab.name == "rzinform") {
        const src = `/platform/recruit/emailmodel/model/rzinform`;
        this.$router.push({
          path: src,
          query: {}
        });
      }
    },
    getAll() {
      fetchList().then(response => {});
    }
  }
};
</script>
<style lang="scss" scoped></style>

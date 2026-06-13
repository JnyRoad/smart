<!--首页 -->
<template>
  <div class="my-basic-container change_record">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-tabs  v-model="activeName" @tab-click="tabChange">
          <el-tab-pane label="水表记录" name="waterRecord"></el-tab-pane>
          <el-tab-pane label="电表记录" name="electricRecord"></el-tab-pane>
        </el-tabs>
        <waterRecord
          ref="waterRecord"
          v-if="isCheckIn"
        />
        <electricRecord
          ref="electricRecord"
          v-else
        />
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import waterRecord from './components/water_record'
import electricRecord from './components/electric_record'
export default {
  name: "change_record",
  components: {
    waterRecord,
    electricRecord
  },
  data() {
    return {
      activeName: 'waterRecord',
      tabOption: {
        column: [
          {
            label: "水表记录",
            prop: "waterRecord"
          },
          {
            label: "电表记录",
            prop: "electricRecord"
          }
        ]
      },
      isCheckIn: true,
    };
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {},
  created() {
  },
  methods: {
    tabChange(item) {
      if (item.name == "waterRecord") {
        this.initInsider();
      } else {
        this.initOutsider();
      }
    },
    initInsider() {
      this.isCheckIn = true;
    },
    initOutsider() {
      this.isCheckIn = false;
    },
  }
};
</script>

<style lang="scss" scoped>
</style>

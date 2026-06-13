<!--园区服务：调查表管理:详情 -->
<template>
  <div class="my-basic-container result">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu clear">
          <div class="top-right">
            <el-button
              type="primary"
              :loading="exportLoading"
              @click="exportExcel"
              icon="icon-yutong-download"
            >导出excel报表</el-button>
          </div>
        </div>
        <div class="ques-title">
          调查表：{{statisticsData.title}}
          <span
            style="padding-left: 50px"
          >已提交人数：{{statisticsData.totalCount}}</span>
        </div>
        <div class="ques-list">
          <template v-for="(item, index) in statisticsData.questions">
            <div class="ques-item" :key="index">
              <div>问题{{index+1}} {{item.title}}({{item.type|filterType}})</div>
              <div class="chart-panel" :ref="'refChart'+index" :id="'refChart'+index"></div>
            </div>
          </template>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { statisticsApi, exportApi } from "@/api/platform/park_service/paper";
import echarts from "echarts";
import jsFileDownload from "js-file-download";
export default {
  data() {
    return {
      myCharts: {},
      chartsData: [],
      exportLoading: false,
      statisticsData: {} //统计结果
    };
  },
  created() {
    statisticsApi(this.$route.params.id).then(res => {
      this.statisticsData = res.data.data;
      let temp = [];
      this.statisticsData.questions.forEach(el => {
        let temp2 = [];
        el.selects.forEach(el2 => {
          temp2.push({
            name: el2.answer,
            value: el2.num
          });
        });
        temp.push(temp2);
      });
      this.chartsData = temp;
      this.setData();
    });
  },
  mounted() {},
  filters: {
    filterType(val) {
      let obj = {
        0: "单选题",
        1: "多选题"
      };
      return obj[val];
    }
  },
  methods: {
    setData() {
      //this.chartsData = this.tempData

      this.$nextTick(() => {
        this.chartsData.forEach((el, index) => {
          if (!this.myCharts["chart" + index]) {
            let dom = document.getElementById("refChart" + index);
            let ref = this.$refs["refChart" + index];
            this.createChart("chart" + index, dom);
          }
          this.updateData("chart" + index, el);
        });
      });
    },
    exportExcel() {
      var id = this.$route.params.id;
      var title = this.statisticsData.title;
      exportApi(id).then(res => {
        var fileDownload = require("js-file-download");
        fileDownload(res.data, title + "-excel报表.xls");
      });
    },
    updateData(targetName, data) {
      this.myCharts[targetName].setOption({
        backgroundColor: "#f5f7fa",
        title: {
          left: "center",
          top: 20,
          textStyle: {
            color: "#ccc"
          }
        },
        tooltip: {
          trigger: "item",
          formatter: "{b} : {c} ({d}%)"
        },
        series: [
          {
            type: "pie",
            radius: "75%",
            center: ["50%", "50%"],
            color: ["#ed6d00", "#999", "#FE5050", "#1DB7E5"],
            data: data.sort(function(a, b) {
              return a.value - b.value;
            }),
            roseType: "radius",
            label: {
              normal: {
                formatter: ["{d|{d}%}", "{b|{b}}"].join("\n"),
                rich: {
                  c: {
                    color: "rgb(241,246,104)",
                    fontSize: 20,
                    fontWeight: "bold",
                    lineHeight: 5
                  },
                  b: {
                    color: "#666",
                    fontSize: 15,
                    height: 40
                  }
                }
              }
            },
            labelLine: {
              normal: {
                lineStyle: {
                  color: "#666"
                },
                smooth: 0.2,
                length: 10,
                length2: 20
              }
            },
            itemStyle: {
              normal: {
                shadowColor: "rgba(0, 0, 0, 0.3)",
                shadowBlur: 20
              }
            }
          }
        ]
      });
    },
    createChart(targetName, ref) {
      this.myCharts[targetName] = echarts.init(ref);
      this.myCharts[targetName].setOption({
        backgroundColor: "#f5f7fa",
        title: {
          left: "center",
          top: 20,
          textStyle: {
            color: "#ccc"
          }
        },
        tooltip: {
          trigger: "item",
          formatter: "{b} : {c} ({d}%)"
        },
        series: [
          {
            type: "pie",
            radius: "75%",
            center: ["50%", "50%"],
            color: ["#ed6d00", "#999", "#FE5050", "#1DB7E5"],
            data: [].sort(function(a, b) {
              return a.value - b.value;
            }),
            roseType: "radius",
            label: {
              normal: {
                formatter: ["{d|{d}%}", "{b|{b}}"].join("\n"),
                rich: {
                  c: {
                    color: "rgb(241,246,104)",
                    fontSize: 20,
                    fontWeight: "bold",
                    lineHeight: 5
                  },
                  b: {
                    color: "#666",
                    fontSize: 15,
                    height: 40
                  }
                }
              }
            },
            labelLine: {
              normal: {
                lineStyle: {
                  color: "#666"
                },
                smooth: 0.2,
                length: 10,
                length2: 20
              }
            },
            itemStyle: {
              normal: {
                shadowColor: "rgba(0, 0, 0, 0.3)",
                shadowBlur: 20
              }
            }
          }
        ]
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.result {
  .ques-title {
    font-size: 16px;
    margin: 20px 0;
  }
  .ques-item {
    margin-bottom: 20px;
  }
  .chart-panel {
    width: 100%;
    height: 280px;
    margin-top: 10px;
  }
}
</style>

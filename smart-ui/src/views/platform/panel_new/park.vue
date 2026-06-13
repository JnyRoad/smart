<!--可视化面板，人员出入 -->
<template>
  <div class="pn-at">
    <top></top>
    <div class="pn-bt">
      <div class="pn-bt_left">
        <div class="pn-bt_left_top">
          <div class="pn-bt_left_top_l">
            <div class="pn-bt_left_top_l_t">
              <p1-1/>
            </div>
            <div class="pn-bt_left_top_l_m">
              <p1-2/>
            </div>
            <div class="pn-bt_left_top_l_b">
              <p1-3/>
            </div>
          </div>
          <div class="pn-bt_left_top_r" style="padding-top: 30px;">
            <mapItem/>
          </div>
        </div>
        <div class="pn-bt_left_btm">
          <p1-4/>
        </div>
      </div>
      <div class="pn-bt_right">
        <div class="pn-bt_right_top">
          <dormItem/>
        </div>
        <div class="pn-bt_right_btm">
          <carItem/>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
  @use "@/styles/platform/panel_new/normal" as *;
</style>
<style scoped lang="scss">
  @use "@/styles/platform/panel_new/park" as *;
</style>
<script>
import top from './top'
import mapItem from './map'
import dormItem from './dorm-item'
import carItem from './car-item'
// 加载./components 下的所有组件
const context = require.context('./park-component2', true, /\.vue/)
let components = {}
context.keys().forEach(item => {
  components['p1-' + item.split('/')[1]] = context(item).default
})

export default {
  name: "park",
  components: {
    top,
    mapItem,
    carItem,
    dormItem,
    ...components
  },
  data() {
    return {
    };
  },
  created: function() {},
  mounted: function() {},
  computed: {},
  methods: {}
};
</script>

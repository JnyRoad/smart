<!--
- @name 宿舍申请-选择房间
-->
<template>
  <div class="page3">
    <div class="page3-left">
      <div class="item" :class="[fsi == index ? 'selected' : '']" @click="floorClick(item, index)" v-for="(item, index) in floorData" :key="index">
        <div class="item-label">
          {{item.label}}层
        </div>
      </div>
    </div>
    <div class="page3-right">
      <div class="item-title">房间</div>
      <div class="room-item" :class="[rsi == index ? 'selected' : '']" @click="roomClick(item, index)" v-for="(item, index) in roomData" :key="index">
          <div>{{item.roomName}}房</div>
          <div>{{item.roomSexDesc}} {{item.freeBedNum}}/{{item.bedTotal}}</div>
      </div>
    </div>
  </div>
</template>
<script>
import { getConditionData, getRoomData, getBedData } from '@/services/checkIn'

export default {
  components: {},
  data() {
    return {
      dataList: [],
      routeData: {},
      floorData: [],
      floorId: null,
      fsi: 0,
      rsi: null,
      roomData: [],
      roomId: null,
      roomName: null,
      bedData: [],
      bedName: null,
      bedId: null
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    async getConditionData() {
      const _this = this
      const obj = {
        parkId: _this.routeData.parkId,
        dormitoryId: _this.routeData.dormitoryId,
        roomType: _this.routeData.roomType
      }
      _this.$loading.show()
      const res = await getConditionData(obj)
      _this.$loading.hide()
      if (res.code === 0 && res.data) {
        if (res.data[0].children[0].children) {
          _this.floorData = res.data[0].children[0].children
          _this.fsi = 0
          _this.floorId = _this.floorData[0].id
          _this.getRoomData(_this.floorData[0].id)
          // _this.roomData = _this.floorData[0].children
        } else {
          this.$tceMobile.toast('无房间信息！')
          this.$router.push({
            path: '/xuchang/checkIn'
          })
        }
      } else {
        this.$tceMobile.toast('获取房间信息失败！')
        this.$router.push({
          path: '/xuchang/checkIn'
        })
      }
    },
    floorClick(item, index) {
      const me = this
      me.fsi = index
      me.rsi = null
      me.floorId = item.id
      me.roomId = null
      me.roomName = null
      me.getRoomData(item.id)
    },
    async getRoomData(floorId) {
      const me = this
      const obj = {
        parkId: me.routeData.parkId,
        dormitoryId: me.routeData.dormitoryId,
        roomType: me.routeData.roomType,
        floorId: floorId
      }
      me.$loading.show()
      const res = await getRoomData(obj)
      me.$loading.hide()
      if (res.code === 0 && res.data) {
        me.roomData = res.data
        if (me.roomData.length > 0) {
          me.roomData.forEach(element => {
            switch (element.roomSex) {
              case 0:
                element['roomSexDesc'] = '男'
                break
              case 1:
                element['roomSexDesc'] = '女'
                break
            }
          })
        }
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    async getBedData() {
      const me = this
      me.$loading.show()
      const res = await getBedData(me.roomId)
      me.$loading.hide()
      if (res.code === 0 && res.data) {
        me.bedData = res.data
        if (me.bedData.length > 0) {
          let i = me.bedData.length
          while (i--) {
            if (me.bedData[i].staffBadge !== null || me.bedData[i].delFlag === 1) {
              me.bedData.splice(i, 1)
            }
          }
          me.bedData.forEach(element => {
            element['text'] = element.bedNumber + '床'
            element['value'] = element.id
          })
          me.showPicker(me.bedData)
        } else {
          this.$tceMobile.toast('无可选用的床位！')
        }
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    roomClick(item, index) {
      const me = this
      me.rsi = index
      me.roomId = item.roomId
      me.roomName = item.roomName
      if (item.freeBedNum > 0) {
        me.getBedData()
      } else {
        this.$tceMobile.toast('该房间无可选用的床位！')
      }
    },
    showPicker(d) {
      const me = this
      me.picker = me.$createPicker({
        title: '选择床位',
        data: [d],
        onSelect: me.selectHandle
        // onCancel: me.cancelHandle
      })
      me.picker.show()
    },
    selectHandle(selectedVal, selectedIndex, selectedText) {
      // console.log(selectedVal, selectedText)
      const me = this
      me.bedName = selectedText[0]
      me.bedId = selectedVal[0]
      me.apply()
    },
    apply() {
      const me = this
      let obj = JSON.parse(localStorage.getItem('roomInfo'))
      if (me.roomId !== null) {
        obj['floorId'] = me.floorId
        obj['roomData'] = {
          label: me.roomName,
          value: me.roomId
        }
        obj['bedData'] = {
          label: me.bedName,
          value: me.bedId
        }
        localStorage.setItem('roomInfo', JSON.stringify(obj))
        this.$router.push({
          path: '/xuchang/checkIn'
        })
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.routeData = this.$route.query
    this.getConditionData()
  },
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {}
}
</script>

<style lang="scss" scoped>
.page3 {
  background: $TCE-Background-Grey;
  width: 100%;
  height: 100%;
  padding: rem(20) rem(20) rem(80);
  .page3-left{
    width: 20%;
    height: 100%;
    float: left;
    border-right:1px solid #ccc;
    padding-right: rem(10);
    .item {
      background: #fff;
      border-radius: 5px;
      padding: rem(30) rem(30) rem(20);
      margin-bottom: rem(20);
      .item-label{
        text-align: center;
      }
    }
    .selected{
      background-color: #666;
      color: #fff;
    }
  }
  .page3-right{
    width: 80%;
    height: 100%;
    float: right;
    .item-title{
      height: rem(45);
      line-height: rem(45);
      text-align: center;
    }
    .room-item{
      width: 30%;
      float: left;
      text-align: center;
      background: #fff;
      border-radius: 5px;
      padding: rem(30) rem(30) rem(20);
      margin: rem(20) 0 0 2%;
    }
    .selected{
      background-color: #0dbc82;
      color: #fff;
    }
  }
  .page3-bottom {
    background: $TCE-Background-Grey;
    box-shadow: none;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .btn-inner {
    width: 90%;
    // padding: 0 rem(52);
    border-radius: rem(50);
    background: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .tce-icons {
    vertical-align: middle;
    margin: -3px rem(10px) 0 0;
  }
  .item-info_label {
    min-width: 70px;
  }
}
</style>

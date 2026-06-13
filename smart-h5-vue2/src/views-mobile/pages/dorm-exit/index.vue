<!--
- @name 退宿申请
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="0" @doApply="doApply" @checkList="checkList"></page3Tab>
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item
          required
          field="roomInfo"
          type="picker"
          :formOption="{
            opt: rooms
          }"
          label="房间信息"
          placeholder="请选择"
          @pickerSelectHandle="roomChange"
        ></tce-form-item>
        <div class="roomLists" v-if="roomList.length > 0">
          <div class="roomList">
            <div class="roomLi" v-for="(item, index) in roomList" :key="index">
              {{ item.roomName }}
              <span class="list-delete">
                <i class="cubeic-delete" @click="deleteRoomList(index)"></i>
              </span>
            </div>
          </div>
        </div>
        <tce-form-item
          required
          field="quitReason"
          type="picker"
          :formOption="{
            opt: quitReasonList
          }"
          label="退宿原因"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item required requiredMessage="请选择预计离开日期" field="applyLeaveTime" type="time-picker" label="预计离开日期" placeholder="请输入"></tce-form-item>
        <tce-form-item field="remark" requiredMessage="请输入" type="textarea" label="备注" placeholder="请输入"></tce-form-item>
      </tce-form-group>

      <tce-form-group>
        <tce-form-item field="imgs" type="upload-image" label="上传物品照片" placeholder="请输入"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round">申请</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Tab from '@/views-mobile/components/page3-tab'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { saveDormExit } from '@/services/dormExit'
import { getRoomDetail } from '@/services/goodRreleaseLive'
import store from '@/store'

import { REPAIRTYPEOPTION } from './const'
export default {
  components: {
    page3Tab,
    page3Bottom
  },
  data() {
    return {
      quitReasonList: REPAIRTYPEOPTION,
      tabList: [
        {
          label: '发起提交',
          value: 0
        },
        {
          label: '查看数据',
          value: 1
        }
      ],
      baseInfo: {},
      parkInfo: {},
      rooms: [],
      roomList: []
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    async apply() {
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData
      const dormitoryIds = []
      const roomIds = []
      const imgArry = []
      if (this.roomList.length === 0) {
        this.$tceMobile.toast('请选择退宿房间！')
        return
      }
      if (formData.imgs && formData.imgs.length > 0) {
        formData.imgs.forEach((ele) => {
          imgArry.push(ele.photoId)
        })
      }
      this.roomList.forEach((ele) => {
        dormitoryIds.push(ele.roomId.split('/')[0])
        roomIds.push(ele.roomId.split('/')[1])
      })
      const obj = {
        dormitoryIds: dormitoryIds,
        roomIds: roomIds,
        quitReason: formData.quitReason.value,
        applyLeaveTime: formData.applyLeaveTime + ':00',
        remark: formData.remark,
        imgs: imgArry,
        parkId: this.parkInfo.id,
        badge: this.baseInfo.employeeBadge,
        name: this.baseInfo.employeeName
      }
      this.$loading.show()
      const res = await saveDormExit(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.$tceMobile.toast('申请成功')
        this.checkList()
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    async getRoomDetail() {
      const res = await getRoomDetail(this.baseInfo.employeeBadge)

      if (res.code === 0) {
        let arr = []
        if (res.data.data && res.data.data.length > 0) {
          res.data.data.forEach((el) => {
            arr.push({
              value: `${el.dormitoryId}/${el.roomId}`,
              label: `${el.dormitoryName}/${el.roomName}号房`
            })
          })
          this.rooms = arr
        } else {
          this.$tceMobile.toast('没有查询到您的房间信息，不能进行退宿申请申请')
        }
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    roomChange(obj) {
      // 选择房间
      const r = this.roomList
      if (obj.selectedVal[0] !== null) {
        if (this.roomList.length > 0) {
          const l = r.filter((item) => item.roomId === obj.selectedVal[0])
          if (l.length === 0) {
            this.roomList.push({
              roomName: obj.selectedText[0],
              roomId: obj.selectedVal[0]
            })
          }
        } else {
          this.roomList.push({
            roomName: obj.selectedText[0],
            roomId: obj.selectedVal[0]
          })
        }
      }
    },
    deleteRoomList(i) {
      this.roomList.splice(i, 1)
    },
    /**
     * 发起提交
     */
    async doApply() {
      this.$router.push({
        path: '/xuchang/dormRepairs'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/dormExit/list'
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
    this.getRoomDetail()
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
  min-height: 100%;
  padding: rem(120) rem(20) rem(190);
  .business-form-group {
    margin-bottom: rem(20);
  }
  .btn-inner {
    width: 100%;
    height: 100%;
    padding: 0 rem(52);
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .roomLists {
    background: #fff;
    width: 100%;
    .roomList {
      transform-origin: 0 0;
      border-bottom: 1px solid #eeeeee;
      margin-left: 10px;
      .roomLi {
        height: 40px;
        line-height: 40px;
        margin-left: 40px;
        i {
          float: right;
          font-size: 16px;
          color: red;
          width: 60px;
        }
      }
    }
  }
}
</style>

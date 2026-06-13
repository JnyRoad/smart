<!--
- @name 宿舍申请-发起提交
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="0" @doApply="doApply" @checkList="checkList"></page3Tab>
    <tce-form ref="tceForm">
      <tce-form-group>
        <div class="form-block">
          <div class="form-row">
            <div class="label required">
              分配方式
            </div>
            <div class="value">
              <cube-button class="form-btn" :primary="!isAuto" :inline="true" @click="isAuto = false">自选房间</cube-button>
              <cube-button class="form-btn" :primary="isAuto" :inline="true" @click="isAuto = true">系统分配</cube-button>
            </div>
          </div>
        </div>
        <tce-form-item
          required
          field="buildData"
          type="picker"
          :valueData="roomInfo.buildData"
          :formOption="{
            opt: builds
          }"
          label="楼栋信息"
          placeholder="请选择"
          @pickerSelectHandle="buildChange"
        ></tce-form-item>
        <tce-form-item
          required
          field="roomType"
          type="picker"
          :valueData="roomInfo.roomType"
          :formOption="{
            opt: roomTypes
          }"
          label="房间类型"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item  type="item-holder" label="房间号" placeholder="请选择" v-if="!isAuto">
          <template slot="itemholder">
            <div @click="openAddRoom" class="personOuter">
              <div style="text-align: right;" v-if="roomInfo.roomData">{{roomInfo.roomData.label}}</div>
              <div v-else>
                <div style="text-align: right; color: #999">请选择</div>
                <span class="-arrow"></span>
              </div>
            </div>
          </template>
        </tce-form-item>
        <tce-form-item  type="item-holder" label="床位" placeholder="请选择" v-if="!isAuto">
          <template slot="itemholder">
            <div @click="openAddRoom" class="personOuter">
              <div style="text-align: right;" v-if="roomInfo.bedData">{{roomInfo.bedData.label}}</div>
              <div v-else>
                <div style="text-align: right; color: #999">请选择</div>
                <span class="-arrow"></span>
              </div>
            </div>
          </template>
        </tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom v-if="isApply">
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round">申请</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Tab from '@/views-mobile/components/page3-tab'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { getDormitoryDetail, getRoomType, getUserData, autoallot } from '@/services/checkIn'
import store from '@/store'

export default {
  components: {
    page3Tab,
    page3Bottom
  },
  data() {
    return {
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
      builds: [],
      roomTypes: [],
      roomInfo: {},
      baseInfo: {},
      parkInfo: {},
      isAuto: false,
      userData: {},
      isApply: true,
      applyObj: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    async apply() {
      await this.$refs.tceForm.verification()
      // let formData = this.$refs.tceForm.formData
      this.applyObj = {
        name: this.userData.name,
        sex: this.userData.sex,
        nation: this.userData.nation,
        certno: this.userData.certno,
        birthday: this.userData.birth,
        address: this.userData.homeAddress,
        signOrg: null,
        validDateStart: this.userData.validDate,
        validDateEnd: this.userData.validDateFm,
        dormitoryId: this.roomInfo.buildData.value,
        roomType: this.roomInfo.roomType.value,
        badge: this.baseInfo.employeeBadge,
        parkId: this.parkInfo.id
      }
      if (this.isAuto) {
        this.autoallot(this.applyObj)
      } else {
        this.manual()
      }
    },
    manual() { // 手动分配
      if (this.roomInfo.roomData) {
        const obj = this.applyObj
        obj['floorId'] = this.roomInfo.floorId
        obj['roomId'] = this.roomInfo.roomData.value
        obj['bedId'] = this.roomInfo.bedData.value
        this.autoallot(obj)
      } else {
        this.$tceMobile.toast('请选择房间号')
      }
    },
    async autoallot(obj) {
      const me = this
      me.$loading.show()
      const res = await autoallot(obj)
      me.$loading.hide()
      if (res.code === 0) {
        // this.$tceMobile.toast('申请成功')
        this.checkList()
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    async getUserData() { // 获取用户信息
      const me = this
      const obj = {
        badge: me.baseInfo.employeeBadge
      }
      const res = await getUserData(obj)
      if (res.code === 0) {
        me.userData = res.data
      } else {
        me.isApply = false
        me.$tceMobile.toast('获取用户信息失败！')
      }
    },
    /**
     * 发起提交
     */
    async doApply() {
      this.$router.push({
        path: '/xuchang/checkIn'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/checkIn/detail'
      })
    },
    async openAddRoom() {
      this.localStorageSave()
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData
      this.$router.push({
        path: '/xuchang/checkIn/selectRoom',
        query: {
          parkId: this.parkInfo.id,
          dormitoryId: formData.buildData.value,
          roomType: formData.roomType.value
        }
      })
    },
    buildChange(obj) {
      let formData = this.$refs.tceForm.formData
      this.getRoomType(obj.selectedVal[0])
      formData.roomType = {
        label: null,
        value: null
      }
      this.roomInfo = formData
      this.clearStorage()
    },
    async getDormitoryDetail() {
      const obj = {
        parkId: this.parkInfo.id,
        isAccount: true
      }
      const res = await getDormitoryDetail(obj)
      if (res.code === 0) {
        let arr = []
        const d = res.data
        d.forEach(element => {
          arr.push({
            label: element.dormitoryName,
            value: element.id
          })
        })
        this.builds = arr
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    async getRoomType(dormitoryId) {
      const obj = {
        parkId: this.parkInfo.id,
        dormitoryId: dormitoryId
      }
      const res = await getRoomType(obj)
      if (res.code === 0) {
        let arr = []
        const d = res.data
        d.forEach(element => {
          arr.push({
            label: element.typeName,
            value: element.id
          })
        })
        this.roomTypes = arr
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    localStorageSave() {
      let formData = this.$refs.tceForm.formData
      this.roomInfo = formData
      localStorage.setItem('roomInfo', JSON.stringify(this.roomInfo))
    },
    storageLoad() {
      let obj = JSON.parse(localStorage.getItem('roomInfo'))
      if (obj) {
        this.roomInfo = obj
        this.getRoomType(this.roomInfo.buildData.value)
      }
    },
    clearStorage() {
      localStorage.setItem('roomInfo', null)
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
    this.getDormitoryDetail()
    this.getUserData()
    this.storageLoad()
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
  .page3{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(120) rem(20) rem(190);
    .business-form-group{
      margin-bottom: rem(20);
    }
    .btn-inner{
      width: 100%;
      height: 100%;
      padding: 0 rem(52);
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .personOuter{
      position: relative;
      padding: rem(30) rem(60) rem(10) rem(30);
      .-arrow{
        position: absolute;
        top: rem(44);
        right: rem(30);
      }
    }
    .form-block{
      background: #fff;
      height: 45px;
      line-height: 45px;
      padding-left: 10px;
      .form-row{
        display: flex;
        height: 100%;
        border-bottom: 1px solid #eeeeee;
        .label{
          flex: none;
          color: #666666;
        }
        .required::before {
            content: '*';
            color: red;
            opacity: 1;
            display: inline-block;
            vertical-align: middle;
        }
        .value{
          flex: 1;
          padding-left: rem(30);
          .form-btn{
            height: 75%;
            width: 40%;
            margin: 0 4%;
          }
        }
      }
    }
  }
</style>

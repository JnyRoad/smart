<template>
  <div class="block1 block2 room-grid-panel">
    <div
      v-if="hasData"
      class="box-outer">
      <div style="margin-bottom: 20px;">
        <el-checkbox
          :indeterminate="isIndeterminate"
          :value="checkAll"
          @input="emitCheckAllInput"
          @change="emitCheckAllChange">全选</el-checkbox>
        <div class="tps">
          <span class="tp tp1"><i/>男宿</span>
          <span class="tp tp2"><i/>女宿</span>
          <span class="tp tp3"><i/>夫妻/混住</span>
          <span class="tp tp4"><i class="lock"/>不参与分配</span>
        </div>
      </div>
      <div style="margin: 15px 0"/>
      <el-checkbox-group
        :value="checkedRoom"
        class="room-list"
        @input="emitCheckedRoomInput"
        @change="emitRoomChange">
        <el-checkbox
          v-for="(item, index) in tableData"
          :key="index"
          :label="item.id">
          <i
            v-if="item.isDormitoryRoom===1"
            class="lock"/>
          <div
            :class="item.roomSex | f_roomGenderClass"
            class="room-item">
            <div>{{ item.roomName }}</div>
            <div>{{ item.bedTotal }}人间</div>
          </div>
          <el-dropdown
            placement="bottom"
            class="dropdown">
            <span class="el-dropdown-link">
              <i class="el-icon-more"/>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="emitEditRoom(item)">编辑房间</el-dropdown-item>
              <el-dropdown-item @click.native="emitDeleteRoom(item)">删除房间</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-checkbox>
      </el-checkbox-group>
    </div>
    <div
      v-else
      class="noData">当前条件下暂无住宿信息（请选择具体楼层）</div>
  </div>
</template>

<script>
import { roomGenderClass } from '../room-rules'

export default {
  name: 'RoomGridPanel',
  filters: {
    f_roomGenderClass(val) {
      return roomGenderClass(val)
    }
  },
  props: {
    hasData: {
      type: Boolean,
      required: true
    },
    tableData: {
      type: Array,
      required: true
    },
    checkedRoom: {
      type: Array,
      required: true
    },
    checkAll: {
      type: Boolean,
      required: true
    },
    isIndeterminate: {
      type: Boolean,
      required: true
    }
  },
  methods: {
    emitCheckAllInput(value) {
      this.$emit('update-check-all', value)
    },
    emitCheckAllChange(value) {
      this.$emit('check-all-change', value)
    },
    emitCheckedRoomInput(value) {
      this.$emit('update-checked-room', value)
    },
    emitRoomChange(value) {
      this.$emit('room-change', value)
    },
    emitEditRoom(row) {
      this.$emit('edit-room', row)
    },
    emitDeleteRoom(row) {
      this.$emit('delete-room', row)
    }
  }
}
</script>

<style lang="scss" scoped>
.room-grid-panel ::v-deep {
  $c_man: #70a9ff;
  $c_woman: #ff98c4;
  $c_mix: #c4a7ff;
  .tps{
    display: inline-block;
    margin-left: 60px;
    .tp{
      margin-right: 20px;
      i{
        width: 10px;
        height: 10px;
        display: inline-block;
        vertical-align: middle;
        margin: -2px 4px 0 0;
      }
    }
    .tp1{
      i{
        background-color: $c_man;
      }
    }
    .tp2{
      i{
        background-color: $c_woman;
      }
    }
    .tp3{
      i{
        background-color: $c_mix;
      }
    }
  }
  .noData {
    color: #999;
    text-align: center;
    padding-top: 100px;
  }
  .lock {
    width: 0px;
    height: 0px;
    border: 7px solid red;
    border-top-color: transparent;
    border-left-color: transparent;
    display: inline-block;
    border-radius: 3px;
  }
  .room-list {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    .el-checkbox + .el-checkbox {
      margin-left: 0;
    }
    .el-checkbox {
      position: relative;
      margin: 0 20px 20px 0;
      .el-checkbox__label {
        padding-left: 0;
      }
      .el-checkbox__input {
        position: absolute;
        left: 5px;
        top: 5px;
      }
    }
    .dropdown {
      position: absolute;
      top: 3px;
      right: 6px;
      .el-icon-more {
        color: #fff;
      }
    }
    .lock {
      position: absolute;
      bottom: 0.7px;
      right: 0.5px;
    }
    .room-item {
      width: 130px;
      height: 60px;
      border-radius: 3px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 15px;
      background: #e0e0e0;
      color: #fff;
      > div {
        margin-top: 14px;
      }
      .room-num {
        text-align: left;
      }
      .room-type {
        text-align: right;
      }
    }
    .man {
      background: $c_man;
    }
    .woman {
      background: $c_woman;
    }
    .mix {
      background: $c_mix;
    }
  }
}
</style>

<template>
  <el-dialog ref="dialog" :visible.sync="currVisible" width="500px" :show-close="false" @open="open" @close="close" :append-to-body="true" :custom-class="'my-dialog-'">
    <el-form :rules="rules" ref="form" class="form" :model="addform">
      <div class="ft-danger">*请直接将员工工号粘贴到下面框里，每个一行</div>
      <el-form-item label="" prop="badges">
        <el-input type="textarea" class="staffs" v-model="addform.badges" placeholder="请输入" clearable></el-input>
      </el-form-item>
      <el-form-item label="权限有效期" prop="dateRange">
        <el-date-picker
          v-model="addform.dateRange"
          :clearable="false"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期" />
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { batchAdd } from '@/api/platform/area/limit'
import { buildPermissionDatePayload, getDefaultPermissionDateRange } from '@/util/permission-validity'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      addform: {
        badges: '',
        dateRange: getDefaultPermissionDateRange()
      },
      rules: {
        badges: [tce.helper.formRules.vempty()]
      },
      addData: {}
    }
  },
  props: {
    visible: Boolean,
    dataItem: Object
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    },
    dataItem: {
      handler: function (val) {
        if (val) {
          this.addData = val
        }
      },
      immediate: true
    }
  },
  methods: {
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    /**
     * 代理保存按钮的提交，保留异步结果以避免重复点击。
     */
    async formSumit() {
      return this.addSubmit()
    },
    /**
     * 添加权限组人员，并在远端调用前校验日期范围。
     */
    async addSubmit() {
      const valid = await this.validateForm().catch(() => false)
      if (!valid) {
        return
      }
      let validityPayload
      try {
        validityPayload = buildPermissionDatePayload(this.addform.dateRange)
      } catch (error) {
        this.$message.warning(error.message)
        return
      }
      this.btnLoading = true
      let arr = this.addform.badges.split(/[\s\n,]/)
      let arr2 = arr.filter((el) => {
        return el != ''
      })
      // this.$emit('refresh', arr2)
      const obj = {
        authId: this.addData.authId,
        type: this.addData.type,
        badges: arr2,
        ...validityPayload
      }
      try {
        const res = await batchAdd(obj)
        if (res.data.code === 0 && res.data.data.length === 0) {
          this.$emit('refresh')
          this.close()
          this.$notify({
            title: '成功',
            message: '添加成功',
            type: 'success',
            duration: 2000
          })
        } else {
          const d = res.data.data
          const msg = '不符合条件员工：' + d.join(',')
          this.$notify({
            title: '失败',
            message: msg,
            type: 'success',
            duration: 5000
          })
        }
      } catch (error) {
        this.$notify({
          title: '失败',
          message: '添加失败，请稍后重试',
          type: 'error',
          duration: 5000
        })
      } finally {
        // 无论远端调用成功或失败，均恢复保存按钮，允许用户修正后重试。
        this.btnLoading = false
      }
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {
        badges: '',
        dateRange: getDefaultPermissionDateRange()
      }
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {
        badges: '',
        dateRange: getDefaultPermissionDateRange()
      }
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
.form {
  margin-bottom: 40px;
}
.staffs ::v-deep {
  margin-top: 20px;
  textarea {
    min-height: 150px !important;
  }
}
</style>

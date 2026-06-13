<template>
  <div class="" >
    <el-form-item label="统一社会信用代码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.creditCode"></el-input>
    </el-form-item>
    <el-form-item label="组织机构代码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.organizationCode"></el-input>
    </el-form-item>
    <el-form-item label="税务登记证号">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.taxCode"></el-input>
    </el-form-item>
    <el-form-item label="社保登记号">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.socialCode"></el-input>
    </el-form-item>
    <el-form-item label="统计证证号">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.statisticalCode"></el-input>
    </el-form-item>
    <el-form-item label="名称">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.name"></el-input>
    </el-form-item>
    <el-form-item label="类型">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.type"></el-input>
    </el-form-item>
    <el-form-item label="住所">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.address"></el-input>
    </el-form-item>
    <el-form-item label="法定代表人">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.legalPerson"></el-input>
    </el-form-item>
    <el-form-item label="组成形式">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.formation"></el-input>
    </el-form-item>
    <el-form-item label="注册资本">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.money"></el-input>
    </el-form-item>
    <el-form-item label="成立日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.createTime"></el-input>
    </el-form-item>
    <el-form-item label="营业期限">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.operatingPeriod"></el-input>
    </el-form-item>
    <el-form-item label="经营范围">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.Scope"></el-input>
    </el-form-item>
    <el-form-item label="登记机关">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.authority"></el-input>
    </el-form-item>
    <el-form-item label="登记日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.registrationDate"></el-input>
    </el-form-item>
    <el-form-item label="二维码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.qrCode"></el-input>
    </el-form-item>

  </div>
</template>

<script>

  export default {
    name: "Ocr",
    data: function () {
      return {
      };
    },
    props: {
      data:Object
    },
    methods: {

    },
    created() {

    },

    mounted() {
    }
  };
</script>

<style scoped>

</style>

<template>
  <div class="" >
    <el-form-item label="姓名">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.name"></el-input>
    </el-form-item>
    <el-form-item label="性别">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.sex"></el-input>
    </el-form-item>
    <el-form-item label="证号">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.idNum"></el-input>
    </el-form-item>
    <el-form-item label="出生日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.birth"></el-input>
    </el-form-item>
    <el-form-item label="住址">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.address"></el-input>
    </el-form-item>
    <el-form-item label="准驾车型">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.carType"></el-input>
    </el-form-item>
    <el-form-item label="初始领证日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.firstDate"></el-input>
    </el-form-item>
    <el-form-item label="有效起始日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.useDate"></el-input>
    </el-form-item>
    <el-form-item label="有效期限">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.expiryDate"></el-input>
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

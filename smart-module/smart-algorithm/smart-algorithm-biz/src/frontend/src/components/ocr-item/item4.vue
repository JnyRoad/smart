<template>
  <div class="" >
    <el-form-item label="姓名">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.name"></el-input>
    </el-form-item>
    <el-form-item label="性别">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.sex"></el-input>
    </el-form-item>
    <el-form-item label="公民身份号码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.idNum"></el-input>
    </el-form-item>
    <el-form-item label="名族">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.nation"></el-input>
    </el-form-item>
    <el-form-item label="出生">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.birth"></el-input>
    </el-form-item>
    <el-form-item label="住址">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.address"></el-input>
    </el-form-item>
    <el-form-item label="签发机关">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.issueAuthority"></el-input>
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

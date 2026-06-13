<template>
  <div class="" >
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

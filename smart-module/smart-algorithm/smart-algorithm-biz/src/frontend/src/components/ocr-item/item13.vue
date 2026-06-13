<template>
  <div class="" >
    <el-form-item label="护照号码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.id"></el-input>
    </el-form-item>
    <el-form-item label="本国姓名">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.name"></el-input>
    </el-form-item>
    <el-form-item label="英文姓名">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.englishName"></el-input>
    </el-form-item>
    <el-form-item label="性别">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.sex"></el-input>
    </el-form-item>
    <el-form-item label="出生日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.birthDate"></el-input>
    </el-form-item>
    <el-form-item label="持证人国籍代码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.nationalityCode"></el-input>
    </el-form-item>
    <el-form-item label="出生地点">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.birthAddress"></el-input>
    </el-form-item>
    <el-form-item label="签发国代码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.issueCountry"></el-input>
    </el-form-item>
    <el-form-item label="签发地点">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.issueAddress"></el-input>
    </el-form-item>
    <el-form-item label="签发日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.issueDate"></el-input>
    </el-form-item>
    <el-form-item label="MR1码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.mrz1"></el-input>
    </el-form-item>
    <el-form-item label="MR2码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.mrz2"></el-input>
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

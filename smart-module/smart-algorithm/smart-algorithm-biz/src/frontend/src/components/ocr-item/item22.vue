<template>
  <div class="" >
    <el-form-item label="证件号码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.id"></el-input>
    </el-form-item>
    <el-form-item label="中文姓名">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.name"></el-input>
    </el-form-item>
    <el-form-item label="英文姓名">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.englishName"></el-input>
    </el-form-item>
    <el-form-item label="其他姓名">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.otherName"></el-input>
    </el-form-item>
    <el-form-item label="性别">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.sex"></el-input>
    </el-form-item>
    <el-form-item label="出生日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.birthDate"></el-input>
    </el-form-item>
    <el-form-item label="本证有效期至">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.expire"></el-input>
    </el-form-item>
    <el-form-item label="港澳证件号码">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.hkmID"></el-input>
    </el-form-item>
    <el-form-item label="签发机关">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.issueOffice"></el-input>
    </el-form-item>
    <el-form-item label="签发日期">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.issueDate"></el-input>
    </el-form-item>
    <el-form-item label="换证次数">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.changeTime"></el-input>
    </el-form-item>
    <el-form-item label="归属地">
      <el-input type="text" :show-word-limit="true" :readonly="true" v-model="data.belongArea"></el-input>
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

<template>
  <el-row>
    <el-col :span="5">
      <div><h1>供应链金融应用</h1></div>
      <el-button
        type="text"
        size="medium"
        style="
          position: absolute;
          right: 10%;
          bottom: 12px;
          font-size: 20px;
          color: #fff;
        "
        @click="logout"
        >登出</el-button
      >
    </el-col>
  </el-row>
</template>
 
 <script>
export default {
  name: "Header",
  methods: {
    logout: function () {
      this.$cookies.remove("orgType");
      this.$cookies.remove("address");
      this.$router.push("/login");
    },
  },
  mounted: function () {
    if (this.$cookies.get("address") == null) {
      alert("请先登录");
      this.$router.push("/login");
    }
  },
};
</script>

<style scoped>
</style>
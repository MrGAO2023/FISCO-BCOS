<template>
  <el-row class="tac">
    <el-col :span="24">
    <el-menu
        class="el-menu-vertical-demo"
        style="height: 100%;" @select="handleSelect">
      <el-menu-item index="1"><h2>我的票据</h2></el-menu-item>
      <el-menu-item index="2"><h2>公司列表</h2></el-menu-item>
      <el-menu-item index="3"><h2>银行列表</h2></el-menu-item>
      <el-menu-item index="4"><h2>账户信息</h2></el-menu-item>
    </el-menu>
    </el-col>
  </el-row>
</template>
 
<script>
export default {
  name: "Navigator",
  methods: {
    handleSelect(key, keyPath) {
      console.log(key, keyPath)
      if (key == '1') {
        this.$router.push('/home')
      }
      if (key == '2') {
        this.$router.push('/company')
      }
      if (key == '3') {
        this.$router.push('/bank')
      }
      if (key == '4') {
        this.$router.push('/individual')
      }

    },
  }
}
</script>

<style>

</style>
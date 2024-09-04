<template>
  <el-row style="height: 100%;">
    <el-col :span="8" :offset="8" style="border-style: solid;border-color: #e6e6e6;margin-top: 5%">
      <el-row >
        <el-col :span="16" :offset="4">
          <el-form label-width="80px">
            <h1>供应链金融应用</h1>
            <h3>登录界面</h3>
            <el-form-item label="用户地址:">
              <el-input type="primary" v-model="address"></el-input>
            </el-form-item>
            <el-form-item label="组织类型:">
              <el-radio-group v-model="orgType">
              <el-radio :label="1">公司</el-radio>
              <el-radio :label="2">银行</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-col>

      </el-row>
      <el-row style="margin-bottom: 20px">
        <el-button type="primary" @click="login">登录</el-button>
        <el-button type="primary" @click="register">注册</el-button>
      </el-row>
    </el-col>
  </el-row>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      orgType: 1,
      address:''
      }
  },
  methods: {
    login: function () {
      if (this.address == "") {
        alert("区块链地址不能为空！")
      }else if (this.orgType == "") {
        alert("用户类型不能为空！")
      }else {
        let postData = {
          orgType: this.orgType,
          address: this.address
        }
        this.axios.post('/finance/org/login', postData).then((response) => {
          if (response.data.code == 200) {
            this.$cookies.set('orgType', this.orgType)
            this.$cookies.set('address', this.address)
            // alert('登录成功')
            this.$router.push('/home')
          }else {
            alert(`登录失败, ${response.data.data}`)
          }
        })
      }


    },
    register: function () {
      this.$router.push('/register')
    }
  }
}
</script>

<style scoped>

</style>
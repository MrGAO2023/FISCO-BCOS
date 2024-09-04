<template>
  <el-row style="height: 100%;">
    <el-col :span="8" :offset="8" style="border-style: solid;border-color: #e6e6e6;margin-top: 5%">
      <el-row>
        <el-col :span="16" :offset="4">
          <el-form label-width="100px">
            <h3>注册界面</h3>
            <el-form-item label="组织名称:">
              <el-input type="primary" v-model="username"></el-input>
            </el-form-item>
            <el-form-item label="区块链地址:">
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
      <el-row style="padding-bottom:20px">
        <el-button type="primary" @click="register">注册</el-button>
        <el-button type="primary" @click="goback">返回</el-button>
      </el-row>
    </el-col>

  </el-row>

</template>

<script>
export default {
  name: "Register",
  data() {
    return {
      orgType: 1,
      username: '',
      address:''
    }
  },
  methods: {
    register: function () {
      if (this.address == "") {
        alert("区块链地址不能为空！")
      }else {
        let postData = {
          orgType: this.orgType,
          username: this.username,
          address:this.address
        }
        this.axios.post('/finance/org/register', postData).then((response) => {
          if (response.data.code == 200) {
            alert('注册成功')
            this.$router.push('/login')
          }else {
            alert(`注册失败, ${response.data.data}`)
          }
        })
      }
    },
    goback: function () {
      this.orgType = ''
      this.username =  ''
      this.address = ''
      this.$router.push('/login')
    }

  }
}
</script>

<style scoped>

</style>
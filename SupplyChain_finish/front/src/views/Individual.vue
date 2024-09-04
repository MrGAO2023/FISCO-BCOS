<template>
  <div id="app">
    <el-container>
      <el-header class="header">
        <Header />
      </el-header>
      <el-container>
        <el-aside width="200px">
          <navigator></navigator>
        </el-aside>
        <el-main style="padding-top: 50px">
          <el-row>
            <el-col :span="12" :offset="6">
              <el-row>
                账户类型：{{orgType==1?'公司':'银行'}}
              </el-row>
              <el-row style="padding-top: 10px">
                账户地址：{{ address }}
              </el-row>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Navigator from "@/components/Navigator";
import Header from "@/components/Header";

export default {
  name: "Individual",
  data() {
    return {
      dialogVisible: false,
      orgType: "",
      address: "",
    };
  },
  components: {
    Navigator,
    Header,
  },
  methods: {
    logout: function () {
      this.$router.push("/login");
    },
    showDialog: function () {
      this.dialogVisible = true;
    },
  },
  mounted:function(){
    this.orgType = this.$cookies.get("orgType");
    this.address = this.$cookies.get("address");
  },
};
</script>

<style scoped>
.header {
  background-color: #409eff;
  color: #fff;
  line-height: 20px;
}
.content {
  font-size: 16px;
}
</style>
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
        <el-main style="padding-top:10px">
          <el-row>
            <el-col :span="23" :offset="1">
              <el-table :data="receiptList" class="content">
                <el-table-column prop="id" label="票据编号" min-width="10%"></el-table-column>
                <el-table-column prop="senderAddress" label="发送人地址" min-width="30%"></el-table-column>
                <el-table-column prop="accepterAddress" label="接收人地址" min-width="30%"></el-table-column>
                <el-table-column prop="amount" label="交易量" min-width="10%"></el-table-column>
                <el-table-column prop="receiptType" label="票据类型" min-width="10%"></el-table-column>
                <el-table-column prop="transferType" label="交易类型" min-width="10%"></el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Navigator from "@/components/Navigator";
import Header from '@/components/Header';
// import config from '@/config'
export default {
  name: "Home",
  data() {
    return {
      receiptList: []

    }
  },
  components: {
    Navigator,
    Header
  },
  methods: {
    query: function() {
      let address = this.$cookies.get('address')
      this.axios.get("/finance/query/listAllReceipt?address="+address).then((response) => {
        console.log(response)
        if (response.data.code == 200) {
          this.receiptList = response.data.data
        }else {
          alert(`请求内容有误, ${response.data.data}`)
        }
      })
    },
  },
  mounted() {
    this.query()
  },
  created() {
    console.log("******* in home ******")
    console.log(this.$cookies.get("token"))
    console.log(this.$cookies.get("username"))
  }
}
</script>

<style>
.header {
  background-color: #409EFF;
  color: #fff;
  line-height: 20px;
}
.content {
  font-size: 16px;
}
</style>
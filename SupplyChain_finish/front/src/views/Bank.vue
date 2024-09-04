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
          <el-col :span="20" :offset="2">
            <el-table :data="bankList" style="font-size: 20px">
              <el-table-column prop="address" label="账户地址"></el-table-column>
              <el-table-column prop="name" label="公司名称"></el-table-column>
              <el-table-column prop="amount" label="账户总额"></el-table-column>
              <el-table-column label="查看详情">
                <template slot-scope="scope">
                  <el-button type="primary" @click="detail(scope.row.address)">查询</el-button>
                </template>
              </el-table-column>
              <el-table-column prop="receiptType" label="转账">
                <template slot-scope="scope">
                  <el-button type="primary" @click="transfer(scope.row)">操作</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>

        <el-row>
      <el-dialog title="银行详情" :visible.sync="dialogVisible" width="50%">
        <el-form label-width="100px">
          <el-form-item label="账户地址:">
            {{bankDetail.address}}
          </el-form-item>
          <el-form-item label="银行名称:">
            {{bankDetail.name}}
          </el-form-item>
          <el-form-item label="账户总额:">
            {{bankDetail.amount}}
          </el-form-item>
        </el-form>
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="发送凭证列表" name="1">
            <el-table :data="bankDetail.senderReceiptList" style="font-size: 15px">
              <el-table-column prop="id" label="票据编号" ></el-table-column>
              <el-table-column prop="senderAddress" label="发送人地址"></el-table-column>
              <el-table-column prop="accepterAddress" label="接收人地址"></el-table-column>
              <el-table-column prop="amount" label="交易量"></el-table-column>
              <el-table-column prop="receiptType" label="票据类型"></el-table-column>
              <el-table-column prop="transferType" label="票据交易类型"></el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="接收凭证列表" name="2">
            <el-table :data="bankDetail.accepterReceiptList" style="font-size: 15px">
              <el-table-column prop="id" label="票据编号" ></el-table-column>
              <el-table-column prop="senderAddress" label="发送人地址"></el-table-column>
              <el-table-column prop="accepterAddress" label="接收人地址"></el-table-column>
              <el-table-column prop="amount" label="交易量"></el-table-column>
              <el-table-column prop="receiptType" label="票据类型"></el-table-column>
              <el-table-column prop="transferType" label="票据交易类型"></el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>
    </el-row>
    <el-row>
      <el-dialog title="交易（发送凭证）页" :visible.sync="transDialogVisible" width="30%">
        <el-form label-width="100px">
          <el-form-item label="发送账户地址:">
            {{transDetail.senderAddress}}
          </el-form-item>
          <el-form-item label="接收账户地址:">
            {{transDetail.accepterAddress}}
          </el-form-item>
          <el-form-item label="交易额:">
            <el-col :span="16" :offset="4">
              <el-input type="primary" v-model="transDetail.amount"></el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="凭证类型:">
            <el-select v-model="transDetail.receiptType" placeholder="请选择">
              <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <el-row>
          <el-button type="primary" size="medium" @click="executeTransaction">确定</el-button>
        </el-row>
      </el-dialog>
    </el-row>

        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Navigator from "@/components/Navigator";
import Header from '@/components/Header';

export default {
  name: "Bank",
  data() {
    return {
      options: [{
        value: '1',
        label: '凭证'
      }, {
        value: '2',
        label: '现金'
      }],
      transDialogVisible:false,
      dialogVisible:false,
      bankList: [],
      activeTab:'1',
      transDetail: {
        senderAddress:'',
        accepterAddress:'',
        amount:'',
        receiptType:'1'
      },
      bankDetail: {
        address:'',
        name:'',
        amount:'',
        senderReceiptList:[],
        accepterReceiptList:[]
      }
    }
  },
  components: {
    Navigator,
    Header
  },
  methods: {
    goback: function () {
      this.$router.push('/home')
    },
    detail: function(queryAddress) {
      this.dialogVisible=true
      let address = this.$cookies.get('address')
      this.axios.get(`/finance/query/getBankEntity?address=${address}&queryAddress=${queryAddress}`).then((response) => {
        console.log(response)
        if (response.data.code == 200) {
          let inAddress = response.data.data.bankVO.address;
          let inName = response.data.data.bankVO.name;
          let inAmount = response.data.data.bankVO.amount;
          this.bankDetail = {
            address:inAddress,
            name:inName,
            amount:inAmount,
            senderReceiptList:response.data.data.senderReceiptList,
            accepterReceiptList:response.data.data.accepterReceiptList
          }
        }else {
          alert(`请求内容有误, ${response.data.data}`)
        }
      })
    },
    transfer: function(row){
      this.transDialogVisible = true
      this.transDetail.senderAddress = row.address;
      this.transDetail.accepterAddress = this.$cookies.get('address')
    },
    executeTransaction: function() {
      let funcName = "companyToBankReceipt";
      if (this.transDetail.amount =='') {
        alert('交易额不能为空！')
        return
      }
      if (this.$cookies.get('orgType') == 2) {
        alert('银行不能给银行发送凭证！')
        return
      }
      if (this.transDetail.accepterAddress == this.transDetail.senderAddress) {
        alert("凭证发送账户和接收账户不能相一致！")
        return
      }
      this.axios.post(`/finance/transaction/${funcName}`, this.transDetail).then((response) => {
        if (response.data.code == 200) {
          alert('凭证发送成功')
          this.query()
          this.transDialogVisible = false
        }else {
          alert(`凭证发送失败, ${response.data.data}`)
        }
      })
    },
    query: function() {
      let address = this.$cookies.get('address')
      this.axios.get("/finance/query/listBank?address="+address).then((response) => {
        
      })
    }
  },
  mounted() {
    
  }
}
</script>

<style scoped>
.header {
  background-color: #409EFF;
  color: #fff;
  line-height: 20px;
}
.content {
  font-size: 16px;
}
</style>
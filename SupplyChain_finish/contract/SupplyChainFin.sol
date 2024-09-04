pragma solidity ^0.4.22;
import './Ownable.sol';

//供应链金融智能合约
contract SupplyChainFin is Ownable {
    
    //监管者信息结构体
    struct Supervisor {
        string supervisorName;
        address supervisorAddress;
    }

    //公司信息结构体
    struct Company {
        string companyName;
        address companyAddress;
        uint creditAsset;
        uint[] acceptReceiptIndex;
        uint[] sendReceiptIndex;
    }

    //银行信息结构体
    struct Bank {
        string bankName;
        address bankAddress;
        uint creditAsset;
        uint[] acceptReceiptIndex;
        uint[] sendReceiptIndex;

    }

    //数字发票收据信息
    struct Receipt {
        address senderAddress;
        address accepterAddress;
        uint8 receiptType;
        uint8 transferType;
        uint amount;
    }
    
    //公司的Map，用于快速搜索
    mapping(address => Company) companyMap;
    
    //银行的Map，用于快速搜索
    mapping(address => Bank) bankMap;
    
    //发票的Map，用于快速搜索
    mapping(uint => Receipt) receiptMap;
    
    //监管者实体
    Supervisor public superviosrIns;

    //公司地址的数组
    address[] public companies;

    //银行地址的数组
    address[] public banks;

    //数字发票的索引
    uint public receiptIndex;
    
    //构造函数
    constructor(string name) {
        receiptIndex = 0;
        superviosrIns = Supervisor(name, msg.sender);
    }
    
    
    function addCompany(string name, address companyAddress) returns(bool) {
        Company memory newCompany = Company(name, companyAddress, 0, new uint[](0), new uint[](0));
        companies.push(companyAddress);
        companyMap[companyAddress] = newCompany;
        return true;
    }
    
    function getCompany(address companyAddress) public view returns(string, address, uint, uint[], uint[]) {
        Company company = companyMap[companyAddress];
        return (company.companyName, company.companyAddress, company.creditAsset, company.acceptReceiptIndex, company.sendReceiptIndex);
    }
    
    function addBank(string name, address bankAddress, uint credit) public onlyOwner {
        Bank memory newBank = Bank(name, bankAddress, credit, new uint[](0), new uint[](0));
        banks.push(bankAddress);
        bankMap[bankAddress] = newBank;
    }
    
    function getBank(address bankAddress) public view returns(string, address, uint, uint[], uint[]){
        Bank bank = bankMap[bankAddress];
        return (bank.bankName, bank.bankAddress, bank.creditAsset, bank.acceptReceiptIndex, bank.sendReceiptIndex);
    }
    
    function getAllCompanyAddress() public view returns(address[]) {
        return companies;
    }
    
    function getAllBankAddress() public view returns(address[]) {
        return banks;
    }
    
    function getReceipt(uint index) public view returns(address, address, uint8, uint8, uint) {
        Receipt receipt = receiptMap[index];
        return (receipt.senderAddress, receipt.accepterAddress, receipt.receiptType, receipt.transferType, receipt.amount);
    }

    //存证交易
    // receiptType: 发票类型(存证、现金)
    //1: 交易类型为存证
    //2：交易类型为现金
    // transferType: 交易类型
    //1: 银行转账给公司 
    //2: 公司与公司间转账
    //3: 公司转账给银行
    
    //银行向公司交易（公司向银行提供交易存证）
    function bankToCompanyReceipt(address senderAddress, address accepterAddress, uint amount, uint8 receiptType) returns(uint) {
       
       //transferType :1 银行转账给公司
       //必须只能接受人创建此交易，也就是接受人承认这笔交易存在
        require(msg.sender == accepterAddress);
        Bank memory bank = bankMap[accepterAddress];
        Company memory company = companyMap[senderAddress];
        
        //确认银行存在
        if (keccak256(bank.bankName) == keccak256("")) {
            return 404001;
        }
        
        //确认公司存在
        if (keccak256(company.companyName) == keccak256("")) {
            return 404002;
        }
        
        if (bank.creditAsset < amount) {
            return 500001;
        }
        
        Receipt memory newReceipt = Receipt(senderAddress, accepterAddress, receiptType, 1, amount);
        receiptIndex += 1;
        receiptMap[receiptIndex] = newReceipt;
        companyMap[senderAddress].sendReceiptIndex.push(receiptIndex);
        bankMap[accepterAddress].acceptReceiptIndex.push(receiptIndex);
        bankMap[accepterAddress].creditAsset -= amount;
        companyMap[senderAddress].creditAsset += amount;
        return 200;
    
    }
    
    
    //公司与公司交易
    //接收存证的公司需要给发送存证的公司转账存证对应的数额
    function companyToCompanyReceipt(address senderAddress, address accepterAddress, uint amount, uint8 receiptType) returns(uint) {
        
        //transferType :2 公司与公司间转账
        //存证接收的公司可以产生这一笔交易
        require(msg.sender == accepterAddress);
        
        Company memory senderCompany = companyMap[senderAddress];
        Company memory accepterCompany = companyMap[accepterAddress];
        //确认发送公司存在
        if (keccak256(senderCompany.companyName) == keccak256("")) {
            return 404001;
        }
        
        //确认接收公司存在
        if (keccak256(accepterCompany.companyName) == keccak256("")) {
            return 404002;
        }
        
        //如果存证接收的公司资产小于存证数额，那么就不能交易发送存证
        if (accepterCompany.creditAsset < amount) {
            return 500001;
        }
        
        //创建存证
        Receipt memory newReceipt = Receipt(senderAddress, accepterAddress, receiptType, 2, amount);
        receiptIndex += 1;
        //记录存证（存证Map，公司Map对应地址的发送和接收存证列表）
        receiptMap[receiptIndex] = newReceipt;
        companyMap[senderAddress].sendReceiptIndex.push(receiptIndex);
        companyMap[accepterAddress].acceptReceiptIndex.push(receiptIndex);
        
        companyMap[senderAddress].creditAsset += amount;
        companyMap[accepterAddress].creditAsset -= amount;
        return 200;
    }
    
    
    //公司与银行交易
    function companyToBankReceipt(address senderAddress, address accepterAddress, uint amount, uint8 receiptType) returns(uint) {

        //transferType :3 公司向银行转账
        //存证接收的公司可以产生这一笔交易
        require(msg.sender == accepterAddress);
        
        Bank memory bank = bankMap[senderAddress];
        Company memory accepterCompany = companyMap[accepterAddress];
        
        //确认发送公司存在
        if (keccak256(bank.bankName) == keccak256("")) {
            return 404001;
        }
        
        //确认接收公司存在
        if (keccak256(accepterCompany.companyName) == keccak256("")) {
            return 404002;
        }
        
        //如果存证接收的公司资产小于存证数额，那么就不能交易发送存证
        if (accepterCompany.creditAsset < amount) {
            return 500001;
        }
        
        //创建存证
        Receipt memory newReceipt = Receipt(senderAddress, accepterAddress, receiptType, 3, amount);
        receiptIndex += 1;
        //记录存证（存证Map，公司Map对应地址的发送和接收存证列表）
        receiptMap[receiptIndex] = newReceipt;
        bankMap[senderAddress].sendReceiptIndex.push(receiptIndex);
        companyMap[accepterAddress].acceptReceiptIndex.push(receiptIndex);
        bankMap[senderAddress].creditAsset += amount;
        companyMap[accepterAddress].creditAsset -= amount;
        return 200;
        
    }

}
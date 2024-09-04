const TimeLock=artifacts.require("TimeLock");
const Attack=artifacts.require("Attack");

module.exports=async function(deployer,network,accounts){
    await deployer.deploy(TimeLock);
    const a=await TimeLock.deployed();
    await deployer.deploy(Attack,a.address);
}
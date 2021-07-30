pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;
// 支持有资产的账户
contract IExternalAccountHolder {
    function transfer(address asset,address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2]);

    function getBalance(address asset,bytes32[4] sign) public constant returns (uint256);


}

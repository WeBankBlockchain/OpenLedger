pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./BaseIdentity.sol";

/**  @title standardAssetsDefineInterfaces */
contract IStandardAsset is BaseIdentity {
    // openAnAccount
    function openAccount(address account, bytes32[4] sign) onlyAccountNormal(account) public returns (bool);

    //    queryAccountList
    function getHolders(bytes32[4] sign) public constant returns (address[]);

    //    balance of account
    function getBalance(address account, bytes32[4] sign) public constant returns (uint256);

    function deposit(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2]);

    function withdrawal(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2]);

    function transfer(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2]);

    function getAddress() public constant returns (address);

    function queryBook(uint[] uint_condition, address[] address_condtion, bytes32[4] sign) public constant returns (string[] memory);

    function addBook(bytes32[4] sign) public returns (uint256);

    function getTotalBalance(bytes32[4] sign) public constant returns (uint256);

}
pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract IAssetManager {
    function createAsset(string assetName) public returns (bool, address);

    function setPrice(address asset, uint256 priceVal) public returns (uint256);

    function setRate(address asset, uint256 rateVal) public returns (uint256);

    function getHolders(address asset) public constant returns (address[]);

    function getBalance(address asset, address account) public constant returns (uint256);

    function registerAccount(address asset, address account) public returns (bool);

    function deposit(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList) public returns (bool, uint[2]);

    function withdrawal(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList) public returns (bool, uint[2]);

    function transfer(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList) public returns (bool, uint[2]);

    function queryBook(address asset, uint[] uintCondition, address[] addressCondition, int[] limit) public constant returns (string[] memory);

    function addBook(address asset) public returns (uint256);

    function getTotalBalance(address asset) public constant returns (uint256);
}
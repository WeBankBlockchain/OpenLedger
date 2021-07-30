pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./lib/UtilLib.sol";
import "./asset/interface/IAssetManager.sol";
import "./asset/fungible/FungibleAsset.sol";
import "./asset/interface/IBaseAsset.sol";

contract FungibleAssetManager is IAssetManager {
    using UtilLib for *;

    modifier onlyCustodian(address asset){
        require(IBaseAsset(asset).isCustodian(msg.sender), "FungibleAssetManager:required custodian called!");
        _;
    }
    modifier onlyAccountHolder(address asset){
        require(IBaseAsset(asset).containsAccount(msg.sender), "FungibleAssetManager:required accountHolder called!");
        _;
    }
    modifier onlyCustodianOrHolder(address asset){
        require(IBaseAsset(asset).containsAccount(msg.sender)||IBaseAsset(asset).isCustodian(msg.sender), "FungibleAssetManager:required accountHolder or custodian called!");
        _;
    }

    function createAsset(string assetName) public returns (bool, address){
        address assetAddr;
        FungibleAsset asset = new FungibleAsset(assetName, msg.sender,msg.sender);
        assetAddr = asset.getAddress();

        if (address(0) == assetAddr) {
            return (false, address(0));
        }
        return (true, assetAddr);
    }

    function setPrice(address asset, uint256 priceVal) public onlyCustodian(asset) returns (uint256){
        return FungibleAsset(asset).setPrice(priceVal);
    }

    function setRate(address asset, uint256 rateVal) public onlyCustodian(asset) returns (uint256){
        return FungibleAsset(asset).setRate(rateVal);
    }

    function getHolders(address asset) public onlyCustodian(asset) constant returns (address[]){
        return FungibleAsset(asset).getHolders();
    }

    function getBalance(address asset,address account) public onlyCustodianOrHolder(asset) constant returns (uint256){
        return FungibleAsset(asset).getBalance(account);
    }

    function registerAccount(address asset, address account) public onlyCustodian(asset) returns (bool){
        return FungibleAsset(asset).openAccount(account);
    }

    function deposit(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList) public onlyCustodian(asset) returns (bool, uint[2]){
        return FungibleAsset(asset).deposit(transactionAddress, amount, typeList, detailList);
    }

    function withdrawal(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList) public onlyCustodian(asset) returns (bool, uint[2]){
        return FungibleAsset(asset).withdrawal(transactionAddress, amount, typeList, detailList);
    }

    function transfer(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList) public onlyAccountHolder(asset) returns (bool, uint[2]){
        return FungibleAsset(asset).transfer(transactionAddress, amount, typeList, detailList);
    }

    function queryBook(address asset,uint[] uintCondition, address[] addressCondition, int[] limit) public onlyCustodianOrHolder(asset) constant returns (string[] ){
        return FungibleAsset(asset).queryBook(uintCondition, addressCondition, limit);
    }

    function addBook(address asset) public onlyCustodian(asset) returns (uint256){
        return FungibleAsset(asset).addBook();
    }

    function getTotalBalance(address asset) public constant onlyCustodian(asset) returns (uint256){
        return FungibleAsset(asset).getTotalBalance();
    }
}
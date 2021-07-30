pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "../../asset/interface/IAssetManager.sol";
import "../../asset/interface/INonFungibleManager.sol";
import "../../asset/interface/IBaseAsset.sol";
import "../../project/interface/IProject.sol";
import "../../auth/interface/IResource.sol";
import "../../storage/AddressSetLib.sol";
import "../../lib/UtilLib.sol";
import "../../govAccount/BaseAccount.sol";


contract BaseAccountHolder  is BaseAccount {
    using UtilLib for *;
    using AddressSetLib for AddressSetLib.Set;

    modifier checkAsset(address asset) {
        require(ownNonFungibleAssetList.contains(asset), "BaseAccountHolder:not contains asset");
        _;
    }
    modifier onlyHolder(address asset, string operation, bytes32[4] sign)  {
        require(IResource(address(this)).checkHolderByAddress(asset, operation, sign), "BaseAccountHolder:Forbbiden ".strConcat(operation));
        _;
    }

    // account's asset
    AddressSetLib.Set ownNonFungibleAssetList;
    AddressSetLib.Set ownFungibleAssetList;
    //assetsManagement
    IAssetManager assetManager;
    INonFungibleManager nonFungibleAssetManager;
    address myProject;
    constructor(address _project, address _holder, address accountManager, address accountAdmin) public  BaseAccount(accountManager,accountAdmin) {
        assetManager = IAssetManager(IProject(_project).getFingibleAssetManager());
        nonFungibleAssetManager = INonFungibleManager(IProject(_project).getNonFingibleAssetManager());
        myProject = _project;
    }

    function addAsset(bool isFungible, address asset) public returns (bool) {
        if (isFungible) {
            return ownFungibleAssetList.insert(asset);
        } else {
            return ownNonFungibleAssetList.insert(asset);
        }
    }

    function getAccountAssets(bool isFungible, bytes32[4] sign) public onlyHolder(address(0), "getAccountAssets", sign) view returns (address[]) {
        if (isFungible) {
            return ownFungibleAssetList.getAll();
        } else {
            return ownNonFungibleAssetList.getAll();
        }
    }

    // nonfungible asset
    function transfer(address asset, address[] transactionAddress, uint[] noteNos, bytes[] stringValueList, bytes32[4] sign) public onlyHolder(asset, "transfer", sign) returns (bool, uint[]){
        return nonFungibleAssetManager.transfer(asset, transactionAddress, noteNos, stringValueList);

    }

    function accountHoldNote(address asset, uint256 noteNo, bytes32[4] sign) public onlyHolder(asset, "accountHoldNote", sign) constant returns (bool isContain){
        isContain = nonFungibleAssetManager.accountHoldNote(asset,noteNo);
    }

    function getAccountNotes(address asset, uint256 start, uint256 end, bytes32[4] sign) public onlyHolder(asset, "getAccountNotes", sign) constant returns (uint256[]){
        return nonFungibleAssetManager.getAccountNotes(asset,address(this), start, end);
    }

    // fungible asset
    function transfer(address asset, address[] transactionAddress, uint256 amount, int[] typeList, bytes[] detailList, bytes32[4] sign) public onlyHolder(asset, "transfer", sign) returns (bool, uint[2]){
        return assetManager.transfer(asset, transactionAddress, amount, typeList, detailList);
    }

    function queryBook(address asset, uint[] uintCondition, address[] addressCondition, int[] limit, bytes32[4] sign) public onlyHolder(asset, "queryBook", sign) constant returns (string[] memory){
        if (IBaseAsset(asset).boolFungible()) {
            return assetManager.queryBook(asset, uintCondition, addressCondition, limit);
        } else {
            return nonFungibleAssetManager.queryBook(asset, uintCondition, addressCondition, limit);
        }
    }

    function getBalance(address asset, bytes32[4] sign) public onlyHolder(asset, "getBalance", sign) constant returns (uint256){
        return assetManager.getBalance(asset, address(this));
    }


}

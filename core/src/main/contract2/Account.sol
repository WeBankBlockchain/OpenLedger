pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./auth/BaseResource.sol";
import "./auth/storage/AddressMapLib.sol";
import "./auth/lib/LibTypeConversion.sol";

contract Account is BaseResource
{
    using AddressMapLib for AddressMapLib.Map;
    using LibTypeConversion for *;
    AddressMapLib.Map fungibleAssetList;
    AddressMapLib.Map nonFungibleAssetList;
    address org;

    event logGet(address, address, address, bytes);
    modifier onlyAssetCall(address asset) {
        require(msg.sender == asset, "Account: only asset is authorized.");
        _;
    }

    constructor(address creatorId, address orgAddr) public  BaseResource(creatorId){
        holder = creatorId;
        org = orgAddr;
    }

    function addAsset(address asset, address org, bool isFungible) public onlyAssetCall(asset) returns (bool) {
        if (isFungible) {
            return fungibleAssetList.insert(asset, org);
        } else {
            return nonFungibleAssetList.insert(asset, org);
        }
    }

    // SDK call: Account.grant(ddress(externalId),getAllAssets,"",sign);
    function getAllAssets(bool isFungible, bytes32[4] sign) public view returns (address[], address[], uint) {
        require(checkAuth(sign, "getAllAssets", ""), "Account:Forbbiden getAllAsset!");
        if (isFungible) {
            return fungibleAssetList.getAll();
        } else {
            return nonFungibleAssetList.getAll();
        }
    }
    // SDK call: Account.grant(address(externalId),issue,address(asset),sign);
    function issue(address asset, address[] transactionAddress, uint256[] uint256Args, string[] stringValueList, bytes32[4] sign) public returns (bool){
        require(checkAuth(sign, "issue", asset.addressToString()), "Account:Forbbiden issue!");
        return true;
    }

    // SDK call: Account.grant(address(externalId),getBalance,address(asset),sign);
    function getBalance(address asset, bytes32[4] sign) public view returns (uint256){
        require(checkAuth(sign,  "getBalance", asset.addressToString()), "Account:Forbbiden getBalance!");
        return 0;
    }



}
    
    
    
    
    
    

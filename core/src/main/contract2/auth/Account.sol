pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./storage/AddressMapLib.sol";

contract Account
{
    using AddressMapLib for AddressMapLib.Map;

    AddressMapLib.Map fungibleAssetList;
    AddressMapLib.Map nonFungibleAssetList;


    event logGet(address, address, address, bytes);

    function Account(address authCenterAddr, address orgAddr) Identity(authCenterAddr, orgAddr) public {
    }

    function addAsset(address asset, address org, bool isFungible) public returns (bool) {
        if (isFungible) {
            return fungibleAssetList.insert(asset, org);
        } else {
            return nonFungibleAssetList.insert(asset, org);
        }
    }

    function getAllAssets(bool isFungible) public view returns (address[], address[], uint) {
        emit logGet(msg.sender, getOrg(), this, "getAllAssets");
        if (isFungible) {
            return fungibleAssetList.getAll();
        } else {
            return nonFungibleAssetList.getAll();
        }
    }

    function addAssetWithSign(address asset, address org, bool isFungible, bytes args, bytes32[4] sign) public returns (bool) {
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(getOrg(), this, "addAsset", args, sign);
        require(check, "addAsset Forbidden!");
        if (isFungible) {
            return fungibleAssetList.insert(asset, org);
        } else {
            return nonFungibleAssetList.insert(asset, org);
        }
    }

    function getAllAssetsWithSign(bool isFungible, bytes32[4] sign) public view returns (address[], address[], uint) {
        bytes memory args;

        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(getOrg(), this, "getAllAssets", args, sign);
        require(check, "getAllAssets Forbidden!");
        if (isFungible) {
            return fungibleAssetList.getAll();
        } else {
            return nonFungibleAssetList.getAll();
        }
    }
}    
    
    
    
    
    
    

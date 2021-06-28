pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./BaseIdentity.sol";

contract IAccount is BaseIdentity
{
    function addAsset(address asset, address org, bool isFungible) public returns (bool);

    function getAllAssets(bool isFungible) public view returns (address[], address[], uint);

    function addAssetWithSign(address asset, address org, bool isFungible, bytes args, bytes32[4] sign) public returns (bool) ;

    function getAllAssetsWithSign(bool isFungible, bytes32[4] sign) public view returns (address[], address[], uint) ;

}
    
    
    
    
    
    

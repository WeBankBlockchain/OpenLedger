pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

//import "./BaseIdentity.sol";

contract IAccount
{
    //账户账号
    function getAccountID() external returns(bytes32);

    //账户所有人Identity对象
    function getHolder() external returns(bytes32);
    function addAsset(address asset, address org, bool isFungible) public returns (bool);

    function getAllAssets(bool isFungible) public view returns (address[], address[], uint);

    function addAssetWithSign(address asset, address org, bool isFungible, bytes args, bytes32[4] sign) public returns (bool) ;

    function getAllAssetsWithSign(bool isFungible, bytes32[4] sign) public view returns (address[], address[], uint) ;

}
    
    

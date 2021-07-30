pragma solidity ^0.4.25;

contract IAssetPoolManager {

    //创建资产池
    function createAssetPool(address authCenterAddr, address orgAddr) public returns (address);
}
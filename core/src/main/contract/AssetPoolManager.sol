pragma solidity ^0.4.25;

import "./AssetPool.sol";


contract AssetPoolManager {

    function createAssetPool(address authCenterAddr, address orgAddr) public returns (address){
        AssetPool assetPool = new AssetPool(authCenterAddr, orgAddr);
        return address(assetPool);
    }

}


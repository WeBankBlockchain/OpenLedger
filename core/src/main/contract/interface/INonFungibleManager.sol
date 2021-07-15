pragma solidity ^0.4.0;

import "./IAssetManager.sol";

contract INonFungibleManager is IAssetManager {

    function upgradeAsset(address externalAccount, string assetName, bytes32[4] sign) public returns (bool, address);

    function checkStorageCallee(address sender, address assetStorage) public view returns (bool);

    function listAssetVersion(string assetName) public view returns (uint256[], address[]);

    function checkUpgrade(string assetName) public view returns (bool);

}

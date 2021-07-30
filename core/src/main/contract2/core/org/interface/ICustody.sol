pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;
//托管人接口
contract ICustody
{
    function registerAsset(string assetName, bool isFungible) external returns (address, bool);

    function upgradeAsset(string assetName, bool isFungible, bytes32[4] sign) external returns (address, bool);

    function getAllAssets(bool isFungible) external view returns (address[], address[], uint);

    function listAssetVersion(string assetName) external view returns (uint256[], address[]);

    function createCurrency(string currencyName, string currencySymbol, uint8 decimals) external returns (address);

    function createAssetPool() external returns (address);

    function getAssetPools() external returns (address[]);
}
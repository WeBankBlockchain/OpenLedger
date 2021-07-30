pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;
// 支持有资产的账户
contract IAccountHolder {
    function getAccountAssets(bool isFungible, bytes32[4] sign) external view returns (address[]);

    function addAsset(bool isFungible, address asset) external returns (bool);

}

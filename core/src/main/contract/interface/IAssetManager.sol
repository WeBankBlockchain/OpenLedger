pragma solidity ^0.4.25;

contract IAssetManager {

    function createAssetWithSign(address externalAccount, string assetName, bytes32[4] sign, address authcenterAddress, address orgAddress) public returns (bool, address);


}
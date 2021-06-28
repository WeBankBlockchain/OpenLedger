pragma solidity ^0.4.25;

import "./FungibleAsset.sol";
import "./interface/IAuthCenter.sol";
import "./lib/UtilLib.sol";


contract FungibleAssetManager {
    using UtilLib for *;



    function createAssetWithSign(address externalAccount, string assetName, bytes32[4] sign, address authcenterAddress, address orgAddress) public returns (bool, address){
        checkAuth(externalAccount, authcenterAddress, orgAddress, sign);
        address assetAddr;
        FungibleAsset asset = new FungibleAsset(assetName, authcenterAddress, orgAddress);
        assetAddr = asset.getAddress();

        if (address(0) == assetAddr) {
            return (false, address(0));
        }

        return (true, assetAddr);
    }

    function checkAuth(address externalAccount, address authcenterAddress, address orgAddress, bytes32[4] sign) internal {
        bytes memory args;
        args = args.bytesAppend(externalAccount);

        address txOrigin;
        bool check;
        (txOrigin, check) = IAuthCenter(authcenterAddress).check2WithSign(orgAddress, this, "createAsset", args, sign);
        require(check, "createAsset Forbidden!");
    }

}
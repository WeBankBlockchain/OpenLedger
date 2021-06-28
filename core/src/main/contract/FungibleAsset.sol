pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./StandardAsset.sol";

contract FungibleAsset is StandardAsset {

    // price of fungibleasset
    uint256 public price;
    // rate of fungibleasset
    uint256 public rate;

    constructor(string tableName, address authCenterAddr, address orgAddr) public StandardAsset(tableName, authCenterAddr, orgAddr)
    {
    }
    function setPrice(uint256 priceVal, bytes32[4] sign) public returns (uint256){
        bytes memory args;
        args = args.bytesAppend(priceVal);
        address txOrigin;
        bool check;
        (txOrigin, check) = authCenter.check2WithSign(org, this, "setPrice", args, sign);
        require(check, "setPrice Forbidden");


        price = priceVal;
        return price;
    }

    function setRate(uint256 rateVal, bytes32[4] sign) public returns (uint256){
        bytes memory args;
        address txOrigin;
        bool check;
        args = args.bytesAppend(rateVal);
        (txOrigin, check) = authCenter.check2WithSign(org, this, "setRate", args, sign);
        require(check, "setRate Forbidden");

        rate = rateVal;
        return rate;
    }


}
pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./StandardAsset.sol";

contract FungibleAsset is StandardAsset {
    // price of fungibleasset
    uint256 public price;
    // rate of fungibleasset
    uint256 public rate;

    constructor(string tableName, address orgAddr,address _custodian) public StandardAsset(tableName, orgAddr)
    {
    }
    function setPrice(uint256 priceVal) public onlyManager returns (uint256){
        price = priceVal;
        return price;
    }

    function setRate(uint256 rateVal) public onlyManager returns (uint256){
        rate = rateVal;
        return rate;
    }


}
pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

import "./StandardAsset.sol";

/** @title  currency ，inheritingStandardAssets*/
contract Currency is StandardAsset {
    // CODE eg：ABC
    string public  symbol;
    // NAME
    string public  name;
    // precision
    uint8  public decimals;

    constructor(string currencyName, string currencySymbol, uint8 _decimals, address authCenterAddr, address orgAddr)  StandardAsset(currencyName, authCenterAddr, orgAddr) public
    {
        name = currencyName;
        symbol = currencySymbol;
        decimals = _decimals;
    }


    //deposit computationalAccuracy
    function deposit(address[] transactionAddress, uint256 amount, int[] typeList, string[] detailList, bytes32[4] sign) public returns (bool, uint[2])
    {
        amount = amount * 10 ** uint256(decimals);
        return super.deposit(transactionAddress, amount, typeList, detailList, sign);
    }


}

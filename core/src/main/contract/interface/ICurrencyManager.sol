pragma solidity ^0.4.25;

contract ICurrencyManager {

    function createCurrency(string currencyName, string currencySymbol, uint8 decimals, address authCenterAddr, address orgAddr) public returns (address);
}
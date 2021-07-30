pragma solidity ^0.4.25;

contract ICurrencyManager {

    function createCurrency(string currencyName, string currencySymbol, uint8 decimals, address orgAddr) public returns (address);
}
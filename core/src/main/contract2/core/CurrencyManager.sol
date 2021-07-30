pragma solidity ^0.4.25;
import "./asset/fungible/Currency.sol";



contract CurrencyManager {

    function createCurrency(string currencyName, string currencySymbol, uint8 decimals, address authCenterAddr, address orgAddr) public returns (address){
        Currency currency = new Currency(currencyName, currencySymbol, decimals,orgAddr);
        return address(currency);
    }

}


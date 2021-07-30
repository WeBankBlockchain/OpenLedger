pragma solidity ^0.4.24;

pragma experimental ABIEncoderV2;

import "./UtilLib.sol";


contract TestUtilLib
{
    using UtilLib for *;

    function t_isStrEmpty(string s) public returns (bool){
        return s.isStrEmpty1();
    }

    function t_strEQ(string a, string b) returns (bool){
        return a.strEQ(b);
    }

    function t_addr2bytes(address a) returns (bytes){
        return a.addr2bytes();
    }
}
pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./AddressSetLib.sol";

contract TestAddressSetLib {
    using AddressSetLib for AddressSetLib.Set;
    AddressSetLib.Set s;

    function t_insert0() public returns (bool){
        s.insert(0x1);
        s.insert(0x2);
        s.insert(0x3);
        return true;
    }

    function t_insert(address addr) public returns (bool){
        return s.insert(addr);
    }

    function t_has(address addr) public returns (bool){
        return s.contains(addr);
    }

    function t_remove(address addr) public returns (bool){
        return s.remove(addr);
    }

    function t_get(uint i) public returns (address){
        return s.get(i);
    }

    function t_geti(address a) public returns (uint){
        return s.geti(a);
    }

    function t_getall() public returns (address[]){
        return s.getAll();
    }

    function t_log() public returns (address[], uint[]) {
        return s.log();
    }
}
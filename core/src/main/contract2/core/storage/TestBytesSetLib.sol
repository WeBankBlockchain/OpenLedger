pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "../BytesSetLib.sol";

contract TestBytesSetLib {
    using BytesSetLib for BytesSetLib.Set;
    BytesSetLib.Set s;

    function t_insert0() public returns (bool){
        s.insert("0x1");
        s.insert("0x2");
        s.insert("0x3");
        return true;
    }

    function t_insert(bytes addr) public returns (bool){
        return s.insert(addr);
    }

    function t_has(bytes addr) public returns (bool){
        return s.contains(addr);
    }

    function t_remove(bytes addr) public returns (bool){
        return s.remove(addr);
    }

    function t_get(uint i) public returns (bytes){
        return s.get(i);
    }

    function t_geti(bytes a) public returns (uint){
        return s.geti(a);
    }

    function t_getall() public returns (bytes[]){
        return s.getAll();
    }

    function t_log() public returns (bytes[], uint[]) {
        return s.log();
    }
}
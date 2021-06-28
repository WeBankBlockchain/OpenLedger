pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "./Address2BytesMapLib.sol";

// refer: https://github.com/ethereum/dapp-bin/blob/master/library/iterable_mapping.sol
// k: address , v: address
contract TestAddress2BytesMapLib
{
    using Address2BytesMapLib for Address2BytesMapLib.Map;

    Address2BytesMapLib.Map m;

    function t_insert0() public returns (bool){
        m.insert(0x1, "0x11");
        m.insert(0x2, "0x22");
        m.insert(0x3, "0x33");
        return true;
    }

    function t_add(address k, bytes v) public returns (bool) {
        return m.add(k, v);
    }

    function t_set(address k, bytes v) public returns (bool) {
        return m.set(k, v);
    }

    function t_insert(address k, bytes v) public returns (bool){
        return m.insert(k, v);
    }

    function t_remove(address k) public returns (bool){
        return m.remove(k);
    }

    function t_has(address k) public returns (bool){
        return m.contains(k);
    }

    function t_get(address k) public returns (bytes){
        return m.get(k);
    }

    function t_size() public returns (uint){
        return m.size();
    }

    function t_insertBatch(address[] keys, bytes[] vals) public returns (uint){
        return m.insertBatch(keys, vals);
    }

    function t_getBatch(address[] keys) public returns (bytes[]){
        return m.getBatch(keys);
    }

    function t_getByIndex(uint start, uint num) constant returns (address[] memory keyList, bytes[] memory valList, uint retNum, uint nextIndex){
        return m.getByIndex(start, num);
    }

    function t_getAll() constant returns (address[] memory keyList, bytes[] memory valList, uint retNum){
        return m.getAll();
    }


}

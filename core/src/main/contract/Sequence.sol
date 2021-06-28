pragma solidity ^0.4.25;

/** 序号计算 */
contract Sequence {
    uint currentSeq;

    function get() public returns (uint) {
        currentSeq = currentSeq + 1;
        return currentSeq;
    }

    function getCurrent() public constant returns (uint){
        return currentSeq;
    }

    constructor(){
        currentSeq = 1;
    }
}

pragma solidity ^0.4.25;

contract IAuthControl {
    function canCallFunction(address contractAddr, bytes4 sig, address caller) public view returns (bool){}
}
pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract BaseResource {
    address holder;
    constructor(address account) public {
        holder = account;
    }

    function getHolder() public view returns (address){
        return holder;
    }

    function isHolder(address account) public view returns (bool){
        return holder == account;
    }
}

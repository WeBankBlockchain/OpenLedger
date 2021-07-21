pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract IResource {
    function getHolder() public view returns (address);

    function isHolder(address account) public view returns (bool);
}

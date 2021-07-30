pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract IResource {
    function getHolder() public view returns (address);

    function isHolder(address account) public view returns (bool);

    function checkHolderByAddress(address res, string operation, bytes32[4] sign) public view returns (bool);

}

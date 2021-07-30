pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract IdInterface {
    function createRole(string name, bytes32[4] sign) public returns (address);

    function createResourceGroup(bytes32[4] sign) public returns (address);

    function addIdToRole(address idAddr, address roleAddr, bytes32[4] sign) public returns (bool);

    function removeIdFromRole(address roleAddr, address idAddr, bytes32[4] sign) public returns (bool);

    function addOperationToRes(address resourceGroup, address resource, string[] operations, bytes32[4] sign) public returns (bool);

    function addDetailToRes(address resourceGroup, address resource, string operation, string detail, bytes32[4] sign) public returns (bool);

    function addResGroupToRole(address role, address resourceGroup, bytes32[4] sign) public returns (bool);

    function grant(address resource, address allowId, string operation, string detail, bytes32[4] sign) public returns (bool);

    function revoke(address resource, address allowId, string operation, string detail, bytes32[4] sign) public returns (bool);

    function check(address resource, address allowId, string operation, string detail, bytes args, bytes32[4] sign) public returns (bool);

    function check(address roleAddress, address resourceGroup, address allowId, bytes args, bytes32[4] sign) public returns (bool);

    function setExternalAddress(address _external) public;

    function getNonce() public view returns (uint256);
}
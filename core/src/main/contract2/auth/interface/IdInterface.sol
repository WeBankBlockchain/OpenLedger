pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract IdInterface {
    function createRole(string name, bytes32[4] sign) public returns (address);

    function createResourceGroup(bytes32[4] sign) public returns (address);

    function addIdToRole(address idAddr, address roleAddr, bytes32[4] sign) public returns (bool);

    function removeIdFromRole(address idAddr, address roleAddr, bytes32[4] sign) public returns (bool);

    function addOperationToRes(address resourceGroup, address resource, string[] operations, bytes32[4] sign) public returns (bool);

    function addDetailToRes(address resourceGroup, address resource, string operation, string detail, bytes32[4] sign) public returns (bool);

    function removeOperationFromRes(address resourceGroup, address resource, string operation, bytes32[4] sign) public returns (bool);

    function removeDetailTFromRes(address resourceGroup, address resource, string operation, string detail, bytes32[4] sign) public returns (bool);

    function addResGroupToRole(address role, address resourceGroup, bytes32[4] sign) public returns (bool);

    function grant(address resource, address allowId, string operation, string detail, bytes32[4] sign) public returns (bool);

    function revoke(address resource, address allowId, string operation, string detail, bytes32[4] sign) public returns (bool);

    function check(address[] addressList, string[] stringList) public view returns (bool);

    function setExternalAddress(address _external) public;

    function getExternalAddress() public view returns (address);

    function getAcl() public view returns (address);

    function getNonce() public view returns (uint256);

    function getResGroupByRole(address role) public view returns (address);

}

pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;


contract BaseIdentity
{

    function getAuthCenter() public view returns (address);

    function getAuthManager() public view returns (address) ;

    function getAccountManager() public view returns (address) ;

    function getOrg() public view returns (address) ;

    function get(bytes key) constant returns (bytes);

    function add(bytes key, bytes value) returns (bool replaced);

    function set(bytes key, bytes value) returns (bool replaced);

    function insert(bytes key, bytes value) returns (bool replaced);

    function remove(bytes key) returns (bool replaced);

    function size() constant returns (uint);

    function getWithSign(bytes key, bytes32[4] sign) constant returns (bytes);


    function addWithSign(bytes key, bytes value, bytes32[4] sign) returns (bool replaced);


    function setWithSign(bytes key, bytes value, bytes32[4] sign) returns (bool replaced);


    function insertWithSign(bytes key, bytes value, bytes32[4] sign) returns (bool replaced);


    function removeWithSign(bytes key, bytes32[4] sign) returns (bool replaced);


    function getNonce() public view returns (uint256);

    function updateNonce() public returns (uint256);


}

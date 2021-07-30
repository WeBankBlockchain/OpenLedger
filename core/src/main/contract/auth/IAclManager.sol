pragma solidity ^0.4.25;

contract IAclManager {
    function grant(address resource, address allowId, string operation, string detail) public returns (bool);

    function grant(address resource, address allowId, string operation) public returns (bool);

    function check(address resource, address allowId, string operation, string detail) public returns (bool);

    function check(address resource, address allowId, string operation) public returns (bool);

    function revoke(address resource, address allowId, string operation) public returns (bool);

    function revoke(address resource, address allowId, string operation, string detail) public returns (bool);
}
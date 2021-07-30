pragma solidity ^0.4.25;

contract IAclManager {
    function grant(address resource, address allowId, string operation, bytes detail) public returns (bool);

    function check(address resource, address allowId, string operation, bytes detail) public returns (bool);

    function revoke(address resource, address allowId, string operation, bytes detail) public returns (bool);

    function getIdByExternal(address _external) public view returns (address);

    function changeExternal(address id, address newExternal) public returns (bool);

    function initExternal(address id, address _external) public returns (bool);

}
